---
name: commit-and-merge
description: "Commit the current task branch, squash-merge it to main with \"Closes #N\", push main, close out the issue and reconcile blocked tasks. Use only when the user says \"commit and merge\" (or \"commit\" alone, for the first step only) in this conversation, never on your own initiative."
argument-hint: "[#N]"
---

# Commit and merge

Run this only on the user's explicit "commit and merge" for the task on the current branch. It pushes `main` and changes the issue, which cannot be taken back quietly. If the user said only "commit", do step 2 and stop.

1. **Check.** `git rev-parse --abbrev-ref HEAD` gives `T-####-<slug>`; N is its number. Every acceptance box on the issue is ticked and the Outcome is there (`glab issue view N`); if not, stop and say what is missing. The gates for the change have passed on this tree. The branch has been reviewed (`codex-review`): the stand-in approved it, and Codex reviewed the approved version, or the issue says why Codex could not.
2. **Commit on the branch.** Stage explicitly: never `git add -A`, `git commit -a` or a bare `git stash`. Never stage `.claude/settings.local.json`, `.mcp.json`, `.claude/scheduled_tasks.lock`, `.claude/worktrees/`, editor files (`.idea/`, `.settings/`, `.classpath`, `.project`, `.factorypath`), or anything under `target/`. Message: `T-####: <summary>`, a body saying what changed, and the `Co-Authored-By:` trailer the session gives.

   **The git hooks** (`scripts/install-git-hooks.sh`; `git config core.hooksPath` says `scripts/git-hooks` once installed) run Maven on each commit, so no other build may be running. If they aren't installed, install them before the first commit. A hook that fails is a failure to fix, like a gate: never `--no-verify`. pre-push refuses to push to `main` any commit that skipped the pre-commit hook.
3. **Squash-merge**, once each of #N's child tasks is closed or ready, with its boxes ticked and its Outcome (`gitlab-task`, "Finishing", lists them); add a `-m "Closes #M"` for each open child:

   ```bash
   git checkout main && git pull --ff-only
   git merge --squash T-####-<slug>
   git commit -m "T-####: <summary> (squash of T-####-<slug>)" -m "Closes #N" -m "Co-Authored-By: <the session's trailer>"
   git push origin main
   ```

   Only `main` is pushed; the task branch stays local (squashed branches are not merged in git's sense, so delete one only when the user asks).
4. **Close out the issue** and its child tasks. The push closes #N, and each child, through `Closes`; confirm each with `glab issue view N` (`state: closed`), and close it with `glab issue close N` if not. Remove the `workflow::` label from each, whichever it has: `glab issue update N --unlabel workflow::planned,workflow::in-progress,workflow::blocked`.
5. **Reconcile**: for each `workflow::blocked` issue whose blockers are all closed now, swap to `workflow::planned` and mark the line `— cleared YYYY-MM-DD` (`gitlab-task`).
6. **Report**: the squash commit's hash, that `main` is pushed, the issue closed, any task unblocked, and a one-line recommendation to clear the context before the next task (`CLAUDE.md`, "Context").
