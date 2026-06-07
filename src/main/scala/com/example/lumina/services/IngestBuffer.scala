package com.example.lumina.services

import cats.Monad
import cats.effect.std.Queue
import cats.syntax.all.*

trait Buffer[F[_], A] {
  def enqueue(obj: List[A]): F[Unit]
  def getSize: F[Int]
  def take: F[A]
  def tryEnqueue(obj: A): F[Boolean]
  def tryTake: F[Option[A]]
}

class IngestBuffer[F[_]: Monad, A](ingestQueue: Queue[F, A]) extends Buffer[F, A] {
  override def enqueue(obj: List[A]): F[Unit] =
    obj.traverse_(ingestQueue.offer)

  override def getSize: F[Int] = {
    this.ingestQueue.size
  }

  override def take: F[A] = {
    this.ingestQueue.take
  }

  override def tryEnqueue(obj: A): F[Boolean] = {
    this.ingestQueue.tryOffer(obj)
  }

  override def tryTake: F[Option[A]] = {
    this.ingestQueue.tryTake
  }

}
