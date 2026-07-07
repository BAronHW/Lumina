package com.example.lumina.repository

import cats.effect.Concurrent
import cats.effect.kernel.Resource
import skunk.*
import skunk.codec.all.{uuid, varchar}
import skunk.data.Completion
import skunk.implicits.sql
import cats.syntax.all.*
import com.example.lumina.Domain.Prompt

import java.util.UUID

class PromptRepository[F[_]: Concurrent](session: Resource[F, Session[F]]) {

  def createPrompt(prompt: Prompt): F[Completion] = {
    session.use { s =>
      s.prepare(PromptRepositoryQueries.createPrompt).flatMap(ps => ps.execute(prompt))
    }
  }

  def deletePrompt(id: UUID): F[Completion] = {
    session.use { s =>
      s.prepare(PromptRepositoryQueries.deletePrompt).flatMap(ps => ps.execute(id))
    }
  }

  def selectPromptWithId(id: UUID): F[Option[Prompt]] = {
    session.use { s =>
      s.prepare(PromptRepositoryQueries.selectPromptWithId).flatMap(ps => ps.option(id))
    }
  }

  def updatePrompt(prompt: Prompt): F[Completion] = {
    session.use { s =>
      s.prepare(PromptRepositoryQueries.updatePrompt).flatMap(ps => ps.execute(prompt))
    }
  }

  private object PromptRepositoryQueries {
    private val promptCodec: Codec[Prompt] = (uuid *: varchar *: varchar).to[Prompt]

    val createPrompt: Command[Prompt] =
      sql"INSERT INTO prompt (id, name, content) VALUES ${promptCodec.values}".command

    val deletePrompt: Command[UUID] =
      sql"DELETE FROM prompt WHERE id = $uuid".command

    val selectPromptWithId: Query[UUID, Prompt] =
      sql"SELECT * FROM prompt WHERE id = $uuid".query(promptCodec)

    val updatePrompt: Command[Prompt] =
      sql"UPDATE prompt SET name = $varchar, content = $varchar WHERE id = $uuid".command.contramap[Prompt] { p =>
        p.name *: p.prompt *: p.id *: EmptyTuple
      }
  }
}
