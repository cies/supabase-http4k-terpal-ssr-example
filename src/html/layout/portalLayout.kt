package html.layout

import domain.menu.navigationTree
import html.backArrow
import html.component.alpineDropdownAttributes
import html.darkModeIcon
import html.smallDropDownIcon
import html.expandOrCollapseMenuGroupArrow
import html.expandOrCollapseSidebarArrow
import html.hamburgerIcon
import html.lightModeIcon
import html.notificationsIcon
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
    // The JS code that makes the dark-mode switch work.
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

    // The JS code that makes the sidebar expandable/collapsable (only available at the mobile breakpoint).
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
              unsafe { +backArrow }
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
//                h3("text-xs uppercase text-gray-400 dark:text-gray-500 font-semibold pl-3") {
//                  span("hidden lg:block lg:sidebar-expanded:hidden 2xl:hidden text-center w-6") {
//                    attributes["aria-hidden"] = "true"
//                    +"•••"
//                  }
//                  span("lg:hidden lg:sidebar-expanded:block 2xl:block") {
//                    +"Pages"
//                  }
//                }
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
                          rootItem.icon?.invoke(isActive)?.let { unsafe { +it } }
                          span("text-sm font-medium ml-4 lg:opacity-0 lg:sidebar-expanded:opacity-100 2xl:opacity-100 duration-200") {
                            +(rootItem.uiString ?: "")
                          }
                        }
                        div("flex shrink-0 ml-2 lg:opacity-0 lg:sidebar-expanded:opacity-100 2xl:opacity-100 duration-200") {
                          unsafe {
                            // Maybe add `${if (isActive) "rotate-180" else "rotate-0"}` to the svg element's classes.
                            +expandOrCollapseMenuGroupArrow
                          }
                        }
                      }
                    }
                    // Only render a block of secondary level entries is there are any...
                    if (rootItem.children.isNotEmpty()) {
                      div("lg:hidden lg:sidebar-expanded:block 2xl:block") {
                        ul("pl-8 mt-1") {
                          // The block of secondary entries is hidden if its ancestor is not active.
                          if (!isActive) {
                            classes += "hidden"
                          }
                          attributes["x-bind:class"] = "open ? 'block!' : 'hidden'"
                          rootItem.children.forEach { secondaryItem ->
                            val isActive = secondaryItem.isActive(currentPath)
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
                  +expandOrCollapseSidebarArrow
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
        header("sticky top-0 before:absolute before:inset-0 before:backdrop-blur-md max-lg:before:bg-white/90 dark:max-lg:before:bg-gray-800/90 before:-z-10 z-30 after:absolute after:h-px after:inset-x-0 after:top-full after:bg-gray-200 dark:after:bg-gray-700/60 after:-z-10 dark:before:bg-gray-900") {
        // header("sticky top-0 before:absolute before:inset-0 before:backdrop-blur-md max-lg:before:bg-white/90 dark:max-lg:before:bg-gray-800/90 before:-z-10 z-30 before:bg-white after:absolute after:h-px after:inset-x-0 after:top-full after:bg-gray-200 dark:after:bg-gray-700/60 after:-z-10 dark:before:bg-gray-900") {
          div("px-4 sm:px-6 lg:px-8") {
            div("flex items-center justify-between h-16 lg:border-b border-gray-200 dark:border-gray-700/60") {
              // Header: Left side
              div("flex") {
                // Hamburger button
                button(classes = "text-gray-500 hover:text-gray-600 dark:hover:text-gray-400 lg:hidden") {
                  attributes["x-on:click.stop"] = "sidebarOpen = !sidebarOpen"
                  attributes["aria-controls"] = "sidebar"
                  attributes["x-bind:aria-expanded"] = "sidebarOpen"
                  span("sr-only") { +"Open sidebar" }
                  unsafe {
                    +hamburgerIcon
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
                    unsafe { +notificationsIcon }
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
                      +lightModeIcon
                      +darkModeIcon
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
                        +smallDropDownIcon
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
