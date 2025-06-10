package com.example.handler.auth

import com.example.html.signout.signedOutPage
import com.example.lib.supabase.clearSupabaseAuthCookies
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.cookie.cookies

fun signOutPostHandler(req: Request): Response {
  val wasSignedIn = req.cookies().any { it.name == "sb-access-token" }
  return signedOutPage(wasSignedIn).clearSupabaseAuthCookies()
}
