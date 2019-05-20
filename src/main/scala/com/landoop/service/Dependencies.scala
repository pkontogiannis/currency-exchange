package com.landoop.service

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.landoop.config.Configuration
import com.landoop.service.client.HttpClientImpl
import com.landoop.service.exchange.{ExchangeService, ExchangeServiceImpl}
import com.typesafe.config.{Config, ConfigFactory}

case class Dependencies(exchangeService: ExchangeService)

object Dependencies {

  private val config: Config = ConfigFactory.load()

  def fromConfig(configuration: Configuration)(implicit system: ActorSystem, mat: Materializer): Dependencies = {

    val baseUrl = config.getString("http.client.baseUrl")
    val httpClient = new HttpClientImpl(baseUrl)
    val exchangeService = new ExchangeServiceImpl(httpClient)

    Dependencies(exchangeService)
  }

}