package com.example.lib.formparser

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import java.lang.reflect.ParameterizedType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class FormParamsToJsonTest {

  @Test
  fun testSimpleStructures() {
    assertJsonResultEquals("{}", deserialize(listOf()))
    assertJsonResultEquals("""{"foo":""}""", deserialize(listOf(".foo" to "")))
    assertJsonResultEquals(
      """{"foo":"bar"}""",
      deserialize(listOf(".foo" to "bar"))
    )
    assertJsonResultEquals(
      """{"foo":"'bar'"}""",
      deserialize(listOf(".foo" to "'bar'"))
    )
    assertJsonResultEquals(
      """{"foo":"\"bar\""}""",
      deserialize(listOf(".foo" to "\"bar\""))
    )
    assertJsonResultEquals( // everything except `.` and `[]` should go in a key (escaping not implemented); values are
      // completely free-form
      """{"!@#$%^&*()~{}":"<>?:;',./\\"}""",
      deserialize(listOf(".!@#$%^&*()~{}" to "<>?:;',./\\"))
    )
  }

  @Test
  fun testNestedObjectStructures() {
    assertJsonResultEquals(
      """{"foo":"1","bar":"2"}""",
      deserialize(listOf(".foo" to "1", ".bar" to "2"))
    )
    assertJsonResultEquals(
      """{"foo":{"bar":"boo"}}""",
      deserialize(listOf(".foo.bar" to "boo"))
    )
    assertJsonResultEquals(
      """{"foo":{"bar":{"boo":"baz"}}}""",
      deserialize(listOf(".foo.bar.boo" to "baz"))
    )
  }

  // This is not a hard requirement, we will usually deal with maps (JSON objects) at the root level.
  @Test
  fun testRootNodeIsArray() {
    assertJsonResultEquals("""["0"]""", deserialize(listOf("[0]" to "1", "[0]" to "0")))
    assertJsonResultEquals(
      """["0","1",{"a":"2","b":["3"]}]""",
      deserialize(
        listOf("[0]" to "0", "[1]" to "1", "[2].a" to "2", "[2].b[0]" to "3")
      )
    )
  }

  @Test
  fun testPairsToJsonObjectArrays() {
    assertJsonResultEquals("""{"a":[""]}""", deserialize(listOf(".a[0]" to "")))
    assertJsonResultEquals("""{"a":["1"]}""", deserialize(listOf(".a[0]" to "1")))
    assertJsonResultEquals(
      """{"a":["2"]}""",
      deserialize(listOf(".a[0]" to "1", ".a[0]" to "2"))
    )
    assertJsonResultEquals(
      """{"a":["c"]}""",
      deserialize(listOf(".a[0]" to "a", ".a[0]" to "b", ".a[0]" to "c"))
    )
    assertJsonResultEquals(
      """{"x":["1","2"]}""",
      deserialize(listOf(".x[0]" to "1", ".x[1]" to "2"))
    )
    assertJsonResultEquals(
      """{"a":"1","b":["3"]}""",
      deserialize(listOf(".a" to "1", ".b[0]" to "1", ".b[0]" to "2", ".b[0]" to "3"))
    )
    assertJsonResultEquals(
      """{"a":["1"],"b":["3"]}""",
      deserialize(listOf(".a[0]" to "1", ".b[0]" to "1", ".b[0]" to "2", ".b[0]" to "3"))
    )
    assertJsonResultEquals(
      """{"x":{"y":{"z":"1"}}}""",
      deserialize(listOf(".x.y.z" to "1"))
    )
    assertJsonResultEquals(
      """{"x":[[["1"]]]}""",
      deserialize(listOf(".x[0][0][0]" to "1"))
    )
    assertJsonResultEquals(
      """{"x":{"y":["2"]}}""",
      deserialize(listOf(".x.y[0]" to "1", ".x.y[0]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":[[null,"2"]]}""",
      deserialize(listOf(".x[0][1]" to "1", ".x[0][1]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":[null,["2"]]}""",
      deserialize(listOf(".x[1][0]" to "1", ".x[1][0]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":[{"y":["1"]}]}""",
      deserialize(listOf(".x[0].y[0]" to "1"))
    )
    assertJsonResultEquals(
      """{"x":{"y":{"z":["2"]}}}""",
      deserialize(listOf(".x.y.z[0]" to "1", ".x.y.z[0]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":{"y":[{"z":"1"}]}}""",
      deserialize(listOf(".x.y[0].z" to "1"))
    )
    assertJsonResultEquals(
      """{"x":[{"y":"1"},{"z":"2"}]}""",
      deserialize(listOf(".x[0].y" to "1", ".x[1].z" to "2"))
    )
    assertJsonResultEquals(
      """{"x":{"y":[{"z":["1"]}]}}""",
      deserialize(listOf(".x.y[0].z[0]" to "1"))
    )
    assertJsonResultEquals(
      """{"x":{"y":[{"v":{"w":["1"]}}]}}""",
      deserialize(listOf(".x.y[0].v.w[0]" to "1"))
    )
    assertJsonResultEquals(
      """{"x":{"y":[{"z":"1"},{"v":{"w":"2"}}]}}""",
      deserialize(listOf(".x.y[0].z" to "1", ".x.y[1].v.w" to "2"))
    )
    assertJsonResultEquals(
      """{"foo":{"0":"zero","1":"one","2":{"0":"two-zero","1":"two-one"}}}""",
      deserialize(
        listOf(
          ".foo.0" to "zero",
          ".foo.1" to "one",
          ".foo.2.0" to "two-zero",
          ".foo.2.1" to "two-one"
        )
      )
    )
  }

  @Test
  fun testPairsToJsonObjectDoubleNestedArrays() {
    assertJsonResultEquals(
      """{"x":[{"y":[[null,"2"]]}]}""",
      deserialize(listOf(".x[0].y[0][1]" to "1", ".x[0].y[0][1]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":[{"y":[null,["2"]]}]}""",
      deserialize(listOf(".x[0].y[1][0]" to "1", ".x[0].y[1][0]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":[{"y":[null,["2","1"]]}]}""",
      deserialize(listOf(".x[0].y[1][1]" to "1", ".x[0].y[1][0]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":{"y":[{"z":"1"},"2"]}}""",
      deserialize(listOf(".x.y[0].z" to "1", ".x[0].y[1][0]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":{"y":[{"z":"2"},{"w":"4"}]}}""",
      deserialize(
        listOf(
          ".x.y[0].z" to "1", ".x.y[0].z" to "2",
          ".x.y[1].w" to "3", ".x.y[1].w" to "4"
        )
      )
    )
    assertJsonResultEquals(
      """{"x":{"y":[{"z":"2","w":"4"}]}}""",
      deserialize(
        listOf(
          ".x.y[0].z" to "1", ".x.y[0].z" to "2",
          ".x.y[0].w" to "3", ".x.y[0].w" to "4"
        )
      )
    )
    assertJsonResultEquals(
      """{"x":{"y":[{"z":[null,"2"]}]}}""",
      deserialize(listOf(".x.y[0].z[1]" to "1", ".x.y[0].z[1]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":{"y":[["2"]]}}""",
      deserialize(listOf(".x.y[0][0]" to "1", ".x.y[0][0]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":{"y":[["4"],"2"]}}""",
      deserialize(
        listOf(
          ".x.y[0][0]" to "3", ".x.y[0][0]" to "4",
          ".x.y[1]" to "1", ".x.y[1]" to "2"
        )
      )
    )
  }

  @Test
  fun testOverwritingBehavior() {
    // The last one wins behavior is extensively tested in other tests.
    assertThat(
      (jsonResultSuccessOrThrow(
        deserialize(
          listOf(
            ".a.b" to "0",
            ".a.b.c" to "1"
          )
        )
      ) as JsonObject).size
    ).isEqualTo(1)
    assertThat(
      (jsonResultSuccessOrThrow(
        deserialize(
          listOf(
            ".a.b.c[0]" to "0",
            ".a.b.c[1]" to "1"
          )
        )
      ) as JsonObject).size
    ).isEqualTo(1)
    assertThat(
      (jsonResultSuccessOrThrow(
        deserialize(
          listOf(
            "[0][0]" to "00",
            "[0][1]" to "01"
          )
        )
      ) as JsonObject).size
    ).isEqualTo(1)
    assertThat(
      (jsonResultSuccessOrThrow(
        deserialize(
          listOf(
            ".x[0][0]" to "00",
            ".x[0][1]" to "01"
          )
        )
      ) as JsonObject).size
    ).isEqualTo(1)
  }

  @Test
  fun testPairsToJsonObjectErrors() {
    assertThat(deserialize(listOf("" to ""))).isInstanceOf(Failure::class.java)
    assertThat(deserialize(listOf("" to "value"))).isInstanceOf(Failure::class.java)
    assertThat(deserialize(listOf("." to "value"))).isInstanceOf(Failure::class.java)
    assertThat(deserialize(listOf(".." to "value"))).isInstanceOf(Failure::class.java)
    assertThat(deserialize(listOf(".a." to "value"))).isInstanceOf(Failure::class.java)
    assertThat(deserialize(listOf("[" to "value"))).isInstanceOf(Failure::class.java)
    assertThat(deserialize(listOf("[[" to "value"))).isInstanceOf(Failure::class.java)
    assertThat(deserialize(listOf("[0][" to "value"))).isInstanceOf(Failure::class.java)
    assertThat(deserialize(listOf("[]" to "value"))).isInstanceOf(Failure::class.java)
    assertThat(deserialize(listOf(".a[]" to "value"))).isInstanceOf(Failure::class.java)
    assertThat(deserialize(listOf(".a[].b" to "value"))).isInstanceOf(Failure::class.java)
    assertThat(deserialize(listOf("[-1]" to "value"))).isInstanceOf(Failure::class.java)
  }

  @Test
  fun testNullPaddedAddToArray() {
    assertThat(nullPaddedAddToArray(null, 0, JsonNull)).isEqualTo(listOf(null))
    assertThat(nullPaddedAddToArray(null, 0, JsonPrimitive("a"))).isEqualTo(listOf("a"))
    assertThat(nullPaddedAddToArray(null, 2, JsonPrimitive("a"))).isEqualTo(listOf(null, null, "a"))

    val jsonArrayOf3Nulls = JsonArray(listOf(JsonNull, JsonNull, JsonNull))

    assertThat(nullPaddedAddToArray(jsonArrayOf3Nulls, 1, JsonPrimitive("a")))
      .isEqualTo(listOf(null, "a", null))

    assertThat(nullPaddedAddToArray(jsonArrayOf3Nulls, 3, JsonPrimitive("a")))
      .isEqualTo(listOf(null, null, null, "a"))

    assertThat(nullPaddedAddToArray(jsonArrayOf3Nulls, 4, JsonPrimitive("a")))
      .isEqualTo(listOf(null, null, null, null, "a"))

    assertThat(nullPaddedAddToArray(jsonArrayOf3Nulls, 0, jsonArrayOf3Nulls))
      .isEqualTo(listOf(listOf(null, null, null), null, null))

    assertThat(nullPaddedAddToArray(jsonArrayOf3Nulls, 4, jsonArrayOf3Nulls))
      .isEqualTo(listOf(null, null, null, null, listOf(null, null, null)))

    assertThat(nullPaddedAddToArray(null, -1, JsonNull))
      .isEqualTo(emptyList<Any?>())

    assertThat(nullPaddedAddToArray(jsonArrayOf3Nulls, -1, JsonNull))
      .isEqualTo(listOf(null, null, null))
  }

  private fun assertJsonResultEquals(expected: String, jsonResult: Result<JsonElement, String>) {
    val actual = jsonResultSuccessOrThrow(jsonResult)
    val json = try {
      Json.encodeToString(actual)
    } catch (e: RuntimeException) {
      e.printStackTrace()
    }
    assertThat(json).isEqualTo(expected)
  }

  private fun jsonResultSuccessOrThrow(jsonResult: Result<JsonElement, String>): JsonElement {
    return when (jsonResult) {
      is Failure -> throw RuntimeException("jsonResult contained an error!")
      is Success -> jsonResult.value
    }
  }
}
