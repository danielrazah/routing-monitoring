-- Application users for login. Passwords are stored as BCrypt hashes.
CREATE TABLE app_user (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(60) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(30) NOT NULL,
    enabled       BOOLEAN NOT NULL DEFAULT TRUE
);

-- Seed the demo accounts (same credentials as before):
--   admin  / admin123   -> ADMIN  (can change state)
--   viewer / viewer123  -> VIEWER (read-only)
INSERT INTO app_user (username, password_hash, role) VALUES
    ('admin',  '$2a$10$1qLSXW5rRkq7xQxWrm63vO/N9Jb1qfcRm293sNp68cpuo9M6XIopW', 'ADMIN'),
    ('viewer', '$2a$10$1Av1vt0MAAtvKEDEfdeCTeaIrrUN59HCw9IlcIwzj.5kSO654Xb2e', 'VIEWER');
