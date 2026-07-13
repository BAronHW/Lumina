import { UUID } from "node:crypto"

export interface Prompt {
    id: UUID
    name: string
    prompt: string
}

export interface PromptVersion {
    id: UUID
    promptId: UUID
    version: number
    content: string
    variables: string[]
    notes: string | null
    createdAt: Date
}
