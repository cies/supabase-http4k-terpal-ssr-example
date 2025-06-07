package com.example.handler

import com.example.Paths
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.SEE_OTHER
import org.http4k.core.Uri
import org.http4k.lens.FormField
import org.http4k.lens.urlEncoded
import org.http4k.urlEncoded

fun redirectAfterFormSubmission(location: String): Response {
  return Response(SEE_OTHER).header("Location", location)
}

fun redirectToSignIn(targetPath: String): Response {
  FormField.urlEncoded()
  return Response(FOUND).header("Location", Paths.signIn.path() + "?target=${targetPath.urlEncoded()}")
}

