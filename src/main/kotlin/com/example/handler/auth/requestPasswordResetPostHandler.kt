package com.example.handler.auth

import com.example.env
import com.example.html.template.passwordreset.RequestPasswordResetForm
import com.example.html.template.passwordreset.passwordResetLinkMaybeSentPage
import com.example.html.template.passwordreset.requestPasswordResetPage
import com.example.lib.formparser.deserialize
import com.example.lib.supabase.client
import com.example.supabaseBaseUrl
import com.example.supabaseServiceRoleKey
import dev.forkhandles.result4k.valueOrNull
import io.github.oshai.kotlinlogging.KotlinLogging
import io.konform.validation.Invalid
import io.konform.validation.Valid
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.body.form


private val log = KotlinLogging.logger {}

fun requestPasswordResetPostHandler(req: Request): Response {
  val deserialized = deserialize(req.form()).valueOrNull() ?: throw AssertionError("Deserialization failed")
  val formDto: RequestPasswordResetForm = Json{}.decodeFromJsonElement(deserialized)
    ?: return Response(BAD_REQUEST)

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
