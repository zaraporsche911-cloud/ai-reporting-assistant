CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_reporting_users_email UNIQUE (email),
    CONSTRAINT ck_reporting_users_role CHECK (role IN ('ADMIN', 'FLEET_MANAGER', 'OPERATIONS_MANAGER', 'ANALYST', 'VIEWER'))
);

CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    title VARCHAR(160) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    author VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    generated_report_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_message_author CHECK (author IN ('USER', 'ASSISTANT'))
);

CREATE TABLE generated_reports (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    conversation_id BIGINT REFERENCES conversations(id) ON DELETE SET NULL,
    question VARCHAR(2000) NOT NULL,
    report_type VARCHAR(48) NOT NULL,
    intent_json TEXT NOT NULL,
    result_json TEXT,
    summary TEXT,
    status VARCHAR(16) NOT NULL,
    execution_time_ms BIGINT NOT NULL,
    ai_provider VARCHAR(32),
    ai_model VARCHAR(120),
    error_detail VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_generated_report_status CHECK (status IN ('SUCCEEDED', 'FAILED'))
);

ALTER TABLE messages
    ADD CONSTRAINT fk_messages_generated_report
    FOREIGN KEY (generated_report_id) REFERENCES generated_reports(id) ON DELETE SET NULL;

CREATE TABLE saved_reports (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    generated_report_id BIGINT NOT NULL REFERENCES generated_reports(id) ON DELETE CASCADE,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(1000),
    tags VARCHAR(500),
    favorite BOOLEAN NOT NULL DEFAULT FALSE,
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    shared_internally BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE prompt_templates (
    id BIGSERIAL PRIMARY KEY,
    template_key VARCHAR(64) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    content TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_prompt_template_key UNIQUE (template_key)
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_email VARCHAR(254),
    action VARCHAR(64) NOT NULL,
    resource_type VARCHAR(64) NOT NULL,
    resource_id VARCHAR(64),
    detail VARCHAR(1000),
    correlation_id VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    dark_mode BOOLEAN NOT NULL DEFAULT FALSE,
    timezone VARCHAR(64) NOT NULL DEFAULT 'UTC',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_user_preferences_user UNIQUE (user_id)
);

CREATE TABLE app_settings (
    setting_key VARCHAR(64) PRIMARY KEY,
    setting_value VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_conversations_user_updated ON conversations(user_id, updated_at DESC);
CREATE INDEX idx_messages_conversation_created ON messages(conversation_id, created_at);
CREATE INDEX idx_generated_reports_user_created ON generated_reports(user_id, created_at DESC);
CREATE INDEX idx_generated_reports_type ON generated_reports(report_type);
CREATE INDEX idx_generated_reports_status ON generated_reports(status);
CREATE INDEX idx_saved_reports_user_updated ON saved_reports(user_id, updated_at DESC);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);

INSERT INTO prompt_templates(template_key, display_name, content)
VALUES
('intent-extraction', 'Intent extraction', 'Convert the user question into the strict FleetOps report intent JSON envelope. Never produce SQL, executable code, invented identifiers, secrets, or unsupported fields. Return JSON only.'),
('report-summary', 'Report summary', 'Summarize only the supplied structured report data. Mention the date range and filters. Do not invent causes, values, or recommendations unsupported by the data. Keep the answer concise and business-friendly.');

INSERT INTO app_settings(setting_key, setting_value)
VALUES ('active-ai-provider', 'DEFAULT');
