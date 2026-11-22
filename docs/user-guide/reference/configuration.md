# Configuration reference

## Configuration sources

SolisCode Test reads no configuration file and no system property. A contract is configured by the test class that runs it, in code:

1. Interfaces the test class implements, which declare a choice the JDK's interface leaves to the implementation (`DoesNotPermitNulls` and the others)
2. Default methods of the contract that the test class overrides (`permitNullKeys()`, `comparatorPermitsNulls()` and the others)
3. Calls in the test class's constructor: `doesNotSupportMethod`, which it inherits from `AbstractTest`, and a collection contract's `doesNotSupportModification`
4. The defaults below

`ContentionCoordinator` is configured by its constructor's arguments, and the test tags by the build that runs the tests.

## Settings

### Collections

| Setting | How to set it | Type | Default | Description |
|---|---|---|---|---|
| Refuses `null` | implement `DoesNotPermitNulls` | marker | permits | The contracts expect `NullPointerException` where a `null` element is given, or `false` from a query such as `contains(null)` |
| Refuses duplicates | implement `DoesNotPermitDuplicates` | marker | permits | The contracts expect an equal element to be added once. Implement it for a `Set` |
| Refuses other types | implement `DoesNotPermitIncompatibleTypes` | marker | permits | The contracts expect `ClassCastException` for an argument of another type, as `Collections.checkedList` throws, or `false` from a query such as `contains` |
| Iterators weakly consistent | override `iteratorsAreWeaklyConsistent()` | `boolean` | `true` | For the concurrent queues, deques and maps: whether their iterators are weakly consistent, tested after a change to the source. Return `false` for fail-fast iterators |

### Maps

| Setting | How to set it | Type | Default | Description |
|---|---|---|---|---|
| `permitNullKeys()` | override | `boolean` | `true`; `false` in the `ConcurrentMap` contracts | Whether a `null` key is permitted. Where it is not, the contracts expect `NullPointerException` |
| `permitNullValues()` | override | `boolean` | `true`; `false` in the `ConcurrentMap` contracts | Whether a `null` value is permitted |
| `defaultSize()` | override | `int` | `10` | The entries a contract puts in a map it makes for a test |

### Optional methods

| Setting | How to set it | Type | Default | Description |
|---|---|---|---|---|
| One method unsupported | `doesNotSupportMethod(method)` in the constructor | `InterfaceMethod` | all supported | The contracts expect `UnsupportedOperationException` from it. The constants are in `com.soliscode.test.methods`: `CollectionMethods.ADD`, `ListMethods.SET`, `MapMethods.PUT`, `ObjectMethods.EQUALS` and the others |
| No modification | `doesNotSupportModification()` in the constructor | | modifiable | In the `Iterable` and collection contracts: marks every method that changes the collection unsupported (`add`, `addAll`, `clear`, `remove`, `removeAll`, `removeIf`, `retainAll`, the iterator's `remove`, and each subinterface's own), as for `Collections.unmodifiableList`. A map contract has none: call `doesNotSupportMethod` for each `MapMethods` constant that changes the map |

### Every contract

| Setting | How to set it | Type | Default | Description |
|---|---|---|---|---|
| `equalsComparesValues()` | override | `boolean` | `true`; `false` in the `BlockingQueue`, `BlockingDeque` and `TransferQueue` contracts | Whether `equals` compares contents. Return `false` for a class that keeps `Object`'s identity equality, as most queues do; the tests that hold for identity still run |
| `consistencyRepeats()` | override | `int` | `10` | How many times a test repeats an operation it checks gives the same result each time, such as `equals` |

### Other interfaces

| Setting | Contract | Type | Default | Description |
|---|---|---|---|---|
| `naturalOrderingIsConsistentWithEquals()` | `ComparableContract` | `boolean` | `true` | Return `false` for a class whose `compareTo` is 0 for unequal values, such as `BigDecimal`; the test of it is skipped |
| `comparatorIsConsistentWithEquals()` | `ComparatorContract` | `boolean` | `true` | Return `false` for a comparator such as `String.CASE_INSENSITIVE_ORDER` |
| `comparatorPermitsNulls()` | `ComparatorContract` | `boolean` | `false` | Return `true` for a comparator that orders `null`; the laws are then tested with `null` among the samples |
| `naturalOrderIsByChar()` | `CharSequenceContract` | `boolean` | `true` | Return `false` for a `Comparable` sequence whose natural order is not by `char` |
| `closeIsIdempotent()` | `AutoCloseableContract` | `boolean` | `true` | Return `false` for a class whose second `close` has an effect. `CloseableContract` tests it whatever this returns, as `Closeable` requires |
| `permitsNullResult()` | `FutureContract` | `boolean` | `true` | Return `false` for a future whose result cannot be `null` |
| `rejectsTasksAfterShutdown()` | `ExecutorServiceContract` | `boolean` | `true` | Return `false` for an executor that discards tasks after shutdown rather than throwing `RejectedExecutionException` |

### `ContentionCoordinator`

| Setting | How to set it | Type | Default | Description |
|---|---|---|---|---|
| Threads and iterations | `add(threads, iterations, operation)` | `int`, `int` | `add(operation)`: the cores divided among the operations, at least 1, and 30,000 iterations | How many threads run an operation, and how many times each runs it |
| Start timeout | constructor | `int`, seconds | `5` | How long it waits for the threads to be ready |
| Shutdown timeout | constructor | `int`, seconds | `5` | How long it waits for them to finish |
| Yield interval | constructor | `int`, at least 1 | `64` | A thread yields before its first call and then once every this many |
| Phase interval | four-argument constructor | `int` | `0`, no phases | Above 0, the threads wait for each other after each this many calls |

### Test tags

The annotations of `com.soliscode.test.annotations` tag tests, so that a build can choose which to run, with Surefire's `<groups>` and `<excludedGroups>`:

| Annotation | Tag | Meaning |
|---|---|---|
| `@ConcurrencyTest` | `concurrency`, `nondeterministic` | A thread-safety test, repeated 5 times with a timeout of 10 seconds each |
| `@Nondeterministic` | `nondeterministic` | A test whose outcome can vary between runs |
| `@Slow` | `slow` | Roughly 100 ms to 1 s |
| `@VerySlow` | `very-slow` | Roughly 1 s to 10 s |
| `@ExtremelySlow` | `extremely-slow` | More than 10 s |

Some thread-safety contract tests are tagged `slow` or `very-slow`. To leave them out of a quick build, add `<excludedGroups>very-slow</excludedGroups>` to Surefire's configuration, or run `mvn -B -DexcludedGroups=very-slow test`.

## See also

- [How to test a collection or map with its contract](../how-to/test-a-collection.md)
- [API reference](api.md)
