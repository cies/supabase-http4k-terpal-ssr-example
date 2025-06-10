package com.example.db

import com.auth0.jwt.interfaces.DecodedJWT
import com.example.filter.lenientMapper
import com.fasterxml.jackson.databind.JsonNode
import java.util.UUID
import kotlin.math.absoluteValue
import org.intellij.lang.annotations.Language
import org.jdbi.v3.core.Handle

/** Cannot do this with a prepared statement (interpolation does not seem to work for `set` queries. */
fun Handle.setSupabaseAuthToAuthenticatedUser(jwt: DecodedJWT) {
  val userMetadata = lenientMapper.readValue(jwt.claims["user_metadata"].toString(), JsonNode::class.java)
  // TODO: More sanitation of "user" input!
  val userUuid = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") // userUuidContextKey(it),
  val userEmail = userMetadata["email"].textValue()
  val orgId = (Math.random() * 100).toInt().absoluteValue.toLong() // TODO: replace with value from metadata
  val issuer = jwt.claims["iss"].toString().replace("\"", "")
  val issuedAt = jwt.claims["iat"].toString()
  this.createUpdate(
    (@Language("PostgreSQL") """
    select set_config('request.jwt.claim.sub', '${userUuid}', false);
    select set_config('request.jwt.claim.email', '${userEmail.replace("'", "").trim()}', false);
    select set_config('request.jwt.claim.org', '$orgId', false);
    select set_config('request.jwt.claim.iss', '$issuer', false);
    select set_config('request.jwt.claim.iat', '$issuedAt', false);
    select set_config('request.jwt.claim.role', 'authenticated', false);
    select set_config('role', 'authenticated', false);
  """).trimIndent()
  ).execute()
}

/**
 * Reset the JWT and set the postgres role to 'anon', the lowest permission-holding role.
 *
 * IMPORTANT: This is run before the db connection with a role set is returned to the pool,
 * or later queries will be wrongly authenticated!
 */
fun Handle.setSupabaseAuthToAnon() {
  // Cannot reset keys, so we set them to empty strings...
  // The 'anon' role is what we also default new connections/sessions to in the HikariCP configuration.
  val q: String = jwtResetStatements + @Language("PostgreSQL") """
    select set_config('role', 'anon', false);
  """.trimIndent()
  this.createUpdate(q).execute()
}

/** Reset the JWT and set the role to 'service_role' which can view and manage all data but not change the schema. */
fun Handle.setSupabaseAuthToServiceRole() {
  // Cannot reset keys, so we set them to empty strings...
  // The 'anon' role is what we also default new connections/sessions to in the HikariCP configuration.
  val q: String = jwtResetStatements + @Language("PostgreSQL") """
    select set_config('role', 'service_role', false);
  """.trimIndent()
  this.createUpdate(q).execute()
}

/** Reset the JWT and set the role to the 'postgres' superuser role which can do everything: also change the schema! */
// TODO: eventually this should be removed, only migrations should do this.
fun Handle.setSupabaseAuthToPostgreRole() {
  // Cannot reset keys, so we set them to empty strings...
  // The 'anon' role is what we also default new connections/sessions to in the HikariCP configuration.
  val q: String = jwtResetStatements + @Language("PostgreSQL") """
    select set_config('role', 'postgres', false);
  """.trimIndent()
  this.createUpdate(q).execute()
}

private val jwtResetStatements = @Language("PostgreSQL") """
    select set_config('request.jwt.claim.sub', '', false);
    select set_config('request.jwt.claim.email', '', false);
    select set_config('request.jwt.claim.org', '', false);
    select set_config('request.jwt.claim.iss', '', false);
    select set_config('request.jwt.claim.iat', '', false);
    select set_config('request.jwt.claim.role', '', false);
  """.trimIndent()
