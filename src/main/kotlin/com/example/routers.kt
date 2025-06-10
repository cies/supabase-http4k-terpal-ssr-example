package com.example

import com.example.filter.authenticatedJdbiInitializer
import com.example.filter.cookieBasedJwtAuthenticator
import com.example.handler.auth.requestPasswordResetPostHandler
import com.example.handler.auth.signUpPostHandler
import com.example.handler.auth.signInPostHandler
import com.example.handler.auth.signOutPostHandler
import com.example.handler.portal.jdbiTestHandler
import com.example.handler.portal.reseedDbHandler
import com.example.html.passwordreset.RequestPasswordResetForm
import com.example.html.passwordreset.requestPasswordResetPage
import com.example.html.signup.SignUpForm
import com.example.html.signup.signUpPage
import com.example.html.signin.SignInForm
import com.example.html.signin.signInPage
import io.konform.validation.Valid
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static

val portalRouter = routes(
  Paths.jdbi.template() bind GET to ::jdbiTestHandler,
  Paths.reseed.template() bind GET to ::reseedDbHandler,
  Paths.metrics.template() bind GET to { req -> Response(OK).body("Example metrics route") }
)

val mainRouter = routes(
  Paths.ping.template() bind GET to { Response(OK).body("pong") },
  Paths.signUp.template() bind GET to { signUpPage(SignUpForm.empty(), Valid(SignUpForm.empty())) },
  Paths.signUp.template() bind POST to ::signUpPostHandler,
  Paths.signIn.template() bind GET to { req ->
    signInPage(SignInForm.empty().copy(target = req.query("target")), Valid(SignInForm.empty()))
  },
  Paths.signIn.template() bind POST to ::signInPostHandler,
  Paths.signOut.template() bind GET to ::signOutPostHandler, // Just for convenience: using POST here is the correct approach.
  Paths.signOut.template() bind POST to ::signOutPostHandler,
  Paths.requestPasswordReset.template() bind GET to { req ->
    requestPasswordResetPage(RequestPasswordResetForm.empty(), Valid(RequestPasswordResetForm.empty()))
  },
  Paths.requestPasswordReset.template() bind POST to :: requestPasswordResetPostHandler,
  //  Paths.passwordResetVerification.template() bind GET to { req ->
  //    resetPasswordPage(ResetPasswordForm.empty(), Valid(ResetPasswordForm.empty()))
  //  },
  //  Paths.passwordResetVerification.template() bind POST to { req -> resetPasswordPostHandler(req) },

  Paths.portal.template() bind
    cookieBasedJwtAuthenticator(jwtContextKey, userUuidContextKey)
      .then(authenticatedJdbiInitializer(dbContextKey))
//      .then(addSupabaseToContext(supabaseContextKey)) // not yet needed, for now we use a global
      .then(portalRouter),

  Paths.faviconIco.template() bind GET to {
    Response(OK).body(Classpath("static").load("/favicon.ico")!!.openStream())
  },
  Paths.robotsTxt.template() bind GET to {
    Response(OK).body(Classpath("static").load("/robots.txt")!!.openStream())
  },
  Paths.static.template() bind static(Classpath("static")),
)
