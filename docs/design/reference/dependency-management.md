# Dependency management

Where versions are set, what the project depends on, and how to change or add a dependency.

Related: [soliscode-parent's dependency-management.md](https://gitlab.com/soliscode/soliscode-parent/-/blob/main/docs/design/reference/dependency-management.md), which lists what the parent manages, and [build-and-test.md](build-and-test.md).

## Where versions are set

Built (#9, #14, #57). soliscode-parent 0.1.8 sets every version: its `dependencyManagement` names each library (JUnit's through its BOM, `org.junit:junit-bom`, imported, so the Jupiter and Platform artifacts agree), and its `pluginManagement` names each plugin. This project's `pom.xml` names its dependencies without versions, and sets no plugin, plugin version or compiler level. The only version it sets is its own, `${revision}`, and its parent's, `0.1.8`.

The parent's enforcer, at `validate`, refuses a dependency graph that does not converge (`dependencyConvergence`) and a class found in two artifacts on the class path (`banDuplicateClasses`).

## What the project depends on

Built (#9). Everything is at compile scope: this is a test library, and its users' tests run on what it carries. Each is declared, at the parent's version:

| Library | Version | Why |
|---------|---------|-----|
| JUnit Jupiter API (`org.junit.jupiter:junit-jupiter-api`) | 6.1.3 | The contracts are interfaces of JUnit test methods; the assertions report through JUnit |
| JUnit Jupiter Params (`org.junit.jupiter:junit-jupiter-params`) | 6.1.3 | For users' parameterised tests; main code does not use it itself. Added by #9 |
| JUnit Jupiter (`org.junit.jupiter:junit-jupiter`) | 6.1.3 | The aggregator, declared for the engine (`junit-jupiter-engine`), which tests need only at run time |
| JUnit Platform Commons (`org.junit.platform:junit-platform-commons`) | 6.1.3 | The internal `StringUtils` and `UnrecoverableExceptions` (below); the tests also use its `AnnotationSupport` |
| OpenTest4J (`org.opentest4j:opentest4j`) | 1.3.0, the version JUnit 6.1.3 uses | `AssertionFailedError`, which the assertions throw |
| JSpecify (`org.jspecify:jspecify`) | 1.0.1; lifts the 1.0.0 that JUnit 6.1.3 brings | `@NullMarked` and `@Nullable` (java-conventions.md, "Nullness"); at compile scope so that users' tools see them |

`org.apiguardian:apiguardian-api` (1.1.2) comes transitively from JUnit and is not declared: no code here uses it. The parent's `dependency:analyze` configuration ignores `junit-jupiter` (declared for its engine) and `junit-jupiter-params` (carried for users).

There is no test-scope dependency, no JUnit Platform test kit and no AssertJ. #9 removed the one test-scope dependency, `junit-platform-launcher`, which no test used (Surefire brings its own).

Main code uses two classes JUnit marks internal, `org.junit.platform.commons.util.StringUtils` (six assertion classes: `AssertContains`, `AssertContainsAll`, `AssertContainsAllByIdentity`, `AssertContainsNone`, `AssertContainsNoneByIdentity`, `AssertDoesNotContain`) and `UnrecoverableExceptions` (`AssertThrowsAnyOf`, `AssertThrowsDifferent`). A JUnit release may change them without notice; read its release notes for them.

Every dependency comes from Maven Central, except the parent, which is in `~/.m2` only (build-and-test.md, "Prerequisites").

## Changing a version

Versions change in soliscode-parent, not here:
1. The parent changes the version's property and releases a new `0.1.N`, by its own procedure.
2. This project moves to that release by changing `<parent>`'s `<version>` in `pom.xml`.
3. Read the library's release notes for changes in behaviour (and for JUnit, in the internal classes above), and say what they are in the task.
4. Run the full verify (the `gates` skill).
5. Record it under `## Unreleased` in [RELEASES.md](../../../RELEASES.md): a changed version of a compile-scope dependency reaches every user.

## Adding a dependency

A new compile-scope dependency is a decision for the user: it becomes a dependency of everyone who uses the library. Propose it with the reason, the licence and the alternatives, and prefer the JDK. A test-scope dependency needs only the task's agreement.

Built (#14). Every dependency, at any scope and with its transitive ones, must name an OSI-approved licence in its POM: the parent's licence gate fails the build at `validate` otherwise (build-and-test.md, "Gates"), and its allowed list and exceptions are in soliscode-build-tools, where this project cannot change them. A dependency the gate refuses needs an exception in soliscode-parent, in a task there, with its reason. The enforcer refuses a `system`-scope dependency, which the licence gate cannot check.

To add one, once agreed:
1. If the parent does not manage it, it is added to the parent first (soliscode-parent's "Adding a managed dependency"), and released.
2. Declare it in `pom.xml`, without a version, at the narrowest scope that works (`<scope>test</scope>` for a library only the tests use).
3. Run `mvn -B dependency:analyze` and the full verify. The enforcer's convergence rule and the licence gate must still pass.
4. Record a compile-scope addition under `## Unreleased` in `RELEASES.md`.

## Security

Inherited: OWASP dependency-check, advisory, in the parent's `dependency-check` profile, reading the NVD key from the `NVD_API_KEY` environment variable: `NVD_API_KEY=<key> mvn -B -P dependency-check -DskipTests verify`. Not run here yet. A key is never written in a file in the repository.
