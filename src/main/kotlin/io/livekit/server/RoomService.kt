package io.livekit.server

import livekit.LivekitModels
import livekit.LivekitRoom
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Retrofit Interface for accessing the RoomService Apis.
 */
interface RoomService {
    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.RoomService/CreateRoom")
    fun createRoom(@Body request: LivekitRoom.CreateRoomRequest, @Header("Authorization") authorization: String): Call<LivekitModels.Room>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.RoomService/ListRooms")
    fun listRooms(
        @Body request: LivekitRoom.ListRoomsRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitRoom.ListRoomsResponse>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.RoomService/DeleteRoom")
    fun deleteRoom(@Body request: LivekitRoom.DeleteRoomRequest, @Header("Authorization") authorization: String): Call<Void>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.RoomService/UpdateRoomMetadata")
    fun updateRoomMetadata(
        @Body request: LivekitRoom.UpdateRoomMetadataRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitModels.Room>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.RoomService/ListParticipants")
    fun listParticipants(
        @Body request: LivekitRoom.ListParticipantsRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitRoom.ListParticipantsResponse>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.RoomService/GetParticipant")
    fun getParticipant(
        @Body request: LivekitRoom.RoomParticipantIdentity,
        @Header("Authorization") authorization: String
    ): Call<LivekitModels.ParticipantInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.RoomService/RemoveParticipant")
    fun removeParticipant(@Body request: LivekitRoom.RoomParticipantIdentity, @Header("Authorization") authorization: String): Call<Void>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.RoomService/MutePublishedTrack")
    fun mutePublishedTrack(
        @Body request: LivekitRoom.MuteRoomTrackRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitRoom.MuteRoomTrackResponse>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.RoomService/UpdateParticipant")
    fun updateParticipant(
        @Body request: LivekitRoom.UpdateParticipantRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitModels.ParticipantInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.RoomService/UpdateSubscriptions")
    fun updateSubscriptions(
        @Body request: LivekitRoom.UpdateSubscriptionsRequest,
        @Header("Authorization") authorization: String
    ): Call<Void>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.RoomService/SendData")
    fun sendData(@Body request: LivekitRoom.SendDataRequest, @Header("Authorization") authorization: String): Call<Void>

}