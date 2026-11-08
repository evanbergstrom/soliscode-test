---
name: codex-review
description: Review a task's design, or its branch before merging, in two stages to one standard (.claude/review-standard.md) — the stand-in (design-reviewer or code-reviewer agent, Opus at high effort) until it approves, then Codex once on the approved version, again only after changes in behaviour; verify each finding and post the rounds to the task's issue. Use for every task's design review before its code is written, and every branch review before "commit and merge".
argument-hint: "design | code  [#N]"
---

# Review: the stand-in first, then Codex

Every design and every branch gets this review. Codex's usage window is the scarce resource, so it is spent once, on a version the stand-in has already approved, where it finds what the stand-in missed rather than what any reviewer would.

The two reviews:

| Review | When | Stand-in | Codex |
|--------|------|----------|-------|
| **Design** | Once the design documents for a task are written (`docs/design/`), before its code | `design-reviewer` | `codex exec` with the design reviewer's brief |
| **Code** | Once the work is done and the gates pass, before "commit and merge" | `code-reviewer` | `codex review` |

Both hold `.claude/review-standard.md`; Codex reaches it, `CLAUDE.md` and the rules through `AGENTS.md`.

## Stage 1: the stand-in, until it approves

Run the agent with `effort: "high"` (the agents are Opus at high effort; pass it anyway). High, not max: max-effort rounds use the token limits too fast (the user's decision).

**The first round's brief** is tight, so that the reviewer does not re-derive what the task has settled. It gives:
- the task's issue number and its acceptance criteria;
- the cause and the fix, in a few lines;
- the change set (`git diff main` and the untracked files for code; the files under `docs/design/` for a design), the files that matter in it, and the risks to check hard;
- what the change leaves out on purpose, and why;
- which gates and tests are already green;
- while another build is going, that it must not run Maven.

Verify every finding against the code or the documents before acting: a reviewer can be wrong. Fix it, or answer it with the evidence (the line, the test or the published source that disproves it). Re-run the affected gates.

**Later rounds resume the same reviewer** (SendMessage to its id), never a new one: a new agent re-reads the whole branch. Point it at what changed since its last review (`git diff <reviewed>..HEAD`, or the files changed since, for uncommitted work), with what changed for each finding. Repeat until it returns APPROVE with every finding fixed or answered.

**A documentation-only follow-up gets no round of its own**: a wording fix, a comment, or a stale paragraph the reviewer named goes into the next round. If nothing else changed, there is no next round.

Post each round, findings and resolutions, to the task's issue (`glab issue note N`).

A finding outside the task is filed as a task of its own (`gitlab-task`) and linked, on the user's ask, or where the task's acceptance list asks for it.

## Stage 2: Codex, once, on the approved version

Never add `--dangerously-bypass-approvals-and-sandbox` or a sandbox override: a review needs read access only, and for that reason the project settings pre-approve no `codex` command. Close standard input (`< /dev/null`), or Codex waits on it. Run it in the background (4 to 15 minutes) and wait for the notification; don't poll.

**Editor files stay out of it.** Codex reads the working tree, so first set aside any file an editor or language server rewrites (`.factorypath`, `.settings/`, `.classpath`, `.project`, `.idea/`): a tracked one saved as a patch in the scratchpad (`git diff -- <files> > <scratchpad>/editor-files.patch`) and restored with `git checkout -- <files>`, an untracked one moved to the scratchpad. Put them back when the review is done (`git apply`, or move them back).

**Code review.** Codex reviews committed changes against a base; uncommitted work with `--uncommitted`. Commit on the branch only if the user has said to.

```bash
codex review --base main < /dev/null \
  > "<scratchpad>/codex-review-<branch>.md" 2> "<scratchpad>/codex-review-<branch>.log"
```

Some Codex versions take no prompt with `--base` or `--uncommitted` ("cannot be used with '[PROMPT]'"), so the review standard reaches it through `AGENTS.md` alone. Its findings, and a usage-limit error, are in the transcript (standard error) as well as on standard output: read both.

**Design review.**

```bash
codex exec "Review the design change in <files> for task #N as the brief in .claude/agents/design-reviewer.md says, and end with a verdict." \
  < /dev/null > "<scratchpad>/codex-design-<branch>.md" 2> "<scratchpad>/codex-design-<branch>.log"
```

Standard output holds the review; standard error is the transcript, needed only if the run failed. Verify each finding as in stage 1, fix or answer it, and re-run the affected gates.

**Run Codex again only if its findings led to changes in behaviour** (code or design, not a test, a comment or wording). Run the stand-in on those changes first, until it approves, so that Codex again starts from an approved version.

## If Codex is at its limit

If Codex can't run (its usage limit, not logged in, a crash), the stand-in's APPROVE is the review, and it is enough to merge. Say so in the issue note, with the reason Codex could not run.

## Report

Post to the task's issue, and tell the user:
- each reviewer's rounds: the stand-in's count, and Codex's version and model from the transcript's header;
- each finding with its resolution;
- the verdict.
