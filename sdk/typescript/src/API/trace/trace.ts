import { UUID } from "node:crypto"
import { SpanStatus } from "../types"

export interface Trace {
    id: UUID
    agentId: UUID
    sessionId: UUID | null
    name: string
    status: SpanStatus
    startedAt: Date
    endedAt: Date | null
    totalCostUsd: number | null
    tags: Record<string, string>
}

export interface StartTraceBody<T, K> {
    agentId: UUID
    sessionId?: UUID
    name: string
    tags?: Record<string, string>
    input: T
    callback: (input: T) => Promise<K>
}