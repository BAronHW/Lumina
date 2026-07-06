package com.example.lumina.services

import cats.effect.Temporal
import cats.effect.std.Console
import cats.syntax.all.*
import com.example.lumina.types.WorkerConfig
import fs2.Stream

import scala.concurrent.duration.*

trait SpanQueueWorker[F[_]] {
  def stream: Stream[F, Unit]
}

/** This span queue worker creates a stream that periodically polls the ingest buffer and creates spans
  */
object SpanQueueWorker {
  def impl[F[_]: Temporal: Console](
      traceAssemblyService: TraceAssemblyService[F],
      workerConfig: WorkerConfig
  ): SpanQueueWorker[F] = new SpanQueueWorker[F] {
    override def stream: Stream[F, Unit] =
      Stream
        .awakeEvery[F](workerConfig.pollInterval.millis)
        .evalTap(_ => Console[F].println("POLLING HERE"))
        .evalMap(_ => traceAssemblyService.processSpans(workerConfig.chunkSize))
        .void
  }
}
