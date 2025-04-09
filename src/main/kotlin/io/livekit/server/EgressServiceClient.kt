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
import livekit.LivekitEgress
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.util.function.Supplier

data class EncodedOutputs(
    val fileOutput: LivekitEgress.EncodedFileOutput?,
    val streamOutput: LivekitEgress.StreamOutput?,
    val segmentOutput: LivekitEgress.SegmentedFileOutput?,
    val imageOutput: LivekitEgress.ImageOutput?,
)

enum class AudioMixing {
    DEFAULT_MIXING,
    DUAL_CHANNEL_AGENT,
    DUAL_CHANNEL_ALTERNATE
}

/**
 * A client for interacting with the Egress service.
 *
 * See: [Egress Overview](https://docs.livekit.io/realtime/egress/overview/)
 */
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
        customBaseUrl: String = "",
        audioMixing: AudioMixing = AudioMixing.DEFAULT_MIXING
    ): Call<LivekitEgress.EgressInfo> {
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.RoomCompositeEgressRequest.newBuilder()
            .setFile(output)
            .addFileOutputs(output)
        return startRoomCompositeEgressImpl(
            requestBuilder,
            roomName,
            layout,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly,
            customBaseUrl,
            audioMixing
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
        customBaseUrl: String = "",
        audioMixing: AudioMixing = AudioMixing.DEFAULT_MIXING
    ): Call<LivekitEgress.EgressInfo> {
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.RoomCompositeEgressRequest.newBuilder()
            .setSegments(output)
            .addSegmentOutputs(output)
        return startRoomCompositeEgressImpl(
            requestBuilder,
            roomName,
            layout,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly,
            customBaseUrl,
            audioMixing
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
        customBaseUrl: String = "",
        audioMixing: AudioMixing = AudioMixing.DEFAULT_MIXING
    ): Call<LivekitEgress.EgressInfo> {
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.RoomCompositeEgressRequest.newBuilder()
            .setStream(output)
            .addStreamOutputs(output)
        return startRoomCompositeEgressImpl(
            requestBuilder,
            roomName,
            layout,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly,
            customBaseUrl,
            audioMixing
        )
    }

    @JvmOverloads
    fun startRoomCompositeEgress(
        roomName: String,
        output: LivekitEgress.ImageOutput,
        layout: String = "",
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
        audioOnly: Boolean = false,
        videoOnly: Boolean = false,
        customBaseUrl: String = "",
        audioMixing: AudioMixing = AudioMixing.DEFAULT_MIXING,
    ): Call<LivekitEgress.EgressInfo> {
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.RoomCompositeEgressRequest.newBuilder()
            .addImageOutputs(output)
        return startRoomCompositeEgressImpl(
            requestBuilder,
            roomName,
            layout,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly,
            customBaseUrl,
            audioMixing
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
        customBaseUrl: String = "",
        audioMixing: AudioMixing = AudioMixing.DEFAULT_MIXING,
    ): Call<LivekitEgress.EgressInfo> {
        val requestBuilder = LivekitEgress.RoomCompositeEgressRequest.newBuilder()
        if (output.fileOutput != null) {
            requestBuilder.addFileOutputs(output.fileOutput)
        }
        if (output.streamOutput != null) {
            requestBuilder.addStreamOutputs(output.streamOutput)
        }
        if (output.segmentOutput != null) {
            requestBuilder.addSegmentOutputs(output.segmentOutput)
        }
        if (output.imageOutput != null) {
            requestBuilder.addImageOutputs(output.imageOutput)
        }

        return startRoomCompositeEgressImpl(
            requestBuilder,
            roomName,
            layout,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly,
            customBaseUrl,
            audioMixing
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
        customBaseUrl: String,
        audioMixing: AudioMixing
    ): Call<LivekitEgress.EgressInfo> {
        val protoAudioMixing = when (audioMixing) {
            AudioMixing.DEFAULT_MIXING -> LivekitEgress.AudioMixing.DEFAULT_MIXING
            AudioMixing.DUAL_CHANNEL_AGENT -> LivekitEgress.AudioMixing.DUAL_CHANNEL_AGENT
            AudioMixing.DUAL_CHANNEL_ALTERNATE -> LivekitEgress.AudioMixing.DUAL_CHANNEL_ALTERNATE
        }

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
            this.audioMixing = protoAudioMixing
            build()
        }
        val credentials = authHeader(RoomRecord(true))

        return service.startRoomCompositeEgress(request, credentials)
    }

    @JvmOverloads
    fun startParticipantEgress(
        roomName: String,
        identity: String,
        output: EncodedOutputs,
        screenShare: Boolean = false,
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
    ): Call<LivekitEgress.EgressInfo> {
        val requestBuilder = LivekitEgress.ParticipantEgressRequest.newBuilder()
        if (output.fileOutput != null) {
            requestBuilder.addFileOutputs(output.fileOutput)
        }
        if (output.streamOutput != null) {
            requestBuilder.addStreamOutputs(output.streamOutput)
        }
        if (output.segmentOutput != null) {
            requestBuilder.addSegmentOutputs(output.segmentOutput)
        }
        if (output.imageOutput != null) {
            requestBuilder.addImageOutputs(output.imageOutput)
        }

        return startParticipantEgressImpl(
            requestBuilder,
            roomName,
            identity,
            screenShare,
            optionsPreset,
            optionsAdvanced
        )
    }

    private fun startParticipantEgressImpl(
        requestBuilder: LivekitEgress.ParticipantEgressRequest.Builder,
        roomName: String,
        identity: String,
        screenShare: Boolean,
        optionsPreset: LivekitEgress.EncodingOptionsPreset?,
        optionsAdvanced: LivekitEgress.EncodingOptions?
    ): Call<LivekitEgress.EgressInfo> {
        val request = with(requestBuilder) {
            this.roomName = roomName
            this.identity = identity
            this.screenShare = screenShare
            if (optionsPreset != null) {
                this.preset = optionsPreset
            } else if (optionsAdvanced != null) {
                this.advanced = optionsAdvanced
            }
            build()
        }
        val credentials = authHeader(RoomRecord(true))

        return service.startParticipantEgress(request, credentials)
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
            .addFileOutputs(output)
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
            .addSegmentOutputs(output)
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
            .addStreamOutputs(output)
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
        output: LivekitEgress.ImageOutput,
        audioTrackId: String?,
        videoTrackId: String?,
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
    ): Call<LivekitEgress.EgressInfo> {
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.TrackCompositeEgressRequest.newBuilder()
            .addImageOutputs(output)
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
            requestBuilder.addFileOutputs(output.fileOutput)
        }
        if (output.streamOutput != null) {
            requestBuilder.addStreamOutputs(output.streamOutput)
        }
        if (output.segmentOutput != null) {
            requestBuilder.addSegmentOutputs(output.segmentOutput)
        }
        if (output.imageOutput != null) {
            requestBuilder.addImageOutputs(output.imageOutput)
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
        videoOnly: Boolean = false,
        awaitStartSignal: Boolean = false
    ): Call<LivekitEgress.EgressInfo> {
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.WebEgressRequest.newBuilder()
            .setFile(output)
            .addFileOutputs(output)
        return startWebEgressImpl(
            requestBuilder,
            url,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly,
            awaitStartSignal
        )
    }

    @JvmOverloads
    fun startWebEgress(
        url: String,
        output: LivekitEgress.SegmentedFileOutput,
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
        audioOnly: Boolean = false,
        videoOnly: Boolean = false,
        awaitStartSignal: Boolean = false
    ): Call<LivekitEgress.EgressInfo> {
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.WebEgressRequest.newBuilder()
            .setSegments(output)
            .addSegmentOutputs(output)
        return startWebEgressImpl(
            requestBuilder,
            url,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly,
            awaitStartSignal
        )
    }

    @JvmOverloads
    fun startWebEgress(
        url: String,
        output: LivekitEgress.StreamOutput,
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
        audioOnly: Boolean = false,
        videoOnly: Boolean = false,
        awaitStartSignal: Boolean = false
    ): Call<LivekitEgress.EgressInfo> {
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.WebEgressRequest.newBuilder()
            .setStream(output)
            .addStreamOutputs(output)
        return startWebEgressImpl(
            requestBuilder,
            url,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly,
            awaitStartSignal
        )
    }

    @JvmOverloads
    fun startWebEgress(
        url: String,
        output: LivekitEgress.ImageOutput,
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
        audioOnly: Boolean = false,
        videoOnly: Boolean = false,
        awaitStartSignal: Boolean = false
    ): Call<LivekitEgress.EgressInfo> {
        @Suppress("DEPRECATION")
        val requestBuilder = LivekitEgress.WebEgressRequest.newBuilder()
            .addImageOutputs(output)
        return startWebEgressImpl(
            requestBuilder,
            url,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly,
            awaitStartSignal
        )
    }

    @JvmOverloads
    fun startWebEgress(
        url: String,
        output: EncodedOutputs,
        optionsPreset: LivekitEgress.EncodingOptionsPreset? = null,
        optionsAdvanced: LivekitEgress.EncodingOptions? = null,
        audioOnly: Boolean = false,
        videoOnly: Boolean = false,
        awaitStartSignal: Boolean = false
    ): Call<LivekitEgress.EgressInfo> {
        val requestBuilder = LivekitEgress.WebEgressRequest.newBuilder()
        if (output.fileOutput != null) {
            requestBuilder.addFileOutputs(output.fileOutput)
        }
        if (output.streamOutput != null) {
            requestBuilder.addStreamOutputs(output.streamOutput)
        }
        if (output.segmentOutput != null) {
            requestBuilder.addSegmentOutputs(output.segmentOutput)
        }
        if (output.imageOutput != null) {
            requestBuilder.addImageOutputs(output.imageOutput)
        }

        return startWebEgressImpl(
            requestBuilder,
            url,
            optionsPreset,
            optionsAdvanced,
            audioOnly,
            videoOnly,
            awaitStartSignal
        )
    }

    private fun startWebEgressImpl(
        requestBuilder: LivekitEgress.WebEgressRequest.Builder,
        url: String,
        optionsPreset: LivekitEgress.EncodingOptionsPreset?,
        optionsAdvanced: LivekitEgress.EncodingOptions?,
        audioOnly: Boolean,
        videoOnly: Boolean,
        awaitStartSignal: Boolean
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
            this.awaitStartSignal = awaitStartSignal
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
    fun listEgress(
        roomName: String? = null,
        egressId: String? = null,
        active: Boolean? = null,
    ): Call<List<LivekitEgress.EgressInfo>> {
        val request = with(LivekitEgress.ListEgressRequest.newBuilder()) {
            if (roomName != null) {
                this.roomName = roomName
            }
            if (egressId != null) {
                this.egressId = egressId
            }
            if (active != null) {
                this.active = active
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

        @Deprecated(
            "Use EgressServiceClient.createClient()",
            ReplaceWith(
                "EgressServiceClient.createClient(host, apiKey, secret, OkHttpFactory(logging))",
                "import io.livekit.server.okhttp.OkHttpFactory",
                "io.livekit.server.IngressServiceClient.createClient"
            )
        )
        @JvmStatic
        @JvmOverloads
        fun create(
            host: String,
            apiKey: String,
            secret: String,
            logging: Boolean = false
        ): EgressServiceClient {
            return createClient(
                host = host,
                apiKey = apiKey,
                secret = secret,
                okHttpSupplier = OkHttpFactory(logging = logging)
            )
        }

        /**
         * Create an EgressServiceClient.
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
        ): EgressServiceClient {
            val okhttp = okHttpSupplier.get()

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
