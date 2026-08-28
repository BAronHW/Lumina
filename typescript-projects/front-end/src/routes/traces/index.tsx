import { createFileRoute } from '@tanstack/react-router'
import { useTraces } from '../../api/traces'

export const Route = createFileRoute('/traces/')({
  component: TracesPage,
})

function TracesPage() {
  const { isPending, error, data } = useTraces(1, 25);
  console.log(data, 'here is the data');
  console.log(isPending, 'here is the pending');
  console.log(error, 'here is the error');
  return (
    <div>
      <h1>Traces</h1>
      <p>{ data?.map(elem => ` ${ elem.status }`) }</p>
    </div>
  )
}
