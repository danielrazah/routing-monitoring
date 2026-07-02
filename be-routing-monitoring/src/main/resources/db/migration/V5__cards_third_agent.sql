-- A third agent on the Cards team, so the dashboard shows a team with more than two agents
-- (which enables the per-agent "who are they serving" hover on each agent row).
INSERT INTO agent (id, name, team_id) VALUES
    (gen_random_uuid(), 'Elisa Souza', '11111111-1111-1111-1111-111111111111');
