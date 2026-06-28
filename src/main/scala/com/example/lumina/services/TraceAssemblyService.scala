package com.example.lumina.services

import Domain.Span

trait TraceAssemblyService[F[_]] {
  def processSpans(spans: List[Span]): F[Boolean]
  def flush: F[Boolean]
}

object TraceAssemblyService {}
