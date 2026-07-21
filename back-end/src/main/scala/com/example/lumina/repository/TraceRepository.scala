package com.example.lumina.repository

import cats.effect.Concurrent
import cats.effect.kernel.Resource
import com.example.lumina.types.{SpanKind, SpanStatus}
import cats.syntax.all.*
import com.example.lumina.Domain.{Pagination, Span, Trace, TraceWithSpans}
import io.circe.{Decoder as CDecoder, Encoder as CEncoder}
import skunk.*
import skunk.circe.codec.all.jsonb
import skunk.codec.all.*
import skunk.data.{Completion, Type}
import skunk.implicits.*

import java.util.UUID

class TraceRepository[F[_]: Concurrent](session: Resource[F, Session[F]]) {
  def createTrace(trace: Trace): F[Trace] =
    session.use { s =>
      s.prepare(TraceRepositoryQueries.createTrace).flatMap(ps => ps.unique(trace))
    }

  def getTraceById(traceId: UUID): F[Option[Trace]] =
    session.use { s =>
      s.prepare(TraceRepositoryQueries.selectTrace).flatMap(ps => ps.option(traceId))
    }

  def updateTrace(trace: Trace): F[Completion] =
    session.use { s =>
      s.prepare(TraceRepositoryQueries.updateTrace).flatMap(ps => ps.execute(trace))
    }

  def deleteTrace(traceId: UUID): F[Completion] =
    session.use { s =>
      s.prepare(TraceRepositoryQueries.deleteTrace).flatMap(ps => ps.execute(traceId))
    }

  def getAllTraces(pagination: Pagination): F[List[Trace]] =
    session.use { s =>
      s.prepare(TraceRepositoryQueries.selectAllTraces).flatMap(ps => ps.stream(pagination, 64).compile.toList)
    }

  def batchCreateTraces(traces: List[Trace]): F[Completion] = {
    session.use { s =>
      s.prepare(TraceRepositoryQueries.batchCreateTrace(traces)).flatMap(ps => ps.execute(traces))
    }
  }

  def batchUpdateTraces(traces: List[Trace]): F[Completion] = {
    session.use { s =>
      s.prepare(TraceRepositoryQueries.batchUpdateTrace(traces)).flatMap(ps => ps.execute(traces))
    }
  }

  def updateTraceBatchWithIds(traceIds: List[UUID]): F[Completion] = {
    session.use { s =>
      s.prepare(TraceRepositoryQueries.updateTraceBatchWithIds(traceIds)).flatMap(ps => ps.execute((traceIds)))
    }
  }

  def getTracesByAgentId(agentId: UUID): F[List[Trace]] = {
    session.use { s =>
      s.prepare(TraceRepositoryQueries.selectTracesByAgentId)
        .flatMap(ps => ps.stream(agentId, 64).compile.toList)
    }
  }

  def getAllFinishedTraces(pagination: Pagination): F[List[Trace]] = {
    session.use { s =>
      s.prepare(TraceRepositoryQueries.selectFinishedTraces).flatMap(ps => ps.stream(pagination, 64).compile.toList)
    }
  }

  def getTraceWithSpans(traceId: UUID): F[Option[TraceWithSpans]] =
    session.use { s =>
      s.prepare(TraceRepositoryQueries.selectTraceWithSpans).flatMap { ps =>
        ps.stream(traceId, 64).compile.toList.map { rows =>
          rows.headOption.map { case (trace, _) =>
            TraceWithSpans(trace, rows.flatMap(_._2))
          }
        }
      }
    }

  private object TraceRepositoryQueries {

    private val spanStatusCodec: Codec[SpanStatus] =
      `enum`(
        (s: SpanStatus) => s.toString.toLowerCase,
        (s: String) => SpanStatus.values.find(_.toString.toLowerCase == s),
        Type("span_status")
      )

    private val tagsCodec: Codec[Map[String, String]] =
      jsonb.imap(_.as[Map[String, String]].getOrElse(Map.empty))(tags => CEncoder[Map[String, String]].apply(tags))

