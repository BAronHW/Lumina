package com.example.lumina.services

import Domain.Span
import cats.effect.Concurrent
import cats.syntax.all.*
import skunk.data.Completion

import java.util.UUID

trait TraceAssemblyService[F[_]] {
  def processSpans(spans: List[Span], chunksToTake: Int): F[Boolean]
  def flush: F[Boolean]
}

object TraceAssemblyService {
  def impl[F[_]: Concurrent](
      ingestBuffer: IngestBuffer[F, Span],
      spanService: SpanService[F],
      traceService: TraceService[F]
  ): TraceAssemblyService[F] = new TraceAssemblyService[F] {

    /** This function will take spans contained in the ingest buffer and then generate them using the span service. It
      * will also use the trace service to group spans together when the final span arrives which is denoted if a span
      * has an ended at field that is not null.
      */
    override def processSpans(spans: List[Span], chunksToTake: Int): F[Boolean] = {
      for {
        items <- ingestBuffer.tryTakeN(chunksToTake)
        completion <- spanService.createBatchSpan(items)

      } yield () // WIP
    }

    /** This function flushes the ingest buffer so that all spans that were in the buffer will now be removed. The
      * function will return true if the buffer is flushed and will return false if it cannot be flushed or if the
      * buffer originally had nothing to flush.
      */
    override def flush: F[Boolean] = {
      ingestBuffer.flushAll
    }

    /** Converts a skunk completion type into boolean by checking if Postgres completion has inserted greater than 0
      * rows
      */
    def completionToBool(res: Completion): Boolean = {
      res match {
        case Completion.Insert(n) => n > 0
        case _                    => false
      }
    }

    /** Finds all root spans (parentSpanId = None, endedAt = Some) in the given span list. For each root span, fetches
      * its corresponding Trace record from the DB and updates it with the root span's endedAt and status, marking the
      * trace as complete. Traces are created explicitly via the SDK before spans arrive, so this only updates existing
      * records.
      */
    def createTraceGroup(spanList: List[Span]): Map[UUID, List[Span]] = {
      spanList
        .filter(span => span.endedAt.isDefined)
        .groupBy(span => span.traceId)
    }

    def updateCompletedTrace(spanList: List[Span]) = {
      val traceIdMap = spanList
        .filter(span => span.endedAt.isDefined)
        .groupBy(span => span.traceId)

      val traceIdList = traceIdMap.keys.toList

      traceService.createTrace()
    }
  }
}
