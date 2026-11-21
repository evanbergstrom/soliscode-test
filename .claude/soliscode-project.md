---
gitlab-project: soliscode/soliscode-test
gitlab-project-id: 72689075
---
# soliscode-test: the project file for the soliscode plugin

The values and the parts of the shared skills and reviewers that are this project's own (soliscode-parent's docs/design/reference/claude-plugin.md, "The project file"). Added in #71, from what this project's own copies of the skills, the reviewers and the review standard said of it.

## Project

SolisCode Test, a Java library of JUnit 6 contracts, breakable implementations, providers and assertions for testing implementations of the JDK's interfaces, and of thread-safety and performance test support.

## Labels

- `area::build` for the build and its gates, `area::ai-dev` for the agent instructions, skills and reviewers. `bug` (a fault in the library) is the project's own too.
- `fix` (a bug) and `quality` are group labels this project may use.
- This project has no modules or features of its own, so no `module::` or feature labels.
- The label picker shows every group label. Never put one on an issue here unless it is listed above, and never delete or rename a group label: that changes it for every project in the group.

## Other projects

soliscode-parent is `soliscode/soliscode-parent` (`72688995`), soliscode-common `soliscode/soliscode-common` (`88563715`), soliscode-quant `soliscode/soliscode-quant` (`88456548`). A blocker there is written fully qualified (`**Blocked by:** soliscode/soliscode-parent#M`) and linked with that project's ID as `target_project_id`.

## Decisions that are the user's

A new compile-scope dependency; a change to public API that breaks callers once 0.1.1 is released (before 0.1.1, public API may break where the task calls for it, each break recorded in `RELEASES.md`); a suppression of a kind `java-conventions.md` does not list; a gate rule that doesn't fit the code; which JDK interfaces get a contract (constitution, goal 2).

## Design

Read `docs/design/system/constitution.md` and `architecture.md` before writing code, `docs/design/reference/java-conventions.md`, and `build-and-test.md` for a change to the build or the tests. Where the docs and the code disagree, the code says what exists; fix the doc in the task, or say why not. A task's design is written under `docs/design/`, or on the issue for a small task.

## Implement

- For a bug, reproduce it first with a failing test, and keep the test.
- A contract follows the JDK's documentation of the interface it tests, cited where it reads it strictly or loosely; a new contract or break is checked by `ContractCoverageTest` (the contract passes against a JDK class and fails for each break, and a break it does not check has its reason in `uncheckedReason`).
- Keep the docs that describe what changed in step, marking what is built and what is only designed.
- What a user of the library sees differently goes under `## Unreleased` in `RELEASES.md`.

## Review focus

**Read first:** `CLAUDE.md` (the gates, the design constraints and the conventions), `docs/design/reference/java-conventions.md`, and for a design `docs/design/system/constitution.md` (goals, scope, rules, anti-patterns) and `architecture.md` (the packages and what each may depend on), with every document the change touches or cites.

**Also review against:** the JDK's documentation of the interface a contract tests or a breakable implements; a contract that is stricter or looser than that documentation, without saying so, is a finding. A suppression outside what `java-conventions.md`, "Suppressions", allows is a finding however it is justified.

**What to look for:**
- **Contracts that cannot fail.** A contract test that only calls a method, asserts on a value the test itself set, or compares the implementation with itself. A new contract or a changed one not run by `ContractCoverageTest` against a JDK class (it must pass) and a breakable with each break (each must fail), or a break left unchecked there without a reason. For a contract, read the JDK's documentation of the method and compare it with what the contract asserts.
- **Breakables.** A break that does nothing, or does more than its description says; two `Break` constants that are equal; a breakable that breaks its interface's contract with no break set (`equals` and `hashCode`, iterator removal, fail-fast iteration, views that reflect the backing collection, `null` handling as the builder's permits say); a built object or copy that shares state with its builder or original.
- **The JDK contracts.** `equals` and `hashCode` that disagree; `compareTo` inconsistent with `equals`; a collection that breaks the `java.util` interface it implements.
- **Concurrency.** A thread-safety or deadlock test with no bound on its time, a `Thread.sleep`, a race the test relies on but cannot show, worker threads left running after a test, state shared between threads without the documentation saying it is safe.
- **Nullness.** A value that may be null without `@Nullable`, a `@Nullable` whose null is not handled, an override whose nullness differs from the method it overrides.
- **Mutability and sharing.** A mutable array or collection exposed or returned without a copy; a public constant that a caller can change.
- **Failure paths.** Invalid input accepted silently, or refused with an unhelpful message; an exception that loses its cause. Paths with no test.
- **The public API.** A change to a public type or method that breaks callers without being recorded in `RELEASES.md`, or a type made public that need not be.
- **The packages.** A dependency against what `architecture.md` allows; public API placed in the wrong package.
- **A design** that leaves a part unmarked Built, Partly built or Designed, or marks as built what the code does not do (check with grep; a structural claim needs the command that proves it).

**Finding uses:** every use of a symbol is found with a text search across `src`, main and test (`grep -rn '<Name>' src`); the Java language server's answers are a lower bound.

**Commands a reviewer may run** (read-only): `git diff`, `git log`, `grep`, `glab issue view`, and a single targeted Maven test (`mvn -B -Dtest=Class test`) to confirm a finding, only if no other build is running (`pgrep -f maven` shows none). No Maven build in a design review, and never `mvn install`.
