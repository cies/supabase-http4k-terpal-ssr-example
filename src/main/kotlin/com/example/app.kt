package com.example

import com.auth0.jwt.interfaces.DecodedJWT
import com.example.filter.DbCtx
import com.example.filter.htmlErrorStyler
import com.example.html.template.error.handleException
import com.squareup.moshi.Moshi
import io.github.cdimascio.dotenv.dotenv
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.util.UUID
import org.http4k.core.HttpHandler
import org.http4k.core.then
import org.http4k.filter.DebuggingFilters
import org.http4k.filter.MicrometerMetrics
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestKey
import org.http4k.server.SunHttp
import org.http4k.server.asServer


val env = dotenv()
val SUPABASE_JWT_SECRET = env["SUPABASE_JWT_SECRET"] ?: throw NoSuchFieldException()
val SUPABASE_BASEURL = env["SUPABASE_BASEURL"] ?: throw NoSuchFieldException()
val SUPABASE_SERVICE_ROLE_KEY = env["SUPABASE_SERVICE_ROLE_KEY"] ?: throw NoSuchFieldException()
val SUPABASE_POSTGRES_URL = env["SUPABASE_POSTGRES_URL"] ?: throw NoSuchFieldException()
val SUPABASE_POSTGRES_USERNAME = env["SUPABASE_POSTGRES_USERNAME"] ?: throw NoSuchFieldException()
val SUPABASE_POSTGRES_PASSWORD = env["SUPABASE_POSTGRES_PASSWORD"] ?: throw NoSuchFieldException()

val APP_BASE_URL = env["APP_BASE_URL"] ?: "http://localhost:8080"

val jwtContextKey = RequestKey.required<DecodedJWT>("jwt")
val userUuidContextKey = RequestKey.required<UUID>("userUuid")
val dbContextKey = RequestKey.required<DbCtx>("db")
val authedQueryCacheContextKey = RequestKey.required<String>("authedQueryCache")

val moshi = Moshi.Builder().build()

//@KotshiJsonAdapterFactory
//private object ExampleJsonAdapterFactory : JsonAdapter.Factory by
//    KotshiExampleJsonAdapterFactory // this class will be generated during compile
//
//val moshi = ConfigurableMoshi(
//  Moshi.Builder()
//    .add(ExampleJsonAdapterFactory) // inject kotshi here
//    .addLast(EventAdapter)
//    .addLast(ThrowableAdapter)
//    .addLast(ListAdapter)
//    .addLast(MapAdapter)
//    .asConfigurable()
//    .withStandardMappings()
//    .done()
//)

/** A micrometer registry used mostly for testing - substitute the correct implementation. */
val registry = SimpleMeterRegistry()

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
  println("Server started on port " + server.port())
}

