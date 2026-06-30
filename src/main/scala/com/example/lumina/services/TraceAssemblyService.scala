package com.example.lumina.services

import Domain.Span

trait TraceAssemblyService[F[_]] {
  def processSpans(spans: List[Span]): F[Boolean]
  def flush: F[Boolean]
}

object TraceAssemblyService {
  def impl[F[_]](ingestBuffer: IngestBuffer[F, Span]): TraceAssemblyService[F] = new TraceAssemblyService[F] {
    override def processSpans(spans: List[Span]): F[Boolean] = ???

    override def flush: F[Boolean] = {
      ingestBuffer.flushAll
    }
  }
}
