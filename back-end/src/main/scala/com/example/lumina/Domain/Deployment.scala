package com.example.lumina.Domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import java.util.UUID

case class Deployment(
    id: UUID,
    name: String
)

object Deployment {
  given Encoder[Deployment] = deriveEncoder[Deployment]
  given Decoder[Deployment] = deriveDecoder[Deployment]
}
