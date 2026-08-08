# ADR 0005: Idempotency Key Strategy

## Status
Accepted

## Context
Network issues or user retry attempts can lead to clients submitting the same ingestion request multiple times. Executing the same ingestion task concurrently can cause duplicate data stores, resource contention, and network waste.

## Decision
We enforce an idempotency key mechanism:
- Clients submit an optional `Idempotency-Key` header in `POST /api/v1/ingest`.
- Before creating a new job, the application queries `StorageService` for any job matching that key.
- If a match is found and is still active (`PENDING` or `RUNNING`), we bypass scheduling and return the existing active job details with an HTTP `200 OK` status.
- If the matched job has terminated (`COMPLETED`, `FAILED`, or `CANCELLED`), we return the result metadata with an HTTP `200 OK` status, preventing duplicates.
- If no key matches, a new job is created, scheduled, and a `202 Accepted` status is returned.

## Consequences
- Guaranteed data safety and protection against duplicate API execution.
- Safe client-side retries with predictable execution outcomes.
