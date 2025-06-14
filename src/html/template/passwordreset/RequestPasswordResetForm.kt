package html.template.passwordreset

import io.konform.validation.Validation
import io.konform.validation.ValidationResult
import io.konform.validation.constraints.pattern

data class RequestPasswordResetForm(val email: String?) {

  companion object {
    fun empty() = RequestPasswordResetForm(null)
  }

  fun validate(): ValidationResult<RequestPasswordResetForm> {
    return Validation {
      RequestPasswordResetForm::email required {
        pattern(".+@.+\\..+") hint "Please provide a valid email address"
      }
    }(this)
  }
}
