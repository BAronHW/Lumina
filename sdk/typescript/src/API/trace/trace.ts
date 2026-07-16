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

export interface StartTraceBody<T> {
    id: UUID
    agentId:UUID
    name: string,
    status: SpanStatus
    startedAt: Date
    tags: Record<string, string>
    callback: ( {...args} ) => Promise<T>
}