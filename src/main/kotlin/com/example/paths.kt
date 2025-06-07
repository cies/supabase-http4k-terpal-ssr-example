package com.example

import kotlin.text.isNotEmpty
import org.http4k.appendIfNotBlank
import org.http4k.core.Request
import org.http4k.core.UriTemplate

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
  val oAuthRoot =
    UrlPath("/oauth")
  val oAuthCallback =
    UrlPath("/oauth/callback")

  val portal =
    UrlPath("/portal")
  val metrics =
    UrlPath(portal, "/metrics")
  val jdbi =
    UrlPath(portal, "/jdbi")
}

data class UrlPath(private val template: UriTemplate, private val basePath: UrlPath? = null) {
  constructor(templateString: String) : this(UriTemplate.from(templateString)) {
    require(templateString.isNotEmpty()) {
      "A path cannot be empty"
    }
  }

  constructor(basePath: UrlPath, templateString: String) : this(UriTemplate.from(templateString), basePath) {
    require(templateString.isNotEmpty()) {
      "A path cannot be empty"
    }
  }


  fun template(): String = template.toString()

  fun path(parameters: Map<String, String> = mapOf()): String = "/" + template.generate(parameters)

  fun absolutePath(parameters: Map<String, String> = mapOf()): String {
    val segments = mutableListOf(template)
    var currentBasePath = basePath
    while (currentBasePath != null) {
      segments.addFirst(currentBasePath.template)
      currentBasePath = currentBasePath.basePath
    }
    return "/" + UriTemplate.from(segments.joinToString("/") { it.toString() }).generate(parameters)
  }

  fun fullUrl(req: Request, parameters: Map<String, String> = mapOf()): String {
    val baseUrl = buildString {
      // Code copied from http4k's `Uri.toString()`
      appendIfNotBlank(req.uri.scheme, req.uri.scheme, ":")
      appendIfNotBlank(req.uri.authority, "//", req.uri.authority)
    }
    return this.fullUrl(baseUrl, parameters)
  }

  fun fullUrl(baseUrl: String, parameters: Map<String, String> = mapOf()): String =
    template.prefixed(baseUrl).generate(parameters)
}
