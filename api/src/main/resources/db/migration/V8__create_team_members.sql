CREATE TABLE team_members
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    team_id    UUID         NOT NULL REFERENCES teams (id) ON DELETE CASCADE,
    user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role       VARCHAR(20)  NOT NULL DEFAULT 'MEMBER',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (team_id, user_id)
);

CREATE INDEX idx_team_members_team_id ON team_members (team_id);
CREATE INDEX idx_team_members_user_id ON team_members (user_id);

INSERT INTO team_members (team_id, user_id, role)
SELECT id, owner_id, 'OWNER'
FROM teams;
