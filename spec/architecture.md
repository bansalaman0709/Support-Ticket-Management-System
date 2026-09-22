# Architecture

**Status:** Approved for implementation (project decision document).

**Sources:** `spec/requirements.md` (technology constraints); runtime choices confirmed for this project (PostgreSQL application DB, H2 for tests).

This file records **approved technical decisions** required to implement the backend. It does not add product features beyond `requirements.md`.

## Stack (approved)

| Concern | Choice | Basis |
|---------|--------|--------|
| Language | Java **21** | Assignment |
| Framework | **Spring Boot** 3.4.x (Spring Boot 3 / Jakarta) | Assignment + approved version band |
| API | REST over HTTP, JSON (`application/json`) | Assignment |
| Persistence API | **Spring Data JPA** + Hibernate | Approved (assignment left JPA vs JDBC open) |
| Application database | **PostgreSQL** | Approved for this project |
| Test database | **H2** (in-memory or file as needed for tests) | Approved for this project |
| Build tool | **Maven** | Approved (no existing build in repo) |
| Auth | **None** | Out of scope |

## Backend module layout (approved)

Single deployable Spring Boot application:

```
backend/
  pom.xml
  src/main/java/com/supportticket/
    SupportTicketApplication.java
    domain/          # entities, status enum, transition policy
    repository/
    service/
    web/             # controllers, DTOs, exception handling
    config/          # non-secret configuration only
  src/main/resources/
    application.yml
    application-prod.yml   # optional; no secrets committed
  src/test/java/...
  src/test/resources/
    application-test.yml   # H2
```

Package root: `com.supportticket`.

## Layering (approved)

1. **Controller** — HTTP mapping per `spec/api-contract.md`; no state-machine rules inline beyond delegating to services.
2. **Service** — business rules, validation orchestration, status transitions.
3. **Domain** — entities; **status transition policy** (allowed edges only).
4. **Repository** — Spring Data JPA repositories.

Constructor injection; no field injection.

## Configuration rules (approved)

- Datasource URL, username, and password come from **environment variables** or externalized config — **never commit secrets**.
- Default local profile may document placeholder property *names* only.
- `ddl-auto`: `update` or `validate` for local/dev as needed; tests may use `create-drop` with H2.
- No authentication, CORS product rules beyond whatever is needed for a later frontend (CORS may be enabled permissively for local UI later; not a product feature).

## Out of scope (architecture)

Do not introduce: security/OAuth, messaging, caching layers, API gateways, microservices split, Flyway/Liquibase unless later approved, WebSockets, file storage.

## Consistency

- Status values and transitions: `spec/state-machine.md`
- Entities/fields: `spec/data-model.md`
- HTTP surface: `spec/api-contract.md`
