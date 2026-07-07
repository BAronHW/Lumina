package Domain

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import com.example.lumina.types.given

import java.time.OffsetDateTime
import java.util.UUID

case class Session(
    id: UUID,
    agentId: UUID,
    name: String,
    createdAt: OffsetDateTime,
    endedAt: Option[OffsetDateTime]
)

object Session {
  given Encoder[Session] = deriveEncoder[Session]
  given Decoder[Session] = deriveDecoder[Session]
}
