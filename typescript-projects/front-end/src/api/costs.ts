import { useQuery } from "@tanstack/react-query";

export interface CostSummary {
    totalCost: number
    byModel: Record<string, number>
    byAgent: Record<string, number>
    byTag: Record<string, number>
}

export interface ExpensiveTrace {
    id: string
    name: string
    agentId: string
    totalCostUsd: number
    startedAt: string
}

export function useCosts(period?: string) {
    const params = new URLSearchParams()
    if (period) params.set('period', period)
    const query = params.toString()

    return useQuery<CostSummary>({
        queryKey: ['costs', { period }],
        queryFn: () => fetch(`/api/costs${query ? `?${query}` : ''}`).then((res) => res.json())
    })
}

export function useExpensiveTraces(period?: string) {
    const params = new URLSearchParams()
    if (period) params.set('period', period)
    const query = params.toString()

    return useQuery<ExpensiveTrace[]>({
        queryKey: ['costTraces', { period }],
        queryFn: () => fetch(`/api/costs/traces${query ? `?${query}` : ''}`).then((res) => res.json())
    })
}
