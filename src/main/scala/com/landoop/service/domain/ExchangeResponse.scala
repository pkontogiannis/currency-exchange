package com.landoop.service.domain

import java.time.LocalDate

import io.circe.Decoder.Result
import io.circe._

case class ExchangeResponse(base: String, rates: Map[String, BigDecimal], date: LocalDate)

object ExchangeResponse {

  implicit def jsonDecoder: Decoder[ExchangeResponse] = Decoder.instance { h =>
    (for {
      keys <- h.keys
      _ <- keys.dropWhile(_ == "base").headOption
    } yield {
      for {
        base <- h.get[String]("base")
        rates <- h.get[Map[String, BigDecimal]]("rates")
        date <- h.get[LocalDate]("date")
      } yield ExchangeResponse(base, rates, date)
    }).getOrElse(Left(DecodingFailure("Not a valid KeyValueRow", List())))
  }

  implicit val dateFormat: Encoder[LocalDate] with Decoder[LocalDate] = new Encoder[LocalDate] with Decoder[LocalDate] {
    override def apply(a: LocalDate): Json = Encoder.encodeLocalDate.apply(a)

    override def apply(c: HCursor): Result[LocalDate] = Decoder.decodeLocalDate.map(ld => LocalDate.of(ld.getYear, ld.getMonth, ld.getDayOfMonth))(c)
  }

}
