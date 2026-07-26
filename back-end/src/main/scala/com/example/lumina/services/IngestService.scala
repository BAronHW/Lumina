package com.example.lumina.services

import cats.Monad
import cats.syntax.all.*
import com.example.lumina.types.CreateSpanRequest
import org.typelevel.log4cats.Logger

trait IngestService[F[_]] {
  def pushSpans(spans: List[CreateSpanRequest]): F[Unit]
}

object IngestService {
  def impl[F[_]: Monad](ingestBuffer: IngestBuffer[F, CreateSpanRequest], logger: Logger[F]): IngestService[F] =
    new IngestService[F] {
      override def pushSpans(spans: List[CreateSpanRequest]): F[Unit] =
        logger.info(s"Pushing ${spans.size} spans to queue") *> ingestBuffer.enqueue(spans)
    }
}
