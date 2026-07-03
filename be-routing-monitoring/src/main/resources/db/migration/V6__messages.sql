-- Chat messages exchanged on an interaction, between the customer (on the public screen)
-- and the agent (on the dashboard). Kept so both sides see the full history.
CREATE TABLE message (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    interaction_id UUID NOT NULL REFERENCES interaction (id),
    sender         VARCHAR(20) NOT NULL,   -- CUSTOMER | AGENT
    body           VARCHAR(2000) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- The thread of one interaction is read oldest-first.
CREATE INDEX idx_message_interaction ON message (interaction_id, created_at);
