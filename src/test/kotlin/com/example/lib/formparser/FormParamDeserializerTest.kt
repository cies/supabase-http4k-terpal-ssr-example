//package com.example.lib.formparser
//
//import com.fasterxml.jackson.databind.exc.MismatchedInputException
//import io.mockk.every
//import io.mockk.mockkClass
//import models.organization.deal.DealFormDto
//import org.assertj.core.api.Assertions
//import org.junit.jupiter.api.Test
//import play.mvc.Scope
//import utils.jsonserializers.FormParamDeserializer.deserialize
//import views.backstage.settings.integration.calendarfeeds.CalendarFeed
//
//private const val NAME = "NAME"
//private const val URL = "URL"
//private const val ANNUAL_TICKET_SALES = 122
//
//class FormParamDeserializerTest {
//  private fun createMockedParams(paramsAsMap: Map<String, Array<String>>): Scope.Params {
//    val params = mockkClass(Scope.Params::class)
//    every { params.containsFiles() } returns false
//    every { params.all() } returns paramsAsMap
//    return params
//  }
//
//  @Test
//  fun deserialize_happyFlow() {
//    val params = createMockedParams(
//      mapOf(
//        ".isClone" to "false",
//        ".closedById" to "1",
//        ".estimatedAnnualTicketSales" to "$ANNUAL_TICKET_SALES",
//        ".startingDate" to "2022-05-21",
//        ".isCustom" to "false",
//        ".payoutFee" to "",
//        ".chargebackFee" to "",
//        ".description" to "",
//        ".userDiscount" to "20",
//        ".pricingPaymentMethods[0].id" to "1",
//        ".pricingPaymentMethods[0].ticketFeeFixedInCents" to "",
//        ".pricingPaymentMethods[0].ticketFeeVariablePercentage" to "",
//        ".pricingPaymentMethods[0].refundFeeFixedInCents" to "",
//        ".pricingPaymentMethods[0].refundFeeVariablePercentage" to "",
//        ".pricingPaymentMethods[1].id" to "2",
//        ".pricingPaymentMethods[1].ticketFeeFixedInCents" to "",
//        ".pricingPaymentMethods[1].ticketFeeVariablePercentage" to "",
//        ".pricingPaymentMethods[1].refundFeeFixedInCents" to "",
//        ".pricingPaymentMethods[1].refundFeeVariablePercentage" to "",
//        ".pricingPaymentMethods[2].id" to "3",
//        ".pricingPaymentMethods[2].ticketFeeFixedInCents" to "",
//        ".pricingPaymentMethods[2].ticketFeeVariablePercentage" to "",
//        ".pricingPaymentMethods[2].refundFeeFixedInCents" to "",
//        ".pricingPaymentMethods[2].refundFeeVariablePercentage" to "",
//        ".pricingPaymentMethods[3].id" to "4",
//        ".pricingPaymentMethods[3].ticketFeeFixedInCents" to "",
//        ".pricingPaymentMethods[3].ticketFeeVariablePercentage" to "",
//        ".pricingPaymentMethods[3].refundFeeFixedInCents" to "",
//        ".pricingPaymentMethods[3].refundFeeVariablePercentage" to ""
//      ).mapValues { arrayOf(it.value) }
//    )
//
//    val dealFormDto = deserialize(params, DealFormDto::class)
//    Assertions.assertThat(dealFormDto!!.isClone).isFalse
//    Assertions.assertThat(dealFormDto.isCustom).isFalse
//    Assertions.assertThat(dealFormDto.userDiscount).isEqualTo(20.0)
//    Assertions.assertThat(dealFormDto.pricingPaymentMethods.size).isEqualTo(4)
//    Assertions.assertThat(dealFormDto.estimatedAnnualTicketSales).isEqualTo(ANNUAL_TICKET_SALES)
//  }
//
//  @Test
//  fun deserialize_filter() {
//    val params = createMockedParams(
//      mapOf(
//        ".name" to NAME,
//        ".url" to URL,
//        "body" to "body",
//        "authenticityToken" to "authenticityToken",
//        "stuff" to "fakeStuff"
//      ).mapValues { arrayOf(it.value) }
//    )
//    val dto = deserialize(params, CalendarFeed::class)
//    Assertions.assertThat(dto!!.name).isEqualTo(NAME)
//    Assertions.assertThat(dto.url).isEqualTo(URL)
//  }
//
//  /**
//   * A param starting with a '[' should be a valid one, but this one isn't in CalendarFeedDto, so it
//   * can't deserialize
//   */
//  @Test
//  fun deserialize_wrongValue() {
//    val params = createMockedParams(
//      mapOf(
//        ".name" to NAME,
//        ".url" to URL,
//        "[0].stuff" to "fakeStuff"
//      ).mapValues { arrayOf(it.value) }
//    )
//    Assertions.assertThatThrownBy { deserialize(params, CalendarFeed::class) }
//      .isInstanceOf(MismatchedInputException::class.java)
//  }
//
//  @Test
//  fun deserialize_emptyListToDefaults() {
//    val params = createMockedParams(mapOf())
//    val dto = deserialize(params, DefaultValueHoldingDto::class)
//    Assertions.assertThat(dto).isNotNull
//    Assertions.assertThat(dto!!.name).isEqualTo("TEST")
//  }
//}
//
//internal class DefaultValueHoldingDto {
//  val name = "TEST"
//}
