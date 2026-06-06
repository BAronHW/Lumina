FROM sbtscala/scala-sbt:eclipse-temurin-alpine-25.0.1_8_1.12.11_3.3.7 AS builder
WORKDIR /app
COPY . /app
COPY build.sbt ./
CMD ["sbt", "run"]
