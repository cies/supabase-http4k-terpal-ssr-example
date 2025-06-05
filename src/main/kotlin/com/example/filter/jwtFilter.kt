package com.example.filter

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import com.example.SUPABASE_JWT_SECRET
import java.util.*
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.cookie.cookies
import org.http4k.core.with
import org.http4k.lens.RequestLens

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

    val decodedJwtAndUserUuid: Pair<DecodedJWT, UUID>? = jwtAccessToken?.let {
      val decodedJwt = verifier.verify(jwtAccessToken)
      val userUuid = decodedJwt.claims["sub"]?.let { claim ->
        try {
          UUID.fromString(claim.toString())
        } catch (_: IllegalArgumentException) {
          null
        }
      }
      if (userUuid != null) {
        Pair(decodedJwt, userUuid)
      } else {
        // TODO: add some logging
        null
      }
    }

    decodedJwtAndUserUuid?.let { (decodedJwt, userUuid) ->
      next(it.with(jwtContextKey of decodedJwt).with(userUuidContextKey of userUuid))
    } ?: Response(UNAUTHORIZED) // Actually unauthenticated
  }
}
