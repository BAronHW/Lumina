import { UUID } from "node:crypto"

export interface Deployment {
    id: UUID
    name: string
}
