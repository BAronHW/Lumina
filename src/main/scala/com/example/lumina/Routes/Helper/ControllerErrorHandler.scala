package com.example.lumina.Routes.Helper

import cats.data.{Kleisli, OptionT}
import cats.effect.Concurrent
import cats.syntax.all.*
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.dsl.Http4sDsl
import skunk.exception.PostgresErrorException

object ControllerErrorHandler {

  def handlePostgresError[F[_]: Concurrent](error: PostgresErrorException): F[Response[F]] = {
    val dsl = new Http4sDsl[F] {}
    import dsl.*
    error.code match {
      case "23503" => UnprocessableContent("Referenced resource does not exist")
      case "23505" => Conflict(s"Already exists: ${error.constraintName.getOrElse("")}")
      case "22P02" => BadRequest("Invalid field value")
      case _       => InternalServerError(error.message)
    }
  }

  def handleRouteErrors[F[_]: Concurrent](routes: HttpRoutes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl.*
    Kleisli { (req: Request[F]) =>
      OptionT(
        routes.run(req).value.handleErrorWith {
          case e: PostgresErrorException => handlePostgresError(e).map(Some(_))
          case _                         => InternalServerError().map(Some(_))
        }
      )
    }
  }
}
