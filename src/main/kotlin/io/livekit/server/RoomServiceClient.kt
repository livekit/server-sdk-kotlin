package io.livekit.server

import com.google.protobuf.ByteString
import io.livekit.server.retrofit.TransformCall
import livekit.LivekitModels
import livekit.LivekitRoom
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory

class RoomServiceClient(
    private val service: RoomService,
    private val apiKey: String,
    private val secret: String,
) {

    /**
     * Creates a new room. Explicit room creation is not required, since rooms will
     * be automatically created when the first participant joins. This method can be
     * used to customize room settings.
     */
    @JvmOverloads
    fun createRoom(
        name: String,
        emptyTimeout: Int? = null,
        maxParticipants: Int? = null,
        nodeId: String? = null,
        metadata: String? = null,
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
            if (metadata != null) {
                this.metadata = metadata
            }
            build()
        }
        val credentials = authHeader(RoomCreate(true))
        return service.createRoom(request, credentials)
    }

    /**
     * List active rooms
     * @param names when null or empty, list all rooms.
     *              otherwise returns rooms with matching names
     */
    @JvmOverloads
    fun listRooms(names: List<String>? = null): Call<List<LivekitModels.Room>> {
        val request = with(LivekitRoom.ListRoomsRequest.newBuilder()) {
            if (names != null) {
                addAllNames(names)
            }
            build()
        }
        val credentials = authHeader(RoomList(true))
        return TransformCall(service.listRooms(request, credentials)) {
            it.roomsList
        }
    }

    fun deleteRoom(roomName: String): Call<Void> {
        val request = LivekitRoom.DeleteRoomRequest.newBuilder()
            .setRoom(roomName)
            .build()
        val credentials = authHeader(RoomCreate(true))
        return service.deleteRoom(request, credentials)
    }

    /**
     * Update metadata of a room
     * @param roomName name of the room
     * @param metadata the new metadata for the room
     */
    fun updateRoomMetadata(roomName: String, metadata: String): Call<LivekitModels.Room> {
        val request = LivekitRoom.UpdateRoomMetadataRequest.newBuilder()
            .setRoom(roomName)
            .setMetadata(metadata)
            .build()
        val credentials = authHeader(
            RoomAdmin(true),
            RoomName(roomName),
        )
        return service.updateRoomMetadata(request, credentials)
    }

    /**
     * List participants in a room
     * @param roomName name of the room
     */
    fun listParticipants(roomName: String): Call<List<LivekitModels.ParticipantInfo>> {
        val request = LivekitRoom.ListParticipantsRequest.newBuilder()
            .setRoom(roomName)
            .build()
        val credentials = authHeader(
            RoomAdmin(true),
            RoomName(roomName),
        )
        return TransformCall(service.listParticipants(request, credentials)) {
            it.participantsList
        }
    }

    /**
     * Get information on a specific participant, including the tracks that participant
     * has published
     * @param roomName name of the room
     * @param identity identity of the participant to return
     */
    fun getParticipant(roomName: String, identity: String): Call<LivekitModels.ParticipantInfo> {
        val request = LivekitRoom.RoomParticipantIdentity.newBuilder()
            .setRoom(roomName)
            .setIdentity(identity)
            .build()
        val credentials = authHeader(
            RoomAdmin(true),
            RoomName(roomName),
        )
        return service.getParticipant(request, credentials)
    }

    /**
     * Removes a participant in the room. This will disconnect the participant
     * and will emit a Disconnected event for that participant.
     * Even after being removed, the participant can still re-join the room.
     * @param roomName
     * @param identity
     */
    fun removeParticipant(roomName: String, identity: String): Call<Void> {
        val request = LivekitRoom.RoomParticipantIdentity.newBuilder()
            .setRoom(roomName)
            .setIdentity(identity)
            .build()
        val credentials = authHeader(
            RoomAdmin(true),
            RoomName(roomName),
        )
        return service.removeParticipant(request, credentials)
    }

    /**
     * Mutes a track that the participant has published.
     * @param roomName
     * @param identity
     * @param trackSid sid of the track to be muted
     * @param mute true to mute, false to unmute
     */
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
            RoomAdmin(true),
            RoomName(roomName),
        )
        return TransformCall(service.mutePublishedTrack(request, credentials)) {
            it.track
        }
    }

    /**
     * Updates a participant's metadata or permissions
     * @param roomName
     * @param identity
     * @param name optional, participant name to update
     * @param metadata optional, metadata to update
     * @param participantPermission optional, new permissions to assign to participant
     */
    @JvmOverloads
    fun updateParticipant(
        roomName: String,
        identity: String,
        name: String? = null,
        metadata: String? = null,
        participantPermission: LivekitModels.ParticipantPermission? = null,
    ): Call<LivekitModels.ParticipantInfo> {
        val request = with(LivekitRoom.UpdateParticipantRequest.newBuilder()) {
            this.room = roomName
            this.identity = identity
            if (name != null) {
                this.name = name
            }
            if (metadata != null) {
                this.metadata = metadata
            }
            if (participantPermission != null) {
                this.permission = participantPermission
            }
            build()
        }

        val credentials = authHeader(
            RoomAdmin(true),
            RoomName(roomName),
        )
        return service.updateParticipant(request, credentials)
    }

    /**
     * Updates a participant's subscription to tracks
     * @param roomName
     * @param identity
     * @param trackSids
     * @param subscribe true to subscribe, false to unsubscribe
     */
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
            RoomAdmin(true),
            RoomName(roomName),
        )
        return service.updateSubscriptions(request, credentials)
    }

    /**
     * Sends data message to participants in the room
     * @param room
     * @param data opaque payload to send
     * @param kind delivery reliability
     * @param destinationSids optional. when empty, message is sent to everyone
     */
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
            RoomAdmin(true),
            RoomName(roomName),
        )
        return service.sendData(request, credentials)
    }

    private fun authHeader(vararg videoGrants: VideoGrant): String {
        val accessToken = AccessToken(apiKey, secret)
        accessToken.addGrants(*videoGrants)

        val jwt = accessToken.toJwt()

        return "Bearer $jwt"
    }

    companion object {

        /**
         * Create a RoomServiceClient.
         *
         * @param okHttpConfigurator provide this if you wish to customize the http client
         * (e.g. proxy, timeout, certificate/auth settings).
         */
        @JvmStatic
        @JvmOverloads
        fun create(
            host: String,
            apiKey: String,
            secret: String,
            logging: Boolean = false,
            okHttpConfigurator: OkHttpConfigurator? = null
        ): RoomServiceClient {

            val okhttp = with(OkHttpClient.Builder()) {
                if (logging) {
                    val loggingInterceptor = HttpLoggingInterceptor()
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                    addInterceptor(loggingInterceptor)
                }
                okHttpConfigurator?.config(this)
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