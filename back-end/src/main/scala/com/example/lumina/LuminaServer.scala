package com.example.lumina
import Domain.Span
import Routes.{AgentRoutes, DeploymentRoutes, IngestRoutes, PromptRoutes, SessionRoutes, SpanRoutes, TraceRoutes}
import Routes.Helper.ControllerErrorHandler
import cats.syntax.semigroupk.*
import cats.effect.{Async, Resource}
import cats.effect.syntax.all.*
import cats.effect.std.{Console, Queue}
import com.comcast.ip4s.*
import com.example.lumina.DB.DataBaseConnection
import com.example.lumina.repository.{
  AgentRepository,
  DeploymentRepository,
  PromptRepository,
  SessionRepository,
  SpanRepository,
  TraceRepository
}
import com.example.lumina.services.{
  AgentService,
  DeploymentService,
  IngestBuffer,
  IngestService,
  PromptService,
  SessionService,
  SpanQueueWorker,
  SpanService,
  TraceAssemblyService,
  TraceService
}
import com.example.lumina.types.{Config, WorkerConfig}
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import pureconfig.ConfigSource
import org.typelevel.otel4s.trace.Tracer.Implicits.noop
import org.typelevel.otel4s.metrics.Meter.Implicits.noop

object LuminaServer:
  def run[F[_]: Async: Network: Console]: F[Nothing] = {
    implicit val logging: LoggerFactory[F] = Slf4jFactory.create[F]
    for {
      conf <- Resource.eval(
        Async[F].fromEither(
          ConfigSource.default.at("db").load[Config].left.map(e => new RuntimeException(e.prettyPrint()))
        )
      )
      workerConf <- Resource.eval(
        Async[F].fromEither(
          ConfigSource.default
            .at("lumina.worker")
            .load[WorkerConfig]
            .left
            .map(e => new RuntimeException(e.prettyPrint()))
        )
      )
      pooled <- DataBaseConnection.pooled(conf)
      queue <- Resource.eval(Queue.bounded[F, Span](256))
      logger = LoggerFactory[F].getLogger
      ingestBuffer = new IngestBuffer[F, Span](queue)
      deploymentRepository = new DeploymentRepository(pooled)
      promptRepository = new PromptRepository[F](pooled)
      traceRepository = new TraceRepository[F](pooled)
      spanRepository = new SpanRepository[F](pooled)
      spanService = SpanService.impl[F](spanRepository, logger)
      traceService = TraceService.impl[F](traceRepository, logger)
      traceAssemblyService = TraceAssemblyService.impl[F](
        ingestBuffer = ingestBuffer,
        spanService = spanService,
        traceService = traceService,
        logger = logger
      )
      agentRepository = new AgentRepository[F](pooled)
      agentService = AgentService.impl[F](agentRepository, logger)
      sessionRepository = new SessionRepository[F](pooled)
      sessionService = SessionService.impl[F](sessionRepository, logger)
      deploymentService = DeploymentService.impl[F](deploymentRepository, logger)
      ingestService = IngestService.impl[F](ingestBuffer, logger)
      promptService = PromptService.impl[F](promptRepository, logger)
      spanQueueWorker = SpanQueueWorker.impl[F](traceAssemblyService, workerConf)
      _ <- spanQueueWorker.stream.compile.drain.background

      httpApp = ControllerErrorHandler
        .handleRouteErrors(
          AgentRoutes.agentRoutes[F](agentService) <+>
            DeploymentRoutes.deploymentRoutes[F](deploymentService) <+>
            IngestRoutes.ingestRoutes[F](ingestService) <+>
            PromptRoutes.promptRoutes[F](promptService) <+>
            SessionRoutes.sessionRoutes[F](sessionService) <+>
            TraceRoutes.traceRoutes[F](traceService) <+>
            SpanRoutes.spanRoutes[F](spanService)
        )
        .orNotFound

      finalHttpApp = Logger.httpApp(true, true)(httpApp)
      _ <-
        EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
