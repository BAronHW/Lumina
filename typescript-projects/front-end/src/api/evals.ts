import { useQuery } from "@tanstack/react-query";

export interface EvalRun {
    id: string
    scorerType: string
    evalName: string
    createdAt: string
}

export interface EvalResult {
    id: string
    traceId: string
    spanId: string
    evalName: string
    scorerType: string
    passed: boolean
    score: number | null
    reason: string | null
    runAt: string
}

export function useEvalRuns() {
    return useQuery<EvalRun[]>({
        queryKey: ['evalRuns'],
        queryFn: () => fetch('/api/evals/runs').then((res) => res.json())
    })
}

export function useEvalResults() {
    return useQuery<EvalResult[]>({
        queryKey: ['evalResults'],
        queryFn: () => fetch('/api/evals/results').then((res) => res.json())
    })
}
