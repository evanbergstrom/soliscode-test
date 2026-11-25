# Upgrading

For the full list of changes in each release, see [RELEASES.md](../../RELEASES.md).

## Moving to a new release

1. Install the new release, and the SolisCode parent it builds on, as [Installation](getting-started/installation.md) says.
2. Change the `<version>` of `com.soliscode:soliscode-test` in your POM.
3. Read the section below for each release between yours and the new one, and make the changes it lists.
4. Build, and fix each use of a deprecated class or member that the compiler reports.

## Upgrading to 0.1.7 from 0.1.6

**Summary:** some tests of `ObjectContract`, which `IterableContract`, `MapContract` and every collection and map contract extend, now check what they did not, and may fail where they passed (T-0070).

- **`equalsWithNullValueReturnsFalse` calls `equals(null)`.** It never did, so an `equals` that returned `true` for `null` passed. Fix `equals`.
- **The hash code distribution test can fail.** It measured nothing before. It now puts 1000 values, distinct by `equals`, from `createInstance(seed)` with seeds drawn below `min(100_000, uniqueSizeLimit())`, into a table laid out as `HashMap`'s, and needs 90% of the buckets a uniform hash fills; it is skipped where the provider's limit is below 1000, or 10,000 seeds give fewer than 1000 distinct values. Fix the hash code, or, for a class whose hash code is legitimately constant or coarse, override `hashCodeSpreadsUnequalValues()` to return `false`. If a provider of your own throws for a seed below its `uniqueSizeLimit()`, make the limit say which seeds it serves.
- **`toString` must not return `null`**, as `Object` requires: the new `toStringWhenCalledReturnsNonNull` checks it.
- **New opt-outs** for the tests that read `Object` more strictly than it is written: `hashCodeSpreadsUnequalValues()`, `toStringDistinguishesUnequalValues()` and `toStringDescribesValue()`. Each is `true` by default, so what your test checks does not shrink.

## Upgrading to 0.1.6 from 0.1.5

**Summary:** one new test may fail. `ThreadSafeCollectionContract` gains `clearWhenCalledDuringForEachDoesNotDisturbIt`: a collection that inherits `Iterable.forEach` over a fail-fast iterator, which its callers must lock by hand, now fails it; override `forEach` to hold the collection's lock. 0.1.6 also adds the performance test library, `com.soliscode.test.performance`, and with it two dependencies your tests get: `com.soliscode:soliscode-performance-api` (installed with the SolisCode parent) and `org.hdrhistogram:HdrHistogram`. It builds on the SolisCode parent 0.1.12, so install that first. `BreakableSequencedCollection.REVERSED_IS_NOT_THREAD_SAFE` now leaves the view `reversed()` returns without a lock; a test that relied on the old behaviour of that break may now fail.

## Upgrading to 0.1.5 from 0.1.4

**Summary:** two packages move. Change the imports.

### Breaking changes

#### The method enums move to `com.soliscode.test.methods`

**What changed:** `CollectionMethods`, `ListMethods`, `MapMethods`, `ObjectMethods`, `QueueMethods`, `DequeMethods`, `IterableMethods`, `ListIteratorMethods`, `MapEntryMethods`, `NavigableMapMethods`, `SequencedCollectionMethods`, `SequencedMapMethods`, `SortedMapMethods`, `BlockingQueueMethods`, `BlockingDequeMethods` and `TransferQueueMethods` were each in their interface's `com.soliscode.test.contract.<interface>` package. Their constants are unchanged.
**Why:** the breakables name them, and must not depend on the contract packages, which the parent's architecture gate refuses as a package cycle.
**What you do:**

```diff
- import com.soliscode.test.contract.list.ListMethods;
+ import com.soliscode.test.methods.ListMethods;
```

A breakable serialized by 0.1.5 with a method status set names the new class, so 0.1.4 cannot read it.

#### `WithString` moves to `com.soliscode.test.contract.object`

**What changed:** `WithString` was in `com.soliscode.test.contract.support`.
**Why:** it gives `ObjectContract` a `String` provider, and now sits beside it.
**What you do:**

```diff
- import com.soliscode.test.contract.support.WithString;
+ import com.soliscode.test.contract.object.WithString;
```

Also in 0.1.5: the `ExecutorService` contract is new; a breakable's views (`subList`, `keySet`, `headSet` and the others) now share its lock and carry its method statuses but none of its breaks; the breaks that threw a plain `RuntimeException` throw `WrongException`, and the tasks and futures that fail on purpose fail with `DeliberateException`. A test of yours that caught `RuntimeException` or `Exception` from them still does.

## Upgrading to 0.1.4 from 0.1.3

**Summary:** nothing to change. 0.1.4 adds the `Comparator`, `CharSequence` and `Future` contracts, and corrects how often `ContentionCoordinator`'s threads yield (once every 64 iterations by default, as documented), so contention runs finish sooner.

## Upgrading to 0.1.3 from 0.1.2

