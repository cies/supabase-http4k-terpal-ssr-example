package com.example.lib.formparser

import com.example.lenientJsonParser
import dev.forkhandles.result4k.onFailure
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * Quick way to get a form submission in http4k decoded to a backing DTO (data class).
 *
 * It first converts the form data to `kotlinx.serialization`'s `JsonElement`. That library is then used to coerce the
 * form data (all strings) into whatever the backing DTO (data class) expects.
 *
 * Errors from both the conversion to `JsonElement` and the decoding to the DTO are passed on the caller if they occur.
 */
inline fun <reified T> List<Pair<String, String?>>.decodeOrFailWith(block: (String) -> Nothing): T {
  // Forms cannot express the difference between `null` and empty string (`""`).
  // We convert all empty strings to `null`, so they can be dealt with using default parameters in the backing DTO.
  val formWithEmptyStringsToNulls = this.map { (key, value) -> if (value == "") key to null else key to value }
  val jsonElement = formToJsonElement(formWithEmptyStringsToNulls).onFailure { block(it.reason) }
  try {
    // Maybe build in leniency (now strict) and log errors/warnings in some cases (like dev-mode or prod-mode)
    return lenientJsonParser.decodeFromJsonElement<T>(jsonElement)
  } catch (e: Exception) {
    block(e.message ?: "Unknown deserialization error")
  }
}
