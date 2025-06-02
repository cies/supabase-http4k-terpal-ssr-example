package com.example

import com.example.formats.kotlinXMessage
import com.example.formats.kotlinXMessageLens
import com.natpryce.krouton.ROOT
import com.natpryce.krouton.div
import com.natpryce.krouton.http4k.resources
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import gg.jte.generated.precompiled.StaticTemplates
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import java.time.Instant.now
import java.util.*
import kotlin.jvm.java
import kotlin.math.absoluteValue
import org.http4k.client.JavaHttpClient
import org.http4k.core.Credentials
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.DebuggingFilters
import org.http4k.filter.MicrometerMetrics
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestKey
import org.http4k.lens.RequestLens
import org.http4k.security.InsecureCookieBasedOAuthPersistence
import org.http4k.security.OAuthProvider
import org.http4k.security.google
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.intellij.lang.annotations.Language
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.attach
import org.jetbrains.kotlin.codegen.inline.dropInlineScopeInfo

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

val templates: gg.jte.generated.precompiled.Templates = StaticTemplates()

val pingPath = ROOT / "ping"
val kotlinxJsonPath = ROOT / "formats" / "json" / "kotlinx"
val templateJtePath = ROOT / "template" / "jte"
val metricsPath = ROOT / "metrics"
val jdbiPath = ROOT / "jdbi"
val oAuthRootPath = ROOT / "oauth"
val oAuthCallbackPath = ROOT / "oauth" / "callback"


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

fun app(): HttpHandler {

  return resources {
    pingPath methods {
      GET { _ -> Response(OK).body("pong") }
    }
    kotlinxJsonPath methods {
      GET { _ -> Response(OK).with(kotlinXMessageLens of kotlinXMessage) }
    }
    templateJtePath methods {
      GET { _ -> Response(OK).body(templates.firstContent("wwohohowhoww").render()) }
    }
    metricsPath methods {
      GET { req -> Response(OK).body("Example metrics route") }
    }
    jdbiPath methods {
      GET { req ->
        val db = dbContextKey(req)
        throw Exception("Testing exception handling...")
        val dbRetrieved = db.inTransaction<String, Exception> { dbtx ->
          val orgDao = dbtx.attach(OrganizationDao::class)
          orgDao.listOrganizations().joinToString(", ") { it.name }
        }
        Response(OK).body("$dbRetrieved -- And no exceptions... must be good!")
      }
    }

    oAuthRootPath methods {
      GET to oauthProvider.authFilter.then { Response(OK).body("hello!") }
    }
    oAuthCallbackPath methods {
      GET to oauthProvider.callback
    }
  }
}


data class Organization(val id: Long, val name: String)

val dataSource = HikariDataSource(
  HikariConfig().apply {
    this.jdbcUrl = "jdbc:postgresql://localhost:54322/postgres"
    this.username = "postgres"
    this.password = "postgres"
    this.driverClassName = "org.postgresql.Driver"
    this.maximumPoolSize = 10
    this.minimumIdle = 2
    this.idleTimeout = 60000 // ms
    this.connectionTimeout = 30000 // ms
    this.maxLifetime = 1800000 // ms
    this.isAutoCommit = true // any good?
    this.poolName = "PostgresHikariPool"
    connectionInitSql = "set role 'anon'" // principle of least privilege
  }
)

var jdbi: Jdbi = Jdbi.create(dataSource)
  .installPlugin(KotlinPlugin())
  .installPlugin(KotlinSqlObjectPlugin())
  .installPlugin(PostgresPlugin())


val jwtContextKey = RequestKey.required<String>("jwt") // TODO(cies): a better JWT type is needed

/**
 * This http4k `Filter`...
 *   - Verifies the authenticity of the JWT. A bad JWT results in a Forbidden response. TODO
 *   - Adds the JWT to the request with the RequestKey mechanism.
 */
fun addJwtToContext(jwtContextKey: RequestLens<String>) = Filter { next ->
  {
    val jwt =
      """{"jwt":{"sub":"123ef123f312434","email":"cies@kde.nl","org":"123456789","iss":"qwe","iat":1698454000}}"""
    next(it.with(jwtContextKey of jwt))
  }
}

