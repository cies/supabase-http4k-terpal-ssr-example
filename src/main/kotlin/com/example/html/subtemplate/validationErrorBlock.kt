package com.example.html.subtemplate

import io.konform.validation.ValidationResult
import kotlinx.html.FlowContent
import kotlinx.html.HtmlTagMarker
import kotlinx.html.div
import kotlinx.html.p

@HtmlTagMarker
fun FlowContent.validationErrorBlock(validatedForm: ValidationResult<*>) {
  if (!validatedForm.isValid) {
    div("errors") {
      validatedForm.errors.forEach { p("error") { +it.message } }
    }
  }
}
