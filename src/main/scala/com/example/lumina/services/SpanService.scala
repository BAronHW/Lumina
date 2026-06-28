package com.example.lumina.services

import Domain.Span
import com.example.lumina.repository.SpanRepository
import skunk.data.Completion
import java.util.UUID

trait SpanService[F[_]] {
  def getSpanById(spanId: UUID): F[Option[Span]]
  def createBatchSpan(spanList: List[Span]): F[Completion]
  def updateSpan(span: Span): F[Completion]
  def deleteSpanById(spanId: UUID): F[Completion]
}

/** This is the service in charge of creating all the spans in this system it should create all spans in buffers
 *  and should also wait until the buffer reaches a certain size before it creates them in batches by a certain time
 * */
object SpanService {
  def impl[F[_]](spanRepository: SpanRepository[F]): SpanService[F] = new SpanService[F] {
    override def getSpanById(spanId: UUID): F[Option[Span]] = {
      spanRepository.getSpanById(spanId)
    }

    override def updateSpan(span: Span): F[Completion] = {
      spanRepository.updateSpanById(spanBody = span)
    }

    override def deleteSpanById(spanId: UUID): F[Completion] = {
      spanRepository.deleteSpanById(spanId)
    }

    override def createBatchSpan(spanList: List[Span]): F[Completion] = {
      spanRepository.createBatchSpan(spanList)
    }
  }
}
