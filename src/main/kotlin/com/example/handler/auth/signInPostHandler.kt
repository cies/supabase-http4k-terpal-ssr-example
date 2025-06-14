package com.example.handler.auth

import com.example.Paths
import com.example.handler.redirectAfterFormSubmission
import com.example.html.template.signin.SignInForm
import com.example.html.template.signin.signInPage
import com.example.lib.formparser.formToJsonElement
import com.example.lib.supabase.fetchSupabaseTokens
import com.example.lib.supabase.toCookies
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.valueOrNull
import io.konform.validation.Invalid
import io.konform.validation.Valid
import io.konform.validation.path.ValidationPath
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.body.form
import org.http4k.core.cookie.cookie


fun signInPostHandler(req: Request): Response {
  val deserialized = formToJsonElement(req.form()).valueOrNull() ?: throw AssertionError("Deserialization failed")
  val formDto: SignInForm = Json{}.decodeFromJsonElement(deserialized)
    ?: return Response(BAD_REQUEST) // TODO: it is currently not yielding null

  when (val validationResult = formDto.validate()) {
    is Invalid -> return signInPage(formDto, validationResult)

    is Valid<SignInForm> -> {
      when (val signInResult = fetchSupabaseTokens(formDto.email ?: "", formDto.password ?: "")) {
        is Success -> {
          val (accessTokenCookie, refreshTokenCookie) = signInResult.value.toCookies()
          val target = formDto.target ?: Paths.db.absolutePath()
          return redirectAfterFormSubmission(target).cookie(accessTokenCookie).cookie(refreshTokenCookie)
        }

        is Failure -> {
          val invalidResult = Invalid.of(
            ValidationPath(listOf()),
            // TODO: make this more generic
            signInResult.reason.message
          )
          return signInPage(formDto, invalidResult)
        }
      }
    }
  }
}
