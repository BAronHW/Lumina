import { UUID } from "node:crypto"
import { SpanKind, SpanStatus } from "../types"

export interface Span {
    id: UUID
    traceId: UUID
    parentSpanId: UUID | null
    name: string
    kind: SpanKind
    status: SpanStatus
    error: string | null
    startedAt: Date
    endedAt: Date | null
    durationMs: number | null
    input: Record<string, unknown>
    output: Record<string, unknown>
    attributes: Record<string, unknown>
}

export interface StartSpanBody<T> {
    id: UUID
    traceId: UUID,
    parentSpanId: UUID | null
    name: string
    kind: SpanKind
    input: Record<string, unknown>
    attributes: Record<string, unknown>
    callback: ( {...args} ) => Promise<T>
}