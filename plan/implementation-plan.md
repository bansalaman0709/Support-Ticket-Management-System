# Support Ticket Management System — Implementation Plan

**Goal:** Divide specified work into small, independently reviewable tasks. **No application code in this document.**

**Sources read (all files under `spec/`):**

- `spec/requirements.md`
- `spec/state-machine.md`
- `spec/ui-flow.md`
- `spec/test-strategy.md`

**Rules for this plan:** Follow specifications exactly; do not invent requirements; keep tasks small and single-purpose; call out cross-layer dependencies; mark work that cannot proceed until an ambiguity is resolved.

**Approved specs (gates cleared for backend implementation):**

- `spec/architecture.md` — present (approved)
- `spec/data-model.md` — present (approved)
- `spec/api-contract.md` — present (approved)

Earlier “missing spec” blockers below are superseded by those approved documents. Prefer the approved specs over historical “blocked” notes in individual tasks when they conflict.

---

## Phase 1 — Backend foundation

### Task 1.1 — Resolve backend stack choices before scaffolding

| Field | Content |
|-------|---------|
| **Task name** | Resolve backend stack choices before scaffolding |
| **Objective** | Obtain approved decisions for Spring Boot setup options that the assignment leaves open, so scaffolding does not invent architecture. |
| **Relevant specification** | `requirements.md` (Java 21, Spring Boot, PostgreSQL/H2, REST API); `rules/java-springboot.md` (explicitly undecided: package layout, Spring Boot version, JPA vs JDBC, security, pagination). |
| **Files/components expected to change** | Decision record only (e.g. future `spec/architecture.md` if approved); no application code in this task. |
| **Dependencies** | None. |
| **Acceptance criteria** | Documented choices for: Spring Boot version band; persistence approach (JPA vs JDBC); database target for local/dev (PostgreSQL and/or H2); package root; whether auth is out of scope (assignment does not require auth). |
| **Tests required** | None (decision task). |
| **Risks / ambiguities** | **Blocked until clarified:** exclusive DB product; persistence API; package layout; Spring Boot version. Do not invent these as requirements. |

### Task 1.2 — Create Spring Boot project skeleton (Java 21)

| Field | Content |
|-------|---------|
| **Task name** | Create Spring Boot project skeleton (Java 21) |
| **Objective** | Establish a runnable Java 21 Spring Boot application shell with no ticket domain logic yet. |
| **Relevant specification** | `requirements.md` — Explicit technology constraints (Java 21, Spring Boot). |
| **Files/components expected to change** | Backend build file(s), application main class, default application config placeholders (no secrets). |
| **Dependencies** | Task 1.1 (stack choices). |
| **Acceptance criteria** | Application starts under Java 21; no credentials committed; no invented business endpoints yet. |
| **Tests required** | Smoke: context loads (optional until architecture approved; not an assignment-mandated test type). |
| **Risks / ambiguities** | Depends on 1.1. Do not add security, messaging, or extra frameworks not stated. |

### Task 1.3 — Wire database connectivity without domain schema

| Field | Content |
|-------|---------|
| **Task name** | Wire database connectivity without domain schema |
| **Objective** | Connect the app to the chosen database so later persistence work can survive restarts. |
| **Relevant specification** | `requirements.md` — Persist data in a database; PostgreSQL/H2; data survives restart; no secrets committed. `test-strategy.md` — Persistence testing uses a database. |
| **Files/components expected to change** | Datasource configuration; env/config templates that do **not** contain secrets; README run notes only if already in scope for docs (prefer not inventing docs beyond need). |
| **Dependencies** | Tasks 1.1, 1.2. |
| **Acceptance criteria** | App connects to configured DB; configuration uses externalized non-secret properties; restart-capable store is available for later tickets/comments. |
| **Tests required** | None beyond proving connectivity if useful; persistence-across-restart is Phase 9 / Task 6.x. |
| **Risks / ambiguities** | PostgreSQL vs H2 (or both) still open until 1.1. |

### Task 1.4 — Establish backend validation and error-handling hooks (no rules yet)

| Field | Content |
|-------|---------|
| **Task name** | Establish backend validation and error-handling hooks (no rules yet) |
| **Objective** | Prepare a place for backend input validation and API error responses that the UI can surface meaningfully—without inventing validation rules or error JSON. |
| **Relevant specification** | `requirements.md` — Validate input at the backend; display meaningful errors in the UI. `api-standards.md` — error payloads not specified beyond product need. |
| **Files/components expected to change** | Shared exception/validation wiring placeholders; no concrete field rules until data model / API contract exist. |
| **Dependencies** | Task 1.2. Prefer Task 5.0 / `api-contract.md` before locking error shape. |
| **Acceptance criteria** | Framework-level validation/error path exists; **no** invented length/regex/required-field matrix treated as assignment requirements. |
| **Tests required** | Deferred to Phase 6 until concrete validation rules are approved (`test-strategy.md`). |
| **Risks / ambiguities** | **Partially blocked:** concrete validation cases and HTTP error body shape need `data-model.md` / `api-contract.md`. |

---

## Phase 2 — Database / entities

### Task 2.0 — Produce or approve `spec/data-model.md` (gate)

| Field | Content |
|-------|---------|
| **Task name** | Produce or approve data-model specification (gate) |
| **Objective** | Define only fields named by requirements (and types/constraints approved by product owner) before coding entities. |
| **Relevant specification** | `requirements.md` — Fields named: title, description, priority, assignee; comments; status; keyword search. `state-machine.md` — five status values. `test-strategy.md` — data-model missing blocks validation tests. |
| **Files/components expected to change** | `spec/data-model.md` (new, when approved)—**specification only in this task**. |
| **Dependencies** | Clarifications listed in plan summary (initial status, priority domain, comment shape, identifiers, timestamps if any). |
| **Acceptance criteria** | Data model lists only approved attributes; status enum matches `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`; ambiguities remaining are explicitly listed, not silently filled. |
| **Tests required** | None (spec gate). |
| **Risks / ambiguities** | **Cannot implement entities until this gate completes.** Assignment does not specify IDs, timestamps, priority values, comment author/body fields, or create-time defaults. |

### Task 2.1 — Persist ticket core fields

| Field | Content |
|-------|---------|
| **Task name** | Persist ticket core fields |
| **Objective** | Store tickets with updateable fields named in requirements: title, description, priority, assignee, plus status for filter/state machine. |
| **Relevant specification** | `requirements.md` — Persist data; fields title, description, priority, assignee; status for filter and state machine. `state-machine.md` — status value names. |
| **Files/components expected to change** | Ticket entity/table mapping; repository access for tickets. |
| **Dependencies** | Tasks 1.3, 2.0. |
| **Acceptance criteria** | Ticket rows can be inserted/read with named fields and a status value from the five allowed names; data stored in DB. |
| **Tests required** | Persistence checks in Phase 6/9; no invented uniqueness rules. |
| **Risks / ambiguities** | Blocked on 2.0 for types/nullability/defaults. **Initial status on create not specified** (`state-machine.md`). |

