package com.example.handler.registration

import com.example.Paths
import com.example.formparser.FormParamDeserializer
import com.example.handler.redirect
import com.example.html.registration.RegistrationForm
import com.example.html.registration.registrationPage
import com.example.lib.supabase.SupabaseTokens
import com.example.lib.supabase.fetchSupabaseTokens
import com.example.supabase
import com.natpryce.krouton.path
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.konform.validation.Invalid
import io.konform.validation.Valid
import io.konform.validation.path.ValidationPath
import io.konform.validation.path.toPathSegment
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.body.form
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.SameSite
import org.http4k.core.cookie.cookie

fun registerPostHandler(req: Request): Response {
  val formDto = FormParamDeserializer.deserialize(req.form(), RegistrationForm::class)
    ?: return Response(BAD_REQUEST)

  when (val validationResult = formDto.validate()) {
    is Invalid -> return registrationPage(formDto, validationResult)

    is Valid<RegistrationForm> -> {
      when (val authResult = supabase.signUpWithEmail(formDto.email!!, formDto.password!!)) {
        is Failure -> {
          val invalidResult = Invalid.of(
            ValidationPath(listOf(RegistrationForm::email.toPathSegment())),
            authResult.reason.toString()
          )
          return registrationPage(formDto, invalidResult)
        }

        is Success -> {
          when (val signInResult = fetchSupabaseTokens(formDto.email, formDto.password)) {
            is Success -> {
              val (accessTokenCookie, refreshTokenCookie) = signInResult.value.toCookies()
              return redirect(Paths.jdbi.path()).cookie(accessTokenCookie).cookie(refreshTokenCookie)
            }

            is Failure -> {
              val invalidResult = Invalid.of(
                ValidationPath(listOf()),
                "${signInResult.reason.second} (${signInResult.reason.first})"
              )
              return registrationPage(formDto, invalidResult)
            }
          }
        }
      }
    }
  }
}

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
