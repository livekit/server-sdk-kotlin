/*
 * Copyright 2024-2026 LiveKit, Inc.
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
import io.livekit.server.okhttp.FailoverConfig
import io.livekit.server.okhttp.RegionFailoverInterceptor
import io.livekit.server.okhttp.OkHttpHolder
import io.livekit.server.retrofit.withTransform
import livekit.LivekitModels.ListUpdate
import livekit.LivekitRoom
import livekit.LivekitSip
import livekit.LivekitSip.SIPDispatchRule
import livekit.LivekitSip.SIPDispatchRuleDirect
import livekit.LivekitSip.SIPDispatchRuleIndividual
import livekit.LivekitSip.SIPDispatchRuleInfo
import livekit.LivekitSip.SIPParticipantInfo
import livekit.LivekitSip.SIPTrunkInfo
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.util.function.Supplier

/**
 * A client for interacting with the SIP service.
 *
 * See: [SIP Overview](https://docs.livekit.io/sip/)
 */
class SipServiceClient(
    private val service: SipService,
    apiKey: String,
    secret: String,
) : ServiceClientBase(apiKey, secret) {

    /**
     * Creates an inbound trunk to accept incoming calls.
     *
     * See: [SIP Inbound Trunk](https://docs.livekit.io/sip/trunk-inbound/)
     */
    @JvmOverloads
    @Suppress("unused")
    fun createSipInboundTrunk(
        name: String,
        numbers: List<String>,
        options: CreateSipInboundTrunkOptions? = null,
    ): Call<LivekitSip.SIPInboundTrunkInfo> {
        val request = with(LivekitSip.CreateSIPInboundTrunkRequest.newBuilder()) {
            trunk = with(LivekitSip.SIPInboundTrunkInfo.newBuilder()) {
                this.name = name
                this.addAllNumbers(numbers)

                options?.let { opt ->
                    opt.metadata?.let { this.metadata = it }
                    opt.allowedAddresses?.let { this.addAllAllowedAddresses(it) }
                    opt.allowedNumbers?.let { this.addAllAllowedNumbers(it) }
                    opt.authUsername?.let { this.authUsername = it }
                    opt.authPassword?.let { this.authPassword = it }
                }
                build()
            }
            build()
        }

        val credentials = authHeader(emptyList(), listOf(SIPAdmin()))
        return service.createSipInboundTrunk(request, credentials)
    }

    /**
     * Creates an outbound trunk for making outgoing calls.
     *
     * See: [SIP Outbound Trunk](https://docs.livekit.io/sip/trunk-outbound/)
     */
    @JvmOverloads
    @Suppress("unused")
    fun createSipOutboundTrunk(
        name: String,
        address: String,
        numbers: List<String>,
        options: CreateSipOutboundTrunkOptions? = null,
    ): Call<LivekitSip.SIPOutboundTrunkInfo> {
        val request = with(LivekitSip.CreateSIPOutboundTrunkRequest.newBuilder()) {
            trunk = with(LivekitSip.SIPOutboundTrunkInfo.newBuilder()) {
                this.name = name
                this.address = address
                this.addAllNumbers(numbers)
                this.transport = LivekitSip.SIPTransport.SIP_TRANSPORT_AUTO

                options?.let { opt ->
                    this.transport = opt.transport
                    opt.metadata?.let { this.metadata = it }
                    opt.authUsername?.let { this.authUsername = it }
                    opt.authPassword?.let { this.authPassword = it }
                    opt.destinationCountry?.let { this.destinationCountry = it }
                }
                build()
            }
            build()
        }

        val credentials = authHeader(emptyList(), listOf(SIPAdmin()))
        return service.createSipOutboundTrunk(request, credentials)
    }

    /**
     * UpdateSIPInboundTrunk updates an existing SIP Inbound Trunk.
     */
    @JvmOverloads
    @Suppress("unused")
    fun updateSipInboundTrunk(
        sipTrunkId: String,
        options: UpdateSipInboundTrunkOptions? = null
    ): Call<LivekitSip.SIPInboundTrunkInfo> {
        val request = with(LivekitSip.UpdateSIPInboundTrunkRequest.newBuilder()) {
            this.sipTrunkId = sipTrunkId

            update = with(LivekitSip.SIPInboundTrunkUpdate.newBuilder()) {
                options?.let { opt ->
                    opt.name?.let { this.name = it }
                    opt.authUsername?.let { this.authUsername = it }
                    opt.authPassword?.let { this.authPassword = it }
                    opt.metadata?.let { this.metadata = it }
                    opt.numbers?.let { this.numbers = buildListUpdate(it) }
                    opt.allowedNumbers?.let { this.allowedNumbers = buildListUpdate(it) }
                    opt.allowedAddresses?.let { this.allowedAddresses = buildListUpdate(it) }
                }
                build()
            }
            build()
        }

        val credentials = authHeader(emptyList(), listOf(SIPAdmin()))
        return service.updateSipInboundTrunk(request, credentials)
    }

    /**
     * UpdateSIPOutboundTrunk updates an existing SIP Outbound Trunk.
     */
    @JvmOverloads
    @Suppress("unused")
    fun updateSipOutboundTrunk(
        sipTrunkId: String,
        options: UpdateSipOutboundTrunkOptions? = null,
    ): Call<LivekitSip.SIPOutboundTrunkInfo> {
        val request = with(LivekitSip.UpdateSIPOutboundTrunkRequest.newBuilder()) {
            this.sipTrunkId = sipTrunkId

            update = with(LivekitSip.SIPOutboundTrunkUpdate.newBuilder()) {
                options?.let { opt ->
                    opt.name?.let { this.name = it }
                    opt.address?.let { this.address = it }
                    opt.metadata?.let { this.metadata = it }
                    opt.transport?.let { this.transport = it }
                    opt.authUsername?.let { this.authUsername = it }
                    opt.authPassword?.let { this.authPassword = it }
                    opt.numbers?.let { this.numbers = buildListUpdate(it) }
                    opt.destinationCountry?.let { this.destinationCountry = it }
                }
                build()
            }
            build()
        }

        val credentials = authHeader(emptyList(), listOf(SIPAdmin()))
        return service.updateSipOutboundTrunk(request, credentials)
    }

    /**
     * List inbound trunks.
     *
     * See: [SIP Inbound Trunk](https://docs.livekit.io/sip/trunk-inbound/)
     */
    @JvmOverloads
    @Suppress("unused")
    fun listSipInboundTrunk(): Call<List<LivekitSip.SIPInboundTrunkInfo>> {
        val request = LivekitSip.ListSIPInboundTrunkRequest.newBuilder().build()

        val credentials = authHeader(emptyList(), listOf(SIPAdmin()))
        return service.listSIPInboundTrunk(request, credentials)
            .withTransform { it.itemsList }
    }

    /**
     * List outbound trunks.
     *
     * See: [SIP Outbound Trunk](https://docs.livekit.io/sip/trunk-outbound/)
     */
    @JvmOverloads
    @Suppress("unused")
    fun listSipOutboundTrunk(): Call<List<LivekitSip.SIPOutboundTrunkInfo>> {
        val request = LivekitSip.ListSIPOutboundTrunkRequest.newBuilder().build()

        val credentials = authHeader(emptyList(), listOf(SIPAdmin()))
        return service.listSipOutboundTrunk(request, credentials)
            .withTransform { it.itemsList }
    }

    /**
     * Deletes a trunk.
     */
    @JvmOverloads
    @Suppress("unused")
    fun deleteSipTrunk(sipTrunkId: String): Call<SIPTrunkInfo> {
        val request = with(LivekitSip.DeleteSIPTrunkRequest.newBuilder()) {
            this.sipTrunkId = sipTrunkId
            build()
        }

        val credentials = authHeader(emptyList(), listOf(SIPAdmin()))
        return service.deleteSipTrunk(request, credentials)
    }

    /**
     * Creates a dispatch rule.
     *
     * See: [Dispatch Rules](https://docs.livekit.io/sip/dispatch-rule/)
     */
    @JvmOverloads
    @Suppress("unused")
    fun createSipDispatchRule(
        rule: SipDispatchRule,
        options: CreateSipDispatchRuleOptions? = null
    ): Call<SIPDispatchRuleInfo> {
        val request = with(LivekitSip.CreateSIPDispatchRuleRequest.newBuilder()) {
            this.dispatchRule = with(SIPDispatchRuleInfo.newBuilder()) {
                options?.let { opt ->
                    opt.trunkIds?.let { this.addAllTrunkIds(it) }
                    opt.hidePhoneNumber?.let { this.hidePhoneNumber = it }
                    opt.name?.let { this.name = it }
                    opt.metadata?.let { this.metadata = it }
                    opt.attributes?.let { this.putAllAttributes(it) }
                    opt.roomPreset?.let { this.roomPreset = it }
                    opt.roomConfig?.let { this.roomConfig = it }
                }
                build()
                this.rule = with(SIPDispatchRule.newBuilder()) {
                    when (rule) {
                        is SipDispatchRuleDirect -> {
                            dispatchRuleDirect = with(SIPDispatchRuleDirect.newBuilder()) {
                                roomName = rule.roomName
                                rule.pin?.let { this.pin = it }
                                build()
                            }
                        }

                        is SipDispatchRuleIndividual -> {
                            dispatchRuleIndividual = with(SIPDispatchRuleIndividual.newBuilder()) {
                                roomPrefix = rule.roomPrefix
                                rule.pin?.let { this.pin = it }
                                build()
                            }
                        }
                    }
                    build()
                }
                build()
            }
            build()
        }
        val credentials = authHeader(emptyList(), listOf(SIPAdmin()))
        return service.createSipDispatchRule(request, credentials)
    }

    /**
     * UpdateSIPDispatchRule updates an existing SIP Dispatch Rule.
     */
    @JvmOverloads
    @Suppress("unused")
    fun updateSipDispatchRule(
        sipDispatchRuleId: String,
        options: UpdateSipDispatchRuleOptions? = null
    ): Call<LivekitSip.SIPDispatchRuleInfo> {
        val request = with(LivekitSip.UpdateSIPDispatchRuleRequest.newBuilder()) {
            this.sipDispatchRuleId = sipDispatchRuleId
            update = with(LivekitSip.SIPDispatchRuleUpdate.newBuilder()) {
                options?.let { opt ->
                    opt.name?.let { this.name = it }
                    opt.metadata?.let { this.metadata = it }
                    opt.trunkIds?.let { this.trunkIds = buildListUpdate(it) }
                    opt.rule?.let { optRule ->
                        this.rule = with(SIPDispatchRule.newBuilder()) {
                            when (optRule) {
                                is SipDispatchRuleDirect -> {
                                    dispatchRuleDirect = with(SIPDispatchRuleDirect.newBuilder()) {
                                        roomName = optRule.roomName
                                        optRule.pin?.let { this.pin = it }
                                        build()
                                    }
                                }

                                is SipDispatchRuleIndividual -> {
                                    dispatchRuleIndividual = with(SIPDispatchRuleIndividual.newBuilder()) {
                                        roomPrefix = optRule.roomPrefix
                                        optRule.pin?.let { this.pin = it }
                                        build()
                                    }
                                }
                            }
                            build()
                        }
                    }
                }
                build()
            }
            build()
        }

        val credentials = authHeader(emptyList(), listOf(SIPAdmin()))
        return service.updateSipDispatchRule(request, credentials)
    }

    /**
     * Lists the dispatch rules.
     *
     * See: [Dispatch Rules](https://docs.livekit.io/sip/dispatch-rule/)
     */
    @Suppress("unused")
    fun listSipDispatchRule(): Call<List<SIPDispatchRuleInfo>> {
        val request = LivekitSip.ListSIPDispatchRuleRequest.newBuilder().build()
        val credentials = authHeader(emptyList(), listOf(SIPAdmin()))
        return service.listSipDispatchRule(request, credentials)
            .withTransform { it.itemsList }
    }

    /**
     * Deletes a dispatch rule.
     *
     * See: [Dispatch Rules](https://docs.livekit.io/sip/dispatch-rule/)
     */
    @Suppress("unused")
    fun deleteSipDispatchRule(sipDispatchRuleId: String): Call<SIPDispatchRuleInfo> {
        val request = with(LivekitSip.DeleteSIPDispatchRuleRequest.newBuilder()) {
            this.sipDispatchRuleId = sipDispatchRuleId
            build()
        }
        val credentials = authHeader(emptyList(), listOf(SIPAdmin()))
        return service.deleteSipDispatchRule(request, credentials)
    }

    /**
     * Create a LiveKit SIP Participant.
     *
     * See: [SIP Participant](https://docs.livekit.io/sip/sip-participant/)
     */
    @Suppress("unused")
    fun createSipParticipant(
        sipTrunkId: String,
        number: String,
        roomName: String,
        options: CreateSipParticipantOptions? = null,
    ): Call<SIPParticipantInfo> {
        val request = with(LivekitSip.CreateSIPParticipantRequest.newBuilder()) {
            this.sipTrunkId = sipTrunkId
            this.sipCallTo = number
            this.roomName = roomName

            options?.let { opts ->
                opts.participantIdentity?.let { this.participantIdentity = it }
                opts.participantName?.let { this.participantName = it }
                opts.participantMetadata?.let { this.participantMetadata = it }
                opts.dtmf?.let { this.dtmf = it }
                opts.hidePhoneNumber?.let { this.hidePhoneNumber = it }
                opts.waitUntilAnswered?.let { this.waitUntilAnswered = it }
                opts.playRingtone?.let {
                    if (it) {
                        this.playRingtone = true
                        this.playDialtone = true
                    }
                }
                opts.playDialtone?.let {
                    if (it) {
                        this.playRingtone = true
                        this.playDialtone = true
                    }
                }
                opts.ringingTimeout?.let { this.ringingTimeout = secondsDuration(it) }
            }
            build()
        }

        val credentials = authHeader(emptyList(), listOf(SIPCall()))
        // When waiting for an answer, dialing takes longer than a normal request
        // and the request must outlast ringing; otherwise honor any user timeout.
        val timeout = if (request.waitUntilAnswered) {
            sipDialTimeout(options?.timeout, options?.ringingTimeout).toString()
        } else {
            options?.timeout?.toString()
        }
        return service.createSipParticipant(request, credentials, timeout)
    }

    /**
     * Transfer a LiveKit SIP Participant to a different SIP peer.
     *
     * See: [SIP Participant](https://docs.livekit.io/sip/sip-participant/)
     */
    @Suppress("unused")
    fun transferSipParticipant(
        roomName: String,
        participantIdentity: String,
        transferTo: String,
        options: TransferSipParticipantOptions? = null,
    ): Call<Void?> {
        val request = with(LivekitSip.TransferSIPParticipantRequest.newBuilder()) {
            this.roomName = roomName
            this.participantIdentity = participantIdentity
            this.transferTo = transferTo

            options?.let { opts ->
                opts.playDialtone?.let { this.playDialtone = it }
                opts.ringingTimeout?.let { this.ringingTimeout = secondsDuration(it) }
            }
            build()
        }

        val credentials = authHeader(listOf(RoomAdmin(true), RoomName(roomName)), listOf(SIPCall()))
        // Transferring a call dials a phone, which takes longer than a normal
        // request, so use a longer default unless the user specified a timeout, and
        // keep the request alive past ringing so the destination can answer.
        val timeout = sipDialTimeout(options?.timeout, options?.ringingTimeout).toString()
        return service.transferSipParticipant(request, credentials, timeout)
    }

    private fun buildListUpdate(values: List<String>): ListUpdate {
        return with(ListUpdate.newBuilder()) {
            this.addAllSet(values)
            build()
        }
    }

    // Request timeout (seconds) for a phone-dialing call: the user value (or the
    // dial default) raised to stay at least RINGING_TIMEOUT_MARGIN_SECONDS above
    // the ringing timeout, so the request doesn't abort before the call is answered.
    private fun sipDialTimeout(timeout: Int?, ringingTimeout: Int?): Int {
        var effective = timeout ?: SIP_DIAL_TIMEOUT_SECONDS
        if (ringingTimeout != null) {
            effective = maxOf(effective, ringingTimeout + RINGING_TIMEOUT_MARGIN_SECONDS)
        }
        return effective
    }

    private fun secondsDuration(seconds: Int): com.google.protobuf.Duration =
        com.google.protobuf.Duration.newBuilder().setSeconds(seconds.toLong()).build()

    companion object {
        // Calls that dial a phone (CreateSIPParticipant with waitUntilAnswered,
        // TransferSIPParticipant) take longer than a normal request.
        private const val SIP_DIAL_TIMEOUT_SECONDS = 30

        // A dialing request must outlast the ringing window, or it would abort
        // before the call can be answered. Keep the request timeout at least this
        // many seconds above the ringing timeout.
        private const val RINGING_TIMEOUT_MARGIN_SECONDS = 2

        /**
         * Create an SipServiceClient.
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
        ): SipServiceClient {
            val okhttp = okHttpSupplier.get().newBuilder()
                .addInterceptor(RegionFailoverInterceptor(FailoverConfig(enabled = failover)))
                .build()

            val service = Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(ProtoConverterFactory.create())
                .client(okhttp)
                .build()
                .create(SipService::class.java)

            return SipServiceClient(service, apiKey, secret)
        }
    }
}

data class CreateSipInboundTrunkOptions(
    var metadata: String? = null,
    var allowedAddresses: List<String>? = null,
    var allowedNumbers: List<String>? = null,
    var authUsername: String? = null,
    var authPassword: String? = null,
)

data class CreateSipOutboundTrunkOptions(
    var metadata: String? = null,
    var transport: LivekitSip.SIPTransport = LivekitSip.SIPTransport.SIP_TRANSPORT_AUTO,
    var authUsername: String? = null,
    var authPassword: String? = null,
    var destinationCountry: String? = null,
)

data class UpdateSipInboundTrunkOptions(
    var name: String? = null,
    var numbers: List<String>? = null,
    var metadata: String? = null,
    var allowedAddresses: List<String>? = null,
    var allowedNumbers: List<String>? = null,
    var authUsername: String? = null,
    var authPassword: String? = null,
)

data class UpdateSipOutboundTrunkOptions(
    var name: String? = null,
    var address: String? = null,
    var numbers: List<String>? = null,
    var metadata: String? = null,
    var transport: LivekitSip.SIPTransport? = null,
    var authUsername: String? = null,
    var authPassword: String? = null,
    var destinationCountry: String? = null,
)

/**
 * @see SipDispatchRuleDirect
 * @see SipDispatchRuleIndividual
 */
sealed class SipDispatchRule(var type: String)

/**
 * This creates a Dispatch Rule that puts all callers into a specified room, optionally protected by a pin.
 */
data class SipDispatchRuleDirect(
    var roomName: String,
    var pin: String? = null,
) : SipDispatchRule("direct")

/**
 * This creates a Dispatch Rule that creates a new room for each caller.
 * The created room will be the phone number of the caller plus a random suffix.
 * You can add a specific room name prefix by using the roomPrefix option.
 */
data class SipDispatchRuleIndividual(
    var roomPrefix: String,
    var pin: String? = null,
) : SipDispatchRule("individual")

data class CreateSipDispatchRuleOptions(
    /** Human-readable name for the Dispatch Rule. */
    var name: String? = null,
    /**
     * User-defined metadata for the Dispatch Rule.
     * Participants created by this rule will inherit this metadata.
     */
    var metadata: String? = null,
    var trunkIds: List<String>? = null,
    var hidePhoneNumber: Boolean? = null,
    /**
     * User-defined attributes for the Dispatch Rule.
     * Participants created by this rule will inherit these attributes.
     */
    var attributes: Map<String, String>? = null,
    /**
     * Cloud-only, config preset to use
     */
    var roomPreset: String? = null,
    /**
     * RoomConfiguration to use if the participant initiates the room
     */
    var roomConfig: LivekitRoom.RoomConfiguration? = null,
)

data class UpdateSipDispatchRuleOptions(
    var trunkIds: List<String>? = null,
    var name: String? = null,
    var metadata: String? = null,
    var rule: SipDispatchRule? = null,
)

data class CreateSipParticipantOptions(
    var participantIdentity: String?,
    var participantName: String? = null,
    var participantMetadata: String? = null,
    var dtmf: String? = null,
    var playRingtone: Boolean? = null, // deprecated, use playDialtone instead
    var playDialtone: Boolean? = null,
    var hidePhoneNumber: Boolean? = null,
    // Wait for the call to be answered before returning. The request blocks until
    // the call is answered or fails, with a longer timeout to allow for dialing.
    var waitUntilAnswered: Boolean? = null,
    // Optional, maximum time for the call to ring in seconds.
    var ringingTimeout: Int? = null,
    // Optional request timeout in seconds. Defaults to a longer value when
    // waitUntilAnswered is set (dialing takes time).
    var timeout: Int? = null,
)

data class TransferSipParticipantOptions(
    var playDialtone: Boolean? = null,
    // Optional, max time for the transfer destination to answer the call, in seconds.
    var ringingTimeout: Int? = null,
    // Optional request timeout in seconds. Defaults to a longer value since
    // transferring dials a phone.
    var timeout: Int? = null,
)
