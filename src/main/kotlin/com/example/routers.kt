package com.example

import com.example.db.OrganizationDao
import com.example.handler.registration.registerPostHandler
import com.example.html.registration.RegistrationForm
import com.example.html.registration.registrationPage
import com.natpryce.krouton.http4k.resources
import io.konform.validation.Valid
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static


val authenticatedRouter: HttpHandler = resources {
  Paths.ping methods {
    GET { _ -> Response(OK).body("pong") }
  }
  Paths.register methods {
    GET { _ -> registrationPage(RegistrationForm.empty(), Valid(RegistrationForm.empty())) }
    POST { r -> registerPostHandler(r) }
  }
  Paths.metrics methods {
    GET { req -> Response(OK).body("Example metrics route") }
  }
  Paths.jdbi methods {
    GET { req ->
      val db = dbContextKey(req)
      val dbRetrieved = db.inTransaction<String, Exception> { dbtx ->
        val orgDao = dbtx.attach(OrganizationDao::class.java)
        orgDao.listOrganizations().joinToString(", ") { it.name }
      }
      Response(OK).body("$dbRetrieved -- And no exceptions... must be good!")
    }
  }

  Paths.oAuthRoot methods {
    GET to oauthProvider.authFilter.then { Response(OK).body("hello!") }
  }
  Paths.oAuthCallback methods {
    GET to oauthProvider.callback
  }
}

val unauthenticatedRouter: HttpHandler = resources {
  Paths.ping methods {
    GET { _ -> Response(OK).body("pong") }
  }
  Paths.register methods {
    GET { _ -> registrationPage(RegistrationForm.empty(), Valid(RegistrationForm.empty())) }
    POST { r -> registerPostHandler(r) }
  }
  Paths.metrics methods {
    GET { req -> Response(OK).body("Example metrics route") }
  }
  Paths.jdbi methods {
    GET { req ->
      val db = dbContextKey(req)
      val dbRetrieved = db.inTransaction<String, Exception> { dbtx ->
        val orgDao = dbtx.attach(OrganizationDao::class.java)
        orgDao.listOrganizations().joinToString(", ") { it.name }
      }
      Response(OK).body("$dbRetrieved -- And no exceptions... must be good!")
    }
  }

  Paths.oAuthRoot methods {
    GET to oauthProvider.authFilter.then { Response(OK).body("hello!") }
  }
  Paths.oAuthCallback methods {
    GET to oauthProvider.callback
  }
}

val staticAssetsRouter: HttpHandler = routes(
  "/favicon.ico" bind static(Classpath(), "favicon.ico" to ContentType.Text("image/x-icon")),
  "/robots.txt" bind static(Classpath(), "robots.txt" to ContentType.TEXT_PLAIN),
  "/static" bind static(Classpath("static"))
)

