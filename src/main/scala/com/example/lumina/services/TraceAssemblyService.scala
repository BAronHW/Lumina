package com.example.lumina.services

import Domain.Span
import cats.effect.Concurrent
import cats.syntax.all.*
import org.typelevel.log4cats.Logger
import skunk.data.Completion

trait TraceAssemblyService[F[_]] {
  def processSpans(chunksToTake: Int): F[Boolean]
  def flush: F[Boolean]
}

object TraceAssemblyService {
  def impl[F[_]: Concurrent](
      ingestBuffer: IngestBuffer[F, Span],
      spanService: SpanService[F],
      traceService: TraceService[F],
      logger: Logger[F]
  ): TraceAssemblyService[F] = new TraceAssemblyService[F] {

    /** This function will take spans contained in the ingest buffer and then generate them using the span service. It
      * will also use the trace service to group spans together when the final span arrives which is denoted if a span
      * has an ended at field that is not null.
      */
    override def processSpans(chunksToTake: Int): F[Boolean] =
      for {
        items <- ingestBuffer.tryTakeN(chunksToTake)
        _ <- logger.info(s"Processing ${items.size} spans from queue")
        result <-
          if (items.isEmpty) Concurrent[F].pure(false)
          else spanService.createBatchSpan(items) *> updateCompletedTrace(items)
      } yield result

    /** This function flushes the ingest buffer so that all spans that were in the buffer will now be removed. The
      * function will return true if the buffer is flushed and will return false if it cannot be flushed or if the
      * buffer originally had nothing to flush.
      */
    override def flush: F[Boolean] =
      logger.info("Flushing ingest buffer") *> ingestBuffer.flushAll

    /** Converts a skunk completion type into boolean by checking if Postgres completion has inserted greater than 0
      * rows
      */
    def completionToBool(res: Completion): Boolean =
      res match {
        case Completion.Insert(n) => n > 0
        case _                    => false
      }

    /** Given a list of spans this function filters all spans so that only spans that have a defined endedAt field
      * remain. We then group these remaining spans by their TraceId's and then we get the keys which are the traceId
      * and then turn them into a list
      */
    def updateCompletedTrace(spanList: List[Span]): F[Boolean] = {
      val traceIdList = spanList
        .filter(span => span.endedAt.isDefined)
        .groupBy(span => span.traceId)
        .keys
        .toList

      logger.info(s"Updating ${traceIdList.size} completed traces") *>
        traceService.updateBatchTracesWithId(traceIdList).map(res => completionToBool(res))
    }
  }
}
