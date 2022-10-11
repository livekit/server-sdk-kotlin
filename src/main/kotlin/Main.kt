import com.google.protobuf.util.JsonFormat
import io.livekit.server.AccessToken
import io.livekit.server.Room
import io.livekit.server.RoomJoin
import io.livekit.server.RoomServiceClient

// TODO: support basic actions from main.
fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    val key = ""
    val secret = ""

    // Example call.
    val client = RoomServiceClient.create("http://example.com", key, secret)

    val job = client.listParticipants("room_name")
    val participants = job.execute()

    participants.body()!!.forEach {
        println(JsonFormat.printer().print(it))
    }

    // Example of creating token.
    val token = AccessToken(key, secret)

    token.name = "name"
    token.identity = "identity"
    token.metadata = "metadata"
    token.addGrants(RoomJoin(true), Room("myroom"))

    println(token.toJwt())
}