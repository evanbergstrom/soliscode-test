# Release process

## Versioning

Before 1.0.0, a release is `0.1.N`: one past the highest `v0.1.*` tag. `main` is always `0.1-SNAPSHOT`. The POM's version is `${revision}`, and a release sets it with `-Drevision`. [RELEASES.md](../../../RELEASES.md) holds the version scheme and says what each release changed. A change to public API that breaks callers is the maintainer's decision, and is recorded there.

The old `org.soliscode:soliscode-test` releases, `1.0.0` to `1.0.14`, are not part of this scheme: their tags (`v1.0.*`) stay, and are not counted.

Nothing is deployed to a remote repository before 1.0.0. A release is installed into the local Maven repository of the machine that made it, and tagged, and others build it from its release commit ([Installation](../../user-guide/getting-started/installation.md)).

## When to release

A release is made only when the maintainer asks for one, with `/release` in Claude Code (the project's `release` skill, `.claude/skills/release/SKILL.md`), or when a task's acceptance list asks for one. Never just because a task merged. A release covers every task merged since the previous tag; with none, there is nothing to release.

## Release flow

```mermaid
flowchart LR
    A[Clean main] --> B[scripts/verify.sh]
    B --> C[release.sh]
    C --> D[Release commit and tag v0.1.N]
    D --> E[Push main]
```

## Checklist

### Prepare

- [ ] You are on `main`, with a clean working copy (`git status --porcelain` prints nothing), and no other build running. Save files an editor changed (`.settings/`, `.classpath`, `.project`, `.factorypath`) as a patch, restore them, and reapply the patch after the release.
- [ ] The git hooks are installed (`git config core.hooksPath` prints `scripts/git-hooks`): the release commit runs the pre-commit hook.
- [ ] `scripts/verify.sh` is green for this tree. `release.sh` refuses a tree without a green record. Check for one with `ls "$(git rev-parse --git-common-dir)/soliscode/verified/$(git rev-parse 'HEAD^{tree}')"`. A task branch's record counts when the squash merge's tree is the branch's; otherwise run `scripts/verify.sh` once on `main`.
- [ ] The release's changes are written as Markdown bullets, without a heading, in a file **outside** the repository. Start from the bullets under `## Unreleased` in `RELEASES.md`, grouped by task, and write "None." if nothing changes for a user.
- [ ] [Upgrading](../../user-guide/upgrading.md) says what a user must do for each breaking change.

### Release

- [ ] Run:

  ```sh
  scripts/release/release.sh --changes <file> --trailer "Co-Authored-By: …"
  ```

  `--trailer` is optional, and may be given more than once. The script picks the version, builds and installs it from clean, writes its entry in `RELEASES.md` (moving the `## Unreleased` bullets into it), commits `Release 0.1.N`, and makes the annotated tag `v0.1.N`. The tag is not signed: no SolisCode release is signed before 1.0.0. It pushes nothing. [build-and-test.md](../../design/reference/build-and-test.md#releasesh), "`release.sh`", says what it does at each step, and what it does when a step fails.
- [ ] The release commit reaches GitLab with the next push of `main` (`git push origin main`). The tag stays local unless the maintainer asks for it to be pushed: `git push origin v0.1.N`.

### After the release

- [ ] Check that `## Unreleased` in `RELEASES.md` is empty again, and the new entry is right.
- [ ] Tell the libraries that test with SolisCode Test, soliscode-common and soliscode-quant, that the release is out.

## No release branches, and no rollback

There are no release branches and no back-ported fixes. A fault in a release is fixed on `main`, and ships in the next release. A release is never withdrawn: its tag stays. There is no signing, SBOM or provenance step before 1.0.0.

## See also

- [CI](ci-cd.md)
- [RELEASES.md](../../../RELEASES.md)
- [Moving to a new release](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.14/docs/guide/05-new-release.md), in the parent's guide, for moving `<parent>`
