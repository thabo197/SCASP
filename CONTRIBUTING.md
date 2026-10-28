# Contributing

These conventions apply to every member and every stage.

## Branches

- `main` always builds and is protected. Changes reach it only through a pull request.
- Stage work: `stage/<n>-<name>`, for example `stage/1-collections`.
- Smaller work inside a stage: `feature/<n>-<short-description>`, `fix/<short-description>`, `docs/<short-description>`.
- If a stage's pull request is still waiting for review, branch the next stage from it, then retarget the pull request to `main` once the earlier one is merged.

## Commits

Format: `<type>(<scope>): <summary>`

- Types: `feat`, `fix`, `test`, `docs`, `build`, `chore`.
- Scope: the module, for example `core-domain` or `persistence-jdbc`.
- Example: `feat(core-domain): add waiting-list promotion on withdrawal`
- Commit under your own Git account and email. Make small, meaningful commits.

## Pull requests and reviews

- One pull request per stage or feature, using the template.
- A different group member must review and approve before merging. Nobody approves their own work.
- Reviewers check: it builds, tests pass, no secrets or real personal data, no duplicated business rules, meaningful names, documentation updated.
- Prefer a merge commit so individual commits stay visible. Squash only for tiny single-author fixes.

## Tags

- Stage 0 is tagged `v0.1`. Stages 1 to 10 are tagged `v1.0` to `v10.0`. The final release is `v11.0`.
- Use annotated tags on `main` after the stage's pull request is merged:
  `git tag -a v1.0 -m "Stage 1: collections core"` then `git push origin v1.0`.
- Keep every stage tag so progression can be inspected.

## Roles and rotation

| Stages | Member 1 | Member 2 | Member 3 |
|---|---|---|---|
| 0 to 3 | Architecture and Integration | Backend and Data | Client, Quality and Documentation |
| 4 to 7 | Backend and Data | Client, Quality and Documentation | Architecture and Integration |
| 8 to 11 | Client, Quality and Documentation | Architecture and Integration | Backend and Data |

A role is leadership responsibility, not exclusive ownership. All members write code.

## Rules

- No passwords, API keys, certificates or personal data in the repository. Sample data is fictional.
- Acknowledge external libraries and generated assistance in `docs/ACKNOWLEDGEMENTS.md`.
- Update `docs/contribution-log.md` in every pull request.
