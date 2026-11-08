# Instructions for Codex and other agents

This project's instructions are written for Claude Code and apply to every agent. Read them before reviewing or changing anything:

- [`CLAUDE.md`](CLAUDE.md): the build and the gates, the design constraints, the conventions, and the task workflow (GitLab issues; issue #N is task T-N).
- [`.claude/review-standard.md`](.claude/review-standard.md): how a change is reviewed here, by you and by the `code-reviewer` and `design-reviewer` agents alike. It says what to review against, what to look for, and how to report.
- [`.claude/skills/`](.claude/skills/): the procedures (`proceed`, `commit-and-merge`, `gates`, `gitlab-task`, `codex-review`, `release`).
- [`docs/design/`](docs/design/): the system design and the reference docs.

When reviewing, verify each finding against the code and cite `path:line`. Give each a severity (Blocker, Major, Minor, Nit) and end with a verdict (APPROVE or CHANGES REQUESTED). A wrong finding costs more than a missed nit. A review reports: change nothing and write nothing to GitLab unless the user asks for a fix.

The `LSP` tool CLAUDE.md mentions is Claude Code's. You have no semantic Java tooling: find every use of a symbol with a text search across `src`, main and test, and get compile errors from `mvn -B -Djacoco.check.skip=true -DskipTests install`.
