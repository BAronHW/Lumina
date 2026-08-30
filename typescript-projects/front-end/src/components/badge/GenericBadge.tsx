import { Badge } from '@mantine/core';

interface StatusBadgeProps {
    label: string;
    colour: string;
    size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
}

export default function GenericBadge(props: StatusBadgeProps) {
  return (
    <Badge color={props.colour} size={props.size}>
        {props.label}
    </Badge>
  )
}