### Task 2.2 — Persist comments linked to tickets

| Field | Content |
|-------|---------|
| **Task name** | Persist comments linked to tickets |
| **Objective** | Store comments associated with a ticket so they can be added and later shown in details. |
| **Relevant specification** | `requirements.md` — Add comments; persist data. `ui-flow.md` U5 — comment persisted and visible in details. `test-strategy.md` — ticket data and comments survive restart. |
| **Files/components expected to change** | Comment entity/table; association to ticket; repository access. |
| **Dependencies** | Tasks 2.0, 2.1. |
| **Acceptance criteria** | A comment can be stored against a ticket and retrieved with that ticket’s details data path. |
| **Tests required** | Covered under comments + persistence tests (Phases 6, 8, 9). |
| **Risks / ambiguities** | Comment body/author/created-at **not specified**—must come from approved data model, not invention. |

### Task 2.3 — Ensure schema supports list, search, and status filter queries

| Field | Content |
|-------|---------|
| **Task name** | Ensure schema supports list, search, and status filter queries |
| **Objective** | Confirm persistence model can support list-all, keyword search, and filter-by-status without inventing indexes or pagination as requirements. |
| **Relevant specification** | `requirements.md` — List; search by keyword; filter by status. `state-machine.md` — status names for filter. `ui-flow.md` U2/U6/U7. |
| **Files/components expected to change** | Query methods / repository finders only as needed for those three capabilities. |
| **Dependencies** | Tasks 2.1, 2.0 (which fields keyword search covers). |
| **Acceptance criteria** | Data layer can return all tickets; filter by each of the five statuses; run a keyword query once search-field scope is approved. |
| **Tests required** | API/service tests in Phase 6 after contract; UI in Phase 8. |
| **Risks / ambiguities** | **Blocked for search implementation detail:** which fields keyword matches, case rules, match style (`ui-flow.md`, `test-strategy.md`). Filter+search combination unspecified. |

---

## Phase 3 — Services / business rules

### Task 3.1 — Create ticket service

| Field | Content |
|-------|---------|
| **Task name** | Create ticket service |
| **Objective** | Implement backend capability to create a ticket and persist it. |
| **Relevant specification** | `requirements.md` FR1; acceptance “Ticket can be created from UI” (service supports that). `ui-flow.md` U1. |
| **Files/components expected to change** | Ticket application/service layer; create use-case. |
| **Dependencies** | Tasks 2.1, 1.4; **initial status clarification**; approved create fields from data model. |
| **Acceptance criteria** | Valid create persists a ticket retrievable by list/details; invalid input rejected once rules approved. |
| **Tests required** | Backend create + validation tests (Phase 6); UI create (Phase 8). |
| **Risks / ambiguities** | **Cannot set initial status until clarified.** Create field set beyond named updateables not fully listed—do not invent extra create-only fields (`ui-flow.md` U1). |

### Task 3.2 — List tickets service

| Field | Content |
|-------|---------|
| **Task name** | List tickets service |
| **Objective** | Return persisted tickets for listing. |
| **Relevant specification** | `requirements.md` FR2; `ui-flow.md` U2. |
| **Files/components expected to change** | Ticket service list method. |
| **Dependencies** | Task 2.1. |
| **Acceptance criteria** | Service returns persisted tickets (ordering/pagination **not** specified—do not invent as requirements). |
| **Tests required** | Phase 6 list capability; Phase 8 UI list. |
| **Risks / ambiguities** | Sort order and pagination unspecified. |

### Task 3.3 — Get ticket details service (including comments)

| Field | Content |
|-------|---------|
| **Task name** | Get ticket details service (including comments) |
| **Objective** | Load a single ticket’s details, including persisted comments for display. |
| **Relevant specification** | `requirements.md` FR3, FR5; `ui-flow.md` U3, U5. |
| **Files/components expected to change** | Ticket/comment read service. |
| **Dependencies** | Tasks 2.1, 2.2. |
| **Acceptance criteria** | Given a ticket identifier (once defined), returns that ticket’s details and its comments. |
| **Tests required** | Phase 6/8 details + comments visibility. |
| **Risks / ambiguities** | Identifier type/format awaits data model / API contract. Behavior when ticket missing awaits API contract. |

### Task 3.4 — Update title, description, priority, assignee

| Field | Content |
|-------|---------|
| **Task name** | Update title, description, priority, assignee |
| **Objective** | Allow changing only the assignment-named updateable fields. |
| **Relevant specification** | `requirements.md` FR4; acceptance fields update + assignee change. `ui-flow.md` U4. `state-machine.md` — status **not** listed among these updateable fields. |
| **Files/components expected to change** | Ticket update service (fields only; not status unless separately clarified). |
| **Dependencies** | Tasks 2.1, 1.4, 2.0 (field constraints). |
| **Acceptance criteria** | Updates to title, description, priority, assignee persist and are visible on subsequent read; does **not** silently treat status as part of this operation unless clarification says so. |
| **Tests required** | Phase 6/8 for field and assignee updates. |
| **Risks / ambiguities** | Whether updates are allowed when status is `CLOSED`/`CANCELLED` **not specified** (`state-machine.md`). Priority allowed values unspecified. |

### Task 3.5 — Add comment service

| Field | Content |
|-------|---------|
| **Task name** | Add comment service |
| **Objective** | Append a comment to a ticket and persist it. |
| **Relevant specification** | `requirements.md` FR5; `ui-flow.md` U5. |
| **Files/components expected to change** | Comment create service. |
| **Dependencies** | Tasks 2.2, 3.3, 1.4. |
| **Acceptance criteria** | Comment persists and appears when details are loaded. |
| **Tests required** | Phase 6/8. |
| **Risks / ambiguities** | Comments on `CLOSED`/`CANCELLED` unspecified; comment payload fields await data model. |

### Task 3.6 — Search tickets by keyword

| Field | Content |
|-------|---------|
| **Task name** | Search tickets by keyword |
| **Objective** | Return tickets matching a keyword. |
| **Relevant specification** | `requirements.md` FR6; `ui-flow.md` U6; `test-strategy.md` — search works. |
| **Files/components expected to change** | Search method in ticket service / repository. |
| **Dependencies** | Tasks 2.3; **clarification of search field scope and match rules**. |
| **Acceptance criteria** | Given an approved keyword rule set, matching tickets are returned. |
| **Tests required** | Phase 6/8 once rules approved. |
| **Risks / ambiguities** | **Blocked until clarified:** which fields are searched; case sensitivity; partial vs exact match; empty keyword behavior. |

### Task 3.7 — Filter tickets by status

