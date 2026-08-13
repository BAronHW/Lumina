import { createRootRoute } from '@tanstack/react-router'
import { MantineProvider } from '@mantine/core'
import HomePage from '../HomePage'

export const Route = createRootRoute({
  notFoundComponent: () => (
    <div style={{ padding: '2rem' }}>
      <h1>404 - Page not found</h1>
      <p>The page you're looking for doesn't exist.</p>
    </div>
  ),
  component: () => (
    <MantineProvider>
      <HomePage />
    </MantineProvider>
  ),
})
