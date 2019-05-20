package com.landoop.service.client

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.settings.{ClientConnectionSettings, ConnectionPoolSettings}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.landoop.service.domain.{Conversion, ExchangeResponse}
import com.landoop.service.errors.ServiceError.ClientServiceError
import com.landoop.service.errors.{HttpClientException, ServiceError}
import com.typesafe.config.{Config, ConfigFactory}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps

class HttpClientImpl(val baseUrl: String) extends HttpClient with FailFastCirceSupport {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private val config: Config = ConfigFactory.load()

  val clientConnectionSettings: ClientConnectionSettings =
    ClientConnectionSettings(system)

  val connectionPoolSettings: ConnectionPoolSettings =
    ConnectionPoolSettings(system)
      .withIdleTimeout(
        FiniteDuration(config.getLong("http.client.timeout"), TimeUnit.MILLISECONDS)
      )
      .withMaxRetries(config.getInt("http.client.retries"))
      .withConnectionSettings(clientConnectionSettings)

  def getExchangeRate(conversion: Conversion): Future[ExchangeResponse] = {
    val responseFuture: Future[HttpResponse] =
      Http().singleRequest(
        HttpRequest()
          .withUri(Uri(baseUrl)
            .withQuery(Query("base" -> conversion.fromCurrency))
            //            .withQuery(Query("symbols" -> conversion.toCurrency))
          ),
        settings = connectionPoolSettings
      )
    responseFuture.flatMap {
      case HttpResponse(StatusCodes.OK, _, responseEntity, _) =>
        Unmarshal(responseEntity).to[ExchangeResponse]
      case HttpResponse(statusCode, _, _, _) =>
        Future.failed(HttpClientException(statusCode.reason))
      case _ =>
        Future.failed(HttpClientException("Unknown Reason"))
    }
  }

  def getExchangeRateEither(conversion: Conversion): Future[Either[ServiceError, ExchangeResponse]] = {
    val responseFuture: Future[HttpResponse] =
      Http().singleRequest(
        HttpRequest()
          .withUri(Uri(config.getString("http.client.baseUrl"))
            .withQuery(Query("base" -> conversion.fromCurrency))
            //            .withQuery(Query("symbols" -> conversion.toCurrency))
          ),
        settings = connectionPoolSettings
      )
    responseFuture.flatMap {
      case HttpResponse(StatusCodes.OK, _, responseEntity, _) =>
        Unmarshal(responseEntity).to[ExchangeResponse].map(
          Right(_)
        )
      case HttpResponse(statusCode, _, _, _) =>
        Future.successful(Left(ClientServiceError(statusCode.reason)))
      case _ =>
        Future.successful(Left(ClientServiceError("Unknown Reason")))
    }

  }

}
