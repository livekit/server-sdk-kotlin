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

import io.livekit.server.okhttp.OkHttpFactory
import livekit.LivekitModels
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RoomServiceClientTest {

    companion object {
        const val HOST = TestConstants.HOST
        const val KEY = TestConstants.KEY
        const val SECRET = TestConstants.SECRET
    }

    lateinit var client: RoomServiceClient
    lateinit var roomName: String

    @BeforeTest
    fun setup() {
        client = RoomServiceClient.createClient(HOST, KEY, SECRET, OkHttpFactory(true, null))
        roomName = "test_room_${UUID.randomUUID()}"
    }

    @AfterTest
    fun cleanup() {
        try {
            client.deleteRoom(roomName).execute()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    @Test
    fun createRoom() {
        val metadata = "metadata"
        val response = client.createRoom(
            name = roomName,
            metadata = metadata,
        ).execute()
        val room = response.body()

        assertTrue(response.isSuccessful)
        assertEquals(roomName, room?.name)
        assertEquals(metadata, room?.metadata)
    }

    @Test
    fun listRooms() {
        client.createRoom(roomName).execute()
        val response = client.listRooms(null).execute()
        val rooms = response.body()

        assertTrue(response.isSuccessful)
        assertNotNull(rooms)
        assertTrue(rooms.any { room -> room.name == roomName })
    }

    @Test
    fun deleteRoom() {
        client.createRoom(roomName).execute()
        client.deleteRoom(roomName).execute()
        val response = client.listRooms(null).execute()
        val rooms = response.body()

        assertTrue(response.isSuccessful)
        assertNotNull(rooms)
        assertTrue(rooms.none { room -> room.name == roomName })
    }

    @Test
    fun updateRoomMetadata() {
        val metadata = "NewMetadata"
        client.createRoom(roomName).execute()
        val response = client.updateRoomMetadata(roomName, metadata).execute()
        val room = response.body()

        assertTrue(response.isSuccessful)
        assertNotNull(room)
        assertEquals(metadata, room.metadata)
    }

    @Test
    fun listParticipants() {
        client.createRoom(roomName).execute()
        val response = client.listParticipants(roomName).execute()
        val participants = response.body()
        assertTrue(response.isSuccessful)
        assertNotNull(participants)
        assertEquals(0, participants.size)
    }

    @Test
    @Ignore("Requires manual participant")
    fun updateParticipant() {
        client.createRoom(roomName).execute()
        val participants = client.listParticipants(roomName).execute().body()
        if (participants != null) {
            val participant = participants.first()

            val newMetadata = "new_metadata"
            val response = client.updateParticipant(roomName, participant.identity, metadata = newMetadata).execute()
            val updatedParticipant = response.body()!!
            assertTrue(response.isSuccessful)
            assertEquals(newMetadata, updatedParticipant.metadata)
        }
    }

    @Test
    fun getParticipant() {
        client.createRoom(roomName).execute()
        client.getParticipant(roomName, "fdsa").execute()
    }

    @Test
    fun sendData() {
        client.createRoom(roomName).execute()
        val response = client.sendData(roomName, "data".toByteArray(), LivekitModels.DataPacket.Kind.RELIABLE).execute()
        assertTrue(response.isSuccessful)
    }
}
