/*
 * Copyright 2025 LiveKit, Inc.
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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AgentDispatchServiceClientTest {

    companion object {
        const val HOST = TestConstants.HOST
        const val KEY = TestConstants.KEY
        const val SECRET = TestConstants.SECRET

        const val ROOM_NAME = "room_name"
        const val METADATA = "metadata"
    }

    lateinit var client: AgentDispatchServiceClient
    lateinit var roomClient: RoomServiceClient

    @BeforeTest
    fun setup() {
        client = AgentDispatchServiceClient.createClient(HOST, KEY, SECRET, OkHttpFactory(true, null))
        roomClient = RoomServiceClient.createClient(HOST, KEY, SECRET, OkHttpFactory(true, null))
    }

    @Test
    fun createAgentDispatch() {
        roomClient.createRoom(name = ROOM_NAME).execute()
        client.createDispatch(
            room = ROOM_NAME,
            agentName = "agent",
        ).execute()
    }

    @Test
    fun listAgentDispatch() {
        roomClient.createRoom(name = ROOM_NAME).execute()
        val dispatchResp = client.createDispatch(
            room = ROOM_NAME,
            agentName = "agent",
            metadata = METADATA,
        ).execute()
        val dispatch = dispatchResp.body()

        assertNotNull(dispatch?.id)

        val listResp = client.listDispatch(room = ROOM_NAME).execute()
        val allDispatches = listResp.body()
        assertTrue(listResp.isSuccessful)
        assertNotNull(allDispatches)
        assertTrue(allDispatches.any { item -> item.id == dispatch?.id })
    }

    @Test
    fun deleteAgentDispatch() {
        roomClient.createRoom(name = ROOM_NAME).execute()
        val dispatchResp = client.createDispatch(
            room = ROOM_NAME,
            agentName = "agent",
            metadata = METADATA,
        ).execute()
        val dispatch = dispatchResp.body()

        assertNotNull(dispatch?.id)

        val deleteResp = client.deleteDispatch(room = ROOM_NAME, dispatchId = dispatch?.id ?: "").execute()
        assertTrue(deleteResp.isSuccessful)
    }
}
