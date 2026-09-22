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

## Approved clarifications (see also data-model / api-contract)

Previously unspecified items **approved** for implementation:

| Topic | Approved decision | Spec |
|-------|-------------------|------|
| Initial status on create | Always `OPEN` | `data-model.md` |
| How status is changed | Dedicated `POST /api/tickets/{id}/status` (not via field PATCH) | `api-contract.md` |
| Skip / non-listed / self-transitions | **Rejected** — only the allowed edges table above | this file + `api-contract.md` |
| `CLOSED` / `CANCELLED` as sources | Only listed edges allowed; no reopen except if an edge exists (none do) | this file |
| Field updates / comments when terminal | **Allowed** in any status | `data-model.md` |
| HTTP codes / error body for rejects | `409` + error shape in API contract | `api-contract.md` |

## Still not specified (do not invent further)

- UI chrome for status change (see `ui-flow.md`); backend contract is defined.

## Consistency notes

- Status names used for filtering (`Filter tickets by status`) must use the same five values above.
- This document must stay aligned with `spec/requirements.md`. If another spec disagrees, prefer identifying the conflict over silently merging.
