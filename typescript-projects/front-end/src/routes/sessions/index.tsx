import { createFileRoute } from '@tanstack/react-router'
import { Center, Container, Loader, Pagination, Stack, Text } from '@mantine/core'
import { useState } from 'react'
import { useSessions, type Session } from '../../api/sessions'
import { GenericTable, type ColumnDef } from '../../components/tables/GenericTable'

export const Route = createFileRoute('/sessions/')({
  component: SessionsPage,
})

const PAGE_SIZE = 20

const columns: ColumnDef<Session>[] = [
  { header: 'Name', accessor: (row) => row.name },
  { header: 'Agent', accessor: (row) => row.agentId },
  { header: 'Created', accessor: (row) => new Date(row.createdAt).toLocaleString() },
  { header: 'Ended', accessor: (row) => row.endedAt ? new Date(row.endedAt).toLocaleString() : '—' },
]

function SessionsPage() {
  const [page, setPage] = useState(1)
  const { isPending, error, data } = useSessions(page, PAGE_SIZE)

  return (
    <Container size="xl">
      { isPending
        ? <Center h="50vh"><Loader /></Center>
        : error
        ? <Center h="50vh"><Text c="red">Failed to load sessions: {error.message}</Text></Center>
        : <><h1>Sessions</h1>
          <Stack align="center" gap="md">
            <GenericTable<Session>
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
