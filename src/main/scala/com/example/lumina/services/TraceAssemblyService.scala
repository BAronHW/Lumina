package com.example.lumina.services

import Domain.Span
import cats.effect.Concurrent
import cats.syntax.all.*
import skunk.data.Completion

trait TraceAssemblyService[F[_]] {
  def processSpans(spans: List[Span], chunksToTake: Int): F[Boolean]
  def flush: F[Boolean]
}

object TraceAssemblyService {
  def impl[F[_]: Concurrent](
      ingestBuffer: IngestBuffer[F, Span],
      spanService: SpanService[F],
      @annotation.unused traceService: TraceService[F]
  ): TraceAssemblyService[F] = new TraceAssemblyService[F] {

    /** This function will take spans contained in the ingest buffer and then generate them using the span service. It
      * will also use the trace service to group spans together when the final span arrives which is denoted if a span
      * has an ended at field that is not null.
      */
    override def processSpans(spans: List[Span], chunksToTake: Int): F[Boolean] = {
      for {
        items <- ingestBuffer.tryTakeN(chunksToTake)
        res <- spanService.createBatchSpan(items)
      } yield (completionToBool(res)) // WIP
    }

    /** This function flushes the ingest buffer so that all spans that were in the buffer will now be removed. The
      * function will return true if the buffer is flushed and will return false if it cannot be flushed or if the
      * buffer originally had nothing to flush.
      */
    override def flush: F[Boolean] = {
      ingestBuffer.flushAll
    }

    def completionToBool(res: Completion): Boolean = {
      res match {
        case Completion.Insert(n) => n > 0
        case _                    => false
      }
    }
  }
}
