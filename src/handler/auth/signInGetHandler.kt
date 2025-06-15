package handler.auth

import dev.forkhandles.result4k.Success
import filter.accessTokenFromCookie
import html.template.signin.SignInForm
import html.template.signin.alreadySingedInPage
import html.template.signin.signInPage
import io.konform.validation.Valid
import lib.jwt.decodeAndVerifySupabaseJwt
import org.http4k.core.Request
import org.http4k.core.Response


fun signInGetHandler(req: Request): Response {
  if (req.accessTokenFromCookie()?.let(::decodeAndVerifySupabaseJwt) is Success) return alreadySingedInPage()

  val emptySignInForm = SignInForm.empty().copy(target = req.query("target"))
  return signInPage(emptySignInForm, Valid(emptySignInForm))
}
