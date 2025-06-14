package filter

import db.DbCtx
import db.renderSetSupabaseAuthToAuthenticatedUserQuery
import db.setSupabaseAuthToAnon
import db.setSupabaseAuthToAuthenticatedUser
import env
import jwtContextKey
import supabasePostgresPassword
import supabasePostgresUrl
import supabasePostgresUsername
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.exoquery.sql.jdbc.TerpalDriver
import org.http4k.core.Filter
import org.http4k.core.with
import org.http4k.lens.RequestLens


private val dataSource = HikariDataSource(
  HikariConfig().apply {
    this.jdbcUrl = "jdbc:${supabasePostgresUrl(env)}"
    this.username = supabasePostgresUsername(env)
    this.password = supabasePostgresPassword(env).use { it } // can read only once
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


val dbctx: DbCtx = TerpalDriver.Postgres(dataSource)

/**
 * Important http4k `Filter` which:
 *   - Initializes a JDBI Handle (`db`), and closes it on response completion (possibly by an error).
 *   - Adds the db (`Handle`) to the request with the RequestKey mechanism.
 *   - Sets Supabase compatible role and claims from JWT (that should have been added by RequestKey in a previous filter).
 *   - Resets the Supabase role and claims after the requests has finished (so it can be returned safely to the connection pool).
 * It does NOT open (BEGIN) and close (COMMIT) a transaction, because:
 *   - Over use of transactions results in many locks being kept longer.
 *   - Increased rollback risk (due to db errors, or the application throwing exceptions).
 *   - Partial commits become cumbersome (polluting the code with `commitAndOpenNewTransaction()` calls).
 *   - Transactions come with overhead not needed for read only queries.
 * We prefer the programmer makes explicit transactions where they are needed (transactions can be nested),
 * instead of implicitly wrapping everything in transactions.
 */
fun authenticatedDbConnection(dbContextKey: RequestLens<DbCtx>, authedQueryCache: RequestLens<String>) =
  Filter { next ->
    {
      // Get auth details (JWT) from the request context and set them auth details on the Jdbi [Handle].
      val authedQueryForCache = renderSetSupabaseAuthToAuthenticatedUserQuery(jwtContextKey(it))
      dbctx.setSupabaseAuthToAuthenticatedUser(authedQueryForCache)

        // We now move on to the next layer (`Filter` or `Handler`) up in the stack, while passing Jdbi [Handle] along in
        // the request context.
        // We do not return the response immediately because we want to reset the db connection (so it does not contain
        // auth details before it is returned to the pool).
        val response = next(
          it.with(dbContextKey of dbctx)
            .with(authedQueryCache of authedQueryForCache)
        )

        // Reset the auth details on the db connection.
        dbctx.setSupabaseAuthToAnon()

        // Ensure this use-block evaluates to the http4k [Response].
        response

        // When this scope is closed, the Jdbi Handle gets closed: the db connection is returned to the pool.
      }
    }

