# CLAUDE.md

soliscode-test: a Java library (`com.soliscode:soliscode-test`) for JUnit 6 with three goals (constitution, section 3.1): test methods JUnit lacks, test suites for the JDK's interfaces (every collection interface, then others where a suite is useful, in order of how commonly classes implement them), and thread-safety testing. It holds contracts (interfaces of default test methods for `Collection`, `List`, `Set`, `Map`, `Queue`, `Deque`, their sequenced forms, `SortedSet`, `NavigableSet`, `BlockingQueue`, `BlockingDeque`, `TransferQueue`, `SortedMap`, `NavigableMap`, `ConcurrentMap`, `ConcurrentNavigableMap` (#36), `Iterable`, `Iterator` and `ListIterator` (#37), weakly consistent iterators (#55), `Spliterator` and its primitive kinds (#38), `Map.Entry` (#39), `Comparable` (#40), `AutoCloseable` and `Closeable` (#41), `Comparator` (#42), `CharSequence` (#43), `Future` (#44), `ExecutorService` (#45), `Object` and `Serializable`; the other chosen interfaces are #46–#48 and #50), a breakable implementation of each interface whose faults are switched on by `Break` constants, providers of instances and elements, assertions, concurrency-test support, and the performance test library (`performance`, #69), the test side of soliscode-parent's performance gate. soliscode-common and soliscode-quant test with it (the split of the quant libraries, soliscode/soliscode-quant#6). It builds on the shared parent `com.soliscode:soliscode-parent` (#9), and is developed with Claude Code, with Codex reviewing; [`AGENTS.md`](AGENTS.md) points Codex at this file.

Built: the parent and its gates (#9; at 0.1.13, #14, #57, #64, #69, #71), the contracts and breakables (#1), quant-test's fixes (#2), this file's structure, the scripts and git hooks (#11), Checkstyle passing with every test method in JUnit's naming (#10); since #71 the skills, reviewers and hooks are the soliscode plugin's. **Don't describe a gate or anything else as existing without checking the code**: each document marks its parts Built, Partly built or Designed, and this file does the same.

## Where things are

| Path | What |
|------|------|
| [`docs/design/system/constitution.md`](docs/design/system/constitution.md) | What the project is, its goals, scope, quality requirements, gates and anti-patterns. **Read before writing code**, with `architecture.md`. |
| [`docs/design/system/architecture.md`](docs/design/system/architecture.md) | The packages, what each may depend on, threading, nullness |
| [`docs/design/reference/`](docs/design/reference/) | `java-conventions.md` (public API, naming, nullness, suppressions, Javadoc, imports, tests), `build-and-test.md` (commands, the gates, test tags, contract coverage, releases, the git hooks), `dependency-management.md` |
| [`RELEASES.md`](RELEASES.md) | The version scheme, and each release's entry; `## Unreleased` collects what tasks change for users |
| `src/main/java/com/soliscode/test/` | The library: `contract/`, `breakable/`, `methods/`, `provider/`, `assertions/`, `safety/`, `interfaces/`, `annotations/`, `util/` |
| `src/test/java/com/soliscode/test/` | Its tests, among them `contract/coverage/ContractCoverageTest` (each contract passes against a JDK class and fails for each break) |

When the docs and the code disagree, the code says what exists; fix the doc in the task that finds it, or say why not.

## Claude Code setup

The task workflow, the reviewers and the shared rules come from the soliscode plugin (pinned in `.claude/settings.json`), whose session hook adds the rules, and the path and GitLab values of `.claude/soliscode-project.md`, to the context. If no `soliscode:` skill is available, or the shared rules are not in the context, stop and tell the user to install the plugin before doing anything else.

Built (#71; this project had its own copies of the skills, reviewers, review standard and hooks until then):
- **The plugin**, `soliscode@soliscode-0.1.13`, pinned to the parent's release: the marketplace `soliscode-0.1.13` (`https://gitlab.com/soliscode/soliscode-parent.git`, `ref` `v0.1.13`) in `.claude/settings.json`. It gives the skills `soliscode:proceed` (a task from start to ready-to-merge), `soliscode:commit-and-merge`, `soliscode:gitlab-task` (the task workflow in full) and `soliscode:codex-review`, the reviewers `soliscode:design-reviewer` and `soliscode:code-reviewer`, the shared rules (`rules.md`: tasks, branches, reviews, context, commits, the Maven settings file) and the hooks: the refusing ones (an edit under `target/`, `generated-sources/` or to `.flattened-pom.xml`; a shell command that names both `.m2` and `settings`, prints the effective settings, or refers to the `.m2` directory outside `repository/`: rephrase a command it refuses for another reason, or use the Edit tool), the approval hook, which asks before most of what the approval list below names (its properties, the gates' profiles and the JVM-options variables; not a `@SuppressWarnings` or a POM setting, which the list still covers), and the session hook, which checks the pin.
- **Moving the pin.** The parent's release is in five places, changed in one commit: the `<parent>` in `pom.xml`, the marketplace's name, its `ref` and the enabled plugin in `.claude/settings.json`, and the marketplace path in `AGENTS.md`. Then each person runs, once, from the root, and restarts Claude Code: `claude plugin marketplace add https://gitlab.com/soliscode/soliscode-parent.git#v0.1.N` and `claude plugin install soliscode@soliscode-0.1.N --scope project`.
- **This project's own**, in `.claude/`: the skills `gates` and `release` (only on the user's `/release`, or where a task's acceptance list asks for a release), and `soliscode-project.md` (the GitLab project, `soliscode/soliscode-test`, ID `72689075`; the labels; the decisions that are the user's; the design to read; how a change is implemented; the review focus).
- **Settings:** `.claude/settings.json` holds the schema, the plugin's marketplace and enablement, and `deny` rules on the Maven settings file. Personal allows go in `.claude/settings.local.json`. It is git-ignored, with `.claude/scheduled_tasks.lock`, `.claude/worktrees/` and `.mcp.json`.

## Build and test

All commands run from the repository root. Detail and pitfalls are in [`build-and-test.md`](docs/design/reference/build-and-test.md); the `gates` skill says which to run. **Never run two Maven builds at once**: they share `target/` and `~/.m2`.

Built (#9): Java 25, from the JDK on the `PATH`; Maven 3.9 or later. The enforcer refuses anything older. The parent, `com.soliscode:soliscode-parent:0.1.13` (#71; 0.1.12 until #71), comes from `~/.m2`.

```bash
mvn -B -Djacoco.check.skip=true -DskipTests install   # the install gate: enforcer, licences, Checkstyle, compile with ErrorProne and NullAway, Javadoc
scripts/verify.sh                                     # the full verify from clean (every test, the coverage check), recorded for release.sh
mvn -B verify                                         # the same build, unrecorded
mvn -B -Dtest='Class#method' test                     # one test
mvn -B -Pall-tests verify                             # every test, the extremely-slow ones included
scripts/install-git-hooks.sh                          # once per clone: the pre-commit and pre-push hooks
```

A test failure is the branch's to fix or report. A full verify takes about 5 minutes (2026-11-15, #45); most of it is `ContractCoverageTest`, its thread-safety part about 70 s.

The git hooks (`scripts/git-hooks/`) compile the main and test sources when a commit changes Java or a build file, and refuse a push to `main` of a commit that skipped them (`--no-verify`). They start Maven, so don't commit while a background build runs.

Measure performance with `-Djacoco.skip=true`: the coverage agent slows the tests.

## Static-analysis gates

The parent's gates are **gates, not advisories**: a finding fails the task like a compile error, and "the tests pass" is no substitute.

| Gate | State |
|------|-------|
| Checkstyle (the parent's `soliscode/checkstyle.xml`, severity error, main and test sources, at `validate`; since parent 0.1.9, cyclomatic complexity at most 15, NPath at most 500 and 200 lines per method; since parent 0.1.13, a copyright header at the top of every file) | Built, #10, #64, #71 |
| Javadoc (`doclint` all, `failOnWarnings`, classic and Markdown `///` comments, main sources, at `verify`) | Built, #9 |
| ErrorProne and NullAway (`error-prone` profile, the root `.errorprone`, main sources, at `compile`; NullAway at ERROR, ErrorProne's warnings fatal through `failOnWarning`), JSpecify `@NullMarked` packages | Built, #9 |
| JaCoCo `check` (`jacoco.minimum.coverage` 0.70, line coverage, and, since parent 0.1.9, each method's missed complexity at most 3, at `verify`), Maven enforcer (Java 25, Maven 3.9, dependency convergence, no duplicate classes, no `system` scope, at `validate`) | Built, #9, #14, #64 |
| Architecture (the parent's ArchUnit rules on the main classes: no package cycles, no generic exceptions constructed, no standard streams, and five more; Surefire execution `soliscode-architecture`, at `prepare-package`, so not in `mvn test` or the install gate) | Built, #64 |
| Licences (every dependency, in any scope, under an OSI-approved licence; the `license-main` and `license-test` profiles, at `validate`) | Built, #14 |
| Gate guard (enforcer executions `soliscode-guard` and `soliscode-guard-<gate>`, at `validate`: refuses (with `soliscode-guard-architecture` since parent 0.1.9) a gate plugin's version, dependencies or deciding settings set in this POM, a property that narrows a gate, in the POM or as a `-D`, and a setting the parent fixes given twice in a gate's execution. Some flags on `CLAUDE.md`'s list it lets through (`-DskipTests` and `-Djacoco.skip` in a verify, `-Djacoco.minimum.coverage`, a gate's profile turned off, `-Dmaven.test.skip`, `-Dmaven.test.failure.ignore`; `-Djacoco.haltOnFailure=false` changes nothing, the check sets it literally); the list holds either way. Each `soliscode-guard-<gate>` is in its gate's profile. The gates' execution ids are in `build-and-test.md`, "Gates") | Built, #57 |

As each is built, the task that builds it marks it Built in every place that names its issue as Designed or Partly built: `grep -rn '#<N>' CLAUDE.md AGENTS.md RELEASES.md docs .claude` lists them. Before calling Java work done, run every gate that is built and confirm zero new findings. ErrorProne findings print with an `errorprone.info` link: count them with `grep -c 'errorprone.info' <log>`.

Never use the following without the user's approval in the current session (approval on another task doesn't carry over):
- `-Dcheckstyle.skip=true`, `-Dmaven.javadoc.skip=true`, `-Denforcer.skip=true`
- `-P '!error-prone'`, `-P '!checkstyle-main'`, `-P '!checkstyle-test'`, `-P '!license-main'`, `-P '!license-test'`, `-P '!coverage'` or `-P '!architecture'`, or an ErrorProne option that turns a check off or demotes it (`-XepAllErrorsAsWarnings`, `-Xep:<Check>:WARN|OFF`).
- `-DskipTests` or `-Djacoco.skip=true` in a full verify (they leave the coverage check nothing to check); `-DskipTests` is fine in the install gate
- `-Dmaven.test.skip=true`, and `-Dmaven.test.failure.ignore=true` in a gate
- `-Djacoco.haltOnFailure=false`, `-Djacoco.minimum.coverage` on the command line, Surefire's `-DforkCount=0`, a command-line `-DargLine`, the enforcer's `-Denforcer.rules`
- a `-D` in `MAVEN_OPTS`, `JAVA_TOOL_OPTIONS`, `_JAVA_OPTIONS` or `JDK_JAVA_OPTIONS` (verify.sh and release.sh refuse one)
- a property, `-D` or plugin configuration that narrows a gate (Checkstyle's `includes`, `excludes`, `suppressionsLocation`, `violationIgnore`; the enforcer's `rulesToSkip`; Javadoc's `excludePackageNames`; the licence plugin's `includedTypes`, `missingLicensesFileArtifact`, or a licence list or exception of this project's own), a plugin of this project's own that writes under `target/license/` or `target/soliscode-architecture/`, a `system`-scope dependency, and configuration of the guard's own executions (`soliscode-guard`, `soliscode-guard-<gate>`)
- hand-written code in a package with a part named `generated`, `artiogen` or `jmh_generated`: the coverage gate leaves it out (and the Javadoc gate leaves out `jmh_generated`)
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
- **Nothing depends on a path or project outside this repository** but the parent and `soliscode-performance-api`, released with it (#69), from `~/.m2`. Dependencies come from Maven Central.

## Conventions

- **Checkstyle** (Built, #10; the rules are in `java-conventions.md`): `final` parameters; lines of at most 120 characters; no star imports; the parent's import order; declaration order; no hidden fields (a constructor parameter that would hide one ends in `Arg`); no `System.out`/`System.err`; test methods named in plain camelCase, the sentence in `@DisplayName` (the user's decision, 2026-11-08; enforced since soliscode-parent 0.1.2, soliscode/soliscode-parent#2, #14). There is no formatter configuration: match the surrounding code.
- **Nullness:** JSpecify (Built, #9). Every package is `@NullMarked`; mark what may be null `@Nullable` (`org.jspecify.annotations.Nullable`, a type annotation: `Map.@Nullable Entry`, `String @Nullable []`). There is no `@NonNull`; the rules are in `java-conventions.md`, "Nullness".
- **Javadoc:** every public and protected type, method and field (the Javadoc gate). Markdown `///` comments.
- **Licence:** Apache 2.0 (`LICENSE.txt`). Every Java file, main and test, starts with the SolisCode header, `Copyright 2025 Evan Bergstrom` and `SPDX-License-Identifier: Apache-2.0` (777 files: the 776 #71 gave it to, and the test class #71 split off); Checkstyle's `[CopyrightHeader]` fails a file without it, since parent 0.1.13. The text is on the parent guide's Checkstyle page, "The copyright header".
- **Mermaid:** no semicolons in diagrams (node text, edge labels, `Note over`); Mermaid reads `;` as a statement end.
- **Changes a user of the library would see** go under `## Unreleased` in `RELEASES.md`, in the task that makes them.
