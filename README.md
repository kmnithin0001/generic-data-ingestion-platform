# Generic Data Ingestion Platform

An enterprise-grade, high-performance, and resilient Generic Data Ingestion Platform built with **Spring Boot 3.3**, **Java 21**, and **MySQL 8.0**. Designed using **Clean Architecture** and **SOLID principles**, the platform dynamically ingests data from external HTTP APIs and stores raw responses.

---

## 🚀 Key Features

- **Dynamic HTTP Connectors:** Support for multiple API sources configured at runtime.
- **Pluggable Authentication Strategies:** Supports API Key, Basic Auth, and OAuth2 Bearer Token out-of-the-box.
- **Dynamic Pagination Handlers:** Implements Next-URL, Cursor, Limit/Offset, and Page-Number navigation styles.
- **Strict Job State Machine:** Manages state transitions (PENDING → RUNNING → COMPLETED/FAILED/CANCELLED) with validation and domain exceptions.
- **Domain Event Sourcing:** Emits Domain Events during job lifecycles (`JobCreatedEvent`, `JobStartedEvent`, `JobCompletedEvent`, etc.).
- **API Idempotency:** Support for optional `Idempotency-Key` headers to prevent duplicate execution requests.
- **Resilience & Rate Limiting:** Circuit Breaker and Retry patterns integrated via Resilience4j.
- **Separation of Profiles:** Dev profile exposes Swagger UI and verbose Actuators, while Prod profile restricts debug interfaces and runs rolling JSON log files.
- **Production Observability:** Exposed Kubernetes-compatible health groups (Readiness/Liveness probes) and Prometheus scrape metrics.

---

## 📁 Architecture Overview

The codebase is structured following **Clean Architecture** patterns:
- **Domain Layer (`domain`):** Contains core business entities (IngestionJob), validation rules, exceptions, and the `JobStateMachine`. Completely independent of database/REST frameworks.
- **Application Layer (`application`):** Contains business use cases (Start, Cancel, Retry, Get Status) and event orchestration workflows.
- **Infrastructure Layer (`infrastructure`):** Core adapters. Implements Database persistence (Spring Data JPA, MySQL, Flyway migrations), external API client calls (WebClient), authentication strategy registry, and domain event publisher handlers.
- **Presentation Layer (`presentation`):** Exposes REST APIs, DTO request mapping, global exception handlers, and security filters.

---

## 🛠️ Getting Started

### Prerequisites
- **Java Development Kit (JDK) 21** (Eclipse Temurin JRE recommended)
- **Apache Maven 3.9+**
- **Docker Desktop** (configured with WSL2 backends)

### 1. Build and Verify
A PowerShell script is provided to compile, test, and perform security checks:
```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1 verify
```
This script executes:
- **Unit and Integration Tests** (running against containerized database using Testcontainers).
- **Checkstyle Rules** verifying codebase style conventions.
- **SpotBugs** scanning for static bug bugs.
- **OWASP Dependency Check** flagging vulnerabilities.
- **JaCoCo Coverage** verifying instruction coverage exceeds the **80%** threshold.

### 2. Run Locally (Development)
By default, the platform boots up pointing to your local MySQL instance on port `3306`:
1. Update database credentials inside `src/main/resources/application-dev.yml`.
2. Start the application:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
3. Access Swagger UI for API testing:
   - `http://localhost:8080/swagger-ui.html`

---

## 🐳 Containerized Deployment

A multi-stage production `Dockerfile` and a resource-constrained `docker-compose.yml` stack are included.

### 1. Build Docker Image
```bash
docker build -t generic-data-ingestion-platform:latest .
```

### 2. Spin Up Orchestrated Stack
To run the application along with MySQL, Prometheus, and Grafana:
```bash
docker compose up -d
```
*Note: MySQL is mapped to port `3307` on the host in docker-compose to prevent conflicts with a native local MySQL instance.*

### Exposed Services
- **Spring Boot App:** `http://localhost:8080`
- **Prometheus Scraper:** `http://localhost:9090`
- **Grafana Dashboard:** `http://localhost:3000`
- **MySQL DB:** `localhost:3307`

---

## 📡 Core API Specifications

### Create Ingestion Job
`POST /api/v1/ingestion/jobs`

**Headers:**
- `Idempotency-Key`: `uuid-string-here` (Optional)

**Request Body Example:**
```json
{
  "sourceId": "weather-api",
  "endpointUrl": "https://api.weather.com/v1/forecast",
  "httpMethod": "GET",
  "authType": "API_KEY",
  "authConfig": {
    "headerName": "X-API-Key",
    "apiKeyValue": "secret-token-key"
  },
  "paginationType": "PAGE_NUMBER",
  "paginationConfig": {
    "pageParam": "page",
    "sizeParam": "size",
    "pageSize": 50
  },
  "requestOptions": {
    "queryParams": {
      "city": "Seattle"
    }
  }
}
```

### Fetch Job Status
`GET /api/v1/jobs/{jobId}`

### Cancel Running Job
`POST /api/v1/jobs/{jobId}/cancel`

### Retry Failed Job
`POST /api/v1/jobs/{jobId}/retry`

---

## 📈 Monitoring & Health Probes

Exposed Kubernetes-compatible actuator health groups:
- **Liveness probe:** `GET http://localhost:8080/actuator/health/liveness`
- **Readiness probe:** `GET http://localhost:8080/actuator/health/readiness`
- **Prometheus Metrics:** `GET http://localhost:8080/actuator/prometheus` (Exposes JVM state, thread pools, and Resilience4j circuit breakers metrics).

---

## 📚 Documentation Catalog

Comprehensive architectural decisions and runbook catalogs are located under the following folders:
- **ADR Index:** [adr/](file:///c:/Users/kmnit/OneDrive/Desktop/Take-Home%20Assignment/adr) cataloging architectural definitions.
- **Operations & DR Guide:** Detailed backup/restore guides in the runbooks.
- **Performance Tuning Report:** JVM parameters and thread pool optimization details.
- **Deployment Guide:** Step-by-step production cluster deployment instructions.
