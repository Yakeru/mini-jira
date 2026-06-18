CREATE TABLE users (
    id          UUID         PRIMARY KEY,
    github_id   VARCHAR(50)  NOT NULL UNIQUE,
    username    VARCHAR(100) NOT NULL,
    name        VARCHAR(255),
    email       VARCHAR(255),
    avatar_url  VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);