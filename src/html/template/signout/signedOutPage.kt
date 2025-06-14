package html.template.signout

import Paths
import html.layout.htmlPage
import html.layout.notSignedIn
import kotlinx.html.*
import org.http4k.core.Response

fun signedOutPage(wasSignedIn: Boolean): Response {
  return htmlPage {
    notSignedIn("Successfully signed out") {
      h1 {
        +"Successfully signed out"
      }
      if (!wasSignedIn) {
        p {
          +"Well, you were not actually signed in to begin with..."
        }
      }
      p {
        +"If you want to sign-in you can do so with the "
        a {
          href = Paths.signIn.pathSegment()
          +"sign-in form"
        }
        +"."
      }
    }
  }
}

