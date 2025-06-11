//package com.example.lib.formparser
//
//import com.squareup.moshi.JsonAdapter
//import com.squareup.moshi.Moshi
//import com.squareup.moshi.Types
//import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
//import org.assertj.core.api.Assertions
//import org.junit.jupiter.api.Test
//
//val moshi = Moshi.Builder()
//  .add(KotlinJsonAdapterFactory())
//  .build()
//val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
//val adapter: JsonAdapter<Map<String, Any?>> = moshi.adapter(type)
//
//class FormParamsToJsonTest {
//
//  @Test
//  fun testSimpleStructures() {
//    assertJsonResultEquals("{}", FormParamsToJson.deserialize(listOf()))
//    assertJsonResultEquals("""{"foo":""}""", FormParamsToJson.deserialize(listOf(".foo" to "")))
//    assertJsonResultEquals(
//      """{"foo":"bar"}""",
//      FormParamsToJson.deserialize(listOf(".foo" to "bar"))
//    )
//    assertJsonResultEquals(
//      """{"foo":"'bar'"}""",
//      FormParamsToJson.deserialize(listOf(".foo" to "'bar'"))
//    )
//    assertJsonResultEquals(
//      """{"foo":"\"bar\""}""",
//      FormParamsToJson.deserialize(listOf(".foo" to "\"bar\""))
//    )
//    assertJsonResultEquals( // everything except `.` and `[]` should go in a key (escaping not implemented); values are
//      // completely free-form
//      """{"!@#$%^&*()~{}":"<>?:;',./\\"}""",
//      FormParamsToJson.deserialize(listOf(".!@#$%^&*()~{}" to "<>?:;',./\\"))
//    )
//  }
//
//  @Test
//  fun testNestedObjectStructures() {
//    assertJsonResultEquals(
//      """{"foo":"1","bar":"2"}""",
//      FormParamsToJson.deserialize(listOf(".foo" to "1", ".bar" to "2"))
//    )
//    assertJsonResultEquals(
//      """{"foo":{"bar":"boo"}}""",
//      FormParamsToJson.deserialize(listOf(".foo.bar" to "boo"))
//    )
//    assertJsonResultEquals(
//      """{"foo":{"bar":{"boo":"baz"}}}""",
//      FormParamsToJson.deserialize(listOf(".foo.bar.boo" to "baz"))
//    )
//  }
//
//  @Test
//  fun testRootNodeIsArray() {
//    assertJsonResultEquals("""["0"]""", FormParamsToJson.deserialize(listOf("[0]" to "1", "[0]" to "0")))
//    assertJsonResultEquals(
//      """["0","1",{"a":"2","b":["3"]}]""",
//      FormParamsToJson.deserialize(
//        listOf("[0]" to "0", "[1]" to "1", "[2].a" to "2", "[2].b[0]" to "3")
//      )
//    )
//  }
//
//  @Test
//  fun testPairsToJsonObjectArrays() {
//    assertJsonResultEquals("""{"a":[""]}""", FormParamsToJson.deserialize(listOf(".a[0]" to "")))
//    assertJsonResultEquals("""{"a":["1"]}""", FormParamsToJson.deserialize(listOf(".a[0]" to "1")))
//    assertJsonResultEquals(
//      """{"a":["2"]}""",
//      FormParamsToJson.deserialize(listOf(".a[0]" to "1", ".a[0]" to "2"))
//    )
//    assertJsonResultEquals(
//      """{"a":["c"]}""",
//      FormParamsToJson.deserialize(listOf(".a[0]" to "a", ".a[0]" to "b", ".a[0]" to "c"))
//    )
//    assertJsonResultEquals(
//      """{"x":["1","2"]}""",
//      FormParamsToJson.deserialize(listOf(".x[0]" to "1", ".x[1]" to "2"))
//    )
//    assertJsonResultEquals(
//      """{"a":"1","b":["3"]}""",
//      FormParamsToJson.deserialize(listOf(".a" to "1", ".b[0]" to "1", ".b[0]" to "2", ".b[0]" to "3"))
//    )
//    assertJsonResultEquals(
//      """{"a":["1"],"b":["3"]}""",
//      FormParamsToJson.deserialize(listOf(".a[0]" to "1", ".b[0]" to "1", ".b[0]" to "2", ".b[0]" to "3"))
//    )
//    assertJsonResultEquals(
//      """{"x":{"y":{"z":"1"}}}""",
//      FormParamsToJson.deserialize(listOf(".x.y.z" to "1"))
//    )
//    assertJsonResultEquals(
//      """{"x":[[["1"]]]}""",
//      FormParamsToJson.deserialize(listOf(".x[0][0][0]" to "1"))
//    )
//    assertJsonResultEquals(
//      """{"x":{"y":["2"]}}""",
//      FormParamsToJson.deserialize(listOf(".x.y[0]" to "1", ".x.y[0]" to "2"))
//    )
//    assertJsonResultEquals(
//      """{"x":[[null,"2"]]}""",
//      FormParamsToJson.deserialize(listOf(".x[0][1]" to "1", ".x[0][1]" to "2"))
//    )
//    assertJsonResultEquals(
//      """{"x":[null,["2"]]}""",
//      FormParamsToJson.deserialize(listOf(".x[1][0]" to "1", ".x[1][0]" to "2"))
//    )
//    assertJsonResultEquals(
//      """{"x":[{"y":["1"]}]}""",
//      FormParamsToJson.deserialize(listOf(".x[0].y[0]" to "1"))
//    )
//    assertJsonResultEquals(
//      """{"x":{"y":{"z":["2"]}}}""",
//      FormParamsToJson.deserialize(listOf(".x.y.z[0]" to "1", ".x.y.z[0]" to "2"))
//    )
//    assertJsonResultEquals(
//      """{"x":{"y":[{"z":"1"}]}}""",
//      FormParamsToJson.deserialize(listOf(".x.y[0].z" to "1"))
//    )
//    assertJsonResultEquals(
//      """{"x":[{"y":"1"},{"z":"2"}]}""",
//      FormParamsToJson.deserialize(listOf(".x[0].y" to "1", ".x[1].z" to "2"))
//    )
//    assertJsonResultEquals(
//      """{"x":{"y":[{"z":["1"]}]}}""",
//      FormParamsToJson.deserialize(listOf(".x.y[0].z[0]" to "1"))
//    )
//    assertJsonResultEquals(
//      """{"x":{"y":[{"v":{"w":["1"]}}]}}""",
//      FormParamsToJson.deserialize(listOf(".x.y[0].v.w[0]" to "1"))
//    )
//    assertJsonResultEquals(
//      """{"x":{"y":[{"z":"1"},{"v":{"w":"2"}}]}}""",
//      FormParamsToJson.deserialize(listOf(".x.y[0].z" to "1", ".x.y[1].v.w" to "2"))
//    )
//    assertJsonResultEquals(
//      """{"foo":{"0":"zero","1":"one","2":{"0":"two-zero","1":"two-one"}}}""",
//      FormParamsToJson.deserialize(
//        listOf(
//          ".foo.0" to "zero",
//          ".foo.1" to "one",
//          ".foo.2.0" to "two-zero",
//          ".foo.2.1" to "two-one"
//        )
//      )
//    )
//  }
//
//  @Test
//  fun testPairsToJsonObjectDoubleNestedArrays() {
//    assertJsonResultEquals(
//      """{"x":[{"y":[[null,"2"]]}]}""",
//      FormParamsToJson.deserialize(listOf(".x[0].y[0][1]" to "1", ".x[0].y[0][1]" to  "2"))
//    )
//    assertJsonResultEquals(
//      """{"x":[{"y":[null,["2"]]}]}""",
//      FormParamsToJson.deserialize(listOf(".x[0].y[1][0]" to "1", ".x[0].y[1][0]" to "2"))
//    )
//    assertJsonResultEquals(
//      """{"x":[{"y":[null,[null,"2"]]}]}""",
//      FormParamsToJson.deserialize(listOf(".x[0].y[1][1]" to "1", ".x[0].y[1][0]" to "2"))
//    )
//    assertJsonResultEquals(
//      """{"x":{"y":[{"z":"2"}]}}""",
//      FormParamsToJson.deserialize(listOf(".x.y[0].z" to "1", ".x[0].y[1][0]" to "2"))
//    )
//    assertJsonResultEquals(
//      """{"x":{"y":[{"z":"2"},{"w":"4"}]}}""",
//      FormParamsToJson.deserialize(
//        listOf(
//          ".x.y[0].z" to "1", ".x.y[0].z" to "2",
//          ".x.y[1].w" to "3", ".x.y[1].w" to "4"
//        )
//      )
//    )
//    assertJsonResultEquals(
//      """{"x":{"y":[{"z":"2","w":"4"}]}}""",
//      FormParamsToJson.deserialize(
//        listOf(
//          ".x.y[0].z" to "1", ".x.y[0].z" to "2",
//          ".x.y[0].w" to "3", ".x.y[0].w" to "4"
//        )
//      )
//    )
//    assertJsonResultEquals(
//      """{"x":{"y":[{"z":[null,"2"]}]}}""",
//      FormParamsToJson.deserialize(listOf(".x.y[0].z[1]" to "1", ".x.y[0].z[1]" to "2"))
//    )
//    assertJsonResultEquals(
//      """{"x":{"y":[["2"]]}}""",
//      FormParamsToJson.deserialize(listOf(".x.y[0][0]" to "1", ".x.y[0][0]" to "2"))
//    )
//    assertJsonResultEquals(
//      """{"x":{"y":[["4"],"2"]}}""",
//      FormParamsToJson.deserialize(
//        listOf(
//          ".x.y[0][0]" to "3", ".x.y[0][0]" to "4",
//          ".x.y[1]" to "1", ".x.y[1]" to "2"
//        )
//      )
//    )
//  }
//
//  @Test
//  fun testOverwritingBehavior() {
//    // last one wins behavior of the values array is extensively tested in other tests
//    Assertions.assertThat(
//      jsonResultSuccessOrThrow(
//        FormParamsToJson.deserialize(
//          listOf(
//            ".a.b" to "0",
//            ".a.b.c" to "1"
//          )
//        )
//      ).size
//    ).isEqualTo(1)
//    Assertions.assertThat(
//      jsonResultSuccessOrThrow(
//        FormParamsToJson.deserialize(
//          listOf(
//            ".a.b.c[0]" to "0",
//            ".a.b.c[1]" to "1"
//          )
//        )
//      ).size
//    ).isEqualTo(1)
//    Assertions.assertThat(
//      jsonResultSuccessOrThrow(
//        FormParamsToJson.deserialize(
//          listOf(
//            "[0][0]" to "00",
//            "[0][1]" to "01"
//          )
//        )
//      ).size
//    ).isEqualTo(1)
//    Assertions.assertThat(
//      jsonResultSuccessOrThrow(
//        FormParamsToJson.deserialize(
//          listOf(
//            ".x[0][0]" to "00",
//            ".x[0][1]" to "01"
//          )
//        )
//      ).size
//    ).isEqualTo(1)
//  }
//
//  @Test
//  fun testPairsToJsonObjectErrors() {
//    Assertions.assertThat(FormParamsToJson.deserialize(listOf("" to "")))
//      .isInstanceOf(FormToMoshiInputResult.Error::class.java)
//
//    Assertions.assertThat(FormParamsToJson.deserialize(listOf("" to "value")))
//      .isInstanceOf(FormToMoshiInputResult.Error::class.java)
//
//    Assertions.assertThat(FormParamsToJson.deserialize(listOf("." to "value")))
//      .isInstanceOf(FormToMoshiInputResult.Error::class.java)
//
//    Assertions.assertThat(FormParamsToJson.deserialize(listOf(".." to "value")))
//      .isInstanceOf(FormToMoshiInputResult.Error::class.java)
//
//    Assertions.assertThat(FormParamsToJson.deserialize(listOf(".a." to "value")))
//      .isInstanceOf(FormToMoshiInputResult.Error::class.java)
//
//    Assertions.assertThat(FormParamsToJson.deserialize(listOf("[" to "value")))
//      .isInstanceOf(FormToMoshiInputResult.Error::class.java)
//
//    Assertions.assertThat(FormParamsToJson.deserialize(listOf("[[" to "value")))
//      .isInstanceOf(FormToMoshiInputResult.Error::class.java)
//
//    Assertions.assertThat(FormParamsToJson.deserialize(listOf("[0][" to "value")))
//      .isInstanceOf(FormToMoshiInputResult.Error::class.java)
//
//    Assertions.assertThat(FormParamsToJson.deserialize(listOf("[]" to "value")))
//      .isInstanceOf(FormToMoshiInputResult.Error::class.java)
//
//    Assertions.assertThat(FormParamsToJson.deserialize(listOf(".a[]" to "value")))
//      .isInstanceOf(FormToMoshiInputResult.Error::class.java)
//
//    Assertions.assertThat(FormParamsToJson.deserialize(listOf(".a[].b" to "value")))
//      .isInstanceOf(FormToMoshiInputResult.Error::class.java)
//
//    Assertions.assertThat(FormParamsToJson.deserialize(listOf("[-1]" to "value")))
//      .isInstanceOf(FormToMoshiInputResult.Error::class.java)
//  }
//
//  @Test
//  fun testNullPaddedAddToArray() {
//    Assertions.assertThat(
//      FormParamsToJson.nullPaddedAddToArray(null, 0, null).toString()
//    ).isEqualTo("[null]")
//
//    Assertions.assertThat(
//      FormParamsToJson.nullPaddedAddToArray(null, 0, "a").toString()
//    ).isEqualTo("""["a"]""")
//
//    Assertions.assertThat(
//      FormParamsToJson.nullPaddedAddToArray(null, 2, "a").toString()
//    ).isEqualTo("""[null,null,"a"]""")
//
//    val jsonArrayOf3Nulls = mutableListOf(null, null, null)
//
//    Assertions.assertThat(
//      FormParamsToJson.nullPaddedAddToArray(jsonArrayOf3Nulls, 1, "a").toString()
//    ).isEqualTo("""[null,"a",null]""")
//
//    Assertions.assertThat(
//      FormParamsToJson.nullPaddedAddToArray(jsonArrayOf3Nulls, 3, "a").toString()
//    ).isEqualTo("""[null,null,null,"a"]""")
//
//    Assertions.assertThat(
//      FormParamsToJson.nullPaddedAddToArray(jsonArrayOf3Nulls, 4, "a").toString()
//    ).isEqualTo("""[null,null,null,null,"a"]""")
//
//    Assertions.assertThat(
//      FormParamsToJson.nullPaddedAddToArray(jsonArrayOf3Nulls, 0, jsonArrayOf3Nulls).toString()
//    ).isEqualTo("[[null,null,null],null,null]")
//
//    Assertions.assertThat(
//      FormParamsToJson.nullPaddedAddToArray(jsonArrayOf3Nulls, 4, jsonArrayOf3Nulls).toString()
//    ).isEqualTo("[null,null,null,null,[null,null,null]]")
//
//    Assertions.assertThat(
//      FormParamsToJson.nullPaddedAddToArray(null, -1, null).toString()
//    ).isEqualTo("[]")
//
//    Assertions.assertThat(
//      FormParamsToJson.nullPaddedAddToArray(jsonArrayOf3Nulls, -1, null).toString()
//    ).isEqualTo("[null,null,null]")
//  }
//
//  private fun assertJsonResultEquals(expected: String, jsonResult: FormToMoshiInputResult) {
//    val actual = jsonResultSuccessOrThrow(jsonResult)
//    val json = try {
//      adapter.toJson(actual)
//    } catch (e: RuntimeException) {
//      e.printStackTrace()
//    }
//    Assertions.assertThat(json).isEqualTo(expected)
//  }
//
//  private fun jsonResultSuccessOrThrow(jsonResult: FormToMoshiInputResult): Map<String, Any?> {
//    return when (jsonResult) {
//      is FormToMoshiInputResult.Error -> throw RuntimeException("jsonResult contained an error!")
//      is FormToMoshiInputResult.Success -> jsonResult.inputForMoshi as Map<String, Any?>
//    }
//  }
//}
