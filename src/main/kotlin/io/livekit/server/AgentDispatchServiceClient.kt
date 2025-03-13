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
import io.livekit.server.okhttp.OkHttpHolder
import io.livekit.server.retrofit.TransformCall
import livekit.LivekitAgentDispatch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.util.function.Supplier

/**
 * A client for explicit agent dispatch.
 *
 * See: [Dispatching agents](https://docs.livekit.io/agents/build/dispatch/#explicit-agent-dispatch)
 */
class AgentDispatchServiceClient(
    private val service: AgentDispatchService,
    private val apiKey: String,
    private val secret: String,
) {

    /**
     * Creates an agent dispatch in a room.
     * @param room Name of the room to create dispatch in
     * @param agentName Name of the agent to dispatch
     * @param metadata Optional metadata to attach to the dispatch
     * @return Created agent dispatch
     */
    @JvmOverloads
    fun createDispatch(
        room: String,
        agentName: String,
        metadata: String? = null,
    ): Call<LivekitAgentDispatch.AgentDispatch> {
        val request = with(LivekitAgentDispatch.CreateAgentDispatchRequest.newBuilder()) {
            setRoom(room)
            setAgentName(agentName)
            if (metadata != null) {
                setMetadata(metadata)
            }
            build()
        }
        val credentials = authHeader(RoomAdmin(true), RoomName(room))
        return service.createDispatch(request, credentials)
    }

    /**
     * Deletes an agent dispatch from a room.
     * @param room Name of the room to delete dispatch from
     * @param dispatchId ID of the dispatch to delete
     * @return Deleted agent dispatch
     */
    fun deleteDispatch(room: String, dispatchId: String): Call<LivekitAgentDispatch.AgentDispatch> {
        val request = LivekitAgentDispatch.DeleteAgentDispatchRequest.newBuilder()
            .setRoom(room)
            .setDispatchId(dispatchId)
            .build()
        val credentials = authHeader(RoomAdmin(true), RoomName(room))
        return service.deleteDispatch(request, credentials)
    }

    /**
     * List all agent dispatches in a room.
     * @param room Name of the room to list dispatches from
     * @return List of agent dispatches
     */
    fun listDispatch(room: String): Call<List<LivekitAgentDispatch.AgentDispatch>> {
        val request = LivekitAgentDispatch.ListAgentDispatchRequest.newBuilder()
            .setRoom(room)
            .build()
        val credentials = authHeader(RoomAdmin(true), RoomName(room))
        return TransformCall(service.listDispatch(request, credentials)) {
            it.agentDispatchesList
        }
    }

    private fun authHeader(vararg videoGrants: VideoGrant): String {
        val accessToken = AccessToken(apiKey, secret)
        accessToken.addGrants(*videoGrants)

        val jwt = accessToken.toJwt()

        return "Bearer $jwt"
    }

    companion object {

        /**
         * Create a new [AgentDispatchServiceClient] with the given host, api key, and secret.
         *
         * @param okHttpSupplier provide an [OkHttpFactory] if you wish to customize the http client
         * (e.g. proxy, timeout, certificate/auth settings), or supply your own OkHttpClient
         * altogether to pool resources with [OkHttpHolder].
         *
         * @see OkHttpHolder
         * @see OkHttpFactory
         */
        @JvmStatic
        @JvmOverloads
        fun createClient(
            host: String,
            apiKey: String,
            secret: String,
            okHttpSupplier: Supplier<OkHttpClient> = OkHttpFactory(),
        ): AgentDispatchServiceClient {
            val okhttp = okHttpSupplier.get()
            val service = Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(ProtoConverterFactory.create())
                .client(okhttp)
                .build()
                .create(AgentDispatchService::class.java)

            return AgentDispatchServiceClient(service, apiKey, secret)
        }
    }
}
