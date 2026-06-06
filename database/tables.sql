CREATE TABLE IF NOT EXISTS client (
  id      UUID    PRIMARY KEY,
  name    VARCHAR UNIQUE NOT NULL
);

CREATE TYPE span_status AS ENUM ('Ok', 'Error');

CREATE TABLE IF NOT EXISTS trace (
    id UUID PRIMARY KEY,
    name VARCHAR NOT NULL,
    agentId UUID NOT NULL,
    status span_status NOT NULL,
    startedAt TIMESTAMPTZ NOT NULL,
    endedAt TIMESTAMPTZ NOT NULL,
    totalCostUsd NUMERIC
    /* tag and spans are still needed here*/
);

CREATE TABLE IF NOT EXISTS tag (
    id UUID PRIMARY KEY
)