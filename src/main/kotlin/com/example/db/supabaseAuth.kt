package com.example.db

import java.time.Instant
import java.util.*
import org.http4k.core.Uri
import org.intellij.lang.annotations.Language
import org.jdbi.v3.core.Handle

/** Cannot do this with a prepared statement (interpolation does not seem to work for `set` queries. */
fun Handle.setSupabaseAuth(userUuid: UUID, userEmail: String, orgId: Long, issuer: Uri, issuedAt: Instant) {
  // TODO: More sanitation of "user" input!
  val q: String = @Language("PostgreSQL") """
    select set_config('request.jwt.claim.sub', '${userUuid}', false);
    select set_config('request.jwt.claim.email', '${userEmail.replace("'", "").trim()}', false);
    select set_config('request.jwt.claim.org', '$orgId', false);
    select set_config('request.jwt.claim.iss', '$issuer', false);
    select set_config('request.jwt.claim.iat', '${issuedAt.epochSecond}', false);
    select set_config('request.jwt.claim.role', 'authenticated', false);
    select set_config('role', 'authenticated', false);
  """.trimIndent()
  this.createUpdate(q).execute()
}

/** Without resetting these values, later queries will be wrongly authenticated. */
fun Handle.resetSupabaseAuth() {
  // Cannot reset keys, so we set them to empty strings...
  // The 'anon' role is what we also default new connections/sessions to in the HikariCP configuration.
  val q: String = @Language("PostgreSQL") """
    select set_config('request.jwt.claim.sub', '', false);
    select set_config('request.jwt.claim.email', '', false);
    select set_config('request.jwt.claim.org', '', false);
    select set_config('request.jwt.claim.iss', '', false);
    select set_config('request.jwt.claim.iat', '', false);
    select set_config('request.jwt.claim.role', 'anon', false);
    select set_config('role', 'anon', false);
  """.trimIndent()
  this.createUpdate(q).execute()
}
