package com.landoop.service

import java.time.LocalDate

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.landoop.service.client.{HttpClient, HttpClientImpl}
import com.landoop.service.domain.Conversion
import com.landoop.service.errors.HttpClientException
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterEach, FunSuite, Matchers}

class HttpClientSpec extends FunSuite
  with ScalaFutures
  with Matchers with BeforeAndAfterEach {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(500, Millis)))

  val wireMockServer = new WireMockServer(wireMockConfig().dynamicPort())

  val conversion: Conversion = Conversion(fromCurrency = "EUR", toCurrency = "USD", amount = 102.6)
  val exchangeRates: (String, BigDecimal) = ("USD", 1.11)
  val currentDate: LocalDate = LocalDate.now

  override def beforeEach {
    wireMockServer.start()
  }

  override def afterEach() {
    wireMockServer.stop()
  }

  test("should get the exchange rates for a currency1") {

    val wireMockPort = wireMockServer.port()
    WireMock.configureFor(wireMockPort)

    val baseUrl = s"http://localhost:$wireMockPort/latest"
    val httpClient = new HttpClientImpl(baseUrl)

    val currencyBase = "EUR"
    stubFor(
      get(urlPathMatching("/latest"))
        .withQueryParam("base", equalTo(currencyBase))
        .willReturn(aResponse
          .withHeader("content-type", "application/json")
          .withStatus(200)
          .withBody(
            s"""
               |{
               |    "rates": {
               |        "${exchangeRates._1}":"${exchangeRates._2}"
               |    },
               |    "base": "${conversion.fromCurrency}",
               |    "date": "$currentDate"
               |}
               |""".stripMargin
          )
        )
    )

    val resultRates = httpClient.getExchangeRate(conversion)

    whenReady(resultRates) { result =>
      result.base shouldBe conversion.fromCurrency
      result.date shouldBe currentDate
      result.rates should contain(exchangeRates)
    }
  }

  test("should return an exception in case of 3rd party api fails") {
    val conversion: Conversion = Conversion(fromCurrency = "EUR", toCurrency = "USD", amount = 102.6)

    val wireMockPort: Int = wireMockServer.port()
    val baseUrl = s"http://localhost:$wireMockPort/latest"
    val httpClient: HttpClient = new HttpClientImpl(baseUrl)
    WireMock.configureFor(wireMockPort)

    stubFor(
      get(urlPathMatching("/latest"))
        .withQueryParam("base", equalTo(conversion.fromCurrency))
        .willReturn(aResponse
          .withStatus(400)
        )
    )

    val resultRates = httpClient.getExchangeRate(conversion)

    whenReady(resultRates.failed) { ex =>
      ex shouldBe a[HttpClientException]
    }
  }

}
