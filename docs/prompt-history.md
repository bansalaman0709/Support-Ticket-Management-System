# Prompt history

Important AI prompts and decisions. SpecStory session files also live under `.specstory/history/`.

## 2026-09-22 — SDD structure only (no implementation)

**Prompt (summary):** Read the assignment (`Assessments .docx`) and inspect the repository. Create `rules/` (java-springboot, testing, api-standards, documentation), `commands/` (review-code, review-spec, generate-tests), `docs/prompt-history.md`, and `spec/requirements.md`. Capture only explicit requirements. Do not invent features. Do not write application code. Document ambiguities. Keep requirements separate from implementation.

**Decisions:**

- Followed the **user-requested file list** for this pass (documentation as `rules/documentation.md`, not `skills/documentation/`).
- Did **not** create assignment-example spec files (`architecture.md`, `data-model.md`, `api-contract.md`, `state-machine.md`, `ui-flow.md`, `test-strategy.md`) because those details are not stated and this pass is requirements-only.
- Recorded PostgreSQL/H2, React/Next.js **or equivalent**, and Cursor + GitHub Copilot as stated constraints, without choosing among options.
- State machine captured as written: allowed chain `OPEN → IN_PROGRESS → RESOLVED → CLOSED`, plus `OPEN → CANCELLED` and `IN_PROGRESS → CANCELLED`; invalid examples as given.
- Empty existing folders `rules/`, `commands/`, `docs/`, `spec/`, `plan/` were left structurally in place; `plan/` was not populated (not requested).

**AI caution (process):** Do not treat empty repo README (“AI-assisted, spec-driven support ticket portal”) as extra product requirements.

## 2026-09-22 — Complete specifications (Step 2, no implementation)

**Prompt (summary):** Read the repository and existing SDD docs (`requirements.md`, and architecture/data-model/api-contract if present). Create `spec/state-machine.md`, `spec/ui-flow.md`, `spec/test-strategy.md`. Use requirements as source of truth. Do not invent features. Check for contradictions, gaps, naming/status mismatches, API/data-model issues, unsupported assumptions, scope creep. Report findings. Do not implement.

**Repository observation:** `spec/architecture.md`, `spec/data-model.md`, and `spec/api-contract.md` do **not** exist. Only `spec/requirements.md` was present among the named read list.

**Decisions:**

- Created only the three requested specs; did **not** invent architecture, data model, or API contract content.
- State machine limited to stated statuses and allowed edges; stated invalid examples recorded as mandatory rejects.
- UI flows limited to create, list, details, update (title/description/priority/assignee), comments, keyword search, status filter, meaningful errors.
- Test strategy centered on required state-machine **integration** tests plus acceptance-mapped coverage; no invented frameworks or coverage %.
- Flagged conflict/gap: status transitions required by acceptance/state machine, but status is not listed among updateable fields / explicit UI FR.

**AI assumptions rejected:** inventing reopen transitions; inventing REST paths/schemas; inventing priority enums; inventing auth UI; inventing initial status; inventing skip-transition rules without clarification; filling missing architecture/data-model/api-contract by invention.

## 2026-09-22 — Add and approve architecture / data-model / api-contract

**Prompt (summary):** User requested to add and approve the missing unblock specs (`architecture.md`, `data-model.md`, `api-contract.md`) after backend implementation was correctly stopped for missing contracts.

**Approved decisions recorded in those specs:**

- Stack: Java 21, Spring Boot 3.4.x, Maven, JPA, PostgreSQL (app), H2 (tests); package `com.supportticket`.
- Entities: Ticket + Comment only; fields limited to requirement-named attributes plus technical `id`; comment body field named `text`.
- Initial status: `OPEN`; status changes via dedicated `POST /api/tickets/{id}/status`; field PATCH cannot set status.
- Only listed state-machine edges allowed (skips/self rejected); updates/comments allowed in any status.
- Search: case-insensitive substring on title OR description; with status filter uses AND.
- Validation/length limits and error JSON + HTTP codes as in `api-contract.md` / `data-model.md`.
- No auth, users, tags, SLA, audit, notifications, delete APIs, or pagination.

**Also updated for consistency:** `state-machine.md`, `ui-flow.md` (U9), `test-strategy.md`, `plan/implementation-plan.md` gate note.

**Not done in this pass:** application/backend code.

## 2026-09-22 — Backend implementation (approved specs)

**Prompt (summary):** User approved running backend implementation after architecture/data-model/api-contract were added.

**Implemented:** Maven Spring Boot 3.4.4 backend under `backend/` per approved specs — entities, repositories, services, state-machine policy, REST API, validation, error handling, H2 integration tests.

**Defect fixed during testing:** Comment create initially returned null `id` when only cascading via Ticket; switched to `CommentRepository.save` so generated id is returned.

**Verification:** `mvn clean test` — 31 tests, 0 failures (JDK 21). Temurin JDK 21 installed locally to satisfy `java.version=21` (machine previously had only 8/17).

## 2026-09-22 — Frontend implementation (Step 5)

**Prompt (summary):** Implement only UI defined by specs and API contract; no invented endpoints/features; run frontend tests and build.

**Decisions:** Vite + React + TypeScript (assignment allows React/Next.js or equivalent). API calls match `api-contract.md` exactly via `/api` proxy to backend. Status transition buttons offer only allowed next statuses from the state machine (backend remains enforcer).

**Verification:** `npm test` — 11 passed; `npm run build` — success.
