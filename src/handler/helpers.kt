package handler

import Paths
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.SEE_OTHER
import org.http4k.urlEncoded

fun redirectAfterFormSubmission(location: String) = Response(SEE_OTHER).header("Location", location)

/**
 * When a user has to sign in (again) it would be best to serve them a 401 response.
 * But doing so has bad UX (browsers do not deal with this nicely). So 302 it is (most big players do this).
 */
fun redirectToSignIn(reason: SignInReason, targetPath: String) = Response(FOUND)
  .header("Location", Paths.signIn.path() + "?reason=${reason.name}&target=${targetPath.urlEncoded()}")

enum class SignInReason {
  InvalidAuthToken,
  SessionExpired
}

