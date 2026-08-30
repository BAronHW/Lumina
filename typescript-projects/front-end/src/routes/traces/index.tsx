import { createFileRoute } from '@tanstack/react-router'
import { Center, Container, Loader, Pagination, Stack, Text } from '@mantine/core'
import { useTraces } from '../../api/traces'
import { GenericTable, type ColumnDef } from '../../components/tables/GenericTable'
import type { Trace } from '@lumina/sdk'
import { useState } from 'react'
import GenericBadge from '../../components/badge/GenericBadge'

export const Route = createFileRoute('/traces/')({
  component: TracesPage,
})

const columns: ColumnDef<Trace>[] = [
  { header: 'Id', accessor: (row) => row.id },
  { header: 'Name', accessor: (row) => row.name },
  { header: 'Agent', accessor: (row) => row.agentId },
  { header: 'Session', accessor: (row) => row.sessionId ?? '—' },
  { header: 'Status', accessor: (row) => <GenericBadge colour={row.status === 'ok' ? 'green' : row.status === 'error' ? 'red' : 'orange'} label={ row.status }/> },
  { header: 'Cost', accessor: (row) => row.totalCostUsd != null ? `$${row.totalCostUsd.toFixed(4)}` : '—' },
  { header: 'Started', accessor: (row) => new Date(row.startedAt).toLocaleString() },
  { header: 'Ended', accessor: (row) => row.endedAt ? new Date(row.endedAt).toLocaleString() : '—' },
]

function TracesPage() {
  const [page, setPage] = useState(1);
  const { isPending, error, data } = useTraces(page, 25)

  return (
    <Container size="xl">
      { isPending
        ? <Center h="50vh"><Loader/></Center>
        : error
        ? <Center h="50vh"><Text c="red">Failed to load traces: {error.message}</Text></Center>
        : <><h1>Traces</h1>
          <Stack align="center" gap="md">
            <GenericTable<Trace>
              columns={columns}
              rows={data?.items ?? []}
              getRowKey={(row) => row.id}
            />
            <Pagination total={Math.ceil((data?.total ?? 0) / 25)} value={page} onChange={setPage} />
          </Stack>
        </> }
    </Container>
  )
}