val dbContextKey = RequestKey.required<Handle>("db")

/**
 * Important http4k `Filter` which:
 *   - Initializes a JDBI Handle (`db`), and closes it on response completion (possibly by an error).
 *   - Adds the db (`Handle`) to the request with the RequestKey mechanism.
 *   - Sets Supabase compatible role and claims from JWT (that should have been added by RequestKey in a previous filter). TODO
 *   - Resets the Supabase role and claims after the requests has finished (so it can be returned safely to the connection pool).
 * It does NOT open (BEGIN) and close (COMMIT) a transaction, because:
 *   - Over use of transactions results in many locks being kept longer.
 *   - Increased rollback risk (due to db errors, or the application throwing exceptions).
 *   - Partial commits become cumbersome (polluting the code with `commitAndOpenNewTransaction()` calls).
 *   - Transactions come with overhead not needed for read only queries.
 * We prefer the programmer makes explicit transactions where they are needed (transactions can be nested),
 * instead of implicitly wrapping everything in transactions.
 */
fun addDbToContext(dbContextKey: RequestLens<Handle>) = Filter { next ->
  {
    jdbi.open().use { db ->

      // TODO(cies): from JWT
      val userUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000") // jwtContextKey(req).jwt

      db.setSupabaseAuth(
        userUuid,
        "cies@kde.nl",
        (Math.random() * 100).toInt().absoluteValue.toLong(),
        Uri.of("https://auth.example.com/auth/v2"),
        now()
      )

      val response = next(it.with(dbContextKey of db))

      db.resetSupabaseAuth()

      response
    }
  }
}

class DummyClass {
  companion object {
    val thisPackage = this::class.java.`package`.toString().drop(8)
  }
}

val stacktraceSplitter = Regex("at (.*?)(\\$.*)?\\((.*?)(:.*)?\\)") // TODO(cies): fix dollar sign matching
fun handleException(e: Throwable): Response {
  val currentPackage = DummyClass.thisPackage
  if (e !is Exception) throw e // Only handle [Exception] and subclasses (not [Throwable]).
  val stackTraceAsHtml = StringWriter().apply { e.printStackTrace(PrintWriter(this)) }.toString()
    .split("\n")
    .filter(String::isNotBlank)
    .map(String::trimStart)
    .joinToString("\n") {
      if (!it.startsWith("at")) """<tr><td></td><td></td><td><strong>$it</strong></td></tr>"""
      else stacktraceSplitter.matchEntire(it)?.groups?.let { m ->
        val fileName = m[3]?.value
        val lineNr = m[4]?.value ?: ""
        val classPath = m[1]?.value
        val lambdaStack = m[2]?.value ?: ""
        val trClass = if (classPath?.startsWith(currentPackage) == true) "applicationCode" else ""
        """
        |          <tr class='$trClass'>
        |            <td align='right'><code>$fileName</code></td>
        |            <td><code>$lineNr&nbsp;&nbsp;</code></td>
        |            <td>$classPath<span style='color:grey'>$lambdaStack</span></td>
        |          </tr>
        """.trimMargin()
      } ?: it
    }
  val html = """
    <html>
    <head>
      <title>${e.message} (500)</title>
    <head>
    <body>
      <h1>Internal Server Error <span style="font-size: 70%">(500)<span></h1>
      <h2>${e.message}</h2>
      <p>
        <table>
          $stackTraceAsHtml
        </table>
      </p>
    </body>
    </html>
  """.trimIndent()
  return Response(INTERNAL_SERVER_ERROR).body(html)
}

fun main() {
  val printingApp: HttpHandler =
    ServerFilters.CatchAll(::handleException)
      .then(DebuggingFilters.PrintRequestAndResponse())
      .then(ServerFilters.MicrometerMetrics.RequestCounter(registry))
      .then(ServerFilters.MicrometerMetrics.RequestTimer(registry))
      .then(addJwtToContext(jwtContextKey))
      .then(addDbToContext(dbContextKey))
      .then(app())

  val server = printingApp.asServer(SunHttp(8080)).start()

  println("Server started on " + server.port())
}

