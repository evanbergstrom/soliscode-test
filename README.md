# SolisCode Test

[![Licence: Apache-2.0](https://img.shields.io/badge/licence-Apache--2.0-blue)](LICENSE.txt)

**SolisCode Test** is a Java library for people who write JUnit 6 tests that gives them ready-made test suites for the JDK's interfaces, the assertions JUnit lacks, and support for testing thread safety.

```java
class ArrayListReadmeTest extends AbstractTest
        implements ListContract<ArrayList<Integer>, Integer>, WithArrayList<Integer>, WithIntegerElement {
}
```

That class runs the 139 tests of the `List` contract against `ArrayList`. Put your own list class in its place, and they test yours.

## Why SolisCode Test?

The library has three goals:

1. **Test methods JUnit lacks.** Assertions that are useful in many tests but missing from JUnit: on collections, strings, ordering, exceptions, actions on elements and deadlocks.
2. **Test suites for the JDK's interfaces.** A ready-made suite of tests (a *contract*) for each JDK interface that a suite is useful for, which tests any class that implements it. Every collection interface gets one; other interfaces are added in order of how commonly classes implement them.
3. **Thread-safety testing.** Utilities for running code on many threads at once and checking it for deadlocks, and suites that test what the JDK says is thread-safe.

What you get:

- **Contracts**: implement one interface in a test class, and it tests your class against what the JDK's documentation of that interface requires, method by method, with no test methods of your own.
- **Contracts that can fail**: each contract passes against a JDK class and fails against a *breakable*, an implementation broken on purpose in one way for each fault it must catch. The breakables are yours to use too, to test a test of your own.
- **Assertions**: on collections, strings, ordering, exceptions, actions on elements and deadlocks.
- **Concurrency support**: `ContentionCoordinator`, which runs operations on one object from many threads at once, bounded in time, and `assertNoDeadlocks`.
- **Providers and helpers**: providers of the instances and elements a contract tests, and classes that implement only one interface, to test code against nothing more than that interface.

It has contracts for these interfaces:
- `Object` (`equals`, `hashCode`, `toString`) and `Serializable`
- `Comparable`, `Comparator` and `CharSequence`
- `Future` and `ExecutorService`, and their thread-safe forms
- `AutoCloseable` and `Closeable`
- `Iterable`, `Iterator`, `ListIterator`, weakly consistent iterators, and `Spliterator` with `Spliterator.OfInt`, `OfLong` and `OfDouble`
- `Collection` and thread-safe collections; `SequencedCollection` and its thread-safe form, and `List`; `Set`, `SequencedSet`, `SortedSet` and `NavigableSet`
- `Queue` and `Deque`; `BlockingQueue`, `BlockingDeque` and `TransferQueue`, and their thread-safe forms
- `Map`, `SequencedMap`, `SortedMap` and `NavigableMap`; `Map.Entry`; `ConcurrentMap`, thread-safe concurrent maps and `ConcurrentNavigableMap`

The library is a work in progress: more interfaces get contracts in later releases.

**Not a good fit if** your tests run on JUnit 4 or 5, or on another test framework: SolisCode Test works only with JUnit 6, and needs it at compile time.

## Installation

**You need:** Java 25 and Maven 3.9 or later, on macOS or Linux.

Before 1.0.0, no SolisCode release is deployed to a remote repository. You build it from its release commit (`RELEASES.md` names it; the tags are not on GitLab yet) and install it into your local Maven repository, after the SolisCode parent it builds on. The [Installation guide](docs/user-guide/getting-started/installation.md) gives the steps. Then add it to your POM, in the `test` scope (a library that adds contracts of its own for interfaces outside the JDK uses it in the `compile` scope instead):

```xml
<dependency>
  <groupId>com.soliscode</groupId>
  <artifactId>soliscode-test</artifactId>
  <version>0.1.6</version>
  <scope>test</scope>
</dependency>
```

## Quick start

Put this test class in `src/test/java`, in a package of yours:

```java
import java.util.ArrayList;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.list.ListContract;
import com.soliscode.test.contract.support.WithArrayList;
import com.soliscode.test.contract.support.WithIntegerElement;

class ArrayListReadmeTest extends AbstractTest
        implements ListContract<ArrayList<Integer>, Integer>, WithArrayList<Integer>, WithIntegerElement {
}
```

Run it with `mvn -B -Dtest=ArrayListReadmeTest test`. It prints:

```text
[INFO] Tests run: 139, Failures: 0, Errors: 0, Skipped: 0
```

`ListContract` holds the tests, as default methods JUnit runs in any class that implements it. `WithArrayList` tells it to make `ArrayList`s, and `WithIntegerElement` gives it `Integer` elements to put in them. This library's own build compiles and runs the class: [`ArrayListReadmeTest.java`](src/test/java/com/soliscode/test/guide/ArrayListReadmeTest.java).

Next: work through the [First steps tutorial](docs/user-guide/getting-started/first-steps.md), which runs the contract against a list class of your own and has it catch a fault.

## Documentation

| Guide | For |
|---|---|
| [User guide](docs/user-guide/README.md) | Installing, using and configuring SolisCode Test |
| [API reference](docs/user-guide/reference/api.md) | The packages and contracts, and how to build the Javadoc of every public class and method, which is not published yet |
| [Developer guide](docs/developer-guide/README.md) | Building from source, how the code is laid out, contributing |
| [Releases](RELEASES.md) | What changed in each release |

## Contributing

Start with [CONTRIBUTING.md](CONTRIBUTING.md). Every outside contribution needs a signed [contributor licence agreement](CLA.md).

## Getting help

- **Questions, bug reports and feature requests:** [open an issue](https://gitlab.com/soliscode/soliscode-test/-/issues). There is no forum or chat.
- **Security problems:** don't describe them in a public issue. Open a [confidential issue](docs/developer-guide/maintainers/security.md) instead.

## Licence

SolisCode Test is licensed under the [Apache License 2.0](LICENSE.txt) (`Apache-2.0`). See [NOTICE](NOTICE) for the copyright notice.

## See also

- [Documentation home](docs/README.md)
- [Upgrading](docs/user-guide/upgrading.md)
