package com.example.handler.auth

import com.example.Paths
import com.example.formparser.FormParamDeserializer
import com.example.html.passwordreset.RequestPasswordResetForm
import com.example.html.passwordreset.passwordResetLinkMaybeSentPage
import com.example.html.passwordreset.requestPasswordResetPage
import com.example.supabase
import io.github.jan.supabase.auth.auth
import io.konform.validation.Invalid
import io.konform.validation.Valid
import kotlinx.coroutines.runBlocking
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.body.form


fun requestPasswordResetPostHandler(req: Request): Response {
  val formDto = FormParamDeserializer.deserialize(req.form(), RequestPasswordResetForm::class)
    ?: return Response(BAD_REQUEST)

  return when (val validationResult = formDto.validate()) {
    is Invalid -> requestPasswordResetPage(formDto, validationResult)

    is Valid<RequestPasswordResetForm> -> {
      val email = validationResult.value.email!!
      runBlocking {
        supabase.supabaseClient.auth.resetPasswordForEmail(email, redirectUrl = Paths.setNewPassword.fullUrl(req))
      }
      passwordResetLinkMaybeSentPage(email)
    }
  }
}
