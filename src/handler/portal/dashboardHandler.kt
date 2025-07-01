package handler.portal

import html.template.dashboard.dashboardPage1
import html.template.dashboard.dashboardPage2
import html.template.dashboard.dashboardPage3
import io.github.oshai.kotlinlogging.KotlinLogging
import jwtContextKey
import org.http4k.core.Request
import org.http4k.core.Response


private val log = KotlinLogging.logger {}

fun dashboardHandler1(req: Request): Response {
  val userEmail = jwtContextKey(req).userEmail!!
  return dashboardPage1(userEmail, req.uri.path)
}

fun dashboardHandler2(req: Request): Response {
  val userEmail = jwtContextKey(req).userEmail!!
  return dashboardPage2(userEmail, req.uri.path)
}

fun dashboardHandler3(req: Request): Response {
  val userEmail = jwtContextKey(req).userEmail!!
  return dashboardPage3(userEmail, req.uri.path)
}
