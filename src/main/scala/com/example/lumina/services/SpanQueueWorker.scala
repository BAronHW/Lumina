package com.example.lumina.services
import Domain.Span
import fs2.Stream

import scala.concurrent.duration.DurationInt

trait SpanQueueWorker {
  def initWorker[F[_]]: Stream[F, Unit]
}

/** This span queue worker creates a stream that periodically polls the ingest buffer and creates spans
  */
object SpanQueueWorker {
  def impl[F[_]](traceAssemblyService: TraceAssemblyService[F]): SpanQueueWorker =
    new SpanQueueWorker {
      override def initWorker[F[_]]: Stream[F, Unit] = {
        Stream
          .awakeEvery[F](200.millis)
          .evalMap(_ => traceAssemblyService.processSpans())
      }
    }
}
