/*
 * Copyright 2024-2026 LiveKit, Inc.
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

import io.livekit.server.okhttp.OkHttpFactory
import livekit.LivekitRoom
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SIPServiceClientTest {
    companion object {
        const val HOST = TestConstants.HOST
        const val KEY = TestConstants.KEY
        const val SECRET = TestConstants.SECRET
    }

    lateinit var client: SipServiceClient

    @BeforeTest
    fun setup() {
        client = SipServiceClient.createClient(
            HOST,
            KEY,
            SECRET,
            OkHttpFactory(true, null)
        )
    }

    @Test
    fun listSipInboundTrunks() {
        client.listSipInboundTrunk().execute()
    }

    @Test
    fun listSipOutboundTrunks() {
        client.listSipOutboundTrunk().execute()
    }

    @Test
    fun listSipDispatchRules() {
        client.listSipDispatchRule().execute()
    }

    @Test
    fun createSipDispatchRule() {
        run {
            val response = client.createSipDispatchRule(
                rule = SipDispatchRuleDirect(
                    roomName = "room",
                    pin = "1234"
                ),
                options = CreateSipDispatchRuleOptions(
                    name = "test_dispatch_rule_name",
                    metadata = "metadata",
                    hidePhoneNumber = true,
                    attributes = mapOf("abc" to "def"),
                    roomPreset = "preset",
                    roomConfig = with(LivekitRoom.RoomConfiguration.newBuilder()) {
                        this.emptyTimeout = 10
                        this.departureTimeout = 20
                        build()
                    }
                ),
            ).execute()

            assertTrue(response.isSuccessful)
            println(response.body())
        }

        run {
            val response = client.listSipDispatchRule().execute()
            assertTrue(response.isSuccessful)

            val infos = response.body() ?: emptyList()
            assertTrue(infos.isNotEmpty())
            println(infos)

            infos.forEach { info ->
                val deleteResponse = client.deleteSipDispatchRule(info.sipDispatchRuleId).execute()
                assertTrue(deleteResponse.isSuccessful)
                println(deleteResponse.body())
            }
        }
    }
}
