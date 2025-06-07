package com.example.handler.registration

import com.example.Paths
import com.example.formparser.FormParamDeserializer
import com.example.handler.redirectAfterFormSubmission
import com.example.html.registration.RegistrationForm
import com.example.html.registration.registrationPage
import com.example.lib.supabase.fetchSupabaseTokens
import com.example.lib.supabase.toCookies
import com.example.supabase
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.konform.validation.Invalid
import io.konform.validation.Valid
import io.konform.validation.path.ValidationPath
import io.konform.validation.path.toPathSegment
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.body.form
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
              return redirectAfterFormSubmission(Paths.jdbi.path()).cookie(accessTokenCookie).cookie(refreshTokenCookie)
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

