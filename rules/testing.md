# Testing guidelines

Reusable AI steering for this project. Prefer `spec/requirements.md` over invention.

## Stated constraints

- Spec-driven workflow includes **Testing** as a distinct stage after implementation.
- **State-machine integration tests** must pass (core acceptance criterion).
- Backend must reject invalid status transitions; tests should cover that stated behavior.
- Valid status transitions must work; tests should cover the transitions listed in `spec/requirements.md`.
- Backend validation is required; tests must not assume unspecified validation rules.

## How to use this file

- Generate tests from specified behavior only.
- Required coverage that *is* stated: state-machine integration tests (allowed transitions and rejected invalid transitions, including the three invalid examples).
- Do not invent extra test layers, tools, or cases as if they were requirements.
- If coverage beyond the assignment is proposed, label it as optional and not required.

## Explicitly not decided here

The assignment does not specify unit vs API vs UI tests (except state-machine **integration** tests), test framework, coverage %, or test data. Do not treat this file as a source of those decisions.
