import { UUID } from "node:crypto"

export interface Session {
    id: UUID
    agentId: UUID
    name: string
    createdAt: Date
    endedAt: Date | null
}
