import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/projects/')({
  component: ProjectsPage,
})

function ProjectsPage() {
  return (
    <div>
      <h1>Projects</h1>
      <p>Organize your agents and traces into projects.</p>
    </div>
  )
}
