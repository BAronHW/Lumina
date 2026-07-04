CREATE TABLE IF NOT EXISTS client (
  id      UUID    PRIMARY KEY,
  name    VARCHAR UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS agent (
    id        UUID PRIMARY KEY,
    client_id UUID NOT NULL REFERENCES client(id),
    name      VARCHAR NOT NULL,
    UNIQUE(client_id, name)
);

CREATE TYPE span_status AS ENUM ('Ok', 'Error');

CREATE TABLE IF NOT EXISTS trace (
  id             UUID PRIMARY KEY,
  agent_id       UUID NOT NULL REFERENCES agent(id),
  name           VARCHAR NOT NULL,
  status         span_status NOT NULL,
  started_at     TIMESTAMPTZ NOT NULL,
  ended_at       TIMESTAMPTZ,
  total_cost_usd NUMERIC,
  tags           JSONB DEFAULT '{}'
);

 CREATE TABLE IF NOT EXISTS span (
  id             UUID PRIMARY KEY,
  trace_id       UUID NOT NULL REFERENCES trace(id),
  parent_span_id UUID REFERENCES span(id),
  name           VARCHAR NOT NULL,
  kind           VARCHAR NOT NULL,
  status         span_status NOT NULL,
  error          TEXT,
  started_at     TIMESTAMPTZ NOT NULL,
  ended_at       TIMESTAMPTZ,
  duration_ms    INTEGER,
  input          JSONB DEFAULT '{}',
  output         JSONB DEFAULT '{}',
  attributes     JSONB DEFAULT '{}'
);

CREATE TABLE IF NOT EXISTS prompts (
    id   UUID PRIMARY KEY,
    name VARCHAR UNIQUE NOT NULL,
    prompt VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS prompt_versions (
    id         UUID PRIMARY KEY,
    prompt_id  UUID NOT NULL REFERENCES prompts(id),
    version    INTEGER NOT NULL,
    content    TEXT NOT NULL,
    variables  JSONB DEFAULT '[]',
    notes      TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(prompt_id, version)
);

CREATE TABLE IF NOT EXISTS eval_runs (
    id          UUID PRIMARY KEY,
    name        VARCHAR NOT NULL,
    scorer_type VARCHAR NOT NULL,
    filter      JSONB DEFAULT '{}',
    started_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ended_at    TIMESTAMPTZ,
    total       INTEGER DEFAULT 0,
    passed      INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS eval_results (
    id          UUID PRIMARY KEY,
    eval_run_id UUID REFERENCES eval_runs(id),
    trace_id    UUID NOT NULL REFERENCES trace(id),
    span_id     UUID REFERENCES span(id),
    eval_name   VARCHAR NOT NULL,
    scorer_type VARCHAR NOT NULL,
    passed      BOOLEAN NOT NULL,
    score       NUMERIC,
    reason      TEXT,
    run_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS deployments (
    id          UUID PRIMARY KEY,
    client_id   UUID NOT NULL REFERENCES client(id),
    version     VARCHAR NOT NULL,
    deployed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);