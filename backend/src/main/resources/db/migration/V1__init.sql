-- =========================================================
-- Sequences
-- =========================================================
CREATE SEQUENCE member_seq    START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE todo_seq      START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE study_log_seq START WITH 1 INCREMENT BY 50;

-- =========================================================
-- Tables
-- =========================================================

-- Member
CREATE TABLE member (
    member_id   BIGINT       PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    provider    VARCHAR(255),
    provider_id VARCHAR(255),
    password    VARCHAR(255),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    birth_date  DATE         NOT NULL,
    category    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    deleted_at  TIMESTAMP,
    CONSTRAINT uk_member_email    UNIQUE (email),
    CONSTRAINT uk_member_provider UNIQUE (provider, provider_id)
);

-- Todo
CREATE TABLE todo (
    id            BIGINT       PRIMARY KEY,
    member_id     BIGINT       NOT NULL,
    title         VARCHAR(255) NOT NULL,
    display_order INTEGER      NOT NULL,
    todo_date     DATE         NOT NULL,
    start_at      TIME         NOT NULL,
    end_at        TIME         NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,
    deleted_at    TIMESTAMP,
    CONSTRAINT fk_todo_member FOREIGN KEY (member_id) REFERENCES member (member_id)
);

-- Study Log
CREATE TABLE study_log (
    study_log_id  BIGINT    PRIMARY KEY,
    todo_id       BIGINT    NOT NULL,
    study_content TEXT,
    started_at    TIME,
    ended_at      TIME,
    paused_at     TIME,
    focus_sec     INTEGER   NOT NULL DEFAULT 0,
    away_sec      INTEGER   NOT NULL DEFAULT 0,
    created_at    TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP NOT NULL,
    CONSTRAINT uk_study_log_todo UNIQUE (todo_id),
    CONSTRAINT fk_study_log_todo FOREIGN KEY (todo_id) REFERENCES todo (id)
);
