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
          if (wasSignedIn) {
            +"You've been signed out"
          } else {
            +"You're signed out"
          }
        }
        div("text-sm text-gray-500 dark:text-gray-300") {
          p("mb-5") {
            +"Feel free to "
            a(classes = "font-medium text-violet-500 hover:text-violet-600 dark:hover:text-violet-400 cursor-pointer") {
              href = Paths.signIn.pathSegment()
              +"sign-in"
            }
            +" when you are ready for it."
          }
        }
      }
    }
  }
}


