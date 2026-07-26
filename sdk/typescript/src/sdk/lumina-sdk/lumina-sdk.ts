import { AsyncLocalStorage } from "node:async_hooks";
import { LuminaSDK } from "../../api/tracer-sdk";
import { StartTraceBody } from "../../api/trace/trace";
import { Span, StartSpanBody } from "../../api/span/span";
import { LuminaHttpClient } from "../../api/http-client/http-client";
import { GenericBuffer } from "../../api/ingest-buffer/ingest-buffer";
import { Agent } from "../../api/agent/agent";
import { UUID, randomUUID } from "node:crypto";

type SpanContext = {
  traceId: UUID;
  spanId: UUID | null;
  span: Span | null;
  sessionId: UUID | null;
  tags: Record<string, unknown>;
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

  /**
   * 1. Create traceId, rootSpanId, startDate.
   * 2. Build the root span (parentSpanId: null, carries name/agentId/sessionId/tags).
   * 3. Seed ALS with the root span so child spans parent to it.
   * 4. Run the callback (children create + buffer THEMSELVES via span()).
   * 5. On finish: set rootSpan status/error/endedAt/output.
   * 6. Push the root span to the ingest buffer — its parentSpanId:null + endedAt
   *    is what signals the backend the trace is complete.
   */
  async trace<T, K>(startTraceBody: StartTraceBody<T, K>): Promise<K> {
    const traceId = randomUUID();
    const rootSpanId = randomUUID();
    const startDate = new Date();

    const sessionId = startTraceBody.sessionId ?? null;
    const tags = startTraceBody.tags ?? {};

    const rootSpan: Span = {
      spanId: rootSpanId,
      traceId: traceId,
      parentSpanId: null,
      name: startTraceBody.name,
      kind: "Trace",
      status: "ok",
      error: null,
      startedAt: startDate,
      endedAt: null,
      durationMs: null,
      input: startTraceBody.input as Record<string, unknown>,
      output: {},
      attributes: {},
      agentId: this.agentInstance.id,
      sessionId,
      tags,
    };

    return this.als.run(
      { traceId, spanId: rootSpanId, span: rootSpan, sessionId, tags },
      async () => {
        const run = async (): Promise<
          { result: K; error: null } | { result: undefined; error: unknown }
        > => {
          try {
            const result = await startTraceBody.callback(startTraceBody.input);
            return { result, error: null };
          } catch (err) {
            return { result: undefined, error: err };
          }
        };

        const { result, error } = await run();
        const endedAt = new Date();

        if (error) {
          rootSpan.status = "error";
          rootSpan.error =
            error instanceof Error ? error.message : String(error);
        }
        rootSpan.endedAt = endedAt;
        rootSpan.durationMs = endedAt.getTime() - startDate.getTime();
        rootSpan.output = (result as Record<string, unknown>) ?? {};

        this.ingestBuffer.add(rootSpan);

        if (error) throw error;
        return result as K;
      },
    );
  }

  async span<T, K>(startSpanBody: StartSpanBody<T, K>): Promise<K> {
    const spanId = randomUUID();
    const startDate = new Date();
    const store = this.als.getStore();

    if (!store?.traceId) {
      throw new Error("span() must be called within an active trace");
    }

    const span: Span = {
      spanId,
      traceId: store.traceId,
      parentSpanId: store.spanId ?? null,
      name: startSpanBody.name,
      kind: startSpanBody.kind,
      status: "ok",
      error: null,
      startedAt: startDate,
      endedAt: null,
      durationMs: null,
      input: startSpanBody.input as Record<string, unknown>,
      output: {},
      attributes: startSpanBody.attributes ?? {},
      agentId: this.agentInstance.id,
      sessionId: store.sessionId,
      tags: store.tags,
    };

    return this.als.run(
      {
        traceId: store.traceId,
        spanId,
        span,
        sessionId: store.sessionId,
        tags: store.tags,
      },
      async () => {
        const run = async (): Promise<
          { result: K; error: null } | { result: undefined; error: unknown }
        > => {
          try {
            const result = await startSpanBody.callback(startSpanBody.input);
            return { result, error: null };
          } catch (err) {
            return { result: undefined, error: err };
          }
        };

        const { result, error: callbackError } = await run();
        const endedAt = new Date();

        // only overwrite on failure a success must not erase what
        // recordError() set during the callback
        if (callbackError) {
          span.status = "error";
          span.error =
            callbackError instanceof Error
              ? callbackError.message
              : String(callbackError);
        }
        span.endedAt = endedAt;
        span.durationMs = endedAt.getTime() - startDate.getTime();
        span.output = (result as Record<string, unknown>) ?? {};

        this.ingestBuffer.add(span);

        if (callbackError) throw callbackError;
        return result as K;
      },
    );
  }

  addAttribute(key: string, value: unknown): void {
    const store = this.als.getStore();
    if (store?.span) {
      store.span.attributes[key] = value;
    }
  }

  recordError(message: string): void {
    const store = this.als.getStore();
    if (store?.span) {
      store.span.error = message;
      store.span.status = "error";
    }
  }

  async shutdown(): Promise<void> {
    await this.ingestBuffer.shutdown();
  }
}
