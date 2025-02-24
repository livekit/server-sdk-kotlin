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

import io.livekit.server.okhttp.OkHttpFactory
import io.livekit.server.okhttp.OkHttpHolder
import io.livekit.server.retrofit.TransformCall
import livekit.LivekitIngress
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.util.function.Supplier

/**
 * A client for interacting with the Ingress service.
 *
 * See: [Ingress Reference](https://docs.livekit.io/realtime/ingress/overview/)
 */
class IngressServiceClient(
    private val service: IngressService,
    private val apiKey: String,
    private val secret: String,
) {
    /**
     * Creates a new ingress. Default audio and video options will be used if none is provided.
     */
    @JvmOverloads
    fun createIngress(
        name: String,
        roomName: String? = null,
        participantIdentity: String? = null,
        participantName: String? = null,
        inputType: LivekitIngress.IngressInput? = LivekitIngress.IngressInput.RTMP_INPUT,
        audioOptions: LivekitIngress.IngressAudioOptions? = null,
        videoOptions: LivekitIngress.IngressVideoOptions? = null,
        bypassTranscoding: Boolean? = null,
        enableTranscoding: Boolean? = null,
        url: String? = null,
    ): Call<LivekitIngress.IngressInfo> {
        val request = with(LivekitIngress.CreateIngressRequest.newBuilder()) {
            this.name = name
            this.inputType = inputType

            if (roomName != null) {
                this.roomName = roomName
            }

            if (participantIdentity != null) {
                this.participantIdentity = participantIdentity
            }

            if (participantName != null) {
                this.participantName = participantName
            }

            if (bypassTranscoding != null) {
                this.bypassTranscoding = bypassTranscoding
            }

            if (enableTranscoding != null) {
                this.enableTranscoding = enableTranscoding
            }

            if (url != null) {
                this.url = url
            }

            if (audioOptions != null) {
                this.audio = audioOptions
            }

            if (videoOptions != null) {
                this.video = videoOptions
            }
            build()
        }
        val credentials = authHeader(IngressAdmin(true))
        return service.createIngress(request, credentials)
    }

    /**
     * Updates the existing ingress with the given ingressID. Only inactive ingress can be updated
     */
    @JvmOverloads
    fun updateIngress(
        ingressID: String,
        name: String? = null,
        roomName: String? = null,
        participantIdentity: String? = null,
        participantName: String? = null,
        audioOptions: LivekitIngress.IngressAudioOptions? = null,
        videoOptions: LivekitIngress.IngressVideoOptions? = null,
        bypassTranscoding: Boolean? = null,
        enableTranscoding: Boolean? = null,
    ): Call<LivekitIngress.IngressInfo> {
        val request = with(LivekitIngress.UpdateIngressRequest.newBuilder()) {
            this.ingressId = ingressID

            if (name != null) {
                this.name = name
            }

            if (roomName != null) {
                this.roomName = roomName
            }

            if (participantIdentity != null) {
                this.participantIdentity = participantIdentity
            }

            if (bypassTranscoding != null) {
                this.bypassTranscoding = bypassTranscoding
            }

            if (enableTranscoding != null) {
                this.enableTranscoding = enableTranscoding
            }

            if (participantName != null) {
                this.participantName = participantName
            }

            if (audioOptions != null) {
                this.audio = audioOptions
            }

            if (videoOptions != null) {
                this.video = videoOptions
            }
            build()
        }
        val credentials = authHeader(IngressAdmin(true))
        return service.updateIngress(request, credentials)
    }

    /**
     * List ingress
     * @param roomName when null or empty, list all rooms.
     *                 otherwise returns rooms with matching room name
     * @param ingressId when set, filters by ingressId
     */
    @JvmOverloads
    fun listIngress(
        roomName: String? = null,
        ingressId: String? = null
    ): Call<List<LivekitIngress.IngressInfo>> {
        val request = with(LivekitIngress.ListIngressRequest.newBuilder()) {
            if (roomName != null) {
                this.roomName = roomName
            }
            if (ingressId != null) {
                this.ingressId = ingressId
            }
            build()
        }
        val credentials = authHeader(IngressAdmin(true))
        return TransformCall(service.listIngress(request, credentials)) {
            it.itemsList
        }
    }

    fun deleteIngress(ingressID: String): Call<LivekitIngress.IngressInfo> {
        val request = LivekitIngress.DeleteIngressRequest.newBuilder()
            .setIngressId(ingressID)
            .build()
        val credentials = authHeader(IngressAdmin(true))
        return service.deleteIngress(request, credentials)
    }

    private fun authHeader(vararg videoGrants: VideoGrant): String {
        val accessToken = AccessToken(apiKey, secret)
        accessToken.addGrants(*videoGrants)

        val jwt = accessToken.toJwt()

        return "Bearer $jwt"
    }

    companion object {
        /**
         * Create an IngressServiceClient.
         */
        @Deprecated(
            "Use IngressServiceClient.createClient()",
            ReplaceWith(
                "IngressServiceClient.createClient(host, apiKey, secret, OkHttpFactory(logging))",
                "import io.livekit.server.okhttp.OkHttpFactory",
            )
        )
        @JvmStatic
        @JvmOverloads
        fun create(host: String, apiKey: String, secret: String, logging: Boolean = false): IngressServiceClient {
            return createClient(
                host = host,
                apiKey = apiKey,
                secret = secret,
                okHttpSupplier = OkHttpFactory(logging = logging)
            )
        }

        /**
         * Create an IngressServiceClient.
         *
         * @param okHttpSupplier provide an [OkHttpFactory] if you wish to customize the http client
         * (e.g. proxy, timeout, certificate/auth settings), or supply your own OkHttpClient
         * altogether to pool resources with [OkHttpHolder].
         *
         * @see OkHttpHolder
         * @see OkHttpFactory
         */
        @JvmStatic
        @JvmOverloads
        fun createClient(
            host: String,
            apiKey: String,
            secret: String,
            okHttpSupplier: Supplier<OkHttpClient> = OkHttpFactory()
        ): IngressServiceClient {
            val okhttp = okHttpSupplier.get()

            val service = Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(ProtoConverterFactory.create())
                .client(okhttp)
                .build()
                .create(IngressService::class.java)

            return IngressServiceClient(service, apiKey, secret)
        }
    }
}
