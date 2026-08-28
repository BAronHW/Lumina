import type { Trace } from "@lumina/sdk";
import { useQuery } from "@tanstack/react-query";

export function useTraces(page?: number, pageSize?: number) {
    const params = new URLSearchParams();
    if (page !== undefined) params.set('page', page.toString());
    if (pageSize !== undefined) params.set('pageSize', pageSize.toString());
    const query = params.toString();

    return useQuery<Trace[]>({
        queryKey: ['traces', { page, pageSize }],
        queryFn: () => fetch(`/api/traces${query ? `?${query}` : ''}`).then((res) => res.json())
    })
}
