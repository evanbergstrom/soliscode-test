---
name: gitlab-task
description: "The project's task workflow on GitLab issues — what a task is (issue #N = T-N), its workflow:: labels, filing, blocking, the acceptance checkboxes, the Outcome, child tasks, and dependency reconciliation. Use when filing, starting, updating, blocking or closing a task, or when asked what to work on next or what a T-number is."
---

# GitLab task workflow

All work is tracked as issues, and child tasks under them, in [`soliscode/soliscode-test`](https://gitlab.com/soliscode/soliscode-test/-/issues) (project ID `72689075`). The issues are the only record of what is planned, in flight, blocked or done; a document never holds a to-do list. Reach GitLab with `glab` (`-R soliscode/soliscode-test`, or from the repository, where it is the default); no GitLab MCP server is used.

**Task identity:** issue #N is task `T-N`, padded to four digits (#7 is T-0007), and the title begins with the tag: `T-0007: <title>`. Speak of tasks to the user by issue number (#7); the T-tag goes in titles, branch names and commit messages.

## Status

| Status | Representation |
|--------|----------------|
| Planned | open + `workflow::planned` |
| In progress | open + `workflow::in-progress` |
| Blocked | open + `workflow::blocked` + a `**Blocked by:** #N` line at the top of the description + a *relates to* link to each blocker. All three, never a subset. |
| Done | closed, no `workflow::` label, every acceptance checkbox ticked, an Outcome paragraph at the end |

**Labels:**
- `workflow::planned`, `workflow::in-progress`, `workflow::blocked`: the status. These come from the `soliscode` group.
- `area::build` for the build and its gates, `area::ai-dev` for the agent instructions, skills and reviewers. These are the project's own.
- `bug` (a fault in the library) is the project's own; `fix` (a bug) and `quality` are group labels this project may use.
- This project has no modules or features of its own, so no `module::` or feature labels.
- The label picker shows every group label. Never put one on an issue here unless it is listed above, and never delete or rename a group label: that changes it for every project in the group.

Swap a label with `glab issue update N --unlabel workflow::planned --label workflow::in-progress`; don't rewrite the whole set.

**What's next:** `glab issue list --label workflow::planned` (and `workflow::in-progress` for anything started).

## Filing a task

Filing writes to GitLab, so file only when the user asks for a task to be filed (except child tasks for a task's own steps; see Child tasks).

1. `glab issue create -t "<title>" -l "workflow::planned,<area or bug label>" -d "<body>"`. The body: what and why, then `Acceptance criteria:` and a `- [ ]` list. Read the IID from the URL it prints. For a long body, write it to a file in the scratchpad and post it with `glab api -X POST projects/72689075/issues -f title=... -F description=@<file> -f labels=...`.
2. Prefix the title: `glab issue update N -t "T-NNNN: <title>"`, the IID padded to four digits.
3. If it is blocked, use `workflow::blocked`, put `**Blocked by:** #M` first in the body, and link it (`glab` has no link command): `glab api -X POST projects/72689075/issues/N/links -f target_project_id=72689075 -f target_issue_iid=M`. A blocker in another project (soliscode-parent is `72688995`, soliscode-quant `88456548`) is linked with that project's ID as `target_project_id`.

## Child tasks

A step of a task worth tracking on its own (a review round, a decision, a measurement to redo) is a GitLab **Task** work item under the task's issue, not a list in a comment or a document. Work on a task may file child tasks under it for its own steps without asking; a new task, or anything in another project, waits for the user's ask. Tasks share the project's IID sequence with issues, so child #N is `T-N` too: title it `T-NNNN: <title>`, give it a `workflow::` label and the parent's `area::` labels, and write its body as a task's (`Acceptance criteria:` and a `- [ ]` list), with `Part of #<parent>.` first. `glab issue create` cannot make a Task, so use GraphQL. The ids are the Task type, `gid://gitlab/WorkItems::Type/5`, and the parent's `gid://gitlab/WorkItem/<id>`, which `workItems(iids:["<parent>"]){nodes{id}}` on the project returns:

```bash
glab api graphql -f query='mutation($t:String!,$d:String!){ workItemCreate(input:{namespacePath:"soliscode/soliscode-test", title:$t,
  workItemTypeId:"gid://gitlab/WorkItems::Type/5", descriptionWidget:{description:$d},
  hierarchyWidget:{parentId:"gid://gitlab/WorkItem/<id>"}}){ workItem{ iid } errors } }' -f t="<title>" -f d="<body>"
```

Then `glab issue update N -t ... -l ...`, `glab issue view N` and `issue_body.py` work on it as on an issue. `glab issue view` does not show a task's children; list them, with their state:

```bash
glab api graphql -f query='{ project(fullPath:"soliscode/soliscode-test"){ workItems(iids:["<parent>"]){ nodes{ widgets{
  ... on WorkItemWidgetHierarchy{ children{ nodes{ iid state title } } } } } } } }'
```

A child task is worked on its parent's branch, never one of its own, and finished with its parent (see Finishing).

## While working

- Decisions and findings worth keeping go as comments: `glab issue note N -m "..."`. Decisions that are the user's go to the user first (AskUserQuestion, recommended option first).
- Read a task with `glab issue view N --comments`.

## Finishing (done by the `commit-and-merge` skill)

- Tick every box with `python3 -I .claude/skills/gitlab-task/scripts/issue_body.py tick N`, run from the repository root (it changes only `- [ ]` to `- [x]`, code blocks included), and add the Outcome with `python3 -I .claude/skills/gitlab-task/scripts/issue_body.py outcome N <file>`: what was built, what the tests found, the gates and their results, and where the logs are. Never tick a box whose work is not done.
- **Child tasks** are finished with their parent, in the same `commit-and-merge`: list them (above); each open child has every box ticked and its Outcome, or the parent is not ready; the squash commit carries a `Closes #M` for each, beside the parent's.
- The squash commit on `main` says `Closes #N`, which closes the issue when pushed. Then remove the `workflow::` label from it and each child, whichever it has: `glab issue update N --unlabel workflow::planned,workflow::in-progress,workflow::blocked`.
- **Dependency reconciliation.** List `glab issue list --label workflow::blocked`. For each whose blockers are now all closed, swap to `workflow::planned` and annotate its line: `**Blocked by:** #M — cleared YYYY-MM-DD`.

## Ship the whole acceptance list

When the user says to execute a filed task, the deliverable is every acceptance checkbox in its body.

- Don't split it into sub-scopes, file follow-up or carryover tasks for listed work, call listed work "out of scope", or close the task with a box unticked.
- If the list looks too large for one session, say so **before starting**, and ask whether to split it into filed tasks, descope it, or attempt it all. Don't quote effort estimates; say what concretely makes it large.
- A regression the work exposes is part of the task. The task doesn't close until it is fixed; the fix is not a new task.
