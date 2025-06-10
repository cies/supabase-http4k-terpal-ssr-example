package com.example.filter

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import com.example.SUPABASE_JWT_SECRET
import com.example.handler.SignInReason
import com.example.handler.redirectToSignIn
import com.example.lib.supabase.fetchSupabaseTokens
import com.example.lib.supabase.toCookies
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.cookies
import org.http4k.core.with
import org.http4k.lens.RequestLens


private val log = KotlinLogging.logger {}

private val verifier: JWTVerifier = JWT.require(Algorithm.HMAC256(SUPABASE_JWT_SECRET))
  .withAudience("authenticated") // or "anon", "service_role", etc. based on your usage
  .build()

/**
 * This http4k `Filter`...
 *   - Decodes and verifies the JWT (access token) from the request cookie.
 *   - Checks the JWT has not yet expired.
 *   - Redirects the user to the sign-in page (with `targetPath` and `reason`) when the above failed.
 *   - Adds the JWT data to the request context with the `RequestKey` mechanism.
 *   - Refreshes the JWT access token with the refresh token, by setting cookies on the response, when JWT gets old.
 */
fun cookieBasedJwtAuthenticator(jwtContextKey: RequestLens<DecodedJWT>, userUuidContextKey: RequestLens<UUID>) =
  Filter { next ->
    {
      val jwtParseResult = it.accessTokenFromCookie()?.let(::validatedJwtAndAuthDetailsFrom)
      authenticateOrRedirectToSignIn(jwtParseResult, jwtContextKey, userUuidContextKey, next, it)
    }
  }

private fun authenticateOrRedirectToSignIn(
  jwtParseResult: JwtParseResult?,
  jwtContextKey: RequestLens<DecodedJWT>,
  userUuidContextKey: RequestLens<UUID>,
  next: HttpHandler,
  request: Request
): Response {
  if (jwtParseResult == null)
    return redirectToSignIn(SignInReason.InvalidAuthToken, request.uri.path)
  if (jwtParseResult.expiresAt.isBefore(Instant.now()))
    return redirectToSignIn(SignInReason.SessionExpired, request.uri.path)

  // We have a valid JWT, so move on to the next layer (`Filter` or `Handler`) up in the stack, while passing data from
  // the JWT along in the request context.
  // We do not return the response immediately, because we may have to refresh the tokens (by setting the cookie).
  val response =
    next(request.with(jwtContextKey of jwtParseResult.decodedJwt).with(userUuidContextKey of jwtParseResult.userUuid))

  val jwtAge = Duration.between(jwtParseResult.issuedAt, jwtParseResult.expiresAt)
  if (jwtAge.minus(50, ChronoUnit.HOURS).isNegative) return response // Not too old? Return it.

  // The JWT access token is getting old, try to refresh it...
  val refreshToken = request.refreshTokenFromCookie()
  if (refreshToken == null) return response // No refresh token, no problem until the JWT access token expires.

  when (val supabaseAuthResult = fetchSupabaseTokens(refreshToken)) {
    is Success -> {
      val (accessTokenCookie, refreshTokenCookie) = supabaseAuthResult.value.toCookies()
      // Set new access and refresh tokens with "Set-Cookie" headers on the result we got from up the filter stack.
      return response.cookie(accessTokenCookie).cookie(refreshTokenCookie)
    }

    is Failure -> {
      val (statusCode, errorCode, _) = supabaseAuthResult.reason
      log.warn { "Could not refresh the access token $errorCode ($statusCode)" }
      return response // Could not refresh the JWT access token: no problem until the JWT access token expires.
    }
  }
}

private fun Request.accessTokenFromCookie(): String? = this.tokenFromCookieWithName("sb-access-token")
private fun Request.refreshTokenFromCookie(): String? = this.tokenFromCookieWithName("sb-refresh-token")
private fun Request.tokenFromCookieWithName(cookieName: String): String? =
  this.cookies().firstOrNull { cookie -> cookie.name == cookieName }?.value

private fun validatedJwtAndAuthDetailsFrom(jwtAccessToken: String): JwtParseResult? {
  val decodedJwt = try {
    verifier.verify(jwtAccessToken)
  } catch (_: JWTVerificationException) {
    log.warn { "Could not verify a JWT token: $jwtAccessToken" }
    return null
  }
  val userUuid = decodedJwt.claims["sub"]?.let { claim ->
    try {
      UUID.fromString(claim.toString().replace("\"", ""))
    } catch (_: IllegalArgumentException) {
      null
    }
  } ?: run {
    log.warn { "Could not parse the user UUID from the JWT token $decodedJwt" }
    return null
  }
  val issuedAt = decodedJwt.claims["iat"]?.toString()?.toLong() ?: throw RuntimeException("Expected iat")
  val expiresAt = decodedJwt.claims["exp"]?.toString()?.toLong() ?: throw RuntimeException("Expected exp")
  return JwtParseResult(decodedJwt, userUuid, Instant.ofEpochSecond(issuedAt), Instant.ofEpochSecond(expiresAt))
}

private data class JwtParseResult(
  val decodedJwt: DecodedJWT,
  val userUuid: UUID,
  val issuedAt: Instant,
  val expiresAt: Instant
)
