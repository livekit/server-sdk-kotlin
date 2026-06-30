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

package io.livekit.server.api

import io.livekit.server.okhttp.FailoverConfig
import io.livekit.server.okhttp.RegionFailoverInterceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Region failover tests against the shared mock LiveKit API server
 * (livekit/livekit cmd/test-server). Point them at a running instance with
 * LK_TEST_SERVER_URL (default http://127.0.0.1:9999); they no-op when no server
 * is reachable. The mock returns Cache-Control: max-age=0, so the region cache
 * never stores entries and scenarios don't interfere.
 *
 * See cmd/test-server/README.md for the X-Lk-Mock-* control protocol. These
 * tests drive the interceptor directly because the Retrofit service methods do
 * not expose per-call headers.
 */
class RegionFailoverTest {
    private val base = System.getenv("LK_TEST_SERVER_URL") ?: "http://127.0.0.1:9999"

    private fun serverUp(): Boolean = try {
        OkHttpClient().newCall(Request.Builder().url("$base/settings/regions").build())
            .execute().use { it.isSuccessful }
    } catch (e: Exception) {
        false
    }

    // force bypasses the cloud-host check (the mock is on 127.0.0.1) and a tiny
    // backoff keeps the tests fast — both are internal, test-only knobs.
    private fun call(
        directives: Map<String, String>,
        failover: Boolean = true,
        force: Boolean = true,
    ): Response {
        val client = OkHttpClient.Builder()
            .addInterceptor(
                RegionFailoverInterceptor(
                    FailoverConfig(enabled = failover, force = force, backoffBaseMs = 1),
                ),
            )
            .build()
        val body = ByteArray(0).toRequestBody("application/protobuf".toMediaTypeOrNull())
        val builder = Request.Builder()
            .url("$base/twirp/livekit.RoomService/CreateRoom")
            .post(body)
            .addHeader("Authorization", "Bearer test-token")
            // These tests exercise failover, not authz; skip the mock's permission check.
            .addHeader("X-Lk-Mock-Skip-Auth", "true")
        directives.forEach { (k, v) -> builder.addHeader(k, v) }
        return client.newCall(builder.build()).execute()
    }

    @Test
    fun healthy() {
        if (!serverUp()) return
        call(emptyMap()).use {
            assertEquals(200, it.code)
            assertEquals("0", it.header("X-Lk-Mock-Region"))
        }
    }

    @Test
    fun primaryUnavailable() {
        if (!serverUp()) return
        call(mapOf("X-Lk-Mock-Fail-Regions" to "0")).use {
            assertEquals(200, it.code)
            assertEquals("1", it.header("X-Lk-Mock-Region"))
        }
    }

    @Test
    fun twoRegionsUnavailable() {
        if (!serverUp()) return
        call(mapOf("X-Lk-Mock-Fail-Regions" to "0,1")).use {
            assertEquals(200, it.code)
            assertEquals("2", it.header("X-Lk-Mock-Region"))
        }
    }

    @Test
    fun allUnavailable() {
        if (!serverUp()) return
        call(mapOf("X-Lk-Mock-Fail-Regions" to "0,1,2,3")).use {
            assertEquals(503, it.code)
        }
    }

    @Test
    fun clientErrorNotRetried() {
        if (!serverUp()) return
        call(mapOf("X-Lk-Mock-Fail-Regions" to "0", "X-Lk-Mock-Fail-Status" to "400")).use {
            assertEquals(400, it.code)
        }
    }

    @Test
    fun transportErrorFailover() {
        if (!serverUp()) return
        call(mapOf("X-Lk-Mock-Fail-Regions" to "0", "X-Lk-Mock-Fail-Mode" to "drop")).use {
            assertEquals(200, it.code)
            assertEquals("1", it.header("X-Lk-Mock-Region"))
        }
    }

    @Test
    fun regionDiscoveryUnreachable() {
        if (!serverUp()) return
        call(mapOf("X-Lk-Mock-Fail-Regions" to "0", "X-Lk-Mock-Regions-Status" to "500")).use {
            assertEquals(503, it.code)
        }
    }

    @Test
    fun notCloudHost() {
        if (!serverUp()) return
        // Enabled but not forced; 127.0.0.1 is not a cloud host, so no failover.
        call(mapOf("X-Lk-Mock-Fail-Regions" to "0"), force = false).use {
            assertEquals(503, it.code)
        }
    }

    @Test
    fun disabled() {
        if (!serverUp()) return
        call(mapOf("X-Lk-Mock-Fail-Regions" to "0"), failover = false).use {
            assertEquals(503, it.code)
        }
    }

    @Test
    fun shortTimeoutSkipsFailover() {
        if (!serverUp()) return
        // A sub-threshold per-request timeout trips the thundering-herd guard, so a
        // failing primary region collapses to a single attempt (no failover). The
        // interceptor consumes the internal timeout header before reaching the mock.
        call(mapOf("X-Lk-Mock-Fail-Regions" to "0", "X-Lk-Request-Timeout" to "1")).use {
            assertEquals(503, it.code)
        }
    }
}
