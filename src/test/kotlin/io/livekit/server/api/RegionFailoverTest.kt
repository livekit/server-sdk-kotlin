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
 * Region failover tests against the shared mock LiveKit API server. See
 * [MockControl] for setup and the X-Lk-Mock JSON control protocol. These drive
 * the interceptor directly because failover relies on internal test-only knobs
 * (force/backoffBaseMs) the service clients don't expose.
 */
class RegionFailoverTest {
    private val base = MockControl.base

    // force bypasses the cloud-host check (the mock is on 127.0.0.1) and a tiny
    // backoff keeps the tests fast — both are internal, test-only knobs.
    private fun call(
        vararg directives: Pair<String, Any?>,
        failover: Boolean = true,
        force: Boolean = true,
        requestTimeout: String? = null,
    ): Response {
        val client = OkHttpClient.Builder()
            .addInterceptor(
                RegionFailoverInterceptor(
                    FailoverConfig(enabled = failover, force = force, backoffBaseMs = 1),
                ),
            )
            .build()
        val body = ByteArray(0).toRequestBody("application/protobuf".toMediaTypeOrNull())
        // These tests exercise failover, not authz; skip the mock's permission check.
        val mock = MockControl.json("skipAuth" to true, *directives)
        val builder = Request.Builder()
            .url("$base/twirp/livekit.RoomService/CreateRoom")
            .post(body)
            .addHeader("Authorization", "Bearer test-token")
            .addHeader("X-Lk-Mock", mock)
        requestTimeout?.let { builder.addHeader("X-Lk-Request-Timeout", it) }
        return client.newCall(builder.build()).execute()
    }

    @Test
    fun healthy() {
        if (!MockControl.serverUp()) return
        call().use {
            assertEquals(200, it.code)
            assertEquals("0", it.header("X-Lk-Mock-Region"))
        }
    }

    @Test
    fun primaryUnavailable() {
        if (!MockControl.serverUp()) return
        call("failRegions" to listOf(0)).use {
            assertEquals(200, it.code)
            assertEquals("1", it.header("X-Lk-Mock-Region"))
        }
    }

    @Test
    fun twoRegionsUnavailable() {
        if (!MockControl.serverUp()) return
        call("failRegions" to listOf(0, 1)).use {
            assertEquals(200, it.code)
            assertEquals("2", it.header("X-Lk-Mock-Region"))
        }
    }

    @Test
    fun allUnavailable() {
        if (!MockControl.serverUp()) return
        call("failRegions" to listOf(0, 1, 2, 3)).use {
            assertEquals(503, it.code)
        }
    }

    @Test
    fun clientErrorNotRetried() {
        if (!MockControl.serverUp()) return
        call("failRegions" to listOf(0), "failStatus" to 400).use {
            assertEquals(400, it.code)
        }
    }

    @Test
    fun transportErrorFailover() {
        if (!MockControl.serverUp()) return
        call("failRegions" to listOf(0), "failMode" to "drop").use {
            assertEquals(200, it.code)
            assertEquals("1", it.header("X-Lk-Mock-Region"))
        }
    }

    @Test
    fun regionDiscoveryUnreachable() {
        if (!MockControl.serverUp()) return
        call("failRegions" to listOf(0), "regionsStatus" to 500).use {
            assertEquals(503, it.code)
        }
    }

    @Test
    fun notCloudHost() {
        if (!MockControl.serverUp()) return
        // Enabled but not forced; 127.0.0.1 is not a cloud host, so no failover.
        call("failRegions" to listOf(0), force = false).use {
            assertEquals(503, it.code)
        }
    }

    @Test
    fun disabled() {
        if (!MockControl.serverUp()) return
        call("failRegions" to listOf(0), failover = false).use {
            assertEquals(503, it.code)
        }
    }

    @Test
    fun shortTimeoutSkipsFailover() {
        if (!MockControl.serverUp()) return
        // A sub-threshold per-request timeout trips the thundering-herd guard, so a
        // failing primary region collapses to a single attempt (no failover). The
        // interceptor consumes the internal timeout header before reaching the mock.
        call("failRegions" to listOf(0), requestTimeout = "1").use {
            assertEquals(503, it.code)
        }
    }
}
