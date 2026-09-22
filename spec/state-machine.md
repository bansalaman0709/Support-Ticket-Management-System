# Ticket status state machine

Source of truth: `spec/requirements.md`. Backend **must** enforce this machine. Invalid transitions **must** be rejected.

This file defines only status values and transitions explicitly required by the assignment. It does not invent reopen paths, extra statuses, or UI behavior.

## Status values (exact names)

| Status         | Notes |
|----------------|--------|
| `OPEN`         | Named in the assignment |
| `IN_PROGRESS`  | Named in the assignment |
| `RESOLVED`     | Named in the assignment |
| `CLOSED`       | Named in the assignment |
| `CANCELLED`    | Named in the assignment |

No other status values are required.

## Allowed transitions

Stated allowed machine:

- `OPEN` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`
- `OPEN` → `CANCELLED`
- `IN_PROGRESS` → `CANCELLED`

Interpreted as the following **allowed edges** (only these are stated):

| From           | To             |
|----------------|----------------|
| `OPEN`         | `IN_PROGRESS`  |
| `IN_PROGRESS`  | `RESOLVED`     |
| `RESOLVED`     | `CLOSED`       |
| `OPEN`         | `CANCELLED`    |
| `IN_PROGRESS`  | `CANCELLED`    |

```
                  ┌──────────────┐
                  │   CANCELLED  │
                  └──────▲───────┘
                         │
          OPEN ──────────┤
           │             │
           ▼             │
      IN_PROGRESS ───────┘
           │
           ▼
        RESOLVED
           │
           ▼
         CLOSED
```

## Invalid transitions

Any transition that is **not** an allowed edge above **must** be rejected by the backend.

Invalid examples **explicitly stated** in the assignment (must be rejected):

| From         | To       | Result   |
|--------------|----------|----------|
| `CLOSED`     | `OPEN`   | rejected |
| `RESOLVED`   | `OPEN`   | rejected |
| `CANCELLED`  | `OPEN`   | rejected |

## Enforcement rules (from requirements)

1. Enforcement is **backend** responsibility (not UI-only).
2. Invalid transitions must be rejected.
3. Valid transitions must succeed (acceptance: “Valid status transitions work”).
4. State-machine **integration tests** must cover allowed and rejected behavior (see `spec/test-strategy.md`).

## Explicitly not specified (do not invent)

Documented here so implementers do not fill gaps silently:

- **Initial status** on ticket create (not stated).
- Whether status change is a dedicated operation or part of a general ticket update (assignment names updateable fields as title, description, priority, assignee — **not** status — while still requiring valid transitions to work).
- Whether skip transitions (e.g. `OPEN` → `RESOLVED`) are allowed: they are **not** listed as allowed edges; treat as **not allowed** only if the product owner confirms that “only listed arrows” is the full set (see Clarifications).
- Behavior of self-transitions (e.g. `OPEN` → `OPEN`).
- Whether `CLOSED` and `CANCELLED` are terminal for **all** targets (only reopen-to-`OPEN` examples are given).
- Whether field updates (title/description/priority/assignee) or comments are allowed while status is `CLOSED` or `CANCELLED`.
- HTTP status codes / error body shape for rejected transitions (deferred to `spec/api-contract.md`, which is currently missing).

## Consistency notes

- Status names used for filtering (`Filter tickets by status`) must use the same five values above.
- This document must stay aligned with `spec/requirements.md`. If another spec disagrees, prefer identifying the conflict over silently merging.
