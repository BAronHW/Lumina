import { createFileRoute } from "@tanstack/react-router"

export const Route = createFileRoute('/test/')({
  component: Test,
})

export default function Test() {
  return (
    <div>index</div>
  )
}
