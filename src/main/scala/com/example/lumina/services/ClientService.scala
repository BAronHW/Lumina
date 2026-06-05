package com.example.lumina.services

import Domain.Client
import com.example.lumina.repository.ClientRepository
import skunk.data.Completion

import java.util.UUID

trait ClientService[F[_]] {
  def registerClient(name: String): F[Completion]
  def getClientById(clientId: UUID): F[Option[Client]]
  def updateClientById(clientId: UUID, newName: String): F[Completion]
  def removeClientById(clientId: UUID): F[Completion]
}

object ClientService {
  def impl[F[_]](clientRepository: ClientRepository[F]): ClientService[F] = new ClientService[F] {
    override def registerClient(clientName: String): F[Completion] = {
      val newClient = Client(java.util.UUID.randomUUID(), clientName)
      clientRepository.createClient(newClient)
    }

    override def getClientById(clientId: UUID): F[Option[Client]] = {
      clientRepository.findClientById(clientId)
    }

    override def updateClientById(clientId: UUID, newName: String): F[Completion] = {
      clientRepository.updateClient(clientId = clientId, name = newName)
    }

    override def removeClientById(clientId: UUID): F[Completion] = {
      clientRepository.deleteClient(clientId)
    }
  }
}
