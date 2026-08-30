package com.example.lumina.repository

import cats.effect.Concurrent
import cats.effect.kernel.Resource
import skunk.*
import skunk.codec.all.*
import skunk.data.Completion
import skunk.implicits.sql
import cats.syntax.all.*
import com.example.lumina.Domain.{Pagination, Prompt}

import java.util.UUID

class PromptRepository[F[_]: Concurrent](session: Resource[F, Session[F]]) {

  def createPrompt(prompt: Prompt): F[Prompt] = {
    session.use { s =>
      s.prepare(PromptRepositoryQueries.createPrompt).flatMap(ps => ps.unique(prompt))
    }
  }

  def getAllPrompts(pagination: Pagination): F[List[Prompt]] = {
    session.use { s =>
      s.prepare(PromptRepositoryQueries.selectAllPrompts).flatMap(ps => ps.stream(pagination, 64).compile.toList)
    }
  }

  def countPrompts(): F[Long] = {
    session.use { s =>
      s.unique(PromptRepositoryQueries.countPrompts)
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

    val selectAllPrompts: Query[Pagination, Prompt] =
      sql"SELECT id, name, prompt FROM prompts ORDER BY name LIMIT ${int4} OFFSET ${int4}"
        .query(promptCodec)
        .contramap[Pagination](p => p.limit *: p.offset *: EmptyTuple)

    val countPrompts: Query[Void, Long] =
      sql"SELECT COUNT(*) FROM prompts".query(int8)

    val createPrompt: Query[Prompt, Prompt] =
      sql"INSERT INTO prompts (id, name, prompt) VALUES ${promptCodec.values} RETURNING id, name, prompt"
        .query(promptCodec)

    val deletePrompt: Command[UUID] =
      sql"DELETE FROM prompts WHERE id = $uuid".command

    val selectPromptWithId: Query[UUID, Prompt] =
      sql"SELECT id, name, prompt FROM prompts WHERE id = $uuid".query(promptCodec)

    val updatePrompt: Command[Prompt] =
      sql"UPDATE prompts SET name = $varchar, prompt = $varchar WHERE id = $uuid".command.contramap[Prompt] { p =>
        p.name *: p.prompt *: p.id *: EmptyTuple
      }
  }
}
