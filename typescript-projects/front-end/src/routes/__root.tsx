import { createRootRoute, Link, Outlet } from '@tanstack/react-router'
import { MantineProvider } from '@mantine/core'

export const Route = createRootRoute({
  notFoundComponent: () => (
    <div style={{ padding: '2rem' }}>
      <h1>404 - Page not found</h1>
      <p>The page you're looking for doesn't exist.</p>
    </div>
  ),
  component: () => (
    <MantineProvider>
      <nav style={{ display: 'flex', gap: '1rem', padding: '1rem', borderBottom: '1px solid #eee' }}>
        <Link to="/traces">Traces</Link>
        <Link to="/sessions">Sessions</Link>
        <Link to="/costs">Costs</Link>
        <Link to="/evals">Evals</Link>
        <Link to="/prompts">Prompts</Link>
      </nav>
      <main style={{ padding: '2rem' }}>
        <Outlet />
      </main>
    </MantineProvider>
  ),
})
