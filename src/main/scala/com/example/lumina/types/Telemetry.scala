package com.example.lumina.types

import org.http4s.headers.Date

case class Span[T, K, A](
    spanId: String,
    traceId: String,
    parentSpanId: Option[String],
    name: String,
    kind: "Trace" | "LlmCall" | "ToolCall" | "Retrieval" | "AgentCall" | "Custom",
    startedAt: Date,
    endedAt: Date,
    status: "Ok" | "Error",
    error: Option[String],
    input: Map[String, T],
    output: Map[String, K],
    attributes: Map[String, A],
    agentId: String,
    tags: Map[String, String]
)
