import { UUID } from "node:crypto"

export interface EvalRun {
    id: UUID
    name: string
    scorerType: string
    filter: Record<string, unknown>
    startedAt: Date
    endedAt: Date | null
    total: number
    passed: number
}

export interface EvalResult {
    id: UUID
    evalRunId: UUID | null
    traceId: UUID
    spanId: UUID | null
    evalName: string
    scorerType: string
    passed: boolean
    score: number | null
    reason: string | null
    runAt: Date
}
