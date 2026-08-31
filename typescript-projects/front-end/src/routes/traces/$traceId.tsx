import { createFileRoute, useNavigate } from '@tanstack/react-router'
import { Anchor, Center, Container, Loader, Text, Title, Group, Stack } from '@mantine/core'
import { useTrace } from '../../api/traces'
import StatusBadge from '../../components/badge/GenericBadge'

export const Route = createFileRoute('/traces/$traceId')({
  component: TraceDetailPage,
})

function TraceDetailPage() {
  const { traceId } = Route.useParams()
  const { isPending, error, data } = useTrace(traceId)
  const navigate = useNavigate();

  if (isPending) return <Center h="50vh"><Loader /></Center>
  if (error) return <Center h="50vh"><Text c="red">Failed to load trace: {error.message}</Text></Center>
  if (!data) return <Center h="50vh"><Text c="dimmed">Trace not found</Text></Center>

  const { trace } = data
  console.log(JSON.stringify(data, null, 2))

  return (
    <Container size="xl">
      <Anchor size="sm" c="dimmed" onClick={() => navigate({ to: '/traces' })} style={{ cursor: 'pointer' }} mb="xs">
        ← Back to traces
      </Anchor>
      <Title order={2} mb="md">{trace.name}</Title>
      <Stack gap="sm">
        <Group><Text fw={500}>Status:</Text> <StatusBadge status={trace.status} /></Group>
        <Group><Text fw={500}>Agent:</Text> <Text>{trace.agentId}</Text></Group>
        <Group><Text fw={500}>Session:</Text> <Text>{trace.sessionId ?? '—'}</Text></Group>
        <Group><Text fw={500}>Cost:</Text> <Text>{trace.totalCostUsd != null ? `$${trace.totalCostUsd.toFixed(4)}` : '—'}</Text></Group>
        <Group><Text fw={500}>Started:</Text> <Text>{new Date(trace.startedAt).toLocaleString()}</Text></Group>
        <Group><Text fw={500}>Ended:</Text> <Text>{trace.endedAt ? new Date(trace.endedAt).toLocaleString() : '—'}</Text></Group>
        <Group><Text fw={500}>Spans:</Text> <Text>{data.spans.length}</Text></Group>
        <Group><Text fw={500}>Data:</Text> <Text>{JSON.stringify(data, null, 2)}</Text></Group>
      </Stack>
    </Container>
  )
}
