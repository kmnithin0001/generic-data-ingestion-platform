# ADR 0007: Testcontainers for Integration Testing

## Status
Accepted

## Context
In-memory databases like H2 have different dialects, constraints, functions, and transaction behaviors compared to production databases like MySQL. Tests that pass on H2 can fail in production due to dialect differences, batch limits, or locking bugs.

## Decision
We enforce Testcontainers for all repository and storage integration tests:
- Tests use a real MySQL 8 container spawned dynamically on startup via the `testcontainers-mysql` library.
- Databases are migrated automatically using Flyway before tests run.
- Dynamic property resolution maps the container connection coordinates back to Spring Boot.

## Consequences
- 100% confidence that SQL queries, transactions, and index behavior in tests match production environments.
- Eliminates the need to maintain mock schemas or configure manual testing databases on developer environments.
