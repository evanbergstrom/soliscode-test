---
name: release
description: Make a local release of soliscode-test (0.1.N) from main — installed in the local Maven repository, recorded in RELEASES.md and tagged, not pushed. Only when the user runs /release, or a task's acceptance list asks for one.
disable-model-invocation: true
---

# Release

A release is made only when the user runs `/release`, or a task's acceptance list asks for one. Merging a task does not make one. Releases stay local: don't push the tag unless the user asks.

**Built.** The release script, `scripts/release/release.sh`, and the `${revision}` version it sets were built by #9 and #11. `docs/design/reference/build-and-test.md`, "Releases", says what the script does, step by step, and what it does when a step fails.

The version scheme is in `RELEASES.md`: `main` is `0.1-SNAPSHOT`; a release is `0.1.N`, one past the highest `v0.1.*` tag, tagged `v0.1.N`. The old `v1.0.*` tags, of `org.soliscode:soliscode-test`, are not counted. A release covers every task merged since the previous tag.

## Steps

1. **Preconditions.**
   - Be on `main` with a clean working copy, as the script requires: `git status --porcelain` prints nothing (no change to a tracked file, no untracked file `.gitignore` does not exclude). If an editor or the Java language server has modified files (`.settings/`, `.classpath`, `.project`, `.factorypath`), or anything else is uncommitted, save it as a patch in the scratchpad, restore it, and reapply the patch at the end.
   - The git hooks are installed (`git config core.hooksPath` says `scripts/git-hooks`), and no other Maven build is running: the script builds, and its commit runs the pre-commit hook.
   - There must be at least one task merged since the last tag (`git log --first-parent --oneline $(git describe --tags --abbrev=0 --match 'v0.1.*')..HEAD`, or all of `main` before the first release). If there is none, say so and stop.
2. **Build record.** `release.sh` needs a green full verify on record for HEAD's tree: `scripts/verify.sh` writes it, in `$(git rev-parse --git-common-dir)/soliscode/verified/<tree>`. Check with `ls "$(git rev-parse --git-common-dir)/soliscode/verified/$(git rev-parse 'HEAD^{tree}')"`. A task branch's run counts when the squash merge's tree is identical to the branch's. If there is no record, run `scripts/verify.sh` once on `main`, in the background, and wait for it. If it is not green, stop and report the failures.
3. **Changes.** Write what a user of the library sees differently to a file in the scratchpad, as Markdown bullets in the entry format's style, with no heading (the script adds `### Changes`): new and changed public types and methods (contracts, breaks, assertions, providers), changed behaviour, removed or deprecated API, new dependencies. Group them by task, and write "None." if nothing changes. If the user gave notes with `/release`, use those. If `RELEASES.md` has an `## Unreleased` section, start from its bullets (the tasks wrote them as they merged); `release.sh` puts the release's entry in place of that section's contents, and leaves an empty `## Unreleased` above it.
4. **Release:** `scripts/release/release.sh --changes <file> --trailer "<the Co-Authored-By: line the session gives>"`, so the release commit carries the trailer every commit here ends with.
5. **Report** the version, the tag, the release commit, the tasks it covers, and the summary of changes. The release commit is on local `main`. It reaches `origin` with the next push of `main`; the tag stays local unless the user asks for it to be pushed.
