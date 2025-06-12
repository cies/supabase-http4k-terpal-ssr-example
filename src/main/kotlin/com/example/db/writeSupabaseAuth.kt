package com.example.db

import com.example.authedQueryCacheContextKey
import com.example.lib.jwt.JwtData
import java.util.UUID
import kotlin.math.absoluteValue
import org.http4k.core.Request
import org.intellij.lang.annotations.Language

/** This function works on a pre-rendered query, so we can cache it. */
fun DbCtx.setSupabaseAuthToAuthenticatedUser(@Language("SQL") preRenderedQuery: String) {
  this.database.connection.use { jdbc -> jdbc.prepareStatement(preRenderedQuery).execute() }
}

/** Cannot do this with a prepared statement (interpolation does not seem to work for `set` queries). */
fun renderSetSupabaseAuthToAuthenticatedUserQuery(jwt: JwtData): String {
  val userUuid = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") // userUuidContextKey(it), TODO: revert
  val userEmail = jwt.userEmail ?: ""
  val orgId = (Math.random() * 100).toInt().absoluteValue.toLong() // TODO: replace with value from metadata
  val issuer = jwt.issuer.replace("\"", "").replace("\'", "")
  val issuedAt = jwt.issuedAt.epochSecond
  return (@Language("SQL") """
    select set_config('request.jwt.claim.sub', '${userUuid}', false);
    select set_config('request.jwt.claim.email', '${userEmail.replace("'", "").trim()}', false);
    select set_config('request.jwt.claim.org', '$orgId', false);
    select set_config('request.jwt.claim.iss', '$issuer', false);
    select set_config('request.jwt.claim.iat', '$issuedAt', false);
    select set_config('request.jwt.claim.role', 'authenticated', false);
    select set_config('role', 'authenticated', false);
  """).trimIndent()
}

/**
 * Convenience function to run one-or-more database queries as 'postrgres' role,
 * and then which to the 'authenticated' role (with Supabase auth JWT details set).
 */
// TODO: eventually this should be removed, only migrations should do this.
fun <R> DbCtx.withPostgresRole(req: Request, block: (DbCtx) -> R): R {
  this.setSupabaseAuthToPostgresRole()

  val blockResult = block(this)

  // Set auth details with a cached query from the request context.
  this.setSupabaseAuthToAuthenticatedUser(@Language("SQL") authedQueryCacheContextKey(req))
  return blockResult
}

/**
 * Convenience function to run one-or-more database queries as 'service_role' role,
 * and then which to the 'authenticated' role (with Supabase auth JWT details set).
 */
fun <R> DbCtx.withServiceRoleDANGER(req: Request, block: (DbCtx) -> R): R {
  this.setSupabaseAuthToServiceRole()

  val blockResult = block(this)

  // Set auth details with a cached query from the request context.
  this.setSupabaseAuthToAuthenticatedUser(@Language("SQL") authedQueryCacheContextKey(req))
  return blockResult
}

/**
 * Reset the JWT and set the postgres role to 'anon', the lowest permission-holding role.
 *
 * IMPORTANT: This is run before the db connection with a role set is returned to the pool,
 * or later queries will be wrongly authenticated!
 */
fun DbCtx.setSupabaseAuthToAnon() {
  // Cannot reset keys, so we set them to empty strings...
  // The 'anon' role is what we also default new connections/sessions to in the HikariCP configuration.
  val q: String = jwtResetStatements + @Language("SQL") """
    select set_config('role', 'anon', false);
  """.trimIndent()
  this.database.connection.use { jdbc -> jdbc.prepareStatement(q).execute() }
}

/** Reset the JWT and set the role to 'service_role' which can view and manage all data but not change the schema. */
fun DbCtx.setSupabaseAuthToServiceRole() {
  // Cannot reset keys, so we set them to empty strings...
  // The 'anon' role is what we also default new connections/sessions to in the HikariCP configuration.
  val q: String = jwtResetStatements + @Language("SQL") """
    select set_config('role', 'service_role', false);
  """.trimIndent()
  this.database.connection.use { jdbc -> jdbc.prepareStatement(q).execute() }
}

/** Reset the JWT and set the role to the 'postgres' superuser role which can do everything: also change the schema! */
// TODO: eventually this should be removed, only migrations should do this.
fun DbCtx.setSupabaseAuthToPostgresRole() {
  // Cannot reset keys, so we set them to empty strings...
  // The 'anon' role is what we also default new connections/sessions to in the HikariCP configuration.
  val q: String = jwtResetStatements + @Language("SQL") """
    select set_config('role', 'postgres', false);
  """.trimIndent()
  this.database.connection.use { jdbc -> jdbc.prepareStatement(q).execute() }
}

private val jwtResetStatements = @Language("PostgreSQL") """
    select set_config('request.jwt.claim.sub', '', false);
    select set_config('request.jwt.claim.email', '', false);
    select set_config('request.jwt.claim.org', '', false);
    select set_config('request.jwt.claim.iss', '', false);
    select set_config('request.jwt.claim.iat', '', false);
    select set_config('request.jwt.claim.role', '', false);
  """.trimIndent()
