package com.example.html.supertemplate

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Header

/**
 * With this function we can create layouts (super templates). We define layouts as values of type
 * [LayoutBuilder]. A [LayoutBuilder] is a type alias for a function that take a (subclass) of
 * BaseController as their first parameter and a [TemplateRenderer]-block containing the
 * kotlinx.html's eDSL code that represents the content the layout wraps around.
 *
 * In order to pass values to a layout, those values need to be declared as public properties on the
 * controller class that particular layout takes in. For instance, the `navigationSidebarLayout`
 * takes a UserController (we need to know the current user in order to build the sidebar menu), and
 * the `eventTabsLayout` takes an EventController as that exposes `event` and `eventTab` properties
 * needed to render the header section with event tabs.
 *
 * Since we can not implement controller's constructors, AND our controllers contain a mix of UI
 * (HTML producing) and API (JSON producing) methods, we have to make the public properties that are
 * used to pass values to the layouts nullable!
 * TODO(cies): Improve this ^^^ maybe with interfaces or by passing multiple arguments to layouts.
 *
 * Since layout usage usually lines up with the controller inheritance tree, the passing of values
 * to layouts by passing them controllers works quite well. The main downside is that some
 * controllers get to contain properties that "belong to the view" (V in MVC). When decoupling the
 * M, V and C we already found that V and C are much more intertwined (they import from each other),
 * while the M is actually decoupled (decouple-able): M can do without importing from V and C.
 */
fun layout(builder: LayoutBuilder): LayoutBuilder {
  return { subtemplate -> builder(subtemplate) }
}

typealias TemplateRenderer = TagConsumer<*>.() -> Unit

typealias LayoutBuilder = TagConsumer<*>.(subtemplate: TemplateRenderer) -> Unit

fun htmlPage(status: Status = Status.OK, block2: HTML.() -> Unit): Response {
  val text = buildString {
    append("<!DOCTYPE html>\n")
    appendHTML().html(block = block2)
  }
  return Response(status)
    .with(Header.CONTENT_TYPE of TEXT_HTML)
    .body(text)
}

/** Evaluate a [TemplateRenderer] block into a [String]. Used in email rendering. */
fun stringFromHtml(renderer: TemplateRenderer): String {
  return StringBuilder().apply {
    append("<!DOCTYPE html>\n")
    appendHTML().renderer()
  }.toString()
}

///** Evaluate a [TemplateRenderer] block into a RePlay [RenderHtml] [Result]. */
//fun resultFromHtml(renderer: TemplateRenderer): RenderHtml {
//  return RenderHtml(stringFromHtml(renderer))
//}

/** Render an HTML fragment to a String. */
fun renderFragment(renderer: TemplateRenderer): String {
  return buildString {
    appendHTML().renderer()
  }
}
