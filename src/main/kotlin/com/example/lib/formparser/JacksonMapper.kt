package com.example.formparser

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * A configured instance of the Jackson Object Mapper used by our code-base (for instance our
 * `FormParamDeserializer`).
 *
 * It uses this mapper to coerce string values (url encoded form param values are all strings)
 * to the types of values used in fields of the target class.
 */
object JacksonMapper {
  private val defaultKotlinModule =
    KotlinModule.Builder()
      .configure(KotlinFeature.NullToEmptyCollection, true)
      .configure(KotlinFeature.NullToEmptyMap, true)
      .configure(KotlinFeature.NullIsSameAsDefault, true)
      .configure(KotlinFeature.StrictNullChecks, true)
      .build()

  /** Used by the FormParamDeserializer to coerce form submission values (all strings) to form backing data classes. */
  val formCoercer: ObjectMapper = jacksonObjectMapper()
    .setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS)
    // With the following two properties, only fields are serialized, getters are ignored
    .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    // Fields that are null are by default not serialized
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .registerModule(JavaTimeModule())
    // Date and date time values should be serialized as textual representation: "2023-04-18T10:20:00".
    // The values will be numeric arrays when this feature is not disabled.
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    // This mey be needed to be able to derive null for an enum field from an empty string value.
    // .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
    .registerModule(
      defaultKotlinModule
    )
}
