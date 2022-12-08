package io.livekit.server

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Access tokens are required to connect to the server.
 *
 * Once information is filled out, create the token string with [toJwt].
 *
 * https://docs.livekit.io/guides/access-tokens
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class AccessToken(
    private val apiKey: String,
    private val secret: String
) {
    private val videoGrants = mutableSetOf<VideoGrant>()

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
     * Date specifying the time [before which this token is invalid](https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-25#section-4.1.5).
     */
    var notBefore: Date? = null

    /**
     * Display name for the participant, available as `Participant.name`
     */
    var name: String? = null

    /**
     * Unique identity of the user, required for room join tokens
     */
    var identity: String? = null

    /**
     * Custom metadata to be passed to participants
     */
    var metadata: String? = null

    /**
     * For verifying integrity of message body
     */
    var sha256: String? = null

    /**
     * Add [VideoGrant] to this token.
     */
    fun addGrants(vararg grants: VideoGrant) {
        for (grant in grants) {
            videoGrants.add(grant)
        }
    }

    /**
     * Add [VideoGrant] to this token.
     */
    fun addGrants(grants: Iterable<VideoGrant>) {
        for (grant in grants) {
            videoGrants.add(grant)
        }
    }

    fun clearGrants() {
        videoGrants.clear()
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
                withJWTId(id)
            } else {
                val hasRoomJoin = videoGrants.any { it is RoomJoin && it.value == true }
                if (hasRoomJoin) {
                    throw IllegalStateException("identity is required for join, but is not set.")
                }
            }
            val claimsMap = mutableMapOf<String, Any>()
            val videoGrantsMap = videoGrants.associate { grant -> grant.toPair() }

            name?.let { claimsMap["name"] = it }
            metadata?.let { claimsMap["metadata"] = it }
            sha256?.let { claimsMap["sha256"] = it }
            claimsMap["video"] = videoGrantsMap

            claimsMap.forEach { key, value ->
                withClaimAny(key, value)
            }

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
            @Suppress("UNCHECKED_CAST")
            withClaim(name, value as Map<String, *>)
        }
    }
}