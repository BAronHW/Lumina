package com.example.lumina.types

import Domain.Span

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.*

// Circe codecs for Java time types
given Encoder[OffsetDateTime] = Encoder[String].contramap(_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
given Decoder[OffsetDateTime] = Decoder[String].emap { s =>
  try Right(OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
  catch case e: Exception => Left(s"Invalid ISO 8601 datetime: $s")
}

enum SpanKind:
  case Trace, LlmCall, ToolCall, Retrieval, AgentCall, Custom

object SpanKind:
  given Encoder[SpanKind] = Encoder[String].contramap(_.toString)
  given Decoder[SpanKind] = Decoder[String].emap { s =>
    SpanKind.values.find(_.toString == s).toRight(s"Invalid SpanKind: $s")
  }

enum SpanStatus:
  case Ok, Error

object SpanStatus:
  given Encoder[SpanStatus] = Encoder[String].contramap(_.toString.toLowerCase)
  given Decoder[SpanStatus] = Decoder[String].emap {
    case "ok"    => Right(SpanStatus.Ok)
    case "error" => Right(SpanStatus.Error)
    case other   => Left(s"Invalid SpanStatus: $other")
  }

object Span:
  given Encoder[Span] = deriveEncoder[Span]
  given Decoder[Span] = deriveDecoder[Span]

case class CreateSpanRequest(
    spans: List[Span]
)

object CreateSpanRequest:
  given Encoder[CreateSpanRequest] = deriveEncoder[CreateSpanRequest]
  given Decoder[CreateSpanRequest] = deriveDecoder[CreateSpanRequest]
