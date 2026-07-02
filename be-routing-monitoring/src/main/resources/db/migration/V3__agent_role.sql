-- Replace the read-only VIEWER profile with a team-scoped AGENT profile.

-- Login accounts can now belong to a team. An AGENT is tied to one team and only sees that
-- team on the dashboard; ADMIN keeps team_id NULL and sees everything.
ALTER TABLE app_user ADD COLUMN team_id UUID REFERENCES team (id);

-- Drop the old read-only account.
DELETE FROM app_user WHERE username = 'viewer';

-- One AGENT per team (password: agent123). Names match agents already on each team.
INSERT INTO app_user (username, password_hash, role, team_id) VALUES
    ('ana',   '$2a$10$7WGggw1WwpHCkzVICC9R7eyQp6Fs0ckHUyJ3GRtnguyZZg3Sq9FP2', 'AGENT', '11111111-1111-1111-1111-111111111111'),
    ('carla', '$2a$10$7WGggw1WwpHCkzVICC9R7eyQp6Fs0ckHUyJ3GRtnguyZZg3Sq9FP2', 'AGENT', '22222222-2222-2222-2222-222222222222'),
    ('diego', '$2a$10$7WGggw1WwpHCkzVICC9R7eyQp6Fs0ckHUyJ3GRtnguyZZg3Sq9FP2', 'AGENT', '33333333-3333-3333-3333-333333333333');
