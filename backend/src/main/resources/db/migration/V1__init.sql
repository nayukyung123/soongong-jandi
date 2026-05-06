-- =========================================================
-- 1. Tables Creation
-- =========================================================

-- Users
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    password        VARCHAR(255),
    gender          VARCHAR(1) CONSTRAINT chk_users_gender CHECK (gender IN ('M', 'F', 'N')),
    provider        VARCHAR(20),
    provider_id     VARCHAR(255),
    category        VARCHAR(50),
    camera_interval INT NOT NULL DEFAULT 10,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP NULL,
    UNIQUE (email),
    UNIQUE (provider, provider_id)
);

-- Todos
CREATE TABLE todos (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    title           VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'TODO' CONSTRAINT chk_todos_status CHECK (status IN ('TODO', 'DONE')),
    order_num       INT NOT NULL,
    date            DATE NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP NULL
);

-- Sessions
CREATE TABLE sessions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    todo_id         BIGINT NOT NULL,
    started_at      TIMESTAMP NOT NULL,
    ended_at        TIMESTAMP NULL,
    duration_sec    INT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CONSTRAINT chk_sessions_status CHECK (status IN ('ACTIVE', 'PAUSED', 'DONE')),
    is_public       BOOLEAN NOT NULL DEFAULT TRUE
);

-- Focus Logs
CREATE TABLE focus_logs (
    id              BIGSERIAL PRIMARY KEY,
    session_id      BIGINT NOT NULL,
    detected_at     TIMESTAMP NOT NULL,
    event_type      VARCHAR(20) NOT NULL CONSTRAINT chk_focus_logs_event_type CHECK (event_type IN ('ABSENT', 'PHONE', 'NORMAL')),
    duration_sec    INT NULL
);

-- Daily Summary
CREATE TABLE daily_summary (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    total_sec       INT NOT NULL DEFAULT 0,
    session_count   INT NOT NULL DEFAULT 0,
    avg_focus_rate  DECIMAL(5,2),
    date            DATE NOT NULL,
    UNIQUE (user_id, date)
);

-- Commits
CREATE TABLE commits (
    id              BIGSERIAL PRIMARY KEY,
    todo_id         BIGINT NOT NULL UNIQUE,
    user_id         BIGINT NOT NULL,
    content_md      TEXT NOT NULL,
    focus_rate      DECIMAL(5,2),
    is_public       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP NULL
);

-- =========================================================
-- 2. Foreign Key Constraints
-- =========================================================

ALTER TABLE todos ADD CONSTRAINT fk_todos_user_id FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE sessions ADD CONSTRAINT fk_sessions_user_id FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE sessions ADD CONSTRAINT fk_sessions_todo_id FOREIGN KEY (todo_id) REFERENCES todos(id);

ALTER TABLE focus_logs ADD CONSTRAINT fk_focus_logs_session_id FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE;

ALTER TABLE daily_summary ADD CONSTRAINT fk_daily_summary_user_id FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE commits ADD CONSTRAINT fk_commits_todo_id FOREIGN KEY (todo_id) REFERENCES todos(id);
ALTER TABLE commits ADD CONSTRAINT fk_commits_user_id FOREIGN KEY (user_id) REFERENCES users(id);

-- =========================================================
-- 3. Indexes
-- =========================================================

CREATE INDEX ix_todos_user_date ON todos (user_id, date);
CREATE INDEX ix_todos_user_date_order ON todos (user_id, date, order_num);

CREATE INDEX ix_sessions_user_started ON sessions (user_id, started_at DESC);
CREATE INDEX ix_sessions_todo_id ON sessions (todo_id);

CREATE INDEX ix_focus_logs_session_detected ON focus_logs (session_id, detected_at);

CREATE INDEX ix_commits_user_created ON commits (user_id, created_at DESC);
CREATE INDEX ix_commits_todo_id ON commits (todo_id);
