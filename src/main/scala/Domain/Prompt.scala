package Domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.util.UUID

case class Prompt(
    Id: UUID,
    Name: String,
    Prompt: String
)

object Prompt {
  given Encoder[Prompt] = deriveEncoder[Prompt]
  given Decoder[Prompt] = deriveDecoder[Prompt]
}
