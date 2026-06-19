ALTER TABLE users
    RENAME COLUMN github_id TO provider_id;

ALTER TABLE users
    ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'github';