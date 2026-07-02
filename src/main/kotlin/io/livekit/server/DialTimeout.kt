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

/**
 * Request-timeout handling shared by calls that dial a phone and wait for an
 * answer (SIP CreateSIPParticipant/TransferSIPParticipant, WhatsApp
 * AcceptWhatsAppCall). These take longer than a normal request, and the request
 * must outlast ringing or it would abort before the call can be answered.
 *
 * All values are in seconds, matching the internal request-timeout header.
 */
internal object DialTimeout {
    // Ring window (seconds) assumed when a request doesn't set a ringing timeout;
    // matches the server default. A dialing request must outlast it.
    const val DEFAULT_RINGING_TIMEOUT_SECONDS = 30

    // Keep the request timeout at least this many seconds above the ringing
    // timeout, so the request doesn't abort before the call can be answered.
    const val RINGING_TIMEOUT_MARGIN_SECONDS = 2

    /**
     * Request timeout (seconds): the ring window plus a margin, so the request
     * doesn't abort before the call can be answered. The ring window is
     * [ringingTimeout] when set, else [DEFAULT_RINGING_TIMEOUT_SECONDS]. A longer
     * user-supplied [timeout] is honored; a shorter one is raised to the floor.
     */
    fun resolve(timeout: Int?, ringingTimeout: Int?): Int {
        val ring = ringingTimeout ?: DEFAULT_RINGING_TIMEOUT_SECONDS
        val floor = ring + RINGING_TIMEOUT_MARGIN_SECONDS
        return maxOf(timeout ?: floor, floor)
    }
}
