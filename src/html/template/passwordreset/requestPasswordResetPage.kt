package html.template.passwordreset

import Paths
import html.component.labelledValidationInput
import html.component.validationErrorBox
import html.layout.htmlPage
import html.layout.authenticationLayout
import html.toNameString
import io.konform.validation.ValidationResult
import kotlinx.html.*
import org.http4k.core.Response

fun requestPasswordResetPage(formContent: RequestPasswordResetForm, validatedForm: ValidationResult<RequestPasswordResetForm>): Response {
  return htmlPage {
    authenticationLayout("Reset password") {

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
          +"Reset password"
        }
        div("text-sm text-gray-500 dark:text-gray-300") {
          p("mb-2") {
            +"With the following form you can request a password reset link."
          }
          p("mb-5") {
            +"The link will be send by email and is only valid for a 30 minutes."
            +"It will take you to a form by which you can set a new password for your account."
          }
        }

        // Form

        validationErrorBox(validatedForm)

        form(Paths.requestPasswordReset.pathSegment(), method = FormMethod.post) {
          novalidate = true

          // +csrfToken()

          div("space-y-4") {
            labelledValidationInput(
              "Email",
              toNameString(RequestPasswordResetForm::email),
              InputType.email,
              validatedForm,
              formContent.email ?: ""
            ) {
              attributes["autofocus"] = "true"
            }
          }
          div("text-right mt-6") {
            button(classes = "btn bg-gray-900 text-gray-100 hover:bg-gray-800 dark:bg-gray-100 dark:text-gray-800 dark:hover:bg-white cursor-pointer whitespace-nowrap ml-3") {
              type = ButtonType.submit
              +"Reset password"
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
