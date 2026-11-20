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
 * Grants for agent databases.
 */
sealed class AgentDatabaseGrant(val key: String, val value: Any) {
    fun toPair() = key to value
}

/** Databases that may be read, written and deleted */
class AgentDatabaseIds(value: List<String>) : AgentDatabaseGrant("databaseIds", value)

/** Can read databases */
class AgentDatabaseRead(value: Boolean = true) : AgentDatabaseGrant("read", value)

/** Can write to databases */
class AgentDatabaseWrite(value: Boolean = true) : AgentDatabaseGrant("write", value)

/** Can delete databases */
class AgentDatabaseDelete(value: Boolean = true) : AgentDatabaseGrant("delete", value)

/** Can create databases in the project */
class AgentDatabaseCreate(value: Boolean = true) : AgentDatabaseGrant("create", value)

/** Can list the project's databases */
class AgentDatabaseList(value: Boolean = true) : AgentDatabaseGrant("list", value)
