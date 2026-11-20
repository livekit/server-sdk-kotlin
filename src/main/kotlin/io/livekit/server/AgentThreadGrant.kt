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
 * Grants for agent threads.
 */
sealed class AgentThreadGrant(val key: String, val value: Any) {
    fun toPair() = key to value
}

/** Covers every thread and scope of the project; the action grants still apply */
class AgentThreadAll(value: Boolean = true) : AgentThreadGrant("all", value)

/** Scope in which threads may be listed and created */
class AgentThreadScope(value: String) : AgentThreadGrant("scope", value)

/** Threads that may be read, written and deleted */
class AgentThreadIds(value: List<String>) : AgentThreadGrant("threadIds", value)

/** Can list threads */
class AgentThreadList(value: Boolean = true) : AgentThreadGrant("list", value)

/** Can create threads */
class AgentThreadCreate(value: Boolean = true) : AgentThreadGrant("create", value)

/** Can write to threads */
class AgentThreadWrite(value: Boolean = true) : AgentThreadGrant("write", value)

/** Can read threads */
class AgentThreadRead(value: Boolean = true) : AgentThreadGrant("read", value)

/** Can delete threads */
class AgentThreadDelete(value: Boolean = true) : AgentThreadGrant("delete", value)

/** Can open a covered thread's database, whose ID is the thread's */
class AgentThreadSql(value: Boolean = true) : AgentThreadGrant("sql", value)
