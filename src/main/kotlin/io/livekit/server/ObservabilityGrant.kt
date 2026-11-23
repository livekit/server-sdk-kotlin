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

// Protocol grant list can be found at
// https://github.com/livekit/protocol/blob/main/auth/grants.go

/**
 * Grants for publishing observability data.
 */
sealed class ObservabilityGrant(val key: String, val value: Any) {
    fun toPair() = key to value
}

/** Can publish observability data */
class ObservabilityWrite(value: Boolean = true) : ObservabilityGrant("write", value)
