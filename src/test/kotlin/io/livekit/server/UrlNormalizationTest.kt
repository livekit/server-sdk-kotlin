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

package io.livekit.server

import kotlin.test.Test
import kotlin.test.assertEquals

class UrlNormalizationTest {
    @Test
    fun convertsWebSocketSchemesToHttp() {
        assertEquals("https://my.livekit.cloud/", normalizeApiUrl("wss://my.livekit.cloud"))
        assertEquals("http://localhost:7880/", normalizeApiUrl("ws://localhost:7880"))
    }

    @Test
    fun leavesHttpSchemesUntouched() {
        assertEquals("https://my.livekit.cloud/", normalizeApiUrl("https://my.livekit.cloud"))
        assertEquals("http://localhost:7880/", normalizeApiUrl("http://localhost:7880"))
    }

    @Test
    fun preservesExistingTrailingSlash() {
        assertEquals("https://my.livekit.cloud/", normalizeApiUrl("wss://my.livekit.cloud/"))
    }
}
