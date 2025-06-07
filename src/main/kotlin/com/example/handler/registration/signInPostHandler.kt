package com.example.handler.registration

import com.example.Paths
import com.example.formparser.FormParamDeserializer
import com.example.handler.redirectAfterFormSubmission
import com.example.html.signin.SignInForm
import com.example.html.signin.signInPage
import com.example.lib.supabase.fetchSupabaseTokens
import com.example.lib.supabase.toCookies
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.konform.validation.Invalid
import io.konform.validation.Valid
import io.konform.validation.path.ValidationPath
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.body.form
import org.http4k.core.cookie.cookie

fun signInPostHandler(req: Request): Response {
  val formDto = FormParamDeserializer.deserialize(req.form(), SignInForm::class)
    ?: return Response(BAD_REQUEST)

  when (val validationResult = formDto.validate()) {
    is Invalid -> return signInPage(formDto, validationResult)

    is Valid<SignInForm> -> {
      when (val signInResult = fetchSupabaseTokens(formDto.email ?: "", formDto.password ?: "")) {
        is Success -> {
          val (accessTokenCookie, refreshTokenCookie) = signInResult.value.toCookies()
          val target = formDto.target ?: Paths.jdbi.absolutePath()
          return redirectAfterFormSubmission(Paths.jdbi.path()).cookie(accessTokenCookie).cookie(refreshTokenCookie)
        }

        is Failure -> {
          val invalidResult = Invalid.of(
            ValidationPath(listOf()),
            // TODO: make this more generic
            "${signInResult.reason.second} (${signInResult.reason.first})"
          )
          return signInPage(formDto, invalidResult)
        }
      }
    }
  }
}
