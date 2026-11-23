# Running tests

## Run everything

```sh
scripts/verify.sh
```

The full build from clean: every test and every gate, with the coverage check last. It records a green build for the tree, which a release needs. Run it before you say a change is done. `mvn -B verify` runs the same build without the record.

A full verify takes about five minutes. Most of the time is `ContractCoverageTest` (`src/test/java/com/soliscode/test/contract/coverage/`), which runs every contract against a JDK class and against a breakable with each of its breaks; its thread-safety part alone takes about 70 seconds. [build-and-test.md](../../design/reference/build-and-test.md#test-tags), "Test tags", has the measured times.

No CI runs the tests. They run on your machine, and the record shows they passed.

## Test suites

| Suite | Command | Location | Needs |
|---|---|---|---|
| Unit, the default selection | `mvn -B test` | `src/test/java` | Nothing |
| Every test, the extremely slow ones included | `mvn -B -Pall-tests verify` | `src/test/java` | Time: no test is tagged `extremely-slow` yet |
| One tag | `mvn -B test -Dgroups=concurrency` | `src/test/java` | A JUnit tag expression |
| Architecture rules | `mvn -B verify` (Surefire's execution `soliscode-architecture`, at `prepare-package`) | the parent's rules, on the main classes | Not run by `mvn test` |

Surefire runs the classes named `*Test`, and no others.

### Test tags

The annotations in `com.soliscode.test.annotations` tag the tests. The default build skips only `extremely-slow`, which the POM's `excludedGroups` property names; `-Pall-tests` clears it, and `-DexcludedGroups=...` replaces it.

| Annotation | Tag | Meaning | Default build |
|---|---|---|---|
| `@Slow` | `slow` | 100 ms to 1 s | runs |
| `@VerySlow` | `very-slow` | 1 s to 10 s | runs |
| `@ExtremelySlow` | `extremely-slow` | over 10 s | skipped |
| `@ConcurrencyTest` | `concurrency`, `nondeterministic` | a thread-safety test, repeated, with a timeout | runs |
| `@Nondeterministic` | `nondeterministic` | may pass or fail differently between runs | runs |

To skip the slow tests while you work, for example: `mvn -B test -DexcludedGroups=slow,very-slow,extremely-slow`.

## Running a subset

```sh
mvn -B -Dtest='ContentionCoordinatorTest' test                                            # one class
mvn -B -Dtest='ContentionCoordinatorTest#executeWithSingleOperationRunsSuccessfully' test  # one method
```

Stop at `test`. At `verify`, the coverage check would measure only the tests you ran. A contract or a breakable is also tested by `ContractCoverageTest`: run it too after you change one.

## Coverage

```sh
mvn -B verify
```

The report is `target/site/jacoco/index.html`. The coverage gate fails the build below the module's `jacoco.minimum.coverage`. What it expects is in [Testing standards](../contributing/testing-standards.md#coverage).

## Measuring test time

Measure how long a test takes with `-Djacoco.skip=true`, since the coverage agent slows the tests:

```sh
mvn -B -Djacoco.skip=true -Dtest='ContentionCoordinatorTest' test
```

Never pass it to a build you rely on: without the agent, the coverage check has nothing to check.

## Debugging tests

- Each test class's report is in `target/surefire-reports/`: the `.txt` file has the failure and its stack trace, and the `.xml` file, in its `system-out`, what the test printed.
- A failure in `ContractCoverageTest` names the contract, the subject and the break. A break that the contract should catch and did not means a contract test is missing or too weak; a correct class that fails means a contract test reads the JDK's documentation wrongly.
- To step through a test, run it with `-Dmaven.surefire.debug` and attach a debugger to port 5005.
- A thread-safety test that fails once in many runs is a finding, not noise: run it again on its own, and report it if it fails again ([Testing standards](../contributing/testing-standards.md#flaky-tests)).

## See also

- [Testing standards](../contributing/testing-standards.md)
- [CI and the Javadoc site](../maintainers/ci-cd.md)
