-- ============================================================
-- HR Module — AzTU
-- Auth via central SSO microservice (NO local login here).
-- Integrates with Finance and Turnstile via an outbox pattern.
-- Mirrors the authoritative DBML 1:1 — do not drop/alter fields.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE departments (
  department_id SERIAL PRIMARY KEY,
  code          VARCHAR UNIQUE NOT NULL,
  name          VARCHAR NOT NULL,
  is_active     BOOLEAN DEFAULT TRUE,
  created_at    TIMESTAMP DEFAULT now()
);

CREATE TABLE vacancies (
  vacancy_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  department_id INT NOT NULL REFERENCES departments(department_id),
  job_title     VARCHAR NOT NULL,
  job_type      VARCHAR NOT NULL,
  salary        NUMERIC,
  category      VARCHAR NOT NULL,
  description   TEXT,
  status        VARCHAR DEFAULT 'open',
  created_by    UUID NOT NULL,
  opened_at     TIMESTAMP DEFAULT now(),
  closes_at     TIMESTAMP,
  created_at    TIMESTAMP DEFAULT now()
);

CREATE TABLE applicants (
  applicant_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id      UUID UNIQUE,
  name         VARCHAR NOT NULL,
  surname      VARCHAR NOT NULL,
  father_name  VARCHAR,
  email        VARCHAR NOT NULL,
  phone        VARCHAR NOT NULL,
  created_at   TIMESTAMP DEFAULT now()
);
CREATE UNIQUE INDEX ux_applicants_email ON applicants(email);

CREATE TABLE applicant_documents (
  document_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  applicant_id  UUID NOT NULL REFERENCES applicants(applicant_id),
  doc_type      VARCHAR DEFAULT 'cv',
  storage_path  VARCHAR NOT NULL,
  original_name VARCHAR,
  mime_type     VARCHAR NOT NULL,
  size_bytes    BIGINT,
  uploaded_at   TIMESTAMP DEFAULT now()
);

CREATE TABLE applications (
  application_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  applicant_id   UUID NOT NULL REFERENCES applicants(applicant_id),
  vacancy_id     UUID NOT NULL REFERENCES vacancies(vacancy_id),
  cv_document_id UUID REFERENCES applicant_documents(document_id),
  source         VARCHAR DEFAULT 'karyera',
  status         VARCHAR DEFAULT 'submitted',
  submitted_at   TIMESTAMP DEFAULT now()
);
CREATE UNIQUE INDEX ux_applications_applicant_vacancy ON applications(applicant_id, vacancy_id);

CREATE TABLE application_reviews (
  review_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  application_id UUID NOT NULL REFERENCES applications(application_id),
  reviewed_by    UUID NOT NULL,
  decision       VARCHAR NOT NULL,
  reason         TEXT,
  reviewed_at    TIMESTAMP DEFAULT now()
);

CREATE TABLE hr_templates (
  template_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  type        VARCHAR NOT NULL,
  name        VARCHAR NOT NULL,
  subject     VARCHAR NOT NULL,
  body        TEXT NOT NULL,
  created_by  UUID NOT NULL,
  is_active   BOOLEAN DEFAULT TRUE,
  created_at  TIMESTAMP DEFAULT now()
);

CREATE TABLE hr_email_log (
  email_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  applicant_id   UUID REFERENCES applicants(applicant_id),
  application_id UUID REFERENCES applications(application_id),
  template_id    UUID REFERENCES hr_templates(template_id),
  to_email       VARCHAR NOT NULL,
  subject        VARCHAR,
  status         VARCHAR DEFAULT 'pending',
  sent_at        TIMESTAMP,
  created_at     TIMESTAMP DEFAULT now()
);

CREATE TABLE employees (
  employee_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  application_id UUID UNIQUE REFERENCES applications(application_id),
  user_id        UUID UNIQUE,
  applicant_id   UUID NOT NULL REFERENCES applicants(applicant_id),
  department_id  INT NOT NULL REFERENCES departments(department_id),
  job_title      VARCHAR NOT NULL,
  job_type       VARCHAR NOT NULL,
  salary         NUMERIC,
  status         VARCHAR DEFAULT 'onboarding',
  approved_at    TIMESTAMP,
  official_at    TIMESTAMP,
  created_at     TIMESTAMP DEFAULT now()
);

CREATE TABLE employee_documents (
  document_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  employee_id   UUID NOT NULL REFERENCES employees(employee_id),
  doc_type      VARCHAR NOT NULL,
  storage_path  VARCHAR NOT NULL,
  original_name VARCHAR,
  mime_type     VARCHAR NOT NULL,
  uploaded_by   UUID,
  uploaded_at   TIMESTAMP DEFAULT now()
);

CREATE TABLE employee_schedules (
  schedule_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  employee_id    UUID NOT NULL REFERENCES employees(employee_id),
  day_of_week    VARCHAR,
  start_time     TIME,
  end_time       TIME,
  hours          NUMERIC,
  effective_from DATE,
  effective_to   DATE,
  created_at     TIMESTAMP DEFAULT now()
);

CREATE TABLE employee_terminations (
  termination_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  employee_id    UUID UNIQUE NOT NULL REFERENCES employees(employee_id),
  reason         TEXT NOT NULL,
  terminated_by  UUID NOT NULL,
  effective_date DATE NOT NULL,
  created_at     TIMESTAMP DEFAULT now()
);

CREATE TABLE termination_documents (
  document_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  termination_id UUID NOT NULL REFERENCES employee_terminations(termination_id),
  doc_type       VARCHAR,
  storage_path   VARCHAR NOT NULL,
  original_name  VARCHAR,
  uploaded_at    TIMESTAMP DEFAULT now()
);

CREATE TABLE integration_events (
  event_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  target_system VARCHAR NOT NULL,
  event_type    VARCHAR NOT NULL,
  employee_id   UUID REFERENCES employees(employee_id),
  payload       JSONB NOT NULL,
  status        VARCHAR DEFAULT 'pending',
  attempts      INT DEFAULT 0,
  last_error    TEXT,
  created_at    TIMESTAMP DEFAULT now(),
  sent_at       TIMESTAMP
);
