package com.example.html.registration

import com.example.Paths
import com.example.html.hasValidationError
import com.example.html.supertemplate.htmlPage
import com.example.html.supertemplate.notSignedIn
import com.example.html.toNameString
import com.natpryce.krouton.path
import io.konform.validation.ValidationResult
import kotlinx.html.*
import org.http4k.core.Response

fun registrationPage(formContent: RegistrationForm, validatedForm: ValidationResult<RegistrationForm>): Response {
  return htmlPage {
    notSignedIn("Sign up") {
      h1 {
        +"Register an account"
      }
      p {
        +"Fill in your email address and the password you like to use for this service."
      }
      p {
        +"After submitting the form we will send a verification email to the address to ensure it belongs to you."
      }
      if (!validatedForm.isValid) {
        div("errors") {
          validatedForm.errors.forEach { p("error") { +it.message } }
        }
      }
      form(Paths.register.path(), method = FormMethod.post) {
        p {
          label {
            +"Email address"
            validationInput(toNameString(RegistrationForm::email), InputType.email, validatedForm) {
              value = formContent.email ?: ""
            }
          }
        }
        p {
          label {
            +"Password"
            validationInput(toNameString(RegistrationForm::password), InputType.password, validatedForm) {
              value = "" // Do not put a password in the response
            }
          }
        }
        button(type = ButtonType.submit) {
          +"Register"
        }
      }
    }
  }
}

@HtmlTagMarker
inline fun FlowOrInteractiveOrPhrasingContent.validationInput(
  nameString: String,
  type: InputType? = null,
  validationResult: ValidationResult<*>,
  classes: String = "",
  value: String? = null,
  isDisabled: Boolean = false,
  isReadOnly: Boolean = false,
  crossinline block: INPUT.() -> Unit = {}
) {
  var newClasses = classes
  if (hasValidationError(validationResult, nameString)) {
    newClasses = "$newClasses invalid".trim()
  }
  input(type, name = nameString, classes = newClasses) {
    value?.let { this.value = it }
    disabled = isDisabled
    readonly = isReadOnly
    block()
  }
}

