import { createFileRoute } from '@tanstack/react-router'
import { Center, Container, Loader, Pagination, Stack, Text } from '@mantine/core'
import { useState } from 'react'
import { usePrompts, type Prompt } from '../../api/prompts'
import { GenericTable, type ColumnDef } from '../../components/tables/GenericTable'

export const Route = createFileRoute('/prompts/')({
  component: PromptsPage,
})

const PAGE_SIZE = 20

const columns: ColumnDef<Prompt>[] = [
  { header: 'Name', accessor: (row) => row.name },
  { header: 'Content', accessor: (row) => row.prompt.length > 80 ? row.prompt.slice(0, 80) + '...' : row.prompt },
]

function PromptsPage() {
  const [page, setPage] = useState(1)
  const { isPending, error, data } = usePrompts(page, PAGE_SIZE)

  return (
    <Container size="xl">
      { isPending
        ? <Center h="50vh"><Loader /></Center>
        : error
        ? <Center h="50vh"><Text c="red">Failed to load prompts: {error.message}</Text></Center>
        : <><h1>Prompts</h1>
          <Stack align="center" gap="md">
            <GenericTable<Prompt>
              columns={columns}
              rows={data?.items ?? []}
              getRowKey={(row) => row.id}
            />
            <Pagination total={Math.ceil((data?.total ?? 0) / PAGE_SIZE)} value={page} onChange={setPage} />
          </Stack>
        </> }
    </Container>
  )
}
