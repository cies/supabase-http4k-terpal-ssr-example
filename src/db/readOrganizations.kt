package db

import io.exoquery.sql.Sql
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import lib.blockingterpal.runOn


fun DbCtx.listOrganizations() =
  Sql("SELECT * FROM organization").queryOf<Organization>().runOn(this)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Organization(
  val organizationId: Long,
  val name: String,
  val managersUserId: Uuid,
  val createdAt: DbInstant,
  val updatedAt: DbInstant
)
