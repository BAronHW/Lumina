import { AsyncLocalStorage } from "node:async_hooks";
import { LuminaSDK } from "../../api/tracer-sdk";
import { StartTraceBody } from "../../api/trace/trace";
import { Span, StartSpanBody } from "../../api/span/span";
import {
  LuminaHttpClient,
  UpdateTraceInput,
} from "../../api/http-client/http-client";
import { GenericBuffer } from "../../api/ingest-buffer/ingest-buffer";
import { Agent } from "../../api/agent/agent";
import { UUID } from "node:crypto";
import { SpanStatus } from "../../api/types";

type SpanContext = {
  traceId: string;
  spanId: string | null;
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
      id: crypto.randomUUID(),
      deploymentId: this.deploymentId,
      name: this.name,
    };
  }

  session<T>(name: string, callback: () => Promise<T>): Promise<T> {
    throw new Error("Not implemented");
  }

  async trace<T, K>(startTraceBody: StartTraceBody<T, K>): Promise<K> {
    const traceId = crypto.randomUUID();
    const startDate = new Date();
    const traceShape = {
      id: traceId,
      agentId: this.agentInstance.id,
      name: startTraceBody.name,
      status: "ok" as SpanStatus,
      startedAt: startDate,
      tags: startTraceBody.tags ?? {},
    };
    await this.httpClient.createTrace(traceShape);

    return this.als.run({ traceId, spanId: null }, async () => {
      try {
        const callbackRes = await startTraceBody.callback(startTraceBody.input);
        await this.httpClient.updateTrace({
          ...traceShape,
          endedAt: new Date(),
        });
        return callbackRes;
      } catch (err) {
        await this.httpClient.updateTrace({
          ...traceShape,
          status: "error" as SpanStatus,
          endedAt: new Date(),
        });
        throw err;
      }
    });
  }

  span<T, K>(startSpanBody: StartSpanBody<T, K>): Promise<K> {
    throw new Error("Not implemented");
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
