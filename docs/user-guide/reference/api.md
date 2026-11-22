# API reference

The complete API reference is the Javadoc, generated from the source code. SolisCode Test does not publish it yet: build it from a clone, as [Building the reference locally](#building-the-reference-locally) says, and open `target/site/en/apidocs/index.html`.

## Packages

| Package | Purpose |
|---|---|
| `com.soliscode.test` | `AbstractTest`, which a test class extends to say which optional methods its class supports, and `InterfaceMethod`, `MethodStatus` and `SupportedMethods` |
| `com.soliscode.test.contract` and its packages | The contracts, one package for each interface (`contract.list`, `contract.map`, …), the choices a test class declares (`DoesNotPermitNulls`, `DoesNotPermitDuplicates`, `DoesNotPermitIncompatibleTypes`), and `contract.support`, the interfaces the contracts extend (`WithIntegerElement`, `WithArrayList`, `CollectionProviderSupport` and the others) |
| `com.soliscode.test.methods` | The constants that name each optional method, for `doesNotSupportMethod`: `CollectionMethods`, `ListMethods`, `MapMethods`, `ObjectMethods` and the others |
| `com.soliscode.test.provider` | The providers of instances and elements: `ObjectProvider`, `CollectionProvider`, `MapProvider` and the providers of the other interfaces, and the factories that make them (`Providers`, `CollectionProviders`, `MapProviders`, …) |
| `com.soliscode.test.breakable` | The breakable implementations, their `Break` constants and their builders |
| `com.soliscode.test.assertions` | The assertions JUnit lacks: `Assertions` (ordering, `assertThrowsAnyOf`, `assertThrowsDifferent`, `assertImplementsOnly`, `assertNoDeadlocks`, the string assertions), and `assertions.collection` and `assertions.actions` |
| `com.soliscode.test.safety` | `ContentionCoordinator`, which runs operations from many threads at once |
| `com.soliscode.test.performance` | The performance test library: `AllocationAssertion`, `LatencyRecorder`, `OpenLoopPacer`, `Sweep`, `ProcessTopology`, `StageTaps` and `HeapSoak` |
| `com.soliscode.test.annotations` | The test tags: `@ConcurrencyTest`, `@Nondeterministic`, `@Slow`, `@VerySlow`, `@ExtremelySlow` |
| `com.soliscode.test.util`, `com.soliscode.test.interfaces` | Helpers for tests: `CollectionTestUtils` (`preventNulls`), `IdentitySet`, consumers that count or collect, `UnbrokenView`, and classes that implement only one interface |

## Contracts

Each contract tests one JDK interface, as the JDK's Javadoc documents it, and extends the contracts of the interfaces it extends. Implement the most specific one your class has.

| Contract | Tests | Package |
|---|---|---|
| `ObjectContract` | `equals`, `hashCode`, `toString` of `Object` | `contract.object` |
| `SerializableContract` | `java.io.Serializable` | `contract.object` |
| `IterableContract` | `Iterable` | `contract.iterable` |
| `CollectionContract` | `Collection` | `contract.collection` |
| `SequencedCollectionContract` | `SequencedCollection` | `contract.sequencedcollection` |
| `ListContract` | `List` | `contract.list` |
| `SetContract` | `Set` | `contract.set` |
| `SequencedSetContract` | `SequencedSet` | `contract.sequencedset` |
| `SortedSetContract` | `SortedSet` | `contract.sortedset` |
| `NavigableSetContract` | `NavigableSet` | `contract.navigableset` |
| `QueueContract` | `Queue` | `contract.queue` |
| `DequeContract` | `Deque` | `contract.deque` |
| `BlockingQueueContract` | `java.util.concurrent.BlockingQueue` | `contract.blockingqueue` |
| `BlockingDequeContract` | `java.util.concurrent.BlockingDeque` | `contract.blockingdeque` |
| `TransferQueueContract` | `java.util.concurrent.TransferQueue` | `contract.transferqueue` |
| `MapContract` | `Map` | `contract.map` |
| `SequencedMapContract` | `SequencedMap` | `contract.sequencedmap` |
| `SortedMapContract` | `SortedMap` | `contract.sortedmap` |
| `NavigableMapContract` | `NavigableMap` | `contract.navigablemap` |
| `ConcurrentMapContract` | `java.util.concurrent.ConcurrentMap` | `contract.concurrentmap` |
| `ConcurrentNavigableMapContract` | `java.util.concurrent.ConcurrentNavigableMap` | `contract.concurrentnavigablemap` |
| `MapEntryContract` | `Map.Entry` | `contract.mapentry` |
| `IteratorContract` | `Iterator` | `contract.iterator` |
| `WeaklyConsistentIteratorContract` | A weakly consistent `Iterator`, as `java.util.concurrent`'s package documentation describes one | `contract.iterator` |
| `ListIteratorContract` | `ListIterator` | `contract.listiterator` |
| `SpliteratorContract` | `Spliterator` | `contract.spliterator` |
| `SpliteratorOfIntContract`, `SpliteratorOfLongContract`, `SpliteratorOfDoubleContract` | `Spliterator.OfInt`, `Spliterator.OfLong`, `Spliterator.OfDouble` | `contract.spliterator` |
| `IntegerContract` | `Number`, for the integer classes | `contract.numeric` |
| `ComparableContract` | `Comparable` | `contract.comparable` |
| `ComparatorContract` | `Comparator` | `contract.comparator` |
| `CharSequenceContract` | `CharSequence` | `contract.charsequence` |
| `AutoCloseableContract` | `AutoCloseable` | `contract.autocloseable` |
| `CloseableContract` | `java.io.Closeable` | `contract.closeable` |
| `FutureContract` | `java.util.concurrent.Future` | `contract.future` |
| `ExecutorServiceContract` | `java.util.concurrent.ExecutorService` | `contract.executorservice` |

The thread-safety contracts extend these with tests under contention:

| Contract | Extends | Package |
|---|---|---|
| `ThreadSafeCollectionContract` | `CollectionContract` | `contract.collection` |
| `ThreadSafeSequencedCollectionContract` | `SequencedCollectionContract` | `contract.sequencedcollection` |
| `ThreadSafeBlockingQueueContract` | `BlockingQueueContract` | `contract.blockingqueue` |
| `ThreadSafeBlockingDequeContract` | `BlockingDequeContract` | `contract.blockingdeque` |
| `ThreadSafeTransferQueueContract` | `TransferQueueContract` | `contract.transferqueue` |
| `ThreadSafeConcurrentMapContract` | `ConcurrentMapContract` | `contract.concurrentmap` |
| `ThreadSafeFutureContract` | `FutureContract` | `contract.future` |
| `ThreadSafeExecutorServiceContract` | `ExecutorServiceContract` | `contract.executorservice` |

Each package also holds the contracts of the interface's single methods (`contract.list.GetContract`, `contract.map.PutContract`), which the interface's contract combines; implement one alone to run only its tests.

## Breakables

For the interfaces above, in `com.soliscode.test.breakable`: `BreakableIterable`, `BreakableCollection`, `BreakableSequencedCollection`, `BreakableList`, `BreakableSet`, `BreakableSequencedSet`, `BreakableSortedSet`, `BreakableNavigableSet`, `BreakableQueue`, `BreakableDeque`, `BreakableBlockingQueue`, `BreakableBlockingDeque`, `BreakableTransferQueue`, `BreakableMap`, `BreakableSortedMap`, `BreakableNavigableMap`, `BreakableConcurrentMap`, `BreakableConcurrentNavigableMap`, `BreakableMapEntry`, `BreakableIterator`, `BreakableListIterator`, `BreakableSpliterator` and its `OfInt`, `OfLong` and `OfDouble` forms, `BreakableComparable`, `BreakableComparator`, `BreakableCharSequence`, `BreakableCloseable`, `BreakableFuture`, `BreakableExecutorService` and `BreakableObject`. Some serve more than one contract: `BreakableSortedMap` breaks `SequencedMapContract`, `BreakableCloseable` breaks `AutoCloseableContract` too, and `BreakableIterator`'s breaks for a changed source break `WeaklyConsistentIteratorContract`. `Serializable` and `Number` have none of their own. Each class's Javadoc lists its breaks.

## Stability and versioning

- Every public class and member in the Javadoc is **public API**, except in a package named `internal`; SolisCode Test has none.
- Before 1.0.0, versions are `0.1.N`, and **any release may change the public API**. [RELEASES.md](../../../RELEASES.md) lists each change, and [Upgrading](../upgrading.md) says what you must do.
- A deprecated class or member is marked `@Deprecated(since = "<version>", forRemoval = <true or false>)`. Its Javadoc names what to use instead. `forRemoval = true` means a later release removes it.

## Building the reference locally

From the root of a clone:

```sh
mvn -B site
```

The Javadoc is in `target/site/en/apidocs/`. Open `target/site/en/apidocs/index.html`.

## See also

- [Concepts](../concepts/README.md): how the parts fit together
- [Java conventions, "Javadoc and comments"](../../design/reference/java-conventions.md#javadoc-and-comments): how the comments are written
