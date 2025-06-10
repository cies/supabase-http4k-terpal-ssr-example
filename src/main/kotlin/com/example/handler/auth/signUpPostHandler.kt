package com.example.handler.auth

import com.example.Paths
import com.example.formparser.FormParamDeserializer
import com.example.handler.redirectAfterFormSubmission
import com.example.html.template.signup.SignUpForm
import com.example.html.template.signup.signUpPage
import com.example.lib.supabase.fetchSupabaseTokens
import com.example.lib.supabase.signUpWithEmail
import com.example.lib.supabase.toCookies
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

fun signUpPostHandler(req: Request): Response {
  val formDto = FormParamDeserializer.deserialize(req.form(), SignUpForm::class)
    ?: return Response(BAD_REQUEST)

  when (val validationResult = formDto.validate()) {
    is Invalid -> return signUpPage(formDto, validationResult)

    is Valid<SignUpForm> -> {
      when (val authResult = signUpWithEmail(formDto.email!!, formDto.password!!)) {
        is Failure -> {
          val invalidResult = Invalid.of(
            ValidationPath(listOf(SignUpForm::email.toPathSegment())),
            authResult.reason.toString()
          )
          return signUpPage(formDto, invalidResult)
        }

        is Success -> {
          when (val signInResult = fetchSupabaseTokens(formDto.email, formDto.password)) {
            is Success -> {
              val (accessTokenCookie, refreshTokenCookie) = signInResult.value.toCookies()
              return redirectAfterFormSubmission(Paths.jdbi.absolutePath())
                .cookie(accessTokenCookie)
                .cookie(refreshTokenCookie)
            }

            is Failure -> {
              val invalidResult = Invalid.of(ValidationPath(listOf()), signInResult.reason.message)
              return signUpPage(formDto, invalidResult)
            }
          }
        }
      }
    }
  }
}

