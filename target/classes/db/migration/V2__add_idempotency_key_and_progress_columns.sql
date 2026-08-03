-- V2__add_idempotency_key_and_progress_columns.sql
-- Add idempotency and progress fields to ingestion_jobs table

ALTER TABLE ingestion_jobs ADD COLUMN idempotency_key VARCHAR(255) NULL;
ALTER TABLE ingestion_jobs ADD COLUMN total_records INT NULL;
ALTER TABLE ingestion_jobs ADD COLUMN percentage_completed DOUBLE NOT NULL DEFAULT 0.0;
ALTER TABLE ingestion_jobs ADD COLUMN estimated_completion DATETIME(6) NULL;

-- Unique constraint for idempotency key
CREATE UNIQUE INDEX idx_job_idempotency_key ON ingestion_jobs (idempotency_key);
