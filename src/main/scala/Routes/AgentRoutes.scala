package Routes

import Domain.Agent
import Domain.Agent.given
import cats.effect.Concurrent
import cats.syntax.all.*
import com.example.lumina.services.AgentService
import io.circe.generic.auto.*
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import skunk.data.Completion

import java.util.UUID

object AgentRoutes:

  private case class CreateAgentRequest(clientId: UUID, name: String)
  private case class UpdateAgentRequest(name: String)

  def agentRoutes[F[_]: Concurrent](service: AgentService[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*
    HttpRoutes.of[F] {
      case GET -> Root / "agents" / UUIDVar(id) =>
        service.getAgentById(id).flatMap {
          case Some(agent) => Ok(agent)
          case None        => NotFound()
        }

      case GET -> Root / "clients" / UUIDVar(clientId) / "agents" =>
        service.getAgentsByClientId(clientId).flatMap(agents => Ok(agents))

      case req @ POST -> Root / "agents" =>
        for {
          body   <- req.as[CreateAgentRequest]
          result <- service.createAgent(body.clientId, body.name)
          resp   <- result match {
            case Completion.Insert(n) if n > 0 => Created()
            case _                             => InternalServerError()
          }
        } yield resp

      case req @ PUT -> Root / "agents" / UUIDVar(id) =>
        for {
          body   <- req.as[UpdateAgentRequest]
          result <- service.updateAgent(id, body.name)
          resp   <- result match {
            case Completion.Update(n) if n > 0 => Ok()
            case _                             => NotFound()
          }
        } yield resp

      case DELETE -> Root / "agents" / UUIDVar(id) =>
        service.deleteAgent(id).flatMap {
          case Completion.Delete(n) if n > 0 => Ok()
          case _                             => NotFound()
        }
    }
