package com.example.handler

import org.http4k.core.Response
import org.http4k.core.Status.Companion.SEE_OTHER

fun redirect(location: String): Response {
  return Response(SEE_OTHER).header("Location", location)
}
