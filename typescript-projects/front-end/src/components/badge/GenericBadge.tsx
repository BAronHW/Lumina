import { Badge } from '@mantine/core';

const colorMap: Record<string, string> = {
  ok: 'green',
  error: 'red',
  pending: 'yellow',
  timeout: 'orange',
}

interface StatusBadgeProps {
    status: string;
    label?: string;
    size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
}

export default function StatusBadge({ status, label, size = 'sm' }: StatusBadgeProps) {
  return (
    <Badge color={colorMap[status] ?? 'gray'} size={size} w="fit-content">
        {label ?? status}
    </Badge>
  )
}
