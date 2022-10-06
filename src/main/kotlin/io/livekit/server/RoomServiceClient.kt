import com.google.common.util.concurrent.ListenableFuture
import io.grpc.ManagedChannelBuilder
import io.livekit.server.JwtCredential
import io.livekit.server.RoomCreate
import io.livekit.server.RoomList
import livekit.LivekitModels
import livekit.LivekitRoom
import livekit.RoomServiceGrpc
import java.util.logging.Logger

class RoomServiceClient(
    private val host: String,
    private val apiKey: String,
    private val secret: String,
) {

    private val channel = ManagedChannelBuilder.forTarget(host)
        .usePlaintext()
        .build()
    private val stub = RoomServiceGrpc.newFutureStub(channel)

    private fun createCredentials(videoGrants: Map<String, Any> = emptyMap()) =
        JwtCredential(apiKey, secret, videoGrants)

    fun createRoom(options: CreateOptions): ListenableFuture<LivekitModels.Room> {
        val request = LivekitRoom.CreateRoomRequest.newBuilder()
            .setName(options.name)
            .build()
        val credentials = createCredentials(mapOf(RoomCreate(true).toPair()))
        return stub.withCallCredentials(credentials).createRoom(request)
    }

    fun listRooms(names: List<String>?) {
        val request = with(LivekitRoom.ListRoomsRequest.newBuilder()) {
            if (names != null) {
                addAllNames(names)
            }
            build()
        }

        val credentials = createCredentials(mapOf(RoomList(true).toPair()))
        stub.withCallCredentials(credentials).listRooms(request)
    }

    fun deleteRoom(roomname: String) {

    }

    companion object {
        private val logger = Logger.getLogger(RoomServiceClient::class.java.name)
    }
}

data class CreateOptions(
    val name: String,
    val emptyTimeout: Int? = null,
    val maxParticipants: Int? = null,
    val nodeId: String? = null,
)