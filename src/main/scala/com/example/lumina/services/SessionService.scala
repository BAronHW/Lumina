package com.example.lumina.services

import Domain.Session
import cats.Monad
import cats.syntax.all.*
import com.example.lumina.repository.SessionRepository
import org.typelevel.log4cats.Logger
import skunk.data.Completion

import java.time.OffsetDateTime
import java.util.UUID

trait SessionService[F[_]] {
  def createSession(agentId: UUID, name: String): F[Completion]
  def getSessionById(sessionId: UUID): F[Option[Session]]
  def getSessionsByAgentId(agentId: UUID): F[List[Session]]
  def endSession(sessionId: UUID): F[Completion]
  def deleteSession(sessionId: UUID): F[Completion]
}

object SessionService {
  def impl[F[_]: Monad](sessionRepository: SessionRepository[F], logger: Logger[F]): SessionService[F] =
    new SessionService[F] {
      override def createSession(agentId: UUID, name: String): F[Completion] =
        val s = Session(UUID.randomUUID(), agentId, name, OffsetDateTime.now(), None)
        logger.info(s"Creating session '$name' for agent $agentId") *> sessionRepository.createSession(s)

      override def getSessionById(sessionId: UUID): F[Option[Session]] =
        logger.info(s"Getting session by id $sessionId") *> sessionRepository.getSessionById(sessionId)

      override def getSessionsByAgentId(agentId: UUID): F[List[Session]] =
        logger.info(s"Getting sessions for agent $agentId") *> sessionRepository.getSessionsByAgentId(agentId)

      override def endSession(sessionId: UUID): F[Completion] =
        logger.info(s"Ending session $sessionId") *> sessionRepository.endSession(sessionId)

      override def deleteSession(sessionId: UUID): F[Completion] =
        logger.info(s"Deleting session $sessionId") *> sessionRepository.deleteSession(sessionId)
    }
}
