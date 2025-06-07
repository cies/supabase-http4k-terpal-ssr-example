package com.example.lib.supabase

import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.SameSite
import java.time.Instant
import java.time.temporal.ChronoUnit

fun SupabaseTokens.toCookies(): Pair<Cookie, Cookie> {
  fun safeCookieFrom(name: String, value: String, expires: Instant) = Cookie(
      name = name,
      value = value,
      expires = expires,
      path = "/", // Sent to all endpoints.
      secure = true, // Instructs browsers to only send the cookie on secure connections (except localhost).
      httpOnly = true, // Not accessible to JavaScript code, but will be send along with HTTP(S) calls made by JS code.
      sameSite = SameSite.Strict // Prevents cookie from being sent with requests that originate from other domains.
  )
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
