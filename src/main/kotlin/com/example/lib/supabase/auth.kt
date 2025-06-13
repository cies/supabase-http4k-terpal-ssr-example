package com.example.lib.supabase

import com.example.env
import com.example.supabaseBaseUrl
import com.example.supabaseServiceRoleKey
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.format.KotlinxSerialization.auto


val client = OkHttp() // only need one

fun fetchSupabaseTokens(email: String, password: String): Result<TokenResponseSuccess, TokenResponseError> {
  val url = "${supabaseBaseUrl(env)}/auth/v1/token?grant_type=password"
  return fetchSupabaseTokens(url, mapOf("email" to email, "password" to password))
}

fun fetchSupabaseTokens(refreshToken: String): Result<TokenResponseSuccess, TokenResponseError> {
  val url = "${supabaseBaseUrl(env)}/auth/v1/token?grant_type=refresh_token"
  return fetchSupabaseTokens(url, mapOf("refresh_token" to refreshToken))
}

private fun fetchSupabaseTokens(
  url: String,
  jsonBody: Map<String, String>
): Result<TokenResponseSuccess, TokenResponseError> {
  val jsonBody = Json.encodeToString(jsonBody)
  val request = Request(POST, url)
    .header("Content-Type", "application/json")
    .body(jsonBody)
  val response = client(request)

  if (response.status == OK) return Success(tokenResponseSuccessLens(response))
  return Failure(tokenResponseErrorLens(response))
}

@Serializable
data class TokenResponseSuccess(
  @SerialName("access_token") val accessToken: String,
  @SerialName("token_type") val tokenType: String,
  @SerialName("expires_in") val expiresIn: Int,
  @SerialName("refresh_token") val refreshToken: String,
  val user: JsonElement
)

val tokenResponseSuccessLens = Body.auto<TokenResponseSuccess>().toLens() // only need one

@Serializable
data class TokenResponseError(
  @SerialName("code") val statusCode: Int,
  @SerialName("error_code") val errorCode: String, // TODO: map these to an enum (invalid_credentials, ...)
  @SerialName("msg") val message: String
)

val tokenResponseErrorLens = Body.auto<TokenResponseError>().toLens() // only need one

@Serializable
data class CreateUserRequest(
  val email: String,
  val password: String,
  @SerialName("email_confirm") val emailConfirm: Boolean = false, // `false` here triggers verification email
  val role: String = "authenticated"
)


fun signUpWithEmail(emailAddress: String, plainPassword: String): Result<UserInfo?, SignUpError> {

  val serviceRoleKey = "your-service-role-jwt"
  val redirectUrl = "https://your-site.com/verify"


  val request = Request(POST, "${supabaseBaseUrl(env)}/auth/v1/admin/users?redirect_to=$redirectUrl")
    .header("Authorization", "Bearer ${supabaseServiceRoleKey(env)}")
    .header("Content-Type", "application/json")
    .body(Json.encodeToString(CreateUserRequest(emailAddress, plainPassword)))
  val response = client(request)

  return when (response.status) {
    OK -> Success(userInfoLens(response))
    UNAUTHORIZED -> Failure(SignUpError.UnknownError)
    else -> Failure(SignUpError.UnknownError)
  }
}

enum class SignUpError {
  AlreadyExists,
  UnknownError
}


val userInfoLens = Body.auto<UserInfo>().toLens() // only need one

// The following are copied from the `supabase-kt` library that was removed from the project due to dependency load.

@Serializable
data class UserInfo(
  @SerialName("app_metadata")
  val appMetadata: JsonObject? = null,
  @SerialName("aud")
  val aud: String,
  @SerialName("confirmation_sent_at")
  val confirmationSentAt: Instant? = null,
  @SerialName("confirmed_at")
  val confirmedAt: Instant? = null,
  @SerialName("created_at")
  val createdAt: Instant? = null,
  @SerialName("email")
  val email: String? = null,
  @SerialName("email_confirmed_at")
  val emailConfirmedAt: Instant? = null,
  val factors: List<UserMfaFactor> = listOf(),
  @SerialName("id")
  val id: String,
  @SerialName("identities")
  val identities: List<Identity>? = null,
  @SerialName("last_sign_in_at")
  val lastSignInAt: Instant? = null,
  @SerialName("phone")
  val phone: String? = null,
  @SerialName("role")
  val role: String? = null,
  @SerialName("updated_at")
  val updatedAt: Instant? = null,
  @SerialName("user_metadata")
  val userMetadata: JsonObject? = null,
  @SerialName("phone_change_sent_at")
  val phoneChangeSentAt: Instant? = null,
  @SerialName("new_phone")
  val newPhone: String? = null,
  @SerialName("email_change_sent_at")
  val emailChangeSentAt: Instant? = null,
  @SerialName("new_email")
  val newEmail: String? = null,
  @SerialName("invited_at")
  val invitedAt: Instant? = null,
  @SerialName("recovery_sent_at")
  val recoverySentAt: Instant? = null,
  @SerialName("phone_confirmed_at")
  val phoneConfirmedAt: Instant? = null,
  @SerialName("action_link")
  val actionLink: String? = null,
)

@Serializable
data class UserMfaFactor(
  val id: String,
  @SerialName("created_at")
  val createdAt: Instant,
  @SerialName("updated_at")
  val updatedAt: Instant,
  private val status: String,
  @SerialName("friendly_name")
  val friendlyName: String? = null,
  @SerialName("factor_type")
  val factorType: String
) {
  val isVerified: Boolean
    get() = status == "verified"
}

@Serializable
data class Identity(
  @SerialName("id")
  val id: String,
  @SerialName("identity_data")
  val identityData: JsonObject,
  @SerialName("identity_id")
  val identityId: String? = null,
  @SerialName("last_sign_in_at")
  val lastSignInAt: String? = null,
  @SerialName("updated_at")
  val updatedAt: String? = null,
  @SerialName("created_at")
  val createdAt: String? = null,
  @SerialName("provider")
  val provider: String,
  @SerialName("user_id")
  val userId: String
)
