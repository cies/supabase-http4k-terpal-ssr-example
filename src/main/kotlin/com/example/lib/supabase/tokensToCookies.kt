package com.example.lib.supabase

import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.SameSite
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.http4k.core.Response
import org.http4k.core.cookie.cookie

/**
 * We store the JWT access token and the reset token in cookies. This to make server-side rendering possible, while also
 * being fully compatible with Supabase's auth module (in case we want to make use of its automatically generated
 * GraphQL, REST or subscriptions API in the future).
 *
 * The downside is that we lose some of the improved safety wrt session tokens:
 * * We send the refresh token with every request (cannot send a cookie conditionally).
 * * We put very long expiry times on the JWT access token.
 *
 * This basically brings the security level of this scheme down to match that of a traditional session token cookie.
 */
fun SupabaseTokens.toCookies(): Pair<Cookie, Cookie> {
  val accessTokenCookie = safeCookieFrom(
    name = "sb-access-token",
    value = this.accessToken,
    expires = Instant.now().plus(this.expiresIn.toLong(), ChronoUnit.SECONDS)
  )
  val refreshTokenCookie = safeCookieFrom(
    name = "sb-refresh-token",
    value = this.refreshToken,
    expires = Instant.now().plus(1, ChronoUnit.DAYS)
  )
  return Pair(accessTokenCookie, refreshTokenCookie)
}

/** This is used on sign-out. Cannot unset cookies, so we update them with empty values and immediate expiration. */
fun Response.clearSupabaseAuthCookies() = this
  .cookie(safeCookieFrom("sb-access-token", "", Instant.now()))
  .cookie(safeCookieFrom("sb-refresh-token", "", Instant.now()))

/** Constructs a cookie with the safety parameters on. */
fun safeCookieFrom(name: String, value: String, expires: Instant) = Cookie(
  name = name,
  value = value,
  expires = expires,
  path = "/", // Sent to all endpoints.
  secure = true, // Instructs browsers to only send the cookie on secure connections (except localhost).
  httpOnly = true, // Not accessible to JavaScript code, but will be send along with HTTP(S) calls made by JS code.
  sameSite = SameSite.Strict // Prevents cookie from being sent with requests that originate from other domains.
)
