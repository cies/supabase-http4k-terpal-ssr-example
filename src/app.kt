import db.DbCtx
import filter.htmlErrorStyler
import html.template.error.handleException
import lib.jwt.JwtData
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.util.UUID
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.http4k.config.Environment
import org.http4k.config.Environment.Companion.ENV
import org.http4k.config.Environment.Companion.from
import org.http4k.config.EnvironmentKey
import org.http4k.config.Secret
import org.http4k.config.enum
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.DebuggingFilters
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestKey
import org.http4k.lens.secret
import org.http4k.lens.string
import org.http4k.lens.uri
import org.http4k.server.SunHttp
import org.http4k.server.asServer


private val log = KotlinLogging.logger {}

enum class AppMode { DEV, PROD, TEST }

val appMode = EnvironmentKey.enum<AppMode>(caseSensitive = false).required("APP_MODE")
val supabaseBaseUrl = EnvironmentKey.uri().required("SUPABASE_BASEURL")
val supabaseJwtSecret = EnvironmentKey.secret().required("SUPABASE_JWT_SECRET")
val supabaseServiceRoleKey = EnvironmentKey.string().required("SUPABASE_SERVICE_ROLE_KEY") // make Secret (but then use only once)
val supabasePostgresUrl = EnvironmentKey.uri().required("SUPABASE_POSTGRES_URL")
val supabasePostgresUsername = EnvironmentKey.string().required("SUPABASE_POSTGRES_USERNAME")
val supabasePostgresPassword = EnvironmentKey.secret().required("SUPABASE_POSTGRES_PASSWORD")
private val defaultConfig = Environment.defaults(
  appMode of AppMode.PROD,
  supabaseBaseUrl of Uri.of("http://localhost:8080"),
  supabasePostgresUrl of Uri.of("postgresql://127.0.0.1:54322/postgres"),
  supabasePostgresUsername of "postgres",
  supabasePostgresPassword of Secret("postgres"),
)
val env = from(File(".env")) overrides ENV overrides defaultConfig

val jwtContextKey = RequestKey.required<JwtData>("jwt")
val userUuidContextKey = RequestKey.required<UUID>("userUuid")
val dbContextKey = RequestKey.required<DbCtx>("db")
val authedQueryCacheContextKey = RequestKey.required<String>("authedQueryCache")


private val json = Json // Our main JSON serialization

val strictJsonDecoder = json // quite strict by default

@OptIn(ExperimentalSerializationApi::class)
val lenientJsonParser = Json {
  ignoreUnknownKeys = true
  isLenient = true
  coerceInputValues = true
  decodeEnumsCaseInsensitive = true
  allowTrailingComma = true
  allowComments = true
  allowSpecialFloatingPointValues = true
}

val minimalizingJsonEncoder = json // quite minimalizing by default

@OptIn(ExperimentalSerializationApi::class)
val prettyJsonEncoder = Json {
  encodeDefaults = true
  explicitNulls = true
  prettyPrint = true
  prettyPrintIndent = "  "
}


/**
 * This contains the http4k `Filter` stack that's the same for all request.
 * Some `Filter`s are only applied within some routes, see the [mainRouter].
 */
val app: HttpHandler = ServerFilters.CatchAll(::handleException)
  .then(htmlErrorStyler)
  .then(DebuggingFilters.PrintRequestAndResponse())
  .then(mainRouter)

fun main() {
  log.info { "Starting server" }
  val server = app.asServer(SunHttp(8080)).start()
  log.info { "Server started on port " + server.port() }
}

