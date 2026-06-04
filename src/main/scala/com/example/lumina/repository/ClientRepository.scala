package com.example.lumina.repository

import Domain.Client
import cats.effect.Resource
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import cats.effect.Concurrent
import cats.syntax.all.*
import java.util.UUID

class ClientRepository[F[_]: Concurrent](session: Resource[F, Session[F]]) {
  def createClient(client: Client) = {
    this.session.use { s =>
      s.prepare(ClientRepositoryQueries.insertClient).flatMap(ps => ps.execute(client))
    }
  }

//  def findClientById(clientId: UUID) = {
//    this.session.use { s =>
//      s.prepare(ClientRepositoryQueries.selectClient).use { cmd => }
//    }
//  }
}

object ClientRepositoryQueries {
  val clientCodec: Codec[Client] = (uuid *: varchar).to[Client]

  val insertClient: Command[Client] =
    sql"INSERT INTO client (id, name) VALUES $clientCodec".command

  val selectClient: Query[UUID, Client] =
    sql"SELECT id, name FROM client WHERE id = $uuid".query(clientCodec)

  val updateClient: Command[String *: UUID *: EmptyTuple] =
    sql"UPDATE client SET name = $varchar WHERE id = $uuid".command

  val deleteClient: Command[UUID *: EmptyTuple] =
    sql"DELETE FROM client WHERE id = $uuid".command
}
