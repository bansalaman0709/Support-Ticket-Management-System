# Command: review-spec

Use this prompt against `spec/` (especially `requirements.md`) and related steering files.

```
Review the specifications for spec-driven development.

Check:
- Requirements contain only what the assignment (and any later approved source) explicitly states.
- Implementation decisions are not mixed into requirements.
- State machine text matches the assignment (allowed transitions and stated invalid examples).
- Acceptance criteria are complete vs the assignment’s “solution is complete when” list.
- Ambiguities are listed instead of assumed.
- Later spec files (architecture, data-model, api-contract, ui-flow, test-strategy) either do not exist yet or do not invent unstated product behavior.

Report gaps, contradictions, and invented content. Do not fill gaps with new features.
```
