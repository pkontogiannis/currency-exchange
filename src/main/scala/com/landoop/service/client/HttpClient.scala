package com.landoop.service.client

import com.landoop.service.domain.{Conversion, ExchangeResponse}
import com.landoop.service.errors.ServiceError

import scala.concurrent.Future

trait HttpClient {

  def getExchangeRate(conversion: Conversion): Future[ExchangeResponse]

  def getExchangeRateEither(conversion: Conversion): Future[Either[ServiceError, ExchangeResponse]]

}
