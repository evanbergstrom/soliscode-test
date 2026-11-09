# SolisCode Test

[![License: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](./LICENSE.txt)

SolisCode Test is a Java library for JUnit 6 with three goals:

1. **Test methods JUnit lacks.** Assertions that are useful in many tests but missing from JUnit: on collections,
   strings, ordering, exceptions, actions on elements and deadlocks.
2. **Test suites for the JDK's interfaces.** A ready-made suite of tests (a *contract*) for each JDK interface that a
   suite is useful for, which tests any class that implements it. Every collection interface gets one; other interfaces
   are added in order of how commonly classes implement them.
3. **Thread-safety testing.** Utilities for running code on many threads at once and checking it for deadlocks, and
   suites that test what the JDK says is thread-safe.

This library is designed to work exclusively with JUnit 6, and requires it at compile time. It is meant to be used in
the test scope, unless it is being used by another library that is expanding its functionality to interfaces outside
the JDK.

This library provides support for:\
- **Contracts** - Classes that test the functionality of individual methods in the standard interfaces.
- **Interface Restriction** - Wrapper classes that restrict the methods of an object to only those in an interface.
- **Assertions** - Assertions JUnit lacks: on collections, strings, ordering, exceptions, actions on elements and
  deadlocks.
- **Concurrency support** - `ContentionCoordinator`, which runs operations on one object from many threads at once,
  and `AssertNoDeadlocks`.
- **Providers** - Provider classes that produce elements for collection tests.
- **Breakables** - Implementations of the standard collection classes that can be programmatically broken in various ways.

> [!NOTE]
> This library is a work in progress. It has contracts for these interfaces:
> - `Object` (`equals`, `hashCode`, `toString`) and `Serializable`
> - `Iterable` (with `Iterator` and `Spliterator`)
>   - `Collection`, and thread-safe collections
>     - `SequencedCollection`
>       - `List`
>     - `Set`
>       - `SequencedSet`
>         - `SortedSet`
>           - `NavigableSet`
>     - `Queue`
>       - `Deque`
> - `Map`
>   - `SortedMap`
>     - `NavigableMap`
>   - `SequencedMap`
>
> It has breakable implementations, but no contracts yet, for `ConcurrentMap`,
> `ConcurrentNavigableMap`, `BlockingQueue`, `BlockingDeque` and `TransferQueue`.

**Docs:** https://evanbergstrom.github.io/soliscode-test/

**API Reference:** https://evanbergstrom.github.io/soliscode-test/apidocs

---
## Requirements

- Java **25+**
- Build: Maven

---
## Installation

### Maven
Releases are installed in the local Maven repository (`~/.m2`) only.

```xml
<dependency>
  <groupId>com.soliscode</groupId>
  <artifactId>soliscode-test</artifactId>
  <version>0.1.1</version>
  <scope>test</scope>
</dependency>
```

---
## Building and testing

`scripts/verify.sh` runs the full build from clean; `mvn -B clean verify` is the same build. How to build and test,
the gates, the test tags and the contract coverage test are in
[docs/design/reference/build-and-test.md](docs/design/reference/build-and-test.md).
