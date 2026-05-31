package com.example.lumina

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple:
  val run = LuminaServer.run[IO]