| Field | Content |
|-------|---------|
| **Task name** | Filter tickets by status |
| **Objective** | Return tickets whose status equals a selected status value. |
| **Relevant specification** | `requirements.md` FR7; `state-machine.md` status names; `ui-flow.md` U7. |
| **Files/components expected to change** | Filter method in ticket service / repository. |
| **Dependencies** | Task 2.1. |
| **Acceptance criteria** | Filter works for each of `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`. |
| **Tests required** | Phase 6/8 for each status value. |
| **Risks / ambiguities** | Interaction of filter + keyword when both applied **not specified** (`ui-flow.md` U7)—**blocked for combined behavior** until clarified. Implement filter alone first. |

### Task 3.8 — Backend input validation rules (approved set only)

| Field | Content |
|-------|---------|
| **Task name** | Backend input validation rules (approved set only) |
| **Objective** | Reject invalid input at the backend per an approved rule set—not invented rules. |
| **Relevant specification** | `requirements.md` FR9; `test-strategy.md` — validation tests blocked until rules approved in data-model/api-contract. |
| **Files/components expected to change** | Validation annotations/validators/service checks mapped to approved rules. |
| **Dependencies** | Tasks 1.4, 2.0, Phase 5 gate (`api-contract.md`). |
| **Acceptance criteria** | Documented invalid inputs are rejected by backend; no undocumented rules presented as assignment requirements. |
| **Tests required** | Backend validation tests in Phase 6 mapped 1:1 to approved rules. |
| **Risks / ambiguities** | **Blocked** until concrete validation rules exist. |

---

## Phase 4 — State machine

### Task 4.1 — Encode allowed transition table

| Field | Content |
|-------|---------|
| **Task name** | Encode allowed transition table |
| **Objective** | Represent exactly the allowed edges from `state-machine.md` as the single source for transition checks. |
| **Relevant specification** | `state-machine.md` allowed edges; `requirements.md` state machine section. |
| **Files/components expected to change** | Status enum + transition policy/table module (backend). |
| **Dependencies** | Task 2.1 (status persisted). |
| **Acceptance criteria** | Module exposes only: `OPEN→IN_PROGRESS`, `IN_PROGRESS→RESOLVED`, `RESOLVED→CLOSED`, `OPEN→CANCELLED`, `IN_PROGRESS→CANCELLED`. |
| **Tests required** | Unit/table tests optional; mandatory coverage via integration tests Task 6.1–6.3. |
| **Risks / ambiguities** | Skip transitions and self-transitions: treat non-listed as reject **only if** product owner confirms exhaustive “only listed arrows” (`state-machine.md` Clarifications). Until then, **do not invent** reopen or skip paths. |

### Task 4.2 — Enforce transitions on status change (backend)

| Field | Content |
|-------|---------|
| **Task name** | Enforce transitions on status change (backend) |
| **Objective** | Apply the transition table on every status change attempt; reject invalid transitions. |
| **Relevant specification** | `requirements.md` — backend-enforced; invalid rejected. `state-machine.md` enforcement rules. `test-strategy.md` — integration tests required. |
| **Files/components expected to change** | Domain/service method that changes status; rejection path. |
| **Dependencies** | Task 4.1; **clarification of how status change is invoked** (dedicated operation vs part of general update—status is not in FR4 updateable fields). |
| **Acceptance criteria** | Allowed edges succeed; non-allowed attempts rejected by backend (not UI-only). |
| **Tests required** | Tasks 6.1–6.3 (mandatory). |
| **Risks / ambiguities** | **Blocked on API/UI shape:** assignment requires transitions to work but does not name status among updateable fields or define a change-status UI (`ui-flow.md` conflict/gap). Must resolve before wiring API/UI. |

### Task 4.3 — Reject stated invalid reopen examples

| Field | Content |
|-------|---------|
| **Task name** | Reject stated invalid reopen examples |
| **Objective** | Ensure the three assignment-stated invalid transitions are rejected. |
| **Relevant specification** | `requirements.md` / `state-machine.md` / `test-strategy.md`: `CLOSED→OPEN`, `RESOLVED→OPEN`, `CANCELLED→OPEN` rejected. |
| **Files/components expected to change** | Same enforcement path as 4.2 (verification focus). |
| **Dependencies** | Task 4.2. |
| **Acceptance criteria** | Those three transitions always rejected by backend. |
| **Tests required** | Task 6.2. |
| **Risks / ambiguities** | Whether `CLOSED`/`CANCELLED` reject **all** targets (only reopen-to-OPEN examples given)—do not invent extra product rules beyond non-allowed edges once exhaustiveness is confirmed. |

### Task 4.4 — Support primary happy-path chain

| Field | Content |
|-------|---------|
| **Task name** | Support primary happy-path chain |
| **Objective** | Ensure `OPEN → IN_PROGRESS → RESOLVED → CLOSED` can be executed as successive valid transitions. |
| **Relevant specification** | `state-machine.md`; `test-strategy.md` happy path. |
| **Files/components expected to change** | Relies on 4.2; no extra transitions. |
| **Dependencies** | Task 4.2; initial status must be `OPEN` or an approved path to reach `OPEN` before the chain—**initial status unspecified**. |
| **Acceptance criteria** | Full primary chain succeeds when started from `OPEN`. |
| **Tests required** | Task 6.3. |
| **Risks / ambiguities** | **Blocked** on initial status at create if create does not yield `OPEN`. |

---

## Phase 5 — REST API

### Task 5.0 — Produce or approve `spec/api-contract.md` (gate)

| Field | Content |
|-------|---------|
| **Task name** | Produce or approve API contract (gate) |
| **Objective** | Define REST paths, methods, payloads, and error/status codes for required capabilities without inventing them in code first. |
| **Relevant specification** | `requirements.md` REST API + FRs; `api-standards.md`; `ui-flow.md` API consistency; `test-strategy.md` dependencies/gaps. |
| **Files/components expected to change** | `spec/api-contract.md` (new, when approved). |
| **Dependencies** | Task 2.0; clarification of status-change operation shape. |
| **Acceptance criteria** | Contract covers create, list, details, update (title/description/priority/assignee), add comments, keyword search, status filter, and **approved** status-transition operation; consistent with requirements; conflicts reported not silently merged. |
| **Tests required** | None (spec gate). |
| **Risks / ambiguities** | **Cannot implement concrete HTTP API until this gate completes.** Error JSON shape unspecified today. |

### Task 5.1 — REST: create ticket

| Field | Content |
|-------|---------|
| **Task name** | REST: create ticket |
| **Objective** | Expose create-ticket capability over REST. |
| **Relevant specification** | `requirements.md` FR1; `api-standards.md`; contract from 5.0. |
| **Files/components expected to change** | Controller/resource for create; DTO mapping per contract. |
| **Dependencies** | Tasks 3.1, 5.0. |
| **Acceptance criteria** | Matches approved contract; persists ticket; validation errors returned for UI. |
| **Tests required** | Phase 6 API-level create/validation once contract exists. |
| **Risks / ambiguities** | Blocked on 5.0 and initial status. |

