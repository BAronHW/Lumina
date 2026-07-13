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
