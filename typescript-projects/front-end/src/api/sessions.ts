import { useQuery } from "@tanstack/react-query";

export interface Session {
    id: string
    agentId: string
    name: string
    createdAt: string
    endedAt: string | null
}

export interface SessionsPage {
    items: Session[]
    total: number
}

export function useSessions(page?: number, pageSize?: number) {
    const params = new URLSearchParams()
    if (page !== undefined) params.set('page', String(page))
    if (pageSize !== undefined) params.set('pageSize', String(pageSize))
    const query = params.toString()

    return useQuery<SessionsPage>({
        queryKey: ['sessions', { page, pageSize }],
        queryFn: () => fetch(`/api/sessions${query ? `?${query}` : ''}`).then((res) => res.json())
    })
}

export function useSession(id: string) {
    return useQuery<Session>({
        queryKey: ['sessions', id],
        queryFn: () => fetch(`/api/sessions/${id}`).then((res) => res.json()),
        enabled: !!id,
    })
}
