package com.example.lumina.repository

import cats.effect.{Concurrent, Resource}
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import cats.syntax.all.*
import com.example.lumina.Domain.{Deployment, Pagination}
import skunk.data.Completion

import java.util.UUID

class DeploymentRepository[F[_]: Concurrent](session: Resource[F, Session[F]]) {
  def createDeployment(deployment: Deployment): F[Deployment] = {
    session.use { s =>
      s.prepare(DeploymentRepositoryQueries.insertDeployment).flatMap(ps => ps.unique(deployment))
    }
  }

  def findDeploymentById(deploymentId: UUID): F[Option[Deployment]] = {
    session.use { s =>
      s.prepare(DeploymentRepositoryQueries.selectDeployment).flatMap {
        pq =>
          pq.option(deploymentId)
      }
    }
  }

  def updateDeployment(deploymentId: UUID, name: String): F[Completion] = {
    session.use { s =>
      s.prepare(DeploymentRepositoryQueries.updateDeployment).flatMap { pq =>
        pq.execute((name *: deploymentId *: EmptyTuple))
      }
    }
  }

  def deleteDeployment(deploymentId: UUID): F[Completion] = {
    session.use { s =>
      s.prepare(DeploymentRepositoryQueries.deleteDeployment).flatMap { pq =>
        pq.execute(deploymentId)
      }
    }
  }

  def getAllDeployments(pagination: Pagination): F[List[Deployment]] = {
    session.use { s =>
      s.prepare(DeploymentRepositoryQueries.getAllDeployment).flatMap(ps => ps.stream(pagination, 64).compile.toList)
    }
  }

  private object DeploymentRepositoryQueries {
    private val deploymentCodec: Codec[Deployment] = (uuid *: varchar).to[Deployment]
    private val deploymentValues = (uuid *: varchar).values.to[Deployment]

    val insertDeployment: Query[Deployment, Deployment] =
      sql"INSERT INTO deployment (id, name) VALUES $deploymentValues RETURNING id, name".query(deploymentCodec)

    val selectDeployment: Query[UUID, Deployment] =
      sql"SELECT id, name FROM deployment WHERE id = $uuid".query(deploymentCodec)

    val updateDeployment: Command[String *: UUID *: EmptyTuple] =
      sql"UPDATE deployment SET name = $varchar WHERE id = $uuid".command

    val deleteDeployment: Command[UUID] =
      sql"DELETE FROM deployment WHERE id = $uuid".command

    val getAllDeployment: Query[Pagination, Deployment] =
      sql"SELECT id, name FROM deployment ORDER BY id DESC LIMIT ${int4} OFFSET ${int4}"
        .query(deploymentCodec)
        .contramap[Pagination](p => p.limit *: p.offset *: EmptyTuple)
  }
}
