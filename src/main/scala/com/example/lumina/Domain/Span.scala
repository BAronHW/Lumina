package com.example.lumina.Domain

import io.circe.{Json, Encoder, Decoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import com.example.lumina.types.{SpanKind, SpanStatus}
import com.example.lumina.types.given

import java.time.OffsetDateTime
import java.util.UUID

case class Span(
    id: UUID,
    traceId: UUID,
    parentSpanId: Option[UUID],
    name: String,
    kind: SpanKind,
    status: SpanStatus,
    error: Option[String],
    startedAt: OffsetDateTime,
    endedAt: Option[OffsetDateTime],
    durationMs: Option[Int],
    input: Json,
    output: Json,
    attributes: Json
)

object Span {
  given Encoder[Span] = deriveEncoder[Span]
  given Decoder[Span] = deriveDecoder[Span]
}
