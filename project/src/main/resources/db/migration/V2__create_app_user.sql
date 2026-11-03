CREATE TABLE app_user (
    id uuid PRIMARY KEY,
    username text NOT NULL UNIQUE,
    password_hash text NOT NULL,
    role text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT app_user_role_known
        CHECK (role IN ('STUDENT', 'TEACHER'))
);
