# Data model

**Status:** Approved for implementation (project decision document).

**Sources:** Field names from `spec/requirements.md`; status values from `spec/state-machine.md`.

Technical attributes required for persistence/REST (identifiers) and clarifications previously blocking implementation are recorded here as **approved project decisions**, not as original assignment text.

## Entities

Only two entities: **Ticket** and **Comment**.

No User, Tag, SLA, Audit, or Notification entities.

---

### Ticket

| Attribute | Type | Required | Notes |
|-----------|------|----------|--------|
| `id` | `Long` | yes (generated) | Surrogate key; approved technical attribute |
| `title` | `String` | yes | Named in requirements; non-blank |
| `description` | `String` | no | Named in requirements; may be null or blank-normalized to null |
| `priority` | `String` | no | Named in requirements; free-form string (no invented enum) |
| `assignee` | `String` | no | Named in requirements; free-form string (not a User entity) |
| `status` | enum / string | yes | Exact values: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED` |

#### Ticket rules (approved)

1. **Initial status on create:** always `OPEN`.
2. **Status** is not updated via the general field-update operation; status changes use the dedicated transition operation (`spec/api-contract.md`).
3. Field updates (`title`, `description`, `priority`, `assignee`) and comments are **allowed** in any status, including `CLOSED` and `CANCELLED` (assignment did not forbid this; approved as allowed).
4. No `createdBy`, `createdAt`, `updatedAt`, tags, SLA, or audit columns.

#### Persistence constraints (approved technical limits)

| Field | DB / validation limit |
|-------|------------------------|
| `title` | max 200 characters; required non-blank |
| `description` | max 5000 characters when present |
| `priority` | max 50 characters when present |
| `assignee` | max 100 characters when present |
| `status` | exactly one of the five status names |

---

### Comment

| Attribute | Type | Required | Notes |
|-----------|------|----------|--------|
| `id` | `Long` | yes (generated) | Surrogate key; approved technical attribute |
| `ticket` | many-to-one → Ticket | yes | Comment belongs to one ticket |
| `text` | `String` | yes | Approved attribute name for comment body (assignment said “comments” only) |

#### Comment rules (approved)

1. `text` is required and non-blank; max **2000** characters.
2. No author/user field (no users in scope).
3. No timestamps unless later approved (omit).
4. Deleting tickets/comments is **not** required; do not implement delete APIs.

#### Relationship

- Ticket **one-to-many** Comments (orphan comments not allowed).
- Loading ticket details includes that ticket’s comments (order: by `id` ascending).

---

## Status values

Must match `spec/state-machine.md` exactly:

`OPEN` | `IN_PROGRESS` | `RESOLVED` | `CLOSED` | `CANCELLED`

## Search and filter (data semantics, approved)

| Capability | Rule |
|------------|------|
| Keyword search | Case-insensitive substring match on **`title` OR `description`** |
| Empty / blank keyword | Invalid input (rejected) |
| Status filter | Exact match on `status` |
| Keyword + status together | **AND** (match keyword rule and status) |

## Out of scope for the model

- Users / accounts / roles  
- Tags  
- Attachments  
- SLA fields  
- Audit / notification fields  
- Soft delete flags  

## Consistency

- Transitions: `spec/state-machine.md`  
- HTTP shapes: `spec/api-contract.md`
