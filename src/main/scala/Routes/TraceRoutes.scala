package Routes

import Domain.Trace
import Domain.Trace.given
import cats.effect.Concurrent
import cats.syntax.all.*
import com.example.lumina.services.TraceService
import com.example.lumina.types.SpanStatus
import com.example.lumina.types.given
import io.circe.generic.auto.*
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import skunk.data.Completion

import java.time.OffsetDateTime
import java.util.UUID

object TraceRoutes {
  private case class CreateTraceRequest(
      agentId: UUID,
      name: String,
      status: SpanStatus,
      startedAt: OffsetDateTime,
      endedAt: Option[OffsetDateTime],
      totalCostUsd: Option[BigDecimal],
      tags: Map[String, String]
  )

  private case class EditTraceRequest(
      id: UUID,
      agentId: UUID,
      name: String,
      status: SpanStatus,
      startedAt: OffsetDateTime,
      endedAt: Option[OffsetDateTime],
      totalCostUsd: Option[BigDecimal],
      tags: Map[String, String]
  )
  def traceRoutes[F[_]: Concurrent](traceService: TraceService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root / "traces" / UUIDVar(traceId) =>
        traceService.selectTrace(traceId).flatMap {
          case Some(trace) => Ok(trace)
          case None        => NotFound()
        }

      case req @ POST -> Root / "traces" =>
        for {
          body <- req.as[CreateTraceRequest]
          res <- traceService.createTrace(
            Trace(
              UUID.randomUUID(),
              body.agentId,
              body.name,
              body.status,
              body.startedAt,
              body.endedAt,
              body.totalCostUsd,
              body.tags
            )
          )
          resp <- Created(res.toString)
        } yield resp

      case DELETE -> Root / "traces" / UUIDVar(traceId) =>
        traceService.deleteTrace(traceId).flatMap {
          case Completion.Delete(n) if n > 0 => Ok()
          case _                             => NotFound()
        }

      case req @ PUT -> Root / "traces" / UUIDVar(traceId) =>
        for {
          body <- req.as[CreateTraceRequest]
          res <- traceService.updateTrace(
            Trace(
              traceId,
              body.agentId,
              body.name,
              body.status,
              body.startedAt,
              body.endedAt,
              body.totalCostUsd,
              body.tags
            )
          )
          resp <- Ok(res.toString)
        } yield resp

      case req @ POST -> Root / "traces" / "batch" =>
        for {
          body <- req.as[List[CreateTraceRequest]]
          res <- traceService.batchCreateTrace(createTraceRequestToTrace(body))
          resp <- Created(res.toString)
        } yield resp

      case req @ PUT -> Root / "traces" / "batch" =>
        for {
          body <- req.as[List[EditTraceRequest]]
          res <- traceService.batchUpdateTraces(editTraceRequestToTrace(body))
          resp <- Ok(res.toString)
        } yield resp
    }
  }

  private def editTraceRequestToTrace(editTraceRequests: List[EditTraceRequest]): List[Trace] =
    editTraceRequests.map { t =>
      Trace(t.id, t.agentId, t.name, t.status, t.startedAt, t.endedAt, t.totalCostUsd, t.tags)
    }

  private def createTraceRequestToTrace(createTraceRequests: List[CreateTraceRequest]): List[Trace] = {
    createTraceRequests.map { trace =>
      Trace(
        id = UUID.randomUUID(),
        agentId = trace.agentId,
        name = trace.name,
        status = trace.status,
        startedAt = trace.startedAt,
        endedAt = trace.endedAt,
        totalCostUsd = trace.totalCostUsd,
        tags = trace.tags
      )
    }
  }
}