### Task 5.2 — REST: list tickets

| Field | Content |
|-------|---------|
| **Task name** | REST: list tickets |
| **Objective** | Expose list-tickets capability over REST. |
| **Relevant specification** | `requirements.md` FR2; contract 5.0. |
| **Files/components expected to change** | List endpoint. |
| **Dependencies** | Tasks 3.2, 5.0. |
| **Acceptance criteria** | Returns persisted tickets per contract. |
| **Tests required** | Phase 6. |
| **Risks / ambiguities** | Pagination not specified—omit unless contract approves it. |

### Task 5.3 — REST: get ticket details

| Field | Content |
|-------|---------|
| **Task name** | REST: get ticket details |
| **Objective** | Expose ticket details (with comments as contracted). |
| **Relevant specification** | `requirements.md` FR3; `ui-flow.md` U3; contract 5.0. |
| **Files/components expected to change** | Details endpoint. |
| **Dependencies** | Tasks 3.3, 5.0. |
| **Acceptance criteria** | Returns one ticket’s details per contract. |
| **Tests required** | Phase 6. |
| **Risks / ambiguities** | 404/error shape awaits contract. |

### Task 5.4 — REST: update title, description, priority, assignee

| Field | Content |
|-------|---------|
| **Task name** | REST: update title, description, priority, assignee |
| **Objective** | Expose field updates for the four named fields only (unless contract later adds status separately). |
| **Relevant specification** | `requirements.md` FR4; `ui-flow.md` U4; contract 5.0. |
| **Files/components expected to change** | Update endpoint. |
| **Dependencies** | Tasks 3.4, 5.0. |
| **Acceptance criteria** | Updates named fields per contract; does not invent extra updatable properties. |
| **Tests required** | Phase 6. |
| **Risks / ambiguities** | Must not quietly fold status into this endpoint without clarification (`state-machine.md` / `ui-flow.md` gap). |

### Task 5.5 — REST: add comment

| Field | Content |
|-------|---------|
| **Task name** | REST: add comment |
| **Objective** | Expose add-comment capability. |
| **Relevant specification** | `requirements.md` FR5; contract 5.0. |
| **Files/components expected to change** | Comment create endpoint. |
| **Dependencies** | Tasks 3.5, 5.0. |
| **Acceptance criteria** | Comment created per contract; visible via details. |
| **Tests required** | Phase 6. |
| **Risks / ambiguities** | Payload fields await data model + contract. |

### Task 5.6 — REST: search by keyword

| Field | Content |
|-------|---------|
| **Task name** | REST: search by keyword |
| **Objective** | Expose keyword search. |
| **Relevant specification** | `requirements.md` FR6; contract 5.0; search clarifications. |
| **Files/components expected to change** | Search endpoint or list query param per contract. |
| **Dependencies** | Tasks 3.6, 5.0; search-rule clarification. |
| **Acceptance criteria** | Keyword search behaves per approved rules and contract. |
| **Tests required** | Phase 6. |
| **Risks / ambiguities** | **Blocked** on search semantics and contract. |

### Task 5.7 — REST: filter by status

| Field | Content |
|-------|---------|
| **Task name** | REST: filter by status |
| **Objective** | Expose filter by the five status values. |
| **Relevant specification** | `requirements.md` FR7; `state-machine.md`; contract 5.0. |
| **Files/components expected to change** | Filter endpoint or list query param per contract. |
| **Dependencies** | Tasks 3.7, 5.0. |
| **Acceptance criteria** | Each status value filters correctly. |
| **Tests required** | Phase 6. |
| **Risks / ambiguities** | Combined search+filter awaits clarification and contract. |

### Task 5.8 — REST: status transition operation (after clarification)

| Field | Content |
|-------|---------|
| **Task name** | REST: status transition operation (after clarification) |
| **Objective** | Expose the approved mechanism for requesting status changes so valid transitions work and invalid ones are rejected by backend. |
| **Relevant specification** | `requirements.md` acceptance valid/invalid transitions; `state-machine.md`; `ui-flow.md` conflict/gap; contract 5.0. |
| **Files/components expected to change** | Dedicated transition endpoint **or** contracted status field handling—**only as approved**. |
| **Dependencies** | Tasks 4.2, 5.0; **mandatory clarification** of status-change API shape. |
| **Acceptance criteria** | Allowed transitions succeed via API; invalid rejected with error UI can show meaningfully. |
| **Tests required** | Tasks 6.1–6.3 must hit this backend path (integration). |
| **Risks / ambiguities** | **Cannot implement until ambiguity resolved:** status not in FR4 updateable fields, yet transitions required. |

### Task 5.9 — REST error responses usable by UI

| Field | Content |
|-------|---------|
| **Task name** | REST error responses usable by UI |
| **Objective** | Ensure validation and transition rejections return errors the UI can display meaningfully. |
| **Relevant specification** | `requirements.md` FR10; `ui-flow.md` U8; `api-standards.md`. |
| **Files/components expected to change** | Error mapping aligned to approved `api-contract.md`. |
| **Dependencies** | Tasks 1.4, 5.0, 3.8, 4.2. |
| **Acceptance criteria** | Failed create/update/comment/transition attempts yield client-visible error information per contract (exact copy not specified). |
| **Tests required** | Backend rejection assertions (Phase 6); UI meaningful-error tests (Phase 8). |
| **Risks / ambiguities** | Exact error schema unspecified until contract. |

---

## Phase 6 — Backend tests

### Task 6.1 — Integration: allowed status transitions

| Field | Content |
|-------|---------|
| **Task name** | Integration: allowed status transitions |
| **Objective** | Prove each allowed edge succeeds against the backend. |
| **Relevant specification** | `test-strategy.md` allowed transitions table; `state-machine.md`. |
| **Files/components expected to change** | Backend integration test suite. |
| **Dependencies** | Tasks 4.2, 5.8 (or equivalent backend entry used by tests). |
| **Acceptance criteria** | Tests green for: `OPEN→IN_PROGRESS`, `IN_PROGRESS→RESOLVED`, `RESOLVED→CLOSED`, `OPEN→CANCELLED`, `IN_PROGRESS→CANCELLED`. |
| **Tests required** | These are the required tests. |
| **Risks / ambiguities** | Need a way to obtain a ticket in `OPEN` (initial status gap). |

### Task 6.2 — Integration: stated invalid transitions rejected

