package com.example.handler.auth

import com.example.Paths
import com.example.handler.redirectAfterFormSubmission
import com.example.html.template.signup.SignUpForm
import com.example.html.template.signup.signUpPage
import com.example.lib.formparser.deserialize
import com.example.lib.supabase.fetchSupabaseTokens
import com.example.lib.supabase.safeCookieFrom
import com.example.lib.supabase.signUpWithEmail
import com.example.lib.supabase.toCookies
import com.example.moshi
import com.squareup.moshi.adapter
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.valueOrNull
import io.konform.validation.Invalid
import io.konform.validation.Valid
import io.konform.validation.path.ValidationPath
import io.konform.validation.path.toPathSegment
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.body.form
import org.http4k.core.cookie.cookie

fun authReturnGetHandler(req: Request): Response {
//  val response = redirectAfterFormSubmission(Paths.jdbi.absolutePath())
//  req.query("access_token")?.let { accessToken ->
//    response.cookie(
//      safeCookieFrom(
//        "access_token",
//        accessToken,
//        Instant.now().plus(360000, ChronoUnit.MINUTES)
//      )
//    )
//  }
//  req.query("refresh_token")?.let { accessToken ->
//    response.cookie(
//      safeCookieFrom(
//        "refresh_token",
//        accessToken,
//        Instant.now().plus(990000, ChronoUnit.MINUTES)
//      )
//    )
//  }
//  return response
  return Response(OK).body("""
    <html>
    <head>
    </head>
    <script>
      (function () {
        const hashLessUrlEncodedPairs = window.location.hash.substring(1);
        const params = new Map(new URLSearchParams(hashLessUrlEncodedPairs));
        console.log(params);
        if (params.get('error_code') === 'opt_expired') {
          window.location.href = "${Paths.optExpired.absolutePath()}";
        }
        var expiresAt = new Date(0); // The 0 there is important
        expiresAt.setUTCSeconds(params.get('expires_at'));
        document.cookie = `sb-access-token="${'$'}{params.get('access_token')}"; expires=${'$'}{expiresAt.toUTCString()}; path=/; Secure; SameSite=Strict`;
        document.cookie = `sb-refresh-token="${'$'}{params.get('refresh_token')}"; expires=${'$'}{expiresAt.toUTCString()}; path=/; Secure; SameSite=Strict`;
        if (params.get('access_token')) {
          window.location.href = "${Paths.jdbi.absolutePath()}";
        }
      })();
    </script>
    </html>
  """.trimIndent())
}

// ?access_token=eyJhbGciOiJIUzI1NiIqdEfeLtZUhe4SnZedo0Kqg
// &expires_at=1750047536
// &expires_in=360000
// &refresh_token=dyfuoblcm5n4
// &token_type=bearer
// &type=signup
