package com.landoop.service.errors

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import io.circe.syntax._

trait ApiExceptionHandler extends LazyLogging with FailFastCirceSupport {

  implicit def routeExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case ex: HttpClientException =>
        logger.error(s"An exception occurred while processing request: $ex", ex)
        complete(
          StatusCodes.BadRequest,
          ErrorResponse(code = "badRequest", message = "There is no available mapping")
        )
      case ex: RuntimeException =>
        logger.error(s"An exception occurred while processing request: $ex", ex)
        complete(
          StatusCodes.InternalServerError,
          ErrorResponse(code = "internalServerError", message = "Unexpected error occurred")
        )
    }
}
