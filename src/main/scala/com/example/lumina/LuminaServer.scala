package com.example.lumina

import cats.effect.{Async, Resource}
import cats.effect.std.Console
import cats.syntax.all.*
import com.comcast.ip4s.*
import com.example.lumina.DB.DataBaseConnection
import com.example.lumina.Resources.LuminaRoutes
import com.example.lumina.services.{HelloWorld, Jokes}
import com.example.lumina.types.Config
import fs2.io.net.Network
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger
import pureconfig.ConfigSource
import org.typelevel.otel4s.trace.Tracer.Implicits.noop
import org.typelevel.otel4s.metrics.Meter.Implicits.noop

object LuminaServer:

  def run[F[_]: Async: Network: Console]: F[Nothing] = {
    for {
      conf <- Resource.eval(
        Async[F].fromEither(
          ConfigSource.default.at("db").load[Config].left.map(e => new RuntimeException(e.prettyPrint()))
        )
      )
      pooled <- DataBaseConnection.pooled(conf)
      client <- EmberClientBuilder.default[F].build
      helloWorldAlg = HelloWorld.impl[F]
      jokeAlg = Jokes.impl[F](client)

      httpApp = (
        LuminaRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
          LuminaRoutes.jokeRoutes[F](jokeAlg)
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
