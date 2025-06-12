package com.example.handler.auth

import com.example.SUPABASE_BASEURL
import com.example.SUPABASE_SERVICE_ROLE_KEY
import com.example.html.template.passwordreset.RequestPasswordResetForm
import com.example.html.template.passwordreset.passwordResetLinkMaybeSentPage
import com.example.html.template.passwordreset.requestPasswordResetPage
import com.example.lib.formparser.deserialize
import com.example.lib.supabase.client
import com.example.moshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.adapter
import dev.forkhandles.result4k.valueOrNull
import io.github.oshai.kotlinlogging.KotlinLogging
import io.konform.validation.Invalid
import io.konform.validation.Valid
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.body.form


private val log = KotlinLogging.logger {}

@OptIn(ExperimentalStdlibApi::class)
private val jsonAdapter: JsonAdapter<RequestPasswordResetForm> = moshi.adapter<RequestPasswordResetForm>()

fun requestPasswordResetPostHandler(req: Request): Response {
  val formDto: RequestPasswordResetForm = jsonAdapter.fromJsonValue(deserialize(req.form()).valueOrNull())
    ?: return Response(BAD_REQUEST)

  when (val validationResult = formDto.validate()) {
    is Invalid -> return requestPasswordResetPage(formDto, validationResult)

    is Valid<RequestPasswordResetForm> -> {
      val email = validationResult.value.email!!
      val request = Request(POST, "$SUPABASE_BASEURL/auth/v1/recover")
        .header("Authorization", "Bearer $SUPABASE_SERVICE_ROLE_KEY")
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
