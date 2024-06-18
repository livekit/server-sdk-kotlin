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

import io.livekit.server.okhttp.OkHttpFactory
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
        client = IngressServiceClient.createClient(HOST, KEY, SECRET, OkHttpFactory(true))
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
