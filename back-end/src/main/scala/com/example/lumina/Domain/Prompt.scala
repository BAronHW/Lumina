package com.example.lumina.Domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.util.UUID

case class Prompt(
    id: UUID,
    name: String,
    prompt: String
)

case class PromptsPage(items: List[Prompt], total: Long)

object Prompt {
  given Encoder[Prompt] = deriveEncoder[Prompt]
  given Decoder[Prompt] = deriveDecoder[Prompt]
}

object PromptsPage {
  given Encoder[PromptsPage] = deriveEncoder[PromptsPage]
}
