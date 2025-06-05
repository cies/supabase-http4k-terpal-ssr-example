package com.example.lib.supabase

import com.example.SUPABASE_BASEURL
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.Status.Companion.OK
import org.http4k.format.KotlinxSerialization.auto


@Serializable
data class SupabaseTokens(
  @SerialName("access_token") val accessToken: String,
  @SerialName("token_type") val tokenType: String,
  @SerialName("expires_in") val expiresIn: Int,
  @SerialName("refresh_token") val refreshToken: String,
  val user: JsonElement
)
val tokenResponseLens = Body.auto<SupabaseTokens>().toLens() // only need one

val client = OkHttp() // only need one

fun fetchSupabaseTokens(email: String, password: String): Result<SupabaseTokens, Pair<Status, String>> {
  val url = "$SUPABASE_BASEURL/auth/v1/token?grant_type=password"
  val jsonBody = Json.encodeToString(mapOf("email" to email, "password" to password))

  val request = Request(POST, url)
    .header("Content-Type", "application/json")
    .body(jsonBody)

  val response = client(request)

  if (response.status == OK) return Success(tokenResponseLens(response))

  return Failure(response.status to response.bodyString())
}

class SupabaseAuth(val supabaseClient: SupabaseClient) {

  fun signUpWithEmail(emailAddress: String, plainPassword: String): Result<UserInfo?, SignUpError> {
    var user: UserInfo? = null
    try {
      runBlocking {
        user = supabaseClient.auth.signUpWith(Email) {
          email = emailAddress
          password = plainPassword
        }
      }
    } catch (e: AuthRestException) {
      if (e.message?.contains("user_already_exists") == true) return Failure(SignUpError.ALREADY_EXISTS)
      return Failure(SignUpError.UNKNOWN_ERROR)
    }
    return Success(user)
  }
}

enum class SignUpError {
  ALREADY_EXISTS,
  UNKNOWN_ERROR
}
