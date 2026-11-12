# CLAUDE.md

soliscode-test: a Java library (`com.soliscode:soliscode-test`) for JUnit 6 with three goals (constitution, section 3.1): test methods JUnit lacks, test suites for the JDK's interfaces (every collection interface, then others where a suite is useful, in order of how commonly classes implement them), and thread-safety testing. It holds contracts (interfaces of default test methods for `Collection`, `List`, `Set`, `Map`, `Queue`, `Deque`, their sequenced forms, `SortedSet`, `NavigableSet`, `BlockingQueue`, `BlockingDeque`, `TransferQueue`, `SortedMap`, `NavigableMap`, `ConcurrentMap`, `ConcurrentNavigableMap` (#36), `Iterable`, `Iterator` and `ListIterator` (#37), weakly consistent iterators (#55), `Spliterator` and its primitive kinds (#38), `Object` and `Serializable`; the other chosen interfaces are #39–#48 and #50), a breakable implementation of each interface whose faults are switched on by `Break` constants, providers of instances and elements, assertions, and concurrency-test support. soliscode-common and soliscode-quant test with it (the split of the quant libraries, soliscode/soliscode-quant#6). It builds on the shared parent `com.soliscode:soliscode-parent` (#9), and is developed with Claude Code, with Codex reviewing; [`AGENTS.md`](AGENTS.md) points Codex at this file.

