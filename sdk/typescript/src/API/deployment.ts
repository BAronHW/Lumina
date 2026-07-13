import { UUID } from "node:crypto"

export interface Deployment {
    id: UUID
    clientId: UUID
    version: string
    deployedAt: Date
}
