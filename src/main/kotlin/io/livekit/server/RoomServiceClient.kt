package io.livekit.server

import com.google.protobuf.ByteString
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.livekit.server.retrofit.TransformCall
import livekit.LivekitModels
import livekit.LivekitRoom
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import javax.crypto.spec.SecretKeySpec

class RoomServiceClient(
    private val service: RoomService,
    private val apiKey: String,
    private val secret: String,
) {

    @JvmOverloads
    fun createRoom(
        name: String,
        emptyTimeout: Int? = null,
        maxParticipants: Int? = null,
        nodeId: String? = null,
    ): Call<LivekitModels.Room> {
        val request = with(LivekitRoom.CreateRoomRequest.newBuilder()) {
            this.name = name
            if (emptyTimeout != null) {
                this.emptyTimeout = emptyTimeout
            }
            if (maxParticipants != null) {
                this.maxParticipants = maxParticipants
            }
            if (nodeId != null) {
                this.nodeId = nodeId
            }
            build()
        }
        val credentials = authHeader(mapOf(RoomCreate(true).toPair()))
        return service.createRoom(request, credentials)
    }

    @JvmOverloads
    fun listRooms(names: List<String>? = null): Call<List<LivekitModels.Room>> {
        val request = with(LivekitRoom.ListRoomsRequest.newBuilder()) {
            if (names != null) {
                addAllNames(names)
            }
            build()
        }
        val credentials = authHeader(mapOf(RoomList(true).toPair()))
        return TransformCall(service.listRooms(request, credentials)) {
            it.roomsList
        }
    }

    fun deleteRoom(roomName: String): Call<Void> {
        val request = LivekitRoom.DeleteRoomRequest.newBuilder()
            .setRoom(roomName)
            .build()
        val credentials = authHeader(mapOf(RoomCreate(true).toPair()))
        return service.deleteRoom(request, credentials)
    }

    fun updateRoomMetadata(roomName: String, metadata: String): Call<LivekitModels.Room> {
        val request = LivekitRoom.UpdateRoomMetadataRequest.newBuilder()
            .setRoom(roomName)
            .setMetadata(metadata)
            .build()
        val credentials = authHeader(
            mapOf(
                RoomAdmin(true).toPair(),
                Room(roomName).toPair(),
            )
        )
        return service.updateRoomMetadata(request, credentials)
    }

    fun listParticipants(roomName: String): Call<List<LivekitModels.ParticipantInfo>> {
        val request = LivekitRoom.ListParticipantsRequest.newBuilder()
            .setRoom(roomName)
            .build()
        val credentials = authHeader(
            mapOf(
                RoomAdmin(true).toPair(),
                Room(roomName).toPair(),
            )
        )
        return TransformCall(service.listParticipants(request, credentials)) {
            it.participantsList
        }
    }

    fun getParticipant(roomName: String, identity: String): Call<LivekitModels.ParticipantInfo> {
        val request = LivekitRoom.RoomParticipantIdentity.newBuilder()
            .setRoom(roomName)
            .setIdentity(identity)
            .build()
        val credentials = authHeader(
            mapOf(
                RoomAdmin(true).toPair(),
                Room(roomName).toPair(),
            )
        )
        return service.getParticipant(request, credentials)
    }

    fun removeParticipant(roomName: String, identity: String): Call<Void> {
        val request = LivekitRoom.RoomParticipantIdentity.newBuilder()
            .setRoom(roomName)
            .setIdentity(identity)
            .build()
        val credentials = authHeader(
            mapOf(
                RoomAdmin(true).toPair(),
                Room(roomName).toPair(),
            )
        )
        return service.removeParticipant(request, credentials)
    }

    fun mutePublishedTrack(
        roomName: String,
        identity: String,
        trackSid: String,
        mute: Boolean,
    ): Call<LivekitModels.TrackInfo> {
        val request = LivekitRoom.MuteRoomTrackRequest.newBuilder()
            .setRoom(roomName)
            .setIdentity(identity)
            .setTrackSid(trackSid)
            .setMuted(mute)
            .build()
        val credentials = authHeader(
            mapOf(
                RoomAdmin(true).toPair(),
                Room(roomName).toPair(),
            )
        )
        return TransformCall(service.mutePublishedTrack(request, credentials)) {
            it.track
        }
    }

    fun updateParticipant(
        roomName: String,
        identity: String,
        metadata: String?,
        participantPermission: LivekitModels.ParticipantPermission?,
    ): Call<LivekitModels.ParticipantInfo> {
        val request = with(LivekitRoom.UpdateParticipantRequest.newBuilder()) {
            this.room = roomName
            this.identity = identity
            if (metadata != null) {
                this.metadata = metadata
            }
            if (participantPermission != null) {
                this.permission = permission
            }
            build()
        }

        val credentials = authHeader(
            mapOf(
                RoomAdmin(true).toPair(),
                Room(roomName).toPair(),
            )
        )
        return service.updateParticipant(request, credentials)
    }

    fun updateSubscriptions(
        roomName: String,
        identity: String,
        trackSids: List<String>,
        subscribe: Boolean,
    ): Call<Void> {
        val request = with(LivekitRoom.UpdateSubscriptionsRequest.newBuilder()) {
            this.room = roomName
            this.identity = identity
            addAllTrackSids(trackSids)
            this.subscribe = subscribe
            build()
        }

        val credentials = authHeader(
            mapOf(
                RoomAdmin(true).toPair(),
                Room(roomName).toPair(),
            )
        )
        return service.updateSubscriptions(request, credentials)
    }

    @JvmOverloads
    fun sendData(
        roomName: String,
        data: ByteArray,
        kind: LivekitModels.DataPacket.Kind,
        destinationSids: List<String> = emptyList(),
    ): Call<Void> {
        val request = with(LivekitRoom.SendDataRequest.newBuilder()) {
            this.room = roomName
            this.data = ByteString.copyFrom(data)
            this.kind = kind
            addAllDestinationSids(destinationSids)
            build()
        }

        val credentials = authHeader(
            mapOf(
                RoomAdmin(true).toPair(),
                Room(roomName).toPair(),
            )
        )
        return service.sendData(request, credentials)
    }

    private fun authHeader(videoGrants: Map<String, Any>): String {
        val jwt = Jwts.builder()
            .setIssuer(apiKey)
            .addClaims(
                mapOf(
                    "video" to videoGrants,
                )
            )
            .signWith(
                SecretKeySpec(secret.toByteArray(), "HmacSHA256"),
                SignatureAlgorithm.HS256
            )
            .compact()

        return "Bearer $jwt"
    }

    companion object {

        @JvmStatic
        @JvmOverloads
        fun create(host: String, apiKey: String, secret: String, logging: Boolean = false): RoomServiceClient {

            val okhttp = with(OkHttpClient.Builder()) {
                if (logging) {
                    val loggingInterceptor = HttpLoggingInterceptor()
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                    addInterceptor(loggingInterceptor)
                }
                build()
            }
            val service = Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(ProtoConverterFactory.create())
                .client(okhttp)
                .build()
                .create(RoomService::class.java)

            return RoomServiceClient(service, apiKey, secret)
        }
    }
}