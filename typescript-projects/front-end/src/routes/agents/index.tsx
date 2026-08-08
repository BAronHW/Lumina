import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/agents/')({
  component: AgentsPage,
})

function AgentsPage() {
  return (
    <div>
      <h1>Agents</h1>
      <p>Monitor your registered LLM agents.</p>
    </div>
  )
}
