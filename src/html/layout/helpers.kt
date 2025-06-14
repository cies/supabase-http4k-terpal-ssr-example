package html.layout

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Header


typealias TemplateRenderer = TagConsumer<*>.() -> Unit

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

/** Render an HTML fragment to a String. */
fun renderFragment(renderer: TemplateRenderer): String {
  return buildString {
    appendHTML().renderer()
  }
}
