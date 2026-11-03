CREATE TABLE user_profile (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL UNIQUE REFERENCES app_user(id) ON DELETE CASCADE,
  headline VARCHAR(200),
  phone VARCHAR(50),
  location VARCHAR(160),
  bio TEXT,
  target_role VARCHAR(160),
  target_salary VARCHAR(120),
  experience_years INTEGER,
  skills TEXT,
  linkedin_url VARCHAR(500),
  github_url VARCHAR(500),
  portfolio_url VARCHAR(500),
  preferred_work_mode VARCHAR(30),
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_user_profile_user_id ON user_profile(user_id);