| Field | Content |
|-------|---------|
| **Task name** | Integration: stated invalid transitions rejected |
| **Objective** | Prove assignment invalid examples are rejected by backend. |
| **Relevant specification** | `test-strategy.md` invalid table; acceptance criterion. |
| **Files/components expected to change** | Backend integration tests. |
| **Dependencies** | Tasks 4.3, 5.8. |
| **Acceptance criteria** | `CLOSED→OPEN`, `RESOLVED→OPEN`, `CANCELLED→OPEN` rejected. |
| **Tests required** | These are the required tests. |
| **Risks / ambiguities** | Setup must reach `CLOSED`/`RESOLVED`/`CANCELLED` via allowed edges only. |

### Task 6.3 — Integration: primary chain happy path

| Field | Content |
|-------|---------|
| **Task name** | Integration: primary chain happy path |
| **Objective** | Prove `OPEN → IN_PROGRESS → RESOLVED → CLOSED` end-to-end on backend. |
| **Relevant specification** | `test-strategy.md` happy path. |
| **Files/components expected to change** | Backend integration test. |
| **Dependencies** | Tasks 4.4, 6.1. |
| **Acceptance criteria** | Full chain test passes. |
| **Tests required** | This test. |
| **Risks / ambiguities** | Initial status clarification. |

### Task 6.4 — Integration: non-listed edges (only if exhaustiveness confirmed)

| Field | Content |
|-------|---------|
| **Task name** | Integration: non-listed edges (only if exhaustiveness confirmed) |
| **Objective** | If product owner confirms “only listed arrows,” reject other non-allowed edges (e.g. skips) without inventing product features. |
| **Relevant specification** | `state-machine.md` / `test-strategy.md` clarification notes. |
| **Files/components expected to change** | Additional integration tests. |
| **Dependencies** | Clarification; Task 4.2. |
| **Acceptance criteria** | Confirmed non-listed edges rejected; no speculative cases beyond confirmation. |
| **Tests required** | As confirmed. |
| **Risks / ambiguities** | **Blocked until clarification.** Self-transitions also unspecified. |

### Task 6.5 — Backend tests: create, list, details, update, comments

| Field | Content |
|-------|---------|
| **Task name** | Backend tests: create, list, details, update, comments |
| **Objective** | Cover acceptance-mapped backend/API capabilities for core CRUD-like flows. |
| **Relevant specification** | `test-strategy.md` acceptance-mapped table (UI + API layers). |
| **Files/components expected to change** | Backend/API tests. |
| **Dependencies** | Tasks 5.1–5.5; preferably 5.0 complete. |
| **Acceptance criteria** | Create, list, details, field updates, assignee change, add comment verified at API/backend. |
| **Tests required** | Capability tests (framework choice not mandated). |
| **Risks / ambiguities** | Concrete HTTP assertions blocked until `api-contract.md`. |

### Task 6.6 — Backend tests: search and status filter

| Field | Content |
|-------|---------|
| **Task name** | Backend tests: search and status filter |
| **Objective** | Verify keyword search and per-status filter. |
| **Relevant specification** | `test-strategy.md`; `ui-flow.md` U6/U7. |
| **Files/components expected to change** | Backend/API tests. |
| **Dependencies** | Tasks 5.6, 5.7; search clarifications. |
| **Acceptance criteria** | Search matches approved rules; filter works for all five statuses. |
| **Tests required** | Those cases. |
| **Risks / ambiguities** | Search rules and filter+search combo may remain blocked. |

### Task 6.7 — Backend validation tests (approved rules only)

| Field | Content |
|-------|---------|
| **Task name** | Backend validation tests (approved rules only) |
| **Objective** | Assert invalid input is rejected by backend for each approved rule. |
| **Relevant specification** | `test-strategy.md` Backend validation testing. |
| **Files/components expected to change** | Validation tests. |
| **Dependencies** | Task 3.8; approved rules in data-model/api-contract. |
| **Acceptance criteria** | Each approved invalid case rejected; no invented rules tested as requirements. |
| **Tests required** | Mapped to approved rules. |
| **Risks / ambiguities** | **Blocked** until rules approved. |

### Task 6.8 — Persistence survives application restart

| Field | Content |
|-------|---------|
| **Task name** | Persistence survives application restart |
| **Objective** | Prove tickets and comments remain after application restart. |
| **Relevant specification** | `requirements.md` acceptance; `test-strategy.md` persistence testing. |
| **Files/components expected to change** | System/persistence test or documented manual verification procedure tied to automated check where feasible. |
| **Dependencies** | Tasks 1.3, 2.1, 2.2, 5.1, 5.5. |
| **Acceptance criteria** | Create/update (and comment) then restart; data remains. |
| **Tests required** | Persistence / system check. |
| **Risks / ambiguities** | In-memory-only H2 config would fail this—configuration must use durable store for the verification environment. |

---

## Phase 7 — Frontend

### Task 7.0 — Resolve frontend stack choice (gate)

| Field | Content |
|-------|---------|
| **Task name** | Resolve frontend stack choice (gate) |
| **Objective** | Choose React/Next.js **or equivalent** as allowed by assignment, without inventing product features. |
| **Relevant specification** | `requirements.md` technology constraints; `ui-flow.md` stack note. |
| **Files/components expected to change** | Decision only (architecture/UI notes if approved). |
| **Dependencies** | None. |
| **Acceptance criteria** | Stack chosen and recorded; auth/dashboard/etc. remain out of scope per `ui-flow.md`. |
| **Tests required** | None. |
| **Risks / ambiguities** | “Or equivalent” is open until chosen. |

### Task 7.1 — Frontend app shell and API client baseline

| Field | Content |
|-------|---------|
| **Task name** | Frontend app shell and API client baseline |
| **Objective** | Create frontend project shell that can call the REST API and display meaningful errors. |
| **Relevant specification** | `ui-flow.md` U8; `requirements.md` meaningful errors; depends on API contract. |
| **Files/components expected to change** | Frontend app entry, routing shell (minimal), API client wrapper. |
| **Dependencies** | Tasks 7.0, 5.0 (paths/error shape). |
| **Acceptance criteria** | App runs; can invoke backend; error handler can surface messages. |
| **Tests required** | Phase 8. |
| **Risks / ambiguities** | Visual design/component library **not** specified—keep minimal. CORS/base URL not specified—coordinate with architecture without inventing product rules. |

### Task 7.2 — UI: create ticket (U1)

| Field | Content |
|-------|---------|
| **Task name** | UI: create ticket |
| **Objective** | User can create a ticket from the UI. |
| **Relevant specification** | `ui-flow.md` U1; acceptance create from UI. |
| **Files/components expected to change** | Create-ticket entry/form; submit wiring to create API. |
| **Dependencies** | Tasks 7.1, 5.1. |
| **Acceptance criteria** | Successful create persists and ticket appears via list/details; failure shows meaningful error. |
| **Tests required** | Task 8.1. |
| **Risks / ambiguities** | Exact create fields/layout not specified; do not invent extra create-only fields. |

### Task 7.3 — UI: list tickets (U2)

