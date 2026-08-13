import { AppShell, Burger, Group, Stack } from "@mantine/core";
import { useDisclosure } from '@mantine/hooks';
import { Link, Outlet } from '@tanstack/react-router';
import styles from './HomePage.module.css';


export default function HomePage() {
    const [opened, { toggle }] = useDisclosure();
  return (
    <AppShell
        padding={"md"}
        header={{ height: 60 }}
        navbar={{
            width: 300,
            breakpoint: 'sm',
            collapsed: { mobile: !opened }
        }}
    >
      <AppShell.Header>
        <Group h="100%" px="md">
          <Burger
            opened={opened}
            onClick={toggle}
            hiddenFrom="sm"
            size="sm"
          />
          <Link to="/" className={styles.logo}>Lumina</Link>
        </Group>
      </AppShell.Header>

      <AppShell.Navbar p="md">
        <AppShell.Section grow>
          <Stack gap="xs">
            <Link to="/traces">Traces</Link>
            <Link to="/sessions">Sessions</Link>
            <Link to="/costs">Costs</Link>
            <Link to="/evals">Evals</Link>
            <Link to="/prompts">Prompts</Link>
          </Stack>
        </AppShell.Section>
      </AppShell.Navbar>

      <AppShell.Main>
        <Outlet />
      </AppShell.Main>
    </AppShell>
  )
}
