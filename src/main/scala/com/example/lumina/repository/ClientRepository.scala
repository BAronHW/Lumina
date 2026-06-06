package com.example.lumina.repository

import Domain.Client
import cats.effect.{Concurrent, Resource}
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import cats.syntax.all.*
import skunk.data.Completion

import java.util.UUID

class ClientRepository[F[_]: Concurrent](session: Resource[F, Session[F]]) {
  def createClient(client: Client): F[Completion] = {
    session.use { s =>
      s.prepare(ClientRepositoryQueries.insertClient).flatMap(ps => ps.execute(client))
    }
  }

  def findClientById(clientId: UUID): F[Option[Client]] = {
    session.use { s => // session.use borrows a session from the postgres pool and s is the live session
      s.prepare(ClientRepositoryQueries.selectClient).flatMap {
        pq => // s.prepare sends the query to postgres parsing and planning and allows postgres to cache if query is run multiple times
          pq.option(clientId) // executes the prepared query with the clientId as the parameter
      }
    }
  }

  def updateClient(clientId: UUID, name: String): F[Completion] = {
    session.use { s =>
      s.prepare(ClientRepositoryQueries.updateClient).flatMap { pq =>
        pq.execute((name *: clientId *: EmptyTuple))
      }
    }
  }

  def deleteClient(clientId: UUID): F[Completion] = {
    session.use { s =>
      s.prepare(ClientRepositoryQueries.deleteClient).flatMap { pq =>
        pq.execute(clientId)
      }
    }
  }

  def getAllClients: F[List[Client]] = {
    session.use { s =>
      s.execute(ClientRepositoryQueries.getAllClient)
    }
  }

  private object ClientRepositoryQueries {
    private val clientCodec: Codec[Client] = (uuid *: varchar).to[Client]
    private val clientValues = (uuid *: varchar).values.to[Client]

    val insertClient: Command[Client] =
      sql"INSERT INTO client (id, name) VALUES $clientValues".command

    val selectClient: Query[UUID, Client] =
      sql"SELECT id, name FROM client WHERE id = $uuid".query(clientCodec)

    val updateClient: Command[String *: UUID *: EmptyTuple] =
      sql"UPDATE client SET name = $varchar WHERE id = $uuid".command

    val deleteClient: Command[UUID] =
      sql"DELETE FROM client WHERE id = $uuid".command

    val getAllClient: Query[Void, Client] =
      sql"SELECT id, name FROM client".query(clientCodec)
  }
}
