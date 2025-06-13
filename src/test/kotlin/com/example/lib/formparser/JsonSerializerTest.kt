package com.example.lib.formparser

import com.example.minimalizingJsonEncoder
import com.example.strictJsonDecoder
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val FIELD_1 = "FIELD1"
private const val FIELD_2 = "FIELD2"


class JsonSerializerTest {

  @Test
  fun serialize_happyFlow() {
    val json = minimalizingJsonEncoder.encodeToString(mapOf("Hello" to "World"))
    assertThat("{\"Hello\":\"World\"}").isEqualTo(json)
  }

  @Test
  fun serialize_null() {
    val json = minimalizingJsonEncoder.encodeToString(mapOf<String, String?>())
    assertThat("{}").isEqualTo(json)
  }

  @Test
  fun deserialize_happyFlow() {
    val json = "{ \"field1\" : \"$FIELD_1\", \"field2\" : \"$FIELD_2\" }"
    val dataClassDto = strictJsonDecoder.decodeFromString<DataClassDto>(json)
    assertThat(DataClassDto(FIELD_1, FIELD_2)).isEqualTo(dataClassDto)
  }

  @Test
  fun reversible() {
    val dataClassDto = DataClassDto(FIELD_1, FIELD_2)
    val json = strictJsonDecoder.encodeToString(dataClassDto)
    val newDataClassDto = strictJsonDecoder.decodeFromString<DataClassDto>(json)
    assertThat(dataClassDto).isEqualTo(newDataClassDto)
  }
}

@Serializable
data class DataClassDto(val field1: String, val field2: String)


fun toJsonElement(value: Any?): JsonElement = when (value) {
  null -> JsonNull
  is Boolean -> JsonPrimitive(value)
  is Number -> JsonPrimitive(value)
  is String -> JsonPrimitive(value)
  is Map<*, *> -> JsonObject(value.mapNotNull {
    val key = it.key as? String ?: return@mapNotNull null
    key to toJsonElement(it.value)
  }.toMap())
  is List<*> -> JsonArray(value.map { toJsonElement(it) })
  else -> error("Unsupported type: ${value::class}")
}
