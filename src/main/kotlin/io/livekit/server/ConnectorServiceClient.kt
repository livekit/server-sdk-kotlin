/*
 * Copyright 2025-2026 LiveKit, Inc.
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

import io.livekit.server.okhttp.FailoverConfig
import io.livekit.server.okhttp.OkHttpFactory
import io.livekit.server.okhttp.OkHttpHolder
import io.livekit.server.okhttp.RegionFailoverInterceptor
import livekit.LivekitAgentDispatch
import livekit.LivekitConnectorTwilio
import livekit.LivekitConnectorWhatsapp
import livekit.LivekitRtc
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.time.Duration
import java.util.function.Supplier

/**
 * A client for interacting with the Connector service.
 *
 * The Connector service allows you to connect external communication platforms
 * (like WhatsApp and Twilio MediaStreams) to LiveKit rooms.
 */
class ConnectorServiceClient(
    private val service: ConnectorService,
    apiKey: String,
    secret: String,
) : ServiceClientBase(apiKey, secret) {

    /**
     * Dial an outbound WhatsApp call.
     *
     * @param whatsappPhoneNumberId The phone number ID of the business initiating the call
     * @param whatsappToPhoneNumber The phone number of the user to call
     * @param whatsappApiKey The API key of the business initiating the call
     * @param whatsappCloudApiVersion WhatsApp Cloud API version (e.g., "23.0", "24.0")
     * @param options Additional options for the call
     */
    @JvmOverloads
    fun dialWhatsAppCall(
        whatsappPhoneNumberId: String,
        whatsappToPhoneNumber: String,
        whatsappApiKey: String,
        whatsappCloudApiVersion: String,
        options: WhatsAppCallOptions? = null,
    ): Call<LivekitConnectorWhatsapp.DialWhatsAppCallResponse> {
        val request = with(LivekitConnectorWhatsapp.DialWhatsAppCallRequest.newBuilder()) {
            this.whatsappPhoneNumberId = whatsappPhoneNumberId
            this.whatsappToPhoneNumber = whatsappToPhoneNumber
            this.whatsappApiKey = whatsappApiKey
            this.whatsappCloudApiVersion = whatsappCloudApiVersion
            applyOptions(options)
            build()
        }

        val credentials = authHeader(RoomCreate(true))
        return service.dialWhatsAppCall(request, credentials)
    }

    /**
     * Disconnect an active WhatsApp call.
     *
     * @param whatsappCallId The call ID sent by Meta
     * @param whatsappApiKey The API key of the business disconnecting the call.
     * Required when [disconnectReason] is [LivekitConnectorWhatsapp.DisconnectWhatsAppCallRequest.DisconnectReason.BUSINESS_INITIATED];
     * may be empty for [LivekitConnectorWhatsapp.DisconnectWhatsAppCallRequest.DisconnectReason.USER_INITIATED].
     * @param disconnectReason The reason for disconnecting the call. Defaults to
     * [LivekitConnectorWhatsapp.DisconnectWhatsAppCallRequest.DisconnectReason.BUSINESS_INITIATED].
     */
    @JvmOverloads
    fun disconnectWhatsAppCall(
        whatsappCallId: String,
        whatsappApiKey: String,
        disconnectReason: LivekitConnectorWhatsapp.DisconnectWhatsAppCallRequest.DisconnectReason? = null,
    ): Call<LivekitConnectorWhatsapp.DisconnectWhatsAppCallResponse> {
        val request = with(LivekitConnectorWhatsapp.DisconnectWhatsAppCallRequest.newBuilder()) {
            this.whatsappCallId = whatsappCallId
            this.whatsappApiKey = whatsappApiKey
            if (disconnectReason != null) {
                this.disconnectReason = disconnectReason
            }
            build()
        }

        val credentials = authHeader(RoomCreate(true))
        return service.disconnectWhatsAppCall(request, credentials)
    }

    /**
     * Connect a WhatsApp call with the provided SDP.
     * This is used to provide the answer SDP for a business-initiated call.
     *
     * @param whatsappCallId The call ID sent by Meta
     * @param sdp The session description from Meta
     */
    fun connectWhatsAppCall(
        whatsappCallId: String,
        sdp: LivekitRtc.SessionDescription,
    ): Call<LivekitConnectorWhatsapp.ConnectWhatsAppCallResponse> {
        val request = with(LivekitConnectorWhatsapp.ConnectWhatsAppCallRequest.newBuilder()) {
            this.whatsappCallId = whatsappCallId
            this.sdp = sdp
            build()
        }

        val credentials = authHeader(RoomCreate(true))
        return service.connectWhatsAppCall(request, credentials)
    }

    /**
     * Accept an incoming WhatsApp call (user-initiated call).
     *
     * @param whatsappPhoneNumberId The phone number ID of the business accepting the call
     * @param whatsappApiKey The API key of the business accepting the call
     * @param whatsappCloudApiVersion WhatsApp Cloud API version (e.g., "23.0", "24.0")
     * @param whatsappCallId The call ID sent by Meta
     * @param sdp The session description from Meta
     * @param waitUntilAnswered If true, wait until the inbound party joins before returning.
     * @param options Additional options for the call
     */
    @JvmOverloads
    fun acceptWhatsAppCall(
        whatsappPhoneNumberId: String,
        whatsappApiKey: String,
        whatsappCloudApiVersion: String,
        whatsappCallId: String,
        sdp: LivekitRtc.SessionDescription,
        waitUntilAnswered: Boolean = false,
        options: WhatsAppCallOptions? = null,
    ): Call<LivekitConnectorWhatsapp.AcceptWhatsAppCallResponse> {
        val request = with(LivekitConnectorWhatsapp.AcceptWhatsAppCallRequest.newBuilder()) {
            this.whatsappPhoneNumberId = whatsappPhoneNumberId
            this.whatsappApiKey = whatsappApiKey
            this.whatsappCloudApiVersion = whatsappCloudApiVersion
            this.whatsappCallId = whatsappCallId
            this.sdp = sdp
            this.waitUntilAnswered = waitUntilAnswered
            applyOptions(options)
            build()
        }

        val credentials = authHeader(RoomCreate(true))
        // When waiting for the inbound party to join, the request can block, so
        // default its timeout to the standard ring window; the caller overrides via
        // options.timeout. Otherwise the client default applies.
        val timeoutSeconds = if (waitUntilAnswered) {
            DialTimeout.resolve(options?.timeout?.seconds?.toInt(), null)
        } else {
            options?.timeout?.seconds?.toInt()
        }
        return service.acceptWhatsAppCall(request, credentials, timeoutSeconds?.toString())
    }

    /**
     * Connect a Twilio call to a LiveKit room.
     *
     * @param twilioCallDirection The direction of the call (inbound or outbound)
     * @param options Additional options for the call
     */
    @JvmOverloads
    fun connectTwilioCall(
        twilioCallDirection: LivekitConnectorTwilio.ConnectTwilioCallRequest.TwilioCallDirection,
        options: TwilioCallOptions? = null,
    ): Call<LivekitConnectorTwilio.ConnectTwilioCallResponse> {
        val request = with(LivekitConnectorTwilio.ConnectTwilioCallRequest.newBuilder()) {
            this.twilioCallDirection = twilioCallDirection
            applyOptions(options)
            build()
        }

        val credentials = authHeader(RoomCreate(true))
        return service.connectTwilioCall(request, credentials)
    }

    companion object {
        /**
         * Create a ConnectorServiceClient.
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
            okHttpSupplier: Supplier<OkHttpClient> = OkHttpFactory(),
            failover: Boolean = true
        ): ConnectorServiceClient {
            val okhttp = okHttpSupplier.get().newBuilder()
                .addInterceptor(RegionFailoverInterceptor(FailoverConfig(enabled = failover)))
                .build()

            val service = Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(ProtoConverterFactory.create())
                .client(okhttp)
                .build()
                .create(ConnectorService::class.java)

            return ConnectorServiceClient(service, apiKey, secret)
        }

        @JvmStatic
        @JvmOverloads
        fun create(
            host: String,
            apiKey: String,
            secret: String,
            logging: Boolean = false
        ): ConnectorServiceClient {
            return createClient(
                host = host,
                apiKey = apiKey,
                secret = secret,
                okHttpSupplier = OkHttpFactory(logging = logging)
            )
        }
    }
}

private fun LivekitConnectorWhatsapp.DialWhatsAppCallRequest.Builder.applyOptions(options: WhatsAppCallOptions?) {
    options?.let { opt ->
        opt.whatsappBizOpaqueCallbackData?.let { this.whatsappBizOpaqueCallbackData = it }
        opt.roomName?.let { this.roomName = it }
        opt.agents?.let { this.addAllAgents(it) }
        opt.participantIdentity?.let { this.participantIdentity = it }
        opt.participantName?.let { this.participantName = it }
        opt.participantMetadata?.let { this.participantMetadata = it }
        opt.participantAttributes?.let { this.putAllParticipantAttributes(it) }
        opt.destinationCountry?.let { this.destinationCountry = it }
        opt.ringingTimeout?.let { this.ringingTimeout = it.toProto() }
    }
}

private fun LivekitConnectorWhatsapp.AcceptWhatsAppCallRequest.Builder.applyOptions(options: WhatsAppCallOptions?) {
    options?.let { opt ->
        opt.whatsappBizOpaqueCallbackData?.let { this.whatsappBizOpaqueCallbackData = it }
        opt.roomName?.let { this.roomName = it }
        opt.agents?.let { this.addAllAgents(it) }
        opt.participantIdentity?.let { this.participantIdentity = it }
        opt.participantName?.let { this.participantName = it }
        opt.participantMetadata?.let { this.participantMetadata = it }
        opt.participantAttributes?.let { this.putAllParticipantAttributes(it) }
        opt.destinationCountry?.let { this.destinationCountry = it }
    }
}

private fun LivekitConnectorTwilio.ConnectTwilioCallRequest.Builder.applyOptions(options: TwilioCallOptions?) {
    options?.let { opt ->
        opt.roomName?.let { this.roomName = it }
        opt.agents?.let { this.addAllAgents(it) }
        opt.participantIdentity?.let { this.participantIdentity = it }
        opt.participantName?.let { this.participantName = it }
        opt.participantMetadata?.let { this.participantMetadata = it }
        opt.participantAttributes?.let { this.putAllParticipantAttributes(it) }
        opt.destinationCountry?.let { this.destinationCountry = it }
    }
}

/**
 * Options for WhatsApp calls (dial and accept).
 */
data class WhatsAppCallOptions(
    /** The LiveKit room to connect the call to */
    var roomName: String? = null,
    /** Agents to dispatch the call to */
    var agents: List<LivekitAgentDispatch.RoomAgentDispatch>? = null,
    /** Identity of the participant in the LiveKit room */
    var participantIdentity: String? = null,
    /** Name of the participant in the LiveKit room */
    var participantName: String? = null,
    /** User-defined metadata to attach to the participant */
    var participantMetadata: String? = null,
    /** User-defined attributes to attach to the participant */
    var participantAttributes: Map<String, String>? = null,
    /** Country where the call terminates (ISO 3166-1 alpha-2) */
    var destinationCountry: String? = null,
    /** An arbitrary string useful for tracking and logging purposes */
    var whatsappBizOpaqueCallbackData: String? = null,
    /** Max time for the callee to answer the call. */
    var ringingTimeout: Duration? = null,
    /**
     * Optional request timeout. When the call waits for an answer
     * (`waitUntilAnswered`), defaults to a longer value (dialing takes time) and
     * is raised, if needed, to stay above [ringingTimeout]; otherwise the client
     * default applies.
     */
    var timeout: Duration? = null,
)

/**
 * Options for Twilio calls.
 */
data class TwilioCallOptions(
    /** The LiveKit room to connect the call to */
    var roomName: String? = null,
    /** Agents to dispatch the call to */
    var agents: List<LivekitAgentDispatch.RoomAgentDispatch>? = null,
    /** Identity of the participant in the LiveKit room */
    var participantIdentity: String? = null,
    /** Name of the participant in the LiveKit room */
    var participantName: String? = null,
    /** User-defined metadata to attach to the participant */
    var participantMetadata: String? = null,
    /** User-defined attributes to attach to the participant */
    var participantAttributes: Map<String, String>? = null,
    /** Country where the call terminates (ISO 3166-1 alpha-2) */
    var destinationCountry: String? = null,
)
