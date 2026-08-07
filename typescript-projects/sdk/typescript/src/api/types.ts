export type SpanStatus = "ok" | "error"

export type SpanKind = "Trace" | "LlmCall" | "ToolCall" | "Retrieval" | "AgentCall" | "Custom"