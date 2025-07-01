package html.template.signout

import Paths
import html.layout.htmlPage
import html.layout.authenticationLayout
import kotlinx.html.*
import org.http4k.core.Response

fun signedOutPage(wasSignedIn: Boolean): Response {
  return htmlPage {
    authenticationLayout("Successfully signed out") {
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
          +"You've been signed out"
        }
        div("text-sm text-gray-500 dark:text-gray-300") {
          if (!wasSignedIn) {
            p("mb-2") {
              +"Well, you were not actually signed in to begin with..."
            }
          }
          p("mb-5") {
            +"If you want to sign in again you can do so with the "
            a(classes = "font-medium text-violet-500 hover:text-violet-600 dark:hover:text-violet-400 cursor-pointer") {
              href = Paths.signIn.pathSegment()
              +"sign-in form"
            }
            +"."
          }
        }
      }
    }
  }
}


