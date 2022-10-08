package io.livekit.server

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EgressServiceClientTest {

    companion object {
        const val HOST = TestConstants.HOST
        const val KEY = TestConstants.KEY
        const val SECRET = TestConstants.SECRET
    }

    lateinit var client: EgressServiceClient

    @BeforeTest
    fun setup() {
        client = EgressServiceClient.create(HOST, KEY, SECRET, true)
    }

    @Test
    fun listEgress() {
        val response = client.listEgress().execute()
        val body = response.body()
        assertTrue(response.isSuccessful)
        assertNotNull(body)
        assertTrue(body.isEmpty())
    }
}