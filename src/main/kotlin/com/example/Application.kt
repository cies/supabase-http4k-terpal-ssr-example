package com.example

import com.auth0.jwt.interfaces.DecodedJWT
import com.example.filter.customErrorPagesFilter
import com.example.filter.dbFilter
import com.example.filter.jwtFilter
import com.example.html.error.handleException
import com.example.lib.supabase.SupabaseAuth
import io.github.cdimascio.dotenv.dotenv
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.util.UUID
import org.http4k.client.JavaHttpClient
import org.http4k.core.Credentials
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.DebuggingFilters
import org.http4k.filter.MicrometerMetrics
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestKey
import org.http4k.security.InsecureCookieBasedOAuthPersistence
import org.http4k.security.OAuthProvider
import org.http4k.security.google
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.jdbi.v3.core.Handle

// this is a micrometer registry used mostly for testing - substitute the correct implementation.
val registry = SimpleMeterRegistry()

// Google OAuth Example
// Browse to: http://localhost:9000/oauth - you'll be redirected to google for authentication
val googleClientId = "myGoogleClientId"
val googleClientSecret = "myGoogleClientSecret"

// this is a test implementation of the OAuthPersistence interface, which should be
// implemented by application developers
val oAuthPersistence = InsecureCookieBasedOAuthPersistence("Google")

// pre-defined configuration exist for common OAuth providers
val oauthProvider = OAuthProvider.google(
  JavaHttpClient(),
  Credentials(googleClientId, googleClientSecret),
  Uri.of("http://localhost:9000/oauth/callback"),
  oAuthPersistence
)

data class Organization(val id: Long, val name: String)


val jwtContextKey = RequestKey.required<DecodedJWT>("jwt")
val userUuidContextKey = RequestKey.required<UUID>("userUuid")
val dbContextKey = RequestKey.required<Handle>("db")

val env = dotenv()
val SUPABASE_JWT_SECRET = env["SUPABASE_JWT_SECRET"] ?: throw NoSuchFieldException()
val SUPABASE_BASEURL = env["SUPABASE_BASEURL"] ?: throw NoSuchFieldException()
val SUPABASE_KEY = env["SUPABASE_KEY"] ?: throw NoSuchFieldException()

val supabaseRaw = createSupabaseClient(SUPABASE_BASEURL, SUPABASE_KEY) {
  install(Auth) {
    // Set these because we do not use this Supabase Client's sessions
    autoSaveToStorage = false
    autoLoadFromStorage = false
    alwaysAutoRefresh = false // probably needed as well
  }
}

val supabase = SupabaseAuth(supabaseRaw)

//val supabaseContextKey = RequestKey.required<SupabaseClient>("supabase")
//fun addSupabaseToContext(supabaseContextKey: RequestLens<SupabaseClient>) = Filter { next ->
//  {
//    next(it.with(supabaseContextKey of supabase))
//  }
//}


fun main() {
  val baseFilters : Filter = ServerFilters.CatchAll(::handleException)
    .then(customErrorPagesFilter)
    .then(DebuggingFilters.PrintRequestAndResponse())
    .then(ServerFilters.MicrometerMetrics.RequestCounter(registry))
    .then(ServerFilters.MicrometerMetrics.RequestTimer(registry))

  val staticApp : HttpHandler = baseFilters
    .then(staticAssetsRouter)

  val unauthenticatedApp : HttpHandler = baseFilters
    .then(unauthenticatedRouter)

  val authenticatedApp : HttpHandler = baseFilters
    .then(jwtFilter(jwtContextKey, userUuidContextKey))
    .then(dbFilter(dbContextKey))
//      .then(addSupabaseToContext(supabaseContextKey)) // not yet needed, for now we use a global
    .then(authenticatedRouter)

  val combinedApp: HttpHandler = { req ->
    val unauthenticatedResult = unauthenticatedApp(req)
    if (unauthenticatedResult.status == NOT_FOUND) {
      // Only try to serve a static file if the endpoint was not found in the `resourceRouter`.
      val authenticatedResult = authenticatedApp(req)
      if (authenticatedResult.status in listOf(NOT_FOUND, UNAUTHORIZED)) {
        staticApp(req)
      } else {
        authenticatedResult
      }
    } else {
      unauthenticatedResult
    }
  }

  val server = combinedApp.asServer(SunHttp(8080)).start()

  println("Server started on " + server.port())
}
