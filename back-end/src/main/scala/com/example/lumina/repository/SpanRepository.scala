package com.example.lumina.repository

import cats.effect.{Concurrent, Resource}
import cats.syntax.all.*
import com.example.lumina.Domain.{Pagination, Span}
import com.example.lumina.types.{SpanKind, SpanStatus}
import skunk.*
import skunk.circe.codec.all.jsonb
import skunk.codec.all.*
import skunk.data.{Completion, Type}
import skunk.implicits.*

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

  def getAllSpans(pagination: Pagination): F[List[Span]] =
    session.use { s =>
      s.prepare(SpanRepositoryQueries.selectAllSpans).flatMap(ps => ps.stream(pagination, 64).compile.toList)
    }

  def timeoutStaleSpan(): F[Completion] =
    session.use { s =>
      s.prepare(SpanRepositoryQueries.timeoutStaleSpan()).flatMap(ps => ps.execute(Void))
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

    def timeoutStaleSpan(): Command[Void] = {
      sql"""UPDATE span SET ended_at = NOW(), status = 'error'::span_status
           |WHERE ended_at IS NULL AND started_at < NOW() - INTERVAL '10 minutes'""".command
    }

    val selectSpanById: Query[UUID, Span] = {
      sql"SELECT * FROM span WHERE id = $uuid".query(spanCodec)
    }

    val selectAllSpans: Query[Pagination, Span] =
      sql"SELECT * FROM span ORDER BY started_at DESC LIMIT ${int4} OFFSET ${int4}"
        .query(spanCodec)
        .contramap[Pagination](p => p.limit *: p.offset *: EmptyTuple)

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
}
