# Build and test

How to build and test, what each gate is here, and what the build does not have yet. All commands run from the repository root. Each part is marked **Built**, **Partly built** or **Designed**, with the task that builds it.

The gates, their switches and the flags never to use are soliscode-parent's, and every child inherits them: [soliscode-parent's build-and-test.md](https://gitlab.com/soliscode/soliscode-parent/-/blob/main/docs/design/reference/build-and-test.md) defines them, and "What a child adds" there says what a child must add. This document says what this project adds and how the gates stand here. Related: [java-conventions.md](java-conventions.md), [dependency-management.md](dependency-management.md).

## Prerequisites

- Maven 3.9 or later.
- Built (#9): JDK 25 on the `PATH`, with no toolchains entry. No preview feature is used. The enforcer refuses an older JDK or Maven.
- `com.soliscode:soliscode-parent:0.1.2` in `~/.m2` (#14). The POM names it with an empty `<relativePath/>`, so Maven never looks for it in a sibling directory; it is not on Maven Central. A release of soliscode-parent, made in its own clone, installs it.
- Never print or change `~/.m2/settings.xml`. It holds credentials, and Maven reads it without being told to.

## Commands

```bash
scripts/verify.sh                                # the full verify from clean, recorded for releases (#11)
mvn -B clean verify                              # the same build, unrecorded: every gate, the default test selection
mvn -B verify -Pall-tests                        # every test, the extremely-slow ones included
mvn -B test -Dtest='Class#method'                # one test
mvn -B test -Dgroups=concurrency                 # only the tests with a tag (a JUnit tag expression)
mvn -B test -DexcludedGroups=very-slow           # skip other tags instead of the default's
mvn -B install                                   # install 0.1-SNAPSHOT into ~/.m2
mvn -B dependency:analyze                        # libraries used but not declared, or declared but not used
scripts/install-git-hooks.sh                     # once per clone (#11)
```

**Never run two Maven builds at once.** They share `target/` and `~/.m2`.

Built (#10): Checkstyle runs at `validate` and passes, with no finding. It had 2,958 under soliscode-parent 0.1.2 on 2026-11-08 (1,742 in main code, 1,216 in tests), #14. Deactivating its profiles (`-P '!checkstyle-main,!checkstyle-test'`) is on the parent's list of flags never to use, so it needs the user's approval in the current session.

## Gates

Every gate is inherited from soliscode-parent 0.1.2 (#14). This project configures none of them itself.

| Gate | Phase | State here |
|------|-------|------------|
| Maven enforcer: Java 25, Maven 3.9, dependency convergence, no class twice on the class path, no `system`-scope dependency, `jacoco.minimum.coverage` set | `validate` | Built (#9, #14). Passes |
| Licences: every dependency, in any scope, transitive ones included, names an OSI-approved licence (the MojoHaus License Maven Plugin, `add-third-party`, the `license-main` and `license-test` profiles; it writes `target/generated-sources/license/THIRD-PARTY.txt`) | `validate` | Built (#14). Passes: 9 dependencies, EPL-2.0 (JUnit's six) and Apache-2.0 (JSpecify, OpenTest4J, apiguardian) |
| Checkstyle, the parent's rules (`soliscode/checkstyle.xml` from soliscode-build-tools), main and test sources | `validate` | Built (#10). Passes. The rule for test-method names, `[TestMethodName]`, is in soliscode-parent 0.1.2 (soliscode/soliscode-parent#2, java-conventions.md, "Test method names") |
| ErrorProne and NullAway, at ERROR, `failOnWarning`, main sources, activated by the empty `.errorprone` file | `compile` | Built (#9). Passes, with the suppressions java-conventions.md lists |
| Javadoc, `doclint` all, `failOnWarnings`, main sources, classic and Markdown `///` comments | `verify` | Built (#9). Passes |
| JaCoCo `check`: line coverage at least `jacoco.minimum.coverage` | `verify` | Built (#9). Passes ("Coverage" below) |
| OWASP dependency-check, key from `NVD_API_KEY` | on request, `-P dependency-check` | Inherited, advisory. Not run here yet |

A change is done only with zero findings from every gate. The flags never to use, and what to do when a gate fires on code you did not touch, are in the parent's document ("Flags never to use"); the same list holds here, with no addition.

## What this project adds

Built (#9). The seven things the parent's "What a child adds" asks for, and two of its own:
1. The parent, `com.soliscode:soliscode-parent:0.1.2`, with `<relativePath/>`.
2. `revision` `0.1-SNAPSHOT` in `pom.xml`; the version is `${revision}`.
3. `.mvn/jvm.config`, exactly the parent's ten `--add-exports` and `--add-opens` lines for ErrorProne.
4. An empty `.errorprone` file at the root.
5. `jacoco.minimum.coverage` 0.70.
6. Its dependencies, without versions (dependency-management.md).
7. `.flattened-pom.xml` in `.gitignore`.
8. The `excludedGroups` property, `extremely-slow`, which Surefire reads as its default tag exclusion ("Test tags").
9. The `all-tests` profile, which sets `excludedGroups` empty.

It adds no Surefire, Javadoc, JaCoCo or Checkstyle configuration, and no plugin.

## Tests

Built (#9). 6,230 tests on 2026-11-08 in the default build, and every one passes. The parent's Surefire runs the classes named `**/*Test.java` and no others, and JUnit runs no abstract class: a test class ends in `Test`, an abstract base ends in `Test` and begins with `Abstract`, and a helper does not end in `Test`.

### Test tags

Built (#5). The annotations in `com.soliscode.test.annotations` add JUnit tags (the names are `TestTags`' constants). Surefire skips the tags in the `excludedGroups` property; `-Pall-tests` clears it, and `-DexcludedGroups=...` replaces it.

| Annotation | Tag | Meaning | Default build |
| :--- | :--- | :--- | :--- |
| `@Slow` | `slow` | 100 ms – 1 s | runs |
| `@VerySlow` | `very-slow` | 1 s – 10 s | runs |
| `@ExtremelySlow` | `extremely-slow` | over 10 s | **skipped** (`-Pall-tests` runs it) |
| `@ConcurrencyTest` | `concurrency`, `nondeterministic` | thread-safety test, repeated (`THREAD_SAFETY_REPEATS`, 5), with a timeout (`THREAD_SAFETY_TIMEOUT`, 10 s) | runs |
| `@Nondeterministic` | `nondeterministic` | may pass or fail differently between runs | runs |

The default was chosen by the user on #5 (2026-11-07), so that the thread-safety contracts run on every build. No test is tagged `extremely-slow` yet.

**Time.** The default `mvn clean verify` took about 2½ minutes on 2026-11-08, for 6,230 tests (Intel i9-8950HK, 12 threads, JDK 25). On 2026-11-07 the tests alone took about 110 s without coverage, most of it the thread-safety part of `ContractCoverageTest` (`very-slow`); the untagged tests took about 13 s. Measure test time with `-Djacoco.skip=true`, since the coverage agent slows the tests; never in a full verify.

### Contract coverage

Built (#6). `ContractCoverageTest` (`src/test/.../contract/coverage`) shows that each contract can pass and can fail. For each contract it runs every test method against a JDK implementation and against an unbroken breakable implementation, which must pass, and then against the breakable with each of its breaks, one at a time, through the breakable's builder, which must fail. A break that shows only when a method is unsupported, or when incompatible types are refused, is applied with that configuration too.

- The contracts it covers: `IterableContract`, `CollectionContract`, `SequencedCollectionContract`, `ListContract`, `SetContract`, `SequencedSetContract`, `QueueContract`, `DequeContract`, `MapContract`, `SequencedMapContract`, `SortedMapContract`, `NavigableMapContract`, `ThreadSafeCollectionContract` and `SerializableContract`. `ObjectContract` and `IntegerContract` are not in it.
- Thread-safety breaks (`*_IS_NOT_THREAD_SAFE`) are checked by `ThreadSafeCollectionContract` only, whose concurrency tests run only there, since they are slow (that part is `@VerySlow`).
- A break it leaves out is one that is not a fault for that contract's type, or that no test could reliably see. `uncheckedReason` in the test gives the reason for each; a new exclusion goes there, with its reason.

A new contract or breakable is added to it, so that every break is shown to fail some test.

### Bounds

Built. Concurrency and deadlock tests do not sleep and do not wait without a limit. `ContentionCoordinator` gives its workers a start and a finish timeout (5 s each by default). It stops waiting for workers that outlive the finish timeout, checks for a deadlock, and interrupts them; its threads are daemon threads, so a deadlock fails the test instead of hanging the build. `@ConcurrencyTest` adds a JUnit timeout. Known: the workers yield on about half the iterations, not once every `yieldInterval` (#12).

## Coverage

Built (#9). The parent's `coverage` profile runs JaCoCo's `report` (`target/site/jacoco/`) and `check` at `verify`: the `LINE` covered ratio of the module as a whole must be at least `jacoco.minimum.coverage`, which is 0.70, quant-test's figure (#3). Measured on 2026-11-08: 6,743 of 7,504 lines, 0.8985 (the `LINE` counter at the end of `target/site/jacoco/jacoco.xml`). A task that raises coverage may raise the minimum; none lowers it without the user's word.

## Releases

The version scheme and the release history are in [RELEASES.md](../../../RELEASES.md). The version is `${revision}`, `0.1-SNAPSHOT` on `main`; releases are `0.1.N`, installed in the local Maven repository only, and the parent's flatten plugin writes the installed POM with the version resolved. The old tags (`v1.0.0`–`v1.0.14`) stay; release numbering counts only `v0.1.*`. The first release, `0.1.1`, is #3's.

### The record of a green build

Built (#11), as in soliscode-quant. `scripts/verify.sh` takes no arguments. It refuses to run if `MAVEN_ARGS` is set, since Maven would add it to the build, or if `MAVEN_OPTS`, `JAVA_TOOL_OPTIONS`, `_JAVA_OPTIONS` or `JDK_JAVA_OPTIONS` sets a property, which the JVM would pass to Maven; `release.sh` refuses the same. It runs the full verify from clean, `mvn -B clean verify`, with its log in `$(git rev-parse --git-common-dir)/soliscode/verify.log` (outside `target/`, which the `clean` deletes), and, if the build is green, records it:
- **Key:** the tree of the working copy when the build started: every tracked file, and every untracked file `.gitignore` does not exclude. The script names it with `git write-tree` from a temporary index (`git read-tree HEAD`, then `git add -A`), so the real index is not touched.
- **Where:** `$(git rev-parse --git-common-dir)/soliscode/verified/<tree>`, inside `.git`: local to the clone, shared by its worktrees, never committed.
- **What:** the date, the commit `HEAD` was at, and the test totals from Surefire's reports.
- A build is recorded only if it ran tests, none failed or errored, and the coverage check was met once and not skipped (`All coverage checks have been met.` in the log, no `Skipping JaCoCo execution`).
- If the tree changed while the build ran, nothing is recorded, and the script says so.

A branch's green verify counts for its squash merge on `main` when the merge's tree is the branch's working copy. Otherwise there is no record for the merge (never a wrong one), and the `release` skill runs the verify once on `main`.

Built (#10): with Checkstyle passing, the full verify runs to the end, and `verify.sh` records it. Before #10 it failed at `validate`, so its other steps were tested with Maven replaced by a stub (#11's notes).

### `release.sh`

Built (#11). `scripts/release/release.sh --changes <file> [--trailer <line>]...`. `<file>` holds the release's changes as bullets, in the entry format's style, without a heading; the script adds `### Changes`, and refuses a file with a heading line. It runs with `set -euo pipefail`:
1. Refuses unless it runs on `main` with a clean working copy (`git status --porcelain` empty), and `<file>` exists and is not empty.
2. Finds the version: `0.1.N`, one past the highest tag of the form `v0.1.<digits>`, or `0.1.1` if there is none. Refuses if `v0.1.N` exists.
3. Refuses unless a record of a green build exists for `HEAD`'s tree, and says to run `scripts/verify.sh`.
4. Builds and installs the release from clean, `mvn -B -Drevision=0.1.N -DskipTests -Djacoco.check.skip=true clean install`: the record shows the tests green for this tree.
5. Writes the release's entry to `RELEASES.md`: the version and date, `HEAD`'s commit, the test count from the record, the install coordinates, and `### Changes` followed by the contents of `<file>`. The entry takes the place of the `## Unreleased` section's contents (up to the next `## ` heading), and an empty `## Unreleased` stays above it, so the next tasks' changes go there. The line "No release has been made yet." is removed.
6. Commits `RELEASES.md` alone as `Release 0.1.N`, with the `--trailer` lines given, then tags that commit `v0.1.N`, annotated. Nothing is pushed. If writing or committing fails, `RELEASES.md` is restored from `HEAD`.

## Git hooks

Built (#11). `scripts/install-git-hooks.sh` sets `core.hooksPath` to `scripts/git-hooks/`; run it once per clone.
- **pre-commit:** when the staged changes touch `src/`, `pom.xml`, `.mvn/` or `.errorprone`, or `HEAD`'s tree has no record (the first commit after installing, an amended `--no-verify` commit), it compiles the main and test sources (`mvn -B -q -DskipTests -Djacoco.skip=true test-compile`), which runs the enforcer, the licence gate, Checkstyle, ErrorProne and NullAway. It refuses a commit that leaves one of those files unstaged or untracked, since the build would vouch for something the commit lacks. On success it records the staged tree in `$(git rev-parse --git-common-dir)/soliscode/pre-commit/`.
- **pre-push:** refuses a push to `main` of any commit whose tree has no pre-commit record here, such as one made with `--no-verify`.

The hooks start Maven: don't commit while a background build is running. Before #10 made Checkstyle pass, a commit that the pre-commit hook built failed at `validate`, so the hooks were not installed in this clone; they are installed once #10 is merged (#11's notes).

## Site and reports

Inherited, not run here since #9: `mvn site` would produce the project reports, Javadoc (with the build-tools stylesheet), the Surefire, Checkstyle and JaCoCo reports and the source cross-reference. `src/site/site.xml` is this project's menu. Nothing in this repository publishes the site the README links to (`evanbergstrom.github.io/soliscode-test`).

## What the build does not have

- No CI pipeline: the gates run locally. The old `.gitlab-ci.yml` included a file soliscode-parent does not have, so its pipelines failed before any job ran from `69f5333` on (#3); #11 removes it.
- No deployment to a remote repository: releases are installed in `~/.m2` only.
- No benchmarks.