    private val traceCodec: Codec[Trace] =
      (uuid *: uuid *: uuid.opt *: varchar *: spanStatusCodec *:
        timestamptz *: timestamptz.opt *: numeric.opt *: tagsCodec).to[Trace]

    val createTrace: Query[Trace, Trace] =
      sql"INSERT INTO trace VALUES ${traceCodec.values} RETURNING id, agent_id, session_id, name, status, started_at, ended_at, total_cost_usd, tags"
        .query(traceCodec)

    val selectTrace: Query[UUID, Trace] =
      sql"SELECT * FROM trace WHERE id = $uuid".query(traceCodec)

    val selectAllTraces: Query[Pagination, Trace] =
      sql"SELECT * FROM trace ORDER BY started_at DESC LIMIT ${int4} OFFSET ${int4}"
        .query(traceCodec)
        .contramap[Pagination](p => p.limit *: p.offset *: EmptyTuple)

    val deleteTrace: Command[UUID] =
      sql"DELETE FROM trace WHERE id = $uuid".command

    val updateTrace: Command[Trace] =
      sql"""UPDATE trace SET
              agent_id = $uuid, session_id = ${uuid.opt}, name = $varchar, status = $spanStatusCodec,
              started_at = $timestamptz, ended_at = ${timestamptz.opt},
              total_cost_usd = ${numeric.opt}, tags = $tagsCodec
            WHERE id = $uuid""".command.contramap[Trace] { t =>
        t.agentId *: t.sessionId *: t.name *: t.status *: t.startedAt *: t.endedAt *: t.totalCostUsd *: t.tags *: t.id *: EmptyTuple
      }

    def batchCreateTrace(traceList: List[Trace]): Command[traceList.type] = {
      val enc = traceCodec.list(traceList)
      sql"INSERT INTO trace VALUES $enc".command
    }

    def batchUpdateTrace(traceList: List[Trace]): Command[traceList.type] = {
      val enc = (uuid *: timestamptz.opt *: spanStatusCodec)
        .contramap[Trace](t => t.id *: t.endedAt *: t.status *: EmptyTuple)
        .list(traceList)
      sql"""UPDATE trace
              SET ended_at = v.ended_at,
                  status   = v.status::span_status
              FROM (VALUES $enc) AS v(id, ended_at, status)
             WHERE trace.id = v.id::uuid""".command
    }

    def updateTraceBatchWithIds(ids: List[UUID]): Command[ids.type] = {
      val enc = uuid.list(ids)
      sql"""UPDATE trace
                SET ended_at = NOW(),
                    status = 'ok'::span_status
               WHERE trace.id IN ($enc)""".command
    }

    val selectTracesByAgentId: Query[UUID, Trace] =
      sql"""SELECT * FROM trace WHERE agent_id = $uuid""".query(traceCodec)

    val selectFinishedTraces: Query[Pagination, Trace] =
      sql"SELECT * FROM trace WHERE ended_at IS NOT NULL ORDER BY started_at DESC LIMIT ${int4} OFFSET ${int4}"
        .query(traceCodec)
        .contramap[Pagination](p => p.limit *: p.offset *: EmptyTuple)

    private val spanKindCodec: Codec[SpanKind] =
      varchar.imap(SpanKind.valueOf)(_.toString)

    private val spanCodec: Codec[Span] =
      (uuid *: uuid *: uuid.opt *: varchar *: spanKindCodec *: spanStatusCodec *:
        text.opt *: timestamptz *: timestamptz.opt *: int4.opt *:
        jsonb *: jsonb *: jsonb).to[Span]

    val selectTraceWithSpans: Query[UUID, (Trace, Option[Span])] =
      sql"""SELECT t.id, t.agent_id, t.session_id, t.name, t.status, t.started_at, t.ended_at, t.total_cost_usd, t.tags,
                   s.id, s.trace_id, s.parent_span_id, s.name, s.kind, s.status, s.error, s.started_at, s.ended_at, s.duration_ms, s.input, s.output, s.attributes
            FROM trace t
            LEFT JOIN span s ON s.trace_id = t.id
            WHERE t.id = $uuid"""
        .query(traceCodec *: spanCodec.opt)
  }
}
