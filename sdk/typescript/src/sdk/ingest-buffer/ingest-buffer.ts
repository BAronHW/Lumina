import { LuminaHttpClient } from '../../api/http-client/http-client';
import { GenericBuffer } from '../../api/ingest-buffer/ingest-buffer'
import { Span } from '../../api/span/span'

export class IngestBufferImpl implements GenericBuffer<Span> {

    private buffer: Span[] = [];
    private readonly httpClient: LuminaHttpClient;
    private timer: ReturnType<typeof setInterval>

    constructor(httpClient: LuminaHttpClient, flushInterval: number) {
        this.httpClient = httpClient;
        this.timer = setInterval(() => this.flush(), flushInterval);
    }

    add(spanArr: Span[]): void {
        this.buffer.push(...spanArr);
    }

    flush() : Promise<void> {
        return new Promise((resolve, _) => {
            const spansToSend = this.buffer;
            this.buffer = [];
            resolve(
                this.httpClient.ingestSpans(spansToSend)
            );
        });
    }

    shutdown(): Promise<void> {
        return new Promise( async (resolve, _) => {
            clearInterval(this.timer);
            return this.flush()
        });
    }
}