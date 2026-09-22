# Java / Spring Boot guidelines

Reusable AI steering for this project. Prefer `spec/requirements.md` over invention.

## Stated constraints

- Language/runtime: **Java 21**.
- Framework: **Spring Boot**.
- Persistence: **PostgreSQL/H2** (assignment does not choose one exclusively).
- API style: **REST API**.
- Backend **must** validate input.
- Backend **must** enforce the ticket status state machine in `spec/requirements.md`.
- Invalid status transitions **must be rejected**.
- Data must persist in a database and **survive application restart**.
- **No secrets** in the repository.

## How to use this file

- Generate or change Java/Spring Boot code only after specifications and a plan exist.
- Do not start from “build the complete application.”
- Do not invent APIs, entities, validations, or behaviors that are not in `spec/requirements.md` or a later approved spec.
- If a detail is missing, document it as an ambiguity instead of filling it in.

## Explicitly not decided here

The assignment does not specify package layout, Spring Boot version (beyond using Spring Boot), JPA vs JDBC, security, pagination, DTO shape, or HTTP status codes. Do not treat this file as a source of those decisions.
