package com.example.handler.portal

import com.example.db.OrganizationDao
import com.example.dbContextKey
import com.example.filter.withServiceRole
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK


fun reseedDbHandler(req: Request): Response {
  dbContextKey(req).inTransaction<Unit, Exception> { dbtx ->
    dbtx.withServiceRole(req) { dbtx2 ->
      val orgDao = dbtx2.attach(OrganizationDao::class.java)
      orgDao.runSeed()
    }
  }
  return Response(OK).body("reseeded.")
}
