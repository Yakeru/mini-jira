CREATE TABLE comments (
    id         UUID        PRIMARY KEY,
    content    TEXT        NOT NULL,
    task_id    UUID        NOT NULL REFERENCES tasks(id),
    author_id  UUID        NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comments_task_id   ON comments(task_id);
CREATE INDEX idx_comments_author_id ON comments(author_id);