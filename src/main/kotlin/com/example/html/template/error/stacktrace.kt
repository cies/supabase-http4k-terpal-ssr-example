package com.example.html.template.error

import java.io.PrintWriter
import java.io.StringWriter
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR

class DummyClass {
  companion object {
    val thisPackage = this::class.java.`package`.toString().drop(8)
  }
}

val currentPackage = DummyClass.thisPackage
val stacktraceSplitter = Regex("at (.*?)(\\$.*)?\\((.*?)(:.*)?\\)") // TODO(cies): fix dollar sign matching
fun handleException(e: Throwable): Response {
  if (e !is Exception) throw e // Only handle [Exception] and subclasses (not [Throwable]).
  val stackTraceAsHtml = StringWriter().apply { e.printStackTrace(PrintWriter(this)) }.toString()
    .split("\n")
    .drop(1) // drop the error message and class name
    .filter(String::isNotBlank)
    .map(String::trimStart)
    .joinToString("\n") {
      if (!it.startsWith("at")) """<tr><td></td><td></td><td><strong>$it</strong></td></tr>"""
      else stacktraceSplitter.matchEntire(it)?.groups?.let { m ->
        val fileName = m[3]?.value
        val lineNr = m[4]?.value ?: ""
        val classPath = m[1]?.value
        val lambdaStack = m[2]?.value ?: ""
        val trClass = if (classPath?.startsWith(currentPackage) == true) "applicationCode" else "libraryCode"
        """
        |          <tr class="$trClass">
        |            <td align="right"><code>$fileName</code></td>
        |            <td><code>$lineNr&nbsp;&nbsp;</code></td>
        |            <td>$classPath<span style="color:grey">$lambdaStack</span></td>
        |          </tr>
        """.trimMargin()
      } ?: it
    }
  val html = """
    <html>
    <head>
      <title>${e.message} (500)</title>
    </head>
    <body style="margin-left: 30px">
      <p>
        <table>
          <tr>
            <td></td>
            <td></td>
            <td>
              <br/>
              <h1>Internal Server Error <span style="font-size: 70%">(500)<span></h1>
              <h2 style="padding-bottom: 0px; margin-bottom: 0px">${e.message}</h2>
              <strong><span style="color:grey">${e::class.java.name}</span></strong>
              <br/>
              <br/>
            </td>
          </tr>
          $stackTraceAsHtml
        </table>
      </p>
    </body>
    </html>
  """.trimIndent()
  return Response(INTERNAL_SERVER_ERROR).body(html)
}
