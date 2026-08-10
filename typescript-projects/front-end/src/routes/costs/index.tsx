import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/costs/')({
  component: CostsPage,
})

function CostsPage() {
  return (
    <div>
      <h1>Costs</h1>
      <p>Monitor your LLM spend.</p>
    </div>
  )
}
