//package com.example.lib.formparser
//
//import com.fasterxml.jackson.core.JsonProcessingException
//import com.fasterxml.jackson.databind.JsonNode
//import com.squareup.moshi.Moshi
//import com.squareup.moshi.adapter
//import kotlin.reflect.KClass
//import kotlin.reflect.KType
//import org.slf4j.LoggerFactory
//
//private val logger = LoggerFactory.getLogger(FormParamDeserializer::class.java)
//
//val moshi = Moshi.Builder().build()
//
///**
// * Combines the functionality of [FormParamsToJson] and [JacksonMapper] adding the sanitization of Play1 controller's
// * [Scope.Params] (removing all params Play1 adds that are not DTO, like: route params, the `authenticationToken` param
// * and the `body` param).
// */
//object FormParamDeserializer {
//
//  /**
//   * Deserializes Play1 url encoded form parameters from a controller's [Scope.Params] into a data class
//   * of type `T` (usually a `FormDto` data class).
//   *
//   * This uses the Jackson library for type coercion (url encoded form parameter values are all
//   * strings, where the fields of `FormDto` data classes have more specific types).
//   *
//   * It also sanitizes any params that are not fields of the form DTO.
//   */
//  fun <T : Any> deserialize(params: List<Pair<String, String?>>, type: KClass<T>): T? {
//    val json = when (val jsonResult = FormParamsToJson.deserialize(params)) {
//      is FormParamToJsonResult.Success -> jsonResult.json
//      is FormParamToJsonResult.Error -> {
//        // Locally we want the error to bubble up, so it is less likely to be ignored.
//        // if (Play.mode.isDev) throw RuntimeException(jsonResult.message)
//
//        // In remote environments we want to log the exception as a warning return `null` so the call site can determine
//        // how to handle it (most likely it will respond with a `BadRequest`). We do want to log a warning.
//        logger.warn(jsonResult.message)
//        logger.warn("Got the following unparsable x-www-form-urlencoded pairs:")
//        params.forEach { (k, v) -> logger.warn("    $k = $v") }
//        return null
//      }
//    }
//    return jsonToDataClassInstance(json, type)
//  }
//
////  /**
////   * Deserializes a JSON body containing form submission pairs into a class of type `T` (usually a `FormDto` data class).
////   * A flat JSON-object is expected as it basically contains the serialized form values but posted from JavaScript code:
////   *
////   *   // This is how a 99% of forms can be serialized to JSON.
////   *   JSON.stringify(Object.fromEntries(new FormData(document.getElementById('MAIN_FORM'))));
////   *
////   * Like `deserialize(...)` but for a JSON-object instead of url encoded form parameters.
////   *
////   * This is needed when we want JavaScript to submit a form instead of letting the browser submit it as url encoded
////   * form parameters. Usually the reason to do this is UX related (e.g. allow the user to "stay on the page").
////   */
////  // TODO(cies): Unify this with the many deserialize functions in [JsonSerializer]
////  fun <T : Any> deserializeFromJsonFormSubmit(jsonString: String, type: KClass<T>): T? {
////    val json = JacksonMapper.formCoercer.readTree(jsonString)
////    if (!json.isObject) throw RuntimeException("A JSON-object was expected")
////    val pairs = json.properties()
////      .filter { isValidFormFieldName(it.key) }
////      .associate { it.key to arrayOf(it.value.textValue()) }
////    when (val jsonResult = FormParamsToJson.deserialize(pairs)) {
////      is FormParamToJsonResult.Success -> return jsonToDataClassInstance(jsonResult.jsonNode, type)
////      is FormParamToJsonResult.Error -> {
////        // Locally we want the error to bubble up, so it cannot be ignored
////        // if (Play.mode.isDev) throw RuntimeException(jsonResult.message)
////
////        // In remote environments we want to log the exception as a warning return `null` so the call site can determine
////        // how to handle it (most likely it will respond with a `BadRequest`).
////        logger.warn(jsonResult.message)
////        logger.warn("Got the following unparsable JSON string:")
////        jsonString.lines().forEach { l -> logger.warn("    $l") }
////        return null
////      }
////    }
////  }
//
//  private fun isValidFormFieldName(formFieldName: String) =
//    FormParamsToJson.LEADING_CHARACTERS.contains(formFieldName[0])
//}
