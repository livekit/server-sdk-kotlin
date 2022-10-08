package io.livekit.server

import com.google.protobuf.util.JsonFormat
import livekit.LivekitWebhook

class WebhookReceiver(
    private val apiKey: String,
    private val secret: String,
) {

    /**
     * Validates a webhook message and converts it to a [LivekitWebhook.WebhookEvent] object.
     */
    @JvmOverloads
    @Throws
    fun receive(body: String, authHeader: String?, skipAuth: Boolean = false): LivekitWebhook.WebhookEvent {
        // Verify token
        if (!skipAuth) {
            requireNotNull(authHeader) { "Auth header is null!" }
            require(authHeader.isNotBlank()) { "Auth header is blank!" }
            // TODO
        }

        val builder = LivekitWebhook.WebhookEvent.newBuilder()
        JsonFormat.parser().merge(body, builder)
        return builder.build()
    }
}