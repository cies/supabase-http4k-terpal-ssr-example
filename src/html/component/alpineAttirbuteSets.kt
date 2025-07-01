package html.component

import kotlinx.html.DIV

fun DIV.alpineDropdownAttributes() {
  attributes["x-on:click.outside"] = "open = false"
  attributes["x-on:keydown.escape.window"] = "open = false"
  attributes["x-show"] = "open"
  attributes["x-transition:enter"] = "transition ease-out duration-200 transform"
  attributes["x-transition:enter-start"] = "opacity-0 -translate-y-2"
  attributes["x-transition:enter-end"] = "opacity-100 translate-y-0"
  attributes["x-transition:leave"] = "transition ease-out duration-200"
  attributes["x-transition:leave-start"] = "opacity-100"
  attributes["x-transition:leave-end"] = "opacity-0"
  attributes["x-cloak"] = "true"
}
