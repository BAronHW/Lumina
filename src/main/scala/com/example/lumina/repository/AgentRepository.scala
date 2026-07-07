package com.example.lumina.repository

import cats.effect.{Concurrent, Resource}
import cats.syntax.all.*
import com.example.lumina.Domain.Agent
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import skunk.data.Completion

import java.util.UUID

class AgentRepository[F[_]: Concurrent](session: Resource[F, Session[F]]) {

  def createAgent(agent: Agent): F[Completion] =
    session.use { s =>
      s.prepare(AgentRepositoryQueries.insertAgent).flatMap(ps => ps.execute(agent))
    }

  def getAgentById(agentId: UUID): F[Option[Agent]] =
    session.use { s =>
      s.prepare(AgentRepositoryQueries.selectAgentById).flatMap(pq => pq.option(agentId))
    }

  def getAgentsByClientId(clientId: UUID): F[List[Agent]] =
    session.use { s =>
      s.prepare(AgentRepositoryQueries.selectAgentsByClientId).flatMap(pq => pq.stream(clientId, 64).compile.toList)
    }

  def updateAgent(agentId: UUID, name: String): F[Completion] =
    session.use { s =>
      s.prepare(AgentRepositoryQueries.updateAgent).flatMap(pq => pq.execute(name *: agentId *: EmptyTuple))
    }

  def deleteAgent(agentId: UUID): F[Completion] =
    session.use { s =>
      s.prepare(AgentRepositoryQueries.deleteAgent).flatMap(pq => pq.execute(agentId))
    }

  private object AgentRepositoryQueries {
    private val agentCodec: Codec[Agent] = (uuid *: uuid *: varchar).to[Agent]

    val insertAgent: Command[Agent] =
      sql"INSERT INTO agent (id, client_id, name) VALUES ($uuid, $uuid, $varchar)".command
        .contramap[Agent](a => a.id *: a.clientId *: a.name *: EmptyTuple)

    val selectAgentById: Query[UUID, Agent] =
      sql"SELECT id, client_id, name FROM agent WHERE id = $uuid".query(agentCodec)

    val selectAgentsByClientId: Query[UUID, Agent] =
      sql"SELECT id, client_id, name FROM agent WHERE client_id = $uuid".query(agentCodec)

    val updateAgent: Command[String *: UUID *: EmptyTuple] =
      sql"UPDATE agent SET name = $varchar WHERE id = $uuid".command

    val deleteAgent: Command[UUID] =
      sql"DELETE FROM agent WHERE id = $uuid".command
  }
}
