-- Initial schema for the routing-monitoring service.

-- A team specializes in a kind of subject (Cards, Loans, ...). New teams are just new rows.
CREATE TABLE team (
    id   UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE
);

-- A person who serves customers, belonging to exactly one team.
CREATE TABLE agent (
    id      UUID PRIMARY KEY,
    name    VARCHAR(120) NOT NULL,
    team_id UUID NOT NULL REFERENCES team (id)
);

-- A customer contact. Its subject decides the team; its state follows
-- WAITING -> IN_SERVICE -> ENDED. assigned_agent_id is null while it waits.
CREATE TABLE interaction (
    id                UUID PRIMARY KEY,
    customer_name     VARCHAR(160) NOT NULL,
    subject           VARCHAR(40) NOT NULL,
    state             VARCHAR(20) NOT NULL,
    assigned_agent_id UUID REFERENCES agent (id),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Counting how many interactions an agent has in service is how we know their load,
-- so we index exactly that lookup.
CREATE INDEX idx_interaction_agent_state ON interaction (assigned_agent_id, state);

-- The persistent waiting line: one row per waiting interaction, drained oldest-first
-- with SELECT ... FOR UPDATE SKIP LOCKED so concurrent workers never grab the same one.
CREATE TABLE interaction_queue (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id        UUID NOT NULL REFERENCES team (id),
    interaction_id UUID NOT NULL UNIQUE REFERENCES interaction (id),
    enqueued_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_queue_team_order ON interaction_queue (team_id, enqueued_at);

-- The three teams the business starts with. Others is the catch-all for any other subject.
INSERT INTO team (id, name) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Cards'),
    ('22222222-2222-2222-2222-222222222222', 'Loans'),
    ('33333333-3333-3333-3333-333333333333', 'Others');

-- A few agents to make the dashboard useful out of the box.
INSERT INTO agent (id, name, team_id) VALUES
    (gen_random_uuid(), 'Ana Ribeiro',    '11111111-1111-1111-1111-111111111111'),
    (gen_random_uuid(), 'Bruno Alves',    '11111111-1111-1111-1111-111111111111'),
    (gen_random_uuid(), 'Carla Nogueira', '22222222-2222-2222-2222-222222222222'),
    (gen_random_uuid(), 'Diego Martins',  '33333333-3333-3333-3333-333333333333');
