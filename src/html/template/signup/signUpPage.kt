package html.template.signup

import Paths
import html.component.labelledValidationInput
import html.component.validationErrorBox
import html.layout.htmlPage
import html.layout.authenticationLayout
import html.toNameString
import io.konform.validation.ValidationResult
import kotlinx.html.*
import org.http4k.core.Response

fun signUpPage(formContent: SignUpForm, validatedForm: ValidationResult<SignUpForm>): Response {
  return htmlPage {
    authenticationLayout("Sign up") {

      // Header
      div("flex-1") {
        div("flex items-center justify-between h-16 px-4 sm:px-6 lg:px-8") {
          // Logo
          a(classes = "block") {
            href = "{{ route('dashboard') }}"
            // logo svg
          }
        }
      }

      // Left side
      div("max-w-sm mx-auto w-full px-4 py-8") {
        h1("text-3xl text-gray-800 dark:text-gray-100 font-bold mb-4") {
          +"Sign-up for an account"
        }
        div("text-sm text-gray-500 dark:text-gray-300") {
          p("mb-2") {
            +"Fill in the email address and the password you like to use for this service."
          }
          p("mb-5") {
            +"After submitting the form we will send a verification email to that address to ensure it belongs to you."
          }
        }

        // Form

        validationErrorBox(validatedForm)

        form(Paths.signUp.pathSegment(), method = FormMethod.post) {
          novalidate = true

          // +csrfToken()

          div("space-y-4") {
            labelledValidationInput(
              "Email",
              toNameString(SignUpForm::email),
              InputType.email,
              validatedForm,
              formContent.email ?: ""
            ) {
              attributes["autofocus"] = "true"
            }
            labelledValidationInput(
              "Password",
              toNameString(SignUpForm::password),
              InputType.password,
              validatedForm,
              "" // Do not put a password in the response
            ) {
              attributes["autocomplete"] = "current-password"
            }
          }
          div("text-right mt-6") {
            button(classes = "btn bg-gray-900 text-gray-100 hover:bg-gray-800 dark:bg-gray-100 dark:text-gray-800 dark:hover:bg-white cursor-pointer whitespace-nowrap ml-3") {
              type = ButtonType.submit
              +"Sign me up"
            }
          }
        }
        // Footer
        div("pt-5 mt-6 border-t border-gray-100 dark:border-gray-700/60") {
          div("text-sm") {
            +"Do you already have an account? "
            a(classes = "font-medium text-violet-500 hover:text-violet-600 dark:hover:text-violet-400") {
              href = Paths.signIn.path()
              +"Sign in"
            }
            +"."
          }
        }
      }
    }
  }
}
