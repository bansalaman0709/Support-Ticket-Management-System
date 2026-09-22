# Test strategy

Source of truth: `spec/requirements.md`, with status behavior detailed in `spec/state-machine.md`.

Defines testing obligations implied by the assignment. Does **not** invent frameworks, coverage percentages, or extra product scenarios beyond stated needs.

## Goals

1. Prove required ticket capabilities work end-to-end against acceptance criteria.
2. Prove the **status state machine** is enforced by the backend (integration tests required).
3. Prove invalid transitions are rejected.
4. Prove backend validation exists (without inventing unspecified validation rules).
5. Prove data persists across application restart.
6. Prove the UI shows meaningful errors for failures that users can hit in required flows.

## Required: state-machine integration tests

Assignment acceptance: **State-machine integration tests pass.**

These tests must exercise the backend (not UI-only stubs) for:

### Allowed transitions (must succeed)

| From           | To             |
|----------------|----------------|
| `OPEN`         | `IN_PROGRESS`  |
| `IN_PROGRESS`  | `RESOLVED`     |
| `RESOLVED`     | `CLOSED`       |
| `OPEN`         | `CANCELLED`    |
| `IN_PROGRESS`  | `CANCELLED`    |

Also verify a full happy path along the primary chain where needed:

`OPEN` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`

### Invalid transitions (must be rejected)

At minimum, the assignment’s stated examples:

| From         | To       | Expected |
|--------------|----------|----------|
| `CLOSED`     | `OPEN`   | rejected |
| `RESOLVED`   | `OPEN`   | rejected |
| `CANCELLED`  | `OPEN`   | rejected |

Also reject other non-allowed edges once the allowed set in `spec/state-machine.md` is treated as exhaustive (pending clarification on skips/self-transitions — do not invent extra product behavior; test only confirmed invalid cases plus clearly non-listed edges if product owner confirms “only listed arrows”).

## Acceptance-mapped test coverage

| Acceptance criterion | What to verify | Layer guidance |
|----------------------|----------------|----------------|
| Ticket can be created from UI | Create flow works | UI + API |
| Tickets can be listed | List returns persisted tickets | UI + API |
| Ticket details can be viewed | Details for a ticket | UI + API |
| Ticket fields can be updated | title, description, priority | UI + API |
| Assignee can be changed | assignee update | UI + API |
| Comments can be added | comment create + visible on details | UI + API |
| Search works | keyword search returns matches | UI + API |
| Status filter works | filter by each status value | UI + API |
| Valid status transitions work | allowed edges succeed | **Backend integration** (required) |
| Invalid status transitions rejected by backend | stated invalid examples rejected | **Backend integration** (required) |
| Data survives application restart | create/update then restart; data remains | Persistence / system |
| Backend validation works | invalid input rejected by backend | Backend |
| UI shows meaningful errors | failed actions show understandable errors | UI |
| State-machine integration tests pass | suite above green | Integration |
| No secrets committed | repo hygiene check | Process / review |

## Backend validation testing

Requirement: validate input at the backend.

- Tests must assert that **invalid input is rejected** by the backend.
- Map validation tests to the approved rules in `spec/data-model.md` and `spec/api-contract.md` (required title, lengths, blank keyword, invalid status filter values, etc.).
- Do **not** invent additional rules beyond those approved specs.

## Persistence testing

- Use a database (PostgreSQL and/or H2 per assignment constraint).
- Verify ticket data (and comments) survive application restart.
- Do not assume a particular DB product for the strategy itself.

## UI error testing

- For required flows that can fail (create, update, comments, and any status-transition attempt), assert the UI shows a **meaningful** error when the backend rejects the action.
- Exact wording is not specified; assert clarity/usefulness at a level appropriate to the chosen UI stack, without inventing a copy catalog.

## Explicitly not decided (not requirements)

Do not treat as mandatory unless later approved:

- Unit vs component vs E2E tool choice (except that state-machine tests are **integration** tests).
- Coverage % targets.
- Load/performance/security test suites.
- Tests for features not in `requirements.md` (auth, attachments, reopen, delete, notifications, etc.).

## Dependencies / gaps

Approved specs now exist:

- `spec/api-contract.md` — use for concrete HTTP assertions (paths, codes, bodies).
- `spec/data-model.md` — use for entity/field validation test cases.
- `spec/architecture.md` — PostgreSQL app runtime; H2 for tests; Maven + Spring Boot + JPA.

Prioritize:

1. State-machine integration tests (allowed edges + stated invalid examples + other non-allowed edges per exhaustive allowed table).
2. Capability tests for create/list/details/update/comments/search/filter per `api-contract.md`.
3. Persistence restart check.
4. UI meaningful-error checks on required flows (frontend step).

## Consistency

- Status values in tests must match `spec/state-machine.md` exactly (`OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`).
- Do not invent statuses or transitions in fixtures.
- If a test needs an unspecified detail, stop and list the ambiguity instead of guessing (`commands/generate-tests.md`).
