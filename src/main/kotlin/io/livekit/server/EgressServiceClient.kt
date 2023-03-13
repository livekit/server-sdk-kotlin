package io.livekit.server

import io.livekit.server.retrofit.TransformCall
import livekit.LivekitEgress
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory

data class EncodedOutputs(
    val fileOutput: LivekitEgress.EncodedFileOutput?,
    val streamOutput: LivekitEgress.StreamOutput?,
    val segmentOutput: LivekitEgress.SegmentedFileOutput?,
)

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
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.RoomCompositeEgressRequest.newBuilder()
            .setFile(output)
            .setFileOutputs(0, output)
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
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.RoomCompositeEgressRequest.newBuilder()
            .setSegments(output)
            .setSegmentOutputs(0, output)
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
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.RoomCompositeEgressRequest.newBuilder()
            .setStream(output)
            .setStreamOutputs(0, output)
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
        output: EncodedOutputs,
        layout: String = "",
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
        audioOnly: Boolean = false,
        videoOnly: Boolean = false,
        customBaseUrl: String = ""
    ): Call<LivekitEgress.EgressInfo> {
        val requestBuilder = LivekitEgress.RoomCompositeEgressRequest.newBuilder()
        if (output.fileOutput != null) {
            requestBuilder.setFileOutputs(0, output.fileOutput)
        }
        if (output.streamOutput != null) {
            requestBuilder.setStreamOutputs(0, output.streamOutput)
        }
        if (output.segmentOutput != null) {
            requestBuilder.setSegmentOutputs(0, output.segmentOutput)
        }
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
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.TrackCompositeEgressRequest.newBuilder()
            .setFile(output)
            .setFileOutputs(0, output)
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
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.TrackCompositeEgressRequest.newBuilder()
            .setSegments(output)
            .setSegmentOutputs(0, output)
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
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.TrackCompositeEgressRequest.newBuilder()
            .setStream(output)
            .setStreamOutputs(0, output)
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
        output: EncodedOutputs,
        audioTrackId: String?,
        videoTrackId: String?,
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
    ): Call<LivekitEgress.EgressInfo> {
        val requestBuilder = LivekitEgress.TrackCompositeEgressRequest.newBuilder()
        if (output.fileOutput != null) {
            requestBuilder.setFileOutputs(0, output.fileOutput)
        }
        if (output.streamOutput != null) {
            requestBuilder.setStreamOutputs(0, output.streamOutput)
        }
        if (output.segmentOutput != null) {
            requestBuilder.setSegmentOutputs(0, output.segmentOutput)
        }
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

    @JvmOverloads
    fun startWebEgress(
        url: String,
        output: LivekitEgress.EncodedFileOutput,
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
        audioOnly: Boolean = false,
        videoOnly: Boolean = false
    ): Call<LivekitEgress.EgressInfo> {
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.WebEgressRequest.newBuilder()
            .setFile(output)
            .setFileOutputs(0, output)
        return startWebEgressImpl(
            requestBuilder,
            url,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly
        )
    }

    @JvmOverloads
    fun startWebEgress(
        url: String,
        output: LivekitEgress.SegmentedFileOutput,
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
        audioOnly: Boolean = false,
        videoOnly: Boolean = false
    ): Call<LivekitEgress.EgressInfo> {
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.WebEgressRequest.newBuilder()
            .setSegments(output)
            .setSegmentOutputs(0, output)
        return startWebEgressImpl(
            requestBuilder,
            url,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly
        )
    }

    @JvmOverloads
    fun startWebEgress(
        url: String,
        output: LivekitEgress.StreamOutput,
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
        audioOnly: Boolean = false,
        videoOnly: Boolean = false
    ): Call<LivekitEgress.EgressInfo> {
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.WebEgressRequest.newBuilder()
            .setStream(output)
            .setStreamOutputs(0, output)
        return startWebEgressImpl(
            requestBuilder,
            url,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly
        )
    }

    @JvmOverloads
    fun startWebEgress(
        url: String,
        output: EncodedOutputs,
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
        audioOnly: Boolean = false,
        videoOnly: Boolean = false
    ): Call<LivekitEgress.EgressInfo> {
        val requestBuilder = LivekitEgress.WebEgressRequest.newBuilder()
        if (output.fileOutput != null) {
            requestBuilder.setFileOutputs(0, output.fileOutput)
        }
        if (output.streamOutput != null) {
            requestBuilder.setStreamOutputs(0, output.streamOutput)
        }
        if (output.segmentOutput != null) {
            requestBuilder.setSegmentOutputs(0, output.segmentOutput)
        }
        return startWebEgressImpl(
            requestBuilder,
            url,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly
        )
    }

    private fun startWebEgressImpl(
        requestBuilder: LivekitEgress.WebEgressRequest.Builder,
        url: String,
        optionsPreset: LivekitEgress.EncodingOptionsPreset?,
        optionsAdvanced: LivekitEgress.EncodingOptions?,
        audioOnly: Boolean,
        videoOnly: Boolean
    ): Call<LivekitEgress.EgressInfo> {
        val request = with(requestBuilder) {
            this.url = url
            if (optionsPreset != null) {
                this.preset = optionsPreset
            } else if (optionsAdvanced != null) {
                this.advanced = optionsAdvanced
            }
            this.audioOnly = audioOnly
            this.videoOnly = videoOnly
            build()
        }
        val credentials = authHeader(RoomRecord(true))

        return service.startWebEgress(request, credentials)
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
        val accessToken = AccessToken(apiKey, secret)
        accessToken.addGrants(*videoGrants)

        val jwt = accessToken.toJwt()

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
