package com.example.lumina.services

import cats.Monad
import cats.syntax.all.*
import com.example.lumina.Domain.Agent
import com.example.lumina.repository.AgentRepository
import org.typelevel.log4cats.Logger
import skunk.data.Completion

import java.util.UUID

trait AgentService[F[_]] {
  def createAgent(deploymentId: UUID, name: String): F[Agent]
  def getAgentById(agentId: UUID): F[Option[Agent]]
  def getAgentsByDeploymentId(deploymentId: UUID): F[List[Agent]]
  def updateAgent(agentId: UUID, name: String): F[Completion]
  def deleteAgent(agentId: UUID): F[Completion]
}

object AgentService {
  def impl[F[_]: Monad](agentRepository: AgentRepository[F], logger: Logger[F]): AgentService[F] =
    new AgentService[F] {
      override def createAgent(deploymentId: UUID, name: String): F[Agent] =
        val agent = Agent(UUID.randomUUID(), deploymentId, name)
        logger.info(s"Creating agent '$name' for deployment $deploymentId") *> agentRepository.createAgent(agent)

      override def getAgentById(agentId: UUID): F[Option[Agent]] =
        logger.info(s"Getting agent by id $agentId") *> agentRepository.getAgentById(agentId)

      override def getAgentsByDeploymentId(deploymentId: UUID): F[List[Agent]] =
        logger.info(s"Getting agents for deployment $deploymentId") *> agentRepository.getAgentsByDeploymentId(deploymentId)

      override def updateAgent(agentId: UUID, name: String): F[Completion] =
        logger.info(s"Updating agent $agentId") *> agentRepository.updateAgent(agentId, name)

      override def deleteAgent(agentId: UUID): F[Completion] =
        logger.info(s"Deleting agent $agentId") *> agentRepository.deleteAgent(agentId)
    }
}
