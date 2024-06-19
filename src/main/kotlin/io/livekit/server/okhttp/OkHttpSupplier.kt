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