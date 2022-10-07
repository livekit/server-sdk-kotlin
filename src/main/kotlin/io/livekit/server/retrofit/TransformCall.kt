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
        return TransformCall(sourceCall, transform)
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
