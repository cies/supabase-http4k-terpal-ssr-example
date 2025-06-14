package html.template.signup

import Paths
import html.block.validationErrorBox
import html.block.validationInput
import html.layout.htmlPage
import html.layout.notSignedIn
import html.toNameString
import io.konform.validation.ValidationResult
import kotlinx.html.*
import org.http4k.core.Response

fun signUpPage(formContent: SignUpForm, validatedForm: ValidationResult<SignUpForm>): Response {
  return htmlPage {
    notSignedIn("Sign up") {
      h1 {
        +"Sign-up for an account"
      }
      p {
        +"Fill in your email address and the password you like to use for this service."
      }
      p {
        +"After submitting the form we will send a verification email to the address to ensure it belongs to you."
      }
      validationErrorBox(validatedForm)
      form(Paths.signUp.pathSegment(), method = FormMethod.post) {
        p {
          label {
            +"Email address"
            validationInput(toNameString(SignUpForm::email), InputType.email, validatedForm) {
              value = formContent.email ?: ""
            }
          }
        }
        p {
          label {
            +"Password"
            validationInput(toNameString(SignUpForm::password), InputType.password, validatedForm) {
              value = "" // Do not put a password in the response
            }
          }
        }
        button(type = ButtonType.submit) {
          +"Sign me up"
        }
      }
    }
  }
}
