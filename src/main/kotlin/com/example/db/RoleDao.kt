package com.example.db

import org.jdbi.v3.sqlobject.statement.SqlQuery

interface RoleDao {

  // `current_Setting('key', true)` is is more robust (`true` prevents errors, ensures null is returns if key not set).

  @SqlQuery("select current_role")
  fun fetchRole(): String

  @SqlQuery("select current_setting('role', true)")
  fun fetchSupabaseRole(): String

  @SqlQuery("select current_setting('request.jwt.claim.role', true)")
  fun fetchSupabaseRole2(): String

  @SqlQuery("select auth.uid()")
  fun fetchUid(): String

  @SqlQuery("select current_setting('request.jwt.claim.org', true)")
  fun fetchOrgClaim(): String
}