| Field | Content |
|-------|---------|
| **Task name** | UI: list tickets |
| **Objective** | Show persisted tickets. |
| **Relevant specification** | `ui-flow.md` U2. |
| **Files/components expected to change** | Ticket list view. |
| **Dependencies** | Tasks 7.1, 5.2. |
| **Acceptance criteria** | List displays tickets from API/DB. |
| **Tests required** | Task 8.2. |
| **Risks / ambiguities** | Pagination UI not required. |

### Task 7.4 — UI: ticket details (U3)

| Field | Content |
|-------|---------|
| **Task name** | UI: ticket details |
| **Objective** | View a single ticket’s details from the list (or equivalent). |
| **Relevant specification** | `ui-flow.md` U3. |
| **Files/components expected to change** | Details view. |
| **Dependencies** | Tasks 7.3, 5.3. |
| **Acceptance criteria** | Selecting a ticket shows its details. |
| **Tests required** | Task 8.2. |
| **Risks / ambiguities** | Layout not specified. |

### Task 7.5 — UI: update title, description, priority, assignee (U4)

| Field | Content |
|-------|---------|
| **Task name** | UI: update title, description, priority, assignee |
| **Objective** | Allow editing the four named fields from details/edit entry. |
| **Relevant specification** | `ui-flow.md` U4; requirements FR4. |
| **Files/components expected to change** | Edit form/controls for those fields. |
| **Dependencies** | Tasks 7.4, 5.4. |
| **Acceptance criteria** | Updates persist and reflect in UI; errors meaningful. |
| **Tests required** | Task 8.3. |
| **Risks / ambiguities** | Status not part of this flow unless clarification adds a separate control. |

### Task 7.6 — UI: add comments (U5)

| Field | Content |
|-------|---------|
| **Task name** | UI: add comments |
| **Objective** | Submit a comment on details; show persisted comments. |
| **Relevant specification** | `ui-flow.md` U5. |
| **Files/components expected to change** | Comment list + submit control on details. |
| **Dependencies** | Tasks 7.4, 5.5. |
| **Acceptance criteria** | Comment persists and is visible; errors meaningful. |
| **Tests required** | Task 8.3. |
| **Risks / ambiguities** | Comment fields await data model. |

### Task 7.7 — UI: search by keyword (U6)

| Field | Content |
|-------|---------|
| **Task name** | UI: search by keyword |
| **Objective** | Keyword entry on list (or equivalent) returns matches. |
| **Relevant specification** | `ui-flow.md` U6. |
| **Files/components expected to change** | Search control on list. |
| **Dependencies** | Tasks 7.3, 5.6; search clarifications. |
| **Acceptance criteria** | Search works per approved backend behavior. |
| **Tests required** | Task 8.4. |
| **Risks / ambiguities** | **Blocked** on search semantics. |

### Task 7.8 — UI: filter by status (U7)

| Field | Content |
|-------|---------|
| **Task name** | UI: filter by status |
| **Objective** | Status filter using the five status names. |
| **Relevant specification** | `ui-flow.md` U7; `state-machine.md`. |
| **Files/components expected to change** | Status filter control on list. |
| **Dependencies** | Tasks 7.3, 5.7. |
| **Acceptance criteria** | Filtering by each status shows matching tickets. |
| **Tests required** | Task 8.4. |
| **Risks / ambiguities** | Combined search+filter UI behavior unspecified. |

### Task 7.9 — UI: status transition control (only after clarification)

| Field | Content |
|-------|---------|
| **Task name** | UI: status transition control (only after clarification) |
| **Objective** | If/when product exposes status change, wire it to the approved API so valid transitions work and invalid ones show meaningful errors. |
| **Relevant specification** | `ui-flow.md` Status transitions and the UI (conflict/gap); acceptance valid/invalid transitions; U8. |
| **Files/components expected to change** | Status-change UI **only if approved**—not invented as a required screen beforehand. |
| **Dependencies** | Clarification; Tasks 5.8, 7.4. |
| **Acceptance criteria** | Valid transitions succeed from UI; invalid show meaningful errors; backend remains enforcer. |
| **Tests required** | Task 8.5. |
| **Risks / ambiguities** | **Cannot implement as a required screen until clarification.** `ui-flow.md` currently does not add a dedicated status-transition flow. |

### Task 7.10 — UI: meaningful errors across required flows (U8)

| Field | Content |
|-------|---------|
| **Task name** | UI: meaningful errors across required flows |
| **Objective** | Ensure create, update, comments, and any transition attempt surface understandable errors on backend rejection. |
| **Relevant specification** | `ui-flow.md` U8; `test-strategy.md` UI error testing. |
| **Files/components expected to change** | Shared error display wired into U1/U4/U5/(U9 if any). |
| **Dependencies** | Tasks 5.9, 7.2–7.6, 7.9 if applicable. |
| **Acceptance criteria** | Failed actions show meaningful errors (exact copy not specified). |
| **Tests required** | Task 8.5. |
| **Risks / ambiguities** | Presentation (toast vs inline) not specified. |

---

## Phase 8 — Frontend tests

### Task 8.1 — Frontend tests: create ticket + error

| Field | Content |
|-------|---------|
| **Task name** | Frontend tests: create ticket + error |
| **Objective** | Verify create flow success and meaningful error on failure. |
| **Relevant specification** | `test-strategy.md` acceptance-mapped + UI error testing. |
| **Files/components expected to change** | Frontend tests for U1/U8. |
| **Dependencies** | Tasks 7.2, 7.10. |
| **Acceptance criteria** | Create success path covered; rejection shows meaningful error. |
| **Tests required** | These UI tests (tool choice not mandated). |
| **Risks / ambiguities** | Framework not decided. |

### Task 8.2 — Frontend tests: list and details

| Field | Content |
|-------|---------|
| **Task name** | Frontend tests: list and details |
| **Objective** | Verify list and details views. |
| **Relevant specification** | `test-strategy.md` list/details rows. |
| **Files/components expected to change** | Frontend tests for U2/U3. |
| **Dependencies** | Tasks 7.3, 7.4. |
| **Acceptance criteria** | List and details behaviors verified. |
| **Tests required** | These UI tests. |
| **Risks / ambiguities** | None beyond missing visual spec. |

### Task 8.3 — Frontend tests: field update, assignee, comments

| Field | Content |
|-------|---------|
| **Task name** | Frontend tests: field update, assignee, comments |
| **Objective** | Verify updates to title/description/priority, assignee change, and add comment. |
| **Relevant specification** | `test-strategy.md` corresponding acceptance rows. |
| **Files/components expected to change** | Frontend tests for U4/U5. |
| **Dependencies** | Tasks 7.5, 7.6. |
| **Acceptance criteria** | Those flows verified including error surfacing where applicable. |
| **Tests required** | These UI tests. |
| **Risks / ambiguities** | Terminal-status edit rules unspecified. |

