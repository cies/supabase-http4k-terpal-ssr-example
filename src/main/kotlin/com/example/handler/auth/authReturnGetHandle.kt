package com.example.handler.auth

import com.example.Paths
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK

fun authReturnGetHandler(reg: Request): Response {

  // This is an example of the query params we get back from Supabase's auth service:
  // ```
  //   ?access_token=eyJhbGciOiJIUzI1NiIqdEfeLtZUhe4SnZedo0Kqg
  //   &expires_at=1750047536
  //   &expires_in=360000
  //   &refresh_token=dyfuoblcm5n4
  //   &token_type=bearer
  //   &type=signup
  // ```

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
