package com.example.lumina.repository

import Domain.Trace
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
}
