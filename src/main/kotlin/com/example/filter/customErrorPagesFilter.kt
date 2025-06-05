package com.example.filter

import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status


val customErrorPagesFilter = Filter { next ->
  { request ->
    val response = next(request)
    when (response.status) {
      Status.NOT_FOUND -> Response(Status.NOT_FOUND)
        .body("<html><h1>Not Found (404)</h1><p>The page you requested does not exist.</p></html>")
        .header("Content-Type", "text/html")

      // This status is called "UNAUTHORIZED", but is more accurately described by "unauthenticated".
      Status.UNAUTHORIZED -> Response(Status.UNAUTHORIZED)
        .body("<html><h1>Unauthorized (401)</h1><p>Please login first.</p></html>")
        .header("Content-Type", "text/html")

      // This status means "unauthorized" (authenticated but insufficient privileges).
      Status.FORBIDDEN -> Response(Status.FORBIDDEN)
        .body("<html><h1>Forbidden (403)</h1><p>Insufficient privileges.</p></html>")
        .header("Content-Type", "text/html")

      else -> response
    }
  }
}