### Task 8.4 — Frontend tests: search and status filter

| Field | Content |
|-------|---------|
| **Task name** | Frontend tests: search and status filter |
| **Objective** | Verify keyword search and status filter UI. |
| **Relevant specification** | `test-strategy.md`; `ui-flow.md` U6/U7. |
| **Files/components expected to change** | Frontend tests. |
| **Dependencies** | Tasks 7.7, 7.8; search clarifications. |
| **Acceptance criteria** | Search and filter behaviors match approved rules; filter covers five statuses. |
| **Tests required** | These UI tests. |
| **Risks / ambiguities** | Search + combined filter may be blocked. |

### Task 8.5 — Frontend tests: status transition errors (if UI exposed)

| Field | Content |
|-------|---------|
| **Task name** | Frontend tests: status transition errors (if UI exposed) |
| **Objective** | If status-change UI exists, assert meaningful errors on invalid transitions and success on valid ones. |
| **Relevant specification** | `test-strategy.md` UI error testing; acceptance valid/invalid transitions (backend still authoritative). |
| **Files/components expected to change** | Frontend tests for transition UI. |
| **Dependencies** | Task 7.9 clarification outcome. |
| **Acceptance criteria** | Only if UI exposed: valid works; invalid shows meaningful error. |
| **Tests required** | Conditional on 7.9. |
| **Risks / ambiguities** | **Blocked** if status UI remains unapproved; backend integration tests still mandatory regardless. |

---

## Phase 9 — Integration

### Task 9.1 — End-to-end: create → list → details

| Field | Content |
|-------|---------|
| **Task name** | End-to-end: create → list → details |
| **Objective** | Exercise UI + API + DB for the core read/write path. |
| **Relevant specification** | Acceptance criteria 1–3; `test-strategy.md` goals. |
| **Files/components expected to change** | E2E or manual integration checklist/automation as chosen (tool not mandated). |
| **Dependencies** | Phases 5–8 core UI/API tasks. |
| **Acceptance criteria** | Ticket created from UI appears in list and details. |
| **Tests required** | Cross-layer verification. |
| **Risks / ambiguities** | E2E tool not specified. |

### Task 9.2 — End-to-end: update fields, assignee, comments

| Field | Content |
|-------|---------|
| **Task name** | End-to-end: update fields, assignee, comments |
| **Objective** | Verify updates and comments through UI against persisted backend. |
| **Relevant specification** | Acceptance 4–6. |
| **Files/components expected to change** | E2E/integration verification. |
| **Dependencies** | Tasks 7.5, 7.6, 5.4, 5.5. |
| **Acceptance criteria** | Field/assignee/comment changes persist and display. |
| **Tests required** | Cross-layer verification. |
| **Risks / ambiguities** | None beyond prior field ambiguities. |

### Task 9.3 — End-to-end: search and status filter

| Field | Content |
|-------|---------|
| **Task name** | End-to-end: search and status filter |
| **Objective** | Verify search and filter through UI + API + DB. |
| **Relevant specification** | Acceptance 7–8. |
| **Files/components expected to change** | E2E/integration verification. |
| **Dependencies** | Tasks 7.7, 7.8, 5.6, 5.7; search clarifications. |
| **Acceptance criteria** | Search and status filter work in integrated system. |
| **Tests required** | Cross-layer verification. |
| **Risks / ambiguities** | Search rules; filter+search combination. |

### Task 9.4 — End-to-end: valid and invalid status transitions

| Field | Content |
|-------|---------|
| **Task name** | End-to-end: valid and invalid status transitions |
| **Objective** | Confirm transitions succeed/reject with backend enforcement visible to the client used in acceptance. |
| **Relevant specification** | Acceptance 9–10, 14; state-machine integration tests must pass. |
| **Files/components expected to change** | Relies on Phase 6 suite + optional UI path from 7.9. |
| **Dependencies** | Tasks 6.1–6.3; 5.8; 7.9 if UI path required for “works” acceptance beyond API. |
| **Acceptance criteria** | Valid transitions work; invalid rejected by backend; state-machine integration tests green. |
| **Tests required** | Mandatory backend integration suite; UI only if transition UI approved. |
| **Risks / ambiguities** | How acceptance “valid status transitions work” is demonstrated in UI without a status control—**clarification required**. |

### Task 9.5 — End-to-end: persistence across restart

| Field | Content |
|-------|---------|
| **Task name** | End-to-end: persistence across restart |
| **Objective** | Create/update data, restart application, confirm data remains visible via API/UI. |
| **Relevant specification** | Acceptance 11; `test-strategy.md` persistence. |
| **Files/components expected to change** | Verification procedure/automation (Task 6.8 evidence). |
| **Dependencies** | Task 6.8; running full stack. |
| **Acceptance criteria** | Data survives restart. |
| **Tests required** | Persistence check. |
| **Risks / ambiguities** | Durable DB configuration must be used. |

### Task 9.6 — End-to-end: backend validation + UI meaningful errors

| Field | Content |
|-------|---------|
| **Task name** | End-to-end: backend validation + UI meaningful errors |
| **Objective** | Invalid input rejected by backend and shown meaningfully in UI. |
| **Relevant specification** | Acceptance 12–13; `test-strategy.md`. |
| **Files/components expected to change** | Integrated verification using approved invalid fixtures. |
| **Dependencies** | Tasks 3.8, 5.9, 7.10, 6.7, 8.1. |
| **Acceptance criteria** | Validation works; UI errors meaningful. |
| **Tests required** | Combined backend + UI checks. |
| **Risks / ambiguities** | **Blocked** on concrete validation rule list. |

---

## Phase 10 — Final review

### Task 10.1 — Spec consistency review (requirements vs implementation)

| Field | Content |
|-------|---------|
| **Task name** | Spec consistency review |
| **Objective** | Confirm implementation matches specs and did not invent features. |
| **Relevant specification** | All `spec/*`; `commands/review-spec.md` / `commands/review-code.md` as process aids. |
| **Files/components expected to change** | Review notes; fixes only if gaps against **stated** requirements. |
| **Dependencies** | Phases 1–9 substantially complete. |
| **Acceptance criteria** | No invented auth/attachments/reopen/delete/notifications; status names exact; FRs covered. |
| **Tests required** | Re-run required suites after fixes. |
| **Risks / ambiguities** | Flag remaining unresolved ambiguities rather than “fixing” by invention. |

### Task 10.2 — Acceptance criteria checklist

| Field | Content |
|-------|---------|
| **Task name** | Acceptance criteria checklist |
| **Objective** | Walk all 15 core acceptance criteria from `requirements.md`. |
| **Relevant specification** | `requirements.md` Core acceptance criteria. |
| **Files/components expected to change** | Checklist record (plan/review artefact). |
| **Dependencies** | Phase 9. |
| **Acceptance criteria** | Each of the 15 items marked pass/fail with evidence pointer. |
| **Tests required** | Evidence from Phases 6–9. |
| **Risks / ambiguities** | Items depending on clarifications may remain fail until resolved. |

