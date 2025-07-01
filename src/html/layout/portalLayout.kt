package html.layout

import domain.menu.navigationTree
import html.component.alpineDropdownAttributes
import kotlin.text.Typography.nbsp
import kotlinx.html.*


@HtmlTagMarker
inline fun HTML.portalLayout(
  title: String,
  userEmail: String,
  currentPath: String,
  crossinline subtemplate: MAIN.() -> Unit = {}
) {
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
    val alpineVersion = "3.14.9"
    script {
      src = "https://cdn.jsdelivr.net/npm/alpinejs@$alpineVersion/dist/cdn.min.js"
      attributes["defer"] = "true"
    }
    listOf("anchor", "collapse", "focus", "intersect", "mask", "morph", "persist").forEach { alpineExtension ->
      script {
        src = "https://cdn.jsdelivr.net/npm/@alpinejs/$alpineExtension@$alpineVersion/dist/cdn.min.js"
        attributes["defer"] = "true"
      }
    }
    script {
      unsafe {
        +"""
          document.addEventListener('DOMContentLoaded', () => {
            // Light switcher
            const lightSwitches = document.querySelectorAll('.light-switch');
            if (lightSwitches.length > 0) {
              lightSwitches.forEach((lightSwitch, i) => {
                if (localStorage.getItem('dark-mode') === 'true') {
                  lightSwitch.checked = true;
                }
                lightSwitch.addEventListener('change', () => {
                  const { checked } = lightSwitch;
                  lightSwitches.forEach((el, n) => {
                    if (n !== i) {
                      el.checked = checked;
                    }
                  });
                  document.documentElement.classList.add('**:transition-none!');
                  if (lightSwitch.checked) {
                    document.documentElement.classList.add('dark');
                    document.querySelector('html').style.colorScheme = 'dark';
                    localStorage.setItem('dark-mode', true);
                    document.dispatchEvent(new CustomEvent('darkMode', { detail: { mode: 'on' } }));
                  } else {
                    document.documentElement.classList.remove('dark');
                    document.querySelector('html').style.colorScheme = 'light';
                    localStorage.setItem('dark-mode', false);
                    document.dispatchEvent(new CustomEvent('darkMode', { detail: { mode: 'off' } }));
                  }
                  setTimeout(() => {
                    document.documentElement.classList.remove('**:transition-none!');
                  }, 1);
                });
              });
            }
          });

          if (localStorage.getItem('dark-mode') === 'false' || !('dark-mode' in localStorage)) {
            document.querySelector('html').classList.remove('dark');
            document.querySelector('html').style.colorScheme = 'light';
          } else {
            document.querySelector('html').classList.add('dark');
            document.querySelector('html').style.colorScheme = 'dark';
          }
        """.trimIndent()
      }
    }
  }


  body("font-inter antialiased bg-gray-100 dark:bg-gray-900 text-gray-600 dark:text-gray-400") {
    attributes["x-bind:class"] = "{ 'sidebar-expanded': sidebarExpanded }"
    attributes["x-data"] = "{ sidebarOpen: false, sidebarExpanded: localStorage.getItem('sidebar-expanded') == 'true' }"
    attributes["x-init"] = "\$watch('sidebarExpanded', value => localStorage.setItem('sidebar-expanded', value))"

    script {
      unsafe {
        +"""
          if (localStorage.getItem('sidebar-expanded') == 'true') {
            document.querySelector('body').classList.add('sidebar-expanded');
          } else {
            document.querySelector('body').classList.remove('sidebar-expanded');
          }
        """.trimIndent()
      }
    }

    // Page wrapper
    div("flex h-[100dvh] overflow-hidden") {

      // Sidebar
      div("min-w-fit") {

        // Sidebar backdrop (mobile only)
        div("fixed inset-0 bg-gray-900/30 z-40 lg:hidden lg:z-auto transition-opacity duration-200") {
          attributes["x-bind:class"] = "sidebarOpen ? 'opacity-100' : 'opacity-0 pointer-events-none'"
          attributes["aria-hidden"] = "true"
          attributes["x-cloak"] = "true"
        }
        // Sidebar
        div("flex lg:flex! flex-col absolute z-40 left-0 top-0 lg:static lg:left-auto lg:top-auto lg:translate-x-0 h-[100dvh] overflow-y-scroll lg:overflow-y-auto no-scrollbar w-64 lg:w-20 lg:sidebar-expanded:!w-64 2xl:w-64! shrink-0 bg-white dark:bg-gray-800 p-4 transition-all duration-200 ease-in-out rounded-r-2xl shadow-xs") {
          id = "sidebar"
          attributes["x-bind:class"] = "sidebarOpen ? 'max-lg:translate-x-0' : 'max-lg:-translate-x-64'"
          attributes["x-on:click.outside"] = "sidebarOpen = false"
          attributes["x-on:keydown.escape.window"] = "sidebarOpen = false"

          // Sidebar header
          div("flex justify-between mb-10 pr-3 sm:px-2") {
            // Close button
            button(classes = "lg:hidden text-gray-500 hover:text-gray-400") {
              attributes["x-on:click.stop"] = "sidebarOpen = !sidebarOpen"
              attributes["aria-controls"] = "sidebar"
              attributes["x-bind:aria-expanded"] = "sidebarOpen"

              span("sr-only") { +"Close sidebar" }
              unsafe {
                +"""
                <svg class="w-6 h-6 fill-current" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path d="M10.7 18.7l1.4-1.4L7.8 13H20v-2H7.8l4.3-4.3-1.4-1.4L4 12z" />
                </svg>
              """.trimIndent()
              }
            }
            // Logo
            a(classes = "block") {
              href = Paths.dashboard.path()
              // LOGO GOES HERE!
            }
          }
          // Links
          div("space-y-8") {
            // Pages group
            div {
              h3("text-xs uppercase text-gray-400 dark:text-gray-500 font-semibold pl-3") {
                span("hidden lg:block lg:sidebar-expanded:hidden 2xl:hidden text-center w-6") {
                  attributes["aria-hidden"] = "true"
                  +"•••"
                }
                span("lg:hidden lg:sidebar-expanded:block 2xl:block") {
                  +"Pages"
                }
              }
              ul("mt-3") {

                navigationTree.forEach { rootItem ->
                  val isActive = rootItem.isActive(currentPath)

                  // Dashboard
                  li("pl-4 pr-3 py-2 rounded-lg mb-0.5 last:mb-0 bg-linear-to-r") {
                    if (isActive) {
                      classes += "from-violet-500/[0.12] dark:from-violet-500/[0.24] to-violet-500/[0.04]"
                    }
                    attributes["x-data"] = "{ open: ${if (isActive) 1 else 0} }"
                    a(classes = "block text-gray-800 dark:text-gray-100 truncate transition") {
                      if (isActive) {
                        classes += "hover:text-gray-900 dark:hover:text-white"
                      }
                      attributes["x-on:click.prevent"] = "open = !open; sidebarExpanded = true"
                      href = "#0"
                      div("flex items-center justify-between") {
                        div("flex items-center") {
                          unsafe {
                            +"""
                              <svg class="shrink-0 fill-current ${if (isActive) "text-violet-500" else "text-gray-400 dark:text-gray-500"}" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16">
                                <path d="M5.936.278A7.983 7.983 0 0 1 8 0a8 8 0 1 1-8 8c0-.722.104-1.413.278-2.064a1 1 0 1 1 1.932.516A5.99 5.99 0 0 0 2 8a6 6 0 1 0 6-6c-.53 0-1.045.076-1.548.21A1 1 0 1 1 5.936.278Z" />
                                <path d="M6.068 7.482A2.003 2.003 0 0 0 8 10a2 2 0 1 0-.518-3.932L3.707 2.293a1 1 0 0 0-1.414 1.414l3.775 3.775Z" />
                              </svg>
                            """.trimIndent()
                          }
                          span("text-sm font-medium ml-4 lg:opacity-0 lg:sidebar-expanded:opacity-100 2xl:opacity-100 duration-200") {
                            +(rootItem.uiString ?: "")
                          }
                        }
                        div("flex shrink-0 ml-2 lg:opacity-0 lg:sidebar-expanded:opacity-100 2xl:opacity-100 duration-200") {
                          unsafe {
                            // Maybe add `${if (isActive) "rotate-180" else "rotate-0"}` to the svg element's classes.
                            +"""
                              <svg class="w-3 h-3 shrink-0 ml-1 fill-current text-gray-400 dark:text-gray-500" x-bind:class="open ? 'rotate-180' : 'rotate-0'" viewBox="0 0 12 12">
                                <path d="M5.9 11.4L.5 6l1.4-1.4 4 4 4-4L11.3 6z" />
                              </svg>
                            """.trimIndent()
                          }
                        }
                      }
                    }
                    if (rootItem.children.isNotEmpty()) {
                      val isActive = rootItem.isActive(currentPath)

                      div("lg:hidden lg:sidebar-expanded:block 2xl:block") {
                        ul("pl-8 mt-1") {
                          if (!isActive) {
                            classes += "hidden"
                          }
                          attributes["x-bind:class"] = "open ? 'block!' : 'hidden'"
                          rootItem.children.forEach { secondaryItem ->
                            li("mb-1 last:mb-0") {
                              a(classes = "block text-gray-500/90 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 transition truncate") {
                                if (isActive) {
                                  classes += "text-violet-500!"
                                }
                                href = secondaryItem.urlPath?.path() ?: ""
                                span("text-sm font-medium lg:opacity-0 lg:sidebar-expanded:opacity-100 2xl:opacity-100 duration-200") {
                                  +(secondaryItem.uiString ?: "")
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }

          div("pt-3 hidden lg:inline-flex 2xl:hidden justify-end mt-auto") {
            div("w-12 pl-4 pr-3 py-2") {
              button(classes = "text-gray-400 hover:text-gray-500 dark:text-gray-500 dark:hover:text-gray-400 transition-colors") {
                attributes["x-on:click"] = "sidebarExpanded = !sidebarExpanded"
                span("sr-only") { +"Expand / collapse sidebar" }
                unsafe {
                  +"""
                  <svg class="shrink-0 fill-current text-gray-400 dark:text-gray-500 sidebar-expanded:rotate-180" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16">
                    <path d="M15 16a1 1 0 0 1-1-1V1a1 1 0 1 1 2 0v14a1 1 0 0 1-1 1ZM8.586 7H1a1 1 0 1 0 0 2h7.586l-2.793 2.793a1 1 0 1 0 1.414 1.414l4.5-4.5A.997.997 0 0 0 12 8.01M11.924 7.617a.997.997 0 0 0-.217-.324l-4.5-4.5a1 1 0 0 0-1.414 1.414L8.586 7M12 7.99a.996.996 0 0 0-.076-.373Z" />
                  </svg>
                """.trimIndent()
                }
              }
            }
          }
        }
      }

      // Content area
      div("relative flex flex-col flex-1 overflow-y-auto overflow-x-hidden") {
        attributes["x-ref"] = "contentarea"

        // Header
        header("sticky top-0 before:absolute before:inset-0 before:backdrop-blur-md max-lg:before:bg-white/90 dark:max-lg:before:bg-gray-800/90 before:-z-10 z-30 before:bg-white after:absolute after:h-px after:inset-x-0 after:top-full after:bg-gray-200 dark:after:bg-gray-700/60 after:-z-10 dark:before:bg-gray-900") {
          div("px-4 sm:px-6 lg:px-8") {
            div("flex items-center justify-between h-16") {
              // Header: Left side
              div("flex") {
                // Hamburger button
                button(classes = "text-gray-500 hover:text-gray-600 dark:hover:text-gray-400 lg:hidden") {
                  attributes["x-on:click.stop"] = "sidebarOpen = !sidebarOpen"
                  attributes["aria-controls"] = "sidebar"
                  attributes["x-bind:aria-expanded"] = "sidebarOpen"
                  span("sr-only") { +"Open sidebar" }
                  unsafe {
                    +"""
                      <svg class="w-6 h-6 fill-current" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <rect x="4" y="5" width="16" height="2" />
                        <rect x="4" y="11" width="16" height="2" />
                        <rect x="4" y="17" width="16" height="2" />
                      </svg>
                    """.trimIndent()
                  }
                }
              }
              // Header: Right side
              div("flex items-center space-x-3") {
                // Search Button with Modal
                // TODO

                // Notifications button
                div("relative inline-flex") {
                  attributes["x-data"] = "{ open: false }"
                  button(classes = "w-8 h-8 flex items-center justify-center hover:bg-gray-100 lg:hover:bg-gray-200 dark:hover:bg-gray-700/50 dark:lg:hover:bg-gray-800 rounded-full") {
                    attributes["x-bind:class"] = "{ 'bg-gray-200 dark:bg-gray-800': open }"
                    attributes["aria-haspopup"] = "true"
                    attributes["x-on:click.prevent"] = "open = !open"
                    attributes["x-bind:aria-expanded"] = "open"
                    span("sr-only") { +"Notifications" }
                    unsafe {
                      +"""
                        <svg class="fill-current text-gray-500/80 dark:text-gray-400/80" width="16" height="16" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                          <path d="M7 0a7 7 0 0 0-7 7c0 1.202.308 2.33.84 3.316l-.789 2.368a1 1 0 0 0 1.265 1.265l2.595-.865a1 1 0 0 0-.632-1.898l-.698.233.3-.9a1 1 0 0 0-.104-.85A4.97 4.97 0 0 1 2 7a5 5 0 0 1 5-5 4.99 4.99 0 0 1 4.093 2.135 1 1 0 1 0 1.638-1.148A6.99 6.99 0 0 0 7 0Z" />
                          <path d="M11 6a5 5 0 0 0 0 10c.807 0 1.567-.194 2.24-.533l1.444.482a1 1 0 0 0 1.265-1.265l-.482-1.444A4.962 4.962 0 0 0 16 11a5 5 0 0 0-5-5Zm-3 5a3 3 0 0 1 6 0c0 .588-.171 1.134-.466 1.6a1 1 0 0 0-.115.82 1 1 0 0 0-.82.114A2.973 2.973 0 0 1 11 14a3 3 0 0 1-3-3Z" />
                        </svg>
                      """.trimIndent()
                    }
                    div("absolute top-0 right-0 w-2.5 h-2.5 bg-red-500 border-2 border-gray-100 dark:border-gray-900 rounded-full")
                  }
                  div("origin-top-right z-10 absolute top-full -mr-48 sm:mr-0 min-w-80 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700/60 py-1.5 rounded-lg shadow-lg overflow-hidden mt-1 right-0") {
                    alpineDropdownAttributes()
                    div("text-xs font-semibold text-gray-400 dark:text-gray-500 uppercase pt-1.5 pb-2 px-4") { +"Notifications" }
                    ul {
                      notificationItem(
                        "📣",
                        "New dashboard features you should know about...",
                        "Sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim."
                      )
                      notificationItem(
                        "📣",
                        "Edit your information in a swipe",
                        "Sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim."
                      )
                      notificationItem(
                        "📣",
                        "Say goodbye to paper receipts!",
                        "Sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim."
                      )
                    }
                  }
                }

                // Info button
                // TODO

                // Dark mode toggle
                div {
                  input(classes = "light-switch sr-only") {
                    type = InputType.checkBox
                    name = "light-switch"
                    id = "light-switch"
                  }
                  label("flex items-center justify-center cursor-pointer w-8 h-8 hover:bg-gray-100 lg:hover:bg-gray-200 dark:hover:bg-gray-700/50 dark:lg:hover:bg-gray-800 rounded-full") {
                    htmlFor = "light-switch"
                    unsafe {
                      +"""
                        <svg class="dark:hidden fill-current text-gray-500/80 dark:text-gray-400/80" width="16" height="16" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                          <path d="M8 0a1 1 0 0 1 1 1v.5a1 1 0 1 1-2 0V1a1 1 0 0 1 1-1Z"/>
                          <path d="M12 8a4 4 0 1 1-8 0 4 4 0 0 1 8 0Zm-4 2a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z" />
                          <path d="M13.657 3.757a1 1 0 0 0-1.414-1.414l-.354.354a1 1 0 0 0 1.414 1.414l.354-.354ZM13.5 8a1 1 0 0 1 1-1h.5a1 1 0 1 1 0 2h-.5a1 1 0 0 1-1-1ZM13.303 11.889a1 1 0 0 0-1.414 1.414l.354.354a1 1 0 0 0 1.414-1.414l-.354-.354ZM8 13.5a1 1 0 0 1 1 1v.5a1 1 0 1 1-2 0v-.5a1 1 0 0 1 1-1ZM4.111 13.303a1 1 0 1 0-1.414-1.414l-.354.354a1 1 0 1 0 1.414 1.414l.354-.354ZM0 8a1 1 0 0 1 1-1h.5a1 1 0 0 1 0 2H1a1 1 0 0 1-1-1ZM3.757 2.343a1 1 0 1 0-1.414 1.414l.354.354A1 1 0 1 0 4.11 2.697l-.354-.354Z" />
                        </svg>
                      """.trimIndent()
                      +"""
                        <svg class="hidden dark:block fill-current text-gray-500/80 dark:text-gray-400/80" width="16" height="16" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                          <path d="M11.875 4.375a.625.625 0 1 0 1.25 0c.001-.69.56-1.249 1.25-1.25a.625.625 0 1 0 0-1.25 1.252 1.252 0 0 1-1.25-1.25.625.625 0 1 0-1.25 0 1.252 1.252 0 0 1-1.25 1.25.625.625 0 1 0 0 1.25c.69.001 1.249.56 1.25 1.25Z" />
                          <path d="M7.019 1.985a1.55 1.55 0 0 0-.483-1.36 1.44 1.44 0 0 0-1.53-.277C2.056 1.553 0 4.5 0 7.9 0 12.352 3.648 16 8.1 16c3.407 0 6.246-2.058 7.51-4.963a1.446 1.446 0 0 0-.25-1.55 1.554 1.554 0 0 0-1.372-.502c-4.01.552-7.539-2.987-6.97-7ZM2 7.9C2 5.64 3.193 3.664 4.961 2.6 4.82 7.245 8.72 11.158 13.36 11.04 12.265 12.822 10.341 14 8.1 14 4.752 14 2 11.248 2 7.9Z" />
                        </svg>
                      """.trimIndent()
                    }
                    span("sr-only") { +"Switch to light / dark version" }
                  }
                }

                // Divider
                hr("w-px h-6 bg-gray-200 dark:bg-gray-700/60 border-none")

                // User button
                div("relative inline-flex") {
                  attributes["x-data"] = "{ open: false }"
                  button(classes = "inline-flex justify-center items-center group") {
                    attributes["aria-haspopup"] = "true"
                    attributes["x-on:click.prevent"] = "open = !open"
                    attributes["x-bind:aria-expanded"] = "open"
                    img(classes = "w-8 h-8 rounded-full") {
                      // TODO real user
                      src = "https://preview.cruip.com/mosaic/images/user-avatar-32.png"
                      width = "32"
                      height = "32"
                      // TODO real user
                      alt = "avatar image for $userEmail"
                    }
                    div("flex items-center truncate") {
                      span("truncate ml-2 text-sm font-medium text-gray-600 dark:text-gray-100 group-hover:text-gray-800 dark:group-hover:text-white") {
                        // TODO real user
                        +userEmail
                      }
                      unsafe {
                        +"""
                          <svg class="w-3 h-3 shrink-0 ml-1 fill-current text-gray-400 dark:text-gray-500" viewBox="0 0 12 12">
                            <path d="M5.9 11.4L.5 6l1.4-1.4 4 4 4-4L11.3 6z" />
                          </svg>
                        """.trimIndent()
                      }
                    }
                  }
                  div("origin-top-right z-10 absolute top-full min-w-44 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700/60 py-1.5 rounded-lg shadow-lg overflow-hidden mt-1 right-0") {
                    alpineDropdownAttributes()
                    div("pt-0.5 pb-2 px-3 mb-1 border-b border-gray-200 dark:border-gray-700/60") {
                      // TODO add real user data
                      div("font-medium text-gray-800 dark:text-gray-100") { +userEmail }
                      div("text-xs text-gray-500 dark:text-gray-400 italic") { +"Administrator" }
                    }
                    ul {
                      li {
                        a(classes = "font-medium text-sm text-violet-500 hover:text-violet-600 dark:hover:text-violet-400 flex items-center py-1 px-3") {
                          href = "#1"
                          attributes["x-on:click"] = "open = false"
                          attributes["x-on:focus"] = "open = true"
                          attributes["x-on:focusout"] = "open = false"
                          +"Settings"
                        }
                      }
                      li {
                        form {
                          method = FormMethod.post
                          action = Paths.signOut.path()
                          attributes["x-data"] = "true"
                          // TODO implement csrf
                          a(classes = "font-medium text-sm text-violet-500 hover:text-violet-600 dark:hover:text-violet-400 flex items-center py-1 px-3") {
                            href = Paths.signOut.path()
                            attributes["x-on:click.prevent"] = "\$root.submit();"
                            attributes["x-on:focus"] = "open = true"
                            attributes["x-on:focusout"] = "open = false"
                            +"Sign Out"
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        main("grow") {
          subtemplate()
        }
      }
    }
  }
}

fun UL.notificationItem(icon: String, title: String, content: String) {
  li("border-b border-gray-200 dark:border-gray-700/60 last:border-0") {
    a(classes = "block py-2 px-4 hover:bg-gray-50 dark:hover:bg-gray-700/20") {
      href = "#0"
      attributes["x-on:click"] = "open = false"
      attributes["x-on:focus"] = "open = true"
      attributes["x-on:focusout"] = "open = false"
      span("block text-sm mb-2") {
        +icon
        +nbsp.toString()
        span("font-medium text-gray-800 dark:text-gray-100") {
          +title
          +nbsp.toString()
        }
        +content
      }
      span("block text-xs font-medium text-gray-400 dark:text-gray-500") { +"Feb 12, 2024" }
    }
  }
}
