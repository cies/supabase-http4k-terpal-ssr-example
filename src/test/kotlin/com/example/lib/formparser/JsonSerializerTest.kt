package com.example.lib.formparser

import com.example.moshi
import com.squareup.moshi.JsonAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val FIELD_1 = "FIELD1"
private const val FIELD_2 = "FIELD2"

val anyJsonAdapter: JsonAdapter<Map<*, *>> = moshi.adapter(Map::class.java)


class JsonSerializerTest {

  @Test
  fun serialize_happyFlow() {
    val json = anyJsonAdapter.toJson(mapOf("Hello" to "World"))
    assertThat("{\"Hello\":\"World\"}").isEqualTo(json)
  }

  @Test
  fun serialize_null() {
    val json = anyJsonAdapter.toJson(null)
    assertThat("{}").isEqualTo(json)
  }

//  @Test
//  fun deserialize_happyFlow() {
//    val json = "{ \"field1\" : \"$FIELD_1\", \"field2\" : \"$FIELD_2\" }"
//    val dataClassDto = JsonSerializer.deserialize(json, DataClassDto::class)
//    assertThat(DataClassDto(FIELD_1, FIELD_2)).isEqualTo(dataClassDto)
//  }
//
//  @Test
//  fun idempotent() {
//    val dataClassDto = DataClassDto(FIELD_1, FIELD_2)
//    val json = anyJsonAdapter.toJson(dataClassDto)
//    val newDataClassDto = JsonSerializer.deserialize(json, DataClassDto::class)
//    assertThat(dataClassDto).isEqualTo(newDataClassDto)
//  }
}

data class DataClassDto(val field1: String, val field2: String)
