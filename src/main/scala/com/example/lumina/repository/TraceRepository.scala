package com.example.lumina.repository

import Domain.{Pagination, Trace}
import cats.effect.Concurrent
import cats.effect.kernel.Resource
import com.example.lumina.types.SpanStatus
import cats.syntax.all.*
import io.circe.{Decoder as CDecoder, Encoder as CEncoder}
import skunk.*
import skunk.circe.codec.all.jsonb
import skunk.codec.all.*
import skunk.data.{Completion, Type}
import skunk.implicits.*

import java.util.UUID

class TraceRepository[F[_]: Concurrent](session: Resource[F, Session[F]]) {
  def createTrace(trace: Trace): F[Completion] =
    session.use { s =>
      s.prepare(TraceRepositoryQueries.createTrace).flatMap(ps => ps.execute(trace))
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
      (uuid *: uuid *: varchar *: spanStatusCodec *:
        timestamptz *: timestamptz.opt *: numeric.opt *: tagsCodec).to[Trace]

    val createTrace: Command[Trace] =
      sql"INSERT INTO trace VALUES ${traceCodec.values}".command

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
              agent_id = $uuid, name = $varchar, status = $spanStatusCodec,
              started_at = $timestamptz, ended_at = ${timestamptz.opt},
              total_cost_usd = ${numeric.opt}, tags = $tagsCodec
            WHERE id = $uuid""".command.contramap[Trace] { t =>
        t.agentId *: t.name *: t.status *: t.startedAt *: t.endedAt *: t.totalCostUsd *: t.tags *: t.id *: EmptyTuple
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
                FROM (VALUES $enc) AS v(id)
               WHERE trace.id = v.id::uuid""".command
    }
  }
}
