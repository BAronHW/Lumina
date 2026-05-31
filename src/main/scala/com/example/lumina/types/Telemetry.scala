package com.example.lumina.types

import java.time.OffsetDateTime
import io.circe.{Json, Encoder, Decoder}
import io.circe.generic.semiauto.*

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

case class Span(
    spanId: String,
    traceId: String,
    parentSpanId: Option[String],
    name: String,
    kind: SpanKind,
    startedAt: OffsetDateTime,
    endedAt: OffsetDateTime,
    status: SpanStatus,
    error: Option[String],
    input: Json,
    output: Json,
    attributes: Json,
    agentId: String,
    tags: Map[String, String]
)

object Span:
  given Encoder[Span] = deriveEncoder[Span]
  given Decoder[Span] = deriveDecoder[Span]

case class Trace(
    traceId: String,
    name: String,
    agentId: String,
    status: SpanStatus,
    startedAt: OffsetDateTime,
    endedAt: OffsetDateTime,
    totalCostUsd: Option[BigDecimal],
    tags: Map[String, String],
    spans: List[Span]
)

object Trace:
  given Encoder[Trace] = deriveEncoder[Trace]
  given Decoder[Trace] = deriveDecoder[Trace]
