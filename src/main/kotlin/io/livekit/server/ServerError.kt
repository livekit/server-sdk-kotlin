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

package io.livekit.server

import com.google.protobuf.Struct
import com.google.protobuf.util.JsonFormat
import retrofit2.Response

/**
 * A LiveKit server API error, parsed from a failed [Response]. Retrofit itself
 * doesn't raise on API errors, so callers inspect the response and use [from] to
 * decode the error body.
 */
open class ServerError internal constructor(
    /** The error code, e.g. "not_found" or "permission_denied". */
    val code: String,
    /** The server-provided error message. */
    val message: String,
    /** Additional error metadata returned by the server. */
    val metadata: Map<String, String>,
) {
    override fun toString(): String {
        val meta = if (metadata.isEmpty()) "" else ", metadata=$metadata"
        return "ServerError(code=$code, message=$message$meta)"
    }

    companion object {
        /**
         * Decodes a [ServerError] (or [SipCallError]) from a failed [response], or
         * null when the response succeeded or its body isn't a server error.
         */
        @JvmStatic
        fun from(response: Response<*>): ServerError? {
            if (response.isSuccessful) return null
            val body = response.errorBody()?.string() ?: return null
            return parse(body)
        }

        internal fun parse(body: String): ServerError? = try {
            val builder = Struct.newBuilder()
            JsonFormat.parser().ignoringUnknownFields().merge(body, builder)
            val struct = builder.build()
            val code = struct.fieldsMap["code"]?.stringValue
            if (code == null) {
                null
            } else {
                val msg = struct.fieldsMap["msg"]?.stringValue ?: ""
                val meta = struct.fieldsMap["meta"]?.structValue?.fieldsMap
                    ?.mapValues { it.value.stringValue } ?: emptyMap()
                if (meta.containsKey("sip_status_code")) {
                    SipCallError(code, msg, meta)
                } else {
                    ServerError(code, msg, meta)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * A [ServerError] from a SIP dialing call (createSipParticipant / transferSipParticipant)
 * that failed with a SIP response status. The SIP code and reason are exposed
 * directly; other metadata remains available via [metadata].
 */
class SipCallError internal constructor(
    code: String,
    message: String,
    metadata: Map<String, String>,
) : ServerError(code, message, metadata) {
    /** The SIP response code of the failed call, e.g. 486 (Busy Here). */
    val sipStatusCode: Int? get() = metadata["sip_status_code"]?.toIntOrNull()

    /** The SIP reason phrase of the failed call, e.g. "Busy Here". */
    val sipStatus: String? get() = metadata["sip_status"]

    override fun toString(): String {
        val reason = sipStatus?.let { " $it" } ?: ""
        val extra = metadata.filterKeys { it !in EXCLUDED_META }
        val extraStr = if (extra.isEmpty()) {
            ""
        } else {
            " [" + extra.entries.joinToString(", ") { "${it.key}=${it.value}" } + "]"
        }
        return "SIP call failed: $sipStatusCode$reason ($code)$extraStr"
    }

    companion object {
        private val EXCLUDED_META = setOf("sip_status_code", "sip_status", "error_details")

        /**
         * Decodes a [SipCallError] from a failed [response], or null when it isn't
         * a SIP-status failure.
         */
        @JvmStatic
        fun from(response: Response<*>): SipCallError? = ServerError.from(response) as? SipCallError
    }
}
