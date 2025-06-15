package db

import io.exoquery.sql.Sql
import lib.blockingterpal.runOn


fun DbCtx.fetchUid(): List<String> =
  Sql("select auth.uid()").queryOf<String>().runOn(this)

fun DbCtx.fetchRole(): List<String> =
  Sql("select current_role").queryOf<String>().runOn(this)

fun DbCtx.fetchSupabaseRole(): List<String> =
  Sql("select current_setting('role', true)").queryOf<String>().runOn(this)

fun DbCtx.fetchSupabaseRole2(): List<String> =
  Sql("select current_setting('request.jwt.claim.role', true)").queryOf<String>().runOn(this)

fun DbCtx.fetchOrgClaim(): List<String> =
  Sql("select current_setting('request.jwt.claim.org', true)").queryOf<String>().runOn(this)

