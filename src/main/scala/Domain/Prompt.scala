package Domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.util.UUID

case class Prompt(
    id: UUID,
    name: String,
    content: String
)

object Prompt {
  given Encoder[Prompt] = deriveEncoder[Prompt]
  given Decoder[Prompt] = deriveDecoder[Prompt]
}
