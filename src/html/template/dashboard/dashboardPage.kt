package html.template.dashboard

import html.component.alpineDropdownAttributes
import html.layout.htmlPage
import html.layout.portalLayout
import kotlinx.html.InputType
import kotlinx.html.UL
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.ul
import kotlinx.html.unsafe
import org.http4k.core.Response

fun dashboardPage(userEmail: String, currentPath: String): Response {
  return htmlPage {
    portalLayout("Dashboard", userEmail, currentPath) {

      div("px-4 sm:px-6 lg:px-8 py-8 w-full max-w-9xl mx-auto") {
        // Dashboard actions
        div("sm:flex sm:justify-between sm:items-center mb-8") {
          // Left: Title
          div("mb-4 sm:mb-0") {
            h1("text-2xl md:text-3xl text-gray-800 dark:text-gray-100 font-bold") { +"Dashboard" }
          }
          // Right: Actions
          div("grid grid-flow-col sm:auto-cols-max justify-start sm:justify-end gap-2") {
            // Filter button
            div("relative inline-flex") {
              attributes["x-data"] = "{ open: false }"
              button(classes = "btn px-2.5 bg-white dark:bg-gray-800 border-gray-200 hover:border-gray-300 dark:border-gray-700/60 dark:hover:border-gray-600 text-gray-400 dark:text-gray-500") {
                attributes["aria-haspopup"] = "true"
                attributes["x-on:click.prevent"] = "open = !open"
                attributes["x-bind:aria-expanded"] = "open"
                span("sr-only") { +"Filter" }
                unsafe {
                  +"""
                    <svg class="fill-current" width="16" height="16" viewBox="0 0 16 16">
                      <path d="M0 3a1 1 0 0 1 1-1h14a1 1 0 1 1 0 2H1a1 1 0 0 1-1-1ZM3 8a1 1 0 0 1 1-1h8a1 1 0 1 1 0 2H4a1 1 0 0 1-1-1ZM7 12a1 1 0 1 0 0 2h2a1 1 0 1 0 0-2H7Z" />
                    </svg>
                  """.trimIndent()
                }
              }
              div("origin-top-right z-10 absolute top-full left-0 right-auto min-w-56 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700/60 pt-1.5 rounded-lg shadow-lg overflow-hidden mt-1 md:left-auto md:right-0") {
                alpineDropdownAttributes()

                div("text-xs font-semibold text-gray-400 dark:text-gray-500 uppercase pt-1.5 pb-2 px-3") { +"Filters" }
                ul("mb-4") {
                  fun UL.item(uiString: String) {
                    li("py-1 px-3") {
                      label("flex items-center") {
                        input(classes = "form-checkbox") {
                          type = InputType.checkBox
                          checked = true
                        }
                        span("text-sm font-medium ml-2") { +uiString }
                      }
                    }
                  }
                  item("Real Time Value")
                  item("Top Channels")
                  item("Sales VS Refunds")
                  item("Last Order")
                  item("Total Spent")
                }
                div("py-2 px-3 border-t border-gray-200 dark:border-gray-700/60 bg-gray-50 dark:bg-gray-700/20") {
                  ul("flex items-center justify-between") {
                    li {
                      button(classes = "btn-xs bg-white dark:bg-gray-800 border-gray-200 dark:border-gray-700/60 hover:border-gray-300 dark:hover:border-gray-600 text-red-500") {
                        +"Clear"
                      }
                    }
                    li {
                      button(classes = "btn-xs bg-white dark:bg-gray-800 border-gray-200 dark:border-gray-700/60 hover:border-gray-300 dark:hover:border-gray-600 text-gray-800 dark:text-gray-300") {
                        +"Apply"
                      }
                    }
                  }
                }
              }
            }

            // Datepicker built with flatpickr
            // TODO


            // Add view button
            button(classes = "btn bg-gray-900 text-gray-100 hover:bg-gray-800 dark:bg-gray-100 dark:text-gray-800 dark:hover:bg-white") {
              unsafe {
                +"""
                  <svg class="fill-current shrink-0 xs:hidden" width="16" height="16" viewBox="0 0 16 16">
                    <path d="M15 7H9V1c0-.6-.4-1-1-1S7 .4 7 1v6H1c-.6 0-1 .4-1 1s.4 1 1 1h6v6c0 .6.4 1 1 1s1-.4 1-1V9h6c.6 0 1-.4 1-1s-.4-1-1-1z" />
                  </svg>
                """.trimIndent()
              }
              span("max-xs:sr-only") { +"Add View" }
            }
          }
        }

        p {
          +"Just a bit of text..."
        }
      }
    }
  }
}
