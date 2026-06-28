package com.example.lumina.repository

import Domain.Span
import cats.effect.{Concurrent, Resource}
import com.example.lumina.types.{SpanKind, SpanStatus}
import skunk.*
import skunk.data.{Completion, Type}
import skunk.implicits.*
import skunk.codec.all.*
import skunk.circe.codec.all.jsonb
import cats.syntax.all.*
import cats.Monad.*

import java.util.UUID

class SpanRepository[F[_]: Concurrent](session: Resource[F, Session[F]]) {
  def createSpan(spanBody: Span): F[Completion] = {
    session.use { s =>
      s.prepare(SpanRepositoryQueries.createSpan).flatMap(ps => ps.execute(spanBody))
    }
  }

  def getSpanById(spanId: UUID): F[Option[Span]] = {
    session.use { s =>
      s.prepare(SpanRepositoryQueries.selectSpanById).flatMap(pq => pq.option(spanId))
    }
  }

  def deleteSpanById(spanId: UUID): F[Completion] = {
    session.use { s =>
      s.prepare(SpanRepositoryQueries.deleteSpanById).flatMap(pq => pq.execute(spanId))
    }
  }

  def updateSpanById(spanBody: Span): F[Completion] = {
    session.use { s =>
      s.prepare(SpanRepositoryQueries.updateSpanById).flatMap(pg => pg.execute(spanBody))
    }
  }

  def createBatchSpan(spanList: List[Span]): F[Completion] = {
    session.use { s =>
      s.prepare(SpanRepositoryQueries.createBatchSpan(spanList)).flatMap(pg => pg.execute(spanList))
    }
  }
}

private object SpanRepositoryQueries {
  private val spanKindCodec: Codec[SpanKind] =
    varchar.imap(SpanKind.valueOf)(_.toString)

  private val spanStatusCodec: Codec[SpanStatus] =
    `enum`(
      (s: SpanStatus) => s.toString.toLowerCase,
      (s: String) => SpanStatus.values.find(_.toString.toLowerCase == s),
      Type("span_status")
    )

  private val spanCodec: Codec[Span] =
    (uuid *: uuid *: uuid.opt *: varchar *: spanKindCodec *: spanStatusCodec *:
      text.opt *: timestamptz *: timestamptz.opt *: int4.opt *:
      jsonb *: jsonb *: jsonb).to[Span]

  val createSpan: Command[Span] =
    sql"INSERT INTO span VALUES ${spanCodec.values}".command

  def createBatchSpan(spanList: List[Span]): Command[spanList.type] = {
    val enc = spanCodec.values.list(spanList)
    sql"INSERT INTO span VALUES $enc".command
  }

  val selectSpanById: Query[UUID, Span] = {
    sql"SELECT * FROM span WHERE id = $uuid".query(spanCodec)
  }

  val deleteSpanById: Command[UUID] = {
    sql"DELETE FROM span WHERE id = $uuid".command
  }

  val updateSpanById: Command[Span] =
    sql"""UPDATE span SET
            trace_id = $uuid, parent_span_id = ${uuid.opt}, name = $varchar,
            kind = $spanKindCodec, status = $spanStatusCodec, error = ${text.opt},
            started_at = $timestamptz, ended_at = ${timestamptz.opt},
            duration_ms = ${int4.opt}, input = $jsonb, output = $jsonb,
            attributes = $jsonb
          WHERE id = $uuid""".command.contramap[Span] { s =>
      s.traceId *: s.parentSpanId *: s.name *: s.kind *: s.status *:
        s.error *: s.startedAt *: s.endedAt *: s.durationMs *:
        s.input *: s.output *: s.attributes *: s.id *: EmptyTuple
    }
}
