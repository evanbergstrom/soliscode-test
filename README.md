# SolisCode Test

[![Maven Central](https://img.shields.io/maven-central/v/soliscode/soliscode-test.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/GROUP_ID/ARTIFACT_ID)
[![javadoc](https://javadoc.io/badge2/org.soliscode/soliscode-test/javadoc.svg)](https://javadoc.io/doc/org.soliscode/soliscode-test)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](./LICENSE.txt)

SolisCode Test is a Java library that provides tests of classes that implement the standard 
interfaces in the JDK. this includes all the collection classes, numeric classes, as well as
other interfaces. 

This library is designed to work exclusively with JUnit5, and requires it at compile time. It is meant to be used in
the test scope, unless it is being used by another library that is expanding its functionality to interfaces outside
the JDK.

This library provides support for:\
- **Contracts** - Classes that test the functionality of individual methods in the standard interfaces.
- **Interface Restriction** - Wrapper classes that restrict the methods of an object to only those in an interface.
- **Assertions** - Assertions that help test collection methods.
- **Providers** - Provider classes that produce elements for collection tests.
- **Breakables** - Implementations of the standard collection classes that can be programmatically broken in various ways.

> [!NOTE]
> This library is currently a work in progress. It currently supports testing for the following
> interfaces:
> - Object 
>   - Iterator 
>   - Iterable 
>     - Collection 
>       - SequencedCollection 
>         - List 
>     - Set
>       - SortedSet
>         - NavigableSet
> 
> Support for the following interfaces is planned:
> - Map
>   - SortedMap
>     - NavigableMap
>   - ConcurrentMap
>     - ConcurrentNavigableMap 
> - Queue
>   - BlockingQueue
>     - TransferQueue
>   - Deque
>     - BlockingDeque
> - Comparable

**Docs:** https://evanbergstrom.github.io/soliscode-test/

**API Reference:** https://evanbergstrom.github.io/soliscode-test/apidocs

---
## Requirements

- Java **23+**
- Build: Maven

---
## Installation

### Maven
```xml
<dependency>
  <groupId>org.soliscode</groupId>
  <artifactId>soliscode</artifactId>
  <version>1.0.5</version>
  <scope>test</scope>
</dependency>

Full dependency information available [here][https://evanbergstrom.github.io/soliscode-test/dependency-info.html]

---
## Building and testing

```bash
mvn verify                 # the default build: every test except those tagged extremely-slow
mvn verify -Pall-tests     # every test
mvn test -Dgroups=concurrency                 # only the tests with a tag (a JUnit tag expression)
mvn test -Dtest.excludedGroups=very-slow      # skip other tags instead of the default's
```

### Test tags

Tests are tagged with the annotations in `org.soliscode.test.annotations`, which add JUnit tags. Surefire skips the
tags in the `test.excludedGroups` property (`pom.xml`); the `all-tests` profile clears it.

| Annotation | Tag | Meaning | Default build |
| :--- | :--- | :--- | :--- |
| `@Slow` | `slow` | 100 ms – 1 s | runs |
| `@VerySlow` | `very-slow` | 1 s – 10 s | runs |
| `@ExtremelySlow` | `extremely-slow` | over 10 s | **skipped** (`-Pall-tests` runs it) |
| `@ConcurrencyTest` | `concurrency`, `nondeterministic` | thread-safety test, repeated, with a timeout | runs |
| `@Nondeterministic` | `nondeterministic` | may pass or fail differently between runs | runs |

The default was chosen by the user on soliscode/soliscode-test#5 (2026-11-07), so that the thread-safety contracts run
on every build. No test is tagged `extremely-slow` yet.

**Time.** Measured on 2026-11-07 (Intel i9-8950HK, 12 threads, JDK 25), the default `mvn clean verify` runs 6,171
tests and takes about 2½ minutes in all (coverage, Checkstyle and Javadoc included). The tests themselves take about
110 s without coverage: the 127 tagged tests about 95 s, most of it the thread-safety part of
`ContractCoverageTest` (`very-slow`), and the 5,950 untagged tests about 13 s.

### Contract coverage

`ContractCoverageTest` shows that each contract can pass and can fail. It runs every test method of the contract
against a JDK implementation and an unbroken breakable implementation, which must pass, and then against the
breakable implementation with each of its breaks, one at a time, which must fail. A break it leaves out is one that
is not a fault for that contract's type, or that no test could reliably see; `uncheckedReason` in the test gives the
reason for each.

**Bounds.** Concurrency and deadlock tests do not sleep and do not wait without a limit.
`ContentionCoordinator` gives its workers a start and a finish timeout. It stops waiting for workers that outlive
the finish timeout, and its threads are daemon threads, so a deadlock fails the test instead of hanging the build.
`@ConcurrencyTest` adds a JUnit timeout.
