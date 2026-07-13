package com.example.lumina.services

import cats.Monad
import cats.syntax.all.*
import com.example.lumina.Domain.{Deployment, Pagination}
import com.example.lumina.repository.DeploymentRepository
import org.typelevel.log4cats.Logger
import skunk.data.Completion

import java.util.UUID

trait DeploymentService[F[_]] {
  def registerDeployment(name: String): F[Deployment]
  def getDeploymentById(deploymentId: UUID): F[Option[Deployment]]
  def updateDeploymentById(deploymentId: UUID, newName: String): F[Completion]
  def removeDeploymentById(deploymentId: UUID): F[Completion]
  def getAllDeployment(pagination: Pagination): F[List[Deployment]]
}

object DeploymentService {
  def impl[F[_]: Monad](deploymentRepository: DeploymentRepository[F], logger: Logger[F]): DeploymentService[F] =
    new DeploymentService[F] {
      override def registerDeployment(deploymentName: String): F[Deployment] = {
        val newDeployment = Deployment(java.util.UUID.randomUUID(), deploymentName)
        logger.info("Creating deployment") *> deploymentRepository.createDeployment(newDeployment)
      }

      override def getDeploymentById(deploymentId: UUID): F[Option[Deployment]] = {
        logger.info(s"Getting deployment by Id $deploymentId") *> deploymentRepository.findDeploymentById(deploymentId)
      }

      override def updateDeploymentById(deploymentId: UUID, newName: String): F[Completion] = {
        logger.info(s"Updating Deployment By Id: $deploymentId") *> deploymentRepository.updateDeployment(deploymentId, newName)
      }

      override def removeDeploymentById(deploymentId: UUID): F[Completion] = {
        logger.info(s"Removing Deployment with id: $deploymentId") *> deploymentRepository.deleteDeployment(deploymentId)
      }

      override def getAllDeployment(pagination: Pagination): F[List[Deployment]] = {
        logger.info("Getting all deployments") *> deploymentRepository.getAllDeployments(pagination)
      }
    }
}
