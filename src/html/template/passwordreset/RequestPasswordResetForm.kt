package html.template.passwordreset

import io.konform.validation.Validation
import io.konform.validation.ValidationResult
import io.konform.validation.constraints.pattern
import kotlinx.serialization.Serializable


@Serializable
data class RequestPasswordResetForm(val email: String?) {

  companion object {
    fun empty() = RequestPasswordResetForm(null)
  }

  fun validate(): ValidationResult<RequestPasswordResetForm> {
    return Validation {
      RequestPasswordResetForm::email required {
        hint = "Email address is required."
        pattern(".+@.+\\..+") hint "Please provide a valid email address."
      }
    }(this)
  }
}
