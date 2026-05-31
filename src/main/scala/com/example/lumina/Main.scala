package com.example.lumina

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp:

  override def run(args: List[String]): IO[ExitCode] = {
    val run = LuminaServer.run[IO].as(ExitCode.Success)
    run
  }
