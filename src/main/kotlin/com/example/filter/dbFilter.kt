package com.example.filter

import com.example.SUPABASE_POSTGRES_PASSWORD
import com.example.SUPABASE_POSTGRES_URL
import com.example.SUPABASE_POSTGRES_USERNAME
import com.example.db.resetSupabaseAuth
import com.example.db.setSupabaseAuth
import com.example.jwtContextKey
import com.example.userUuidContextKey
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule.Builder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlin.math.absoluteValue
import org.http4k.core.Filter
import org.http4k.core.with
import org.http4k.lens.RequestLens
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin


val dataSource = HikariDataSource(
  HikariConfig().apply {
    this.jdbcUrl = "jdbc:$SUPABASE_POSTGRES_URL"
    this.username = SUPABASE_POSTGRES_USERNAME
    this.password = SUPABASE_POSTGRES_PASSWORD
    this.driverClassName = "org.postgresql.Driver"
    this.maximumPoolSize = 10
    this.minimumIdle = 2
    this.idleTimeout = 60000 // ms
    this.connectionTimeout = 30000 // ms
    this.maxLifetime = 1800000 // ms
    this.isAutoCommit = true // any good?
    this.poolName = "PostgresHikariPool"
    connectionInitSql = "set role 'anon'" // principle of least privilege
  }
)

var jdbi: Jdbi = Jdbi.create(dataSource)
  .installPlugin(KotlinPlugin())
  .installPlugin(KotlinSqlObjectPlugin())
  .installPlugin(PostgresPlugin())

// Ignore JSON fields that are not known for us, e.g. when a third party adds fields that we haven't specified in DTO's yet

val lenientMapper: ObjectMapper = jacksonObjectMapper()
  .setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS)
  // With the following two properties only fields are serialized, getters are ignored
  .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
  .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
  // Fields that are null are by default not serialized
  .setSerializationInclusion(JsonInclude.Include.NON_NULL)
  .registerModule(JavaTimeModule())
  // This was needed to be able to derive null for an enum field from an empty string value.
  .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
  // If a third party adds more fields than our DTO's have, it should not throw an error
  .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
  .registerModule(
    Builder()
      .configure(KotlinFeature.NullToEmptyCollection, true)
      .configure(KotlinFeature.NullToEmptyMap, true)
      .configure(KotlinFeature.NullIsSameAsDefault, true)
      .configure(KotlinFeature.StrictNullChecks, false)
      .build()
  )

/**
 * Important http4k `Filter` which:
 *   - Initializes a JDBI Handle (`db`), and closes it on response completion (possibly by an error).
 *   - Adds the db (`Handle`) to the request with the RequestKey mechanism.
 *   - Sets Supabase compatible role and claims from JWT (that should have been added by RequestKey in a previous filter). TODO
 *   - Resets the Supabase role and claims after the requests has finished (so it can be returned safely to the connection pool).
 * It does NOT open (BEGIN) and close (COMMIT) a transaction, because:
 *   - Over use of transactions results in many locks being kept longer.
 *   - Increased rollback risk (due to db errors, or the application throwing exceptions).
 *   - Partial commits become cumbersome (polluting the code with `commitAndOpenNewTransaction()` calls).
 *   - Transactions come with overhead not needed for read only queries.
 * We prefer the programmer makes explicit transactions where they are needed (transactions can be nested),
 * instead of implicitly wrapping everything in transactions.
 */
fun dbFilter(dbContextKey: RequestLens<Handle>) = Filter { next ->
  {
    jdbi.open().use { db ->

      val jwt = jwtContextKey(it)

      val userMetadata = lenientMapper.readValue(jwt.claims["user_metadata"].toString(), JsonNode::class.java)

      db.setSupabaseAuth(
        userUuidContextKey(it),
        userMetadata["email"].textValue(),
        (Math.random() * 100).toInt().absoluteValue.toLong(), // TODO: replace with value from metadata
        jwt.claims["iss"].toString().replace("\"", ""),
        jwt.claims["iat"].toString()
      )

      val response = next(it.with(dbContextKey of db))

      db.resetSupabaseAuth()

      response
    }
  }
}
