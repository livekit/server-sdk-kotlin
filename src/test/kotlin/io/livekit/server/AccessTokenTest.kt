/*
 * Copyright 2024-2025 LiveKit, Inc.
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
import livekit.LivekitEgress
import livekit.LivekitRoom
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
        assertEquals(token.identity, claims["jti"]?.asString())
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
    fun testEgressFileOutputConfiguration() {
        val roomConfig = with(RoomConfiguration.newBuilder()) {
            name = "test_room"
            // agentDispatches = with(agentDispatchesBuilder) {
            //     add(
            //         LivekitRoom.Agent.newBuilder()
            //             .setAgentName("test_agent")
            //             .setMetadata("{\"user_id\": \"12345\"}")
            //             .build()
            //     )
            //     buildPartial()
            // }
            egress = with(egressBuilder) {
                room = with(roomBuilder) {
                    roomName = "test_room"
                    addFileOutputs(
                        LivekitEgress.EncodedFileOutput.newBuilder()
                            .setFileType(LivekitEgress.EncodedFileType.MP4)
                            .setFilepath("livekit/test.mp4")
                            .setS3(
                                LivekitEgress.S3Upload.newBuilder()
                                    .setBucket("test-bucket")
                                    .setRegion("us-west-2")
                                    .setAccessKey("test-access-key")
                                    .setSecret("test-secret")
                                    .setEndpoint("https://s3.us-west-2.amazonaws.com")
                                    .build()
                            )
                            .build()
                    )
                    buildPartial()
                }
                buildPartial()
            }
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
        
        val egressMap = roomConfigMap?.get("egress") as? Map<*, *>
        assertNotNull(egressMap)
        
        val roomMap = egressMap?.get("room") as? Map<*, *>
        assertNotNull(roomMap)
        
        val fileOutputs = roomMap?.get("file_outputs") as? List<*>
        assertNotNull(fileOutputs)
        assertEquals(1, fileOutputs?.size)
        
        val fileOutput = fileOutputs?.first() as? Map<*, *>
        assertNotNull(fileOutput)
        assertEquals("MP4", fileOutput?.get("file_type"))
        assertEquals("livekit/test.mp4", fileOutput?.get("filepath"))
        
        val s3Config = fileOutput?.get("s3") as? Map<*, *>
        assertNotNull(s3Config)
        assertEquals("test-bucket", s3Config?.get("bucket"))
        assertEquals("us-west-2", s3Config?.get("region"))
        assertEquals("test-access-key", s3Config?.get("access_key"))
        assertEquals("test-secret", s3Config?.get("secret"))
        assertEquals("https://s3.us-west-2.amazonaws.com", s3Config?.get("endpoint"))
    }
}
