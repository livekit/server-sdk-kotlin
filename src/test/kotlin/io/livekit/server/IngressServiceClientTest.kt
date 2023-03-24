package io.livekit.server

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IngressServiceClientTest {

    companion object {
        const val HOST = TestConstants.HOST
        const val KEY = TestConstants.KEY
        const val SECRET = TestConstants.SECRET
    }

    lateinit var client: IngressServiceClient

    @BeforeTest
    fun setup() {
        client = IngressServiceClient.create(HOST, KEY, SECRET, true)
    }

    @Test
    fun listIngress() {
        val response = client.listIngress().execute()
        val body = response.body()
        assertTrue(response.isSuccessful)
        assertNotNull(body)
        assertTrue(body.isEmpty())
    }
}
