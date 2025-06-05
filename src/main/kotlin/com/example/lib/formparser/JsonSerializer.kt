//package com.example.formparser
//
//import com.fasterxml.jackson.core.JsonProcessingException
//import com.fasterxml.jackson.core.type.TypeReference
//import kotlin.reflect.KClass
//import play.mvc.Http
//import utils.constants.ApiConstant
//
//object JsonSerializer {
//  fun serialize(obj: Any?): String = if (obj == null) ApiConstant.EMPTY_JSON_OBJECT else valueAsString(obj)
//
//  /** Deserializes the request body into the given type */
//  fun <T : Any> deserialize(request: Http.Request, type: KClass<T>): T = deserialize(request.params.get("body"), type)
//
//  /**
//   *  Deserializes the request body into the given type. Uses a more lenient instance, that won't throw errors as much
//   *  as the strict instance.It won't throw an error if:
//   *
//   *  - The JSON contains a field that is not specified in the [type].
//   */
//  fun <T : Any> deserializeLenient(request: Http.Request, type: KClass<T>): T = deserializeLenient(request.params.get("body"), type)
//
//  fun <T : Any> deserializeList(json: String, type: KClass<T>): List<T> {
//    val mapper = JacksonMapper.formCoercer
//    val typeRef = object : TypeReference<List<T>>() {
//      override fun getType() = mapper.typeFactory.constructCollectionType(List::class.java, type.java)
//    }
//    return mapper.readValue(json, typeRef)
//  }
//
//  /** Tries to deserialize the request body into the given type. If this fails, null will be returned. */
//  fun <T : Any> deserializeOrNull(request: Http.Request, type: KClass<T>): T? {
//    val requestBody = request.params.get("body") // Where puts the request body
//    return try {
//      deserialize(requestBody, type)
//    } catch (e: JsonProcessingException) {
//      null
//    }
//  }
//
//  fun <T : Any> deserialize(json: String?, type: KClass<T>): T = JacksonMapper.formCoercer.readValue(json, type.java)
//
//  fun <T : Any> deserializeLenient(json: String?, type: KClass<T>): T = JacksonMapper.lenientInstance.readValue(json, type.java)
//
//  fun valueAsString(value: Any): String = JacksonMapper.formCoercer.writeValueAsString(value)
//}
