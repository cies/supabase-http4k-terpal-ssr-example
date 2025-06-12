package com.example.lib.jwt

import com.example.SUPABASE_JWT_SECRET
import com.example.moshi
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.adapter
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.oshai.kotlinlogging.KotlinLogging
import io.konform.validation.Invalid
import io.konform.validation.Valid
import io.konform.validation.Validation
import io.konform.validation.ValidationResult
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


private val log = KotlinLogging.logger {}

@OptIn(ExperimentalStdlibApi::class)
private val jwtPayloadAdapter = moshi.adapter<JwtDataLenient>()

private val jwtHs256Verifier: Mac = Mac.getInstance("HmacSHA256").apply {
  init(SecretKeySpec(SUPABASE_JWT_SECRET.toByteArray(), "HmacSHA256"))
}


fun decodeAndVerifySupabaseJwt(jwtAccessToken: String): Result<JwtData, JwtError> {
  val parts = jwtAccessToken.split('.')
  if (parts.size != 3) {
    log.warn { "JWT must have 3 parts, got '$jwtAccessToken'" }
    return Failure(JwtError.Malformed)
  }
  val (headerB64, payloadB64, signatureB64) = parts

  // Base64 decode header and payload (URL-safe, no padding), check if the algorithm matches
  val decoder = Base64.getUrlDecoder()
  val headerJson = decoder.decode(headerB64).toString(StandardCharsets.UTF_8)
  // headerJson looks like `{ "alg": "HS256", "typ": "JWT" }`; no need to parse it (this is quick)
  if (!headerJson.contains("HS256", ignoreCase = true)) {
    log.warn { "Only HS256 is supported when for signing our JWTs, got '$headerJson'" }
    return Failure(JwtError.UnsupportedAlgorithmOrInvalidSignature)
  }
  val payloadJson = decoder.decode(payloadB64).toString(StandardCharsets.UTF_8)
  val signatureBytes = decoder.decode(signatureB64)

  // Verify the signature
  val data = "${headerB64}.${payloadB64}".toByteArray()
  val expectedSignature = jwtHs256Verifier.doFinal(data)
  if (!expectedSignature.contentEquals(signatureBytes)) {
    log.warn { "Could not verify a JWT based on it's signature, got '$jwtAccessToken'" }
    return Failure(JwtError.PayloadDoesNotMatchSignature)
  }

  // Check the payload (more checks --like validation-- may follow)
  val jwtDataLenient: JwtDataLenient =
    jwtPayloadAdapter.fromJson(payloadJson) ?: return Failure(JwtError.UnparsablePayload)
  return when (jwtDataLenient.validate()) {
    is Invalid -> {
      log.warn { "Invalid JWT payload structure, got '$payloadJson'" }
      Failure(JwtError.InvalidPayloadStructure)
    }

    is Valid -> {
      if (Instant.now().epochSecond > (jwtDataLenient.expiresAt ?: 0)) {
        Failure(JwtError.Expired)
      } else {
        try {
          Success(JwtData.from(jwtDataLenient))
        } catch (e: Exception) {
          log.warn(e) { "Could not convert JWT payload to internal, got '$jwtDataLenient'" }
          Failure(JwtError.Unknown)
        }
      }
    }
  }
}


// Example of a JWT payload
// {
//   "iss": "http://127.0.0.1:54321/auth/v1",
//   "sub": "06555eef-9b08-41b2-90ee-747e07b6b1f6",
//   "aud": "authenticated",
//   "exp": 1750060567,
//   "iat": 1749700567,
//   "email": "w@kde.nl",
//   "phone": "",
//   "app_metadata": {
//     "provider": "email",
//     "providers": [
//       "email"
//     ]
//   },
//   "user_metadata": {
//     "email": "w@kde.nl",
//     "email_verified": true,
//     "phone_verified": false,
//     "sub": "06555eef-9b08-41b2-90ee-747e07b6b1f6"
//   },
//   "role": "authenticated",
//   "aal": "aal1",
//   "amr": [
//     {
//       "method": "otp",
//       "timestamp": 1749696430
//     }
//   ],
//   "session_id": "74b48395-ba09-49f7-8841-df8c4e7ff030",
//   "is_anonymous": false
// }

@JsonClass(generateAdapter = true)
data class JwtData(
  val userUuid: UUID?,
  val userEmail: String?,
  val userRole: String,
  val audience: String,
  val issuer: String,
  val issuedAt: Instant,
  val expiresAt: Instant,
  val sessionId: UUID,
  val isAnonymous: Boolean
) {
  companion object {
    fun from(jwtDataLenient: JwtDataLenient): JwtData {
      return JwtData(
        jwtDataLenient.userUuid?.let { UUID.fromString(it) },
        jwtDataLenient.userEmail,
        jwtDataLenient.userRole!!,
        jwtDataLenient.audience!!,
        jwtDataLenient.issuer!!,
        Instant.ofEpochSecond(jwtDataLenient.issuedAt!!),
        Instant.ofEpochSecond(jwtDataLenient.expiresAt!!),
        UUID.fromString(jwtDataLenient.sessionId!!),
        jwtDataLenient.isAnonymous!!,
      )
    }
  }
}

enum class JwtError {
  UnsupportedAlgorithmOrInvalidSignature,
  UnparsablePayload,
  Expired,
  Malformed,
  PayloadDoesNotMatchSignature,
  InvalidPayloadStructure,
  Unknown
}

@JsonClass(generateAdapter = true)
data class JwtDataLenient(
  @Json(name = "sub")
  val userUuid: String?,

  @Json(name = "email")
  val userEmail: String?,

  @Json(name = "role")
  val userRole: String?,

  @Json(name = "aud")
  val audience: String?,

  @Json(name = "iss")
  val issuer: String?,

  @Json(name = "iat")
  val issuedAt: Long?,

  @Json(name = "exp")
  val expiresAt: Long?,

  @Json(name = "session_id")
  val sessionId: String?,

  @Json(name = "is_anonymous")
  val isAnonymous: Boolean?
) {
  fun validate(): ValidationResult<JwtDataLenient> {
    return Validation {
      JwtDataLenient::userRole required {}
      JwtDataLenient::audience required {}
      JwtDataLenient::issuer required {}
      JwtDataLenient::issuedAt required {}
      JwtDataLenient::expiresAt required {}
      JwtDataLenient::sessionId required {}
      JwtDataLenient::isAnonymous required {}
    }(this)
  }
}
