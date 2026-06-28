package com.example.lumina.services

import Domain.Trace
import skunk.data.Completion

import java.util.UUID

trait TraceService[F[_]] {
  def createTrace(traceBody: Trace): F[Completion]
  def deleteTrace(traceId: UUID): F[Completion]
  def selectTrace(traceId: UUID): F[Option[Trace]]
  def updateTrace(traceBody: Trace): F[Completion]
}

object TraceService {}
