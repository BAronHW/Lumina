package com.example.lumina.services

trait IngestService[F[_]] {}

object IngestService {
  def impl[F[_]](): IngestService[F] = new IngestService[F] {}

}
