import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/sessions/')({
  component: SessionsPage,
})

function SessionsPage() {
  return (
    <div>
      <h1>Sessions</h1>
      <p>View grouped agent sessions.</p>
    </div>
  )
}