### Task 10.3 — Secrets and repository hygiene check

| Field | Content |
|-------|---------|
| **Task name** | Secrets and repository hygiene check |
| **Objective** | Ensure no secrets are committed. |
| **Relevant specification** | `requirements.md` acceptance 15; process constraints. |
| **Files/components expected to change** | Remove any accidental secrets; config templates only. |
| **Dependencies** | None (can run anytime; mandatory at end). |
| **Acceptance criteria** | No API keys, passwords, certificates, or private credentials in repo. |
| **Tests required** | Process/review check. |
| **Risks / ambiguities** | None. |

### Task 10.4 — State-machine integration suite final gate

| Field | Content |
|-------|---------|
| **Task name** | State-machine integration suite final gate |
| **Objective** | Confirm required state-machine integration tests are green before calling the solution complete. |
| **Relevant specification** | Acceptance 14; `test-strategy.md`. |
| **Files/components expected to change** | None if green; otherwise Phase 4/5/6 fixes. |
| **Dependencies** | Tasks 6.1–6.3. |
| **Acceptance criteria** | Suite passes. |
| **Tests required** | State-machine integration tests. |
| **Risks / ambiguities** | None beyond earlier transition API clarification. |

### Task 10.5 — Record prompts and AI mistakes (process)

| Field | Content |
|-------|---------|
| **Task name** | Record prompts and AI mistakes (process) |
| **Objective** | Satisfy assignment process: save prompts; identify meaningful AI mistakes/incorrect suggestions. |
| **Relevant specification** | `requirements.md` process/quality constraints; `rules/documentation.md`. |
| **Files/components expected to change** | `docs/prompt-history.md`; SpecStory history (existing practice). |
| **Dependencies** | Ongoing; finalize in review. |
| **Acceptance criteria** | Important prompts/decisions recorded; at least meaningful AI mistakes identified (process requirement). |
| **Tests required** | None. |
| **Risks / ambiguities** | Token-optimisation plugins named in assignment are process tools—not product features; do not build them into the app. |

---

## Cross-cutting dependency map (backend ↔ API ↔ frontend)

```
Clarifications + data-model.md + api-contract.md
        │
        ▼
Backend foundation (P1) → Entities (P2) → Services (P3) → State machine (P4)
        │                      │              │                │
        └──────────► REST API (P5) ◄──────────┴────────────────┘
                           │
              Backend tests (P6)  [state machine tests require P4+P5.8]
                           │
              Frontend stack (P7.0) → UI flows (P7) → Frontend tests (P8)
                           │
                     Integration (P9) → Final review (P10)
```

**Hard ordering rules:**

1. Do not code entities until Task 2.0 (`data-model.md`) is approved.
2. Do not code HTTP paths/schemas until Task 5.0 (`api-contract.md`) is approved.
3. Frontend API wiring depends on Phase 5 contracts; UI flows U1–U8 depend on matching endpoints.
4. State-machine **integration tests** depend on a real backend transition path (Task 5.8), which itself depends on clarifying how status changes are requested.
5. Search UI/API/tests share one clarification gate (keyword semantics).
6. Validation UI/API/tests share one clarification gate (approved validation rules).

---

## Summary

### Implementation phases

1. Backend foundation  
2. Database/entities  
3. Services/business rules  
4. State machine  
5. REST API  
6. Backend tests  
7. Frontend  
8. Frontend tests  
9. Integration  
10. Final review  

### Task count

**48 tasks** total (including gate/clarification tasks 1.1, 2.0, 5.0, 7.0).

| Phase | Tasks |
|-------|-------|
| 1 Backend foundation | 4 |
| 2 Database/entities | 4 |
| 3 Services/business rules | 8 |
| 4 State machine | 4 |
| 5 REST API | 10 |
| 6 Backend tests | 8 |
| 7 Frontend | 11 |
| 8 Frontend tests | 5 |
| 9 Integration | 6 |
| 10 Final review | 5 |

### Major dependencies

- Missing `spec/data-model.md` and `spec/api-contract.md` (and preferably `spec/architecture.md`) gate concrete persistence, HTTP, and validation work.
- Status transition **API + UI** depend on resolving the FR4 vs acceptance conflict (`ui-flow.md`).
- Frontend depends on REST contract and error shape.
- Mandatory state-machine integration tests depend on backend transition enforcement + reachable `OPEN` tickets.
- Persistence-across-restart depends on durable DB configuration (PostgreSQL and/or H2 per assignment).
- Search and combined filter+search depend on keyword semantics clarification.
- Validation tests/UI depend on an approved validation rule set (not inventable from assignment alone).

### Risks

- Implementing status change via general ticket update without clarification would invent API/UI behavior the specs explicitly flag as a gap.
- Choosing search fields, priority enums, or initial status without approval would invent requirements.
- Using non-durable DB settings would fail “survives application restart.”
- Inventing auth, pagination, attachments, reopen, or delete would be scope creep (`ui-flow.md` out of scope).
- Writing HTTP assertions before `api-contract.md` will churn or invent contracts.
- Treating AI-suggested fields/endpoints as requirements (process risk called out in assignment).

### Items requiring clarification

**Cannot be implemented until resolved:**

1. **Initial status** on ticket create.  
2. **How status is changed** (dedicated API/UI vs update payload)—FR4 omits status; acceptance still requires transitions.  
3. **Whether “only listed arrows” is exhaustive** (skips, self-transitions).  
4. **Keyword search** field scope, case rules, match style; **filter + search** combined behavior.  
5. **Concrete validation rules** (required fields, lengths, priority domain, etc.).  
6. **Comment** attribute set (body/author/timestamps).  
7. **Priority** allowed values/type.  
8. Whether **field updates/comments** are allowed in `CLOSED` / `CANCELLED`.  
9. **PostgreSQL vs H2** (or both) for which environments.  
10. **Frontend stack** choice among React/Next.js or equivalent.  
11. **API error/HTTP status** contract (`api-contract.md` missing).  
12. **Data model** identifiers and any non-named attributes (`data-model.md` missing).  
13. **Architecture** packaging/persistence technology choices (`architecture.md` missing).  

**Explicitly not treated as requirements in this plan:** authentication/roles, dashboards, notifications, attachments, delete ticket, reopen paths, pagination, coverage %, specific UI chrome/visual design.

---

## Confirmation

- Every file under `spec/` was read before writing this plan.  
- **No application code** was written or modified.  
- Only artefact created for this step: `plan/implementation-plan.md`.  
- No new product requirements were invented; gaps are listed as clarifications or gate tasks.
