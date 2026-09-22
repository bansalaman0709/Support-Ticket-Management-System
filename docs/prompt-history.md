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
