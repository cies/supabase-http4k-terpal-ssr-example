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

  val portal =
    UrlPath("/portal")
  val metrics =
    UrlPath(portal, "/metrics")
  val jdbi =
    UrlPath(portal, "/jdbi")
}

