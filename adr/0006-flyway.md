# ADR 0006: Flyway Schema Migration Strategy

## Status
Accepted

## Context
Letting ORMs like Hibernate modify database tables directly (`ddl-auto: update` or `create`) in production is extremely dangerous. It can lead to unintended table drops, locked schemas, data corruption, and untracked database mutations across staging and production.

## Decision
We enforce database schema version control using Flyway:
- Schema updates must be written as structured SQL migration scripts under `src/main/resources/db/migration/`.
- `V1__init_schema.sql` establishes the base ingestion tables.
- `V2__add_idempotency_key_and_progress_columns.sql` updates the schema with idempotency and progress fields.
- Production and local profiles enforce validation (`spring.jpa.hibernate.ddl-auto: validate`), forcing the application to fail startup if the database schema does not match the compiled JPA entity metadata.

## Consequences
- Database changes are fully versioned, tracked in source control, and executed automatically on startup.
- Safe, predictable, and repeatable database schema migrations.
