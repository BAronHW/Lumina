import { LuminaHttpClient } from "../../api/http-client/http-client";
import { GenericBuffer } from "../../api/ingest-buffer/ingest-buffer";
import { Span } from "../../api/span/span";

export class IngestBufferImpl implements GenericBuffer<Span> {
  private buffer: Span[] = [];
  private flushing = false;
  private readonly httpClient: LuminaHttpClient;
  private timer: ReturnType<typeof setInterval>;

  constructor(httpClient: LuminaHttpClient, flushInterval: number) {
    this.httpClient = httpClient;
    this.timer = setInterval(() => this.flush().catch(() => {}), flushInterval);
  }

  add(span: Span): void {
    this.buffer.push(span);
  }

  async flush(): Promise<void> {
    if (this.flushing || this.buffer.length === 0) return;
    this.flushing = true;
    try {
      // spans stay in the buffer until the send succeeds, so a failed
      // flush retries them on the next tick instead of dropping them
      const count = this.buffer.length;
      await this.httpClient.ingestSpans(this.buffer.slice(0, count));
      this.buffer.splice(0, count);
    } finally {
      this.flushing = false;
    }
  }

  async shutdown(): Promise<void> {
    clearInterval(this.timer);
    await this.flush();
  }
}
