import { AppShell, Stack } from "@mantine/core";
import { Link } from "@tanstack/react-router";

interface NavLinks {
    href: string;
    title: string;
}

interface NavbarProps {
    links: NavLinks[];
}

export default function Navbar({ links }: NavbarProps) {
  return (
    <AppShell.Navbar p="md">
        <AppShell.Section grow>
            <Stack gap="xs">
                {links.map((link) => (
                    <Link key={link.href} to={link.href}>
                        {link.title}
                    </Link>
                ))}
            </Stack>
        </AppShell.Section>
    </AppShell.Navbar>
  )
}
