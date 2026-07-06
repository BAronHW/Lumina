package com.example.lumina.DB

import cats.effect.{Resource, Temporal}
import cats.effect.std.Console
import com.example.lumina.types.Config
import fs2.io.net.Network
import org.typelevel.otel4s.trace.Tracer
import org.typelevel.otel4s.metrics.Meter
import skunk.{Session, TypingStrategy}

object DataBaseConnection {

  def pooled[F[_]: Temporal: Tracer: Meter: Network: Console](config: Config): Resource[F, Resource[F, Session[F]]] =
    Session
      .Builder[F]
      .withHost(config.host)
      .withPort(config.port)
      .withUserAndPassword(config.username, config.password)
      .withDatabase(config.database)
      .withTypingStrategy(TypingStrategy.SearchPath)
      .pooled(max = 10)
}
