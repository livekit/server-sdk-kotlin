fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    val key = "APITLWrK8tbwr47"
    val secret = "lmFSh8b0DBwoakeHcuj9l7JQvSk0AuSf3AJ1HGvtgneB"

    val client = RoomServiceClient("192.168.11.5:7800", key, secret)

    val job = client.createRoom(CreateOptions(name = "asdffff"))
    val room = job.get()

    println(room.toString())
}