import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/traces/')({
  component: TracesPage,
})

function TracesPage() {
  return (
    <div>
      <h1>Traces</h1>
      <p>View and inspect LLM agent traces.</p>
    </div>
  )
}
