import { useQuery } from "@tanstack/react-query";

export interface Prompt {
    id: string
    name: string
    prompt: string
}

export interface PromptsPage {
    items: Prompt[]
    total: number
}

export function usePrompts(page?: number, pageSize?: number) {
    const params = new URLSearchParams()
    if (page !== undefined) params.set('page', String(page))
    if (pageSize !== undefined) params.set('pageSize', String(pageSize))
    const query = params.toString()

    return useQuery<PromptsPage>({
        queryKey: ['prompts', { page, pageSize }],
        queryFn: () => fetch(`/api/prompts${query ? `?${query}` : ''}`).then((res) => res.json())
    })
}

export function usePrompt(id: string) {
    return useQuery<Prompt>({
        queryKey: ['prompts', id],
        queryFn: () => fetch(`/api/prompts/${id}`).then((res) => res.json()),
        enabled: !!id,
    })
}
