package html.component

import html.hasValidationError
import io.konform.validation.ValidationResult
import kotlinx.html.*

@HtmlTagMarker
inline fun FlowOrInteractiveOrPhrasingContent.validationInput(
  nameString: String,
  type: InputType? = null,
  validationResult: ValidationResult<*>,
  value: String? = null,
  extraClasses: String = "",
  isDisabled: Boolean = false,
  isReadOnly: Boolean = false,
  crossinline block: INPUT.() -> Unit = {}
) {
  var newClasses = extraClasses
  if (hasValidationError(validationResult, nameString)) {
    newClasses = "$newClasses invalid".trim()
  }
  input(type, name = nameString, classes = newClasses) {
    value?.let { this.value = it }
    disabled = isDisabled
    readonly = isReadOnly
    block()
  }
}

@HtmlTagMarker
inline fun FlowOrInteractiveOrPhrasingContent.labelledValidationInput(
  labelText: String,
  nameString: String,
  type: InputType? = null,
  validationResult: ValidationResult<*>,
  value: String? = null,
  extraClasses: String = "",
  isDisabled: Boolean = false,
  isReadOnly: Boolean = false,
  crossinline block: INPUT.() -> Unit = {}
) {

  label(classes = "block text-sm font-medium mb-3") {
    +labelText

    val isInvalid = hasValidationError(validationResult, nameString)
    val invalidClasses = if (isInvalid) "border-red-500 focus:outline-none focus:ring-3 focus:ring-red-200" else ""
    val newClasses = "form-input w-full $invalidClasses $extraClasses"
    input(type, name = nameString, classes = newClasses) {
      value?.let { this.value = it }
      disabled = isDisabled
      readonly = isReadOnly
      if (isInvalid) {
        attributes["aria-invalid"] = "true"
      }
      block()
    }

    validationResult.errors.filter { it.path.dataPath == nameString }.forEach { error ->
      p("text-xs text-red-600 mt-0.5") {
        role = "alert"
        attributes["aria-live"] = "polite"
        +error.message
      }
    }
  }
}
