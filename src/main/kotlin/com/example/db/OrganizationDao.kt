package com.example.db

import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator
import org.jdbi.v3.sqlobject.statement.SqlQuery

@UseClasspathSqlLocator
interface OrganizationDao {

  @SqlQuery
  fun listOrganizations(): List<Organization>

  @SqlQuery
  fun fetchOrganizationById(id: Long): Organization
}


data class Organization(val id: Long, val name: String)
