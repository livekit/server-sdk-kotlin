package io.livekit.server

/**
 * @link https://docs.livekit.io/guides/access-tokens/#video-grant
 */
sealed class VideoGrant(val key: String, val value: Any) {
    fun toPair() = key to value
}

/**
 * permission to create rooms
 */
class RoomCreate(value: Boolean) : VideoGrant("roomCreate", value)

/**
 * permission to join a room
 */
class RoomJoin(value: Boolean) : VideoGrant("roomJoin", value)

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
 * name of the room, required if join or admin is set
 * @see [RoomName]
 */
@Deprecated("Use io.livekit.server.RoomName")
class Room(value: String) : VideoGrant("room", value)

/**
 * name of the room, required if join or admin is set
 */
class RoomName(value: String) : VideoGrant("room", value)


/**
 * allow participant to publish tracks
 */
class CanPublish(value: Boolean) : VideoGrant("canPublish", value)

/**
 * allow participant to publish data to the room
 */
class CanSubscribe(value: Boolean) : VideoGrant("canSubscribe", value)

/**
 * allow participant to subscribe to tracks
 */
class CanPublishData(value: Boolean) : VideoGrant("canPublishData", value)

/**
 * hide participant from others (used by recorder)
 */
class Hidden(value: Boolean) : VideoGrant("hidden", value)

/**
 * indicates this participant is recording the room
 */
class Recorder(value: Boolean) : VideoGrant("recorder", value)


/**
 * permission to manage ingress
 */
class IngressAdmin(value: Boolean) : VideoGrant("ingressAdmin", value)

