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

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import livekit.LivekitAgentDispatch
import livekit.LivekitRoom.RoomConfiguration
import org.junit.jupiter.api.Test
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AccessTokenTest {

    companion object {
        const val KEY = "abcdefg"
        const val SECRET = "abababa"
    }

    @Test
    fun createToken() {
        val token = AccessToken(KEY, SECRET)

        val roomConfig = with(RoomConfiguration.newBuilder()) {
            name = "name"
            emptyTimeout = 1
            departureTimeout = 2
            maxParticipants = 3
            minPlayoutDelay = 4
            maxPlayoutDelay = 5
            egress = with(egressBuilder) {
                room = with(roomBuilder) {
                    roomName = "name"
                    buildPartial()
                }
                buildPartial()
            }
            syncStreams = true
            addAgents(with(LivekitAgentDispatch.RoomAgentDispatch.newBuilder()) {
                agentName = "agent_name"
                metadata = "agent_metadata"
                build()
            })
            build()
        }

        token.expiration = Date(33254282804000) // 10/15/3023
        token.name = "name"
        token.identity = "identity"
        token.metadata = "metadata"
        token.sha256 = "gfedcba"
        token.attributes["key"] = "value"
        token.roomPreset = "roomPreset"
        token.roomConfiguration = roomConfig

        token.addGrants(RoomName("room_name"))
        token.addGrants(CanPublishSources(listOf("camera", "microphone")))
        token.addSIPGrants(SIPAdmin(true))

        val jwt = token.toJwt()

        val alg = Algorithm.HMAC256(SECRET)
        val decodedJWT = JWT.require(alg)
            .withIssuer(KEY)
            .build()
            .verify(jwt)

        val claims = decodedJWT.claims

        assertEquals(KEY, claims["iss"]?.asString())
        assertEquals(token.name, claims["name"]?.asString())
        assertEquals(token.metadata, claims["metadata"]?.asString())
        assertEquals(token.sha256, claims["sha256"]?.asString())
        assertEquals(token.roomPreset, claims["roomPreset"]?.asString())
        assertEquals(token.expiration, decodedJWT.expiresAt)
        assertEquals(token.attributes["key"], claims["attributes"]?.asMap()?.get("key"))
        assertEquals(token.roomConfiguration?.toMap(), claims["roomConfig"]?.asMap())

        val videoGrants = claims["video"]?.asMap()
        assertNotNull(videoGrants)
        assertEquals("room_name", videoGrants["room"])
        assertEquals(listOf("camera", "microphone"), videoGrants["canPublishSources"])

        val sipGrants = claims["sip"]?.asMap()
        assertNotNull(sipGrants)
        assertEquals(true, sipGrants["admin"])
    }

    @Test
    fun protobufMapConversion() {
        val roomConfig = with(RoomConfiguration.newBuilder()) {
            name = "name"
            emptyTimeout = 1
            departureTimeout = 2
            maxParticipants = 3
            minPlayoutDelay = 4
            maxPlayoutDelay = 5
            egress = with(egressBuilder) {
                room = with(roomBuilder) {
                    roomName = "name"
                    buildPartial()
                }
                buildPartial()
            }
            syncStreams = true
            build()
        }

        val map = roomConfig.toMap()

        assertEquals(roomConfig.emptyTimeout, map["empty_timeout"])
        assertEquals(roomConfig.egress.room.roomName, ((map["egress"] as Map<*, *>)["room"] as Map<*, *>)["room_name"])
    }

    @Test
    fun testArraysInRoomConfiguration() {
        val roomConfig = with(RoomConfiguration.newBuilder()) {
            name = "test_room"
            addAgents(
                LivekitAgentDispatch.RoomAgentDispatch.newBuilder()
                    .setAgentName("agent_name")
                    .setMetadata("metadata")
                    .build()
            )
            build()
        }

        val token = AccessToken(KEY, SECRET)
        token.roomConfiguration = roomConfig

        // This should not throw an exception
        val jwt = token.toJwt()

        // Verify the JWT can be decoded
        val alg = Algorithm.HMAC256(SECRET)
        val decodedJWT = JWT.require(alg)
            .withIssuer(KEY)
            .build()
            .verify(jwt)

        // Verify the room configuration was properly encoded
        val claims = decodedJWT.claims
        val roomConfigMap = claims["roomConfig"]?.asMap()
        assertNotNull(roomConfigMap)

        val agentsMap = roomConfigMap.get("agents") as? List<*>
        assertNotNull(agentsMap)
        assertEquals(1, agentsMap.size)

        val agentMap = agentsMap.first() as? Map<*, *>
        assertNotNull(agentMap)
        assertEquals("agent_name", agentMap.get("agent_name"))
        assertEquals("metadata", agentMap.get("metadata"))
    }
}
