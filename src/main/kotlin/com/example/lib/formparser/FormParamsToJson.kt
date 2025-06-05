package com.example.formparser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

private const val DOT = '.'
private const val OPEN_BRACKET = '['

/** Instantiate only once to save resources */
private val mapper = JacksonMapper.formCoercer

/** To prevent DOS attacks */
private const val DEPTH_LIMIT = 50

/** With a positive lookahead to not removes delimiters */
private val NAME_SPLITTER = "(?=\\.)|(?=\\[)".toRegex()

/**
 * Forms are submitted as x-www-form-urlencoded pairs. In order to provide structure to the submitted
 * data various naming conventions for the keys ('name' attributes in the form's input fields) have
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
 * This class uses the Jackson library for creating json structures because this library provides
 * sophisticated type coercion when deserializing a json string to an object. This is needed because
 * this class does not coerce the values (it does not know what to coerce them to) so all values
 * resulting from transposing a form submission to json are of type string.
 *
 * Some behavior: last value wins (and since the order of a Map is undetermined the last is
 * not known at compile time), array indexes need to be of int >= 0, if an array indexes leave gaps
 * they are filled with NullNodes.
 *
 * The output of the [deserialize] method (a Jackson-internal JSON representation) can then be used to do
 * the actual mapping to a DTO.
 */
object FormParamsToJson {
  val LEADING_CHARACTERS = charArrayOf(DOT, OPEN_BRACKET)

  /**
   * Turns key-value pairs, url encoded form params, into a json node tree following the name
   * convention for keys (same convention Konform uses). All keys and primitive values in the
   * resulting json structure are of type string.
   *
   * Since keys (names) can be reused in forms, but not in the Map collection, an array of
   * strings is allowed for each key.
   *
   * The type of [pairs] (`Map<String, Array<String>>`) matches the type of x-www-form-urlencoded
   * form submissions as exposed by our web-framework RePlay.
   */
  fun deserialize(pairs: List<Pair<String, String?>>): FormParamToJsonResult {
    var rootNode: JsonNode = mapper.createObjectNode()
    pairs.forEach { (name, value) ->
      val splitName = name.split(NAME_SPLITTER)
      if (splitName.firstOrNull()?.isNotEmpty() == true) {
        // Always starts with empty string (before the first delimiter)
        return FormParamToJsonResult.Error("Name '$name' did not start with a '.' or '['")
      }
      val taggedSegments = splitName
        .drop(1) // Since proper names start with a delimiter
        .map {
          if (it.length < 2) {
            return FormParamToJsonResult.Error("Zero length segment in: $name")
          }
          // Next we remove tailing `]`, but keep the prefixing `.` or `[` which we call this the segment's "tag".
          if (it.startsWith(OPEN_BRACKET)) it.dropLast(1) else it
        }
      if (taggedSegments.firstOrNull() != null) {
        val jsonResult = normalizeToJson(rootNode, taggedSegments, 0, value)
        rootNode = when (jsonResult) {
          is FormParamToJsonResult.Success -> jsonResult.jsonNode
          is FormParamToJsonResult.Error -> return jsonResult
        }
      } else {
        return FormParamToJsonResult.Error("Cannot parse an empty name")
      }
    }
    return FormParamToJsonResult.Success(rootNode)
  }

  /**
   * Use this to transpose a single key-value pair, while taking into account the [parentNode]. This
   * method is recursive: it calls itself.
   *
   * Keys should adhere the convention we use for the key format: same as Konform selectors. When
   * keys are not understood a [FormParamToJsonResult.Error] is containing a message describing the problem
   * is returned. This is not to be shown to end-users: it's a programming error. We probably want to log
   * the error and make sure the call-site makes sure an appropriate error status (usually BadRequest) is returned.
   *
   * @param parentNode Hold the parent structure (if any) for the current processing step.
   * @param taggedSegments The full key (name) of the form submission pair as a list of tagged
   * strings (the tag is the first char: `.` or `[`).
   * @param segmentIndex The index of the current segment in the [taggedSegments] list.
   * @param value The [value] of the form's input field.
   * @return A [FormParamToJsonResult] containing the [JsonNode] tree as resulted from this parsing step (either an
   * [ObjectNode] or an [ArrayNode]), or alternatively an error message in case we could not parse the input.
   */
  private fun normalizeToJson(
    parentNode: JsonNode?,
    taggedSegments: List<String>,
    segmentIndex: Int,
    value: String?
  ): FormParamToJsonResult {
    if (segmentIndex > DEPTH_LIMIT) return FormParamToJsonResult.Error("Structure too deep")
    if (segmentIndex >= taggedSegments.size) {
      return FormParamToJsonResult.Success(value?.let { TextNode(it) } ?: NullNode.instance)
    }
    val currentTaggedSegment = taggedSegments[segmentIndex] // With the prefixing `.` or `[` (the "tag").
    val currentSegment = currentTaggedSegment.substring(1) // Without the "tag".
    return when (currentTaggedSegment[0]) {
      DOT -> {
        val (currentObject: ObjectNode, nextNode: JsonNode?) = when (parentNode) {
          is ObjectNode -> Pair(parentNode, parentNode[currentSegment])
          // If `parent` is NullNode, TextNode, ArrayNode or `null`: simply replace it (last one wins).
          else -> Pair(mapper.createObjectNode(), null)
        }
        when (val result = normalizeToJson(nextNode, taggedSegments, segmentIndex + 1, value)) {
          is FormParamToJsonResult.Error -> result
          is FormParamToJsonResult.Success ->
            FormParamToJsonResult.Success(currentObject.set(currentSegment, result.jsonNode))
        }
      }

      OPEN_BRACKET -> {
        // Get the currentSegment as an int (the index) and fail if it's not positive
        val index = currentSegment.toIntOrNull()
          ?: return FormParamToJsonResult.Error("Expected an integer instead of '$currentSegment'")
        if (index < 0) return FormParamToJsonResult.Error("Expected integer > 0, got $index")

        val (currentArray: ArrayNode, nextNode: JsonNode?) = when (parentNode) {
          is ArrayNode -> Pair(parentNode, parentNode[index])
          // If `parent` is ObjectNode, NullNode, TextNode or `null`: simply replace it (last one wins).
          else -> Pair(mapper.createArrayNode(), null)
        }
        when (val jsonResult = normalizeToJson(nextNode, taggedSegments, segmentIndex + 1, value)) {
          is FormParamToJsonResult.Error -> jsonResult
          is FormParamToJsonResult.Success ->
            FormParamToJsonResult.Success(nullPaddedAddToArray(currentArray, index, jsonResult.jsonNode))
        }
      }

      else -> FormParamToJsonResult.Error(
        "Every segment should start with '.' or '[', got '${taggedSegments.joinToString(" ")}'"
      )
    }
  }

  /** Adds [value] to [initialArray] on the [index] position, padded with NullNodes if needed. */
  fun nullPaddedAddToArray(initialArray: ArrayNode?, index: Int, value: JsonNode): ArrayNode {
    // Handle `initialArray` being `null` or `JsonNodeType.NULL`
    val array: ArrayNode = if (initialArray == null || initialArray.isNull) mapper.createArrayNode() else initialArray
    if (index < 0) return array // ignore index < 0
    (array.size()..index).forEach { _ -> array.addNull() } // pad with nulls if needed
    array[index] = value
    return array
  }
}

/** Result type, contains the [JsonNode] on [Success] and an error message on [Error]. */
sealed class FormParamToJsonResult {
  data class Success(val jsonNode: JsonNode) : FormParamToJsonResult()
  data class Error(val message: String) : FormParamToJsonResult()
}
