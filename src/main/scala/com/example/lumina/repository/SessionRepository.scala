package com.example.lumina.repository

import Domain.{Session as DomainSession}
import cats.effect.{Concurrent, Resource}
import cats.syntax.all.*
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import skunk.data.Completion

import java.util.UUID

class SessionRepository[F[_]: Concurrent](session: Resource[F, Session[F]]) {

  def createSession(s: DomainSession): F[Completion] =
    session.use { sess =>
      sess.prepare(SessionRepositoryQueries.insertSession).flatMap(ps => ps.execute(s))
    }

  def getSessionById(sessionId: UUID): F[Option[DomainSession]] =
    session.use { sess =>
      sess.prepare(SessionRepositoryQueries.selectSessionById).flatMap(ps => ps.option(sessionId))
    }

  def getSessionsByAgentId(agentId: UUID): F[List[DomainSession]] =
    session.use { sess =>
      sess.prepare(SessionRepositoryQueries.selectSessionsByAgentId).flatMap(ps => ps.stream(agentId, 64).compile.toList)
    }

  def endSession(sessionId: UUID): F[Completion] =
    session.use { sess =>
      sess.prepare(SessionRepositoryQueries.endSession).flatMap(ps => ps.execute(sessionId))
    }

  def deleteSession(sessionId: UUID): F[Completion] =
    session.use { sess =>
      sess.prepare(SessionRepositoryQueries.deleteSession).flatMap(ps => ps.execute(sessionId))
    }

  private object SessionRepositoryQueries {

    private val sessionCodec: Codec[DomainSession] =
      (uuid *: uuid *: varchar *: timestamptz *: timestamptz.opt).to[DomainSession]

    val insertSession: Command[DomainSession] =
      sql"""INSERT INTO session (id, agent_id, name, created_at, ended_at)
            VALUES ($uuid, $uuid, $varchar, $timestamptz, ${timestamptz.opt})""".command
        .contramap[DomainSession](s => s.id *: s.agentId *: s.name *: s.createdAt *: s.endedAt *: EmptyTuple)

    val selectSessionById: Query[UUID, DomainSession] =
      sql"SELECT id, agent_id, name, created_at, ended_at FROM session WHERE id = $uuid".query(sessionCodec)

    val selectSessionsByAgentId: Query[UUID, DomainSession] =
      sql"SELECT id, agent_id, name, created_at, ended_at FROM session WHERE agent_id = $uuid".query(sessionCodec)

    val endSession: Command[UUID] =
      sql"UPDATE session SET ended_at = NOW() WHERE id = $uuid".command

    val deleteSession: Command[UUID] =
      sql"DELETE FROM session WHERE id = $uuid".command
  }
}
