package com.example.lumina
import Routes.LuminaRoutes
import cats.effect.{Async, Resource}
import cats.effect.std.Console
import com.comcast.ip4s.*
import com.example.lumina.DB.DataBaseConnection
import com.example.lumina.repository.ClientRepository
import com.example.lumina.services.ClientService
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
      logger = LoggerFactory[F].getLogger
      clientRepository = new ClientRepository(pooled)
      clientService = ClientService.impl[F](clientRepository, logger)
      client <- EmberClientBuilder.default[F].build

      httpApp = (
        LuminaRoutes.clientRoutes[F](clientService)
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
