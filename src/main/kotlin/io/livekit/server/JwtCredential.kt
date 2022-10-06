/*
 * Copyright 2019 The gRPC Authors
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

import io.grpc.CallCredentials
import io.grpc.Metadata
import io.grpc.Metadata.ASCII_STRING_MARSHALLER
import io.grpc.Status
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.concurrent.Executor
import javax.crypto.spec.SecretKeySpec

/**
 * CallCredentials implementation, which carries the JWT value that will be propagated to the
 * server in the request metadata with the "Authorization" key and the "Bearer" prefix.
 */
class JwtCredential(
    private val apiKey: String,
    private val secret: String,
    private val videoGrants: Map<String, Any>
) : CallCredentials() {
    override fun applyRequestMetadata(
        requestInfo: RequestInfo,
        executor: Executor,
        metadataApplier: MetadataApplier
    ) {
        // Make a JWT compact serialized string.
        // This example omits setting the expiration, but a real application should do it.
        val jwt: String = Jwts.builder()
            .setIssuer(apiKey)
            .addClaims(
                mapOf(
                    "video" to videoGrants,
                )
            )
            .signWith(
                SecretKeySpec(secret.toByteArray(), "HmacSHA256"),
                SignatureAlgorithm.HS256
            )
            .compact()
        executor.execute {
            try {
                val headers = Metadata()
                headers.put(
                    Metadata.Key.of("Authorization", ASCII_STRING_MARSHALLER),
                    java.lang.String.format("%s %s", "Bearer", jwt)
                )
                metadataApplier.apply(headers)
            } catch (e: Throwable) {
                metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e))
            }
        }
    }

    override fun thisUsesUnstableApi() {
        // noop
    }
}