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

import com.google.protobuf.ByteString
import io.livekit.server.okhttp.OkHttpFactory
import io.livekit.server.okhttp.OkHttpHolder
import io.livekit.server.retrofit.TransformCall
import livekit.LivekitModels
import livekit.LivekitRoom
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.nio.ByteBuffer
import java.util.UUID
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * A client for interacting managing LiveKit rooms and participants.
 *
 * See: [Managing Rooms](https://docs.livekit.io/realtime/server/managing-rooms/)
 */
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
        minPlayoutDelay: Int? = null,
        maxPlayoutDelay: Int? = null,
        syncStreams: Boolean? = null,
        departureTimeout: Int? = null,
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
            if (minPlayoutDelay != null) {
                this.minPlayoutDelay = minPlayoutDelay
            }
            if (maxPlayoutDelay != null) {
                this.maxPlayoutDelay = maxPlayoutDelay
            }
            if (syncStreams != null) {
                this.syncStreams = syncStreams
            }
            if (departureTimeout != null) {
                this.departureTimeout = departureTimeout
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

    fun deleteRoom(roomName: String): Call<Void?> {
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
    fun removeParticipant(roomName: String, identity: String): Call<Void?> {
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
     * Forward a participant's track(s) to another room. Requires `roomAdmin` and `destinationRoom`. The forwarding will
     * stop when the participant leaves the room or `RemoveParticipant` has been called in the destination room.
     * A participant can be forwarded to multiple rooms. The destination room will be created if it does not exist.
     * @param roomName
     * @param identity
     * @param destinationRoomName
     */
    fun forwardParticipant(roomName: String, identity: String, destinationRoomName: String): Call<LivekitRoom.ForwardParticipantResponse> {
        val request = LivekitRoom.ForwardParticipantRequest.newBuilder()
            .setRoom(roomName)
            .setIdentity(identity)
            .setDestinationRoom(destinationRoomName)
            .build()
        val credentials = authHeader(
            RoomAdmin(true),
            RoomName(roomName),
            DestinationRoomName(destinationRoomName),
        )
        return service.forwardParticipant(request, credentials)
    }

    /**
     * Move a participant from one room to another room.
     * @param roomName
     * @param identity
     * @param destinationRoomName
     */
    fun moveParticipant(roomName: String, identity: String, destinationRoomName: String): Call<LivekitRoom.MoveParticipantResponse> {
        val request = LivekitRoom.MoveParticipantRequest.newBuilder()
            .setRoom(roomName)
            .setIdentity(identity)
            .setDestinationRoom(destinationRoomName)
            .build()
        val credentials = authHeader(
            RoomAdmin(true),
            RoomName(roomName),
            DestinationRoomName(destinationRoomName),
        )
        return service.moveParticipant(request, credentials)
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
     * @param attributes attributes to update. It will make updates only to keys that
     * are present in [attributes], and will not override others. To delete a value,
     * set the value to an empty string.
     */
    @JvmOverloads
    fun updateParticipant(
        roomName: String,
        identity: String,
        name: String? = null,
        metadata: String? = null,
        participantPermission: LivekitModels.ParticipantPermission? = null,
        attributes: Map<String, String>? = null,
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
            if (attributes != null) {
                this.putAllAttributes(attributes)
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
    ): Call<Void?> {
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
        destinationIdentities: List<String> = emptyList(),
        topic: String? = null,
    ): Call<Void?> {
        val uuid = UUID.randomUUID()
        val b = ByteBuffer.wrap(ByteArray(16))
        b.putLong(uuid.mostSignificantBits)
        b.putLong(uuid.leastSignificantBits)

        val request = with(LivekitRoom.SendDataRequest.newBuilder()) {
            this.room = roomName
            this.data = ByteString.copyFrom(data)
            this.kind = kind
            addAllDestinationSids(destinationSids)
            addAllDestinationIdentities(destinationIdentities)
            if (topic != null) {
                this.topic = topic
            }
            this.nonce = ByteString.copyFrom(b)
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
        @Deprecated(
            "Use RoomServiceClient.createClient()",
            ReplaceWith(
                "RoomServiceClient.createClient(host, apiKey, secret, OkHttpFactory(logging, okHttpConfigurator))",
                "import io.livekit.server.okhttp.OkHttpFactory",
            )
        )
        @JvmStatic
        @JvmOverloads
        fun create(
            host: String,
            apiKey: String,
            secret: String,
            logging: Boolean = false,
            okHttpConfigurator: Consumer<OkHttpClient.Builder>? = null
        ): RoomServiceClient {
            return createClient(
                host = host,
                apiKey = apiKey,
                secret = secret,
                okHttpSupplier = OkHttpFactory(
                    logging = logging,
                    okHttpConfigurator = okHttpConfigurator,
                )
            )
        }

        /**
         * Create a RoomServiceClient.
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
            okHttpSupplier: Supplier<OkHttpClient> = OkHttpFactory()
        ): RoomServiceClient {
            val okhttp = okHttpSupplier.get()
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
