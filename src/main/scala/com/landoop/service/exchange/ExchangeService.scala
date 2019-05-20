package com.landoop.service.exchange

import com.landoop.service.domain.{Conversion, RateResponse}
import com.landoop.service.errors.ServiceError

import scala.concurrent.Future

trait ExchangeService {

  def getExchangeRateEither(conversion: Conversion): Future[Either[ServiceError, RateResponse]]

  def getExchangeRate(conversion: Conversion): Future[RateResponse]

  def getExchangeRateFutureOfEither(conversion: Conversion): Future[Either[ServiceError, RateResponse]]
}
