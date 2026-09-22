# AI review — genuine mistakes and incorrect suggestions

**Purpose:** Satisfy the assignment process requirement to identify meaningful AI mistakes (not accept output blindly).  
**Sources:** `docs/prompt-history.md`, SpecStory history under `.specstory/history/`, and defects found during implementation/testing.  
**Rule:** Only real examples from this project. No fabricated mistakes.

---

## Example 1 — Cascade-only comment save returned null `id`

1. **What the AI suggested/generated**  
   Persist a new comment by attaching it to the ticket and saving the parent (`cascade = ALL` / `saveAndFlush` on `Ticket`), then map that comment entity to the `201` response.

2. **Why it was incorrect/risky/unnecessary**  
   With `GenerationType.IDENTITY`, the cascaded child often had no generated `id` available when the controller serialized the response, so the API returned a comment with `id: null`.

3. **Which requirement/specification it conflicted with**  
   `spec/api-contract.md` — Add comment returns `201` + Comment representation including `id`.  
   `spec/data-model.md` — Comment `id` is a required generated surrogate key.

4. **What was rejected or changed**  
   Cascade-only create path was rejected for the response-mapped entity. Implementation switched to `CommentRepository.save(comment)` so the ID is assigned before mapping.

5. **What the correct approach was**  
   Save the `Comment` through its own repository (or otherwise ensure flush/ID assignment) before returning the Comment representation.

**Evidence:** `docs/prompt-history.md` (backend implementation defect); SpecStory backend session noting null comment `id` after cascade save.

---

## Example 2 — Treat priority as an enum

1. **What the AI suggested/generated**  
   While drafting unblock specs, the model proposed defining **priority** as a closed enum of values (typical “ticket system” inventiveness).

2. **Why it was incorrect/risky/unnecessary**  
   The assignment only names the field `priority`; it does not define allowed values. An enum invents product rules and forces UI/API constraints the assignment never required.

3. **Which requirement/specification it conflicted with**  
   `spec/requirements.md` — only named fields; “No other fields, types, enums… are specified.”  
   Final `spec/data-model.md` — priority is a free-form string with “no invented enum.”

4. **What was rejected or changed**  
   Enum priority was rejected. Approved model uses optional free-form `String` with a max length only.

5. **What the correct approach was**  
   Keep priority as an optional unconstrained string (length-limited for persistence), matching named requirements without inventing a value domain.

**Evidence:** SpecStory when creating architecture/data-model/api-contract (priority enum considered, then corrected to free-form string); `docs/prompt-history.md` rejected “inventing priority enums.”

---

## Example 3 — Status change via `PATCH /api/tickets/{id}/status`

1. **What the AI suggested/generated**  
   Draft thinking used a **PATCH** dedicated status endpoint (`PATCH /api/tickets/{id}/status`) to keep status out of the field-update body.

2. **Why it was incorrect/risky/unnecessary**  
   Mixing PATCH (partial resource update) with a pure transition command is ambiguous and easy to confuse with `PATCH /api/tickets/{id}` field updates. The approved contract settled on a clear command-style **POST**.

3. **Which requirement/specification it conflicted with**  
   Approved `spec/api-contract.md` / `spec/state-machine.md` / `spec/ui-flow.md` — status changes use `POST /api/tickets/{id}/status`, not field PATCH and not a PATCH status sub-resource.

4. **What was rejected or changed**  
   PATCH-for-status was rejected before/as specs were finalized; contract and implementation use POST only. Field `PATCH` still rejects a `status` property with `400`.

5. **What the correct approach was**  
   Dedicated `POST /api/tickets/{id}/status` with `{ "status": "..." }`, separate from U4 field updates.

**Evidence:** SpecStory draft notes using PATCH for status; final approved specs and code use POST.

---

## Example 4 — Invent missing architecture / data-model / API contract in code

1. **What the AI suggested/generated**  
   When asked to implement the backend before those specs existed, a natural (risky) path would be to invent entities, REST paths, validation matrices, error JSON, and search rules in application code.

2. **Why it was incorrect/risky/unnecessary**  
   That would bake invented requirements into the product and violate SDD gates (“do not invent”; stop on ambiguity). It also conflicts with the implementation plan’s hard ordering rules.

