package html.template.signin

import Paths
import html.layout.htmlPage
import html.layout.notSignedIn
import kotlinx.html.*
import org.http4k.core.Response

fun alreadySingedInPage(): Response {
  return htmlPage {
    notSignedIn("Already signed in") {
      h1 {
        +"You have already signed in"
      }
      p {
        +"If you want to, you may "
        a {
          href = Paths.signOut.path()
          +"sign out"
        }
        +" to then sign in with a different account or sign up for an account."
      }
    }
  }
}

