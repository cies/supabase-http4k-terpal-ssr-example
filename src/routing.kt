import filter.authenticatedDbConnection
import filter.cookieBasedJwtAuthenticator
import handler.auth.authReturnGetHandler
import handler.auth.requestPasswordResetPostHandler
import handler.auth.signInGetHandler
import handler.auth.signUpPostHandler
import handler.auth.signInPostHandler
import handler.auth.signOutPostHandler
import handler.portal.dbTestHandler
import handler.portal.reseedDbHandler
import html.template.passwordreset.RequestPasswordResetForm
import html.template.passwordreset.requestPasswordResetPage
import html.template.signup.SignUpForm
import html.template.signup.signUpPage
import html.template.signin.SignInForm
import html.template.signin.signInPage
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
  Paths.db.template() bind GET to ::dbTestHandler,
  Paths.reseed.template() bind GET to ::reseedDbHandler,
  Paths.metrics.template() bind GET to { req -> Response(OK).body("Example metrics route") }
)

val mainRouter = routes(

  // ### Related to sign up/in/out

  Paths.ping.template() bind GET to { Response(OK).body("pong") },
  Paths.signUp.template() bind GET to { signUpPage(SignUpForm.empty(), Valid(SignUpForm.empty())) },
  Paths.signUp.template() bind POST to ::signUpPostHandler,
  Paths.signIn.template() bind GET to ::signInGetHandler,
  Paths.signIn.template() bind POST to ::signInPostHandler,
  Paths.signOut.template() bind GET to ::signOutPostHandler, // Just for convenience: using POST here is the correct approach.
  Paths.signOut.template() bind POST to ::signOutPostHandler,
  Paths.requestPasswordReset.template() bind GET to { req ->
    requestPasswordResetPage(RequestPasswordResetForm.empty(), Valid(RequestPasswordResetForm.empty()))
  },
  Paths.requestPasswordReset.template() bind POST to ::requestPasswordResetPostHandler,
  Paths.authReturn.template() bind GET to ::authReturnGetHandler,
  Paths.verificationEmailSent.template() bind GET to {
    Response(OK).body("<html><body>Email sent! Check your <a href='http://localhost:54324' target='_blank'>mail box</a>.</body></html>")
  },
  Paths.optExpired.template() bind GET to {
    Response(OK).body("<html><body>Verification link expired. Try to <a>reset your password</a> to receive a new link.</body></html>")
  },

  //  Paths.passwordResetVerification.template() bind GET to { req ->
  //    resetPasswordPage(ResetPasswordForm.empty(), Valid(ResetPasswordForm.empty()))
  //  },
  //  Paths.passwordResetVerification.template() bind POST to { req -> resetPasswordPostHandler(req) },


  // ### The portal sub-router (with its own additional filter stack)

  Paths.portal.template() bind cookieBasedJwtAuthenticator(jwtContextKey, userUuidContextKey)
    .then(authenticatedDbConnection(dbContextKey, authedQueryCacheContextKey))
    .then(portalRouter),


  // ### Static routing

  Paths.faviconIco.template() bind GET to {
    Response(OK).body(Classpath("static").load("/favicon.ico")!!.openStream())
  },
  Paths.robotsTxt.template() bind GET to {
    Response(OK).body(Classpath("static").load("/robots.txt")!!.openStream())
  },
  Paths.static.template() bind static(Classpath("static"))
)
