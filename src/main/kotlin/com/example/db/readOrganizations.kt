package com.example.db

import com.example.filter.DbCtx
import io.exoquery.sql.Sql
import io.exoquery.sql.runOn
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable


suspend fun DbCtx.listOrganizations() =
  Sql("SELECT * FROM organization").queryOf<Organization>().runOn(this)

@Serializable
data class Organization(
  val organizationId: Long,

  val name: String,

  @OptIn(ExperimentalUuidApi::class)
  val managersUserId: Uuid,

  @Serializable(with = InstantToPgTimestamptzSerializer::class)
  val createdAt: Instant,

  @Serializable(with = InstantToPgTimestamptzSerializer::class)
  val updatedAt: Instant
)
