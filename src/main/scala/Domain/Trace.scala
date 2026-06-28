package Domain

import com.example.lumina.types.SpanStatus
import com.example.lumina.types.given
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.OffsetDateTime
import java.util.UUID

case class Trace(
    id: UUID,
    agentId: UUID,
    name: String,
    status: SpanStatus,
    startedAt: OffsetDateTime,
    endedAt: Option[OffsetDateTime],
    totalCostUsd: Option[BigDecimal],
    tags: Map[String, String]
)

object Trace {
  given Encoder[Trace] = deriveEncoder[Trace]
  given Decoder[Trace] = deriveDecoder[Trace]
}
