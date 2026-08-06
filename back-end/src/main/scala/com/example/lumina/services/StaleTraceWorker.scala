package com.example.lumina.services
import cats.syntax.all.*
import cats.effect.Temporal
import org.typelevel.log4cats.Logger
import fs2.Stream
import skunk.data.Completion
import scala.concurrent.duration.FiniteDuration

trait StaleTraceWorker[F[_]] {
  def clearStaleTraces(): Stream[F, Unit]
}

object StaleTraceWorker {
  def impl[F[_]: Temporal](
      traceService: TraceService[F],
      spanService: SpanService[F],
      pollInterval: FiniteDuration,
      logger: Logger[F]
  ): StaleTraceWorker[F] = new StaleTraceWorker[F] {
    override def clearStaleTraces(): Stream[F, Unit] = {
      Stream
        .awakeEvery[F](pollInterval)
        .evalMap(_ =>
          this
            .clearStaleTracesAndSpans()
            .handleErrorWith(err =>
              logger.error(err)("StaleTraceWorker failed") *> Temporal[F].pure(Completion.Update(0))
            )
        )
        .void
    }

    def clearStaleTracesAndSpans(): F[Completion] = {
      traceService.timeoutStaleTraces() *> spanService.timeoutStaleSpan()
    }
  }
}
