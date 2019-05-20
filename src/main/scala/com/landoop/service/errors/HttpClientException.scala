package com.landoop.service.errors

case class HttpClientException(message: String) extends RuntimeException(message)