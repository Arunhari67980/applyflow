CREATE TABLE app_user (
  id UUID PRIMARY KEY, name VARCHAR(120) NOT NULL, email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL, created_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE job_application (
  id UUID PRIMARY KEY, user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  company_name VARCHAR(160) NOT NULL, job_title VARCHAR(200) NOT NULL,
  description TEXT, source VARCHAR(30) NOT NULL, job_type VARCHAR(30) NOT NULL,
  work_mode VARCHAR(30) NOT NULL, location VARCHAR(160), posting_url VARCHAR(500),
  salary_range VARCHAR(120), applied_date DATE NOT NULL, status VARCHAR(30) NOT NULL,
  priority VARCHAR(20) NOT NULL, next_follow_up_date DATE, notes TEXT,
  created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_application_user_status ON job_application(user_id, status);
CREATE INDEX ix_application_user_applied ON job_application(user_id, applied_date);
CREATE TABLE status_history (
  id UUID PRIMARY KEY, application_id UUID NOT NULL REFERENCES job_application(id) ON DELETE CASCADE,
  previous_status VARCHAR(30), new_status VARCHAR(30) NOT NULL, note TEXT,
  changed_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE follow_up (
  id UUID PRIMARY KEY, user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  application_id UUID REFERENCES job_application(id) ON DELETE CASCADE,
  due_date DATE NOT NULL, type VARCHAR(30) NOT NULL, notes TEXT, completed BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE interview (
  id UUID PRIMARY KEY, user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  application_id UUID REFERENCES job_application(id) ON DELETE CASCADE,
  scheduled_at TIMESTAMPTZ NOT NULL, round_name VARCHAR(120) NOT NULL, type VARCHAR(30) NOT NULL,
  meeting_link VARCHAR(500), notes TEXT, cancelled BOOLEAN NOT NULL DEFAULT FALSE, created_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE notification (
  id UUID PRIMARY KEY, user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  follow_up_id UUID REFERENCES follow_up(id) ON DELETE CASCADE, message VARCHAR(300) NOT NULL,
  read_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL,
  UNIQUE(follow_up_id)
);
