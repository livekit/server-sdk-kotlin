/*
 * Copyright 2025 LiveKit, Inc.
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

package io.livekit.server.okhttp

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Holds an [OkHttpClient] object that is used as-is when supplying one.
 */
class OkHttpHolder(val okHttp: OkHttpClient) : Supplier<OkHttpClient> {
    override fun get() = okHttp
}

/**
 * Lazily creates and caches an [OkHttpClient] object.
 */
class OkHttpFactory
@JvmOverloads
constructor(
    /**
     * When set to true, turns on body level logging.
     */
    val logging: Boolean = false,
    /**
     * Provide this if you wish to customize the http client
     * (e.g. proxy, timeout, certificate/auth settings)
     */
    val okHttpConfigurator: Consumer<OkHttpClient.Builder>? = null
) : Supplier<OkHttpClient> {

    val okHttp by lazy {
        with(OkHttpClient.Builder()) {
            if (logging) {
                val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                addInterceptor(loggingInterceptor)
            }
            okHttpConfigurator?.accept(this)
            build()
        }
    }

    override fun get() = okHttp
}
