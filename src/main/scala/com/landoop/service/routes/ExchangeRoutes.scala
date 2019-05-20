package com.landoop.service.routes

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Route
import com.landoop.service.domain.Conversion
import com.landoop.service.errors._
import com.landoop.service.exchange.ExchangeService
import io.circe.generic.auto._
import io.circe.syntax._

import scala.util.{Failure, Success}

class ExchangeRoutes(val exchangeService: ExchangeService)
  extends Routes {

  val exchangeRoutes: Route =
    exchange.routes

  object exchange {

    def completeEither[E <: ServiceError, R: ToEntityMarshaller]
    (statusCode: StatusCode, either: => Either[E, R])(
      implicit mapper: ErrorMapper[E, HttpError]
    ): Route = {
      either match {
        case Left(value) => complete(statusCode, ErrorResponse(code = value.code, message = value.message))
        case Right(value) => complete(statusCode, value)
      }
    }

    implicit val httpErrorMapper: ErrorMapper[ServiceError, HttpError] =
      Routes.buildErrorMapper(ServiceError.httpErrorMapper)

    def routes: Route = logRequestResult("LandoopRoutes") {
      pathPrefix("api" / version)(
        exchangeManagement
      )
    }

    def exchangeManagement: Route =
      convert

    def convert: Route = {
      pathPrefix("convert") {
        pathEndOrSingleSlash {
          post {
            entity(as[Conversion]) {
              conversion =>
                onComplete(exchangeService.getExchangeRate(conversion)) {
                  case Success(future) =>
                    complete(StatusCodes.OK, future)
                  case Failure(ex) =>
                    complete(StatusCodes.BadRequest,
                      ErrorResponse(code = "badRequest", message = "There is no available mapping"))
                }
            }
          }
        }
      }
    }
  }

}



