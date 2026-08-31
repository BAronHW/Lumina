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

export interface TraceFilters {
    page?: number;
    pageSize?: number;
    status?: string;
    from?: string;
    to?: string;
}

export function useTraces(filters: TraceFilters = {}) {
    const params = new URLSearchParams();
    if (filters.page !== undefined) params.set('page', filters.page.toString());
    if (filters.pageSize !== undefined) params.set('pageSize', filters.pageSize.toString());
    if (filters.status) params.set('status', filters.status);
    if (filters.from) params.set('from', filters.from);
    if (filters.to) params.set('to', filters.to);
    const query = params.toString();

    return useQuery<TracesPage>({
        queryKey: ['traces', filters],
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
