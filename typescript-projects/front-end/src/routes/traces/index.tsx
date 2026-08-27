import { createFileRoute } from '@tanstack/react-router'
import { GenericTable } from '../../components/tables/GenericTable'
import { useTraces } from '../../api/traces'

export const Route = createFileRoute('/traces/')({
  component: TracesPage,
})

function TracesPage() {
  const { isPending, error, data } = useTraces();
  console.log(data, 'here is the data');
  console.log(isPending, 'here is the pending');
  console.log(error, 'here is the error');
  return (
    <div>
      <h1>Traces</h1>
    </div>
  )
}
