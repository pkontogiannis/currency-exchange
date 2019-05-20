package com.landoop.service

import java.time.LocalDate

import com.landoop.service.client.HttpClient
import com.landoop.service.domain.{Conversion, ExchangeResponse, RateResponse}
import com.landoop.service.errors.HttpClientException
import com.landoop.service.exchange.ExchangeServiceImpl
import org.mockito.BDDMockito._
import org.mockito.Mockito.{times, verify}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar._
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.Future

class ExchangeServiceSpec extends FunSuite
  with ScalaFutures
  with Matchers {

  val mockHttpClient: HttpClient = mock[HttpClient]
  val exchangeService: ExchangeServiceImpl = new ExchangeServiceImpl(mockHttpClient)
  val conversion: Conversion = Conversion("EUR", "USD", 102.6)
  val rate: BigDecimal = 1.11
  val expectedConversionAmount: BigDecimal = conversion.amount * rate
  val exchangeRates: Map[String, BigDecimal] = Map("USD" -> rate)
  val expectedExchangeResponse: ExchangeResponse = ExchangeResponse(conversion.fromCurrency, exchangeRates, LocalDate.now())

  test("should make a remote call to get the currency") {
    given(mockHttpClient.getExchangeRate(conversion))
      .willReturn(Future.successful(expectedExchangeResponse))

    val result: Future[RateResponse] = exchangeService.getExchangeRate(conversion)

    whenReady(result) { result =>
      result.exchange shouldBe rate
      result.original shouldBe conversion.amount
      result.amount shouldBe expectedConversionAmount
    }

  }

  test("should not make a remote call to get the currency") {
    given(mockHttpClient.getExchangeRate(conversion))
      .willReturn(Future.successful(expectedExchangeResponse))

    val conversionFromRemote = exchangeService.getExchangeRate(conversion)

    whenReady(conversionFromRemote) { result =>
      result.exchange shouldBe rate
      result.original shouldBe conversion.amount
      result.amount shouldBe expectedConversionAmount
    }

    val cachedConversion = exchangeService.getExchangeRate(conversion)
    whenReady(cachedConversion) { result =>
      result.exchange shouldBe rate
      result.original shouldBe conversion.amount
      result.amount shouldBe expectedConversionAmount
      verify(mockHttpClient, times(1)).getExchangeRate(conversion)
    }
  }

  test("should return an exception if the mapping is not exist either in cache or in the api") {

    val exchangeService: ExchangeServiceImpl = new ExchangeServiceImpl(mockHttpClient)

    val exchangeRates: Map[String, BigDecimal] = Map("TUR" -> rate)
    val expectedExchangeResponse: ExchangeResponse = ExchangeResponse(conversion.fromCurrency, exchangeRates, LocalDate.now())

    given(mockHttpClient.getExchangeRate(conversion))
      .willReturn(Future.successful(expectedExchangeResponse))

    val result: Future[RateResponse] = exchangeService.getExchangeRate(conversion)

    whenReady(result.failed) { ex =>
      ex shouldBe a[HttpClientException]
    }

  }


}
