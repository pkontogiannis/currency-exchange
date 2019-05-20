package com.landoop.service.errors

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError, NotFound}

sealed trait HttpError extends Serializable {
  val statusCode: StatusCode
  val code: String
  val message: String
}

case class MappingNotFoundErrorHttp() extends HttpError {
  val statusCode: StatusCode = NotFound
  val code: String = "MappingNotFoundError"
  val message: String = "MappingNotFoundError"
}

case class ClientServiceErrorHttp() extends HttpError {
  override val statusCode: StatusCode = BadRequest
  override val code: String = "badRequest"
  override val message: String = "MappingNotFoundError"
}

case class InternalErrorHttp(message: String) extends HttpError {
  override val statusCode: StatusCode = InternalServerError
  override val code: String = "internalError"
}
