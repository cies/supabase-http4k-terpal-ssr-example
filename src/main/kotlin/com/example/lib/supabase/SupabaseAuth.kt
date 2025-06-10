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
import org.http4k.core.Status.Companion.OK
import org.http4k.format.KotlinxSerialization.auto


val client = OkHttp() // only need one

fun fetchSupabaseTokens(email: String, password: String): Result<SupabaseTokens, TokenResponseError> {
  val url = "$SUPABASE_BASEURL/auth/v1/token?grant_type=password"
  return fetchSupabaseTokens(url, mapOf("email" to email, "password" to password))
}

fun fetchSupabaseTokens(refreshToken: String): Result<SupabaseTokens, TokenResponseError> {
  val url = "$SUPABASE_BASEURL/auth/v1/token?grant_type=refresh_token"
  return fetchSupabaseTokens(url, mapOf("refresh_token" to refreshToken))
}

private fun fetchSupabaseTokens(
  url: String,
  jsonBody: Map<String, String>
): Result<SupabaseTokens, TokenResponseError> {
  val jsonBody = Json.encodeToString(jsonBody)
  val request = Request(POST, url)
    .header("Content-Type", "application/json")
    .body(jsonBody)
  val response = client(request)

  if (response.status == OK) return Success(tokenResponseSuccessLens(response))
  return Failure(tokenResponseErrorLens(response))
}

@Serializable
data class SupabaseTokens(
  @SerialName("access_token") val accessToken: String,
  @SerialName("token_type") val tokenType: String,
  @SerialName("expires_in") val expiresIn: Int,
  @SerialName("refresh_token") val refreshToken: String,
  val user: JsonElement
)
val tokenResponseSuccessLens = Body.auto<SupabaseTokens>().toLens() // only need one

@Serializable
data class TokenResponseError(
  @SerialName("code") val statusCode: Int,
  @SerialName("error_code") val errorCode: String, // TODO: map these to an enum (invalid_credentials, ...)
  @SerialName("msg") val message: String
)
val tokenResponseErrorLens = Body.auto<TokenResponseError>().toLens() // only need one

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
