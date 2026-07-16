import type { UUID } from "node:crypto";
import type { Deployment } from "../api/deployment/deployment";
import type { Agent } from "../api/agent/agent";
import type { Session } from "../api/session/session";
import type { Trace } from "../api/trace/trace";
import type { Span } from "../api/span/span";
import type { Prompt } from "../api/prompt/prompt";
import type { CreateTraceInput, LuminaHttpClient, UpdateTraceInput } from "../api/http-client/http-client";

export class LuminaHttpClientImpl implements LuminaHttpClient {
    private readonly baseUrl: string;

    constructor(baseUrl: string) {
        this.baseUrl = baseUrl;
    }

    private async request(method: string, path: string, body?: unknown): Promise<Response> {
        const headers: Record<string, string> = {};
        if (body !== undefined) {
            headers["Content-Type"] = "application/json";
        }
        return fetch(`${this.baseUrl}${path}`, {
            method,
            headers,
            body: body !== undefined ? JSON.stringify(body) : undefined,
        });
    }

    private async requestJson<T>(method: string, path: string, body?: unknown): Promise<T> {
        const response = await this.request(method, path, body);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json() as Promise<T>;
    }

    private async requestVoid(method: string, path: string, body?: unknown): Promise<void> {
        const response = await this.request(method, path, body);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
    }

    private async requestNullable<T>(path: string): Promise<T | null> {
        const response = await this.request("GET", path);
        if (response.status === 404) return null;
        if (!response.ok) throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        return response.json() as Promise<T>;
    }

    private parseDeployment(raw: any): Deployment {
        return raw as Deployment;
    }

    private parseSession(raw: any): Session {
        return {
            ...raw,
            createdAt: new Date(raw.createdAt),
            endedAt: raw.endedAt ? new Date(raw.endedAt) : null,
        };
    }

    private parseTrace(raw: any): Trace {
        return {
            ...raw,
            startedAt: new Date(raw.startedAt),
            endedAt: raw.endedAt ? new Date(raw.endedAt) : null,
        };
    }

    private parseSpan(raw: any): Span {
        return {
            ...raw,
            startedAt: new Date(raw.startedAt),
            endedAt: raw.endedAt ? new Date(raw.endedAt) : null,
        };
    }

    private serializeTrace(input: CreateTraceInput): Record<string, unknown> {
        return {
            ...input,
            startedAt: input.startedAt.toISOString(),
            endedAt: input.endedAt ? input.endedAt.toISOString() : undefined,
        };
    }

    private serializeSpan(span: Span): Record<string, unknown> {
        return {
            ...span,
            startedAt: span.startedAt.toISOString(),
            endedAt: span.endedAt ? span.endedAt.toISOString() : null,
        };
    }

    async createDeployment(name: string): Promise<Deployment> {
        const raw = await this.requestJson<unknown>("POST", "/deployments", { name });
        return this.parseDeployment(raw);
    }

    async getDeployment(id: UUID): Promise<Deployment | null> {
        const raw = await this.requestNullable<unknown>(`/deployments/${id}`);
        return raw !== null ? this.parseDeployment(raw) : null;
    }

    async updateDeployment(id: UUID, name: string): Promise<void> {
        await this.requestVoid("PUT", `/deployments/${id}`, { name });
    }

    async deleteDeployment(id: UUID): Promise<void> {
        await this.requestVoid("DELETE", `/deployments/${id}`);
    }

    async listDeployments(page: number, pageSize: number): Promise<Deployment[]> {
        const raw = await this.requestJson<unknown[]>("GET", `/deployments?page=${page}&pageSize=${pageSize}`);
        return raw.map((r) => this.parseDeployment(r));
    }

    async createAgent(deploymentId: UUID, name: string): Promise<Agent> {
        return this.requestJson<Agent>("POST", "/agents", { deploymentId, name });
    }

    async getAgent(id: UUID): Promise<Agent | null> {
        return this.requestNullable<Agent>(`/agents/${id}`);
    }

    async listAgentsByDeployment(deploymentId: UUID): Promise<Agent[]> {
        return this.requestJson<Agent[]>("GET", `/deployments/${deploymentId}/agents`);
    }

    async updateAgent(id: UUID, name: string): Promise<void> {
        await this.requestVoid("PUT", `/agents/${id}`, { name });
    }

