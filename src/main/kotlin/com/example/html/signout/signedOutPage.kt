package com.example.html.signout

import com.example.Paths
import com.example.html.supertemplate.htmlPage
import com.example.html.supertemplate.notSignedIn
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
          href = Paths.signIn.path()
          +"sign-in form"
        }
        +"."
      }
    }
  }
}

