package com.example.html.layout

import kotlinx.html.HTML
import kotlinx.html.HtmlTagMarker
import kotlinx.html.MAIN
import kotlinx.html.body
import kotlinx.html.footer
import kotlinx.html.head
import kotlinx.html.header
import kotlinx.html.lang
import kotlinx.html.link
import kotlinx.html.main
import kotlinx.html.meta
import kotlinx.html.nav
import kotlinx.html.title


@HtmlTagMarker
inline fun HTML.notSignedIn(title: String, crossinline subtemplate: MAIN.() -> Unit = {}) {
  lang = "en"
  head {
    meta { charset = "UTF-8" }
    meta { name = "viewport"; content = "width=device-width, initial-scale=1.0" }
    link { rel = "stylesheet"; type = "text/css"; media = "screen"; href = "/static/styling.css" }
    title {
      +title
    }
  }
  body {
    nav {
      comment("Nothing yet")
    }
    header {
      comment("Nothing yet")
    }
    main {
      subtemplate()
    }
    footer {
      comment("Nothing yet")
    }
  }
}


