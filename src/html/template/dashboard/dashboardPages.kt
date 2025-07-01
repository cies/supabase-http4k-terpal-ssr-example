package html.template.dashboard

import html.bigDropDownIcon
import html.component.alpineDropdownAttributes
import html.layout.htmlPage
import html.layout.portalLayout
import html.plusIcon
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

fun dashboardPage1(userEmail: String, currentPath: String): Response {
  return htmlPage {
    portalLayout("Dashboard 1", userEmail, currentPath) {

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
                unsafe { +bigDropDownIcon }
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
              unsafe { +plusIcon } // This icon only shows in small mobile breakpoints, otherwise show the text:
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

fun dashboardPage2(userEmail: String, currentPath: String): Response {
  return htmlPage {
    portalLayout("Dashboard 2", userEmail, currentPath) {

      div("px-4 sm:px-6 lg:px-8 py-8 w-full max-w-9xl mx-auto") {
        // Dashboard actions
        div("sm:flex sm:justify-between sm:items-center mb-8") {
          // Left: Title
          div("mb-4 sm:mb-0") {
            h1("text-2xl md:text-3xl text-gray-800 dark:text-gray-100 font-bold") { +"Dashboard" }
          }
        }

        p {
          +"Just a bit of text..."
        }
      }
    }
  }
}

fun dashboardPage3(userEmail: String, currentPath: String): Response {
  return htmlPage {
    portalLayout("Dashboard 3", userEmail, currentPath) {

      div("px-4 sm:px-6 lg:px-8 py-8 w-full max-w-9xl mx-auto") {
        // Dashboard actions
        div("sm:flex sm:justify-between sm:items-center mb-8") {
          // Left: Title
          div("mb-4 sm:mb-0") {
            h1("text-2xl md:text-3xl text-gray-800 dark:text-gray-100 font-bold") { +"Dashboard" }
          }
        }

        p("pb-4") {
          +"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus ut tortor ac metus efficitur laoreet laoreet vel eros. Duis dapibus sodales neque sed finibus. Aliquam varius quis diam non sollicitudin. Cras ligula purus, rhoncus non interdum in, suscipit et elit. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Cras tellus ipsum, venenatis vel vehicula vel, pretium eget risus. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed pellentesque viverra arcu. Mauris aliquam, turpis ut sagittis gravida, elit elit auctor risus, a interdum elit enim sit amet augue. Nulla lacinia bibendum aliquet. Duis placerat libero a velit auctor vulputate. Aliquam dui leo, pharetra a elementum eu, semper sit amet massa. Aliquam malesuada, eros a varius tempor, nunc ipsum fringilla mauris, elementum tempus ipsum enim a lorem. Nam ultrices hendrerit lorem at consectetur. Aliquam bibendum malesuada aliquet. Lorem ipsum dolor sit amet, consectetur adipiscing elit."
        }
        p("pb-4") {
          +"Nulla vulputate sollicitudin nisi finibus hendrerit. Maecenas quis sodales est. Donec malesuada accumsan molestie. Nulla auctor metus sit amet placerat feugiat. Praesent rutrum urna non nibh aliquam, nec hendrerit dolor luctus. Ut eleifend mollis suscipit. Donec suscipit sem quis orci accumsan, gravida mattis libero tincidunt. Donec feugiat risus magna, et fringilla leo venenatis vel. Quisque mattis in sapien vel pulvinar. Morbi neque ex, molestie a libero vitae, tincidunt blandit erat. Sed et convallis turpis, eget cursus neque. Nulla venenatis est blandit diam pretium, sed pretium nibh eleifend. Quisque interdum dignissim erat eget vestibulum."
        }
        p("pb-4") {
          +"Integer elementum posuere lectus, ac pellentesque ipsum aliquet vitae. In hac habitasse platea dictumst. Curabitur faucibus tempus elit, eu tincidunt neque sodales sed. Integer eu mollis tellus. Donec sed lorem ligula. Vivamus nulla nunc, congue pulvinar dictum at, venenatis eget tellus. Praesent ex quam, sodales in arcu luctus, hendrerit bibendum lectus. Nullam feugiat vel tortor sed euismod. Donec varius augue nibh, ac pulvinar felis aliquet pharetra. Maecenas faucibus sit amet libero ac aliquet. Nullam vitae fringilla ex, et ultrices lorem. Nullam interdum lacus id tellus feugiat accumsan. Cras at cursus risus. Nunc non magna id massa aliquam ultricies. Etiam hendrerit sit amet ipsum eu auctor."
        }
        p("pb-4") {
          +"Etiam id nulla sodales, cursus ante nec, volutpat massa. Sed id orci faucibus enim congue vulputate id vitae elit. Nullam vitae posuere nibh. Aliquam ut neque quis magna ullamcorper porttitor. Integer finibus turpis eget ullamcorper consequat. Vivamus vel sem ac quam varius finibus. Nulla turpis odio, maximus non sem eu, vehicula posuere magna. Suspendisse mauris arcu, egestas et vulputate sollicitudin, eleifend ut lectus. Nullam quis ipsum mauris. Praesent ultrices, ligula eu fringilla viverra, leo justo posuere nisi, quis dapibus magna nisl ornare nisl. Maecenas at sapien hendrerit lorem vehicula rutrum eu sit amet tortor. Nulla euismod eros vitae nisl faucibus rutrum. Vestibulum tortor leo, pulvinar finibus pellentesque in, posuere a dui. Nullam non pharetra purus, vel finibus leo. Duis bibendum dapibus commodo."
        }
        p("pb-4") {
          +"Praesent imperdiet turpis eu sapien sagittis, lobortis rutrum augue vestibulum. Phasellus vestibulum, lacus quis rhoncus egestas, arcu augue dictum lorem, id venenatis dui lectus a est. Nullam metus erat, sollicitudin sed feugiat id, maximus id ligula. Pellentesque rhoncus egestas viverra. Donec odio quam, semper mattis ipsum sed, vulputate pellentesque arcu. Sed tincidunt mauris nec interdum efficitur. Duis sit amet elit est. Curabitur congue placerat justo, eu finibus eros fringilla eu. Phasellus eu felis at dolor volutpat ullamcorper ut ac massa. Sed tincidunt mauris vel dui rhoncus rhoncus. Curabitur justo nunc, aliquam eu rhoncus aliquam, condimentum sed nunc. Quisque efficitur luctus felis, a tempor ligula mollis sit amet. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Donec ac sagittis risus. Aenean commodo fermentum nibh, sed pellentesque felis pretium nec. "
        }
        p("pb-4") {
          +"Nulla vulputate sollicitudin nisi finibus hendrerit. Maecenas quis sodales est. Donec malesuada accumsan molestie. Nulla auctor metus sit amet placerat feugiat. Praesent rutrum urna non nibh aliquam, nec hendrerit dolor luctus. Ut eleifend mollis suscipit. Donec suscipit sem quis orci accumsan, gravida mattis libero tincidunt. Donec feugiat risus magna, et fringilla leo venenatis vel. Quisque mattis in sapien vel pulvinar. Morbi neque ex, molestie a libero vitae, tincidunt blandit erat. Sed et convallis turpis, eget cursus neque. Nulla venenatis est blandit diam pretium, sed pretium nibh eleifend. Quisque interdum dignissim erat eget vestibulum."
        }
        p("pb-4") {
          +"Integer elementum posuere lectus, ac pellentesque ipsum aliquet vitae. In hac habitasse platea dictumst. Curabitur faucibus tempus elit, eu tincidunt neque sodales sed. Integer eu mollis tellus. Donec sed lorem ligula. Vivamus nulla nunc, congue pulvinar dictum at, venenatis eget tellus. Praesent ex quam, sodales in arcu luctus, hendrerit bibendum lectus. Nullam feugiat vel tortor sed euismod. Donec varius augue nibh, ac pulvinar felis aliquet pharetra. Maecenas faucibus sit amet libero ac aliquet. Nullam vitae fringilla ex, et ultrices lorem. Nullam interdum lacus id tellus feugiat accumsan. Cras at cursus risus. Nunc non magna id massa aliquam ultricies. Etiam hendrerit sit amet ipsum eu auctor."
        }
        p("pb-4") {
          +"Etiam id nulla sodales, cursus ante nec, volutpat massa. Sed id orci faucibus enim congue vulputate id vitae elit. Nullam vitae posuere nibh. Aliquam ut neque quis magna ullamcorper porttitor. Integer finibus turpis eget ullamcorper consequat. Vivamus vel sem ac quam varius finibus. Nulla turpis odio, maximus non sem eu, vehicula posuere magna. Suspendisse mauris arcu, egestas et vulputate sollicitudin, eleifend ut lectus. Nullam quis ipsum mauris. Praesent ultrices, ligula eu fringilla viverra, leo justo posuere nisi, quis dapibus magna nisl ornare nisl. Maecenas at sapien hendrerit lorem vehicula rutrum eu sit amet tortor. Nulla euismod eros vitae nisl faucibus rutrum. Vestibulum tortor leo, pulvinar finibus pellentesque in, posuere a dui. Nullam non pharetra purus, vel finibus leo. Duis bibendum dapibus commodo."
        }
        p("pb-4") {
          +"Praesent imperdiet turpis eu sapien sagittis, lobortis rutrum augue vestibulum. Phasellus vestibulum, lacus quis rhoncus egestas, arcu augue dictum lorem, id venenatis dui lectus a est. Nullam metus erat, sollicitudin sed feugiat id, maximus id ligula. Pellentesque rhoncus egestas viverra. Donec odio quam, semper mattis ipsum sed, vulputate pellentesque arcu. Sed tincidunt mauris nec interdum efficitur. Duis sit amet elit est. Curabitur congue placerat justo, eu finibus eros fringilla eu. Phasellus eu felis at dolor volutpat ullamcorper ut ac massa. Sed tincidunt mauris vel dui rhoncus rhoncus. Curabitur justo nunc, aliquam eu rhoncus aliquam, condimentum sed nunc. Quisque efficitur luctus felis, a tempor ligula mollis sit amet. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Donec ac sagittis risus. Aenean commodo fermentum nibh, sed pellentesque felis pretium nec. "
        }
      }
    }
  }
}


