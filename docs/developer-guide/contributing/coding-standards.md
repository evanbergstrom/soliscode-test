# Coding standards

## The gates

Every build runs the SolisCode parent's gates. A gate fails the build like a compile error: there is no warning to ignore. The switches that would turn one off, or narrow it, are on [`CLAUDE.md`](../../../CLAUDE.md)'s list, and need the maintainer's approval. Each is described, with what a failure looks like and how to fix it, in the parent's guide:

| Gate | What it checks | When |
|---|---|---|
| [Checkstyle](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/03-gates/checkstyle.md) | Layout, naming, the copyright header, method complexity and length, in main and test code | `validate` |
| [The enforcer](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/03-gates/enforcer.md) | Java 25, Maven 3.9, dependency convergence, no duplicate classes, no `system` scope, the coverage minimum set | `validate` |
| [Licences](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/03-gates/licences.md) | Every dependency under an OSI-approved licence | `validate` |
| [ErrorProne and NullAway](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/03-gates/errorprone-nullaway.md) | Common bugs, and null safety, in main code | `compile` |
| [Architecture](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/03-gates/architecture.md) | Package cycles, `internal` packages, standard streams, logging and the other rules, in main code | `prepare-package` |
| [Javadoc](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/03-gates/javadoc.md) | A well-formed comment on every public and protected member | `verify` |
| [Coverage](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/03-gates/coverage.md) | Line coverage, and each method's missed complexity | `verify` |

[The gates](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/03-gates/README.md) says when each runs. [Ground rules](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/07-ground-rules.md) says what you must not do to a gate (skip it, narrow it, configure its plugin), and how to ask for a change to one. A failed build is explained in the parent's [Troubleshooting](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/06-troubleshooting.md). This project's list of the flags never to use without the maintainer's approval is in [`CLAUDE.md`](../../../CLAUDE.md), "Static-analysis gates", and how each gate stands here is in [build-and-test.md](../../design/reference/build-and-test.md#gates), "Gates".

The git hooks run the early gates when you commit; `scripts/verify.sh` runs them all.

If a gate fires on code you did not touch, don't fix it on the side or suppress it: say so on the issue, with the gate, the file and the count, and agree there whether it is fixed in the task or in a task of its own.

## The copyright header

Every Java file starts with it, and Checkstyle fails a file without it:

```java
/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */
```

The year is when the project began, not when the file was written, and it never changes. Checkstyle checks Java files only: the scripts under `scripts/` carry the long Apache 2.0 header as `#` comments, with `Copyright 2026 Evan Bergstrom`, after the shebang line where there is one.

## The library's own conventions

[java-conventions.md](../../design/reference/java-conventions.md) holds them. In short:

### Naming

- A contract is `XxxContract`, an interface of default test methods: an interface contract is named for its interface (`ListContract`), a method contract for its method (`AddContract`), in the package `contract.<interface in lower case>`.
- A breakable is `BreakableXxx`, for the JDK interface `Xxx`, built with its `BreakableXxxBuilder` where it has one. A `Break` constant names the fault it causes (`ADD_DOES_NOT_ADD_ELEMENT`).
- A test method has a plain camelCase name, with no underscores and no `test` prefix (`toArrayWhenEmptyReturnsEmptyArray`), and its `@DisplayName` holds the readable sentence. This holds for the contracts in `src/main` too.
- A constructor parameter that would hide a field ends in `Arg` (`this.breaks = breaksArg`). An unnamed variable is `ignored`, not `_`.
- Parameters are `final`. A utility class is `final`, with a private constructor.

The rest are in [java-conventions.md](../../design/reference/java-conventions.md#naming), "Naming".

### Nullness

- Every package is `@NullMarked` in its `package-info.java`, so everything is non-null unless it is marked `@Nullable` (`org.jspecify.annotations.Nullable`). There is no `@NonNull`.
- `@Nullable` is a type annotation, so it sits next to the type: `Map.@Nullable Entry<K, V>`, and `T @Nullable []` for a nullable array.
- An argument that must not be null is checked with `Objects.requireNonNull` at the start of the method.

### Errors and validation

- A refused argument throws `IllegalArgumentException`, or `NullPointerException` for a null, with a message that names the argument.
- An exception caught and rethrown keeps its cause. No exception is swallowed: an empty `catch` is allowed only if its variable is named `expected`, `ignore` or `ignored`.
- No `System.out` or `System.err`, in main or test code.

### Suppressions

Where the code does on purpose what an ErrorProne or NullAway check warns against, it gets one `@SuppressWarnings` for the one finding, on the smallest declaration, with a one-line `// Check: reason` above it; never a workaround that hides the finding. [java-conventions.md](../../design/reference/java-conventions.md#suppressions), "Suppressions", lists the kinds allowed. Another kind is the maintainer's decision.

### Dependencies

- A new third-party library is named without a version, and only once the parent manages it. Ask for one in an [issue of the parent](https://gitlab.com/soliscode/soliscode-parent/-/issues), as [Ground rules](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/07-ground-rules.md#a-new-managed-library-or-a-new-version) says.
- A new compile-scope dependency is the maintainer's decision: every library that tests with SolisCode Test gets it. [dependency-management.md](../../design/reference/dependency-management.md) lists those there are, and why.

## Public API design

- There is no `module-info.java`, so every public type in `com.soliscode.test.*` is public API, the contracts' test methods included: their names are what a user's test reports show.
- A change that breaks callers (a type, method or signature removed, renamed or changed, a result that changes) is the maintainer's decision. Record each change a user sees under `## Unreleased` in [RELEASES.md](../../../RELEASES.md), in the task that makes it, a breaking one marked as such, and say in the user guide's [Upgrading](../../user-guide/upgrading.md) what a user must do.
- Deprecate before removing where you can: `@Deprecated(since = "<version>", forRemoval = <true or false>)`, with the Javadoc's `@deprecated` naming what to use instead.
- **A contract follows the JDK's documentation** of the interface it tests, and says, in its Javadoc, where it reads it more strictly or more loosely.
- **A contract can fail.** Every contract is run against a correct JDK class and against a breakable with each of its breaks ([Testing standards](testing-standards.md#contract-coverage)).
- **A breakable keeps the contract of its interface** unless a break is set, and each break changes only what its description says.
- **A contract respects what the class supports.** A test of an optional method checks `supportsMethod(...)` first, and a collection contract reads `permitNulls()`, `permitDuplicates()` and `permitIncompatibleTypes()`.

## Review checklist

A review checks:

- [ ] Correctness and edge cases
- [ ] Tests cover the change, and a new contract or break is checked by `ContractCoverageTest`
- [ ] A contract asserts what the JDK's documentation says, and says where it is stricter or looser
- [ ] Public API changes are intended, documented in the Javadoc, and listed under `## Unreleased` in `RELEASES.md`
- [ ] No needless complexity or dependency
- [ ] The user guide and the developer guide match the change
- [ ] The design documents under `docs/design/` match the change

## See also

- [Testing standards](testing-standards.md)
- [Documentation standards](documentation.md)
