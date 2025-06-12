package com.example.db

import io.exoquery.sql.Sql
import io.exoquery.sql.runOn


suspend fun DbCtx.fetchUid(): List<String> =
  Sql("select auth.uid()").queryOf<String>().runOn(this)

suspend fun DbCtx.fetchRole(): List<String> =
  Sql("select current_role").queryOf<String>().runOn(this)

suspend fun DbCtx.fetchSupabaseRole(): List<String> =
  Sql("select current_setting('role', true)").queryOf<String>().runOn(this)

suspend fun DbCtx.fetchSupabaseRole2(): List<String> =
  Sql("select current_setting('request.jwt.claim.role', true)").queryOf<String>().runOn(this)

suspend fun DbCtx.fetchOrgClaim(): List<String> =
  Sql("select current_setting('request.jwt.claim.org', true)").queryOf<String>().runOn(this)

