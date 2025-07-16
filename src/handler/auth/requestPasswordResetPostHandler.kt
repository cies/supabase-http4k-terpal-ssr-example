package handler.auth

import env
import html.template.passwordreset.RequestPasswordResetForm
import html.template.passwordreset.passwordResetLinkMaybeSentPage
import html.template.passwordreset.requestPasswordResetPage
import lib.formparser.formToJsonElement
import lib.supabase.client
import supabaseBaseUrl
import supabaseServiceRoleKey
import dev.forkhandles.result4k.valueOrNull
import html.template.signin.SignInForm
import io.github.oshai.kotlinlogging.KotlinLogging
import io.konform.validation.Invalid
import io.konform.validation.Valid
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import lib.formparser.decodeOrFailWith
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.body.form


private val log = KotlinLogging.logger {}

fun requestPasswordResetPostHandler(req: Request): Response {
  val formDto: RequestPasswordResetForm = req.form().decodeOrFailWith { reason ->
    log.warn { "Failed to decode RequestPasswordResetForm: $reason" }
    return Response(BAD_REQUEST)
  }

  when (val validationResult = formDto.validate()) {
    is Invalid -> return requestPasswordResetPage(formDto, validationResult)

    is Valid<RequestPasswordResetForm> -> {
      val email = validationResult.value.email!!
      val request = Request(POST, "${supabaseBaseUrl(env)}/auth/v1/recover")
        .header("Authorization", "Bearer ${supabaseServiceRoleKey(env)}")
        .header("Content-Type", "application/json")
        .body(Json.encodeToString(PasswordRecoveryRequest(email)))
      val response = client(request)

      if (response.status != OK) {
        log.warn { "Failed to request password reset link for '$email': ${response.bodyString()} (${response.status})" }
      }

      return passwordResetLinkMaybeSentPage(email)
    }
  }
}

@Serializable
data class PasswordRecoveryRequest(val email: String)
