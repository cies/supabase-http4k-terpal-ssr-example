package com.example

import com.example.db.OrganizationDao
import com.example.filter.authenticatedJdbiInitializer
import com.example.filter.jwtByCookiesAuthenticator
import com.example.handler.registration.registerPostHandler
import com.example.handler.registration.signInPostHandler
import com.example.html.registration.RegistrationForm
import com.example.html.registration.registrationPage
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
  Paths.jdbi.template() bind GET to { req ->
    val db = dbContextKey(req)
    val dbRetrieved = db.inTransaction<String, Exception> { dbtx ->
      val orgDao = dbtx.attach(OrganizationDao::class.java)
      orgDao.listOrganizations().joinToString(", ") { it.name }
    }
    Response(OK).body("$dbRetrieved -- And no exceptions... must be good!")
  },
  Paths.metrics.template() bind GET to { req -> Response(OK).body("Example metrics route") }
)

val mainRouter = routes(
  Paths.ping.template() bind GET to { Response(OK).body("pong") },
  Paths.signUp.template() bind GET to { registrationPage(RegistrationForm.empty(), Valid(RegistrationForm.empty())) },
  Paths.signUp.template() bind POST to { req -> registerPostHandler(req) },
  Paths.signIn.template() bind GET to { req ->
    signInPage(SignInForm.empty().copy(target = req.query("target")), Valid(SignInForm.empty()))
  },
  Paths.signIn.template() bind POST to { req -> signInPostHandler(req) },

  Paths.portal.template() bind
    jwtByCookiesAuthenticator(jwtContextKey, userUuidContextKey)
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
