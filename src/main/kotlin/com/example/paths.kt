package com.example

import com.example.lib.urlpath.UrlPath

data object Paths {

  // Static endpoints
  val faviconIco =
    UrlPath("/favicon.ico")
  val robotsTxt =
    UrlPath("/robots.txt")
  val static =
    UrlPath("/static")

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
  val metrics =
    UrlPath(portal, "/metrics")
  val db =
    UrlPath(portal, "/db")
  val reseed =
    UrlPath(portal, "/reseed")
}

