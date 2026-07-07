package com.example.lumina.services

import cats.Monad
import cats.syntax.all.*
import com.example.lumina.Domain.Span
import org.typelevel.log4cats.Logger

trait IngestService[F[_]] {
  def pushSpans(spans: List[Span]): F[Unit]
}

object IngestService {
  def impl[F[_]: Monad](ingestBuffer: IngestBuffer[F, Span], logger: Logger[F]): IngestService[F] =
    new IngestService[F] {
      override def pushSpans(spans: List[Span]): F[Unit] =
        logger.info(s"Pushing ${spans.size} spans to queue") *> ingestBuffer.enqueue(spans)
    }
}
