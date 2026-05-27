CREATE TABLE pages
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    project_id UUID         NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    title      VARCHAR(255) NOT NULL,
    slug       VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL DEFAULT '',
    sort_order INT          NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (project_id, slug)
);

CREATE INDEX idx_pages_project_id ON pages (project_id);
