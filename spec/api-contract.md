# API contract

**Status:** Approved for implementation (project decision document).

**Sources:** Capabilities from `spec/requirements.md`; status rules from `spec/state-machine.md`; fields from `spec/data-model.md`.

Base path: `/api`  
Content-Type: `application/json`  
No authentication.

---

## Error response (approved)

All error responses use this JSON shape:

```json
{
  "message": "Human-readable summary",
  "errors": [
    {
      "field": "optionalFieldName",
      "message": "Field-specific detail"
    }
  ]
}
```

- `message` is always present.
- `errors` may be an empty array when there is no field-level detail.
- Do not include stack traces or internal exception types in the body.

### HTTP status codes (approved)

| Situation | Code |
|-----------|------|
| Success (read/update/transition) | `200` |
| Created | `201` |
| Validation / invalid request / blank keyword | `400` |
| Ticket not found | `404` |
| Invalid status transition | `409` |

---

## Resources

### Ticket representation

```json
{
  "id": 1,
  "title": "string",
  "description": "string or null",
  "priority": "string or null",
  "assignee": "string or null",
  "status": "OPEN"
}
```

### Ticket details representation

Same as ticket, plus:

```json
{
  "id": 1,
  "title": "string",
  "description": "string or null",
  "priority": "string or null",
  "assignee": "string or null",
  "status": "OPEN",
  "comments": [
    {
      "id": 1,
      "text": "string"
    }
  ]
}
```

### Comment representation

```json
{
  "id": 1,
  "text": "string"
}
```

---

## Endpoints

### 1. Create ticket

`POST /api/tickets`

**Request body:**

```json
{
  "title": "string",
  "description": "string",
  "priority": "string",
  "assignee": "string"
}
```

| Field | Rule |
|-------|------|
| `title` | Required; non-blank; max 200 |
| `description` | Optional |
| `priority` | Optional |
| `assignee` | Optional |

- Do **not** accept `status` on create; server sets `OPEN`.
- Unknown JSON properties: **ignore** (Spring default).
- **Response:** `201` + Ticket representation.
- **Errors:** `400` validation failure.

---

### 2. List tickets (optional search and status filter)

`GET /api/tickets`

**Query parameters:**

| Name | Required | Rule |
|------|----------|------|
| `q` | no | Keyword; if present must be non-blank; search title OR description (case-insensitive substring) |
| `status` | no | One of `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED` |

- If both `q` and `status` are present: **AND**.
- If neither is present: return all tickets.
- No pagination (not required).
- **Response:** `200` + JSON array of Ticket representations.
- **Errors:** `400` if `q` is blank, or `status` is not a valid status name.

---

### 3. Get ticket details

`GET /api/tickets/{id}`

- **Response:** `200` + Ticket details (including `comments`).
- **Errors:** `404` if not found.

---

### 4. Update ticket fields

`PATCH /api/tickets/{id}`

Updates only: **title**, **description**, **priority**, **assignee**.

**Request body** (all fields optional; at least one should be present — if body empty, `400`):

```json
{
  "title": "string",
  "description": "string",
  "priority": "string",
  "assignee": "string"
}
```

| Field | Rule when present |
|-------|-------------------|
| `title` | Non-blank; max 200 |
| `description` | Max 5000; empty string stored as null |
| `priority` | Max 50; empty string stored as null |
| `assignee` | Max 100; empty string stored as null |

- Do **not** accept `status` on this endpoint (`400` if `status` is provided).
- **Response:** `200` + Ticket representation.
- **Errors:** `400` validation; `404` not found.

---

### 5. Change ticket status (state transition)

`POST /api/tickets/{id}/status`

**Request body:**

```json
{
  "status": "IN_PROGRESS"
}
```

| Field | Rule |
|-------|------|
| `status` | Required; must be a valid status name; transition from current status must be an **allowed edge** in `spec/state-machine.md` |

**Allowed transitions only:**

| From | To |
|------|-----|
| `OPEN` | `IN_PROGRESS` |
| `IN_PROGRESS` | `RESOLVED` |
| `RESOLVED` | `CLOSED` |
| `OPEN` | `CANCELLED` |
| `IN_PROGRESS` | `CANCELLED` |

- Any other transition (including self-transitions and skips) → `409` with a meaningful `message`.
- **Response:** `200` + Ticket representation with updated `status`.
- **Errors:** `400` if `status` missing/invalid name; `404` if ticket missing; `409` if transition not allowed.

This dedicated endpoint resolves the requirements gap: FR4 updateable fields omit status, while acceptance still requires valid/invalid transitions.

---

### 6. Add comment

`POST /api/tickets/{id}/comments`

**Request body:**

```json
{
  "text": "string"
}
```

| Field | Rule |
|-------|------|
| `text` | Required; non-blank; max 2000 |

- **Response:** `201` + Comment representation.
- **Errors:** `400` validation; `404` if ticket not found.

---

## Not in contract (do not implement)

- `DELETE` ticket or comment  
- Authentication headers  
- Pagination parameters  
- Sort parameters  
- Bulk operations  
- Reopen endpoints beyond allowed edges above  

## Consistency

- Must stay aligned with `spec/requirements.md`, `spec/data-model.md`, and `spec/state-machine.md`.
- If a future change is needed, update this file **before** changing code.
