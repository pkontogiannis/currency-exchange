package com.landoop.service.errors

import com.landoop.service.domain.Conversion

sealed trait ServiceError

object ServiceError {

  case class NotAvailableMapping(conversion: Conversion) extends ServiceError

  case class ClientServiceError(message: String) extends ServiceError

  val httpErrorMapper: PartialFunction[ServiceError, HttpError] = {
    case NotAvailableMapping(conversion) =>
      new MappingNotFoundErrorHttp {
        override val code: String = "mappingNotFound"
        override val message: String = s"There is no available mapping from ${conversion.fromCurrency} to ${conversion.toCurrency}"
      }
    case ClientServiceError(msg) =>
      new ClientServiceErrorHttp {
        override val code: String = "mappingNotFound"
        override val message: String = s"$msg"
      }
  }
}
