package com.example

import com.auth0.jwt.interfaces.DecodedJWT
import com.example.filter.htmlErrorStyler
import com.example.html.error.handleException
import com.example.lib.supabase.SupabaseAuth
import io.github.cdimascio.dotenv.dotenv
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.util.*
import org.http4k.core.HttpHandler
import org.http4k.core.then
import org.http4k.filter.DebuggingFilters
import org.http4k.filter.MicrometerMetrics
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestKey
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.jdbi.v3.core.Handle

// this is a micrometer registry used mostly for testing - substitute the correct implementation.
val registry = SimpleMeterRegistry()


val jwtContextKey = RequestKey.required<DecodedJWT>("jwt")
val userUuidContextKey = RequestKey.required<UUID>("userUuid")
val dbContextKey = RequestKey.required<Handle>("db")
val renderedSetSupabaseAuthenticatedUserQueryContextKey = RequestKey.required<Handle>("user_query")

val env = dotenv()
val SUPABASE_JWT_SECRET = env["SUPABASE_JWT_SECRET"] ?: throw NoSuchFieldException()
val SUPABASE_BASEURL = env["SUPABASE_BASEURL"] ?: throw NoSuchFieldException()
val SUPABASE_SERVICE_ROLE_KEY = env["SUPABASE_SERVICE_ROLE_KEY"] ?: throw NoSuchFieldException()
val SUPABASE_POSTGRES_URL = env["SUPABASE_POSTGRES_URL"] ?: throw NoSuchFieldException()
val SUPABASE_POSTGRES_USERNAME = env["SUPABASE_POSTGRES_USERNAME"] ?: throw NoSuchFieldException()
val SUPABASE_POSTGRES_PASSWORD = env["SUPABASE_POSTGRES_PASSWORD"] ?: throw NoSuchFieldException()

val supabaseRaw = createSupabaseClient(SUPABASE_BASEURL, SUPABASE_SERVICE_ROLE_KEY) {
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


/**
 * This contains the http4k `Filter` stack that's the same for all request.
 * Some `Filter`s are only applied within some routes, see the [mainRouter].
 */
val app: HttpHandler = ServerFilters.CatchAll(::handleException)
  .then(htmlErrorStyler)
  .then(DebuggingFilters.PrintRequestAndResponse())
  .then(ServerFilters.MicrometerMetrics.RequestCounter(registry))
  .then(ServerFilters.MicrometerMetrics.RequestTimer(registry))
  .then(mainRouter)

fun main() {
  val server = app.asServer(SunHttp(8080)).start()
  println("Server started on " + server.port())
}

