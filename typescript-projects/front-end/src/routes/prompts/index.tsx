import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/prompts/')({
  component: PromptsPage,
})

function PromptsPage() {
  return (
    <div>
      <h1>Prompts</h1>
      <p>Manage and version your system prompts.</p>
    </div>
  )
}
