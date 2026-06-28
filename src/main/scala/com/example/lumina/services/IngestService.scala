package com.example.lumina.services

import Domain.Span

trait IngestService[F[_]] {
  def pushSpans(spans: List[Span]): F[Unit]
}

object IngestService {
  def impl[F[_]](ingestBuffer: IngestBuffer[F, Span]): IngestService[F] = new IngestService[F] {
    override def pushSpans(spans: List[Span]): F[Unit] =
      ingestBuffer.enqueue(spans)
  }
}
