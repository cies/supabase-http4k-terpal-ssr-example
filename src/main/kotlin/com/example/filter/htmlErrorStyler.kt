package com.example.filter

import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status

private val log = KotlinLogging.logger {}

val htmlErrorStyler = Filter { next ->
  { req ->
    val response = next(req)
    when (response.status) {
      Status.NOT_FOUND -> Response(Status.NOT_FOUND)
        .body("<html><h1>Not Found (404)</h1><p>The page you requested does not exist.</p></html>")
        .header("Content-Type", "text/html")

      // This status is called "UNAUTHORIZED", but is more accurately described by "unauthenticated".
      // In practice, we often respond with a "redirect to sign-in" instead of this status code.
      Status.UNAUTHORIZED -> Response(Status.UNAUTHORIZED)
        .body("<html><h1>Unauthorized (401)</h1><p>Please login first.</p></html>")
        .header("Content-Type", "text/html")

      // This status means "unauthorized" (authenticated but insufficient privileges).
      Status.FORBIDDEN -> Response(Status.FORBIDDEN)
        .body("<html><h1>Forbidden (403)</h1><p>Insufficient privileges.</p></html>")
        .header("Content-Type", "text/html")

      // This get thrown when the path matches but the HTTP verb does not.
      Status.METHOD_NOT_ALLOWED -> {
        log.warn { "Call to ${req.uri} with HTTP verb ${req.method} could not be routed (405)" }
        Response(Status.METHOD_NOT_ALLOWED)
          .body("<html><h1>Not allowed (405)</h1><p>Incorrect request.</p></html>")
          .header("Content-Type", "text/html")
      }

      else -> response
    }
  }
}
