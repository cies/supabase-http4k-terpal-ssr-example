package com.example.html.template.signin

import com.example.Paths
import com.example.html.block.validationErrorBox
import com.example.html.block.validationInput
import com.example.html.layout.htmlPage
import com.example.html.layout.notSignedIn
import com.example.html.toNameString
import io.konform.validation.ValidationResult
import kotlinx.html.*
import org.http4k.core.Response

fun signInPage(formContent: SignInForm, validatedForm: ValidationResult<SignInForm>): Response {
  return htmlPage {
    notSignedIn("Sign in") {
      h1 {
        +"Sign in to the portal"
      }
      validationErrorBox(validatedForm)
      form(Paths.signIn.path(), method = FormMethod.post) {
        input(InputType.hidden) {
          name = toNameString(SignInForm::target)
          value = formContent.target ?: ""
        }
        p {
          label {
            +"Email address"
            validationInput(toNameString(SignInForm::email), InputType.email, validatedForm) {
              value = formContent.email ?: ""
            }
          }
        }
        p {
          label {
            +"Password"
            validationInput(toNameString(SignInForm::password), InputType.password, validatedForm) {
              value = "" // Do not put a password in the response
            }
          }
        }
        p {
          label {
            input(InputType.checkBox) {
              name = toNameString(SignInForm::rememberMe)
              value = formContent.rememberMe.toString()
            }
            +"Remember me"
          }
        }
        button(type = ButtonType.submit) {
          +"Sign in"
        }
      }
      a {
        href = Paths.requestPasswordReset.absolutePath()
        +"Forgot password"
      }
    }
  }
}

