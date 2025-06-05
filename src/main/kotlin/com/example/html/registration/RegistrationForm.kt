package com.example.html.registration

import io.konform.validation.Validation
import io.konform.validation.ValidationResult
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.pattern

data class RegistrationForm(val email: String?, val password: String?) {

  companion object {
    fun empty() = RegistrationForm(null, null)
  }

  fun validate(): ValidationResult<RegistrationForm> {
    return Validation {
      RegistrationForm::email required {
        pattern(".+@.+\\..+") hint "Please provide a valid email address"
      }
      RegistrationForm::password required {
        minLength(8) hint "Password must be at least 8 characters long"
        pattern(".+[a-z]+.+") hint "Password must contain at least one lowercase letter"
        pattern(".+[A-Z]+.+") hint "Password must contain at least one uppercase letter"
        pattern(".+[0-9]+.+") hint "Password must contain at least one digit"
      }
    }(this)
  }
}

