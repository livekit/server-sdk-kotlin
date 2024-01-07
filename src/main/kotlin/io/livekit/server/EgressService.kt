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

import livekit.LivekitEgress
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Retrofit Interface for accessing the RoomService Apis.
 */
interface EgressService {

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Egress/StartRoomCompositeEgress")
    fun startRoomCompositeEgress(
        @Body request: LivekitEgress.RoomCompositeEgressRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitEgress.EgressInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Egress/StartParticipantEgress")
    fun startParticipantEgress(
        @Body request: LivekitEgress.ParticipantEgressRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitEgress.EgressInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Egress/StartTrackCompositeEgress")
    fun startTrackCompositeEgress(
        @Body request: LivekitEgress.TrackCompositeEgressRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitEgress.EgressInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Egress/StartTrackEgress")
    fun startTrackEgress(
        @Body request: LivekitEgress.TrackEgressRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitEgress.EgressInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Egress/StartWebEgress")
    fun startWebEgress(
        @Body request: LivekitEgress.WebEgressRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitEgress.EgressInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Egress/UpdateLayout")
    fun updateLayout(
        @Body request: LivekitEgress.UpdateLayoutRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitEgress.EgressInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Egress/UpdateStream")
    fun updateStream(
        @Body request: LivekitEgress.UpdateStreamRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitEgress.EgressInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Egress/ListEgress")
    fun listEgress(
        @Body request: LivekitEgress.ListEgressRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitEgress.ListEgressResponse>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Egress/StopEgress")
    fun stopEgress(
        @Body request: LivekitEgress.StopEgressRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitEgress.EgressInfo>
}
