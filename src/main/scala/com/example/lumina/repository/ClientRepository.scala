package com.example.lumina.repository

import cats.effect.{Concurrent, Resource}
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import cats.syntax.all.*
import com.example.lumina.Domain.{Client, Pagination}
import skunk.data.Completion

import java.util.UUID

class ClientRepository[F[_]: Concurrent](session: Resource[F, Session[F]]) {
  def createClient(client: Client): F[Client] = {
    session.use { s =>
      s.prepare(ClientRepositoryQueries.insertClient).flatMap(ps => ps.unique(client))
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

  def getAllClients(pagination: Pagination): F[List[Client]] = {
    session.use { s =>
      s.prepare(ClientRepositoryQueries.getAllClient).flatMap(ps => ps.stream(pagination, 64).compile.toList)
    }
  }

  private object ClientRepositoryQueries {
    private val clientCodec: Codec[Client] = (uuid *: varchar).to[Client]
    private val clientValues = (uuid *: varchar).values.to[Client]

    val insertClient: Query[Client, Client] =
      sql"INSERT INTO client (id, name) VALUES $clientValues RETURNING id, name".query(clientCodec)

    val selectClient: Query[UUID, Client] =
      sql"SELECT id, name FROM client WHERE id = $uuid".query(clientCodec)

    val updateClient: Command[String *: UUID *: EmptyTuple] =
      sql"UPDATE client SET name = $varchar WHERE id = $uuid".command

    val deleteClient: Command[UUID] =
      sql"DELETE FROM client WHERE id = $uuid".command

    val getAllClient: Query[Pagination, Client] =
      sql"SELECT id, name FROM client ORDER BY id DESC LIMIT ${int4} OFFSET ${int4}"
        .query(clientCodec)
        .contramap[Pagination](p => p.limit *: p.offset *: EmptyTuple)
  }
}
