/*
 * Copyright 2026 LiveKit, Inc.
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

package io.livekit.server.api

import io.livekit.server.AccessToken
import io.livekit.server.CreateSipInboundTrunkOptions
import io.livekit.server.CreateSipParticipantOptions
import io.livekit.server.EncodedOutputs
import io.livekit.server.LiveKitAPI
import io.livekit.server.RoomCreate
import io.livekit.server.ServerError
import io.livekit.server.SipCallError
import io.livekit.server.SipDispatchRuleCallee
import io.livekit.server.SipDispatchRuleDirect
import io.livekit.server.TransferSipParticipantOptions
import io.livekit.server.UpdateSipDispatchRuleOptions
import io.livekit.server.UpdateSipInboundTrunkOptions
import io.livekit.server.UpdateSipOutboundTrunkOptions
import io.livekit.server.okhttp.OkHttpFactory
import io.livekit.server.okhttp.OkHttpHolder
import livekit.LivekitConnectorTwilio
import livekit.LivekitConnectorWhatsapp
import livekit.LivekitEgress
import livekit.LivekitModels
import livekit.LivekitRtc
import livekit.LivekitSip
import okhttp3.OkHttpClient
import retrofit2.Call
import java.util.function.Supplier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * API tests that drive the unified [LiveKitAPI] against the mock LiveKit server.
 * See [MockControl] for setup; tests no-op when it is not reachable. Because the
 * mock enforces the same per-method grants as the real server, a call that
 * succeeds also proves the SDK attached the right grants. Mock directives are
 * injected as a default X-Lk-Mock header on the shared OkHttp client (the
 * Retrofit methods don't expose per-call headers).
 */
