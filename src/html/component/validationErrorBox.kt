package html.component

import io.konform.validation.Invalid
import io.konform.validation.ValidationResult
import kotlinx.html.FlowContent
import kotlinx.html.HtmlTagMarker
import kotlinx.html.div
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.ul

@HtmlTagMarker
fun FlowContent.validationErrorBox(validatedForm: ValidationResult<*>) {
  if (validatedForm is Invalid) {
    div("px-4 py-2 rounded-lg text-sm bg-red-200 text-gray-800 mb-5") {
      p("font-bold mt-1 mb-1") {
        +"Whoops! Something went wrong..."
      }
      p("text-sm mb-2") {
        if (validatedForm.errors.size == 1) {
          +"Fix the error below and submit the form again."
        } else {
          +"Fix the errors below and submit the form again."
        }
      }
    }
  }
}