3. **Which requirement/specification it conflicted with**  
   `spec/requirements.md` process constraints; `plan/implementation-plan.md` Tasks 2.0 / 5.0 gates; `rules/documentation.md` — specifications before implementation; user STRICT SDD rules.

4. **What was rejected or changed**  
   Implementation was **stopped**. Missing contracts were reported; inventing endpoints/entities/validation/search/error shapes was explicitly rejected until the user approved `architecture.md`, `data-model.md`, and `api-contract.md`.

5. **What the correct approach was**  
   Create/approve the three gate specs first, then implement exactly against them.

**Evidence:** `docs/prompt-history.md` (Step 2 + backend blocked pass); SpecStory “Stopped before writing backend code.”

---

## Example 5 — Treat empty README marketing copy as product requirements

1. **What the AI suggested/generated**  
   Risk of reading the empty/marketing README phrasing (“AI-assisted, spec-driven support ticket portal”) as extra product scope beyond the assignment document.

2. **Why it was incorrect/risky/unnecessary**  
   README slogans are not assignment acceptance criteria. Treating them as requirements would expand scope without evidence.

3. **Which requirement/specification it conflicted with**  
   `spec/requirements.md` — source is the assignment (`Assessments .docx`); only stated FRs and acceptance criteria.

4. **What was rejected or changed**  
   README marketing text was **not** treated as requirements when writing `spec/requirements.md`.

5. **What the correct approach was**  
   Capture only explicit assignment statements; ignore repo marketing fluff.

**Evidence:** `docs/prompt-history.md` — “AI caution (process): Do not treat empty repo README… as extra product requirements.”

---

## Example 6 — H2 on the production runtime classpath

1. **What the AI suggested/generated**  
   Maven dependency for H2 with `<scope>runtime</scope>` in the main backend POM (alongside PostgreSQL).

2. **Why it was incorrect/risky/unnecessary**  
   Architecture approves H2 **for tests**, not as an application runtime database. Shipping H2 on the production classpath is unnecessary and widens the runtime surface for no product reason.

3. **Which requirement/specification it conflicted with**  
   `spec/architecture.md` — Application database PostgreSQL; Test database H2.

4. **What was rejected or changed**  
   During final review, H2 scope was corrected to `test`.

5. **What the correct approach was**  
   Keep H2 as a test-scoped dependency only; PostgreSQL for application runtime.

**Evidence:** Initial `backend/pom.xml` used `runtime`; corrected in Step 7 final review.

---

## Example 7 — Unescaped LIKE wildcards in keyword search

1. **What the AI suggested/generated**  
   Keyword search implemented as JPQL `LIKE CONCAT('%', :keyword, '%')` without escaping `%` / `_` in the user keyword.

2. **Why it was incorrect/risky/unnecessary**  
   A keyword of `%` or `_` does not behave as a **literal substring** match; `%` matches everything. That silently changes search semantics and can surprise users / weaken tests.

3. **Which requirement/specification it conflicted with**  
   `spec/data-model.md` / `spec/api-contract.md` — case-insensitive **substring** match on title OR description (literal characters, not SQL LIKE wildcards).

4. **What was rejected or changed**  
   Final review escaped LIKE metacharacters and added `ESCAPE '\'` in repository queries, plus an integration test for `%` / `_`.

5. **What the correct approach was**  
   Escape `\`, `%`, and `_` in the keyword before binding, and declare an ESCAPE character in the LIKE predicate so search remains literal substring matching.

**Evidence:** Discovered in Step 7 specification-driven review of `TicketRepository` / `TicketService`.

---

## Count

**7** genuine AI mistakes/incorrect suggestions documented above (more than the minimum of 5). No examples were fabricated to pad the count.

## Related process notes (not counted as separate “mistakes”)

- Inventing reopen transitions, auth UI, skip rules without clarification, and coverage % targets were considered and **rejected** during Step 2 (`docs/prompt-history.md`).
- Frontend kept API paths aligned with the contract; no invented endpoints were shipped.
- Unused Vite scaffold files under `frontend/src/assets/` (and unused `App.css` if present) are dead leftovers — documented as a remaining limitation, not treated as a product defect requiring deletion in final review.
