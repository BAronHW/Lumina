import type { Trace } from "@lumina/sdk";
import { useQuery } from "@tanstack/react-query";

export interface Span {
    spanId: string
    traceId: string
    parentSpanId: string | null
    name: string
    kind: string
    status: string
    error: string | null
    startedAt: string
    endedAt: string | null
    durationMs: number | null
    input: Record<string, unknown>
    output: Record<string, unknown>
    attributes: Record<string, unknown>
}

export interface TraceWithSpans {
    trace: Trace
    spans: Span[]
}

export interface TracesPage {
    items: Trace[];
    total: number;
}

export function useTraces(page?: number, pageSize?: number) {
    const params = new URLSearchParams();
    if (page !== undefined) params.set('page', page.toString());
    if (pageSize !== undefined) params.set('pageSize', pageSize.toString());
    const query = params.toString();

    return useQuery<TracesPage>({
        queryKey: ['traces', { page, pageSize }],
        queryFn: () => fetch(`/api/traces${query ? `?${query}` : ''}`).then((res) => res.json())
    })
}

export function useTrace(traceId: string) {
    return useQuery<TraceWithSpans>({
        queryKey: ['traces', traceId],
        queryFn: () => fetch(`/api/traces/${traceId}`).then((res) => res.json()),
        enabled: !!traceId,
    })
}
