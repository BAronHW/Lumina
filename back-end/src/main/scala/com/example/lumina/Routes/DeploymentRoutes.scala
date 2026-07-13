package com.example.lumina.Routes

import com.example.lumina.Domain.Deployment.given
import cats.effect.Concurrent
import cats.syntax.all.*
import com.example.lumina.Domain.{Deployment, Pagination}
import com.example.lumina.services.DeploymentService
import io.circe.{Decoder, Encoder}
import org.http4s.HttpRoutes
import skunk.data.Completion
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import io.circe.generic.auto.*

object DeploymentRoutes:

  private case class CreateDeploymentRequest(name: String)
  private case class UpdateDeploymentRequest(name: String)

  def deploymentRoutes[F[_]: Concurrent](service: DeploymentService[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    object PageMatcher extends QueryParamDecoderMatcher[Int]("page")
    object PageSizeMatcher extends OptionalQueryParamDecoderMatcher[Int]("pageSize")
    HttpRoutes.of[F] {
      case GET -> Root / "deployments" / UUIDVar(id) =>
        service.getDeploymentById(id).flatMap {
          case Some(deployment) => Ok(deployment)
          case None             => NotFound()
        }

      case req @ POST -> Root / "deployments" =>
        for {
          body <- req.as[CreateDeploymentRequest]
          deployment <- service.registerDeployment(body.name)
          resp <- Created(deployment)
        } yield resp

      case req @ PUT -> Root / "deployments" / UUIDVar(id) =>
        for {
          body <- req.as[UpdateDeploymentRequest]
          result <- service.updateDeploymentById(id, body.name)
          resp <- result match {
            case Completion.Update(n) if n > 0 => Ok()
            case _                             => NotFound()
          }
        } yield resp

      case DELETE -> Root / "deployments" / UUIDVar(id) =>
        service.removeDeploymentById(id).flatMap {
          case Completion.Delete(n) if n > 0 => Ok()
          case _                             => NotFound()
        }

      case GET -> Root / "deployments" :? PageMatcher(page) +& PageSizeMatcher(pageSize) =>
        for {
          result <- service.getAllDeployment(Pagination(page, pageSize.getOrElse(20)))
          resp <- Ok(result)
        } yield resp
    }
