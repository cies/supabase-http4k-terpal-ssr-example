package com.example.lib.formparser

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

private const val DOT = '.'
private const val OPEN_BRACKET = '['

/** To prevent DOS attacks */
private const val DEPTH_LIMIT = 50

/** With a positive lookahead to not removes delimiters */
private val NAME_SPLITTER = "(?=\\.)|(?=\\[)".toRegex()

/**
 * Forms are submitted as x-www-form-urlencoded pairs. To provide structure to the submitted data,
 * various naming conventions for the keys ('name' attributes in the form's input fields) have
 * emerged. This class uses [Konform](https://www.konform.io)'s convention.
 *
 * Here are some test cases of Konform (look for the dataPath strings):
 * https://github.com/konform-kt/konform/blob/master/src/commonTest/kotlin/io/konform/validation/ValidationResultTest.kt
 *
 * The convention is simple: property names start with a dot (`.`) and array elements are
 * specified with a number between brackets (`[]`).
 *
 * See the tests of this class for examples.
 *
 * This class uses the Jackson library for creating JSON structures because this library provides
 * sophisticated type coercion when deserializing a JSON string to an object. This is needed because
 * this class does not coerce the values (it does not know what to coerce them to), so all values
 * resulting from transposing a form submission to JSON are of type string.
 *
 * Some behavior: last value wins (and since the order of a Map is undetermined, the last is
 * not known at compile time), array indexes need to be of int >= 0, if array indexes leave gaps,
 * they are filled with nulls.
 *
 * The output of the [deserialize] method (a Jackson-internal JSON representation) can then be used to do
 * the actual mapping to a DTO.
 */


val LEADING_CHARACTERS = charArrayOf(DOT, OPEN_BRACKET)

/**
 * Turns key-value pairs (basically a www-form-urlencoded form submission), into something Moshi
 * understands following the name convention for keys (the same convention [Konform](https://www.konform.io)
 * uses). All keys and primitive values in the resulting structure are of type string.
 *
 * Since keys (from the HTML input element's name fields) can be reused in forms, it should be expected
 * to have multiple instances of the same key.
 *
 * The type of [pairs] matches the type of form submissions as exposed by [http4k](https://http4k.org).
 */
fun deserialize(pairs: List<Pair<String, String?>>): Result<Any?, String> {
  var rootNode: Map<String, Any?> = mutableMapOf()
  pairs.forEach { (name, value) ->
    val splitName = name.split(NAME_SPLITTER)
    if (splitName.firstOrNull()?.isNotEmpty() == true) {
      // Always starts with an empty string (before the first delimiter)
      return Failure("Name '$name' did not start with a '.' or '['")
    }
    val taggedSegments = splitName
      .drop(1) // Since proper names start with a delimiter
      .map {
        if (it.length < 2) {
          return Failure("Zero length segment in: $name")
        }
        // Next we remove tailing `]`, but keep the prefixing `.` or `[` which we call this the segment's "tag".
        if (it.startsWith(OPEN_BRACKET)) it.dropLast(1) else it
      }
    if (taggedSegments.firstOrNull() != null) {
      val jsonResult = normalizeToJson(rootNode, taggedSegments, 0, value)
      rootNode = when (jsonResult) {
        is Success -> when (jsonResult.value) {
          is MutableMap<*, *> -> jsonResult.value as MutableMap<String, Any?>
          else -> return Failure("Got weird value ${jsonResult.value}")
        }

        is Failure -> return jsonResult
      }
    } else {
      return Failure("Cannot parse an empty name")
    }
  }
  return Success(rootNode)
}

/**
 * Use this to transpose a single key-value pair, while taking into account the [parentMap]. This
 * method is recursive: it calls itself.
 *
 * Keys should adhere the convention we use for the key format: same as Konform selectors. When
 * keys are not understood a [Failure] is containing a message describing the problem
 * is returned. This is not to be shown to end-users: it's a programming error. We probably want to log
 * the error and make sure the call-site makes sure an appropriate error status (usually BadRequest) is returned.
 *
 * @param parentMap Hold the parent structure (if any) for the current processing step.
 * @param taggedSegments The full key (name) of the form submission pair as a list of tagged
 * strings (the tag is the first char: `.` or `[`).
 * @param segmentIndex The index of the current segment in the [taggedSegments] list.
 * @param value The [value] of the form's input field.
 * @return A [FormToMoshiInputResult] containing the [JsonNode] tree as resulted from this parsing step (either an
 * [ObjectNode] or an [ArrayNode]), or alternatively an error message in case we could not parse the input.
 */
private fun normalizeToJson(
  parentMap: Any?,
  taggedSegments: List<String>,
  segmentIndex: Int,
  value: String?
): Result<Any?, String> {
  if (segmentIndex > DEPTH_LIMIT) return Failure("Structure too deep")
  if (segmentIndex >= taggedSegments.size) {
    return Success(value)
  }
  val currentTaggedSegment = taggedSegments[segmentIndex] // With the prefixing `.` or `[` (the "tag").
  val currentSegment = currentTaggedSegment.substring(1) // Without the "tag".
  return when (currentTaggedSegment[0]) {
    DOT -> {
      val (currentObject: MutableMap<*, *>, nextNode: Any?) = when (parentMap) {
        is MutableMap<*, *> -> Pair(parentMap, parentMap[currentSegment])
        // If `parent` is NullNode, TextNode, ArrayNode or `null`: simply replace it (last one wins).
        else -> Pair(mutableMapOf<String, Any?>(), null)
      }
      when (val result = normalizeToJson(nextNode, taggedSegments, segmentIndex + 1, value)) {
        is Failure -> result
        is Success -> {
          (currentObject as MutableMap<String, Any?>)[currentSegment] = result.value
          Success(currentObject)
        }
      }
    }

    OPEN_BRACKET -> {
      // Get the currentSegment as an int (the index) and fail if it's not positive
      val index = currentSegment.toIntOrNull()
        ?: return Failure("Expected an integer instead of '$currentSegment'")
      if (index < 0) return Failure("Expected integer > 0, got $index")

      val (currentArray: MutableList<*>, nextNode: Any?) = when (parentMap) {
        is MutableList<*> -> Pair(parentMap, parentMap[index])
        // If `parent` is ObjectNode, NullNode, TextNode or `null`: simply replace it (last one wins).
        else -> Pair(mutableListOf<Any?>(), null)
      }
      when (val jsonResult = normalizeToJson(nextNode, taggedSegments, segmentIndex + 1, value)) {
        is Failure -> jsonResult
        is Success ->
          Success(nullPaddedAddToArray(currentArray, index, jsonResult.value))
      }
    }

    else -> Failure(
      "Every segment should start with '.' or '[', got '${taggedSegments.joinToString(" ")}'"
    )
  }
}

/** Adds [value] to [initialArray] on the [index] position, padded with NullNodes if needed. */
fun nullPaddedAddToArray(initialArray: MutableList<*>?, index: Int, value: Any?): MutableList<Any?> {
  // Handle `initialArray` being `null` or `JsonNodeType.NULL`
  val array: MutableList<Any?> = (initialArray ?: mutableListOf<Any?>()) as MutableList<Any?>
  if (index < 0) return array // ignore index < 0
  (array.size..index).forEach { _ -> array.add(null) } // pad with nulls if needed
  array[index] = value
  return array
}
