package io.livekit.server

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.livekit.server.retrofit.TransformCall
import livekit.LivekitEgress
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.util.logging.Logger
import javax.crypto.spec.SecretKeySpec

class EgressServiceClient(
    private val service: EgressService,
    private val apiKey: String,
    private val secret: String,
) {

    @JvmOverloads
    fun startRoomCompositeEgress(
        roomName: String,
        output: LivekitEgress.EncodedFileOutput,
        layout: String = "",
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
        audioOnly: Boolean = false,
        videoOnly: Boolean = false,
        customBaseUrl: String = ""
    ): Call<LivekitEgress.EgressInfo> {
        val requestBuilder = LivekitEgress.RoomCompositeEgressRequest.newBuilder()
            .setFile(output)
        return startRoomCompositeEgressImpl(
            requestBuilder,
            roomName,
            layout,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly,
            customBaseUrl
        )
    }

    @JvmOverloads
    fun startRoomCompositeEgress(
        roomName: String,
        output: LivekitEgress.SegmentedFileOutput,
        layout: String = "",
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
        audioOnly: Boolean = false,
        videoOnly: Boolean = false,
        customBaseUrl: String = ""
    ): Call<LivekitEgress.EgressInfo> {
        val requestBuilder = LivekitEgress.RoomCompositeEgressRequest.newBuilder()
            .setSegments(output)
        return startRoomCompositeEgressImpl(
            requestBuilder,
            roomName,
            layout,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly,
            customBaseUrl
        )
    }

    @JvmOverloads
    fun startRoomCompositeEgress(
        roomName: String,
        output: LivekitEgress.StreamOutput,
        layout: String = "",
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
        audioOnly: Boolean = false,
        videoOnly: Boolean = false,
        customBaseUrl: String = ""
    ): Call<LivekitEgress.EgressInfo> {
        val requestBuilder = LivekitEgress.RoomCompositeEgressRequest.newBuilder()
            .setStream(output)
        return startRoomCompositeEgressImpl(
            requestBuilder,
            roomName,
            layout,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly,
            customBaseUrl
        )
    }

    private fun startRoomCompositeEgressImpl(
        requestBuilder: LivekitEgress.RoomCompositeEgressRequest.Builder,
        roomName: String,
        layout: String,
        optionsPreset: LivekitEgress.EncodingOptionsPreset?,
        optionsAdvanced: LivekitEgress.EncodingOptions?,
        audioOnly: Boolean,
        videoOnly: Boolean,
        customBaseUrl: String
    ): Call<LivekitEgress.EgressInfo> {
        val request = with(requestBuilder) {
            this.roomName = roomName
            this.layout = layout
            if (optionsPreset != null) {
                this.preset = optionsPreset
            } else if (optionsAdvanced != null) {
                this.advanced = optionsAdvanced
            }
            this.audioOnly = audioOnly
            this.videoOnly = videoOnly
            this.customBaseUrl = customBaseUrl
            build()
        }
        val credentials = authHeader(RoomRecord(true))

        return service.startRoomCompositeEgress(request, credentials)
    }

    @JvmOverloads
    fun startTrackCompositeEgress(
        roomName: String,
        output: LivekitEgress.EncodedFileOutput,
        audioTrackId: String?,
        videoTrackId: String?,
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
    ): Call<LivekitEgress.EgressInfo> {
        val requestBuilder = LivekitEgress.TrackCompositeEgressRequest.newBuilder()
            .setFile(output)
        return startTrackCompositeEgressImpl(
            requestBuilder,
            roomName,
            audioTrackId,
            videoTrackId,
            optionsPreset,
            optionsAdvanced,
        )
    }

    @JvmOverloads
    fun startTrackCompositeEgress(
        roomName: String,
        output: LivekitEgress.SegmentedFileOutput,
        audioTrackId: String?,
        videoTrackId: String?,
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
    ): Call<LivekitEgress.EgressInfo> {
        val requestBuilder = LivekitEgress.TrackCompositeEgressRequest.newBuilder()
            .setSegments(output)
        return startTrackCompositeEgressImpl(
            requestBuilder,
            roomName,
            audioTrackId,
            videoTrackId,
            optionsPreset,
            optionsAdvanced,
        )
    }

    @JvmOverloads
    fun startTrackCompositeEgress(
        roomName: String,
        output: LivekitEgress.StreamOutput,
        audioTrackId: String?,
        videoTrackId: String?,
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
    ): Call<LivekitEgress.EgressInfo> {
        val requestBuilder = LivekitEgress.TrackCompositeEgressRequest.newBuilder()
            .setStream(output)
        return startTrackCompositeEgressImpl(
            requestBuilder,
            roomName,
            audioTrackId,
            videoTrackId,
            optionsPreset,
            optionsAdvanced,
        )
    }

    private fun startTrackCompositeEgressImpl(
        requestBuilder: LivekitEgress.TrackCompositeEgressRequest.Builder,
        roomName: String,
        audioTrackId: String?,
        videoTrackId: String?,
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
    ): Call<LivekitEgress.EgressInfo> {
        val request = with(requestBuilder) {
            this.roomName = roomName
            this.audioTrackId = audioTrackId ?: ""
            this.videoTrackId = videoTrackId ?: ""
            if (optionsPreset != null) {
                this.preset = optionsPreset
            } else if (optionsAdvanced != null) {
                this.advanced = optionsAdvanced
            }
            build()
        }
        val credentials = authHeader(RoomRecord(true))
        return service.startTrackCompositeEgress(request, credentials)
    }

    fun startTrackEgress(
        roomName: String,
        output: LivekitEgress.DirectFileOutput,
        trackId: String,
    ): Call<LivekitEgress.EgressInfo> {
        val request = with(LivekitEgress.TrackEgressRequest.newBuilder()) {
            this.roomName = roomName
            this.file = output
            this.trackId = trackId
            build()
        }
        val credentials = authHeader(RoomRecord(true))

        return service.startTrackEgress(request, credentials)
    }

    fun startTrackEgress(
        roomName: String,
        websocketUrl: String,
        trackId: String,
    ): Call<LivekitEgress.EgressInfo> {
        val request = with(LivekitEgress.TrackEgressRequest.newBuilder()) {
            this.roomName = roomName
            this.websocketUrl = websocketUrl
            this.trackId = trackId
            build()
        }
        val credentials = authHeader(RoomRecord(true))

        return service.startTrackEgress(request, credentials)
    }

    fun updateLayout(
        egressId: String,
        layout: String,
    ): Call<LivekitEgress.EgressInfo> {
        val request = with(LivekitEgress.UpdateLayoutRequest.newBuilder()) {
            this.egressId = egressId
            this.layout = layout
            build()
        }
        val credentials = authHeader(RoomRecord(true))

        return service.updateLayout(request, credentials)
    }

    @JvmOverloads
    fun updateStream(
        egressId: String,
        addOutputUrls: List<String> = emptyList(),
        removeOutputUrls: List<String> = emptyList(),
    ): Call<LivekitEgress.EgressInfo> {
        val request = with(LivekitEgress.UpdateStreamRequest.newBuilder()) {
            this.egressId = egressId
            addAllAddOutputUrls(addOutputUrls)
            addAllRemoveOutputUrls(removeOutputUrls)
            build()
        }
        val credentials = authHeader(RoomRecord(true))

        return service.updateStream(request, credentials)
    }

    @JvmOverloads
    fun listEgress(roomName: String? = null): Call<List<LivekitEgress.EgressInfo>> {
        val request = with(LivekitEgress.ListEgressRequest.newBuilder()) {
            if (roomName != null) {
                this.roomName = roomName
            }
            build()
        }
        val credentials = authHeader(RoomRecord(true))

        return TransformCall(service.listEgress(request, credentials)) {
            it.itemsList
        }
    }

    fun stopEgress(egressId: String): Call<LivekitEgress.EgressInfo> {
        val request = with(LivekitEgress.StopEgressRequest.newBuilder()) {
            this.egressId = egressId
            build()
        }
        val credentials = authHeader(RoomRecord(true))

        return service.stopEgress(request, credentials)
    }

    private fun authHeader(vararg videoGrants: VideoGrant): String {
        val videoGrantsMap = videoGrants.associate { grant -> grant.toPair() }
        val jwt = Jwts.builder()
            .setIssuer(apiKey)
            .addClaims(
                mapOf(
                    "video" to videoGrantsMap,
                )
            )
            .signWith(
                SecretKeySpec(secret.toByteArray(), "HmacSHA256"),
                SignatureAlgorithm.HS256
            )
            .compact()

        return "Bearer $jwt"
    }

    companion object {

        @JvmStatic
        @JvmOverloads
        fun create(host: String, apiKey: String, secret: String, logging: Boolean = false): EgressServiceClient {

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
                .create(EgressService::class.java)

            return EgressServiceClient(service, apiKey, secret)
        }
    }
}