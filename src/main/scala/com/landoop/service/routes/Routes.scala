package com.landoop.service.routes

import akka.http.scaladsl.server.{Directives, Route}
import com.landoop.config.Version
import com.landoop.service.Dependencies
import com.landoop.service.errors._
import io.circe.generic.auto._
import io.circe.syntax._

trait Routes extends Version
  with Directives
  with ApiExceptionHandler


object Routes extends Directives {

  def buildRoutes(dependencies: Dependencies): Route =
    new ExchangeRoutes(dependencies.exchangeService).exchangeRoutes

  def buildErrorMapper(serviceErrorMapper: PartialFunction[ServiceError, HttpError]): ErrorMapper[ServiceError, HttpError] =
    (e: ServiceError) =>
      serviceErrorMapper
        .applyOrElse(e, (_: ServiceError) => InternalErrorHttp("Unexpected error"))

}
