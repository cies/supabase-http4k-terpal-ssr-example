package com.example.handler.portal

import com.example.db.OrganizationDao
import com.example.dbContextKey
import com.example.filter.withServiceRole
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK


fun jdbiTestHandler(req: Request): Response {
  val dbRetrieved = dbContextKey(req).inTransaction<String, Exception> { dbtx ->
    val orgDao = dbtx.attach(OrganizationDao::class.java)

    val firstWithout = orgDao.listOrganizations().joinToString(", ") { it.name }

    val withServiceRole = dbtx.withServiceRole(req) {
      orgDao.listOrganizations().joinToString(", ") { it.name }
    }

    orgDao.listOrganizations().joinToString(", ") { it.name } + "//" + withServiceRole + "//"  + firstWithout
  }
  return Response(OK).body("$dbRetrieved -- And no exceptions... must be good!")
}
