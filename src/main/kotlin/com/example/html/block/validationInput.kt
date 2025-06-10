package com.example.html.block

import com.example.html.hasValidationError
import io.konform.validation.ValidationResult
import kotlinx.html.FlowOrInteractiveOrPhrasingContent
import kotlinx.html.HtmlTagMarker
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.input

@HtmlTagMarker
inline fun FlowOrInteractiveOrPhrasingContent.validationInput(
    nameString: String,
    type: InputType? = null,
    validationResult: ValidationResult<*>,
    classes: String = "",
    value: String? = null,
    isDisabled: Boolean = false,
    isReadOnly: Boolean = false,
    crossinline block: INPUT.() -> Unit = {}
) {
  var newClasses = classes
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
