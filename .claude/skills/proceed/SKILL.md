---
name: proceed
description: "Work a filed task from start to ready-to-merge — read the issue, branch, mark it in progress, read and write the design and review it, implement every acceptance criterion, run the gates, review the branch, tick the boxes, report. Use when the user says \"proceed with #N\" (or T-####), \"do #N\" or \"finish #N\". Stops before committing; committing and merging is the commit-and-merge skill, on the user's word."
argument-hint: "<#N or T-####>"
---

# Proceed with $ARGUMENTS

The user's "proceed with #N" is the go-ahead for this whole loop, including the GitLab writes in it: the task's label swap, comments on its issue, and child tasks under it for its own steps (`gitlab-task`, "Child tasks"). Anything else in GitLab, and any commit, push or merge, waits for the user. The task procedure is the `gitlab-task` skill; read it if it is not loaded.

1. **Read the task**: `glab issue view N --comments`. Note every acceptance criterion; that list is the deliverable. The title and opening paragraph say what the task is; where they and a later section disagree, ask. If the list is too large for one session, say so now and ask (split, descope, or attempt it all) before starting. If it is `workflow::blocked`, say by what, and ask before working around the blocker.
2. **Decisions first.** If the task names decisions that are the user's, ask them now (AskUserQuestion, recommended option first) and record the answers on the issue.
3. **Branch** before the first edit: `git checkout main && git pull --ff-only`, then `git checkout -b T-####-<kebab-slug>`. A child task (its body starts `Part of #P`) is worked on #P's branch instead. Swap the label, from whichever it has (a blocked task the user said to start included): `glab issue update N --unlabel workflow::planned,workflow::blocked --label workflow::in-progress`.
4. **Read the design** the task touches: `docs/design/system/constitution.md` and `architecture.md` before writing code, `docs/design/reference/java-conventions.md`, and `build-and-test.md` for a change to the build or the tests. Where the docs and the code disagree, the code says what exists; fix the doc in the task, or say why not.
5. **Design review.** Once the task's design is written (under `docs/design/`, or on the issue for a small task), and before its code: the `codex-review` skill's design review, the `design-reviewer` agent until it approves, then Codex once. A task with no design change skips it; say so in the report.
6. **Implement.** For a bug, reproduce it first with a failing test and keep the test. A regression the work exposes is fixed in this task. A contract follows the JDK's documentation of the interface it tests, cited where it reads it strictly or loosely; a new contract or break is checked by `ContractCoverageTest` (the contract passes against a JDK class and fails for each break). Keep the docs that describe what changed in step, marking what is built and what is only designed.
7. **Gates**: the `gates` skill for what changed. All green, zero new findings.
8. **Code review**: the `codex-review` skill's code review, the `code-reviewer` agent until it approves, then Codex once on the approved branch, again only after changes in behaviour. Re-run the gates a fix touches.
9. **Tick and record**: tick the issue's boxes and add its Outcome (`gitlab-task`, "Finishing") only once every criterion is met. Comments on the issue for decisions and findings worth keeping.
10. **Report** to the user, outcome first: what was built, what the tests found, the gate results, the reviews and their verdicts, anything unusual or routed around, and that it is ready for "commit and merge". Don't commit unless asked. If the task is finished, recommend clearing the context in one line (`CLAUDE.md`, "Context").
