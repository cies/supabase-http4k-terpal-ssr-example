package com.example.html.signin

import com.example.Paths
import com.example.html.subtemplate.validationErrorBlock
import com.example.html.subtemplate.validationInput
import com.example.html.supertemplate.htmlPage
import com.example.html.supertemplate.notSignedIn
import com.example.html.toNameString
import io.konform.validation.ValidationResult
import kotlinx.html.*
import org.http4k.core.Response

fun signInPage(formContent: SignInForm, validatedForm: ValidationResult<SignInForm>): Response {
  return htmlPage {
    notSignedIn("Sign up") {
      h1 {
        +"Sign in"
      }
      validationErrorBlock(validatedForm)
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
          +"Register"
        }
      }
    }
  }
}

