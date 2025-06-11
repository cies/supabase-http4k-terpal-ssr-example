package com.example.filter

import com.example.SUPABASE_POSTGRES_PASSWORD
import com.example.SUPABASE_POSTGRES_URL
import com.example.SUPABASE_POSTGRES_USERNAME
import com.example.db.renderSetSupabaseAuthToAuthenticatedUserQuery
import com.example.db.setSupabaseAuthToAnon
import com.example.db.setSupabaseAuthToAuthenticatedUser
import com.example.jwtContextKey
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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
fun authenticatedJdbiInitializer(dbContextKey: RequestLens<Handle>, authedQueryCache: RequestLens<String>) = Filter { next ->
  {
    // Open a Jdbi [Handle]: a db connection gets taken from the pool.
    jdbi.open().use { db ->

      // Get auth details (JWT) from the request context and set them auth details on the Jdbi [Handle].
      val authedQueryForCache = renderSetSupabaseAuthToAuthenticatedUserQuery(jwtContextKey(it))
      db.setSupabaseAuthToAuthenticatedUser(authedQueryForCache)

      // We now move on to the next layer (`Filter` or `Handler`) up in the stack, while passing Jdbi [Handle] along in
      // the request context.
      // We do not return the response immediately because we want to reset the db connection (so it does not contain
      // auth details before it is returned to the pool).
      val response = next(
        it.with(dbContextKey of db)
          .with(authedQueryCache of authedQueryForCache)
      )

      // Reset the auth details on the db connection.
      db.setSupabaseAuthToAnon()

      // Ensure this use-block evaluates to the http4k [Response].
      response

      // When this scope is closed, the Jdbi Handle gets closed: the db connection is returned to the pool.
    }
  }
}
