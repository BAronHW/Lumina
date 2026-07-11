package com.example.lumina.Domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import java.util.UUID

case class Client(
    id: UUID,
    name: String
)

object Client {
  given Encoder[Client] = deriveEncoder[Client]
  given Decoder[Client] = deriveDecoder[Client]
}
