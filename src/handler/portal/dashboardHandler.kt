package handler.portal

import html.template.dashboard.dashboardPage
import io.github.oshai.kotlinlogging.KotlinLogging
import jwtContextKey
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.path


private val log = KotlinLogging.logger {}

fun dashboardHandler(req: Request): Response {
  val userEmail = jwtContextKey(req).userEmail!!
  return dashboardPage(userEmail, req.uri.path)
}
