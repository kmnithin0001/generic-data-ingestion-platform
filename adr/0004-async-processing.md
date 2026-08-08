# ADR 0004: Asynchronous Processing Strategy

## Status
Accepted

## Context
Ingesting millions of records or paging across large datasets takes minutes or hours. Executing this within the synchronous HTTP request thread blocks client resources, risks gateway timeouts, and degrades API responsiveness.

## Decision
We decouple ingestion request handling from execution:
- The controller accepts ingestion parameters and immediately schedules a background task, returning an HTTP `202 Accepted` status along with a unique `JobId`.
- Ingestion runs in a background thread managed by Spring's `@Async` annotation.
- A custom `ThreadPoolTaskExecutor` (defined in `AsyncConfig`) is configured with configurable pool sizes (`corePoolSize: 10`, `maxPoolSize: 20`, `queueCapacity: 1000`) and a `CallerRunsPolicy` to handle overflow gracefully without silently losing tasks.
- Progress monitoring and result retrieval are handled through separate, quick `GET /api/v1/jobs/{jobId}` polling endpoints.

## Consequences
- The API remains responsive under high ingestion demand.
- Prevents thread starvation on the Tomcat container thread pool.
- Allows clients to check progress and cancel active tasks in a non-blocking manner.
