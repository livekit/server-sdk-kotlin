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

import livekit.LivekitConnectorTwilio
import livekit.LivekitConnectorWhatsapp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Retrofit Interface for accessing the Connector Service APIs.
 */
interface ConnectorService {

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Connector/DialWhatsAppCall")
    fun dialWhatsAppCall(
        @Body request: LivekitConnectorWhatsapp.DialWhatsAppCallRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitConnectorWhatsapp.DialWhatsAppCallResponse>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Connector/DisconnectWhatsAppCall")
    fun disconnectWhatsAppCall(
        @Body request: LivekitConnectorWhatsapp.DisconnectWhatsAppCallRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitConnectorWhatsapp.DisconnectWhatsAppCallResponse>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Connector/ConnectWhatsAppCall")
    fun connectWhatsAppCall(
        @Body request: LivekitConnectorWhatsapp.ConnectWhatsAppCallRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitConnectorWhatsapp.ConnectWhatsAppCallResponse>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Connector/AcceptWhatsAppCall")
    fun acceptWhatsAppCall(
        @Body request: LivekitConnectorWhatsapp.AcceptWhatsAppCallRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitConnectorWhatsapp.AcceptWhatsAppCallResponse>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Connector/ConnectTwilioCall")
    fun connectTwilioCall(
        @Body request: LivekitConnectorTwilio.ConnectTwilioCallRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitConnectorTwilio.ConnectTwilioCallResponse>
}
