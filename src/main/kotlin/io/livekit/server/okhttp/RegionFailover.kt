/*
 * Copyright 2026 LiveKit, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.livekit.server.okhttp

import com.google.protobuf.util.JsonFormat
import livekit.LivekitRtc
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

// Total attempts (the original request plus fallback regions) and the base
// retry backoff are fixed, not user-configurable, so retries can't be tuned to
// values that could overwhelm the server.
internal const val FAILOVER_MAX_ATTEMPTS = 3
internal const val FAILOVER_BACKOFF_BASE_MS = 200L

// Default per-request timeout (seconds), applied fresh to each attempt. Calls
// that dial a phone (see SipServiceClient) override it via TIMEOUT_HEADER.
internal const val DEFAULT_REQUEST_TIMEOUT_SECONDS = 10

// Below this per-request timeout a retry is unlikely to help, and many clients
// would retry in lockstep across regions, so a short request gets a single
// attempt (thundering-herd guard).
internal const val MIN_FAILOVER_TIMEOUT_SECONDS = 5

// Internal header carrying a per-request timeout override (seconds). Consumed by
// the interceptor and not sent to the server.
internal const val TIMEOUT_HEADER = "X-Lk-Request-Timeout"

/**
 * Internal region-failover configuration. The public API exposes only the
 * [enabled] toggle (default true); [force] and [backoffBaseMs] are test-only.
 *
 * @property enabled whether failover is active at all.
 * @property force bypasses the cloud-host check. Internal testing only.
 * @property backoffBaseMs retry backoff base. Internal testing only.
 */
internal data class FailoverConfig(
    val enabled: Boolean = true,
    val force: Boolean = false,
    val backoffBaseMs: Long = FAILOVER_BACKOFF_BASE_MS,
) {
    /**
     * Total request attempts for a host; 1 means no failover. Failover only
     * engages when enabled, the host is a LiveKit Cloud domain, and the
     * per-attempt [timeoutSeconds] is long enough that retrying is worthwhile.
     * [force] bypasses the cloud-host check and is for internal testing only.
     */
    fun attempts(host: String, timeoutSeconds: Int): Int {
        if (!(enabled && (force || isCloud(host)))) return 1
        if (timeoutSeconds < MIN_FAILOVER_TIMEOUT_SECONDS) return 1
        return FAILOVER_MAX_ATTEMPTS
    }
}

/**
 * OkHttp interceptor that fails over to alternative LiveKit Cloud regions on a
 * retryable error. On any [IOException] or HTTP 5xx it discovers regions via
 * `/settings/regions` and replays the request — body and headers intact —
 * against the next untried region, with exponential backoff. A 4xx is returned
 * immediately.
 */
