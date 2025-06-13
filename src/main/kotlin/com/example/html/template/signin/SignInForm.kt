package com.example.html.template.signin

import io.konform.validation.Validation
import io.konform.validation.ValidationResult
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.pattern
import kotlinx.serialization.Serializable


@Serializable
data class SignInForm(
  val email: String?,
  val password: String?,
  val rememberMe: Boolean = true,
  val target: String? = null
) {

  companion object {
    fun empty() = SignInForm(null, null)
  }

  fun validate(): ValidationResult<SignInForm> {
    return Validation {
      SignInForm::email required {
        pattern(".+@.+\\..+") hint "Please provide a valid email address"
      }
      SignInForm::password required {
        minLength(8) hint "Password must be at least 8 characters long"
        pattern(".+[a-z]+.+") hint "Password must contain at least one lowercase letter"
        pattern(".+[A-Z]+.+") hint "Password must contain at least one uppercase letter"
        pattern(".+[0-9]+.+") hint "Password must contain at least one digit"
      }
    }(this)
  }
}
