package filter

import handler.SignInReason
import handler.redirectToSignIn
import lib.jwt.JwtData
import lib.jwt.JwtError
import lib.jwt.decodeAndVerifySupabaseJwt
import lib.supabase.fetchSupabaseTokens
import lib.supabase.toCookies
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
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

/**
 * This http4k `Filter`...
 *   - Decodes and verifies the JWT (access token) from the request cookie.
 *   - Checks the JWT has not yet expired.
 *   - Redirects the user to the sign-in page (with `targetPath` and `reason`) when the above failed.
 *   - Adds the JWT data to the request context with the `RequestKey` mechanism.
 *   - Refreshes the JWT access token with the refresh token, by setting cookies on the response, when JWT gets old.
 */
fun cookieBasedJwtAuthenticator(jwtContextKey: RequestLens<JwtData>, userUuidContextKey: RequestLens<UUID>) =
  Filter { next ->
    {
      val jwtParseResult = it.accessTokenFromCookie()?.let(::decodeAndVerifySupabaseJwt)
      authenticateOrRedirectToSignIn(jwtParseResult, jwtContextKey, userUuidContextKey, next, it)
    }
  }

private fun authenticateOrRedirectToSignIn(
  jwtParseResult: Result<JwtData, JwtError>?,
  jwtContextKey: RequestLens<JwtData>,
  userUuidContextKey: RequestLens<UUID>,
  next: HttpHandler,
  request: Request
): Response {
  val validJwt: JwtData = when (jwtParseResult) {
    null -> return redirectToSignIn(SignInReason.InvalidAuthToken, request.uri.path)
    is Failure -> return if (jwtParseResult.reason == JwtError.Expired) {
      redirectToSignIn(SignInReason.SessionExpired, request.uri.path)
    } else {
      redirectToSignIn(SignInReason.InvalidAuthToken, request.uri.path)
    }

    is Success -> {
      jwtParseResult.value.apply {
        if (this.isAnonymous) redirectToSignIn(SignInReason.InvalidAuthToken, request.uri.path)
        if (this.userUuid == null) redirectToSignIn(SignInReason.InvalidAuthToken, request.uri.path)
      }
    }
  }

  // We have a valid JWT, so move on to the next layer (`Filter` or `Handler`) up in the stack, while passing data from
  // the JWT along in the request context.
  // We do not return the response immediately because we may have to refresh the tokens (by setting the cookie).
  val response =
    next(request.with(jwtContextKey of validJwt).with(userUuidContextKey of validJwt.userUuid!!))

  val jwtAge = Duration.between(validJwt.issuedAt, validJwt.expiresAt)
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
