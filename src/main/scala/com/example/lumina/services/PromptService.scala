package com.example.lumina.services

import Domain.Prompt
import com.example.lumina.repository.PromptRepository
import skunk.data.Completion

import java.util.UUID

trait PromptService[F[_]] {
  def createPrompt(name: String, content: String): F[Completion]
  def getPrompt(id: UUID): F[Option[Prompt]]
  def updatePrompt(prompt: Prompt): F[Completion]
  def deletePrompt(id: UUID): F[Completion]
}

object PromptService {
  def impl[F[_]](promptRepository: PromptRepository[F]): PromptService[F] = new PromptService[F] {
    override def getPrompt(id: UUID): F[Option[Prompt]] = {
      promptRepository.selectPromptWithId(id)
    }

    override def updatePrompt(prompt: Prompt): F[Completion] = {
      promptRepository.updatePrompt(prompt)
    }

    override def deletePrompt(id: UUID): F[Completion] = {
      promptRepository.deletePrompt(id)
    }

    override def createPrompt(name: String, content: String): F[Completion] = {
      val newPrompt = Prompt(java.util.UUID.randomUUID(), name, content)
      promptRepository.createPrompt(newPrompt)
    }
  }
}
