CREATE TABLE teams
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL,
    owner_id   UUID         NOT NULL REFERENCES users (id),
    plan       VARCHAR(20)  NOT NULL DEFAULT 'FREE',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
