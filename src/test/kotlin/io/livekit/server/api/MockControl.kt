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

import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Shared helpers for API tests against the mock LiveKit server (livekit/livekit
 * cmd/test-server). Point them at a running instance with LK_TEST_SERVER_URL
 * (default http://127.0.0.1:9999); tests no-op when it is not reachable.
 */
internal object MockControl {
    val base: String = System.getenv("LK_TEST_SERVER_URL") ?: "http://127.0.0.1:9999"

    fun serverUp(): Boolean = try {
        OkHttpClient().newCall(Request.Builder().url("$base/settings/regions").build())
            .execute().use { it.isSuccessful }
    } catch (e: Exception) {
        false
    }

    /**
     * Builds an X-Lk-Mock header value from simple directives. Values may be
     * String, Int/Long, Boolean, List, or Map (see cmd/test-server/config.go).
     */
    fun json(vararg entries: Pair<String, Any?>): String =
        entries.filter { it.second != null }
            .joinToString(",", "{", "}") { (k, v) -> "\"$k\":${encode(v!!)}" }

    private fun encode(v: Any): String = when (v) {
        is String -> "\"$v\""
        is Boolean, is Int, is Long -> v.toString()
        is List<*> -> v.filterNotNull().joinToString(",", "[", "]") { encode(it) }
        is Map<*, *> -> v.entries.joinToString(",", "{", "}") { "\"${it.key}\":${encode(it.value!!)}" }
        else -> "\"$v\""
    }
}
