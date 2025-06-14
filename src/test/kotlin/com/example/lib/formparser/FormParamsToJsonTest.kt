package com.example.lib.formparser

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class FormParamsToJsonTest {

  @Test
  fun testSimpleStructures() {
    assertJsonResultEquals("{}", formToJsonElement(listOf()))
    assertJsonResultEquals("""{"foo":""}""", formToJsonElement(listOf(".foo" to "")))
    assertJsonResultEquals(
      """{"foo":"bar"}""",
      formToJsonElement(listOf(".foo" to "bar"))
    )
    assertJsonResultEquals(
      """{"foo":"'bar'"}""",
      formToJsonElement(listOf(".foo" to "'bar'"))
    )
    assertJsonResultEquals(
      """{"foo":"\"bar\""}""",
      formToJsonElement(listOf(".foo" to "\"bar\""))
    )
    assertJsonResultEquals( // everything except `.` and `[]` should go in a key (escaping not implemented); values are
      // completely free-form
      """{"!@#$%^&*()~{}":"<>?:;',./\\"}""",
      formToJsonElement(listOf(".!@#$%^&*()~{}" to "<>?:;',./\\"))
    )
  }

  @Test
  fun testNestedObjectStructures() {
    assertJsonResultEquals(
      """{"foo":"1","bar":"2"}""",
      formToJsonElement(listOf(".foo" to "1", ".bar" to "2"))
    )
    assertJsonResultEquals(
      """{"foo":{"bar":"boo"}}""",
      formToJsonElement(listOf(".foo.bar" to "boo"))
    )
    assertJsonResultEquals(
      """{"foo":{"bar":{"boo":"baz"}}}""",
      formToJsonElement(listOf(".foo.bar.boo" to "baz"))
    )
  }

  @Test
  fun testPairsToJsonObjectArrays() {
    assertJsonResultEquals("""{"a":[""]}""", formToJsonElement(listOf(".a[0]" to "")))
    assertJsonResultEquals("""{"a":["1"]}""", formToJsonElement(listOf(".a[0]" to "1")))
    assertJsonResultEquals(
      """{"a":["2"]}""",
      formToJsonElement(listOf(".a[0]" to "1", ".a[0]" to "2"))
    )
    assertJsonResultEquals(
      """{"a":["c"]}""",
      formToJsonElement(listOf(".a[0]" to "a", ".a[0]" to "b", ".a[0]" to "c"))
    )
    assertJsonResultEquals(
      """{"x":["1","2"]}""",
      formToJsonElement(listOf(".x[0]" to "1", ".x[1]" to "2"))
    )
    assertJsonResultEquals(
      """{"a":"1","b":["3"]}""",
      formToJsonElement(listOf(".a" to "1", ".b[0]" to "1", ".b[0]" to "2", ".b[0]" to "3"))
    )
    assertJsonResultEquals(
      """{"a":["1"],"b":["3"]}""",
      formToJsonElement(listOf(".a[0]" to "1", ".b[0]" to "1", ".b[0]" to "2", ".b[0]" to "3"))
    )
    assertJsonResultEquals(
      """{"x":{"y":{"z":"1"}}}""",
      formToJsonElement(listOf(".x.y.z" to "1"))
    )
    assertJsonResultEquals(
      """{"x":[[["1"]]]}""",
      formToJsonElement(listOf(".x[0][0][0]" to "1"))
    )
    assertJsonResultEquals(
      """{"x":{"y":["2"]}}""",
      formToJsonElement(listOf(".x.y[0]" to "1", ".x.y[0]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":[[null,"2"]]}""",
      formToJsonElement(listOf(".x[0][1]" to "1", ".x[0][1]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":[null,["2"]]}""",
      formToJsonElement(listOf(".x[1][0]" to "1", ".x[1][0]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":[{"y":["1"]}]}""",
      formToJsonElement(listOf(".x[0].y[0]" to "1"))
    )
    assertJsonResultEquals(
      """{"x":{"y":{"z":["2"]}}}""",
      formToJsonElement(listOf(".x.y.z[0]" to "1", ".x.y.z[0]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":{"y":[{"z":"1"}]}}""",
      formToJsonElement(listOf(".x.y[0].z" to "1"))
    )
    assertJsonResultEquals(
      """{"x":[{"y":"1"},{"z":"2"}]}""",
      formToJsonElement(listOf(".x[0].y" to "1", ".x[1].z" to "2"))
    )
    assertJsonResultEquals(
      """{"x":{"y":[{"z":["1"]}]}}""",
      formToJsonElement(listOf(".x.y[0].z[0]" to "1"))
    )
    assertJsonResultEquals(
      """{"x":{"y":[{"v":{"w":["1"]}}]}}""",
      formToJsonElement(listOf(".x.y[0].v.w[0]" to "1"))
    )
    assertJsonResultEquals(
      """{"x":{"y":[{"z":"1"},{"v":{"w":"2"}}]}}""",
      formToJsonElement(listOf(".x.y[0].z" to "1", ".x.y[1].v.w" to "2"))
    )
    assertJsonResultEquals(
      """{"foo":{"0":"zero","1":"one","2":{"0":"two-zero","1":"two-one"}}}""",
      formToJsonElement(
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
      formToJsonElement(listOf(".x[0].y[0][1]" to "1", ".x[0].y[0][1]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":[{"y":[null,["2"]]}]}""",
      formToJsonElement(listOf(".x[0].y[1][0]" to "1", ".x[0].y[1][0]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":[{"y":[null,["2","1"]]}]}""",
      formToJsonElement(listOf(".x[0].y[1][1]" to "1", ".x[0].y[1][0]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":{"y":[{"z":"1"},["2"]]}}""",
      formToJsonElement(listOf(".x.y[0].z" to "1", ".x.y[1][0]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":{"y":[{"z":"2"},{"w":"4"}]}}""",
      formToJsonElement(
        listOf(
          ".x.y[0].z" to "1", ".x.y[0].z" to "2",
          ".x.y[1].w" to "3", ".x.y[1].w" to "4"
        )
      )
    )
    assertJsonResultEquals(
      """{"x":{"y":[{"z":"2","w":"4"}]}}""",
      formToJsonElement(
        listOf(
          ".x.y[0].z" to "1", ".x.y[0].z" to "2",
          ".x.y[0].w" to "3", ".x.y[0].w" to "4"
        )
      )
    )
    assertJsonResultEquals(
      """{"x":{"y":[{"z":[null,"2"]}]}}""",
      formToJsonElement(listOf(".x.y[0].z[1]" to "1", ".x.y[0].z[1]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":{"y":[["2"]]}}""",
      formToJsonElement(listOf(".x.y[0][0]" to "1", ".x.y[0][0]" to "2"))
    )
    assertJsonResultEquals(
      """{"x":{"y":[["4"],"2"]}}""",
      formToJsonElement(
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
        formToJsonElement(
          listOf(
            ".a.b" to "0",
            ".a.b.c" to "1"
          )
        )
      ) as JsonObject).size
    ).isEqualTo(1)
    assertThat(
      (jsonResultSuccessOrThrow(
        formToJsonElement(
          listOf(
            ".a.b.c[0]" to "0",
            ".a.b.c[1]" to "1"
          )
        )
      ) as JsonObject).size
    ).isEqualTo(1)
    assertThat(
      (jsonResultSuccessOrThrow(
        formToJsonElement(
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
    assertThat(formToJsonElement(listOf("" to ""))).isInstanceOf(Failure::class.java)
    assertThat(formToJsonElement(listOf("" to "value"))).isInstanceOf(Failure::class.java)
    assertThat(formToJsonElement(listOf("." to "value"))).isInstanceOf(Failure::class.java)
    assertThat(formToJsonElement(listOf(".." to "value"))).isInstanceOf(Failure::class.java)
    assertThat(formToJsonElement(listOf(".a." to "value"))).isInstanceOf(Failure::class.java)
    assertThat(formToJsonElement(listOf("[" to "value"))).isInstanceOf(Failure::class.java)
    assertThat(formToJsonElement(listOf("[[" to "value"))).isInstanceOf(Failure::class.java)
    assertThat(formToJsonElement(listOf("[0][" to "value"))).isInstanceOf(Failure::class.java)
    assertThat(formToJsonElement(listOf("[]" to "value"))).isInstanceOf(Failure::class.java)
    assertThat(formToJsonElement(listOf(".a[]" to "value"))).isInstanceOf(Failure::class.java)
    assertThat(formToJsonElement(listOf(".a[].b" to "value"))).isInstanceOf(Failure::class.java)
    assertThat(formToJsonElement(listOf("[-1]" to "value"))).isInstanceOf(Failure::class.java)
  }

  @Test
  fun testNullPaddedAddToArray() {
    assertThat(nullPaddedAddToArray(null, 0, JsonNull))
      .isEqualTo(JsonArray(listOf(JsonNull)))
    assertThat(nullPaddedAddToArray(null, 0, JsonPrimitive("a")))
      .isEqualTo(JsonArray(listOf(JsonPrimitive("a"))))
    assertThat(nullPaddedAddToArray(null, 2, JsonPrimitive("a")))
      .isEqualTo(JsonArray(listOf(JsonNull, JsonNull, JsonPrimitive("a"))))

    val jsonArrayOf3Nulls = JsonArray(listOf(JsonNull, JsonNull, JsonNull))

    assertThat(nullPaddedAddToArray(jsonArrayOf3Nulls, 1, JsonPrimitive("a")))
      .isEqualTo(JsonArray(listOf(JsonNull, JsonPrimitive("a"), JsonNull)))

    assertThat(nullPaddedAddToArray(jsonArrayOf3Nulls, 3, JsonPrimitive("a")))
      .isEqualTo(JsonArray(listOf(JsonNull, JsonNull, JsonNull, JsonPrimitive("a"))))

    assertThat(nullPaddedAddToArray(jsonArrayOf3Nulls, 4, JsonPrimitive("a")))
      .isEqualTo(JsonArray(listOf(JsonNull, JsonNull, JsonNull, JsonNull, JsonPrimitive("a"))))

    assertThat(nullPaddedAddToArray(jsonArrayOf3Nulls, 0, jsonArrayOf3Nulls))
      .isEqualTo(JsonArray(listOf(JsonArray(listOf(JsonNull, JsonNull, JsonNull)), JsonNull, JsonNull)))

    assertThat(nullPaddedAddToArray(jsonArrayOf3Nulls, 4, jsonArrayOf3Nulls))
      .isEqualTo(JsonArray(listOf(JsonNull, JsonNull, JsonNull, JsonNull, JsonArray(listOf(JsonNull, JsonNull, JsonNull)))))

    assertThat(nullPaddedAddToArray(null, -1, JsonNull))
      .isEqualTo(JsonArray(listOf()))

    assertThat(nullPaddedAddToArray(jsonArrayOf3Nulls, -1, JsonNull))
      .isEqualTo(JsonArray(listOf(JsonNull, JsonNull, JsonNull)))
  }

  @Test
  fun testFailure() {
    // Cannot redefine the structure (first `.x.y` then `.x[0]`; makes no sense).
    assertThat(formToJsonElement(listOf(".x.y[0].z" to "1", ".x[0].y[1][0]" to "2"))).isInstanceOf(Failure::class.java)
    // Root must be a JsonObject
    assertThat(formToJsonElement(listOf("[0]" to "1", "[0]" to "0"))).isInstanceOf(Failure::class.java)
    assertThat(formToJsonElement(listOf("a" to "b"))).isInstanceOf(Failure::class.java)
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
      is Success -> jsonResult.value
      is Failure -> throw RuntimeException("jsonResult contained an error: ${jsonResult.reason}")
    }
  }
}
