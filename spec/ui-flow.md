# UI flows

Source of truth: `spec/requirements.md`.

Defines **only** user flows required by stated functional requirements and UI-related acceptance criteria. Does not invent screens, layouts, navigation chrome, roles, or extra features.

Frontend stack constraint (assignment): **React/Next.js or equivalent** — choice not decided here.

## Required UI capabilities

| # | Capability | Acceptance linkage |
|---|------------|--------------------|
| U1 | Create a ticket | Ticket can be created from UI |
| U2 | List tickets | Tickets can be listed |
| U3 | View ticket details | Ticket details can be viewed |
| U4 | Update title, description, priority, assignee | Ticket fields can be updated; Assignee can be changed |
| U5 | Add comments | Comments can be added |
| U6 | Search tickets by keyword | Search works |
| U7 | Filter tickets by status | Status filter works |
| U8 | Show meaningful errors | UI shows meaningful errors |
| U9 | Change ticket status | Valid status transitions work; invalid rejected by backend |

## Flows

### U1 — Create ticket

1. User opens the create-ticket entry point (exact screen layout not specified).
2. User provides ticket input needed to create a ticket (exact fields for create beyond product need are not fully listed; updateable fields named elsewhere are title, description, priority, assignee — **do not invent extra create-only fields**).
3. User submits.
4. On success: ticket is created and persisted; user can subsequently see it via list/details flows.
5. On failure (backend validation or other error): UI displays a **meaningful error**.

### U2 — List tickets

1. User opens the ticket list.
2. System shows tickets from persisted data.
3. User may combine with U6 (search) and/or U7 (status filter) when those controls are used.

### U3 — View ticket details

1. User selects a ticket from the list (or equivalent entry to a single ticket).
2. System shows that ticket’s details.
3. From details, user may perform U4 (field updates) and U5 (add comments), subject to backend rules.

### U4 — Update title, description, priority, assignee

1. User is viewing a ticket (or an edit entry point for that ticket).
2. User changes one or more of: **title**, **description**, **priority**, **assignee**.
3. User submits the update.
4. On success: changes are persisted and reflected in details/list as applicable.
5. On failure: UI displays a meaningful error.

Status is **not** listed among these updateable fields in requirements; see Ambiguities.

### U5 — Add comments

1. User is on ticket details (or equivalent).
2. User submits a comment.
3. On success: comment is persisted and visible in ticket details.
4. On failure: UI displays a meaningful error.

### U6 — Search by keyword

1. User enters a keyword on the list (or equivalent search entry).
2. System returns matching tickets.
3. Which fields are searched, case rules, and match style are **not** specified (see Ambiguities).

### U7 — Filter by status

1. User chooses a status filter using values from `spec/state-machine.md` (`OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`).
2. System shows tickets matching that status.
3. Interaction of filter + keyword search when both are applied is **not** specified.

### U8 — Meaningful errors

Applies to all write and read flows that can fail:

- Backend validation failures.
- Rejected invalid status transitions (when the UI triggers a transition attempt).
- Other API/business failures that the UI surfaces.

Exact copy, error codes, and toast-vs-inline presentation are **not** specified. Requirement is only that errors are **meaningful** to the user.

## Status transitions and the UI

Requirements:

- Backend enforces the state machine (`spec/state-machine.md`).
- Acceptance: valid status transitions work; invalid ones are rejected by the backend.

Functional requirement #4 names updateable fields as title, description, priority, assignee — **not** status.

**Approved resolution:** Status is changed via a dedicated API (`POST /api/tickets/{id}/status` in `spec/api-contract.md`), not via U4 field update.

### U9 — Change status (approved)

1. User is on ticket details (or equivalent).
2. User requests a new status using only values from `spec/state-machine.md`.
3. On success: status updates and is reflected in details/list/filter.
4. On invalid transition or other failure: UI displays a meaningful error (U8).

Filter-by-status (U7) remains required and uses the five status names.

## API consistency

UI flows depend on the REST API in `spec/api-contract.md`.

- Paths, methods, payloads, and status codes must follow that contract.
- If `api-contract.md` and `requirements.md` ever conflict, report the conflict; do not silently pick one.

## Explicitly out of scope for UI (not required)

Do not treat as required UI:

- Authentication / login / roles
- Dashboards, analytics, notifications
- Attachments, delete ticket, reopen ticket
- Pagination UI (not specified)
- Specific visual design, routing structure, or component library choices beyond the stack constraint
