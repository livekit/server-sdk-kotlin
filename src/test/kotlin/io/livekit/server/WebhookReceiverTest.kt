package io.livekit.server

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class WebhookReceiverTest {

    @Test
    fun receive() {
        val body =
            """{"event":"room_started", "room":{"sid":"RM_TkVjUvAqgzKz", "name":"mytestroom", "emptyTimeout":300, "creationTime":"1628545903", "turnPassword":"ICkSr2rEeslkN6e9bXL4Ji5zzMD5Z7zzr6ulOaxMj6N", "enabledCodecs":[{"mime":"audio/opus"}, {"mime":"video/VP8"}]}}"""
        val testApiKey = "abcdefg"
        val testSecret = "ababababababababababababababababababababababababababababababa"

        val token = AccessToken(testApiKey, testSecret)
        token.sha256 = "CoEQz1chqJ9bnZRcORddjplkvpjmPujmLTR42DbefYI="
        val jwt = token.toJwt()

        val receiver = WebhookReceiver(testApiKey, testSecret)

        val event = receiver.receive(body, jwt)

        assertEquals("mytestroom", event.room.name)
        assertEquals("room_started", event.event)
    }
}