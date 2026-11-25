# Instructions for Codex and other agents

This project's instructions are written for Claude Code and apply to every agent. Read them before reviewing or changing anything:

- [`CLAUDE.md`](CLAUDE.md): the build and the gates, the design constraints, the conventions, and the approval list.
- `~/.claude/plugins/marketplaces/soliscode-0.1.17/claude-plugin/rules.md`: the rules every SolisCode project shares (tasks, GitLab issues, where issue #N is task T-N; branches; reviews; commits), from the Claude Code plugin `soliscode`, pinned to soliscode-parent 0.1.17 in `.claude/settings.json`. Where `CLAUDE.md` departs from them, `CLAUDE.md` wins.
- `~/.claude/plugins/marketplaces/soliscode-0.1.17/claude-plugin/review-standard.md`: how a change is reviewed, by you and by the `soliscode:code-reviewer` and `soliscode:design-reviewer` agents alike (`~/.claude/plugins/marketplaces/soliscode-0.1.17/claude-plugin/agents/` has their briefs). Its second half is the "Review focus" of [`.claude/soliscode-project.md`](.claude/soliscode-project.md): what to read first, what to look for in this project, and the read-only commands you may run.
- `~/.claude/plugins/marketplaces/soliscode-0.1.17/claude-plugin/skills/` (`proceed`, `commit-and-merge`, `gitlab-task`, `codex-review`, `docs`) and [`.claude/skills/`](.claude/skills/) (`gates`, `release`): the procedures.
- [`docs/design/`](docs/design/): the system design and the reference docs.

When reviewing, verify each finding against the code and cite `path:line`. Give each a severity (Blocker, Major, Minor, Nit) and end with a verdict (APPROVE or CHANGES REQUESTED). A wrong finding costs more than a missed nit. A review reports: change nothing and write nothing to GitLab unless the user asks for a fix.

The `LSP` tool CLAUDE.md mentions is Claude Code's. You have no semantic Java tooling: find every use of a symbol with a text search across `src`, main and test, and get compile errors from `mvn -B -Djacoco.check.skip=true -DskipTests install`.
