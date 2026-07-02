-- Tie each AGENT login to the actual agent it represents, and give the Cards team its
-- second agent (Bruno) a login too.

-- The missing relation: an AGENT account now points at its agent row. ADMIN keeps it NULL.
ALTER TABLE app_user ADD COLUMN agent_id UUID REFERENCES agent (id);

-- Agent ids are random (V1 uses gen_random_uuid), so link by the agent's name.
UPDATE app_user SET agent_id = (SELECT id FROM agent WHERE name = 'Ana Ribeiro')
    WHERE username = 'ana';
UPDATE app_user SET agent_id = (SELECT id FROM agent WHERE name = 'Carla Nogueira')
    WHERE username = 'carla';
UPDATE app_user SET agent_id = (SELECT id FROM agent WHERE name = 'Diego Martins')
    WHERE username = 'diego';

-- Cards has two agents (Ana and Bruno); add Bruno's login too (password: agent123),
-- linked to his agent row and the Cards team.
INSERT INTO app_user (username, password_hash, role, team_id, agent_id)
SELECT 'bruno', '$2a$10$7WGggw1WwpHCkzVICC9R7eyQp6Fs0ckHUyJ3GRtnguyZZg3Sq9FP2', 'AGENT',
       '11111111-1111-1111-1111-111111111111', a.id
FROM agent a
WHERE a.name = 'Bruno Alves';
