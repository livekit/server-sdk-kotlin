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

import io.livekit.server.okhttp.FailoverConfig
import io.livekit.server.okhttp.OkHttpFactory
import io.livekit.server.okhttp.RegionFailoverInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.util.function.Supplier

/**
 * A single entry point to every LiveKit server API, exposing each service.
 * The services share one underlying OkHttp client and Retrofit instance.
 *
 * Create it with [createClient] (API key & secret) or [createClientWithToken]
 * (a pre-signed token, for client-side use).
 *
 * ```
 * val api = LiveKitAPI.createClient(host, apiKey, secret)
 * api.room.createRoom(name = "my-room").execute()
 * ```
 */
class LiveKitAPI internal constructor(
    val room: RoomServiceClient,
    val egress: EgressServiceClient,
    val ingress: IngressServiceClient,
    val sip: SipServiceClient,
    val agentDispatch: AgentDispatchServiceClient,
    val connector: ConnectorServiceClient,
) {
    companion object {
        /**
         * Creates a LiveKitAPI authenticated with an API key and secret. Any
         * omitted value falls back to `LIVEKIT_URL` / `LIVEKIT_API_KEY` /
         * `LIVEKIT_API_SECRET`.
         */
        @JvmStatic
        @JvmOverloads
        fun createClient(
            host: String? = null,
            apiKey: String? = null,
            secret: String? = null,
            okHttpSupplier: Supplier<OkHttpClient> = OkHttpFactory(),
            failover: Boolean = true,
        ): LiveKitAPI {
            val h = host ?: System.getenv("LIVEKIT_URL")
            val k = apiKey ?: System.getenv("LIVEKIT_API_KEY")
            val s = secret ?: System.getenv("LIVEKIT_API_SECRET")
            require(!h.isNullOrEmpty()) { "host is required (pass it or set LIVEKIT_URL)" }
            require(!k.isNullOrEmpty() && !s.isNullOrEmpty()) {
                "apiKey and secret are required (pass them or set LIVEKIT_API_KEY / LIVEKIT_API_SECRET)"
            }
            return build(h, k, s, null, okHttpSupplier, failover)
        }

        /**
         * Creates a LiveKitAPI authenticated with a pre-signed token, sent verbatim
         * (no secret required, so it can run client-side). The token must already
         * carry the grants for the calls it's used with. `host` falls back to
         * `LIVEKIT_URL` and `token` to `LIVEKIT_TOKEN`.
         */
        @JvmStatic
        @JvmOverloads
        fun createClientWithToken(
            host: String? = null,
            token: String? = null,
            okHttpSupplier: Supplier<OkHttpClient> = OkHttpFactory(),
            failover: Boolean = true,
        ): LiveKitAPI {
            val h = host ?: System.getenv("LIVEKIT_URL")
            val t = token ?: System.getenv("LIVEKIT_TOKEN")
            require(!h.isNullOrEmpty()) { "host is required (pass it or set LIVEKIT_URL)" }
            require(!t.isNullOrEmpty()) { "token is required (pass it or set LIVEKIT_TOKEN)" }
            return build(h, "", "", t, okHttpSupplier, failover)
        }

        private fun build(
            host: String,
            apiKey: String,
            secret: String,
            token: String?,
            okHttpSupplier: Supplier<OkHttpClient>,
            failover: Boolean,
        ): LiveKitAPI {
            val okhttp = okHttpSupplier.get().newBuilder()
                .addInterceptor(RegionFailoverInterceptor(FailoverConfig(enabled = failover)))
                .build()
            val baseUrl = if (host.endsWith("/")) host else "$host/"
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(ProtoConverterFactory.create())
                .client(okhttp)
                .build()
            return LiveKitAPI(
                room = RoomServiceClient(retrofit.create(RoomService::class.java), apiKey, secret, token),
                egress = EgressServiceClient(retrofit.create(EgressService::class.java), apiKey, secret, token),
                ingress = IngressServiceClient(retrofit.create(IngressService::class.java), apiKey, secret, token),
                sip = SipServiceClient(retrofit.create(SipService::class.java), apiKey, secret, token),
                agentDispatch = AgentDispatchServiceClient(
                    retrofit.create(AgentDispatchService::class.java), apiKey, secret, token,
                ),
                connector = ConnectorServiceClient(retrofit.create(ConnectorService::class.java), apiKey, secret, token),
            )
        }
    }
}
