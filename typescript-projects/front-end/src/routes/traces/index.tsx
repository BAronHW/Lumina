import { createFileRoute } from '@tanstack/react-router'
import { Container, Loader, Pagination, Stack, Text } from '@mantine/core'
import { useTraces } from '../../api/traces'
import { GenericTable, type ColumnDef } from '../../components/tables/GenericTable'
import type { Trace } from '@lumina/sdk'
import { useState } from 'react'

export const Route = createFileRoute('/traces/')({
  component: TracesPage,
})

const columns: ColumnDef<Trace>[] = [
  { header: 'Id', accessor: (row) => row.id },
  { header: 'Name', accessor: (row) => row.name },
  { header: 'Agent', accessor: (row) => row.agentId },
  { header: 'Session', accessor: (row) => row.sessionId ?? '—' },
  { header: 'Status', accessor: (row) => row.status },
  { header: 'Cost', accessor: (row) => row.totalCostUsd != null ? `$${row.totalCostUsd.toFixed(4)}` : '—' },
  { header: 'Started', accessor: (row) => new Date(row.startedAt).toLocaleString() },
  { header: 'Ended', accessor: (row) => row.endedAt ? new Date(row.endedAt).toLocaleString() : '—' },
]

function TracesPage() {
  const [page, setPage] = useState(1);
  const { isPending, error, data } = useTraces(page, 25)

  if (isPending) return <Loader />
  if (error) return <Text c="red">Failed to load traces: {error.message}</Text>

  return (
    <Container size="xl">
      <h1>Traces</h1>
      <Stack align="center" gap="md">
        <GenericTable<Trace>
          columns={columns}
          rows={data ?? []}
          getRowKey={(row) => row.id}
        />
        <Pagination total={10} value={page} onChange={setPage} />
      </Stack>
    </Container>
  )
}
