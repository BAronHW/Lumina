package com.example.lumina
import Domain.Span
import Routes.{ClientRoutes, IngestRoutes, PromptRoutes, TraceRoutes}
import cats.syntax.semigroupk.*
import cats.effect.{Async, Resource}
import cats.effect.std.{Console, Queue}
import com.comcast.ip4s.*
import com.example.lumina.DB.DataBaseConnection
import com.example.lumina.repository.{ClientRepository, PromptRepository, TraceRepository}
import com.example.lumina.services.{ClientService, IngestBuffer, IngestService, PromptService, TraceService}
import com.example.lumina.types.Config
import fs2.io.net.Network
import org.http4s.ember.client.EmberClientBuilder
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
      pooled <- DataBaseConnection.pooled(conf)
      queue <- Resource.eval(Queue.bounded[F, Span](256))
      logger = LoggerFactory[F].getLogger
      ingestBuffer = new IngestBuffer[F, Span](queue)
      clientRepository = new ClientRepository(pooled)
      promptRepository = new PromptRepository[F](pooled)
      traceRepository = new TraceRepository[F](pooled)
      clientService = ClientService.impl[F](clientRepository, logger)
      ingestService = IngestService.impl[F](ingestBuffer)
      promptService = PromptService.impl[F](promptRepository)
      traceService = TraceService.impl[F](traceRepository)
      client <- EmberClientBuilder.default[F].build

      httpApp = (
        ClientRoutes.clientRoutes[F](clientService) <+>
          IngestRoutes.ingestRoutes[F](ingestService) <+>
          PromptRoutes.promptRoutes[F](promptService) <+>
          TraceRoutes.traceRoutes[F](traceService)
      ).orNotFound

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
