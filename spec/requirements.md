# Support Ticket Management System — Requirements

Source: SE / SSE Assignment (`Assessments .docx`).

This file captures **only** requirements stated in the assignment. It does not record implementation choices.

## Out of scope for this file

- Architecture, data model, API contract, UI flows, and test strategy details that were **not** specified in the assignment.
- Features, fields, screens, validations, and technical behavior that were **not** stated.

## Product

Build a Support Ticket Management System.

## Explicit functional requirements

1. Create a ticket.
2. List tickets.
3. View ticket details.
4. Update title, description, priority and assignee.
5. Add comments.
6. Search tickets by keyword.
7. Filter tickets by status.
8. Persist data in a database.
9. Validate input at the backend.
10. Display meaningful errors in the UI.

## Status state machine (backend-enforced)

The following state machine **must be enforced by the backend**.

Allowed transitions stated in the assignment:

- `OPEN` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`
- `OPEN` → `CANCELLED`
- `IN_PROGRESS` → `CANCELLED`

Invalid transitions **must be rejected**.

Invalid examples stated in the assignment:

- `CLOSED` → `OPEN` — rejected
- `RESOLVED` → `OPEN` — rejected
- `CANCELLED` → `OPEN` — rejected

## Explicit technology constraints

The assignment requires the system to be built using:

- Java 21
- Spring Boot
- PostgreSQL/H2
- REST API
- React/Next.js or equivalent frontend
- Cursor
- GitHub Copilot

## Process / quality constraints stated in the assignment

- Use Cursor / Kiro / VS Code with spec-driven development.
- Workflow: Requirement → Specification → Plan / Tasks → Implementation → Testing → Review → Fix.
- Do not start by asking AI to “Build the complete application.”
- Create specifications before implementation.
- Maintain reusable AI steering artefacts (Java Spring Boot guidelines, testing guidelines, API standards, documentation skills, and commands to review code, review spec, and generate tests).
- Save prompts (SpecStory `.specstory/history/` and `docs/prompt-history.md`).
- Identify at least meaningful mistakes or incorrect suggestions by the AI.
- Use token-optimisation plugins (examples named: Graphify, Caveman, Codebase-memory MCP).
- No secrets are committed.

## Core acceptance criteria (verbatim intent)

The solution is complete when:

1. Ticket can be created from UI.
2. Tickets can be listed.
3. Ticket details can be viewed.
4. Ticket fields can be updated.
5. Assignee can be changed.
6. Comments can be added.
7. Search works.
8. Status filter works.
9. Valid status transitions work.
10. Invalid status transitions are rejected by backend.
11. Data survives application restart.
12. Backend validation works.
13. UI shows meaningful errors.
14. State-machine integration tests pass.
15. No secrets are committed.

## Fields named in the assignment

The assignment names these updateable ticket fields: **title**, **description**, **priority**, **assignee**.

The assignment also names **comments**, **keyword** search, and **status** (filter and state machine).

No other fields, types, enums, or screen layouts are specified.

## Ambiguities (not assumed)

See the “Ambiguities / questions” section in the delivery notes for this SDD pass. Do not treat unanswered items as requirements.
