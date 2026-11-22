# Testing standards

For how to *run* the tests, see [Running tests](../getting-started/running-tests.md).

## What must be tested

| Change | Tests it needs |
|---|---|
| Bug fix | A test that fails without the fix, kept |
| New feature | Tests of its behaviour, through the public API |
| Public API change | Tests for every new or changed public class and method |
| New contract, or a new test method in one | Its run in `ContractCoverageTest`: it passes against a JDK class and fails for each break of its breakable |
| New break | Its run in `ContractCoverageTest`: some contract test fails with it, or it has a reason it cannot |
| New assertion | Tests that show it passing and failing, and that check its failure message |
| Refactoring | The existing tests pass unchanged |
| A code sample in the user guide | Kept as a test class in `src/test/java/com/soliscode/test/guide/`, which the build compiles and runs, but for the performance scenario, which it only compiles |

## Contract coverage

A contract that never fails tests nothing. `ContractCoverageTest` (`src/test/java/com/soliscode/test/contract/coverage/`) shows that each contract can pass and can fail: it runs every test method of the contract against a JDK implementation and against an unbroken breakable, which must pass, and then against the breakable with each of its breaks, one at a time, which must fail.

- A new contract, or a new breakable, is added to it.
- A break that no test of a contract can see, or that is not a fault for that contract's type, has its reason in `UncheckedReasons`, beside the test, in the rule method for its kind of break.
- A thread-safety break is checked by the thread-safety contracts only.

[build-and-test.md](../../design/reference/build-and-test.md#contract-coverage), "Contract coverage", lists the contracts it covers, and what was measured for the thread-safety breaks.

## Concurrency tests

- **Bounded in time.** A concurrency or deadlock test never waits without a limit. It runs its threads with `com.soliscode.test.safety.ContentionCoordinator`, whose workers have a start and a finish timeout and run on daemon threads, and it is annotated `@ConcurrencyTest`, which adds a JUnit timeout. A deadlock then fails the test instead of hanging the build.
- **No `Thread.sleep`.** Checkstyle fails a call in `src/test`. The contracts, the thread-safety contracts among them, are in `src/main`, where Checkstyle does not look for it: there the review does. Wait for a condition, with a limit, instead.
- **A race measured, not hoped for.** A test that relies on a race is measured: how often it catches the break it is for, in single runs, idle and under load. build-and-test.md records the rates, and the task that adds the test records its own.

[build-and-test.md](../../design/reference/build-and-test.md#bounds), "Bounds", says how the coordinator bounds its runs.

## Coverage

The coverage gate fails the build when a module's line coverage is below its `jacoco.minimum.coverage`, or when any method misses more than 3 of its complexity ([the parent's coverage page](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.14/docs/guide/03-gates/coverage.md)).

SolisCode Test's minimum is **0.70**, set in `pom.xml`.

- Don't lower the minimum to make a change pass. Changing it is the maintainer's decision.
- A new or changed class should not lower the coverage.
- Coverage is a guide, not the goal. A meaningful assertion is worth more than a line run without one.
- Never pass `-DskipTests` or `-Djacoco.skip=true` to a build you rely on: without tests, the coverage check passes without checking ([Ground rules](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.14/docs/guide/07-ground-rules.md#dont-skip-the-tests-or-the-coverage-check)).

## Test organisation

- Tests are in `src/test/java`, in the same package as the class they test.
- A test class ends in `Test`, and only a test class does: Surefire runs no other. An abstract base class of tests starts with `Abstract`. A helper that is not a test does not end in `Test` (`DynamicContract`, `ContractRuns`).
- A test method's name is plain camelCase, with no underscores. Checkstyle fails one with an underscore. Put the readable sentence in `@DisplayName`, on every test method.
- A collection is tested with the contracts: implement the contract interfaces for it, with a provider (`WithXxx`, `XxxSupport`), rather than writing the same tests again.
- A slow test is tagged with the annotations in `com.soliscode.test.annotations` ([Running tests](../getting-started/running-tests.md#test-tags)).
- JUnit 6 only, at the version the parent manages. There is no AssertJ and no mocking library: the assertions are JUnit's and this library's own.
- Fixtures and test data: `src/test/resources/`, in the package of the test that reads them.

```java
@Test
@DisplayName("toArray() returns an empty array for an empty collection")
void toArrayWhenEmptyReturnsEmptyArray() {
    assertArrayEquals(new Object[0], new ArrayList<>().toArray());
}
```

## Writing good tests

- **One behaviour per test**, named after that behaviour.
- **Deterministic:** no reliance on the clock, the network or an unseeded random source.
- **Fast:** a unit test takes milliseconds. Tag a slower one `@Slow` or `@VerySlow`.
- **Isolated:** no mutable state shared between tests.
- For each method a contract tests, cover the empty and non-empty cases, a null argument where nulls are and are not permitted, duplicates where they are and are not permitted, an argument of an incompatible type, and an unsupported method.

## Mocks and test doubles

There is no mocking library. A fault is made with a breakable, set with one of its `Break` constants; an object that hides all but one interface comes from `com.soliscode.test.interfaces`; values that are equal but not identical are `UncachedInteger` and `UncachedString`. Otherwise prefer a real object or a small fake.

## Flaky tests

A flaky test is a bug. Open an issue for it, and fix it before the next release. Don't disable it without an issue that says why.

## See also

- [Running tests](../getting-started/running-tests.md)
- [Coding standards](coding-standards.md)
