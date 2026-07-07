package com.example.lumina.Routes

import cats.effect.Concurrent
import cats.syntax.all.*
import com.example.lumina.Domain.Session
import com.example.lumina.services.SessionService
import io.circe.generic.auto.*
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import skunk.data.Completion

import java.util.UUID

object SessionRoutes:

  private case class CreateSessionRequest(agentId: UUID, name: String)

  def sessionRoutes[F[_]: Concurrent](service: SessionService[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root / "sessions" / UUIDVar(id) =>
        service.getSessionById(id).flatMap {
          case Some(session) => Ok(session)
          case None          => NotFound()
        }

      case GET -> Root / "agents" / UUIDVar(agentId) / "sessions" =>
        service.getSessionsByAgentId(agentId).flatMap(sessions => Ok(sessions))

      case req @ POST -> Root / "sessions" =>
        for {
          body <- req.as[CreateSessionRequest]
          result <- service.createSession(body.agentId, body.name)
          resp <- result match {
            case Completion.Insert(n) if n > 0 => Created()
            case _                             => InternalServerError()
          }
        } yield resp

      case PUT -> Root / "sessions" / UUIDVar(id) / "end" =>
        service.endSession(id).flatMap {
          case Completion.Update(n) if n > 0 => Ok()
          case _                             => NotFound()
        }

      case DELETE -> Root / "sessions" / UUIDVar(id) =>
        service.deleteSession(id).flatMap {
          case Completion.Delete(n) if n > 0 => Ok()
          case _                             => NotFound()
        }
    }
