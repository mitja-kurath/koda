CREATE TABLE projects
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name         VARCHAR(255) NOT NULL,
    slug         VARCHAR(255) NOT NULL UNIQUE,
    description  TEXT,
    visibility   VARCHAR(20)  NOT NULL DEFAULT 'PRIVATE',
    team_id      UUID         NOT NULL REFERENCES teams (id) ON DELETE CASCADE,
    spec_content TEXT,
    spec_title   VARCHAR(255),
    spec_version VARCHAR(50),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_projects_team_id ON projects (team_id);
CREATE INDEX idx_projects_slug ON projects (slug);
