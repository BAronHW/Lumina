import { createFileRoute } from '@tanstack/react-router'
import { Center, Container, Loader, Stack, Text, Title } from '@mantine/core'
import { useCosts, useExpensiveTraces, type ExpensiveTrace } from '../../api/costs'
import { GenericTable, type ColumnDef } from '../../components/tables/GenericTable'

export const Route = createFileRoute('/costs/')({
  component: CostsPage,
})

const expensiveTraceColumns: ColumnDef<ExpensiveTrace>[] = [
  { header: 'Name', accessor: (row) => row.name },
  { header: 'Agent', accessor: (row) => row.agentId },
  { header: 'Cost', accessor: (row) => `$${row.totalCostUsd.toFixed(4)}` },
  { header: 'Started', accessor: (row) => new Date(row.startedAt).toLocaleString() },
]

function CostsPage() {
  const { isPending, error, data: costs } = useCosts()
  const { data: expensiveTraces } = useExpensiveTraces()

  return (
    <Container size="xl">
      { isPending
        ? <Center h="50vh"><Loader /></Center>
        : error
        ? <Center h="50vh"><Text c="red">Failed to load costs: {error.message}</Text></Center>
        : <><h1>Costs</h1>
          <Stack gap="md">
            <Text size="xl" fw={700}>Total Spend: ${costs?.totalCost?.toFixed(2) ?? '0.00'}</Text>
            <Title order={3}>Most Expensive Traces</Title>
            <Stack align="center">
              <GenericTable<ExpensiveTrace>
                columns={expensiveTraceColumns}
                rows={expensiveTraces ?? []}
                getRowKey={(row) => row.id}
              />
            </Stack>
          </Stack>
        </> }
    </Container>
  )
}
