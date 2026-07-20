import { AsyncLocalStorage } from "node:async_hooks";
import { LuminaSDK } from "../../api/tracer-sdk";
import { Trace, StartTraceBody } from "../../api/trace/trace";
import { Span, StartSpanBody } from "../../api/span/span";
import {
  LuminaHttpClient,
  UpdateTraceInput,
} from "../../api/http-client/http-client";
import { GenericBuffer } from "../../api/ingest-buffer/ingest-buffer";
import { Agent } from "../../api/agent/agent";
import { UUID, randomUUID } from "node:crypto";
import { SpanStatus } from "../../api/types";

type SpanContext = {
  traceId: UUID;
  spanId: UUID | null;
};

export class LuminaSDKImpl implements LuminaSDK {
  private readonly als = new AsyncLocalStorage<SpanContext>();
  private readonly httpClient: LuminaHttpClient;
  private readonly ingestBuffer: GenericBuffer<Span>;
  private readonly agentInstance: Agent;
  private readonly name: string;
  private readonly deploymentId: UUID;

  constructor(
    httpClient: LuminaHttpClient,
    ingestBuffer: GenericBuffer<Span>,
    name: string,
    deploymentId: UUID,
  ) {
    this.httpClient = httpClient;
    this.ingestBuffer = ingestBuffer;
    this.name = name;
    this.deploymentId = deploymentId;

    this.agentInstance = {
      id: randomUUID(),
      deploymentId: this.deploymentId,
      name: this.name,
    };
  }

  session<T>(name: string, callback: () => Promise<T>): Promise<T> {
    throw new Error("Not implemented");
  }

  async trace<T, K>(startTraceBody: StartTraceBody<T, K>): Promise<K> {
    const traceId = randomUUID();
    const startDate = new Date();
    const baseTrace = {
      id: traceId,
      agentId: this.agentInstance.id,
      sessionId: startTraceBody.sessionId,
      name: startTraceBody.name,
      status: "ok" as SpanStatus,
      startedAt: startDate,
      tags: startTraceBody.tags ?? {},
    };
    await this.httpClient.createTrace(baseTrace);

    return this.als.run({ traceId, spanId: null }, async () => {
      const run = async (): Promise<{ result: K; error: null } | { result: undefined; error: unknown }> => {
        try {
          const result = await startTraceBody.callback(startTraceBody.input);
          return { result, error: null };
        } catch (err) {
          return { result: undefined, error: err };
        }
      };

      const { result, error: callbackError } = await run();
      const status: SpanStatus = callbackError ? "error" : "ok";
      const endedAt = new Date();

      await this.httpClient.updateTrace({ ...baseTrace, status, endedAt });

      if (callbackError) throw callbackError;
      return result as K;
    });
  }

  async span<T, K>(startSpanBody: StartSpanBody<T, K>): Promise<K> {
    const spanId = randomUUID();
    const startDate = new Date();
    const store = this.als.getStore();

    if (!store?.traceId) {
      throw new Error("span() must be called within an active trace");
    }

    return this.als.run({ traceId: store.traceId, spanId }, async () => {
      const run = async (): Promise<{ result: K; error: null } | { result: undefined; error: unknown }> => {
        try {
          const result = await startSpanBody.callback(startSpanBody.input);
          return { result, error: null };
        } catch (err) {
          return { result: undefined, error: err };
        }
      };

      const { result, error: callbackError } = await run();
      const status: SpanStatus = callbackError ? "error" : "ok";
      const errorMessage = callbackError instanceof Error ? callbackError.message : callbackError ? String(callbackError) : null;
      const endedAt = new Date();

      const span: Span = {
        id: spanId,
        traceId: store.traceId,
        parentSpanId: store.spanId ?? null,
        name: startSpanBody.name,
        kind: startSpanBody.kind,
        status,
        error: errorMessage,
        startedAt: startDate,
        endedAt,
        durationMs: endedAt.getTime() - startDate.getTime(),
        input: startSpanBody.input as Record<string, unknown>,
        output: result as Record<string, unknown> ?? {},
        attributes: startSpanBody.attributes ?? {},
      };

      this.ingestBuffer.add(span);

      if (callbackError) throw callbackError;
      return result as K;
    });
  }

  addAttribute(key: string, value: unknown): void {
    throw new Error("Not implemented");
  }

  recordError(message: string): void {
    throw new Error("Not implemented");
  }

  shutdown(): Promise<void> {
    throw new Error("Not implemented");
  }
}