**Summary:** nothing to change. 0.1.3 adds the `Iterator`, `ListIterator`, `Spliterator`, `Map.Entry`, `Comparable`, `AutoCloseable` and `Closeable` contracts, and tests the concurrent collections' iterators for weak consistency: a concurrent collection of yours whose iterators are fail-fast must override `iteratorsAreWeaklyConsistent()` to return `false`.

## Upgrading to 0.1.2 from 0.1.1

**Summary:** three changes to the breakables and `preventNulls`.

### Breaking changes

#### `BreakableConcurrentMap`'s race-condition breaks are renamed

**What changed:** `COMPUTE_RACE_CONDITION`, `COMPUTE_IF_ABSENT_RACE_CONDITION`, `COMPUTE_IF_PRESENT_RACE_CONDITION` and `MERGE_RACE_CONDITION` are now `COMPUTE_IS_NOT_THREAD_SAFE`, `COMPUTE_IF_ABSENT_IS_NOT_THREAD_SAFE`, `COMPUTE_IF_PRESENT_IS_NOT_THREAD_SAFE` and `MERGE_IS_NOT_THREAD_SAFE`.
**Why:** they are named as the other breakables' thread-safety breaks are.
**What you do:**

```diff
- .addBreak(BreakableConcurrentMap.COMPUTE_RACE_CONDITION)
+ .addBreak(BreakableConcurrentMap.COMPUTE_IS_NOT_THREAD_SAFE)
```

#### `BreakableConcurrentNavigableMap.NAVIGATION_RACE_CONDITION` is removed

**What changed:** the break is gone.
**Why:** no contract could catch it: each of the store's calls is atomic, so its result was one a correct map could give.
**What you do:** remove it from your builders. The inherited `*_IS_NOT_THREAD_SAFE` breaks are the thread-safety faults.

#### `preventNulls(List)` refuses `null` in `remove(Object)`, `indexOf` and `lastIndexOf`

**What changed:** they returned `false` or `-1` for `null`; they now throw `NullPointerException`.
**Why:** as the wrapper's `contains` does, and as `preventNulls(Collection)` does.
**What you do:** a test that called them with `null` on such a list expects the exception.

## Upgrading to 0.1.1 from `org.soliscode:soliscode-test` 1.0.14

**Summary:** 0.1.1 is the first release under the coordinates `com.soliscode:soliscode-test`. It needs Java 25 and brings JUnit 6, renames every package, puts the collection type first in every type parameter list, and renames the contracts' test methods to camelCase.

### Breaking changes

#### New coordinates, Java 25 and JUnit 6

**What changed:** the group is `com.soliscode`, the packages are under `com.soliscode.test`, the library needs Java 25 (1.0.14 was built for Java 23), and it brings JUnit 6 (1.0.14 brought JUnit Jupiter 5.10.1). Its nullness annotations are JSpecify's, not JetBrains'. It no longer brings AssertJ or `junit-platform-launcher`: a test that used AssertJ declares it, and a Gradle build, which needs the launcher on the test runtime class path, declares `org.junit.platform:junit-platform-launcher`.
**Why:** the move to the SolisCode parent.
**What you do:**

```diff
- <groupId>org.soliscode</groupId>
+ <groupId>com.soliscode</groupId>
```

and change each import from `org.soliscode.test` to `com.soliscode.test`. Code that used JetBrains' `@NotNull` and `@Nullable` through this library declares that dependency itself.

#### The collection type comes first

**What changed:** every collection type parameter list puts the collection type first and the element type last.
**Why:** one order for every contract, provider and support interface.
**What you do:**

```diff
- implements ListContract<Integer, MyList<Integer>>
+ implements ListContract<MyList<Integer>, Integer>
```

#### Renamed types, methods and constants

**What changed:** `OptionalMethod` is `InterfaceMethod`, `OptionalMethodSupport` is `MethodSupport`, the method enums' constants are `UPPER_SNAKE_CASE` (`CollectionMethods.Add` is `CollectionMethods.ADD`), `ObjectProvider.createInstance(int)` is `createInstance(long)`, the contracts' test methods are camelCase (`size_whenEmpty_returnsZero` is `sizeWhenEmptyReturnsZero`), and `ContractSupport.CONSISTENCY_REPEATS` and `MapContractSupport.DEFAULT_SIZE` are the methods `consistencyRepeats()` and `defaultSize()`. [RELEASES.md](../../RELEASES.md), "0.1.1", lists every one.
**Why:** JUnit's and Java's naming conventions, and a seed as wide as a `long`.
**What you do:**

```diff
- doesNotSupportMethod(CollectionMethods.Add);
+ doesNotSupportMethod(CollectionMethods.ADD);
```

and the same for each renamed type and method your tests use.

### Deprecations

None: the renamed types and methods were removed, not deprecated.

## Version support

Before 1.0.0, versions are `0.1.N`, and any release may change the public API. Only the newest release gets fixes: a fault is fixed in the next release, not in an older one.

## See also

- [API reference](reference/api.md#stability-and-versioning)
- [Installation](getting-started/installation.md)