internal class RegionFailoverInterceptor(private val config: FailoverConfig) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        // A per-request timeout override (e.g. SIP dialing) travels as an internal
        // header; consume it so it isn't sent to the server. Otherwise use the default.
        val timeoutSeconds = original.header(TIMEOUT_HEADER)?.toIntOrNull() ?: DEFAULT_REQUEST_TIMEOUT_SECONDS
        val baseRequest = original.newBuilder().removeHeader(TIMEOUT_HEADER).build()
        val originalUrl = baseRequest.url
        val maxAttempts = config.attempts(originalUrl.host, timeoutSeconds)

        // Reset the full timeout budget for each attempt so a retry isn't starved
        // by time a previous, unresponsive region already consumed.
        val timedChain = chain
            .withConnectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .withReadTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .withWriteTimeout(timeoutSeconds, TimeUnit.SECONDS)

        val attempted = mutableSetOf(hostKey(originalUrl))
        var regions: List<HttpUrl>? = null
        var currentUrl = originalUrl

        for (attempt in 0 until maxAttempts) {
            val isLast = attempt == maxAttempts - 1
            val request = baseRequest.newBuilder().url(currentUrl).build()

            var response: Response? = null
            var error: IOException? = null
            try {
                response = timedChain.proceed(request)
            } catch (e: IOException) {
                error = e
            }

            // Success or a non-retryable 4xx is terminal.
            if (response != null && response.code < 500) {
                return response
            }

            var next: HttpUrl? = null
            if (!isLast) {
                if (regions == null) {
                    regions = fetchRegions(originalUrl, baseRequest.headers)
                }
                next = pickNext(regions, attempted)
            }

            if (next == null) {
                // Out of attempts or no fallback: surface the last failure.
                response?.let { return it }
                throw error ?: IOException("request failed")
            }

            val reason = response?.let { "status ${it.code}" } ?: (error?.message ?: "error")
            logger.warning(
                "livekit API request to ${currentUrl.host} failed ($reason), " +
                    "retrying with fallback url $next",
            )

            response?.close()
            sleep(config.backoffBaseMs shl attempt)
            attempted.add(hostKey(next))
            // Swap only the region's scheme/host/port, preserving the request path.
            currentUrl = originalUrl.newBuilder()
                .scheme(next.scheme)
                .host(next.host)
                .port(next.port)
                .build()
        }

        throw IOException("failover loop exited without returning") // unreachable
    }

    private fun sleep(ms: Long) {
        if (ms > 0) {
            try {
                Thread.sleep(ms)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    private fun fetchRegions(originalUrl: HttpUrl, headers: Headers): List<HttpUrl> {
        val origin = originalUrl.newBuilder().encodedPath("/settings/regions").query(null).build()
        return regionCache.get(origin, headers)
    }

    companion object {
        // Short timeout so a slow/unreachable discovery endpoint doesn't stall
        // the failover path.
        private val regionCache = RegionCache(
            OkHttpClient.Builder().callTimeout(java.time.Duration.ofSeconds(2)).build(),
        )
        private val logger = java.util.logging.Logger.getLogger(RegionFailoverInterceptor::class.java.name)
    }
}

// Auto mode only enables failover for LiveKit Cloud project domains.
internal fun isCloud(host: String): Boolean = host.endsWith(".livekit.cloud")

/** Normalizes a region URL to an http(s) scheme (ws -> http, wss -> https). */
internal fun toHttp(url: String): String =
    if (url.startsWith("ws")) "http" + url.substring(2) else url

/** A stable key identifying a host (including port) for dedup across attempts. */
internal fun hostKey(url: HttpUrl): String = "${url.host}:${url.port}".lowercase()

/** Returns the first region whose host has not yet been attempted. */
internal fun pickNext(regions: List<HttpUrl>, attempted: Set<String>): HttpUrl? =
    regions.firstOrNull { hostKey(it) !in attempted }

private data class CacheEntry(val origins: List<HttpUrl>, val fetchedAtMs: Long, val ttlMs: Long)

/**
 * Process-wide cache of the LiveKit Cloud region list, keyed by host. Best-effort:
 * on a fetch failure it serves a stale cached list when available, otherwise an
 * empty list. Forwards the caller's headers so a valid token — and any test
 * directives — reach the discovery endpoint.
 */
internal class RegionCache(private val client: OkHttpClient) {
    private val entries = ConcurrentHashMap<String, CacheEntry>()

    fun get(origin: HttpUrl, headers: Headers): List<HttpUrl> {
        val key = hostKey(origin)
        entries[key]?.let {
            if (System.currentTimeMillis() - it.fetchedAtMs < it.ttlMs) {
                return it.origins
            }
        }

        return try {
            val (origins, ttlMs) = fetch(origin, headers)
            // A zero TTL (e.g. Cache-Control: max-age=0) means "do not cache".
            if (ttlMs > 0) {
                entries[key] = CacheEntry(origins, System.currentTimeMillis(), ttlMs)
            }
            origins
        } catch (e: Exception) {
            entries[key]?.origins ?: emptyList()
        }
    }

    private fun fetch(origin: HttpUrl, headers: Headers): Pair<List<HttpUrl>, Long> {
        val builder = Request.Builder().url(origin).get()
        for (i in 0 until headers.size) {
            val name = headers.name(i)
            if (name.equals("Content-Type", true) || name.equals("Content-Length", true)) continue
            builder.addHeader(name, headers.value(i))
        }

        client.newCall(builder.build()).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("region discovery failed: ${response.code}")
            }
            val ttlMs = parseMaxAgeMs(response.header("Cache-Control"))
            val body = response.body?.string() ?: "{}"
            val settings = LivekitRtc.RegionSettings.newBuilder()
            JsonFormat.parser().ignoringUnknownFields().merge(body, settings)
            val origins = settings.regionsList
                .mapNotNull { it.url.takeIf(String::isNotEmpty) }
                .mapNotNull { toHttp(it).toHttpUrlOrNull() }
            return origins to ttlMs
        }
    }
}

private fun parseMaxAgeMs(cacheControl: String?): Long {
    if (cacheControl.isNullOrEmpty()) return 0
    for (directive in cacheControl.split(",")) {
        val trimmed = directive.trim().lowercase()
        if (trimmed.startsWith("max-age=")) {
            return trimmed.removePrefix("max-age=").toLongOrNull()?.takeIf { it > 0 }?.times(1000) ?: 0
        }
    }
    return 0
}
