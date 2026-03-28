-- This file is executed only when PostgreSQL initializes a fresh data directory.
-- It creates the application schema, roles, tables, grants, and seed data.

CREATE ROLE app_rw WITH
  LOGIN
  PASSWORD 'app_rw_dev_password';

CREATE ROLE app_ro WITH
  LOGIN
  PASSWORD 'app_ro_dev_password';

CREATE SCHEMA app;

DO $$
BEGIN
  EXECUTE format('GRANT CONNECT ON DATABASE %I TO app_rw', current_database());
  EXECUTE format('GRANT CONNECT ON DATABASE %I TO app_ro', current_database());
END
$$;

GRANT USAGE ON SCHEMA app TO app_rw;
GRANT USAGE ON SCHEMA app TO app_ro;

CREATE TABLE app.users (
  id BIGSERIAL PRIMARY KEY,
  email TEXT NOT NULL UNIQUE,
  display_name TEXT NOT NULL,
  user_type TEXT NOT NULL CHECK (user_type IN ('admin', 'analyst', 'viewer')),
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE app.incident_log (
  id BIGSERIAL PRIMARY KEY,
  incident_key TEXT NOT NULL UNIQUE,
  title TEXT NOT NULL,
  description TEXT,
  status TEXT NOT NULL CHECK (status IN ('open', 'investigating', 'resolved', 'closed')),
  priority TEXT NOT NULL CHECK (priority IN ('low', 'medium', 'high', 'critical')),
  severity TEXT NOT NULL CHECK (severity IN ('low', 'medium', 'high', 'critical')),
  assigned_user_id BIGINT REFERENCES app.users(id),
  created_by_user_id BIGINT NOT NULL REFERENCES app.users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  resolved_at TIMESTAMPTZ
);

CREATE INDEX idx_incident_log_status ON app.incident_log (status);
CREATE INDEX idx_incident_log_assigned_user_id ON app.incident_log (assigned_user_id);
CREATE INDEX idx_incident_log_created_at ON app.incident_log (created_at DESC);

CREATE TABLE app.incidents_history (
  id BIGSERIAL PRIMARY KEY,
  incident_id BIGINT NOT NULL REFERENCES app.incident_log(id) ON DELETE CASCADE,
  action_type TEXT NOT NULL,
  old_values JSONB,
  new_values JSONB,
  note TEXT,
  actor_user_id BIGINT REFERENCES app.users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_incidents_history_incident_id_created_at
  ON app.incidents_history (incident_id, created_at DESC);
CREATE INDEX idx_incidents_history_action_type
  ON app.incidents_history (action_type);

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA app TO app_rw;
GRANT SELECT ON ALL TABLES IN SCHEMA app TO app_ro;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA app TO app_rw;

ALTER DEFAULT PRIVILEGES IN SCHEMA app
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_rw;

ALTER DEFAULT PRIVILEGES IN SCHEMA app
  GRANT SELECT ON TABLES TO app_ro;

ALTER DEFAULT PRIVILEGES IN SCHEMA app
  GRANT USAGE, SELECT ON SEQUENCES TO app_rw;

INSERT INTO app.users (id, email, display_name, user_type, is_active)
VALUES
  (1, 'alice.admin@example.com', 'Alice Admin', 'admin', TRUE),
  (2, 'bob.analyst@example.com', 'Bob Analyst', 'analyst', TRUE),
  (3, 'carol.viewer@example.com', 'Carol Viewer', 'viewer', TRUE);

SELECT setval(
  pg_get_serial_sequence('app.users', 'id'),
  (SELECT MAX(id) FROM app.users)
);

INSERT INTO app.incident_log (
  id,
  incident_key,
  title,
  description,
  status,
  priority,
  severity,
  assigned_user_id,
  created_by_user_id,
  created_at,
  updated_at,
  resolved_at
)
VALUES
  (
    1,
    'INC-1001',
    'Payment API latency spike',
    'Increased response times detected on the payment API.',
    'investigating',
    'high',
    'high',
    2,
    1,
    NOW() - INTERVAL '4 hours',
    NOW() - INTERVAL '2 hours',
    NULL
  ),
  (
    2,
    'INC-1002',
    'Reporting job failure',
    'Nightly reporting job failed because of a missing source file.',
    'resolved',
    'medium',
    'medium',
    2,
    1,
    NOW() - INTERVAL '1 day',
    NOW() - INTERVAL '6 hours',
    NOW() - INTERVAL '6 hours'
  );

SELECT setval(
  pg_get_serial_sequence('app.incident_log', 'id'),
  (SELECT MAX(id) FROM app.incident_log)
);

INSERT INTO app.incidents_history (
  incident_id,
  action_type,
  old_values,
  new_values,
  note,
  actor_user_id,
  created_at
)
VALUES
  (
    1,
    'created',
    NULL,
    jsonb_build_object(
      'status', 'open',
      'priority', 'high',
      'severity', 'high',
      'assigned_user_id', NULL
    ),
    'Incident created from monitoring alert.',
    1,
    NOW() - INTERVAL '4 hours'
  ),
  (
    1,
    'assigned',
    jsonb_build_object('assigned_user_id', NULL),
    jsonb_build_object('assigned_user_id', 2),
    'Assigned to Bob for investigation.',
    1,
    NOW() - INTERVAL '3 hours 30 minutes'
  ),
  (
    1,
    'status_changed',
    jsonb_build_object('status', 'open'),
    jsonb_build_object('status', 'investigating'),
    'Investigation started.',
    2,
    NOW() - INTERVAL '2 hours'
  ),
  (
    2,
    'created',
    NULL,
    jsonb_build_object(
      'status', 'open',
      'priority', 'medium',
      'severity', 'medium',
      'assigned_user_id', 2
    ),
    'Incident created after scheduled job failure.',
    1,
    NOW() - INTERVAL '1 day'
  ),
  (
    2,
    'status_changed',
    jsonb_build_object('status', 'investigating'),
    jsonb_build_object('status', 'resolved'),
    'Source file restored and job rerun successfully.',
    2,
    NOW() - INTERVAL '6 hours'
  );
