package io.livekit.server

import io.jsonwebtoken.Jwts
import org.junit.jupiter.api.Test
import java.util.*
import javax.crypto.spec.SecretKeySpec
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AccessTokenTest {

    companion object {
        const val KEY = "key"
        const val SECRET = "ababababababababababababababababababababababababababababababa"
    }

    @Test
    fun createToken() {
        val token = AccessToken(KEY, SECRET)

        token.expiration = Date(33254282804000) // 10/15/3023
        token.name = "name"
        token.identity = "identity"
        token.metadata = "metadata"

        token.addGrants(Room("room_name"))

        val jwt = token.toJwt()

        val claimsJws = Jwts.parserBuilder()
            .setSigningKey(SecretKeySpec(SECRET.toByteArray(), "HmacSHA256"))
            .build()
            .parseClaimsJws(jwt)

        assertEquals(KEY, claimsJws.body["iss"])
        assertEquals(token.name, claimsJws.body["name"])
        assertEquals(token.identity, claimsJws.body["jti"])
        assertEquals(token.metadata, claimsJws.body["metadata"])

        val videoGrants = claimsJws.body["video"] as? Map<*, *>
        assertNotNull(videoGrants)
        assertEquals("room_name", videoGrants["room"])
    }
}