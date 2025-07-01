package html.template.signin

import Paths
import html.component.labelledValidationInput
import html.component.validationErrorBox
import html.layout.htmlPage
import html.layout.authenticationLayout
import html.toNameString
import io.konform.validation.ValidationResult
import kotlinx.html.*
import org.http4k.core.Response

fun signInPage(formContent: SignInForm, validatedForm: ValidationResult<SignInForm>): Response {
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
        h1("text-3xl text-gray-800 dark:text-gray-100 font-bold mb-6") {
          +"Welcome back!"
        }
        // Form

        validationErrorBox(validatedForm)

        form(Paths.signIn.pathSegment(), method = FormMethod.post) {
          novalidate = true

          // +csrfToken()

          input(InputType.hidden) {
            name = toNameString(SignInForm::target)
            value = formContent.target ?: ""
          }

          div("space-y-4") {
            labelledValidationInput(
              "Email",
              toNameString(SignInForm::email),
              InputType.email,
              validatedForm
            ) {
              attributes["autofocus"] = "true"
            }
            labelledValidationInput(
              "Password",
              toNameString(SignInForm::password),
              InputType.password,
              validatedForm,
              "" // Do not put a password in the response
            ) {
              attributes["autocomplete"] = "current-password"
            }
          }
          div("flex items-center justify-between mt-6") {
            div("mr-1") {
              a(classes = "text-sm underline hover:no-underline") {
                href = Paths.requestPasswordReset.path()
                +"Forgot password"
              }
            }
            button(classes = "btn bg-gray-900 text-gray-100 hover:bg-gray-800 dark:bg-gray-100 dark:text-gray-800 dark:hover:bg-white cursor-pointer whitespace-nowrap ml-3") {
              type = ButtonType.submit
              +"Sign in"
            }
          }
        }
        // Footer
        div("pt-5 mt-6 border-t border-gray-100 dark:border-gray-700/60") {
          div("text-sm") {
            +"Don't you have an account? "
            a(classes = "font-medium text-violet-500 hover:text-violet-600 dark:hover:text-violet-400") {
              href = Paths.signUp.path()
              +"Sign up"
            }
            +"."
          }
        }
      }
    }
  }
}

