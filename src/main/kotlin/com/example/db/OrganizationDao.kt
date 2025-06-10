package com.example.db

import java.time.Instant
import java.util.UUID
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate


/** This to demo the query "in separate file" style that Jdbi supports. */
@UseClasspathSqlLocator
interface OrganizationDao {

  @SqlQuery
  fun listOrganizations(): List<Organization>

  @SqlQuery
  fun fetchOrganizationById(id: Long): Organization

  @SqlUpdate
  fun runSeed()
}


data class Organization(
  val organizationId: Long,
  val name: String,
  val managerUserId: UUID,
  val createdAt: Instant,
  val updatedAt: Instant
)
