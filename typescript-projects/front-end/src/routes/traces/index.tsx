import { createFileRoute } from '@tanstack/react-router'
import { GenericTable } from '../../components/tables/GenericTable'

export const Route = createFileRoute('/traces/')({
  component: TracesPage,
})

function TracesPage() {
  return (
    <div>
      <h1>Traces</h1>
      <GenericTable/>
    </div>
  )
}
