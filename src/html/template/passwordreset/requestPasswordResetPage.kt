package html.template.passwordreset

import Paths
import html.template.signin.SignInForm
import html.block.validationErrorBox
import html.block.validationInput
import html.layout.htmlPage
import html.layout.notSignedIn
import html.toNameString
import io.konform.validation.ValidationResult
import kotlinx.html.*
import org.http4k.core.Response

fun requestPasswordResetPage(formContent: RequestPasswordResetForm, validatedForm: ValidationResult<RequestPasswordResetForm>): Response {
  return htmlPage {
    notSignedIn("Reset password") {
      h1 {
        +"Reset password"
      }
      p {
        +"With the following form you can request a password reset link."
      }
      p {
        +"The link will be send by email and is only valid for a 30 minutes."
        +"It will take you to a form by which you can set a new password for your account."
      }
      validationErrorBox(validatedForm)
      form(Paths.requestPasswordReset.path(), method = FormMethod.post) {
        p {
          label {
            +"Email address"
            validationInput(toNameString(SignInForm::email), InputType.email, validatedForm) {
              value = formContent.email ?: ""
            }
          }
        }
        button(type = ButtonType.submit) {
          +"Reset password"
        }
      }
    }
  }
}
