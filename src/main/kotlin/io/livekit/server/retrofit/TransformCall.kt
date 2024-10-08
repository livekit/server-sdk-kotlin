/*
 * Copyright 2024 LiveKit, Inc.
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

package io.livekit.server.retrofit

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Utility class to transform a call's response
 */
internal class TransformCall<T, R>(private val sourceCall: Call<T>, private val transform: (T) -> R) :
    Call<R> {
    override fun clone(): Call<R> {
        return TransformCall(sourceCall.clone(), transform)
    }

    override fun execute(): Response<R> {
        val response = sourceCall.execute()
        return response.map(transform)
    }

    override fun enqueue(callback: Callback<R>) {
        sourceCall.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                callback.onResponse(this@TransformCall, response.map(transform))
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onFailure(this@TransformCall, t)
            }
        })
    }

    override fun isExecuted() = sourceCall.isExecuted

    override fun cancel() = sourceCall.cancel()

    override fun isCanceled() = sourceCall.isCanceled

    override fun request(): Request = sourceCall.request()

    override fun timeout(): Timeout = sourceCall.timeout()

    private fun Response<T>.map(transform: (T) -> R): Response<R> {
        return if (isSuccessful) {
            val body = body()
            val transformBody = if (body != null) {
                transform(body)
            } else {
                null
            }
            Response.success(code(), transformBody)
        } else {
            Response.error(errorBody()!!, raw())
        }
    }
}

/**
 * Inline operator to transform a call's response
 */
fun <T, R> Call<T>.withTransform(transform: (T) -> R): Call<R> {
    return TransformCall(this, transform)
}
