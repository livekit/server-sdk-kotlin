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

import livekit.LivekitSip
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Retrofit Interface for accessing the SipService Apis.
 */
interface SipService {

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.SIP/CreateSIPInboundTrunk")
    fun createSipInboundTrunk(
        @Body request: LivekitSip.CreateSIPInboundTrunkRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitSip.SIPInboundTrunkInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.SIP/CreateSIPOutboundTrunk")
    fun createSipOutboundTrunk(
        @Body request: LivekitSip.CreateSIPOutboundTrunkRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitSip.SIPOutboundTrunkInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.SIP/UpdateSIPInboundTrunk")
    fun updateSipInboundTrunk(
        @Body request: LivekitSip.UpdateSIPInboundTrunkRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitSip.SIPInboundTrunkInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.SIP/UpdateSIPOutboundTrunk")
    fun updateSipOutboundTrunk(
        @Body request: LivekitSip.UpdateSIPOutboundTrunkRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitSip.SIPOutboundTrunkInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.SIP/ListSIPInboundTrunk")
    fun listSIPInboundTrunk(
        @Body request: LivekitSip.ListSIPInboundTrunkRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitSip.ListSIPInboundTrunkResponse>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.SIP/ListSIPOutboundTrunk")
    fun listSipOutboundTrunk(
        @Body request: LivekitSip.ListSIPOutboundTrunkRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitSip.ListSIPOutboundTrunkResponse>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.SIP/DeleteSIPTrunk")
    fun deleteSipTrunk(
        @Body request: LivekitSip.DeleteSIPTrunkRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitSip.SIPTrunkInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.SIP/CreateSIPDispatchRule")
    fun createSipDispatchRule(
        @Body request: LivekitSip.CreateSIPDispatchRuleRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitSip.SIPDispatchRuleInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.SIP/UpdateSIPDispatchRule")
    fun updateSipDispatchRule(
        @Body request: LivekitSip.UpdateSIPDispatchRuleRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitSip.SIPDispatchRuleInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.SIP/ListSIPDispatchRule")
    fun listSipDispatchRule(
        @Body request: LivekitSip.ListSIPDispatchRuleRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitSip.ListSIPDispatchRuleResponse>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.SIP/DeleteSIPDispatchRule")
    fun deleteSipDispatchRule(
        @Body request: LivekitSip.DeleteSIPDispatchRuleRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitSip.SIPDispatchRuleInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.SIP/CreateSIPParticipant")
    fun createSipParticipant(
        @Body request: LivekitSip.CreateSIPParticipantRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitSip.SIPParticipantInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.SIP/TransferSIPParticipant")
    fun transferSipParticipant(
        @Body request: LivekitSip.TransferSIPParticipantRequest,
        @Header("Authorization") authorization: String
    ): Call<Void?>
}
