import filter.authenticatedDbConnection
import filter.cookieBasedJwtAuthenticator
import handler.auth.authReturnGetHandler
import handler.auth.requestPasswordResetPostHandler
import handler.auth.signInGetHandler
import handler.auth.signInPostHandler
import handler.auth.signOutPostHandler
import handler.auth.signUpPostHandler
import handler.portal.dashboardHandler1
import handler.portal.dashboardHandler2
import handler.portal.dashboardHandler3
import handler.portal.dbTestHandler
import handler.portal.reseedDbHandler
import handler.redirectTo
import html.template.passwordreset.RequestPasswordResetForm
import html.template.passwordreset.requestPasswordResetPage
import html.template.signup.SignUpForm
import html.template.signup.signUpPage
import io.konform.validation.Valid
import lib.urlpath.UrlPath
import org.http4k.core.Method
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.routing.PathMethod
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes
import org.http4k.routing.static

val portalRouter = routes(
  Paths.dashboard bind GET to ::dashboardHandler1,
  Paths.dashboard1 bind GET to ::dashboardHandler1,
  Paths.dashboard2 bind GET to ::dashboardHandler2,
  Paths.dashboard3 bind GET to ::dashboardHandler3,
  Paths.db bind GET to ::dbTestHandler,
  Paths.reseed bind GET to ::reseedDbHandler,
)

val mainRouter = routes(

  // ### Related to sign up/in/out

  Paths.ping bind GET to { Response(OK).body("pong") },
  Paths.signUp bind GET to { signUpPage(SignUpForm.empty(), Valid(SignUpForm.empty())) },
  Paths.signUp bind POST to ::signUpPostHandler,
  Paths.signIn bind GET to ::signInGetHandler,
  Paths.signIn bind POST to ::signInPostHandler,
  Paths.signOut bind GET to ::signOutPostHandler, // Just for convenience: using POST here is the correct approach.
  Paths.signOut bind POST to ::signOutPostHandler,
  Paths.requestPasswordReset bind GET to { req ->
    requestPasswordResetPage(RequestPasswordResetForm.empty(), Valid(RequestPasswordResetForm.empty()))
  },
  Paths.requestPasswordReset bind POST to ::requestPasswordResetPostHandler,
  Paths.authReturn bind GET to ::authReturnGetHandler,
  Paths.verificationEmailSent bind GET to {
    Response(OK).body("<html><body>Email sent! Check your <a href='http://localhost:54324' target='_blank'>mail box</a>.</body></html>")
  },
  Paths.optExpired bind GET to {
    Response(OK).body("<html><body>Verification link expired. Try to <a>reset your password</a> to receive a new link.</body></html>")
  },

  Paths.root bind GET to { redirectTo(Paths.dashboard.path()) }, // when not signed in, ends up in at the sign-in form

  //  Paths.passwordResetVerification bind GET to { req ->
  //    resetPasswordPage(ResetPasswordForm.empty(), Valid(ResetPasswordForm.empty()))
  //  },
  //  Paths.passwordResetVerification bind POST to { req -> resetPasswordPostHandler(req) },


  // ### The portal sub-router (with its own additional filter stack)

  Paths.dashboard bind GET to { redirectTo(Paths.dashboard.path()) },
  Paths.portal bind cookieBasedJwtAuthenticator(jwtContextKey, userUuidContextKey)
    .then(authenticatedDbConnection(dbContextKey, authedQueryCacheContextKey))
    .then(portalRouter),


  // ### Static routing

  Paths.faviconIco bind GET to {
    Response(OK).body(Classpath("static").load("/favicon.ico")!!.openStream())
  },
  Paths.robotsTxt bind GET to {
    Response(OK).body(Classpath("static").load("/robots.txt")!!.openStream())
  },
  Paths.static bind static(Classpath("static"))
)


// Copied from `org.http4k.routing.bindKt`, adjusted to work on [UrlPath].
infix fun UrlPath.bind(method: Method) = PathMethod(this.template(), method)
infix fun UrlPath.bind(httpHandler: RoutingHttpHandler) = httpHandler.withBasePath(this.template())
