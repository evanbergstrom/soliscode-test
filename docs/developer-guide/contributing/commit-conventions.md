# Commit conventions

Each task lands on `main` as **one squash commit**. A commit on a task branch is `T-NNNN: <summary>`, with a body that says what changed, but only the squash commit is kept.

## The squash commit

```text
T-NNNN: <summary> (squash of T-NNNN-<slug>)

<optional: the CLA check's lines, for a merge request from outside>

Closes #N

Co-Authored-By: <name> <email>
```

- `T-NNNN` is the issue number, padded to four digits: issue #34 is `T-0034`.
- The summary says what the task did, in the imperative mood, usually as the issue's title does. The branch's name follows it in brackets.
- `Closes #N` closes the issue when the commit reaches `main`.
- A `Co-Authored-By:` line credits each co-author, including an AI assistant that wrote part of the change. Every commit on the branch ends with it too.

The `soliscode:commit-and-merge` skill, in Claude Code, makes this commit, squashes the branch into `main` and pushes it. A merge request from outside is merged the same way, not with GitLab's merge button.

## Example

```text
T-0042: Add a timeout to the retry policy (squash of T-0042-retry-timeout)

Closes #42

Co-Authored-By: Claude <noreply@anthropic.com>
```

## Branches

A task's branch is `T-NNNN-<slug>`, such as `T-0042-retry-timeout`. A branch is never reused: further work on a closed task gets a new branch.

## Staging

Stage each file by name (`git add <path>`), not `git add -A` or `git add .`, so that nothing you did not mean to commit goes in.

## The CLA

Every outside contribution needs a signed [SolisCode Individual Contributor License Agreement](../../../CLA.md) before it is merged. You sign once, in your first merge request:

1. Add your row to the table in [CLA-SIGNATURES.md](../../../CLA-SIGNATURES.md): your name, the email you commit with, your GitLab username, the CLA's version, the date, and your merge request's number.
2. Commit that change on its own, from the email in your row, with this message, word for word for the version you sign:

   ```text
   I have read and agree to the SolisCode Individual CLA, version <N>.
   ```

3. Tick the CLA box in the merge request's description.

The merge checks the signature before the squash. The copyright holder signs nothing: their commit emails are in the first table of `CLA-SIGNATURES.md`.

## Enforcement

The squash commit's format is written by `soliscode:commit-and-merge`. No hook checks a commit message. The pre-commit hook checks the build, and the pre-push hook refuses a push to `main` of a commit that skipped it.

## See also

- [Contribution workflow](workflow.md)
- [Release process](../maintainers/release-process.md)
