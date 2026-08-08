# ADR 0001: Clean Architecture Implementation

## Status
Accepted

## Context
The platform must support ingestion from multiple diverse sources, support extensions, and enforce strict testability. Hard coupling the business rules to Spring Boot, database components, or HTTP client engines would make scaling or switching technologies extremely risky.

## Decision
We enforce a strict Clean Architecture model divided into concentric layer zones:
1. **Domain Layer**: Contains fundamental models (`IngestionJob`, `JobStatus`), exceptions (`DomainException`), domain events (`JobCreatedEvent`), and interface specifications (`StorageService`, `JobStateMachine`, `ProgressCalculator`). No dependencies on external frameworks.
2. **Application Layer**: Contains business Use Cases (`StartIngestionUseCase`, `CancelJobUseCase`, etc.) coordinating orchestrations, event publishers, and lifecycle stages.
3. **Infrastructure / Adapters Layer**: Contains concrete technologies such as Flyway database scripts, JPA entities, WebClient clients, RestControllers, and configuration classes.

## Consequences
- Business logic is 100% testable using simple unit tests.
- Changing persistence technologies or web framework components requires modifying only the outermost infrastructure layer.
- Execution flow must strictly traverse: Controller -> Use Case -> Domain Services -> Storage Adapter -> DB.
