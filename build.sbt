ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

lazy val root = (project in file("."))
  .settings(
    name := "Lumina"
  )

val http4sVersion = "0.23.34"
val circeVersion = "0.14.15"
val skunkVersion = "1.0.0"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-core" % http4sVersion,
  "org.http4s" %% "http4s-client" % http4sVersion,
  "org.http4s" %% "http4s-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.tpolecat" %% "skunk-core" % skunkVersion,
  "org.tpolecat" %% "natchez-noop" % "0.3.10",
  "org.scalameta" %% "munit" % "1.3.1",
  "com.github.pureconfig" %% "pureconfig-core" % "0.17.10"
)
