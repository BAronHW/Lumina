package com.example.lumina.repository

import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import java.util.UUID

class ClientRepository[F[_]] {}

object ClientRepositoryQueries {
  val insertClient: Command[UUID *: String *: EmptyTuple] =
    sql"INSERT INTO client (id, name) VALUES ($uuid, $varchar)".command
}
