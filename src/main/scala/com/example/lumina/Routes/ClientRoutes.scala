package com.example.lumina.Routes

import com.example.lumina.Domain.Client.given
import cats.effect.Concurrent
import cats.syntax.all.*
import com.example.lumina.Domain.Client
import com.example.lumina.services.ClientService
import io.circe.{Decoder, Encoder}
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import io.circe.generic.auto.*

object ClientRoutes:

  private case class CreateClientRequest(name: String)
  private case class UpdateClientRequest(name: String)

  def clientRoutes[F[_]: Concurrent](service: ClientService[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*
    HttpRoutes.of[F] {
      case GET -> Root / "clients" / UUIDVar(id) =>
        service.getClientById(id).flatMap {
          case Some(client) => Ok(client)
          case None         => NotFound()
        }

      case req @ POST -> Root / "clients" =>
        for {
          body   <- req.as[CreateClientRequest]
          client <- service.registerClient(body.name)
          resp   <- Created(client)
        } yield resp

      case req @ PUT -> Root / "clients" / UUIDVar(id) =>
        for {
          body <- req.as[UpdateClientRequest]
          result <- service.updateClientById(id, body.name)
          resp <- Ok(result.toString)
        } yield resp

      case DELETE -> Root / "clients" / UUIDVar(id) =>
        for {
          result <- service.removeClientById(id)
          resp <- Ok(result.toString)
        } yield resp

      case GET -> Root / "clients" =>
        for {
          result <- service.getAllClient
          resp <- Ok(result)
        } yield resp
    }
