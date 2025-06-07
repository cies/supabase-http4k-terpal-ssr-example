package com.example.filter

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import com.example.SUPABASE_JWT_SECRET
import com.example.handler.redirectAfterFormSubmission
import com.example.handler.redirectToSignIn
import com.example.lib.supabase.fetchSupabaseTokens
import com.example.lib.supabase.toCookies
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.cookies
import org.http4k.core.with
import org.http4k.lens.RequestLens
import org.http4k.routing.path

val verifier: JWTVerifier = JWT.require(Algorithm.HMAC256(SUPABASE_JWT_SECRET))
  .withAudience("authenticated") // or "anon", "service_role", etc. based on your usage
  .build()

/**
 * This http4k `Filter`...
 *   - Verifies the authenticity of the JWT. A bad JWT results in a Forbidden response. T
 *   - Adds the JWT to the request with the RequestKey mechanism.
 */
fun jwtFilter(jwtContextKey: RequestLens<DecodedJWT>, userUuidContextKey: RequestLens<UUID>) = Filter { next ->
  {
    val jwtAccessToken = it.cookies().firstOrNull { cookie -> cookie.name == "sb-access-token" }?.value

    val decodedJwtAndUserUuid: Triple<DecodedJWT, UUID, Duration>? = jwtAccessToken?.let { jwtAccessToken ->
      val decodedJwt = verifier.verify(jwtAccessToken)
      val userUuid = decodedJwt.claims["sub"]?.let { claim ->
        try {
          UUID.fromString(claim.toString().replace("\"", ""))
        } catch (_: IllegalArgumentException) {
          null
        }
      }
      val issuedAt = decodedJwt.claims["iat"]?.toString()?.toLong() ?: throw RuntimeException("Expected iat")
      val expiresAt = decodedJwt.claims["exp"]?.toString()?.toLong() ?: throw RuntimeException("Expected exp")
      val jwtAge = Duration.between(Instant.ofEpochSecond(issuedAt), Instant.ofEpochSecond(expiresAt))

      if (userUuid != null) {
        Triple(decodedJwt, userUuid, jwtAge)
      } else {
        // TODO: add some logging
        null
      }
    }

    decodedJwtAndUserUuid?.let { (decodedJwt, userUuid, jwtAge) ->
      val result = next(it.with(jwtContextKey of decodedJwt).with(userUuidContextKey of userUuid))

      if (jwtAge.minus(50, ChronoUnit.HOURS).isPositive) {
        it.cookies().firstOrNull { cookie -> cookie.name == "sb-refresh-token" }?.value?.let { refreshToken ->
          when (val supabaseAuthResult = fetchSupabaseTokens(refreshToken)) {
            is Success -> {
              val (accessTokenCookie, refreshTokenCookie) = supabaseAuthResult.value.toCookies()
              result.cookie(accessTokenCookie).cookie(refreshTokenCookie)
            }
            is Failure -> {
              // TODO: add logging
              result
            }
          }
        } ?: result
      } else {
        result
      }
    } ?: redirectToSignIn(it.uri.path) // 401 better, but has bad UX. So 302 it is (all big players do this).
  }
}
