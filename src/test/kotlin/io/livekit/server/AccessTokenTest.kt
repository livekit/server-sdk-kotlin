package io.livekit.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AccessTokenTest {

    companion object {
        const val KEY = "abcdefg"
        const val SECRET = "abababa"
    }

    @Test
    fun createToken() {
        val token = AccessToken(KEY, SECRET)

        token.expiration = Date(33254282804000) // 10/15/3023
        token.name = "name"
        token.identity = "identity"
        token.metadata = "metadata"
        token.sha256 = "gfedcba"

        token.addGrants(RoomName("room_name"))

        val jwt = token.toJwt()

        val alg = Algorithm.HMAC256(SECRET)
        val decodedJWT = JWT.require(alg)
            .withIssuer(KEY)
            .build()
            .verify(jwt)

        val claims = decodedJWT.claims

        assertEquals(KEY, claims["iss"]?.asString())
        assertEquals(token.name, claims["name"]?.asString())
        assertEquals(token.identity, claims["jti"]?.asString())
        assertEquals(token.metadata, claims["metadata"]?.asString())
        assertEquals(token.sha256, claims["sha256"]?.asString())
        assertEquals(token.expiration, decodedJWT.expiresAt)

        val videoGrants = claims["video"]?.asMap()
        assertNotNull(videoGrants)
        assertEquals("room_name", videoGrants["room"])
    }
}