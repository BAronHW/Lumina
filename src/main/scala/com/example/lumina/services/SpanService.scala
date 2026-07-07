package com.example.lumina.services

import Domain.{Pagination, Span}
import cats.Monad
import cats.syntax.all.*
import com.example.lumina.repository.SpanRepository
import org.typelevel.log4cats.Logger
import skunk.data.Completion

import java.util.UUID

trait SpanService[F[_]] {
  def getSpanById(spanId: UUID): F[Option[Span]]
  def createBatchSpan(spanList: List[Span]): F[Completion]
  def updateSpan(span: Span): F[Completion]
  def deleteSpanById(spanId: UUID): F[Completion]
  def getAllSpan(pagination: Pagination): F[List[Span]]
}

/** This is the service in charge of creating all the spans in this system it should create all spans in buffers and
  * should also wait until the buffer reaches a certain size before it creates them in batches by a certain time
  */
object SpanService {
  def impl[F[_]: Monad](spanRepository: SpanRepository[F], logger: Logger[F]): SpanService[F] =
    new SpanService[F] {
      override def getSpanById(spanId: UUID): F[Option[Span]] =
        logger.info(s"Getting span by id: $spanId") *> spanRepository.getSpanById(spanId)

      override def createBatchSpan(spanList: List[Span]): F[Completion] = {
        if (spanList.nonEmpty) {
          spanRepository.createBatchSpan(spanList)
        } else {
          Monad[F].pure(Completion.Insert(0))
        }
      }

      override def updateSpan(span: Span): F[Completion] =
        logger.info(s"Updating span: ${span.id}") *> spanRepository.updateSpanById(spanBody = span)

      override def deleteSpanById(spanId: UUID): F[Completion] =
        logger.info(s"Deleting span: $spanId") *> spanRepository.deleteSpanById(spanId)

      override def getAllSpan(pagination: Pagination): F[List[Span]] = {
        logger.info(s"Get All span") *> spanRepository.getAllSpans(pagination)
      }
    }
}
