CREATE TABLE assignment (
  id            uuid PRIMARY KEY,
  business_key  text NOT NULL UNIQUE,
  status        text NOT NULL,
  title         text NOT NULL,
  created_at    timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT assignment_status_known
    CHECK (status IN ('ASSIGNED', 'SUBMITTED', 'CHECKED', 'APPROVED'))
);
