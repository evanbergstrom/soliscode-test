# Review standard

How a change is reviewed in this repository, by Codex (`codex review`, through `AGENTS.md`) and by the `code-reviewer` and `design-reviewer` agents alike. Assume the change is wrong until you have checked it, and look for what its author would not have looked for.

## What to review against

- The acceptance criteria of the task the change claims to deliver: GitLab issue #N for branch `T-####-…` (`glab issue view N --comments`; a child task's issue says `Part of #P`). Every ticked criterion must actually be met. A criterion met only in part, or by a test that does not test it, is a finding.
- The rules in `docs/design/system/constitution.md` and the package dependencies in `architecture.md`.
- The JDK's documentation of the interface a contract tests or a breakable implements. A contract that is stricter or looser than that documentation, without saying so, is a finding.
- The static-analysis gates in `CLAUDE.md`. A suppression outside what `java-conventions.md`, "Suppressions", allows, a workaround that hides a finding, a skipped gate, or a prohibited flag is a finding however it is justified.
- The task's own claims. Where its Outcome says something was verified, check that it was, and how.

## What to look for

- **Contracts that cannot fail.** A contract test that only calls a method, asserts on a value the test itself set, or compares the implementation with itself. A new contract or a changed one not run by `ContractCoverageTest` against a JDK class (it must pass) and a breakable with each break (each must fail), or a break left unchecked there without a reason.
- **Breakables.** A break that does nothing, or does more than its description says; two `Break` constants that are equal; a breakable that breaks its interface's contract with no break set (`equals` and `hashCode`, iterator removal, fail-fast iteration, views that reflect the backing collection, `null` handling as the builder's permits say); a built object or copy that shares state with its builder or original.
- **The JDK contracts.** `equals` and `hashCode` that disagree; `compareTo` inconsistent with `equals`; a collection that breaks the `java.util` interface it implements.
- **Concurrency.** A thread-safety or deadlock test with no bound on its time, a `Thread.sleep`, a race the test relies on but cannot show, worker threads left running after a test, state shared between threads without the documentation saying it is safe.
- **Nullness.** A value that may be null without `@Nullable`, a `@Nullable` whose null is not handled, an override whose nullness differs from the method it overrides.
- **Mutability and sharing.** A mutable array or collection exposed or returned without a copy; a public constant that a caller can change.
- **Failure paths.** Invalid input accepted silently, or refused with an unhelpful message; an exception that loses its cause. Paths with no test.
- **Tests that cannot fail.** Assertions on values the test itself set, an expected value computed by the code under test, a test that passes whether or not the fix is there.
- **The public API.** A change to a public type or method that breaks callers without being recorded in `RELEASES.md`, or a type made public that need not be.
- **The packages.** A dependency against what `architecture.md` allows.
- **Documents** that describe something as existing that does not, or the reverse.

## How to report

- Each finding: `[SEVERITY] path:line — the defect in one sentence → a concrete scenario (these inputs or this state lead to this wrong result) → a suggested fix`. SEVERITY is Blocker, Major, Minor or Nit. A concern you cannot turn into a scenario is a question, and is reported as one.
- Most severe first. Say which findings you confirmed by running something and which you inferred from reading.
- Say briefly what you checked and found sound, so the absence of a finding means something.
- End with a verdict: APPROVE or CHANGES REQUESTED. Don't soften a finding because the change is otherwise good, and don't invent findings to appear thorough. A wrong finding costs more than a missed nit.

## What not to do during a review

- Don't change code, tests or documents, don't commit, and don't write to GitLab. A review reports; the user decides what is filed.
- Don't run commands that change shared state: no `git` operation that moves a branch, and no Maven build while another is running (they share `target/` and `~/.m2`).
- Find every use of a symbol with a text search across `src`, main and test; the Java language server's answers are a lower bound (`CLAUDE.md`, "Java tooling").
