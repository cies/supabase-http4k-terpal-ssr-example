package lib.formparser

import minimalizingJsonEncoder
import strictJsonDecoder
import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val FIELD_1 = "FIELD1"
private const val FIELD_2 = "FIELD2"


class JsonSerializerTest {

  @Serializable
  data class DataClassDto(val field1: String, val field2: String)

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
