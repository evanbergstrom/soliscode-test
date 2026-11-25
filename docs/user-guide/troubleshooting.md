# Troubleshooting and FAQ

## Before you start

1. Check which version you use: the `<version>` of `com.soliscode:soliscode-test` in your POM, or `mvn -B dependency:tree -Dincludes=com.soliscode`.
2. Check that the version is installed: `ls ~/.m2/repository/com/soliscode/soliscode-test/`.
3. Search the [existing issues](https://gitlab.com/soliscode/soliscode-test/-/issues?state=all).

## Installation

### Maven cannot find `com.soliscode:soliscode-test:0.1.6`

**Cause:** before 1.0.0, no SolisCode release is in a remote repository, so Maven finds it only in your local repository, where you install it yourself.
**Fix:** install it as [Installation](getting-started/installation.md) says, and use the version you installed.

### Maven cannot find `com.soliscode:soliscode-parent` or `com.soliscode:soliscode-performance-api`

**Cause:** the build of SolisCode Test needs the SolisCode parent it names, installed first; the parent's install also installs `soliscode-performance-api`, which SolisCode Test depends on.
**Fix:** install the parent at the version the release names (0.1.12 for SolisCode Test 0.1.6) as step 1 of [Installation](getting-started/installation.md#1-the-soliscode-parent) says, then build SolisCode Test again.

### The build fails while you install it

**Cause:** the install runs every test and gate. On a JDK older than 25, or Maven older than 3.9, the build stops at once.
**Fix:** check `java -version` and `mvn -version`. For any other failure, the [parent's troubleshooting page](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/06-troubleshooting.md) explains each gate's message.

### `UnsupportedClassVersionError` when your tests run

**Cause:** SolisCode Test is compiled for Java 25, and your tests run on an older JVM.
**Fix:** run Maven, and Surefire's forked JVM, on Java 25.

## Runtime

### A contract fails on a method your class does not support

**Cause:** the contract assumes every optional method is supported, and your class throws `UnsupportedOperationException`.
**Fix:** call `doesNotSupportMethod(...)` in your test class's constructor for each such method, or `doesNotSupportModification()` for a collection that cannot be changed ([Configuration](reference/configuration.md#optional-methods)).

### Tests about `null` fail with `NullPointerException`

**Cause:** your class refuses `null`, and the contract was not told.
**Fix:** implement `DoesNotPermitNulls` for a collection, or override `permitNullKeys()` and `permitNullValues()` to return `false` for a map.

### `equals` and `hashCode` tests fail for a queue or a synchronized wrapper

**Cause:** the class keeps `Object`'s identity equality, as most queues and `Collections.synchronizedCollection` do.
**Fix:** override `equalsComparesValues()` to return `false`, or call `doesNotSupportMethod(ObjectMethods.EQUALS)` and `doesNotSupportMethod(ObjectMethods.HASH_CODE)`.

### A JUnit test class finds no tests

**Cause:** the class implements no contract, or it is not run as a test class: it is abstract, it is a non-static inner class that is not `@Nested`, or its name is not one Maven's Surefire includes by default (`*Test`, `*Tests`, `Test*`, `*TestCase`).
**Fix:** check that the class is concrete, implements the contract, extends `AbstractTest`, and is named `…Test`.

### A thread-safety test times out

**Cause:** a deadlock, an operation that waits for another thread that never comes, or operations too slow for the iterations; a contract's `clear` on an `AbstractList` subclass that does not override it is the last kind.
**Fix:** see [ContentionCoordinator timeouts](reference/errors.md#contentioncoordinator-timeouts).

## Performance

### The test run is slow

**Cause:** a contract runs many tests, each on new instances, and the thread-safety contracts run each of theirs 5 times, under contention.
**Fix:** run the thread-safety contracts in their own test classes, and leave the slow tags out of a quick build: `mvn -B -DexcludedGroups=very-slow test` ([Test tags](reference/configuration.md#test-tags)).

## Frequently asked questions

### Do I need to declare JUnit?

No. SolisCode Test depends on `junit-jupiter`, with its engine, at compile scope, so your tests get JUnit 6 from it. Declare it yourself only to pin another version, which must still be JUnit 6.

### Can I run only some of a contract's tests?

Yes: implement the method contract alone (`contract.list.GetContract`, say) rather than the interface's contract, or select methods with Surefire's `-Dtest='YourTest#get*'`.

### Does a passing contract prove my class correct?

It shows your class passes every test of the interface's contract, and each of those tests is known to catch the faults of the [breakable](concepts/breakables.md) it was checked against. It does not test what the interface leaves to your class, such as its performance or its own methods.

## Still stuck?

[Open an issue](https://gitlab.com/soliscode/soliscode-test/-/issues/new). Include the version you use, your Java version and operating system, the code that fails, and the whole error output. For a security problem, make the issue confidential.

## See also

- [Error reference](reference/errors.md)
- [Installation](getting-started/installation.md)
