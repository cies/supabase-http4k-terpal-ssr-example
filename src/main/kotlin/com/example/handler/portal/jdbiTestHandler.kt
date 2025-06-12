package com.example.handler.portal

import com.example.db.listOrganizations
import com.example.db.withServiceRoleDANGER
import com.example.dbContextKey
import kotlinx.coroutines.runBlocking
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK


fun jdbiTestHandler(req: Request): Response {
  val dbctx = dbContextKey(req)

  val orgs = runBlocking { dbctx.listOrganizations() }

  val firstWithout = orgs.joinToString(", ") { it.name }

  val withServiceRole = dbctx.withServiceRoleDANGER(req) {
    val orgs2 = runBlocking { dbctx.listOrganizations() }
    orgs2.joinToString(", ") { it.name }
  }

  val lastWithout = runBlocking { dbctx.listOrganizations() }
  val dbRetrieved = lastWithout + "//" + withServiceRole + "//" + firstWithout

  return Response(OK).body("$dbRetrieved -- And no exceptions... must be good!")
}
