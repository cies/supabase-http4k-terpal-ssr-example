package com.example.db

import com.example.Organization
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

@UseClasspathSqlLocator
interface OrganizationDao {

  @SqlQuery
  fun listOrganizations(): List<Organization>

  @SqlQuery
  fun fetchOrganizationById(id: Long): Organization
}
