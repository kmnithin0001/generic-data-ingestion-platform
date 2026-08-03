-- V1__initial_schema.sql
-- Database Ingestion Platform Initial Schema

CREATE TABLE source_configurations (
    id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(1024) NOT NULL,
    method VARCHAR(10) NOT NULL,
    auth_type VARCHAR(32) NOT NULL,
    auth_config JSON,
    pagination_type VARCHAR(32) NOT NULL,
    pagination_config JSON,
    request_options JSON,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_source_configurations PRIMARY KEY (id),
    CONSTRAINT uq_source_configurations_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE ingestion_jobs (
    id BINARY(16) NOT NULL,
    source_id BINARY(16) NOT NULL,
    status VARCHAR(32) NOT NULL,
    start_time DATETIME(6) NOT NULL,
    end_time DATETIME(6),
    total_records_fetched INT,
    total_pages_fetched INT,
    error_message VARCHAR(2048),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_ingestion_jobs PRIMARY KEY (id),
    CONSTRAINT fk_job_source FOREIGN KEY (source_id) REFERENCES source_configurations (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE raw_api_responses (
    id BINARY(16) NOT NULL,
    job_id BINARY(16) NOT NULL,
    page_number INT NOT NULL,
    request_url VARCHAR(2048) NOT NULL,
    request_headers JSON,
    response_body LONGTEXT NOT NULL,
    response_metadata JSON,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_raw_api_responses PRIMARY KEY (id),
    CONSTRAINT fk_response_job FOREIGN KEY (job_id) REFERENCES ingestion_jobs (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Performance Indexes
CREATE INDEX idx_job_status ON ingestion_jobs (status);
CREATE INDEX idx_job_source_id ON ingestion_jobs (source_id);
CREATE INDEX idx_response_job_id ON raw_api_responses (job_id);
