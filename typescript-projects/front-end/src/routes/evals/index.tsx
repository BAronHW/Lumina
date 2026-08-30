import { createFileRoute } from '@tanstack/react-router'
import { Center, Container, Loader, Stack, Text } from '@mantine/core'
import { useEvalRuns, type EvalRun } from '../../api/evals'
import { GenericTable, type ColumnDef } from '../../components/tables/GenericTable'

export const Route = createFileRoute('/evals/')({
  component: EvalsPage,
})

const columns: ColumnDef<EvalRun>[] = [
  { header: 'Name', accessor: (row) => row.evalName },
  { header: 'Scorer', accessor: (row) => row.scorerType },
  { header: 'Created', accessor: (row) => new Date(row.createdAt).toLocaleString() },
]

function EvalsPage() {
  const { isPending, error, data } = useEvalRuns()

  return (
    <Container size="xl">
      { isPending
        ? <Center h="50vh"><Loader /></Center>
        : error
        ? <Center h="50vh"><Text c="red">Failed to load eval runs: {error.message}</Text></Center>
        : <><h1>Evals</h1>
          <Stack align="center" gap="md">
            <GenericTable<EvalRun>
              columns={columns}
              rows={data ?? []}
              getRowKey={(row) => row.id}
            />
          </Stack>
        </> }
    </Container>
  )
}
