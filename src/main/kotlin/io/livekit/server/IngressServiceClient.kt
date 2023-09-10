package io.livekit.server

import io.livekit.server.retrofit.TransformCall
import livekit.LivekitIngress
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory

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
    ): Call<LivekitIngress.IngressInfo> {
        val request = with(LivekitIngress.UpdateIngressRequest.newBuilder()) {
            this.ingressId = ingressID

            if (name != null) {
                this.name = name
            }

            if (roomName != null) {
                this.roomName = roomName
            }

            if (participantIdentity == null) {
                this.participantIdentity = participantIdentity
            }

            if (bypassTranscoding != null) {
                this.bypassTranscoding = bypassTranscoding
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

        @JvmStatic
        @JvmOverloads
        fun create(host: String, apiKey: String, secret: String, logging: Boolean = false): IngressServiceClient {

            val okhttp = with(OkHttpClient.Builder()) {
                if (logging) {
                    val loggingInterceptor = HttpLoggingInterceptor()
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                    addInterceptor(loggingInterceptor)
                }
                build()
            }

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
