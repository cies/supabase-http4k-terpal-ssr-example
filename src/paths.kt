import lib.urlpath.UrlPath

data object Paths {

  // Static endpoints
  val faviconIco =
    UrlPath("/favicon.ico")
  val robotsTxt =
    UrlPath("/robots.txt")
  val static =
    UrlPath("/static")

  val root =
    UrlPath("/")

  val ping =
    UrlPath("/ping")
  val signUp =
    UrlPath("/sign-up")
  val signIn =
    UrlPath("/sign-in")
  val signOut =
    UrlPath("/sign-out")
  val requestPasswordReset =
    UrlPath("/reset-password")
  val passwordResetVerification =
    UrlPath("/reset-password-verification/{token}")
  val verificationEmailSent =
    UrlPath("/verification-email-sent")
  val setNewPassword =
    UrlPath("/set-new-password")
  val authReturn =
    UrlPath("/auth-return")
  val optExpired =
    UrlPath("/otp-expired")

  val portal =
    UrlPath("/portal")
  val dashboard =
    UrlPath(portal, "/dashboard")
  val dashboard1 =
    UrlPath(portal, "/dashboard/1")
  val dashboard2 =
    UrlPath(portal, "/dashboard/2")
  val dashboard3 =
    UrlPath(portal, "/dashboard/3")
  val db =
    UrlPath(portal, "/db")
  val reseed =
    UrlPath(portal, "/reseed")
}

