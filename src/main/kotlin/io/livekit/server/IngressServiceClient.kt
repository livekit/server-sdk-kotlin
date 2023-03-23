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
        roomName: String,
        participantIdentity: String,
        participantName: String? = null,
        inputType: LivekitIngress.IngressInput? = LivekitIngress.IngressInput.RTMP_INPUT,
        audioOptions: LivekitIngress.IngressAudioOptions? = null,
        videoOptions: LivekitIngress.IngressVideoOptions? = null,
    ): Call<LivekitIngress.IngressInfo> {
        val request = with(LivekitIngress.CreateIngressRequest.newBuilder()) {
            this.name = name
            this.participantIdentity = participantIdentity
            this.inputType = inputType

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
        return service.createIngress(request, credentials)
    }

    /**
     * List ingress
     * @param roomName when null or empty, list all rooms.
     *                 otherwise returns rooms with matching room name
     */
    @JvmOverloads
    fun listIngress(roomName: String? = null): Call<List<LivekitIngress.IngressInfo>> {
        val request = with(LivekitIngress.ListIngressRequest.newBuilder()) {
            if (roomName != null) {
                this.roomName = roomName
            }
            build()
        }
        val credentials = authHeader(IngressAdmin(true))
        return TransformCall(service.listIngress(request, credentials)) {
            it.itemsList
        }
    }


    private fun authHeader(vararg videoGrants: VideoGrant): String {
        val accessToken = AccessToken(apiKey, secret)
        accessToken.addGrants(*videoGrants)

        val jwt = accessToken.toJwt()

        return "Bearer $jwt"
    }
}
