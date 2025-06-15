package handler.auth

import Paths
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import handler.redirectAfterFormSubmission
import html.template.signin.SignInForm
import html.template.signin.signInPage
import io.github.oshai.kotlinlogging.KotlinLogging
import io.konform.validation.Invalid
import io.konform.validation.Valid
import io.konform.validation.path.ValidationPath
import lib.formparser.decodeOrFailWith
import lib.supabase.fetchSupabaseTokens
import lib.supabase.toCookies
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.body.form
import org.http4k.core.cookie.cookie


private val log = KotlinLogging.logger {}

fun signInPostHandler(req: Request): Response {
  val formDto: SignInForm = req.form().decodeOrFailWith { reason ->
    log.warn { "Failed to decode SignUpForm: $reason" }
    return Response(BAD_REQUEST)
  }
  
  when (val validationResult = formDto.validate()) {
    is Invalid -> return signInPage(formDto, validationResult)

    is Valid<SignInForm> -> {
      when (val signInResult = fetchSupabaseTokens(formDto.email ?: "", formDto.password ?: "")) {
        is Success -> {
          val (accessTokenCookie, refreshTokenCookie) = signInResult.value.toCookies()
          val target = if (formDto.target.isNullOrBlank()) Paths.db.path() else formDto.target
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
