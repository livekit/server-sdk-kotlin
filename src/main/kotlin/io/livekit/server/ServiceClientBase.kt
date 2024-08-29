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

import java.util.concurrent.TimeUnit

open class ServiceClientBase(
    private val apiKey: String,
    private val secret: String,
    private val ttl: Long = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES),
) {

    protected fun authHeader(vararg videoGrants: VideoGrant): String {
        val accessToken = AccessToken(apiKey, secret)
        accessToken.addGrants(*videoGrants)
        accessToken.ttl = ttl

        val jwt = accessToken.toJwt()

        return "Bearer $jwt"
    }

    protected fun authHeader(videoGrants: List<VideoGrant> = emptyList(), sipGrants: List<SIPGrant> = emptyList()): String {
        val accessToken = AccessToken(apiKey, secret)
        accessToken.addGrants(videoGrants)
        accessToken.addSIPGrants(sipGrants)
        accessToken.ttl = ttl
        val jwt = accessToken.toJwt()

        return "Bearer $jwt"
    }
}
