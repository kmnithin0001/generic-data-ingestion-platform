# ADR 0003: Registry Pattern for Strategy and Connector Resolution

## Status
Accepted

## Context
At runtime, the Ingestion Engine must select the correct strategies and connector to run based on the ingestion request's configuration. Hardcoding `switch` statements or conditional chains creates tight coupling and restricts extensibility.

## Decision
We implement registry classes to manage and resolve instances dynamically:
- `AuthenticationStrategyRegistry` collects all Spring-managed `AuthenticationStrategy` instances and matches the correct strategy via `AuthenticationType`.
- `PaginationRegistry` maps `PaginationType` to the corresponding `PaginationStrategy`.
- `ConnectorRegistry` resolves connector instances based on protocol/type coordinates.

## Consequences
- Spring's dependency injection automatically registers new strategies or connectors as they are added to the application context.
- Lookups occur in $O(1)$ constant time.
- Extensibility is fully dynamic and requires zero code updates to the orchestrator layer.
