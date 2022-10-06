package io.livekit.server

import livekit.LivekitModels
import livekit.LivekitRoom
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST


interface RoomService {
    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.RoomService/CreateRoom")
    fun createRoom(@Body request: LivekitRoom.CreateRoomRequest, @Header("Authorization") authorization: String): Call<LivekitModels.Room>
}