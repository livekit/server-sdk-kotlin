package io.livekit.server

import livekit.LivekitIngress
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Retrofit Interface for accessing the IngressService Apis.
 */

interface IngressService {

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Ingress/CreateIngress")
    fun createIngress(
        @Body request: LivekitIngress.CreateIngressRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitIngress.IngressInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Ingress/UpdateIngress")
    fun updateIngress(
        @Body request: LivekitIngress.UpdateIngressRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitIngress.IngressInfo>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Ingress/ListIngress")
    fun listIngress(
        @Body request: LivekitIngress.ListIngressRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitIngress.ListIngressResponse>

    @Headers("Content-Type: application/protobuf")
    @POST("/twirp/livekit.Ingress/DeleteIngress")
    fun deleteIngress(
        @Body request: LivekitIngress.DeleteIngressRequest,
        @Header("Authorization") authorization: String
    ): Call<LivekitIngress.IngressInfo>

}


