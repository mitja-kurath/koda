CREATE TABLE audit_logs
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id    UUID         REFERENCES users (id) ON DELETE SET NULL,
    event      VARCHAR(64)  NOT NULL,
    ip_address VARCHAR(45),
    details    TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_event   ON audit_logs (event);
CREATE INDEX idx_audit_logs_created ON audit_logs (created_at DESC);
