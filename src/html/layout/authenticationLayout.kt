package html.layout

import kotlinx.html.*


@HtmlTagMarker
inline fun HTML.authenticationLayout(title: String, crossinline subtemplate: DIV.() -> Unit = {}) {
  lang = "en"
  head {
    meta {
      charset = "utf-8"
    }
    title {
      +title
    }
    meta {
      name = "viewport"
      content = "width=device-width,initial-scale=1"
    }
    link {
      rel = "preconnect"
      href = "https://fonts.googleapis.com"
    }
    link {
      rel = "preconnect"
      href = "https://fonts.gstatic.com"
      attributes["crossorigin"] = "true"
    }
    link {
      href = "https://fonts.googleapis.com/css2?family=Inter:wght@400..700&display=swap"
      rel = "stylesheet"
    }
    link {
      href = "/static/styling.css"
      rel = "stylesheet"
    }
  }


  body("font-inter antialiased bg-gray-100 dark:bg-gray-900 text-gray-600 dark:text-gray-400") {
    main("bg-white dark:bg-gray-900") {
      div("relative flex") {
        // Content
        div("w-full md:w-1/2") {
          div("min-h-[100dvh] h-full flex flex-col after:flex-1") {
            subtemplate()
          }
        }
        // Side image
        div("hidden md:block absolute top-0 bottom-0 right-0 md:w-1/2") {
          attributes["aria-hidden"] = "true"
          img(classes = "object-cover object-center w-full h-full") {
            src = "https://preview.cruip.com/mosaic/images/auth-image.jpg"
            width = "760"
            height = "1024"
            alt = "Authentication image"
          }
        }
      }
    }
  }
}
