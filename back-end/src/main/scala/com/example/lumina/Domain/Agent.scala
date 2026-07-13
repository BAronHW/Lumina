package com.example.lumina.Domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import java.util.UUID

case class Agent(
    id: UUID,
    deploymentId: UUID,
    name: String
)

object Agent {
  given Encoder[Agent] = deriveEncoder[Agent]
  given Decoder[Agent] = deriveDecoder[Agent]
}
