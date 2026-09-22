# API standards

Reusable AI steering for this project. Prefer `spec/requirements.md` over invention.

## Stated constraints

- Expose a **REST API**.
- Support the stated ticket capabilities: create, list, view details, update title/description/priority/assignee, add comments, search by keyword, filter by status.
- **Validate input at the backend**.
- **Enforce the status state machine on the backend**; reject invalid transitions.
- UI must display **meaningful errors**; API error payloads are not specified beyond that product need.

## How to use this file

- Do not invent endpoint paths, HTTP methods, status codes, request/response schemas, authentication, or pagination.
- Do not add resources or fields that are not named in `spec/requirements.md`.
- When generating API work later, keep a dedicated `spec/api-contract.md` (assignment example artefact) and keep it consistent with requirements.

## Explicitly not decided here

The assignment does not specify URL design, versioning, content type, error JSON shape, or idempotency. Do not treat this file as a source of those decisions.
