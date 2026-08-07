import { UUID } from "node:crypto"

export interface DeploymentVersion {
    id: UUID
    deploymentId: UUID
    version: string
    deployedAt: Date
}
