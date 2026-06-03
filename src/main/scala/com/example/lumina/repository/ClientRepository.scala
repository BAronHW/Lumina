package com.example.lumina.repository

import Domain.Client
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*

import java.util.UUID

object ClientRepository {
  def insertClient(client: Client) = {}
}

object ClientRepositoryQueries {
  val clientCodec: Codec[Client] = (uuid *: varchar).to[Client]

  val insertClient: Command[Client] =
    sql"INSERT INTO client (id, name) VALUES $clientCodec".command

  val selectClient: Query[UUID, Client] =
    sql"SELECT id, name FROM client WHERE id = $uuid".query(clientCodec)

  val updateClient: Command[String *: UUID *: EmptyTuple] =
    sql"UPDATE client SET name = $varchar WHERE id = $uuid".command
}
