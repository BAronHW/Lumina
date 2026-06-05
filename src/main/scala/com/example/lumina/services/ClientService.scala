package com.example.lumina.services

import Domain.Client
import cats.Monad
import cats.syntax.all.*
import com.example.lumina.repository.ClientRepository
import org.typelevel.log4cats.Logger
import skunk.data.Completion

import java.util.UUID

trait ClientService[F[_]] {
  def registerClient(name: String): F[Completion]
  def getClientById(clientId: UUID): F[Option[Client]]
  def updateClientById(clientId: UUID, newName: String): F[Completion]
  def removeClientById(clientId: UUID): F[Completion]
}

object ClientService {
  def impl[F[_]: Monad](clientRepository: ClientRepository[F], logger: Logger[F]): ClientService[F] =
    new ClientService[F] {
      override def registerClient(clientName: String): F[Completion] = {
        val newClient = Client(java.util.UUID.randomUUID(), clientName)
        logger.info("Creating client") >> clientRepository.createClient(newClient)
      }

      override def getClientById(clientId: UUID): F[Option[Client]] = {
        logger.info("Getting client by Id") >> clientRepository.findClientById(clientId)
      }

      override def updateClientById(clientId: UUID, newName: String): F[Completion] = {
        logger.info("Updating Client By Id") >> clientRepository.updateClient(clientId = clientId, name = newName)
      }

      override def removeClientById(clientId: UUID): F[Completion] = {
        clientRepository.deleteClient(clientId)
      }
    }
}
