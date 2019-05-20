package com.landoop.service.errors

trait ErrorMapper[-FromError <: ServiceError, +ToError <: HttpError] extends (FromError => ToError)