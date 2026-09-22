# Command: review-code

Use this prompt against the current diff or named files. Do not implement unless asked.

```
Review the code against spec/requirements.md and the rules in rules/.

Check:
- Only specified ticket behaviors are implemented (no invented features).
- Backend validates input as required (do not invent extra rules; flag unspecified checks).
- Status state machine is enforced on the backend; invalid transitions are rejected.
- REST API is used as required; do not require unspecified HTTP conventions.
- Data is persisted in a database and would survive restart.
- No secrets are committed.
- Java 21 / Spring Boot / PostgreSQL or H2 / frontend stack match stated constraints where code exists.

Report:
- Defects vs stated requirements
- Possible AI mistakes or over-invention
- Ambiguities still unresolved
- What was not reviewed (missing specs or missing code)

Do not rewrite the application unless explicitly asked.
```
