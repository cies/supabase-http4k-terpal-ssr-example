package html

import io.konform.validation.ValidationResult
import io.konform.validation.messagesAtDataPath
import kotlin.reflect.KProperty1

/**
 * Build names for form fields using the Konform syntax for propertyPath selectors.
 * This should make building form names (that become the keys in the x-www-form-urlencoded bodies)
 * "typo safe" (not fully type safe as it does not take into account the DTO).
 *
 * The string format Konform uses is also expected by our [utils.jsonserializers.FormParamsToJson] class.
 */
fun toNameString(vararg propertyPath: Any): String {
  fun toNameSegment(it: Any): String = when (it) {
    is KProperty1<*, *> -> ".${it.name}"
    is Int -> "[$it]"
    else -> ".$it"
  }
  return propertyPath.joinToString("", transform = ::toNameSegment)
}


/** Returns true when this property path has any validation errors. */
fun hasValidationError(validationResult: ValidationResult<*>, propertyPath: Array<out Any>): Boolean {
  return validationResult.errors.messagesAtDataPath(*propertyPath).isNotEmpty()
}

/** Returns true when this name string has any validation errors. */
fun hasValidationError(validationResult: ValidationResult<*>, name: String): Boolean {
  return validationResult.errors.any { it.dataPath == name }
}

