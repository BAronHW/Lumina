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

  def createAgent(agent: Agent): F[Agent] =
    session.use { s =>
      s.prepare(AgentRepositoryQueries.insertAgent).flatMap(ps => ps.unique(agent))
    }

  def getAgentById(agentId: UUID): F[Option[Agent]] =
    session.use { s =>
      s.prepare(AgentRepositoryQueries.selectAgentById).flatMap(pq => pq.option(agentId))
    }

  def getAgentsByDeploymentId(deploymentId: UUID): F[List[Agent]] =
    session.use { s =>
      s.prepare(AgentRepositoryQueries.selectAgentsByDeploymentId).flatMap(pq => pq.stream(deploymentId, 64).compile.toList)
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

    val insertAgent: Query[Agent, Agent] =
      sql"INSERT INTO agent (id, deployment_id, name) VALUES ($uuid, $uuid, $varchar) RETURNING id, deployment_id, name"
        .query(agentCodec)
        .contramap[Agent](a => a.id *: a.deploymentId *: a.name *: EmptyTuple)

    val selectAgentById: Query[UUID, Agent] =
      sql"SELECT id, deployment_id, name FROM agent WHERE id = $uuid".query(agentCodec)

    val selectAgentsByDeploymentId: Query[UUID, Agent] =
      sql"SELECT id, deployment_id, name FROM agent WHERE deployment_id = $uuid".query(agentCodec)

    val updateAgent: Command[String *: UUID *: EmptyTuple] =
      sql"UPDATE agent SET name = $varchar WHERE id = $uuid".command

    val deleteAgent: Command[UUID] =
      sql"DELETE FROM agent WHERE id = $uuid".command
  }
}
