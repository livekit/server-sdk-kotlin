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