package com.example.handler.auth

import com.example.Paths
import com.example.html.template.passwordreset.RequestPasswordResetForm
import com.example.html.template.passwordreset.passwordResetLinkMaybeSentPage
import com.example.html.template.passwordreset.requestPasswordResetPage
import com.example.lib.supabase.supabaseClient
import com.example.moshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.adapter
import io.github.jan.supabase.auth.auth
import io.konform.validation.Invalid
import io.konform.validation.Valid
import kotlinx.coroutines.runBlocking
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.body.form

@OptIn(ExperimentalStdlibApi::class)
private val jsonAdapter: JsonAdapter<RequestPasswordResetForm> = moshi.adapter<RequestPasswordResetForm>()

fun requestPasswordResetPostHandler(req: Request): Response {
  val formDto: RequestPasswordResetForm = jsonAdapter.fromJsonValue((req.form())) ?: return Response(BAD_REQUEST)

  return when (val validationResult = formDto.validate()) {
    is Invalid -> requestPasswordResetPage(formDto, validationResult)

    is Valid<RequestPasswordResetForm> -> {
      val email = validationResult.value.email!!
      runBlocking {
        supabaseClient.auth.resetPasswordForEmail(email, redirectUrl = Paths.setNewPassword.fullUrl(req))
      }
      passwordResetLinkMaybeSentPage(email)
    }
  }
}
