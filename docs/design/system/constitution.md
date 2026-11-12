# Constitution

What soliscode-test is, what it is for, and the rules every change keeps. Read it, with [architecture.md](architecture.md), before writing code.

## 1. How to read

Each part of the system documents is marked:
- **Built**: the code does it today.
- **Partly built**: some of it exists; the text says which part.
- **Designed**: intended, not yet in the code. The issue that builds it is named.

A rule or decision that no code builds names the task that recorded it.

When a document and the code disagree, the code says what exists. A change that finds a document wrong fixes the document in the same task, or says why not.

## 2. What the system is

Built. A Java library for testing implementations of JDK interfaces with JUnit 6. One Maven module, `com.soliscode:soliscode-test`, with `com.soliscode:soliscode-parent:0.1.2` as its parent (#9, #14); its packages are `com.soliscode.test.*` (#9). It provides:

| Part | What |
|------|------|
| Contracts | Interfaces of `default` `@Test` methods, one per method of a JDK interface and one per interface that extends them. A test class implements a contract and supplies a provider of the class under test |
| Breakables | An implementation of each interface whose behaviour can be broken on purpose: each break is a `Break` constant that turns on one fault. Most are built with builders; `BreakableIterator`, `BreakableListIterator`, the spliterators and `BreakableMapEntry` (#39) are made with their constructors, over a delegate |
| Providers | Instances of the class under test, and elements for a collection under test |
| Assertions | Assertions beyond JUnit's: collections, strings, actions on elements, ordering, exceptions, deadlocks |
| Concurrency support | `ContentionCoordinator`, which runs operations on one subject from many threads at once, and `AssertNoDeadlocks` |
| Interface narrowing | Wrappers that hide every method but one interface's (`IterableOnly`, `CollectionOnly`, `NumberOnly`, `ObjectOnly`) |
| Test tags | Annotations that tag a test slow, very slow, extremely slow, concurrency or nondeterministic |

It is a library of test code: its users put it on their test class path. It is for soliscode-common and soliscode-quant, the libraries of the split of the quant libraries (the user's decision of 2026-11-05, soliscode/soliscode-quant#6). soliscode-quant's own copy, `quant-test`, diverged from this project; its fixes were brought in by #2. Its first release under these coordinates is `0.1.1` (#3); [RELEASES.md](../../../RELEASES.md) has the version scheme and each release.

It replaces `org.soliscode:soliscode-test` 1.0.x (Java 23, JUnit 5, semantic-release, a CI deploy), whose tags `v1.0.0`–`v1.0.14` stay (#3).

## 3. Goals

Each goal has a "strong means" criterion: a test or a measurement that shows it met. Section 9 gives each criterion's state.

### 3.1 What the library is for

The user's three goals for the library (2026-11-08, #29):

1. **Test methods that JUnit lacks.** Assertions that are generally useful in tests but missing from JUnit: on collections, strings, ordering, exceptions, actions on elements and deadlocks.
   Strong means: each assertion has tests that show it passing and failing, and that check its failure message. Partly built: the collection, ordering, exception, action and deadlock assertions have tests that pass and fail and check their messages (the collection assertions' since #15 and #16). `AssertStringContains`, `AssertStringContainsInOrder` and `ArrayAssertions` have no tests of their own, and `AssertContains`'s message overloads are not checked on failure: #49.
2. **Test suites for the JDK's interfaces.** A contract for each JDK interface a test suite is useful for, which tests any class that implements it.
   Which interfaces: every collection interface, that is `Iterable`, and `Collection`, `Map` and each interface of `java.util` and `java.util.concurrent` that extends them; and the iterator, spliterator, entry and stream types they return (`Iterator`, `ListIterator`, `Spliterator`, `Map.Entry`, `Stream`; the primitive streams are not chosen). Beyond the collections, an interface can get a suite when its Javadoc states behaviour a test can check beyond its method signatures, in order of how commonly classes implement it. An interface whose Javadoc states nothing more than its signatures gets none: the `java.util.function` interfaces, `Runnable`, `RandomAccess`. Which interfaces are chosen is the user's decision, and each chosen interface has a task.
   Strong means: each chosen interface has a contract that `ContractCoverageTest` checks (goal 4). Partly built: section 4.1 lists the contracts there are (`Iterator` and `ListIterator` since #37, `Spliterator` and its primitive kinds since #38, weakly consistent iterators since #55, `Map.Entry` since #39), and section 4.2 the tasks for the rest.
3. **Thread-safety testing.** Utilities for testing code under concurrent use, and test suites for what the JDK says is thread-safe.
   Strong means: `ContentionCoordinator` and `AssertNoDeadlocks` have tests; and each chosen interface whose Javadoc states thread safety or atomicity (the `java.util.concurrent` collections, `Lock`, `ExecutorService`, `Future`) has a thread-safety contract, bounded in time and with no `Thread.sleep`, that passes against a JDK class and fails for a thread-unsafe break. Partly built: the utilities have tests (`ContentionCoordinatorTest`, `AssertNoDeadlocksTest`), and #12 fixes a `ContentionCoordinator` fault; `ThreadSafeCollectionContract` is built (#5, #6; `CLEAR_IS_NOT_THREAD_SAFE` waits on #7), `ThreadSafeBlockingQueueContract` (#32), `ThreadSafeBlockingDequeContract` (#33), `ThreadSafeTransferQueueContract` (#34) and `ThreadSafeConcurrentMapContract` (#35), which also covers `ConcurrentNavigableMap` and its views (#36); the rest by #44–#46.

### 3.2 Quality goals

4. **Each contract can pass and can fail.** A contract that never fails tests nothing.
   Strong means: each interface contract passes against a JDK class and against its unbroken breakable, and fails against the breakable with each of its breaks, applied one at a time. `ContractCoverageTest` checks this (#6). A break it leaves unchecked has a reason in `uncheckedReason`, which says why no test could see it or which other contract checks it. Partly built: see section 4.
5. **Usable by any SolisCode library.** Strong means: its only compile-scope dependencies are JUnit 6 (Jupiter API, params and engine, Platform commons), OpenTest4J and JSpecify, at the parent's versions; it depends on no other SolisCode library; and a release is installed into `~/.m2` as `com.soliscode:soliscode-test:0.1.N`. The dependencies: Built (#9). The release: Built (#3): `0.1.1`, tagged `v0.1.1`, made by `release.sh`.
6. **The same gates as every SolisCode library.** Strong means: every gate the parent gives (section 7) runs and passes, with no gate turned off or lowered. Built (#10).

## 4. Scope

### 4.1 Built
Contracts for `Object` (`equals`, `hashCode`, `toString`, `Serializable`), `Iterable`, `Collection`, `SequencedCollection`, `List`, `Set`, `SequencedSet`, `SortedSet` (#30), `NavigableSet` (#31), `Queue`, `Deque`, `BlockingQueue` (#32), `BlockingDeque` (#33), `TransferQueue` (#34), `Map`, `SequencedMap`, `SortedMap`, `NavigableMap`, `ConcurrentMap` (#35) and `ConcurrentNavigableMap` (#36), `Iterator` and `ListIterator` (#37), weakly consistent iterators (`WeaklyConsistentIteratorContract`, #55), `Spliterator`, `Spliterator.OfInt`, `OfLong` and `OfDouble` (#38), `Map.Entry` (`MapEntryContract`, #39), and `ThreadSafeCollectionContract` for collections that claim thread safety, `ThreadSafeBlockingQueueContract` for blocking queues (#32), `ThreadSafeBlockingDequeContract` for blocking deques (#33), `ThreadSafeTransferQueueContract` for transfer queues (#34) and `ThreadSafeConcurrentMapContract` for concurrent maps (#35) and concurrent navigable maps (#36); `IntegerContract` for integer `Number` types. Breakables for each of those collection and map interfaces but `SequencedMap` (its contract runs against `BreakableSortedMap`), for `Iterator` and `ListIterator` (`BreakableIterator`, `BreakableListIterator`, #37), for `Spliterator` and its primitive kinds (`BreakableSpliterator`, `BreakableSpliteratorOfInt`, `OfLong`, `OfDouble`, #38), for `Map.Entry` (`BreakableMapEntry`, #39), and for `Object` (`BreakableObject`), which has no contract yet. Iterator providers (`IteratorProvider`, `IteratorSource`, `IteratorProviders`, #37) make the iterator under test over a source of given elements, and spliterator providers (`SpliteratorProvider`, `SpliteratorSource`, `SpliteratorProviders`, #38) the spliterator under test, and map-entry providers (`MapEntryProvider`, `MapEntrySource`, `MapEntryProviders`, #39) the entry under test, with the map behind it where there is one. `BreakableTransferQueue` built with a capacity or fairness has a bounded `TransferQueue` store of its own (#34), since the JDK has none. The providers, assertions, concurrency support, interface narrowing and test tags of section 2. By #1, #2, #4, #5 and #6.

`ContractCoverageTest` covers 33 contracts: `Iterable`, `Iterator`, `ListIterator`, `WeaklyConsistentIterator`, `Spliterator`, `Spliterator.OfInt`, `Spliterator.OfLong`, `Spliterator.OfDouble`, `MapEntry`, `Collection`, `SequencedCollection`, `List`, `Set`, `SequencedSet`, `SortedSet`, `NavigableSet`, `Queue`, `Deque`, `BlockingQueue`, `BlockingDeque`, `TransferQueue`, `Map`, `SequencedMap`, `SortedMap`, `NavigableMap`, `ConcurrentMap`, `ConcurrentNavigableMap`, `ThreadSafeCollection`, `ThreadSafeBlockingQueue`, `ThreadSafeBlockingDeque`, `ThreadSafeTransferQueue`, `ThreadSafeConcurrentMap` and `Serializable`. `ObjectContract` and `IntegerContract` are not in it. `SerializableContract` is run against a chosen few breaks, not each.

Built by #55: the iterators of the `java.util.concurrent` collections are tested as weakly consistent, after a change to their source during a traversal (no `ConcurrentModificationException`, no element returned twice, every element held throughout returned, only elements held returned). `WeaklyConsistentIteratorContract` tests an iterator by itself; `BlockingQueueContract` (`iterator()`), `BlockingDequeContract` (`descendingIterator()`), `ConcurrentMapContract` (the iterators of `keySet()`, `values()` and `entrySet()`) and `ConcurrentNavigableMapContract` (both iterators of each key set) run the same checks. Only `ConcurrentNavigableMap` promises it in its own Javadoc; for the others the contracts read the `java.util.concurrent` package summary more strictly than their interface, by default, and a test class opts out with `iteratorsAreWeaklyConsistent()` (the user's decision on #55). `BreakableIterator`'s `ITERATOR_IS_FAIL_FAST`, `ITERATOR_RETURNS_AN_ELEMENT_TWICE_AFTER_A_CHANGE` and `ITERATOR_SKIPS_AN_ELEMENT_AFTER_A_CHANGE` show they can fail. Spliterators are not tested for it.

Built by #8 and #51: the `preventNulls` wrappers refuse `null` through their bulk methods, and the `List` wrapper through its queries, list iterators, sub-lists and `replaceAll`.

Built by #9: the `com.soliscode` coordinates and packages, the shared parent, JSpecify nullness in every package, ErrorProne and NullAway. Built by #10: Checkstyle passes on main and test code, every test method has a plain camelCase name and a `@DisplayName`, and the coverage check passes.

### 4.2 Designed
- `CLEAR_IS_NOT_THREAD_SAFE` caught reliably, so that `ContractCoverageTest` can check it: #7.
- Fixes: `ContentionCoordinator` yields on half the iterations, not every `yieldInterval` (#12).
- Breakables and contracts typed with nullable elements (#13).

Contracts for the interfaces chosen under goal 2 (the user's decision, 2026-11-08, #29), each with a task:
- The type the collection interfaces return that has none: `Stream` (#50).
- Other interfaces, in order of how commonly classes implement them: `Comparable` (#40), `AutoCloseable` and `Closeable` (#41), `Comparator` (#42), `CharSequence` (#43), `Future` (#44), `ExecutorService` (#45), `Lock` and `ReadWriteLock` (#46), `Collector` (#47), `Appendable` (#48).

### 4.3 Not goals
- A test runner or engine of its own: the contracts run on JUnit's.
- Contracts for interfaces outside the JDK. A library that adds its own (soliscode-common, for example) builds them on this one's support interfaces.
- A deploy to a remote repository, or CI. Releases are local, and the gates run on the developer's machine (the user's decision, soliscode/soliscode-quant#6; the pipeline was removed by #11).

## 5. Constraints

- Java 25 and Maven 3.9 or later (Built, #9; the parent's enforcer refuses older).
- Dependencies come from Maven Central only. Nothing depends on a path outside this repository or on another local project, except the parent, read from `~/.m2`.
- JUnit, OpenTest4J and JSpecify are at compile scope, because the library's main code is test code its users run. A user declares it at test scope.

## 6. Rules every change keeps

- **A contract is shown to fail.** A new contract, or a new test method in one, is run by `ContractCoverageTest` against a JDK class and against each break of its breakable. A break that cannot be checked gets a reason in `uncheckedReason`.
- **A break is one fault.** Each `Break` targets one violation of the interface's specification, not general brokenness. It is a `public static final` constant, named for what it breaks (`ITERATOR_SKIPS_FIRST_ELEMENT`, `ADD_IS_NOT_THREAD_SAFE`), declared in the first breakable class that supports it, and its Javadoc says what normal behaviour it overrides and how.
- **Contracts respect what the class supports.** A test method of an optional method first checks `supportsMethod(...)` with the method's enum constant (`CollectionMethods.ADD`, for example). A collection contract reads `permitNulls()`, `permitDuplicates()` and `permitIncompatibleTypes()`, and, where nulls are refused, tests that a `NullPointerException` is thrown.
- **Names follow the parts.** An interface contract is named after its interface (`ListContract`), a method contract after its method (`AddContract`), both with the suffix `Contract`, in the package `contract.<interface in lower case>`, one level under `contract`. A breakable is `Breakable` and the interface's name. An interface-narrowing class is the interface's name and `Only`. An assertion class is `Assert` and what it asserts, with a utility class that aggregates its package's assertions (`Assertions`, `CollectionAssertions`, `AssertActions`).
- **Test methods are named as JUnit's are.** A plain camelCase name, with the readable sentence in `@DisplayName` (the user's decision on #3). Built (#10).
- **Nullness is stated.** Every main package is `@NullMarked`, and `@Nullable` (JSpecify, as a type annotation) marks what may be null (#9).
- **A finding is fixed, or suppressed on purpose.** Where the code does on purpose what an ErrorProne or NullAway check warns against (a `LinkedList` kept because it is the one JDK deque that permits null, a contract that passes null to a JDK method to test it), it gets one `@SuppressWarnings` per finding, on the smallest declaration, with a one-line reason; never a workaround that hides the finding (the user's decision, #9). [java-conventions.md](../reference/java-conventions.md) lists the kinds.
- **Public API recorded.** A change a user would notice (a renamed contract method, a removed break, a changed provider) goes under `## Unreleased` in `RELEASES.md`, a break marked as one (#10).
- **Tests.** Every public behaviour has a test. A bug fix comes with a test that failed before it.

## 7. Gates

Gates fail the build like a compile error; "the tests pass" is no substitute. They come from soliscode-parent; `CLAUDE.md` lists them with the flags never to use, and [build-and-test.md](../reference/build-and-test.md) says how they run. In short:
- **Checkstyle**, on main and test sources, with the parent's rules from soliscode-build-tools, under 0.1.2's rule for test-method names (soliscode/soliscode-parent#2): Built (#10). It passes, with no finding and no suppression; it had 2,958 findings on 2026-11-08 (#14).
- **Javadoc** with `doclint` all, failing on warnings: Built; it passes.
- **ErrorProne and NullAway** at ERROR, on main sources, with JSpecify nullness: Built (#9).
- **JaCoCo line coverage**, at least 0.70 (`jacoco.minimum.coverage`): Built; measured 0.8985 on 2026-11-08 (#10).
- **Maven enforcer**: Built (#9).
- **Licences**: every dependency under an OSI-approved licence, and none at `system` scope: Built (#14).

## 8. Anti-patterns

- A suppression with no reason, one that covers more than its finding, or a workaround that hides a finding instead.
- A skipped gate or a weakened rule to make a build pass.
- A contract test method that passes whatever the class under test does.
- A break that changes more than one behaviour, or a break no contract can see and no reason explains.
- A test whose expected value is computed by the code under test.
- A to-do list in a document: work is tracked as GitLab issues (`CLAUDE.md`).
- Describing what is designed as if it were built.

## 9. Success criteria

| Criterion | State |
|-----------|-------|
| Every test passes | Yes (#9) |
| Goal 1: each assertion has tests that show it passing and failing, and check its message | Partly (#15, #16); the string and array assertions and `AssertContains`'s messages are #49 |
| Goal 2: each chosen interface has a contract that `ContractCoverageTest` checks | 26 of the collection interfaces and the types they return, with `Serializable` (#6, #30, #31, #32, #33, #34, #35, #36, #37, #38, #39), and weakly consistent iterators (#55); the rest are #40–#48 and #50 |
| Goal 3: the concurrency utilities have tests, and each chosen thread-safe interface a thread-safety contract that can fail | The utilities have tests (#12 fixes a fault); `ThreadSafeCollectionContract` (#6), `CLEAR_IS_NOT_THREAD_SAFE` waiting on #7; `ThreadSafeBlockingQueueContract` (#32), `ThreadSafeBlockingDequeContract` (#33), `ThreadSafeTransferQueueContract` (#34), `ThreadSafeConcurrentMapContract` (#35), which also covers `ConcurrentNavigableMap` (#36); the rest are #44–#46 |
| Goal 4: each contract in `ContractCoverageTest` passes against a JDK class and its unbroken breakable, and fails for each break, an unchecked break having a reason | Yes for the 33 contracts it covers (#6, #30, #31, #32, #33, #34, #35, #36, #37, #38, #39, #55); `ObjectContract` and `IntegerContract` are not covered, and `CLEAR_IS_NOT_THREAD_SAFE` waits on #7 |
| Goal 5: only JUnit 6, OpenTest4J and JSpecify at compile scope | Yes (#9) |
| Goal 5: `0.1.1` installed into `~/.m2` from a tag | Yes (#3) |
| Goal 6: every gate in section 7 passes | Yes (#10) |
