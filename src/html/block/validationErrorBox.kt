package html.block

import io.konform.validation.ValidationResult
import kotlinx.html.FlowContent
import kotlinx.html.HtmlTagMarker
import kotlinx.html.div
import kotlinx.html.p

@HtmlTagMarker
fun FlowContent.validationErrorBox(validatedForm: ValidationResult<*>) {
  if (!validatedForm.isValid) {
    div("errors") {
      validatedForm.errors.forEach { p("error") { +it.message } }
    }
  }
}
