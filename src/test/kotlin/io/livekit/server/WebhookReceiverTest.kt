/*
 * Copyright 2024 LiveKit, Inc.
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

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class WebhookReceiverTest {

    @Test
    fun receive() {
        val body =
            """{"event":"room_started", "room":{"sid":"RM_TkVjUvAqgzKz", "name":"mytestroom", 
                |"emptyTimeout":300, "creationTime":"1628545903", "turnPassword":"ICkSr2rEeslkN6e9bXL4Ji5zzMD5Z7zzr6ulOaxMj6N", 
                |"enabledCodecs":[{"mime":"audio/opus"}, {"mime":"video/VP8"}]}}""".trimMargin()
        val testApiKey = "abcdefg"
        val testSecret = "ababababababababababababababababababababababababababababababa"

        val token = AccessToken(testApiKey, testSecret)
        token.sha256 = "1renMMRYeCXsy6M9bjJ90XA3M1q1byhUGNoD91aPuhM="
        val jwt = token.toJwt()

        val receiver = WebhookReceiver(testApiKey, testSecret)

        val event = receiver.receive(body, jwt)

        assertEquals("mytestroom", event.room.name)
        assertEquals("room_started", event.event)
    }
}
