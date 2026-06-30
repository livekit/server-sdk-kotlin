/*
 * Copyright 2024-2025 LiveKit, Inc.
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
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import com.google.protobuf.MessageOrBuilder
import livekit.LivekitRoom.RoomConfiguration
import java.time.Instant
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Access tokens are required to connect to the server.
 *
 * Once information is filled out, create the token string with [toJwt].
 *
 * https://docs.livekit.io/home/get-started/authentication/
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class AccessToken(private val apiKey: String, private val secret: String) {
    private val videoGrants = mutableSetOf<VideoGrant>()
    private val sipGrants = mutableSetOf<SIPGrant>()

    /**
     * Amount of time in milliseconds the created token is valid for.
     *
     * If [expiration] is not null, this value is ignored.
     *
     * Defaults to 6 hours.
     */
    var ttl: Long = TimeUnit.MILLISECONDS.convert(6, TimeUnit.HOURS)

    /**
     * Used to specify a expiration time.
     *
     * If not null, takes preference over [ttl].
     */
    var expiration: Date? = null

    /**
     * Date specifying the time
     * [before which this token is invalid](https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.5)
     * .
     */
    var notBefore: Date? = null

    /** Display name for the participant, available as `Participant.name` */
    var name: String? = null

    /** Unique identity of the user, required for room join tokens */
    var identity: String? = null

    /** Custom metadata to be passed to participants */
    var metadata: String? = null

    /** For verifying integrity of message body */
    var sha256: String? = null

    /** Key/value attributes to attach to the participant */
    val attributes = mutableMapOf<String, String>()

    /**
     * Use a named preset room configuration.
     *
     * Any options set in [roomConfiguration] will take precedence.
     */
    var roomPreset: String? = null

    /** Configuration for when creating a room. */
    var roomConfiguration: RoomConfiguration? = null

    /** Add [VideoGrant] to this token. */
    fun addGrants(vararg grants: VideoGrant) {
        for (grant in grants) {
            videoGrants.add(grant)
        }
    }

    /** Add [VideoGrant] to this token. */
    fun addGrants(grants: Iterable<VideoGrant>) {
        for (grant in grants) {
            videoGrants.add(grant)
        }
    }

    /** Clear all previously added [VideoGrant]s. */
    fun clearGrants() {
        videoGrants.clear()
    }

    /** Add [VideoGrant] to this token. */
    fun addSIPGrants(vararg grants: SIPGrant) {
        for (grant in grants) {
            sipGrants.add(grant)
        }
    }

    /** Add [VideoGrant] to this token. */
    fun addSIPGrants(grants: Iterable<SIPGrant>) {
        for (grant in grants) {
            sipGrants.add(grant)
        }
    }

    /** Clear all previously added [SIPGrant]s. */
    fun clearSIPGrants() {
        sipGrants.clear()
    }

    fun toJwt(): String {
        return with(JWT.create()) {
            withIssuer(apiKey)
            val exp = expiration
            if (exp != null) {
                withExpiresAt(exp)
            } else {
                withExpiresAt(Date(System.currentTimeMillis() + ttl))
            }

            val nbf = notBefore
            if (nbf != null) {
                withNotBefore(nbf)
            }

            val id = identity
            if (id != null) {
                withSubject(id)
            } else {
                val hasRoomJoin = videoGrants.any { it is RoomJoin && it.value == true }
                if (hasRoomJoin) {
                    throw IllegalStateException("identity is required for join, but is not set.")
                }
            }
            val claimsMap = mutableMapOf<String, Any>()
            val videoGrantsMap = videoGrants.associate { grant -> grant.toPair() }
            val sipGrantsMap = sipGrants.associate { grant -> grant.toPair() }

            name?.let { claimsMap["name"] = it }
            metadata?.let { claimsMap["metadata"] = it }
            sha256?.let { claimsMap["sha256"] = it }
            roomPreset?.let { claimsMap["roomPreset"] = it }
            attributes.toMap().let { attributesCopy ->
                if (attributesCopy.isNotEmpty()) {
                    claimsMap["attributes"] = attributesCopy
                }
            }
            roomConfiguration?.let { claimsMap["roomConfig"] = it.toMap() }

            claimsMap["video"] = videoGrantsMap
            claimsMap["sip"] = sipGrantsMap

            claimsMap.forEach { (key, value) -> withClaimAny(key, value) }

            val alg = Algorithm.HMAC256(secret)

            // Build token
            sign(alg)
        }
    }
}

internal fun JWTCreator.Builder.withClaimAny(name: String, value: Any) {
    when (value) {
        is Boolean -> withClaim(name, value)
        is Int -> withClaim(name, value)
        is Long -> withClaim(name, value)
        is Double -> withClaim(name, value)
        is String -> withClaim(name, value)
        is Date -> withClaim(name, value)
        is Instant -> withClaim(name, value)
        is List<*> -> withClaim(name, value)
        is Map<*, *> -> {
            @Suppress("UNCHECKED_CAST") withClaim(name, value as Map<String, *>)
        }
    }
}

internal fun MessageOrBuilder.toMap(): Map<String, *> = buildMap {
    for ((field, value) in allFields) {
        put(
            field.name,
            when (value) {
                is MessageOrBuilder -> value.toMap()
                is List<*> ->
                    value.map { item ->
                        when (item) {
                            is MessageOrBuilder -> item.toMap()
                            else -> if (isSupportedType(item)) item else item.toString()
                        }
                    }
                else -> if (isSupportedType(value)) value else value.toString()
            }
        )
    }
}

private fun isSupportedType(value: Any?) =
    value == null ||
        value is Boolean ||
        value is Int ||
        value is Long ||
        value is Double ||
        value is String ||
        value is Map<*, *> ||
        value is List<*>
