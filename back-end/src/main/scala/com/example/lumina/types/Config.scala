package com.example.lumina.types

import pureconfig.ConfigReader

import scala.concurrent.duration.FiniteDuration

final case class Config(
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String
) derives ConfigReader

final case class WorkerConfig(chunkSize: Int, pollInterval: Int) derives ConfigReader

final case class StaleTraceConfig(pollInterval: FiniteDuration) derives ConfigReader
