package com.example.lumina.services

import com.example.lumina.types.Span
import skunk.data.Completion

import java.util.UUID

trait IngestService[F[_]] {
  def receiveSpans(spans: List[Span]): F[Unit]
  def getSpanById(spanId: UUID): F[Option[Span]]
  def updateSpanById(spanId: UUID): F[Completion]
  def deleteSpanById(spanId: UUID): F[Completion]
  def getAllSpan: F[List[Span]]
}

object IngestService {
  def impl[F[_]](): IngestService[F] = new IngestService[F] {
    override def receiveSpans(spans: List[Span]): F[Unit] = ???

    override def getSpanById(spanId: UUID): F[Option[Span]] = ???

    override def getAllSpan: F[List[Span]] = ???

    override def deleteSpanById(spanId: UUID): F[Completion] = ???

    override def updateSpanById(spanId: UUID): F[Completion] = ???

  }
}
