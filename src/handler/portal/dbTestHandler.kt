package handler.portal

import db.listOrganizations
import db.withServiceRoleDANGER
import dbContextKey
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK


fun dbTestHandler(req: Request): Response {
  val dbctx = dbContextKey(req)

  val orgs = dbctx.listOrganizations()

  val firstWithout = orgs.joinToString(", ") { it.name }

  val withServiceRole = dbctx.withServiceRoleDANGER(req) {
    val orgs2 = dbctx.listOrganizations()
    orgs2.joinToString(", ") { it.name }
  }

  val lastWithout = dbctx.listOrganizations()
  val dbRetrieved = lastWithout + "//" + withServiceRole + "//" + firstWithout

  return Response(OK).body("$dbRetrieved -- And no exceptions... must be good!")
}
