package com.example.filter

import com.example.db.resetSupabaseAuth
import com.example.db.setSupabaseAuth
import com.example.jwtContextKey
import com.example.userUuidContextKey
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.time.Instant
import java.util.UUID
import kotlin.math.absoluteValue
import org.http4k.core.Filter
import org.http4k.core.Uri
import org.http4k.core.with
import org.http4k.lens.RequestLens
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin


val dataSource = HikariDataSource(
  HikariConfig().apply {
    this.jdbcUrl = "jdbc:postgresql://localhost:54322/postgres"
    this.username = "postgres"
    this.password = "postgres"
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

      db.setSupabaseAuth(
        userUuidContextKey(it),
        "qw@qw", // jwt.claims["email"],
        (Math.random() * 100).toInt().absoluteValue.toLong(),
        Uri.of("https://auth.example.com/auth/v2"),
        Instant.now()
      )

      val response = next(it.with(dbContextKey of db))

      db.resetSupabaseAuth()

      response
    }
  }
}
