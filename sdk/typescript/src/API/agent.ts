import { UUID } from "node:crypto"

export interface Agent {
    id: UUID
    deploymentId: UUID
    name: string
}
