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

import io.livekit.server.okhttp.FailoverConfig
import io.livekit.server.okhttp.OkHttpFactory
import io.livekit.server.okhttp.OkHttpHolder
import io.livekit.server.okhttp.RegionFailoverInterceptor
import livekit.LivekitConnectorTwilio
import livekit.LivekitConnectorWhatsapp
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.util.function.Supplier

/**
 * A client for the Connector service, bridging WhatsApp and Twilio calls into
 * LiveKit rooms.
 *
 * The request types carry many fields, so each method takes a fully-built
 * protobuf request (constructed with its `newBuilder()`) and returns the
 * protobuf response.
 */
class ConnectorServiceClient(
    private val service: ConnectorService,
    private val apiKey: String,
    private val secret: String,
) {
    /**
     * Initiate an outbound WhatsApp call.
     */
    fun dialWhatsAppCall(
        request: LivekitConnectorWhatsapp.DialWhatsAppCallRequest,
    ): Call<LivekitConnectorWhatsapp.DialWhatsAppCallResponse> {
        return service.dialWhatsAppCall(request, authHeader(RoomCreate(true)))
    }

    /**
     * Accept an inbound WhatsApp call.
     */
    fun acceptWhatsAppCall(
        request: LivekitConnectorWhatsapp.AcceptWhatsAppCallRequest,
    ): Call<LivekitConnectorWhatsapp.AcceptWhatsAppCallResponse> {
        return service.acceptWhatsAppCall(request, authHeader(RoomCreate(true)))
    }

    /**
     * Connect an established WhatsApp call (used for business-initiated calls).
     */
    fun connectWhatsAppCall(
        request: LivekitConnectorWhatsapp.ConnectWhatsAppCallRequest,
    ): Call<LivekitConnectorWhatsapp.ConnectWhatsAppCallResponse> {
        return service.connectWhatsAppCall(request, authHeader(RoomCreate(true)))
    }

    /**
     * Disconnect an active WhatsApp call.
     */
    fun disconnectWhatsAppCall(
        request: LivekitConnectorWhatsapp.DisconnectWhatsAppCallRequest,
    ): Call<LivekitConnectorWhatsapp.DisconnectWhatsAppCallResponse> {
        return service.disconnectWhatsAppCall(request, authHeader(RoomCreate(true)))
    }

    /**
     * Connect a Twilio call to a LiveKit room.
     */
    fun connectTwilioCall(
        request: LivekitConnectorTwilio.ConnectTwilioCallRequest,
    ): Call<LivekitConnectorTwilio.ConnectTwilioCallResponse> {
        return service.connectTwilioCall(request, authHeader(RoomCreate(true)))
    }

    private fun authHeader(vararg videoGrants: VideoGrant): String {
        val accessToken = AccessToken(apiKey, secret)
        accessToken.addGrants(*videoGrants)

        val jwt = accessToken.toJwt()

        return "Bearer $jwt"
    }

    companion object {
        /**
         * Create a ConnectorServiceClient.
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
        ): ConnectorServiceClient {
            val okhttp = okHttpSupplier.get().newBuilder()
                .addInterceptor(RegionFailoverInterceptor(FailoverConfig(enabled = failover)))
                .build()

            val service = Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(ProtoConverterFactory.create())
                .client(okhttp)
                .build()
                .create(ConnectorService::class.java)

            return ConnectorServiceClient(service, apiKey, secret)
        }
    }
}
