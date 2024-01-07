/*
 * Copyright 2024 LiveKit, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.livekit.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.protobuf.util.JsonFormat
import livekit.LivekitWebhook
import java.security.MessageDigest
import java.util.*

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

            val alg = Algorithm.HMAC256(secret)
            val decodedJWT = JWT.require(alg)
                .withIssuer(apiKey)
                .build()
                .verify(authHeader)

            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(body.toByteArray())
            val hash = Base64.getEncoder().encodeToString(hashBytes)

            if (decodedJWT.getClaim("sha256")?.asString() != hash) {
                throw IllegalArgumentException("sha256 checksum of body does not match!")
            }
        }

        val builder = LivekitWebhook.WebhookEvent.newBuilder()
        JsonFormat.parser().ignoringUnknownFields().merge(body, builder)
        return builder.build()
    }
}