    async deleteAgent(id: UUID): Promise<void> {
        await this.requestVoid("DELETE", `/agents/${id}`);
    }

    async createSession(agentId: UUID, name: string): Promise<Session> {
        const raw = await this.requestJson<unknown>("POST", "/sessions", { agentId, name });
        return this.parseSession(raw);
    }

    async getSession(id: UUID): Promise<Session | null> {
        const raw = await this.requestNullable<unknown>(`/sessions/${id}`);
        return raw !== null ? this.parseSession(raw) : null;
    }

    async listSessionsByAgent(agentId: UUID): Promise<Session[]> {
        const raw = await this.requestJson<unknown[]>("GET", `/agents/${agentId}/sessions`);
        return raw.map((r) => this.parseSession(r));
    }

    async endSession(id: UUID): Promise<void> {
        await this.requestVoid("PUT", `/sessions/${id}/end`);
    }

    async deleteSession(id: UUID): Promise<void> {
        await this.requestVoid("DELETE", `/sessions/${id}`);
    }

    async createTrace(input: CreateTraceInput): Promise<Trace> {
        const raw = await this.requestJson<unknown>("POST", "/traces", this.serializeTrace(input));
        return this.parseTrace(raw);
    }

    async getTrace(id: UUID): Promise<Trace | null> {
        const raw = await this.requestNullable<unknown>(`/traces/${id}`);
        return raw !== null ? this.parseTrace(raw) : null;
    }

    async updateTrace(id: UUID, input: UpdateTraceInput): Promise<void> {
        await this.requestVoid("PUT", `/traces/${id}`, this.serializeTrace(input));
    }

    async deleteTrace(id: UUID): Promise<void> {
        await this.requestVoid("DELETE", `/traces/${id}`);
    }

    async batchCreateTraces(inputs: CreateTraceInput[]): Promise<void> {
        await this.requestVoid("POST", "/traces/batch", inputs.map((i) => this.serializeTrace(i)));
    }

    async batchUpdateTraces(inputs: (UpdateTraceInput & { id: UUID })[]): Promise<void> {
        await this.requestVoid(
            "PUT",
            "/traces/batch",
            inputs.map((input) => {
                const { id, ...rest } = input;
                return { id, ...this.serializeTrace(rest) };
            }),
        );
    }

    async listTraces(page: number, pageSize: number): Promise<Trace[]> {
        const raw = await this.requestJson<unknown[]>("GET", `/traces?page=${page}&pageSize=${pageSize}`);
        return raw.map((r) => this.parseTrace(r));
    }

    async listTracesByAgent(agentId: UUID): Promise<Trace[]> {
        const raw = await this.requestJson<unknown[]>("GET", `/agents/${agentId}/traces`);
        return raw.map((r) => this.parseTrace(r));
    }

    async listFinishedTraces(page: number, pageSize: number): Promise<Trace[]> {
        const raw = await this.requestJson<unknown[]>("GET", `/traces/finished?page=${page}&pageSize=${pageSize}`);
        return raw.map((r) => this.parseTrace(r));
    }

    async getSpan(id: UUID): Promise<Span | null> {
        const raw = await this.requestNullable<unknown>(`/spans/${id}`);
        return raw !== null ? this.parseSpan(raw) : null;
    }

    async listSpans(page: number, pageSize: number): Promise<Span[]> {
        const raw = await this.requestJson<unknown[]>("GET", `/spans?page=${page}&pageSize=${pageSize}`);
        return raw.map((r) => this.parseSpan(r));
    }

    async ingestSpans(spans: Span[]): Promise<void> {
        await this.requestVoid("POST", "/ingest/spans", { spans: spans.map((s) => this.serializeSpan(s)) });
    }

    async createPrompt(name: string, content: string): Promise<Prompt> {
        return this.requestJson<Prompt>("POST", "/prompt", { name, content });
    }

    async getPrompt(id: UUID): Promise<Prompt | null> {
        return this.requestNullable<Prompt>(`/prompt/${id}`);
    }

    async updatePrompt(id: UUID, name: string, content: string): Promise<void> {
        await this.requestVoid("PUT", `/prompt/${id}`, { name, content });
    }

    async deletePrompt(id: UUID): Promise<void> {
        await this.requestVoid("DELETE", `/prompt/${id}`);
    }
}
