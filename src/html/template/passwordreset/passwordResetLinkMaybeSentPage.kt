package html.template.passwordreset

import html.layout.htmlPage
import html.layout.notSignedIn
import kotlinx.html.*
import org.http4k.core.Response

fun passwordResetLinkMaybeSentPage(emailAddress: String): Response {
  return htmlPage {
    notSignedIn("Password reset link sent") {
      h1 {
        +"Password reset link sent"
      }
      p {
        +"If the email address you just submitted ($emailAddress) is known to us, a password reset link has just been sent to it."
      }
      p {
        +"Follow the link in that email to set a new password for your account."
      }
    }
  }
}
