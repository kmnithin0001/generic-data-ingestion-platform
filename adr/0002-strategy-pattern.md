# ADR 0002: Strategy Pattern for Auth and Pagination

## Status
Accepted

## Context
REST APIs require different headers, parameters, and query parameters to authenticate requests and paginate responses. Incorporating API-specific auth/paging logic directly into the HTTP client or Connector would violate the Open/Closed Principle, requiring code modifications for every new vendor integration.

## Decision
We implement authentication and pagination using the Strategy Pattern:
- **Authentication**: `AuthenticationStrategy` defines the contract. Implemented by `NoneAuthenticationStrategy`, `ApiKeyAuthenticationStrategy`, `BearerTokenAuthenticationStrategy`, and `BasicAuthenticationStrategy`.
- **Pagination**: `PaginationStrategy` defines the contract. Implemented by `NonePaginationStrategy`, `PageNumberPaginationStrategy`, `LimitOffsetPaginationStrategy`, `NextUrlPaginationStrategy`, and `CursorPaginationStrategy`.

## Consequences
- New authentication methods and pagination strategies can be added by simply implementing their respective interfaces, without touching any connector or orchestrator code.
- Auth and paging strategies can be unit tested in isolation.
