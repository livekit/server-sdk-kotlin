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

/**
 * See: [Video Grants](https://docs.livekit.io/home/get-started/authentication/#Video-grant)
 */
sealed class VideoGrant(val key: String, val value: Any) {
    fun toPair() = key to value
}

// Protocol videogrant list can be found at
// https://github.com/livekit/protocol/blob/main/auth/grants.go

/**
 * permission to create rooms
 */
class RoomCreate(value: Boolean) : VideoGrant("roomCreate", value)

/**
 * permission to list available rooms
 */
class RoomList(value: Boolean) : VideoGrant("roomList", value)

/**
 * permissions to use Egress service
 */
class RoomRecord(value: Boolean) : VideoGrant("roomRecord", value)

/**
 * permission to moderate a room
 */
class RoomAdmin(value: Boolean) : VideoGrant("roomAdmin", value)

/**
 * permission to join a room
 */
class RoomJoin(value: Boolean) : VideoGrant("roomJoin", value)

/**
 * name of the room, required if join or admin is set
 * @see [RoomName]
 */
@Deprecated("Use io.livekit.server.RoomName")
class Room(value: String) : VideoGrant("room", value)

/**
 * name of the room, required if join or admin is set
 */
class RoomName(value: String) : VideoGrant("room", value)

class DestinationRoomName(value: String) : VideoGrant("destinationRoom", value)

/**
 * allow participant to publish tracks
 */
class CanPublish(value: Boolean) : VideoGrant("canPublish", value)

/**
 * allow participant to subscribe to the room
 */
class CanSubscribe(value: Boolean) : VideoGrant("canSubscribe", value)

/**
 * allow participant to publish data to tracks
 */
class CanPublishData(value: Boolean) : VideoGrant("canPublishData", value)

/**
 * TrackSource types that a participant may publish.
 * When set, it supersedes CanPublish. Only sources explicitly set here can be published
 *
 * Currently available sources:
 * * "camera"
 * * "microphone"
 * * "screen_share"
 * * "screen_share_audio"
 */
class CanPublishSources(value: List<String>) : VideoGrant("canPublishSources", value)

/**
 * allow participant to update its own metadata
 */
class CanUpdateOwnMetadata(value: Boolean) : VideoGrant("canUpdateOwnMetadata", value)

/**
 * permission to manage ingress
 */
class IngressAdmin(value: Boolean) : VideoGrant("ingressAdmin", value)

/**
 * hide participant from others (used by recorder)
 */
class Hidden(value: Boolean) : VideoGrant("hidden", value)

/**
 * indicates this participant is recording the room
 */
class Recorder(value: Boolean) : VideoGrant("recorder", value)

/**
 * indicates this participant is allowed to connect to LiveKit as an Agent Framework worker
 */
class Agent(value: Boolean) : VideoGrant("agent", value)
