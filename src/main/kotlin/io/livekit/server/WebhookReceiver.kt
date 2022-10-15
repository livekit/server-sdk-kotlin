package io.livekit.server

import com.google.protobuf.util.JsonFormat
import io.jsonwebtoken.Jwts
import livekit.LivekitWebhook
import java.security.MessageDigest
import java.util.*
import javax.crypto.spec.SecretKeySpec


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

            val claimsJws = Jwts.parserBuilder()
                .setSigningKey(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
                .build()
                .parseClaimsJws(authHeader)

            if (claimsJws.body["iss"] != apiKey) {
                throw IllegalArgumentException("Issuer doesn't match the given key!")
            }
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(body.toByteArray())
            val hash = Base64.getEncoder().encodeToString(hashBytes)

            if (claimsJws.body["sha256"] != hash) {
                throw IllegalArgumentException("sha256 checksum of body does not match!")
            }
        }

        val builder = LivekitWebhook.WebhookEvent.newBuilder()
        JsonFormat.parser().merge(body, builder)
        return builder.build()
    }
}