-- TraceVault: skema awal database
-- File ini dijalankan otomatis oleh Flyway saat backend pertama kali start.
-- JANGAN mengubah file ini setelah sudah dijalankan; buat file baru V2__....sql

CREATE TABLE users (
    id            UUID         PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL DEFAULT '',
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL
);

CREATE TABLE analyses (
    id            UUID         PRIMARY KEY,
    owner_id      UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name     VARCHAR(255) NOT NULL,
    file_size     BIGINT       NOT NULL,
    analysis_type VARCHAR(20)  NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    sha256        CHAR(64)     NOT NULL,
    md5           CHAR(32)     NOT NULL,
    risk_score    INTEGER      NOT NULL,
    risk_level    VARCHAR(10)  NOT NULL,
    ai_summary    TEXT,
    notes         TEXT,
    duration_ms   BIGINT       NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    completed_at  TIMESTAMPTZ
);

CREATE INDEX idx_analyses_owner_created ON analyses (owner_id, created_at DESC);

CREATE TABLE analysis_findings (
    analysis_id UUID         NOT NULL REFERENCES analyses(id) ON DELETE CASCADE,
    position    INTEGER      NOT NULL,
    code        VARCHAR(20)  NOT NULL,
    severity    VARCHAR(10)  NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    category    VARCHAR(50)  NOT NULL,
    PRIMARY KEY (analysis_id, position)
);

CREATE TABLE analysis_metadata (
    analysis_id UUID         NOT NULL REFERENCES analyses(id) ON DELETE CASCADE,
    position    INTEGER      NOT NULL,
    meta_key    VARCHAR(100) NOT NULL,
    meta_value  TEXT         NOT NULL,
    PRIMARY KEY (analysis_id, position)
);

CREATE TABLE analysis_recommendations (
    analysis_id    UUID    NOT NULL REFERENCES analyses(id) ON DELETE CASCADE,
    position       INTEGER NOT NULL,
    recommendation TEXT    NOT NULL,
    PRIMARY KEY (analysis_id, position)
);

CREATE TABLE analysis_timeline (
    analysis_id UUID         NOT NULL REFERENCES analyses(id) ON DELETE CASCADE,
    position    INTEGER      NOT NULL,
    event_time  TIMESTAMPTZ  NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    PRIMARY KEY (analysis_id, position)
);
