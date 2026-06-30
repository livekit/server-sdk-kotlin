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

import io.livekit.server.okhttp.FailoverConfig
import io.livekit.server.okhttp.OkHttpFactory
import io.livekit.server.okhttp.OkHttpHolder
import io.livekit.server.okhttp.RegionFailoverInterceptor
import io.livekit.server.retrofit.TransformCall
import livekit.LivekitAgentDispatch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.util.function.Supplier

/**
 * A client for interacting with the Agent Dispatch service.
 *
 * Explicit dispatch requires your agent to be registered with an `agentName`.
 *
 * See: [Dispatching agents](https://docs.livekit.io/agents/build/dispatch/)
 */
class AgentDispatchServiceClient(
    private val service: AgentDispatchService,
    private val apiKey: String,
    private val secret: String,
) {
    /**
     * Create an explicit dispatch for an agent to join a room.
     *
     * @param roomName name of the room to dispatch the agent to
     * @param agentName name of the agent to dispatch
     * @param metadata optional custom data to send along with the job (distinct
     *   from room and participant metadata)
     * @param restartPolicy controls whether the job is restarted when it fails (cloud only)
     * @param deployment optional deployment to dispatch to; leave empty to target production
     */
    @JvmOverloads
    fun createDispatch(
        roomName: String,
        agentName: String,
        metadata: String? = null,
        restartPolicy: LivekitAgentDispatch.JobRestartPolicy? = null,
        deployment: String? = null,
    ): Call<LivekitAgentDispatch.AgentDispatch> {
        val request = with(LivekitAgentDispatch.CreateAgentDispatchRequest.newBuilder()) {
            this.room = roomName
            this.agentName = agentName
            metadata?.let { this.metadata = it }
            restartPolicy?.let { this.restartPolicy = it }
            deployment?.let { this.deployment = it }
            build()
        }
        val credentials = authHeader(RoomAdmin(true), RoomName(roomName))
        return service.createDispatch(request, credentials)
    }

    /**
     * Delete an explicit dispatch for an agent in a room.
     *
     * @param dispatchId id of the dispatch to delete
     * @param roomName name of the room the dispatch is for
     */
    fun deleteDispatch(dispatchId: String, roomName: String): Call<LivekitAgentDispatch.AgentDispatch> {
        val request = with(LivekitAgentDispatch.DeleteAgentDispatchRequest.newBuilder()) {
            this.dispatchId = dispatchId
            this.room = roomName
            build()
        }
        val credentials = authHeader(RoomAdmin(true), RoomName(roomName))
        return service.deleteDispatch(request, credentials)
    }

    /**
     * Get an agent dispatch by ID.
     *
     * @param dispatchId id of the dispatch to get
     * @param roomName name of the room the dispatch is for
     * @return the matching dispatch, or null if none was found
     */
    fun getDispatch(dispatchId: String, roomName: String): Call<LivekitAgentDispatch.AgentDispatch?> {
        val request = with(LivekitAgentDispatch.ListAgentDispatchRequest.newBuilder()) {
            this.dispatchId = dispatchId
            this.room = roomName
            build()
        }
        val credentials = authHeader(RoomAdmin(true), RoomName(roomName))
        return TransformCall(service.listDispatch(request, credentials)) {
            it.agentDispatchesList.firstOrNull()
        }
    }

    /**
     * List all agent dispatches for a room.
     *
     * @param roomName name of the room to list dispatches for
     */
    fun listDispatch(roomName: String): Call<List<LivekitAgentDispatch.AgentDispatch>> {
        val request = with(LivekitAgentDispatch.ListAgentDispatchRequest.newBuilder()) {
            this.room = roomName
            build()
        }
        val credentials = authHeader(RoomAdmin(true), RoomName(roomName))
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
         * Create an AgentDispatchServiceClient.
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
            failover: Boolean = true
        ): AgentDispatchServiceClient {
            val okhttp = okHttpSupplier.get().newBuilder()
                .addInterceptor(RegionFailoverInterceptor(FailoverConfig(enabled = failover)))
                .build()

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
