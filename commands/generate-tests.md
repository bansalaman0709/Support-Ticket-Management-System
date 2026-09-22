# Command: generate-tests

Use this prompt only after behavior is specified. Do not implement production code as part of this command unless asked.

```
Generate tests from spec/requirements.md and rules/testing.md.

Must cover stated acceptance related to tests:
- Valid status transitions from the stated state machine work.
- Invalid transitions are rejected by the backend, including:
  CLOSED → OPEN
  RESOLVED → OPEN
  CANCELLED → OPEN
- State-machine integration tests (assignment requires these to pass).

Do not invent:
- Unspecified validation rules
- Unspecified endpoints or fields
- Unspecified UI interactions

If a test needs a detail that is not specified, stop and list the ambiguity instead of guessing.
```
