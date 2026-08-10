import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/evals/')({
  component: EvalsPage,
})

function EvalsPage() {
  return (
    <div>
      <h1>Evals</h1>
      <p>Run scorers against your traces.</p>
    </div>
  )
}
