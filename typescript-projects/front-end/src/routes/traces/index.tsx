import { createFileRoute, useNavigate } from '@tanstack/react-router'
import { Center, Container, Group, Loader, Pagination, Select, Stack, Text } from '@mantine/core'
import { DatePickerInput } from '@mantine/dates'
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
  { header: 'Status', accessor: (row) => <GenericBadge status={ row.status } /> },
  { header: 'Cost', accessor: (row) => row.totalCostUsd != null ? `$${row.totalCostUsd.toFixed(4)}` : '—' },
  { header: 'Started', accessor: (row) => new Date(row.startedAt).toLocaleString() },
  { header: 'Ended', accessor: (row) => row.endedAt ? new Date(row.endedAt).toLocaleString() : '—' },
]

const PAGE_SIZE = 25;

function TracesPage() {
  const [page, setPage] = useState(1);
  const [status, setStatus] = useState<string | null>(null);
  const [from, setFrom] = useState<Date | null>(null);
  const [to, setTo] = useState<Date | null>(null);
  const navigate = useNavigate();

  const { isPending, error, data } = useTraces({
    page,
    pageSize: PAGE_SIZE,
    status: status ?? undefined,
    from: from?.toISOString(),
    to: to?.toISOString(),
  })

  const handleFilterChange = () => setPage(1);

  return (
    <Container size="xl">
      { isPending
        ? <Center h="50vh"><Loader/></Center>
        : error
        ? <Center h="50vh"><Text c="red">Failed to load traces: {error.message}</Text></Center>
        : <><h1>Traces</h1>
          <Group mb="md">
            <Select
              placeholder="Status"
              data={[
                { value: 'ok', label: 'Ok' },
                { value: 'error', label: 'Error' },
              ]}
              value={status}
              onChange={(v) => { setStatus(v); handleFilterChange(); }}
              clearable
              w={120}
            />
            <DatePickerInput
              placeholder="From"
              value={from}
              onChange={(v) => { setFrom(v); handleFilterChange(); }}
              clearable
              w={160}
            />
            <DatePickerInput
              placeholder="To"
              value={to}
              onChange={(v) => { setTo(v); handleFilterChange(); }}
              clearable
              w={160}
            />
          </Group>
          <Stack align="center" gap="md">
            <GenericTable<Trace>
              columns={columns}
              rows={data?.items ?? []}
              getRowKey={(row) => row.id}
              onRowClick={(row) => navigate({ to: '/traces/$traceId', params: { traceId: row.id } })}
            />
            <Pagination total={Math.ceil((data?.total ?? 0) / PAGE_SIZE)} value={page} onChange={setPage} />
          </Stack>
        </> }
    </Container>
  )
}
