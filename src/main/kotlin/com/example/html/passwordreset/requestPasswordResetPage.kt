package com.example.html.passwordreset

import com.example.Paths
import com.example.html.signin.SignInForm
import com.example.html.subtemplate.validationErrorBlock
import com.example.html.subtemplate.validationInput
import com.example.html.supertemplate.htmlPage
import com.example.html.supertemplate.notSignedIn
import com.example.html.toNameString
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
      validationErrorBlock(validatedForm)
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
