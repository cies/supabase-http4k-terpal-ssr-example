package com.example.lib.formparser

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.valueOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val NAME = "NAME"
private const val URL = "URL"
private const val ANNUAL_TICKET_SALES = 122

class FormParamDeserializerTest {

  @Test
  fun deserialize_happyFlow() {
    val params = listOf(
      ".isClone" to "false",
      ".closedById" to "1",
      ".estimatedAnnualTicketSales" to "$ANNUAL_TICKET_SALES",
      ".startingDate" to "2022-05-21",
      ".isCustom" to "false",
      ".payoutFee" to "",
      ".chargebackFee" to "",
      ".description" to "",
      ".userDiscount" to "20",
      ".paymentMethods[0].id" to "1",
      ".paymentMethods[0].feeFixedInCents" to "",
      ".paymentMethods[0].feeVariablePercentage" to "",
      ".paymentMethods[1].id" to "2",
      ".paymentMethods[1].feeFixedInCents" to "",
      ".paymentMethods[1].feeVariablePercentage" to "",
      ".paymentMethods[2].id" to "3",
      ".paymentMethods[2].feeFixedInCents" to "",
      ".paymentMethods[2].feeVariablePercentage" to "",
      ".paymentMethods[3].id" to "4",
      ".paymentMethods[3].feeFixedInCents" to "",
      ".paymentMethods[3].feeVariablePercentage" to ""
    )

    val dealFormDto: DealFormDto = params.decodeOrFailWith { reason ->
      throw AssertionError("Deserialization failed: $reason")
    }
    assertThat(dealFormDto.isClone).isFalse
    assertThat(dealFormDto.isCustom).isFalse
    assertThat(dealFormDto.userDiscount).isEqualTo(20.0)
    assertThat(dealFormDto.paymentMethods.size).isEqualTo(4)
    assertThat(dealFormDto.estimatedAnnualTicketSales).isEqualTo(ANNUAL_TICKET_SALES)
  }

  @Test
  fun deserialize_filter() {
    val params = listOf(
      ".name" to NAME,
      ".url" to URL,
      "body" to "body",
      "authenticityToken" to "authenticityToken",
      "stuff" to "fakeStuff"
    )
    // Keys have to start with `.` or `[`. Filter them out first if you have a mixed bag of keys at hands.
    assertThat(formToJsonElement(params)).isInstanceOf(Failure::class.java)
  }

  /** A param starting with a `[` should be valid, but this one isn't in CalendarFeedDto, so it can't deserialize. */
  @Test
  fun deserialize_wrongValue() {
    val params = listOf(
      ".name" to NAME,
      ".url" to URL,
      "[0].stuff" to "fakeStuff"
    )
    assertThat(formToJsonElement(params)).isInstanceOf(Failure::class.java)
  }

  @Test
  fun deserialize_emptyListToDefaults() {
    val params = listOf<Pair<String, String?>>()
    val deserialized = formToJsonElement(params).valueOrNull() ?: throw AssertionError("Deserialization failed")
    val dto: DefaultValueHoldingDto = Json{}.decodeFromJsonElement(deserialized)
    assertThat(dto).isNotNull
    assertThat(dto.name).isEqualTo("TEST")
  }
}

@Serializable
data class DefaultValueHoldingDto(val name: String = "TEST")

@Serializable
data class CalendarFeed(val name: String, val url: String)

@Serializable
data class DealFormDto(
  val closedById: Long? = 0,
  val estimatedAnnualTicketSales: Int? = null,
  @Serializable(with = LocalDateSerializer::class)
  val startingDate: LocalDate? = null,
  val isCustom: Boolean = false,
  val isClone: Boolean = false,
  val payoutFee: Int? = 85,
  val chargebackFee: Int? = 500,
  val userDiscount: Double? = 0.0,
  val description: String? = null,
  val paymentMethods: List<PaymentMethodDto>,
  val dealTemplateId: Long? = null
)

@Serializable
data class PaymentMethodDto(
  val id: Long,
  val feeFixedInCents: Int?,
  val feeVariablePercentage: Double?,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LocalDate::class)
object LocalDateSerializer : KSerializer<LocalDate> {
  private val formatter = DateTimeFormatter.ISO_DATE

  override fun serialize(encoder: Encoder, value: LocalDate) {
    encoder.encodeString(value.format(formatter))
  }

  override fun deserialize(decoder: Decoder): LocalDate {
    return LocalDate.parse(decoder.decodeString(), formatter)
  }
}