class LiveKitApiTest {
    private fun api(mock: String? = null): LiveKitAPI {
        val supplier: Supplier<OkHttpClient> = if (mock == null) {
            OkHttpFactory()
        } else {
            OkHttpHolder(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        chain.proceed(chain.request().newBuilder().header("X-Lk-Mock", mock).build())
                    }
                    .build(),
            )
        }
        return LiveKitAPI.createClient(MockControl.base, "devkey", "secret", supplier)
    }

    private fun assertOk(call: Call<*>) {
        val resp = call.execute()
        assertTrue(resp.isSuccessful, "expected success, got ${resp.code()}: ${resp.errorBody()?.string()}")
    }

    // -- smoke: fully-populated calls across every service ----------------------

    @Test
    fun roomSmoke() {
        if (!MockControl.serverUp()) return
        val api = api()
        assertOk(api.room.createRoom(name = "test-room", emptyTimeout = 300, maxParticipants = 50, metadata = "{}"))
        assertOk(api.room.listRooms(listOf("test-room", "lobby")))
        assertOk(api.room.deleteRoom("test-room"))
        assertOk(api.room.listParticipants("test-room"))
        assertOk(api.room.getParticipant("test-room", "participant-42"))
        assertOk(api.room.removeParticipant("test-room", "participant-42"))
        assertOk(api.room.removeParticipant("test-room", "participant-99", revokeTokenTs = 1_700_000_000_000L))
        assertOk(api.room.forwardParticipant("test-room", "participant-42", "overflow-room"))
        assertOk(api.room.moveParticipant("test-room", "participant-42", "breakout-room"))
        assertOk(api.room.mutePublishedTrack("test-room", "participant-42", "TR_video1", true))
        assertOk(
            api.room.updateParticipant(
                "test-room",
                "participant-42",
                name = "Alice",
                metadata = "{}",
                attributes = mapOf("seat" to "1A"),
            ),
        )
        assertOk(api.room.updateSubscriptions("test-room", "participant-42", listOf("TR_video1"), true))
        assertOk(api.room.updateRoomMetadata("test-room", "{}"))
        assertOk(
            api.room.sendData(
                "test-room",
                "hello".toByteArray(),
                LivekitModels.DataPacket.Kind.RELIABLE,
                destinationIdentities = listOf("participant-42"),
                topic = "chat",
            ),
        )
    }

    @Test
    fun egressSmoke() {
        if (!MockControl.serverUp()) return
        val api = api()
        val file = LivekitEgress.EncodedFileOutput.newBuilder()
            .setFileType(LivekitEgress.EncodedFileType.MP4).setFilepath("room.mp4").build()
        assertOk(api.egress.startRoomCompositeEgress("test-room", file, layout = "grid"))
        val stream = LivekitEgress.StreamOutput.newBuilder()
            .setProtocol(LivekitEgress.StreamProtocol.RTMP).addUrls("rtmps://a.example.com/live/key").build()
        assertOk(api.egress.startWebEgress("https://example.com/scene", stream))
        assertOk(api.egress.startParticipantEgress("test-room", "participant-42", EncodedOutputs(file, null, null, null)))
        assertOk(api.egress.startTrackCompositeEgress("test-room", file, "TR_audio1", "TR_video1"))
        assertOk(api.egress.startTrackEgress("test-room", "wss://example.com/ws", "TR_video1"))
        assertOk(api.egress.updateLayout("EG_abc123", "speaker"))
        assertOk(api.egress.updateStream("EG_abc123", addOutputUrls = listOf("rtmps://b.example.com/live/key")))
        assertOk(api.egress.listEgress(roomName = "test-room", egressId = "EG_abc123", active = true))
        assertOk(api.egress.stopEgress("EG_abc123"))
    }

    @Test
    fun ingressSmoke() {
        if (!MockControl.serverUp()) return
        val api = api()
        assertOk(
            api.ingress.createIngress(
                name = "stream-input",
                roomName = "test-room",
                participantIdentity = "ingress-bot",
                participantName = "Live Stream",
                inputType = livekit.LivekitIngress.IngressInput.RTMP_INPUT,
                enableTranscoding = true,
            ),
        )
        assertOk(api.ingress.updateIngress("IN_abc123", name = "stream-input-v2", roomName = "test-room"))
        assertOk(api.ingress.listIngress(roomName = "test-room", ingressId = "IN_abc123"))
        assertOk(api.ingress.deleteIngress("IN_abc123"))
    }

    @Test
    fun sipSmoke() {
        if (!MockControl.serverUp()) return
        val api = api()
        assertOk(api.sip.createSipInboundTrunk("inbound", listOf("+15105550100")))
        assertOk(
            api.sip.createSipInboundTrunk(
                "inbound-krisp",
                listOf("+15105550101"),
                CreateSipInboundTrunkOptions(
                    krispEnabled = true,
                    ringingTimeout = 30,
                    maxCallDuration = 3600,
                    headers = mapOf("X-Custom" to "1"),
                    headersToAttributes = mapOf("X-Custom" to "custom"),
                ),
            ),
        )
        assertOk(api.sip.createSipOutboundTrunk("outbound", "sip.telco.example.com", listOf("+15105550100")))
        assertOk(api.sip.listSipInboundTrunk())
        assertOk(api.sip.listSipOutboundTrunk())
        // field-level update + full replace for both trunk kinds
        assertOk(api.sip.updateSipInboundTrunk("ST_abc123", UpdateSipInboundTrunkOptions(metadata = "{}")))
        assertOk(
            api.sip.updateSipInboundTrunk(
                "ST_abc123",
                LivekitSip.SIPInboundTrunkInfo.newBuilder().setName("inbound").build(),
            ),
        )
        assertOk(api.sip.updateSipOutboundTrunk("ST_abc123", UpdateSipOutboundTrunkOptions(metadata = "{}")))
        assertOk(
            api.sip.updateSipOutboundTrunk(
                "ST_abc123",
                LivekitSip.SIPOutboundTrunkInfo.newBuilder()
                    .setName("outbound").setAddress("sip.telco.example.com").build(),
            ),
        )
        assertOk(api.sip.deleteSipTrunk("ST_abc123"))
        assertOk(api.sip.createSipDispatchRule(SipDispatchRuleDirect(roomName = "support", pin = "1234")))
        assertOk(api.sip.createSipDispatchRule(SipDispatchRuleCallee(roomPrefix = "call-", randomize = true)))
        assertOk(api.sip.updateSipDispatchRule("SDR_abc123", UpdateSipDispatchRuleOptions(name = "rule")))
        assertOk(api.sip.listSipDispatchRule())
        assertOk(api.sip.deleteSipDispatchRule("SDR_abc123"))
    }

    @Test
    fun connectorSmoke() {
        if (!MockControl.serverUp()) return
        val api = api()
        val offer = LivekitRtc.SessionDescription.newBuilder().setType("offer").setSdp("v=0\r\n").build()
        assertOk(api.connector.dialWhatsAppCall("123456789012345", "+15105550100", "wa-secret-key", "23.0"))
        assertOk(api.connector.connectWhatsAppCall("wacid.HBgLABC", offer))
        assertOk(api.connector.acceptWhatsAppCall("123456789012345", "wa-secret-key", "23.0", "wacid.HBgLABC", offer))
        assertOk(
            api.connector.disconnectWhatsAppCall(
                "wacid.HBgLABC",
                "wa-secret-key",
                LivekitConnectorWhatsapp.DisconnectWhatsAppCallRequest.DisconnectReason.BUSINESS_INITIATED,
            ),
        )
        assertOk(
            api.connector.connectTwilioCall(
                LivekitConnectorTwilio.ConnectTwilioCallRequest.TwilioCallDirection.TWILIO_CALL_DIRECTION_INBOUND,
            ),
        )
    }

    @Test
    fun agentDispatchSmoke() {
        if (!MockControl.serverUp()) return
        val api = api()
        assertOk(api.agentDispatch.createDispatch("test-room", "inbound-agent", metadata = "{}"))
        assertOk(api.agentDispatch.listDispatch("test-room"))
        assertOk(api.agentDispatch.getDispatch("test-room", "AD_abc123"))
        assertOk(api.agentDispatch.deleteDispatch("test-room", "AD_abc123"))
    }

    // -- deep: create_room round-trip -------------------------------------------

    @Test
    fun createRoomEchoesFields() {
        if (!MockControl.serverUp()) return
        val room = api().room.createRoom(name = "echo-room", metadata = "{\"scene\":\"lobby\"}", emptyTimeout = 300, maxParticipants = 50)
            .execute().body()
        assertNotNull(room)
        assertEquals("echo-room", room.name)
        assertEquals("{\"scene\":\"lobby\"}", room.metadata)
        assertEquals(300, room.emptyTimeout)
        assertEquals(50, room.maxParticipants)
        assertTrue(room.sid.isNotEmpty()) // placeholder assigned by the mock
    }

    // -- deep: SIP participant (delayMs:0 skips the mock's answer wait) ----------

    @Test
    fun sipParticipant() {
        if (!MockControl.serverUp()) return
        val p = api().sip.createSipParticipant(
            "ST_abc123",
            "+15105550100",
            "test-room",
            CreateSipParticipantOptions(participantIdentity = "sip-caller", participantName = "SIP Caller"),
        ).execute().body()
        assertNotNull(p)
        assertEquals("test-room", p.roomName)
        assertEquals("sip-caller", p.participantIdentity)

        // Inline outbound trunk config (no stored trunk id) + custom caller ID.
        val inline = api().sip.createSipParticipant(
            "",
            "+15105550100",
            "test-room",
            CreateSipParticipantOptions(
                participantIdentity = "sip-inline",
                outboundConfig = LivekitSip.SIPOutboundConfig.newBuilder()
                    .setHostname("sip.telco.example.com")
                    .setTransport(LivekitSip.SIPTransport.SIP_TRANSPORT_UDP).build(),
                fromNumber = "+15105550199",
                displayName = "Support",
            ),
        ).execute().body()
        assertNotNull(inline)

        val waitApi = api(MockControl.json("delayMs" to 0))
        assertOk(
            waitApi.sip.createSipParticipant(
                "ST_abc123",
                "+15105550100",
                "test-room",
                CreateSipParticipantOptions(participantIdentity = "sip-caller", waitUntilAnswered = true, ringingTimeout = 2),
            ),
        )
        assertOk(
            waitApi.sip.transferSipParticipant(
                "test-room",
                "sip-caller",
                "tel:+15105550122",
                TransferSipParticipantOptions(ringingTimeout = 2),
            ),
        )
    }

    // -- cross-cutting: token auth ----------------------------------------------

    @Test
    fun tokenAuth() {
        if (!MockControl.serverUp()) return
        val token = AccessToken("devkey", "secret").apply { addGrants(RoomCreate(true)) }.toJwt()
        val api = LiveKitAPI.createClientWithToken(MockControl.base, token)
        val room = api.room.createRoom(name = "token-room").execute().body()
        assertNotNull(room)
        assertEquals("token-room", room.name)
    }

    // -- cross-cutting: SIP call errors parse into SipCallError ------------------

    private fun sipError(sipStatus: Map<String, Any>): SipCallError {
        val api = api(MockControl.json("delayMs" to 0, "sipStatus" to sipStatus))
        val resp = api.sip.createSipParticipant("ST_abc123", "+15105550100", "test-room").execute()
        assertFalse(resp.isSuccessful)
        return assertNotNull(SipCallError.from(resp))
    }

    @Test
    fun sipBusy() {
        if (!MockControl.serverUp()) return
        val e = sipError(mapOf("code" to 486, "status" to "Busy Here"))
        assertTrue(e is ServerError)
        assertEquals("resource_exhausted", e.code)
        assertEquals(486, e.sipStatusCode)
        assertEquals("Busy Here", e.sipStatus)
        assertTrue(e.toString().contains("486") && e.toString().contains("Busy Here"))
    }

    @Test
    fun sipDeclined() {
        if (!MockControl.serverUp()) return
        val e = sipError(mapOf("code" to 603, "status" to "Decline"))
        assertEquals("permission_denied", e.code)
        assertEquals(603, e.sipStatusCode)
    }

    @Test
    fun sipNoAnswer() {
        if (!MockControl.serverUp()) return
        val e = sipError(mapOf("code" to 408, "status" to "Request Timeout"))
        assertEquals("deadline_exceeded", e.code)
        assertEquals(408, e.sipStatusCode)
    }

    // -- cross-cutting: client-side dial timeout --------------------------------

    @Test
    fun sipDialTimeout() {
        if (!MockControl.serverUp()) return
        // ringingTimeout 1s -> ~3s dial budget; the mock delays the answer past it.
        val call = api(MockControl.json("delayMs" to 4000)).sip.createSipParticipant(
            "ST_abc123",
            "+15105550100",
            "test-room",
            CreateSipParticipantOptions(participantIdentity = "sip-caller", waitUntilAnswered = true, ringingTimeout = 1),
        )
        assertFailsWith<java.io.IOException> { call.execute() }
    }
}
