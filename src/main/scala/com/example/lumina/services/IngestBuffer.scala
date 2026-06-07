package com.example.lumina.services

import cats.effect.std.Queue

trait Buffer[F[_], A] {
  def enqueue(obj: A): F[Unit]
  def getSize: F[Int]
  def take: F[A]
  def tryEnqueue(obj: A): F[Boolean]
  def tryTake: F[Option[A]]
}

class IngestBuffer[F[_], A](ingestQueue: Queue[F, A]) extends Buffer[F, A] {
  override def enqueue(obj: A): F[Unit] = {
    this.ingestQueue.offer(obj)
  }

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
