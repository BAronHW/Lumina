package com.example.lumina.Routes

import com.example.lumina.Domain.Prompt.given
import cats.effect.Concurrent
import cats.syntax.all.*
import com.example.lumina.Domain.Prompt
import com.example.lumina.services.PromptService
import io.circe.generic.auto.*
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import skunk.data.Completion

object PromptRoutes {

  private case class CreatePromptRequest(name: String, content: String)
  private case class UpdatePromptRequest(name: String, content: String)

  def promptRoutes[F[_]: Concurrent](promptService: PromptService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root / "prompt" / UUIDVar(id) =>
        promptService.getPrompt(id).flatMap {
          case Some(prompt) => Ok(prompt)
          case None         => NotFound()
        }

      case req @ POST -> Root / "prompt" =>
        for {
          body <- req.as[CreatePromptRequest]
          prompt <- promptService.createPrompt(body.name, body.content)
          resp <- Created(prompt)
        } yield resp

      case req @ PUT -> Root / "prompt" / UUIDVar(id) =>
        for {
          body <- req.as[UpdatePromptRequest]
          result <- promptService.updatePrompt(Prompt(id, body.name, body.content))
          resp <- result match {
            case Completion.Update(n) if n > 0 => Ok()
            case _                             => NotFound()
          }
        } yield resp

      case DELETE -> Root / "prompt" / UUIDVar(id) =>
        promptService.deletePrompt(id).flatMap {
          case Completion.Delete(n) if n > 0 => Ok()
          case _                             => NotFound()
        }
    }
  }
}
