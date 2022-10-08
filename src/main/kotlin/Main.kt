import com.google.protobuf.util.JsonFormat
import io.livekit.server.RoomServiceClient

fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    val key = ""
    val secret = ""

    val client = RoomServiceClient.create("http://example.com", key, secret)

    val job = client.listParticipants("room_name")
    val participants = job.execute()

    participants.body()!!.forEach {
        println(JsonFormat.printer().print(it))
    }
}