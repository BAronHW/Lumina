import type { UUID } from "node:crypto";
import type { Deployment } from "../deployment/deployment";
import type { Agent } from "../agent/agent";
import type { Session } from "../session/session";
import type { Trace } from "../trace/trace";
import type { Span } from "../span/span";
import type { Prompt } from "../prompt/prompt";
import type { SpanStatus } from "../types";

export type CreateTraceInput = {
    id: UUID;
    agentId: UUID;
    sessionId?: UUID;
    name: string;
    status: SpanStatus;
    startedAt: Date;
    endedAt?: Date;
    totalCostUsd?: number;
    tags: Record<string, string>;
};

export type UpdateTraceInput = CreateTraceInput;

export interface LuminaHttpClient {
    createDeployment(name: string): Promise<Deployment>;
    getDeployment(id: UUID): Promise<Deployment | null>;
    updateDeployment(id: UUID, name: string): Promise<void>;
    deleteDeployment(id: UUID): Promise<void>;
    listDeployments(page: number, pageSize: number): Promise<Deployment[]>;

    createAgent(deploymentId: UUID, name: string): Promise<Agent>;
    getAgent(id: UUID): Promise<Agent | null>;
    listAgentsByDeployment(deploymentId: UUID): Promise<Agent[]>;
    updateAgent(id: UUID, name: string): Promise<void>;
    deleteAgent(id: UUID): Promise<void>;

    createSession(agentId: UUID, name: string): Promise<Session>;
    getSession(id: UUID): Promise<Session | null>;
    listSessionsByAgent(agentId: UUID): Promise<Session[]>;
    endSession(id: UUID): Promise<void>;
    deleteSession(id: UUID): Promise<void>;

    createTrace(input: CreateTraceInput): Promise<Trace>;
    getTrace(id: UUID): Promise<Trace | null>;
    updateTrace(id: UUID, input: UpdateTraceInput): Promise<void>;
    deleteTrace(id: UUID): Promise<void>;
    batchCreateTraces(inputs: CreateTraceInput[]): Promise<void>;
    batchUpdateTraces(inputs: (UpdateTraceInput & { id: UUID })[]): Promise<void>;
    listTraces(page: number, pageSize: number): Promise<Trace[]>;
    listTracesByAgent(agentId: UUID): Promise<Trace[]>;
    listFinishedTraces(page: number, pageSize: number): Promise<Trace[]>;

    getSpan(id: UUID): Promise<Span | null>;
    listSpans(page: number, pageSize: number): Promise<Span[]>;

    ingestSpans(spans: Span[]): Promise<void>;

    createPrompt(name: string, content: string): Promise<Prompt>;
    getPrompt(id: UUID): Promise<Prompt | null>;
    updatePrompt(id: UUID, name: string, content: string): Promise<void>;
    deletePrompt(id: UUID): Promise<void>;
}

