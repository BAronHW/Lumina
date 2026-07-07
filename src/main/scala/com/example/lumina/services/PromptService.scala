package com.example.lumina.services

import cats.Monad
import cats.syntax.all.*
import com.example.lumina.Domain.Prompt
import com.example.lumina.repository.PromptRepository
import org.typelevel.log4cats.Logger
import skunk.data.Completion

import java.util.UUID

trait PromptService[F[_]] {
  def createPrompt(name: String, content: String): F[Completion]
  def getPrompt(id: UUID): F[Option[Prompt]]
  def updatePrompt(prompt: Prompt): F[Completion]
  def deletePrompt(id: UUID): F[Completion]
}

object PromptService {
  def impl[F[_]: Monad](promptRepository: PromptRepository[F], logger: Logger[F]): PromptService[F] =
    new PromptService[F] {
      override def createPrompt(name: String, content: String): F[Completion] =
        logger.info(s"Creating prompt: $name") *>
          promptRepository.createPrompt(Prompt(java.util.UUID.randomUUID(), name, content))

      override def getPrompt(id: UUID): F[Option[Prompt]] =
        logger.info(s"Getting prompt by id: $id") *> promptRepository.selectPromptWithId(id)

      override def updatePrompt(prompt: Prompt): F[Completion] =
        logger.info(s"Updating prompt: ${prompt.id}") *> promptRepository.updatePrompt(prompt)

      override def deletePrompt(id: UUID): F[Completion] =
        logger.info(s"Deleting prompt: $id") *> promptRepository.deletePrompt(id)
    }
}
