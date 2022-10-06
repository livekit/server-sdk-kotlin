import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.livekit.server.RoomCreate
import io.livekit.server.RoomService
import livekit.LivekitModels
import livekit.LivekitRoom
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.util.logging.Logger
import javax.crypto.spec.SecretKeySpec

class RoomServiceClient(
    private val host: String,
    private val apiKey: String,
    private val secret: String,
) {

    val okhttp = with(OkHttpClient.Builder()) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        addInterceptor(loggingInterceptor)
        build()
    }
    val service = Retrofit.Builder()
        .baseUrl(host)
        .addConverterFactory(ProtoConverterFactory.create())
        .client(okhttp)
        .build()
        .create(RoomService::class.java)

    fun createRoom(options: CreateOptions): Call<LivekitModels.Room> {
        val request = LivekitRoom.CreateRoomRequest.newBuilder()
            .setName(options.name)
            .build()
        val credentials = authHeader(mapOf(RoomCreate(true).toPair()))
        return service.createRoom(request, credentials)
    }

    fun listRooms(names: List<String>?) {
        val request = with(LivekitRoom.ListRoomsRequest.newBuilder()) {
            if (names != null) {
                addAllNames(names)
            }
            build()
        }

    }

    fun deleteRoom(roomname: String) {

    }

    fun authHeader(videoGrants: Map<String, Any>): String {
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
        private val logger = Logger.getLogger(RoomServiceClient::class.java.name)
    }
}

data class CreateOptions(
    val name: String,
    val emptyTimeout: Int? = null,
    val maxParticipants: Int? = null,
    val nodeId: String? = null,
)