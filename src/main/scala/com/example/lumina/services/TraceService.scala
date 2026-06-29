package com.example.lumina.services

import Domain.Trace
import com.example.lumina.repository.TraceRepository
import skunk.data.Completion

import java.util.UUID

trait TraceService[F[_]] {
  def createTrace(traceBody: Trace): F[Completion]
  def deleteTrace(traceId: UUID): F[Completion]
  def selectTrace(traceId: UUID): F[Option[Trace]]
  def updateTrace(traceBody: Trace): F[Completion]
}

object TraceService {
  def impl[F[_]](traceRepository: TraceRepository[F]): TraceService[F] = new TraceService[F] {
    override def createTrace(traceBody: Trace): F[Completion] = {
      traceRepository.createTrace(traceBody)
    }

    override def deleteTrace(traceId: UUID): F[Completion] = {
      traceRepository.deleteTrace(traceId)
    }

    override def selectTrace(traceId: UUID): F[Option[Trace]] = {
      traceRepository.getTraceById(traceId)
    }

    override def updateTrace(traceBody: Trace): F[Completion] = {
      traceRepository.updateTrace(traceBody)
    }
  }
}
