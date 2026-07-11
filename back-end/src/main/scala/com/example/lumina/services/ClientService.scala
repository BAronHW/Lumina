package com.example.lumina.services

import cats.Monad
import cats.syntax.all.*
import com.example.lumina.Domain.{Client, Pagination}
import com.example.lumina.repository.ClientRepository
import org.typelevel.log4cats.Logger
import skunk.data.Completion

import java.util.UUID

trait ClientService[F[_]] {
  def registerClient(name: String): F[Client]
  def getClientById(clientId: UUID): F[Option[Client]]
  def updateClientById(clientId: UUID, newName: String): F[Completion]
  def removeClientById(clientId: UUID): F[Completion]
  def getAllClient(pagination: Pagination): F[List[Client]]
}

object ClientService {
  def impl[F[_]: Monad](clientRepository: ClientRepository[F], logger: Logger[F]): ClientService[F] =
    new ClientService[F] {
      override def registerClient(clientName: String): F[Client] = {
        val newClient = Client(java.util.UUID.randomUUID(), clientName)
        logger.info("Creating client") *> clientRepository.createClient(newClient)
      }

      override def getClientById(clientId: UUID): F[Option[Client]] = {
        logger.info(s"Getting client by Id $clientId") *> clientRepository.findClientById(clientId)
      }

      override def updateClientById(clientId: UUID, newName: String): F[Completion] = {
        logger.info(s"Updating Client By Id: $clientId") *> clientRepository.updateClient(clientId, newName)
      }

      override def removeClientById(clientId: UUID): F[Completion] = {
        logger.info(s"Removing Client with id: $clientId") *> clientRepository.deleteClient(clientId)
      }

      override def getAllClient(pagination: Pagination): F[List[Client]] = {
        logger.info("Getting all clients") *> clientRepository.getAllClients(pagination)
      }
    }
}
