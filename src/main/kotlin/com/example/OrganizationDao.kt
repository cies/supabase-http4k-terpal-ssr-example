package com.example

import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

@UseClasspathSqlLocator
interface OrganizationDao {

  @SqlQuery
  fun listOrganizations(): List<Organization>

  @SqlQuery
  fun fetchOrganizationById(id: Long): Organization

  @SqlUpdate
  fun setSupabaseAuthParams(userUuid: String, userEmail: String, orgId: String, issuer: String, issuedAt: String)
}
