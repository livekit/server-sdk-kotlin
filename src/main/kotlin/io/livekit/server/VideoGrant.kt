package io.livekit.server

sealed class VideoGrant(private val key: String, private val value: Any) {
    fun toPair() = key to value
}

class RoomCreate(value: Boolean) : VideoGrant("roomCreate", value)
class RoomJoin(value: Boolean) : VideoGrant("roomJoin", value)
class RoomList(value: Boolean) : VideoGrant("roomList", value)
class RoomRecord(value: Boolean) : VideoGrant("roomRecord", value)
class RoomAdmin(value: Boolean) : VideoGrant("roomAdmin", value)
class Room(value: String) : VideoGrant("room", value)
class CanPublish(value: Boolean) : VideoGrant("canPublish", value)
class CanSubscribe(value: Boolean) : VideoGrant("canSubscribe", value)
class CanPublishData(value: Boolean) : VideoGrant("canPublishData", value)
class Hidden(value: Boolean) : VideoGrant("hidden", value)
class Recorder(value: Boolean) : VideoGrant("recorder", value)