Built: the parent and its gates (#9; at 0.1.2, #14), the contracts and breakables (#1), quant-test's fixes (#2), this file's structure, the scripts and git hooks (#11), Checkstyle passing with every test method in JUnit's naming (#10). **Don't describe a gate or anything else as existing without checking the code**: each document marks its parts Built, Partly built or Designed, and this file does the same.

## Where things are

| Path | What |
|------|------|
| [`docs/design/system/constitution.md`](docs/design/system/constitution.md) | What the project is, its goals, scope, quality requirements, gates and anti-patterns. **Read before writing code**, with `architecture.md`. |
| [`docs/design/system/architecture.md`](docs/design/system/architecture.md) | The packages, what each may depend on, threading, nullness |
| [`docs/design/reference/`](docs/design/reference/) | `java-conventions.md` (public API, naming, nullness, suppressions, Javadoc, imports, tests), `build-and-test.md` (commands, the gates, test tags, contract coverage, releases, the git hooks), `dependency-management.md` |
| [`RELEASES.md`](RELEASES.md) | The version scheme, and each release's entry; `## Unreleased` collects what tasks change for users |
| `src/main/java/com/soliscode/test/` | The library: `contract/`, `breakable/`, `provider/`, `assertions/`, `safety/`, `interfaces/`, `annotations/`, `util/` |
| `src/test/java/com/soliscode/test/` | Its tests, among them `contract/coverage/ContractCoverageTest` (each contract passes against a JDK class and fails for each break) |

When the docs and the code disagree, the code says what exists; fix the doc in the task that finds it, or say why not.

## Claude Code setup

- **Skills** (`.claude/skills/`):
  - `proceed`: a task from start to ready-to-merge.
  - `commit-and-merge`.
  - `gitlab-task`: the task workflow in full.
  - `gates`.
  - `codex-review`.
  - `release`: only on the user's `/release`, or where a task's acceptance list asks for a release.
- **Agents** (`.claude/agents/`): `design-reviewer` and `code-reviewer`. Both are read-only, run on Opus at high effort, and review to [`.claude/review-standard.md`](.claude/review-standard.md), the standard Codex reviews to as well.
- **Reviews** (the `codex-review` skill). Every task's design is reviewed before its code, and every branch before "commit and merge".
  - The stand-in agent goes first, and runs again until it approves.
  - Codex then reviews the approved version once. It reviews again only if its findings lead to changes in behaviour, which the stand-in checks first.
  - If Codex is at its limit, the stand-in's approval is enough to merge.
  - To spend fewer tokens: the first round gets a tight brief, later rounds resume the same reviewer on what changed since, and a documentation-only fix waits for the next round.
  - Run the stand-ins at effort high, not max: max-effort rounds use the usage limits too fast.
- **Hooks** (`.claude/hooks/`, set in `.claude/settings.json`): edits under `target/` and `generated-sources/` and to `.flattened-pom.xml` are refused, and so are shell commands that name both `.m2` and `settings` (in any order, with any command), print the effective settings, or refer to `~/.m2` itself or to anything in it outside `repository/`: `~/.m2/settings.xml` must not be printed, changed or removed. Commands on `~/.m2/repository/...` pass. A command that needs to mention both for another reason is refused too; rephrase it, or use the Edit tool for a file that mentions them.
- **Settings:** `.claude/settings.json` holds only the schema, the hooks and `deny` rules. Personal allows go in `.claude/settings.local.json`. It is git-ignored, with `.claude/scheduled_tasks.lock`, `.claude/worktrees/` and `.mcp.json`.
- **Never** print or change `~/.m2/settings.xml`: it holds credentials, and Maven reads it without being told to. Never commit `.claude/settings.local.json` or `.mcp.json`.

## Tasks (GitLab issues)

Tasks are issues in [`soliscode/soliscode-test`](https://gitlab.com/soliscode/soliscode-test/-/issues) (project ID `72689075`). GitLab is reached with `glab` only, not an MCP server, and a task lands on `main` as one squash commit.
- **Numbering.** Issue #N is task `T-N`, padded to four digits (#7 is T-0007). Call tasks by issue number (#7) when talking to the user; the T-tag goes in titles, branch names and commit messages.
- **Status.** Status is a `workflow::planned`, `workflow::in-progress` or `workflow::blocked` label on an open issue; done is closed. The `gitlab-task` skill has the procedure and the labels.

The rules:
- **Commit only when the user asks.** "Proceed with #N" means the `proceed` skill: do the task and stop before committing. "Commit and merge" means the `commit-and-merge` skill.
- **One branch per task**, `T-####-<kebab-slug>` off `main`, made before the first edit, design-only tasks included, never reused. Its child tasks are done on it too. Only `main` is pushed (`git push origin main`).
- **Ship the whole acceptance list.** Don't split a filed task, defer listed work to a follow-up, call it out of scope, or close with a box unticked. If it looks too large for one session, say so before starting, say what makes it large, and ask. Don't quote effort estimates. A regression the work exposes is fixed in the task.
- **The task's title and opening paragraph say what it is.** If a later section seems to say otherwise, ask.
- **A blocked task** (`workflow::blocked`) is not started without the user's word. Say what blocks it.
- **Decisions that are the user's go to the user**, with a recommended option first. That includes a new compile-scope dependency, a change to public API that breaks callers once 0.1.1 is released, a suppression of a kind `java-conventions.md` does not list, and a gate rule that doesn't fit the code. Before 0.1.1, public API may break where the task calls for it, each break recorded in `RELEASES.md`.
- **GitLab work items are the only work tracker.** A task is an issue. A step of a task worth tracking on its own (a review round, a decision, a measurement to redo) is a child task under it, never a list in a document. Work on a task may file child tasks under it for its own steps without asking; a new task waits for the user's ask, and so does anything in another project.

## Context

Recommend clearing the context (`/clear`, or a new session) in one line of the closing report; never clear it yourself. The points:
- after a task merges and its issue is closed, before the next task starts (the issue, this file, the skills and the docs carry what the next task needs);
- after a release;
- before a task in an unrelated area, even mid-session;
- when the session has been compacted and a new task is about to start;
- never in the middle of a task: its review rounds, its test runs and its merge belong to one context.

## Build and test

All commands run from the repository root. Detail and pitfalls are in [`build-and-test.md`](docs/design/reference/build-and-test.md); the `gates` skill says which to run. **Never run two Maven builds at once**: they share `target/` and `~/.m2`.

Built (#9): Java 25, from the JDK on the `PATH`; Maven 3.9 or later. The enforcer refuses anything older. The parent, `com.soliscode:soliscode-parent:0.1.2` (#14), comes from `~/.m2`.

```bash
mvn -B -Djacoco.check.skip=true -DskipTests install   # the install gate: enforcer, licences, Checkstyle, compile with ErrorProne and NullAway, Javadoc
scripts/verify.sh                                     # the full verify from clean (every test, the coverage check), recorded for release.sh
mvn -B verify                                         # the same build, unrecorded
mvn -B -Dtest='Class#method' test                     # one test
mvn -B -Pall-tests verify                             # every test, the extremely-slow ones included
scripts/install-git-hooks.sh                          # once per clone: the pre-commit and pre-push hooks
```

A test failure is the branch's to fix or report. A full verify takes about 2½ minutes; most of it is the thread-safety contracts and `ContractCoverageTest`.

The git hooks (`scripts/git-hooks/`) compile the main and test sources when a commit changes Java or a build file, and refuse a push to `main` of a commit that skipped them (`--no-verify`). They start Maven, so don't commit while a background build runs.

Measure performance with `-Djacoco.skip=true`: the coverage agent slows the tests.

## Static-analysis gates

The parent's gates are **gates, not advisories**: a finding fails the task like a compile error, and "the tests pass" is no substitute.

| Gate | State |
|------|-------|
| Checkstyle (the parent's `soliscode/checkstyle.xml`, severity error, main and test sources, at `validate`) | Built, #10 |
| Javadoc (`doclint` all, `failOnWarnings`, classic and Markdown `///` comments, main sources, at `verify`) | Built, #9 |
| ErrorProne and NullAway (`error-prone` profile, the root `.errorprone`, main sources, at `compile`; NullAway at ERROR, ErrorProne's warnings fatal through `failOnWarning`), JSpecify `@NullMarked` packages | Built, #9 |
| JaCoCo `check` (`jacoco.minimum.coverage` 0.70, line coverage, at `verify`), Maven enforcer (Java 25, Maven 3.9, dependency convergence, no duplicate classes, no `system` scope, at `validate`) | Built, #9, #14 |
| Licences (every dependency, in any scope, under an OSI-approved licence; the `license-main` and `license-test` profiles, at `validate`) | Built, #14 |

As each is built, the task that builds it marks it Built in every place that names its issue as Designed or Partly built: `grep -rn '#<N>' CLAUDE.md AGENTS.md RELEASES.md docs .claude` lists them. Before calling Java work done, run every gate that is built and confirm zero new findings. ErrorProne findings print with an `errorprone.info` link: count them with `grep -c 'errorprone.info' <log>`.

Never use the following without the user's approval in the current session (approval on another task doesn't carry over):
- `-Dcheckstyle.skip=true`, `-Dmaven.javadoc.skip=true`, `-Denforcer.skip=true`
- `-P '!error-prone'`, `-P '!checkstyle-main'`, `-P '!checkstyle-test'`, `-P '!license-main'`, `-P '!license-test'` or `-P '!coverage'`, or an ErrorProne option that turns a check off or demotes it (`-XepAllErrorsAsWarnings`, `-Xep:<Check>:WARN|OFF`).
- `-DskipTests` or `-Djacoco.skip=true` in a full verify (they leave the coverage check nothing to check); `-DskipTests` is fine in the install gate
- `-Dmaven.test.skip=true`, and `-Dmaven.test.failure.ignore=true` in a gate
- `-Djacoco.haltOnFailure=false`, Surefire's `-DforkCount=0`, a command-line `-DargLine`, the enforcer's `-Denforcer.rules`
- a `-D` in `MAVEN_OPTS`, `JAVA_TOOL_OPTIONS`, `_JAVA_OPTIONS` or `JDK_JAVA_OPTIONS` (verify.sh and release.sh refuse one)
- a property, `-D` or plugin configuration that narrows a gate (Checkstyle's `includes`, `excludes`, `suppressionsLocation`, `violationIgnore`; the enforcer's `rulesToSkip`; Javadoc's `excludePackageNames`; the licence plugin's `includedTypes`, `missingLicensesFileArtifact`, or a licence list or exception of this project's own), a plugin of this project's own that writes under `target/license/`, and a `system`-scope dependency
- a `@SuppressWarnings` for a check, except as `java-conventions.md` ("Suppressions") allows: code that does on purpose what the check warns against, one finding, the smallest declaration, a one-line reason (the user's decision on #9). A workaround that hides a finding instead is not allowed.

If a gate fires on code you didn't touch, pre-existing or not: stop and tell the user the gate, file and count, and offer:
- (a) fix in line;
- (b) a filed clean-up task, the default recommendation;
- (c) a narrow time-boxed suppression, which must come with a filed task to remove it.

## Java tooling

The `LSP` tool, if a Java language server is installed, is reliable for one file: use `documentSymbol` to skim a file's structure, and `hover` and `goToDefinition`. Its cross-file answers (`findReferences`, `workspaceSymbol`, `incomingCalls`) are a lower bound, because a language server that has not imported the Maven build finds only what it has happened to open.

**Before renaming or removing a non-private symbol, find its uses with a text search across `src`, main and test** (`grep -rn '<Name>' src`), and use `findReferences` only to add to them. Compile with Maven to find what broke.

## Design constraints

- **The contracts follow the JDK's documentation** of the interface they test, and say where they read it more strictly or more loosely.
- **A contract can fail.** Each is run against a correct JDK implementation, which must pass, and against a breakable with each of its breaks, which must fail (`ContractCoverageTest`); a break it does not check carries its reason there.
- **Breakables keep the contract of the interface they implement** unless a break is set, and each break changes only what its description says.
- **Concurrency tests are bounded in time** and have no `Thread.sleep`; they use `safety.ContentionCoordinator`.
- **Nothing depends on a path or project outside this repository** but the parent, from `~/.m2`. Dependencies come from Maven Central.

## Conventions

- **Checkstyle** (Built, #10; the rules are in `java-conventions.md`): `final` parameters; lines of at most 120 characters; no star imports; the parent's import order; declaration order; no hidden fields (a constructor parameter that would hide one ends in `Arg`); no `System.out`/`System.err`; test methods named in plain camelCase, the sentence in `@DisplayName` (the user's decision, 2026-11-08; enforced since soliscode-parent 0.1.2, soliscode/soliscode-parent#2, #14). There is no formatter configuration: match the surrounding code.
- **Nullness:** JSpecify (Built, #9). Every package is `@NullMarked`; mark what may be null `@Nullable` (`org.jspecify.annotations.Nullable`, a type annotation: `Map.@Nullable Entry`, `String @Nullable []`). There is no `@NonNull`; the rules are in `java-conventions.md`, "Nullness".
- **Javadoc:** every public and protected type, method and field (the Javadoc gate). Markdown `///` comments.
- **Licence:** Apache 2.0 (`LICENSE.txt`). 256 of the 585 Java source files carry its header; put it on new files.
- **Mermaid:** no semicolons in diagrams (node text, edge labels, `Note over`); Mermaid reads `;` as a statement end.
- **Commits:** stage explicitly (no `git add -A`, `git commit -a` or bare `git stash`), and end the message with the `Co-Authored-By:` trailer.
- **Changes a user of the library would see** go under `## Unreleased` in `RELEASES.md`, in the task that makes them.
