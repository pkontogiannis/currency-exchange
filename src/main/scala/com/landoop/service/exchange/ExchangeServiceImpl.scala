package com.landoop.service.exchange

import akka.actor.ActorSystem
import akka.http.caching.LfuCache
import akka.http.caching.scaladsl.{Cache, CachingSettings, LfuCacheSettings}
import akka.stream.ActorMaterializer
import com.landoop.service.client.HttpClient
import com.landoop.service.domain.{Conversion, ExchangeResponse, RateResponse}
import com.landoop.service.errors.ServiceError.NotAvailableMapping
import com.landoop.service.errors.{HttpClientException, ServiceError}
import com.landoop.utils.TaskExtras.toFutureOption
import com.typesafe.config.{Config, ConfigFactory}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps

class ExchangeServiceImpl(val httpClient: HttpClient) extends ExchangeService with FailFastCirceSupport {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private val config: Config = ConfigFactory.load()

  val defaultCachingSettings: CachingSettings = CachingSettings(system)
  val lfuCacheSettings: LfuCacheSettings =
    defaultCachingSettings.lfuCacheSettings
      .withInitialCapacity(config.getInt("akka.http.caching.initialCapacity"))
      .withMaxCapacity(config.getInt("akka.http.caching.maxCapacity"))
      .withTimeToLive(config.getInt("akka.http.caching.timeToLive") seconds)
      .withTimeToIdle(config.getInt("akka.http.caching.timeToIdle") seconds)

  val cachingSettings: CachingSettings =
    defaultCachingSettings.withLfuCacheSettings(lfuCacheSettings)

  val lfuCache: Cache[String, ExchangeResponse] = LfuCache(cachingSettings)

  def getExchangeRate(conversion: Conversion): Future[RateResponse] = {

    val maybeCachedRatesFuture: Future[ExchangeResponse] = lfuCache
      .getOrLoad(
        conversion.fromCurrency,
        _ => httpClient.getExchangeRate(conversion)
      )

    maybeCachedRatesFuture.map(
      _.rates.get(conversion.toCurrency)
    ).flatMap {
      case Some(rate) =>
        Future.successful(
          RateResponse(
            exchange = rate,
            original = conversion.amount,
            amount = conversion.amount * rate
          ))
      case None =>
        Future.failed(HttpClientException(s"There is no available mapping from ${conversion.fromCurrency} to ${conversion.toCurrency}"))
    }

  }

  def getExchangeRateFutureOfEither(conversion: Conversion): Future[Either[ServiceError, RateResponse]] = {

    toFutureOption(lfuCache.get(conversion.fromCurrency)).flatMap {
      case None => httpClient.getExchangeRateEither(conversion).flatMap {
        case Left(_) => Future.successful(Left(NotAvailableMapping(conversion)))
        case Right(exchangeResponse) =>
          exchangeResponse.rates.get(conversion.toCurrency) match {
            case None => Future.successful(Left(NotAvailableMapping(conversion)))
            case Some(rate) => Future.successful(Right(
              RateResponse(rate, conversion.amount, conversion.amount * rate)
            ))
          }
      }
      case Some(exchangeResponse) =>
        exchangeResponse.rates.get(conversion.toCurrency) match {
          case None => Future.successful(Left(NotAvailableMapping(conversion)))
          case Some(rate) => Future.successful(Right(
            RateResponse(rate, conversion.amount, conversion.amount * rate)
          ))
        }
    }

  }

  def getExchangeRateEither(conversion: Conversion): Future[Either[ServiceError, RateResponse]] = {

    val maybeCachedRatesFuture: Future[ExchangeResponse] = lfuCache
      .getOrLoad(
        conversion.fromCurrency,
        _ => httpClient.getExchangeRate(conversion)
      )

    maybeCachedRatesFuture.map(
      _.rates.get(conversion.toCurrency)
    ).flatMap {
      case Some(rate) =>
        Future.successful(
          Right(
            RateResponse(
              exchange = rate,
              original = conversion.amount,
              amount = conversion.amount * rate
            )
          ))
      case None =>
        Future.successful(Left(NotAvailableMapping(conversion)))
    }

  }

}
