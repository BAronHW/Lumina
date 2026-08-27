import { useQuery } from "@tanstack/react-query";

export function useTraces() {
    return useQuery({
        queryKey: ['traces'],
        queryFn: () => fetch('/api/traces').then((res) => res.json())
    })
}
