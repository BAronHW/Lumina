package com.example.lumina.services

import Domain.Agent
import cats.Monad
import cats.syntax.all.*
import com.example.lumina.repository.AgentRepository
import org.typelevel.log4cats.Logger
import skunk.data.Completion

import java.util.UUID

trait AgentService[F[_]] {
  def createAgent(clientId: UUID, name: String): F[Completion]
  def getAgentById(agentId: UUID): F[Option[Agent]]
  def getAgentsByClientId(clientId: UUID): F[List[Agent]]
  def updateAgent(agentId: UUID, name: String): F[Completion]
  def deleteAgent(agentId: UUID): F[Completion]
}

object AgentService {
  def impl[F[_]: Monad](agentRepository: AgentRepository[F], logger: Logger[F]): AgentService[F] =
    new AgentService[F] {
      override def createAgent(clientId: UUID, name: String): F[Completion] =
        val agent = Agent(UUID.randomUUID(), clientId, name)
        logger.info(s"Creating agent '$name' for client $clientId") *> agentRepository.createAgent(agent)

      override def getAgentById(agentId: UUID): F[Option[Agent]] =
        logger.info(s"Getting agent by id $agentId") *> agentRepository.getAgentById(agentId)

      override def getAgentsByClientId(clientId: UUID): F[List[Agent]] =
        logger.info(s"Getting agents for client $clientId") *> agentRepository.getAgentsByClientId(clientId)

      override def updateAgent(agentId: UUID, name: String): F[Completion] =
        logger.info(s"Updating agent $agentId") *> agentRepository.updateAgent(agentId, name)

      override def deleteAgent(agentId: UUID): F[Completion] =
        logger.info(s"Deleting agent $agentId") *> agentRepository.deleteAgent(agentId)
    }
}
