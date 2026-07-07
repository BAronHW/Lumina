package com.example.lumina.services

import Domain.{Pagination, Trace}
import cats.Monad
import cats.syntax.all.*
import com.example.lumina.repository.TraceRepository
import org.typelevel.log4cats.Logger
import skunk.data.Completion

import java.util.UUID

trait TraceService[F[_]] {
  def createTrace(traceBody: Trace): F[Completion]
  def deleteTrace(traceId: UUID): F[Completion]
  def selectTrace(traceId: UUID): F[Option[Trace]]
  def updateTrace(traceBody: Trace): F[Completion]
  def batchCreateTrace(traceBody: List[Trace]): F[Completion]
  def batchUpdateTraces(traces: List[Trace]): F[Completion]
  def updateBatchTracesWithId(traceIds: List[UUID]): F[Completion]
  def getAllTraces(pagination: Pagination): F[List[Trace]]
  def getTracesByAgentId(agentId: UUID): F[List[Trace]]
}

object TraceService {
  def impl[F[_]: Monad](traceRepository: TraceRepository[F], logger: Logger[F]): TraceService[F] =
    new TraceService[F] {
      override def createTrace(traceBody: Trace): F[Completion] =
        logger.info(s"Creating trace: ${traceBody.id}") *> traceRepository.createTrace(traceBody)

      override def deleteTrace(traceId: UUID): F[Completion] =
        logger.info(s"Deleting trace: $traceId") *> traceRepository.deleteTrace(traceId)

      override def selectTrace(traceId: UUID): F[Option[Trace]] =
        logger.info(s"Getting trace: $traceId") *> traceRepository.getTraceById(traceId)

      override def updateTrace(traceBody: Trace): F[Completion] =
        logger.info(s"Updating trace: ${traceBody.id}") *> traceRepository.updateTrace(traceBody)

      override def batchCreateTrace(traceBodyBatch: List[Trace]): F[Completion] =
        logger.info(s"Batch creating ${traceBodyBatch.size} traces") *> traceRepository.batchCreateTraces(
          traceBodyBatch
        )

      override def batchUpdateTraces(traces: List[Trace]): F[Completion] =
        logger.info(s"Batch updating ${traces.size} traces") *> traceRepository.batchUpdateTraces(traces)

      override def updateBatchTracesWithId(traceIds: List[UUID]): F[Completion] =
        logger.info(s"Batch updating ${traceIds.size} traces by id") *> traceRepository.updateTraceBatchWithIds(
          traceIds
        )

      override def getAllTraces(pagination: Pagination): F[List[Trace]] =
        logger.info(s"Getting all traces with pagination") *> traceRepository.getAllTraces(pagination)

      override def getTracesByAgentId(agentId: UUID): F[List[Trace]] =
        logger.info(s"Getting all traces belonging to agent with id of ${agentId}") *> traceRepository
          .getTracesByAgentId(agentId)
    }
}
