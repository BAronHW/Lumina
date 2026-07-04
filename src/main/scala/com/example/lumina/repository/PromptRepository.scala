package com.example.lumina.repository

import Domain.Prompt
import cats.effect.Concurrent
import cats.effect.kernel.Resource
import skunk.codec.all.{uuid, varchar}
import skunk.data.Completion
import skunk.implicits.sql
import skunk.{Codec, Command, Query, Session}
import cats.syntax.all.*

import java.util.UUID

class PromptRepository[F[_]: Concurrent](session: Resource[F, Session[F]]) {

  def createPrompt(id: UUID, name: String, prompt: String): F[Completion] = {
    session.use { s =>
      s.prepare(PromptRepositoryQueries.createPrompt).flatMap(ps => ps.execute(Prompt(id, name, prompt)))
    }
  }

  def deletePrompt(id: UUID): F[Completion] = {
    session.use { s =>
      s.prepare(PromptRepositoryQueries.deletePrompt).flatMap(ps => ps.execute(id))
    }
  }

  private object PromptRepositoryQueries {
    val promptEnc = (uuid *: varchar *: varchar).values.to[Prompt]
    val promptDec: Codec[Prompt] = (uuid *: varchar *: varchar).to[Prompt]
    val createPrompt: Command[Prompt] = sql"INSERT INTO prompt (id, name, prompt) VALUES $promptEnc".command
    val deletePrompt: Command[UUID] = sql"DELETE FROM prompt where id = $uuid".command
    val selectPromptWithId: Query[UUID, Prompt] = sql"SELECT * FROM trace WHERE id = $uuid".query(promptDec)

  }
}
