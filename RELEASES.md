# SolisCode Test releases

## The version scheme

- `main` is `0.1-SNAPSHOT`.
- A release is `0.1.N`: one past the highest `v0.1.*` tag, starting at `0.1.1`. It is tagged `v0.1.N`.
- A release is installed into the local Maven repository (`~/.m2`) as `com.soliscode:soliscode-test:0.1.N`.
- Releases are local. Nothing is deployed to a remote repository, and tags are pushed only when the user asks.
- A release is made only when the user runs `/release`, or a task's acceptance list asks for one, never on a merge (the `release` skill).
- Until 0.1.1, public API may be broken where a task calls for it, and each break is recorded under `## Unreleased`.

The old `org.soliscode:soliscode-test` releases, `1.0.0` to `1.0.14` (tags `v1.0.0`–`v1.0.14`, made by semantic-release and published to Maven Central), are not part of this scheme, and their tags stay. Their notes, from the old `CHANGELOG.md`, are at the end of this file.

Built (#9, #11): the POM's version is `${revision}`, `0.1-SNAPSHOT` unless a release sets it, and the flatten plugin installs the POM with the version resolved. `scripts/release/release.sh` makes and records a release; it needs a green full verify recorded by `scripts/verify.sh` for the tree it releases ([build-and-test.md](docs/design/reference/build-and-test.md), "Releases").

## Entry format

Each release gets an entry, newest first, written by `release.sh`:

```markdown
## 0.1.N — YYYY-MM-DD

Commit `<hash>`. Full verify: Tests run: <n>, Failures: 0, Errors: 0.

Install coordinates: `com.soliscode:soliscode-test:0.1.N`.

### Changes
- **<What a user of the library sees, in one sentence> (T-####).** <Detail: the types and methods, the old and new behaviour.>
```

As a task merges, it adds what a user of the library will see differently under `## Unreleased`, below. That covers new or changed public API, a changed result, a removed or deprecated type, and a new dependency. At a release, the `release` skill writes the release's changes to a file, as bullets without a heading, starting from those under `## Unreleased`, and `release.sh --changes <file>` puts the release's entry, made from that file, in place of the contents of `## Unreleased`, and leaves an empty `## Unreleased` above it for the next tasks.

## Unreleased

- **Fix: `CollectionTestUtils.preventNulls` checks the bulk methods' argument for null (T-0008).** The `preventNulls(Collection)` wrapper's `containsAll`, `addAll`, `removeAll` and `retainAll` checked the wrapped collection for `null` instead of the argument. They let a `null` in through `addAll`, and threw `NullPointerException` for any argument once the wrapped collection held one. They now throw `NullPointerException` for an argument with a `null` element, and otherwise act as the wrapped collection does. The `preventNulls(List)` wrapper's `containsAll`, both `addAll`s, `removeAll` and `retainAll` threw `NullPointerException` for an argument that refuses null, such as `List.of(1)`, because they asked it `contains(null)`. They now accept it. Both wrappers find a `null` by iterating the argument.
- **Fix: `CollectionTestUtils.preventNulls(List)` refuses null through its list iterators, sub-lists and `replaceAll` (T-0051).** The wrapper's `listIterator()` and `listIterator(int)` returned the backing list's iterator, and its `subList` the backing list's view, so their `add(null)` and `set(null)` put a `null` in; `replaceAll(x -> null)` did too, through the list iterator. The list iterators' `add` and `set` now throw `NullPointerException` for `null`, `subList` returns a view that refuses `null` as the wrapper does, and `replaceAll` throws `NullPointerException` when the operator returns `null`. `sort` now sorts the backing list as it sorts itself.
- **Breaking: `preventNulls(List)`'s `remove(Object)`, `indexOf` and `lastIndexOf` throw for null (T-0051).** They passed a `null` to the backing list, so `indexOf(null)` returned `-1` and `remove(null)` returned `false`; they now throw `NullPointerException`, as the wrapper's `contains` and the `preventNulls(Collection)` wrapper's `remove` do (the user's decision on #51). `IndexOfContract`'s null test accepts that exception from a list that does not permit nulls, which `List.indexOf` makes optional, where it required `-1`.
- **Fix: a `BreakableSortedSet` or `BreakableNavigableSet` built thread-safe is thread-safe (T-0023).** `BreakableSortedSet`'s protected constructor ignored its `isSafe` argument, so a sorted or navigable set built with `setSafe(true)`, or copied from a thread-safe one, reported `isSafe()` false and ran its methods without the lock. It now takes the lock as the other breakables do.

## 0.1.1 — 2026-11-08

Commit `0d0198c`. Full verify: Tests run: 6230, Failures: 0, Errors: 0.

Install coordinates: `com.soliscode:soliscode-test:0.1.1`.

### Changes
- **Breaking, for a build moving from `org.soliscode:soliscode-test` 1.0.14 (T-0001, T-0009).** The library needs **Java 25** (1.0.14 was built for Java 23), so it does not load on an older JDK, and it brings **JUnit 6** (1.0.14 brought JUnit Jupiter 5.10.1 and Platform 1.10.1). It no longer depends on `org.jetbrains:annotations`: its JetBrains `@NotNull` and `@Nullable` are replaced by JSpecify's (below), so code that used them through this library must declare them itself. Every package is renamed (below), and these public types are removed or renamed: `OptionalMethod` (now `InterfaceMethod`, with `MethodStatus`), `OptionalMethodSupport` (now `MethodSupport`), `contract.CollectionMethods` (now in `contract.collection`), the `contract.sequenced` package (now `contract.sequencedcollection`), `contract.iterable.ForEachContract`, `IteratorContract` and `SpliteratorContract` (now `ForEachMethodContract`, `IteratorMethodContract`, `SpliteratorMethodContract`), `contract.object.EqualsContract`, `HashCodeContract` and `ToStringContract` (now `EqualsMethodContract`, `HashCodeMethodContract`, `ToStringMethodContract`), `assertions.AssertThrowsAny` (now `AssertThrowsAnyOf`), `assertions.actions.AssertAction` (now `CheckableAction`) and `AssertConsumer` (now `Consumer` and `CheckableAction`, which the `AssertConsume*` classes implement), `assertions.collection.IterableAssertion` (an internal helper, removed), and `util.CollectionTestOps` and `IterableTestOps` (now `CollectionTestUtils` and `IterableTestUtils`).
- **Breaking, for a test written against 1.0.14: type parameters and renamed methods (T-0001, T-0009).** Every collection type parameter list now puts the collection type first and the element type last: `ListContract<E, L extends List<E>>` is `ListContract<L extends List<E>, E>`, so `implements ListContract<Integer, MyList<Integer>>` becomes `implements ListContract<MyList<Integer>, Integer>`. That holds for the 42 types that had the element first: `IterableContract`, `CollectionContract`, `ListContract`, `SequencedCollectionContract` and every method contract under them, `CollectionContractSupport`, `CollectionProviderSupport`, `CollectionProvider` and `FunctionalCollectionProvider`. The method enums' constants are `UPPER_SNAKE_CASE`: `CollectionMethods.Add` is `CollectionMethods.ADD`, `ObjectMethods.HashCode` is `ObjectMethods.HASH_CODE`, and each names its method (`methodName()`); `supportsMethod` and `doesNotSupportMethod` take an `InterfaceMethod`. A provider's seed is a `long`: `ObjectProvider.createInstance(int)` is `createInstance(long)`, and the integer number providers' `createValue(int)` is `createValue(long)`, so a provider written for 1.0.14 must change its override; `createRandoInstances` is `createRandomInstances`. In `Assertions`, `assertThrowsAny(Collection, Executable, ...)` is `assertThrowsAnyOf` (an `assertThrowsAny(Executable, ...)` now asserts that any exception is thrown), and `assertInstanceOf` and `assertInstanceOfOnly` are replaced by `assertImplementsOnly`. `AssertActions.assertConsumeNone` is `consumeNone`. `BreakableCollection`'s `setPermitsNulls`, `setPermitsDuplicates` and `setPermitsIncompatibleTypes`, and its public `(Collection, Collection<Break>, int)` constructor, are removed: a breakable is configured through its builder. `BreakableIterable` extends `BreakableObject`, and its builder extends `AbstractBreakableBuilder`.
- **Contracts and breakables for the collection and map interfaces (T-0001).** New contracts for `Set`, `SequencedSet`, `Map`, `SortedMap`, `NavigableMap`, `SequencedMap`, `Queue`, `Deque`, `Serializable` and thread safety, a breakable implementation of each collection and map interface, and builders for them (`BreakableBuilder`, `BreakableCollectionBuilder`, `BreakableMapBuilder`). `SortedSet`, `NavigableSet`, `ConcurrentMap`, `ConcurrentNavigableMap`, `BlockingQueue`, `BlockingDeque` and `TransferQueue` have breakables but no contracts yet. The static providers go through the builders. Built objects and copies do not share state with their builder or original. Test-tag annotations select which tests run (`build-and-test.md`, "Test tags"). Many contracts that could not fail now check their results against a JDK reference, and breaks that did nothing now work.
- **New coordinates, packages and parent (T-0009).** The library is `com.soliscode:soliscode-test`, version `0.1-SNAPSHOT` on `main` and `0.1.N` for releases, installed in the local `~/.m2` only; it was `org.soliscode:soliscode-test` 1.0.x on Maven Central. The packages are `com.soliscode.test.*`, from `org.soliscode.test.*`. The parent is `com.soliscode:soliscode-parent`, and with it JUnit 6.1.3 (1.0.14 had JUnit Jupiter 5.10.1) and JSpecify 1.0.1.
- **Dependencies (T-0002).** The library no longer depends on `junit-platform-testkit`, which it did not use, and so no longer brings AssertJ or `junit-platform-launcher` with it. A build that ran its tests with the launcher this library brought (Gradle needs one on the test runtime class path) must now declare `org.junit.platform:junit-platform-launcher` itself. The library declares `junit-jupiter`, `junit-jupiter-api`, `junit-jupiter-params`, `junit-platform-commons`, `opentest4j` and `jspecify`, at compile scope.
- **JSpecify nullness everywhere (T-0009).** Every package is `@NullMarked`: an unannotated type is non-null, and `@Nullable` marks what may be null. There is no `@NonNull` annotation: non-null is the default. Kotlin callers and nullness checkers now read unannotated types as non-null.
- **Nullable results and parameters (T-0002, T-0009).** The breakable methods that return `null` on a break are `@Nullable`: 58 were not, and 26 of those were annotated non-null, among them `toArray`, `Deque.pop`, `Queue.remove`, `Map.keySet` and the sorted and navigable map views. So are `BreakableMap.putIfAbsent`, `BreakableBlockingDeque.take`, `BreakableConcurrentNavigableMap.keySet`, `IterableTestUtils.first` and `last`, and the sorted builders' `comparator()`. The assertion methods' message, message-supplier and message-or-supplier parameters are `@Nullable`, as JUnit's are, and so are `CollectionAssertions.assertContainsOnly`'s `expected`, `CollectionTestUtils.listOf`'s elements and every `equals` parameter. A caller whose tools read JSpecify must now handle `null` from these methods.
- **API changes (T-0009).** `BreakableDeque.wrap` takes any `Deque<E>`, where it took a `LinkedList<E>`; `BreakableDeque.wrap(arrayDeque, breaks)` now returns a `BreakableDeque` instead of resolving to `BreakableQueue.wrap`. `BreakableSortedMap.newTreeMap` (protected) and `OrderedMapContractSupport.sortedReference` return a `NavigableMap`, where they returned a `TreeMap`.
- **`preventNulls(Collection)` equals only itself (T-0009).** `CollectionTestUtils.preventNulls(Collection)` returns a collection equal only to itself, as `Collections.unmodifiableCollection` does, where it was equal to another such wrapper around an equal collection: a `Collection` has no defined equality. `preventNulls(List)` still follows `List.equals`.
- **`CHARACTERISTIC_VALUES` is a list (T-0002).** `SpliteratorMethodContract.CHARACTERISTIC_VALUES` is an unmodifiable `List<Integer>`, where it was a public `int[]` any caller could change.
- **Every break is distinct (T-0002).** Breaks that shared a description with another class's break now have their own. 25 of them did, and `Break` is a record, so each pair was equal: an object given one break of a pair had both. The fields are unchanged; their descriptions, which show in messages, are prefixed with their type: `DEQUE_` for the 16 `BreakableDeque` breaks that matched `BreakableSequencedCollection`'s, `CONCURRENT_NAVIGABLE_MAP_` for the 8 `BreakableConcurrentNavigableMap` breaks that matched `BreakableSortedMap`'s and `BreakableNavigableMap`'s, and `QUEUE_` for `BreakableQueue.REMOVE_DOES_NOT_REMOVE_ELEMENT`, which matched `BreakableCollection`'s. So a queue given the collection's `remove(Object)` break no longer has the queue's `remove()` break, and the reverse. `BreakableSpliterator`'s `SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE` had an empty description, and `SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO`'s named the wrong method.
- **Breaks for null arguments (T-0002).** `BreakableCollection` has two new breaks: `REMOVE_ALL_DOES_NOT_THROW_ON_NULL_COLLECTION` and `REMOVE_IF_DOES_NOT_THROW_ON_NULL_FILTER` make `removeAll(null)` and `removeIf(null)` return `false` instead of throwing. Without them, `removeIf(null)` now always throws `NullPointerException`; with `REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS`, or with a skip break when no element reached the filter, it returned without throwing.
- **`SpliteratorMethodContract` and `StreamContract` follow `Spliterator` (T-0002).** After `trySplit`, the spliterator contract requires each part's estimated size to be at most the size before, where it required the remainder to be strictly smaller, which fails every spliterator of unknown size. It traverses an ORDERED spliterator's split-off prefix first and checks the order, where it checked no order after a split. For a spliterator that is not ORDERED it checks only the elements. Both contracts compare elements with `equals`, where for a spliterator that is not ORDERED they compared by identity and in order, so a collection that is not ordered failed.
- **`IdentitySet.add` and `remove` return whether the set changed (T-0002),** where `add` always returned `true` and `remove` returned `true` for an element it did not hold.
- **Breakable fixes (T-0009).** The breakables' random choices (`AbstractBreakable`) could pick a negative index once in about four billion draws (`Math.abs(Integer.MIN_VALUE)`); they use `Math.floorMod`. The breaks that make a blocking queue's `put` or `take`, or a transfer queue's `transfer`, block for ever keep blocking through a spurious wakeup, where one ended the block early. `BreakableBlockingDeque.takeLast` with `TAKE_LAST_DOES_NOT_REMOVE_ELEMENT` and `TAKE_FIRST_ALWAYS_RETURNS_NULL` together returns `null`, where it threw `NullPointerException` putting the `null` back.
- **Assertion and utility fixes (T-0002, T-0009).** `Assertions.assertThrowsAny(Executable, Supplier)` accepts a `null` supplier, as the other overloads accept a `null` message. `AssertContainsNone` and `AssertContainsNoneByIdentity` no longer use AssertJ, which the library did not declare. `UncachedString.equals` uses `instanceof`, so an instance of a subclass with the same value is equal, where it compared exact classes.
- **Breaking: the contracts' test methods are renamed to plain camelCase (T-0010).** Every contract test method had an underscore name, `method_condition_result`; it now joins the parts in camelCase, so `size_whenEmpty_returnsZero` is `sizeWhenEmptyReturnsZero` and `toArrayStore_withNullArray_throwsException` is `toArrayStoreWithNullArrayThrowsException` (the user's decision on #3: JUnit's convention, with the sentence in `@DisplayName`). A test class that overrides a contract method, or refers to one by name (a method reference, a `@see`, a JUnit method selector or filter), must use the new name. Every contract test method has a `@DisplayName`; the seven in `IteratorMethodContract` that had none get one, so their reports show a sentence instead of the method name.
- **Breaking: two contract constants are now methods (T-0010).** `ContractSupport.CONSISTENCY_REPEATS` is the default method `consistencyRepeats()`, and `MapContractSupport.DEFAULT_SIZE` is `defaultSize()`: an interface with only a constant fails Checkstyle's `InterfaceIsType`, and a method lets a test class change the value. Both still return 10. `CollectionContractSupport.DEFAULT_SIZE` is unchanged.
- **`CollectionAssertions.assertEquals` handles null elements (T-0016).** It compares elements with `Objects.equals`, so two iterables (or an array and an iterable) with `null` in the same places are equal, and a `null` against a non-null element fails the assertion. It threw `NullPointerException` when an expected element was `null`; a test that wrapped it in a `try` hid that.
- **`CollectionMethods.ADD` and `ListMethods.ADD_AT_INDEX` name their methods (T-0010).** Their `methodName()` is `"add(Object)"` and `"add(int, Object)"`, where it was `"add_singleElement_returnsTrueAndUpdatesSize(Object)"` and `"add_singleElement_returnsTrueAndUpdatesSize(int, Object)"`, left by an old rename that replaced the word `add`. It shows in the message of the `UnsupportedOperationException` a breakable throws for an unsupported `add`. The same rename had replaced `add` in about 250 places of the Javadoc and comments; they say `add` again.

## Before 0.1: the org.soliscode releases

The notes semantic-release wrote to `CHANGELOG.md` for `org.soliscode:soliscode-test`, kept as they were.

### [1.0.14](https://gitlab.com/soliscode/soliscode-test/compare/v1.0.13...v1.0.14) (2025-08-26)


#### Bug Fixes

* enable signing in publish release job ([fd0c9df](https://gitlab.com/soliscode/soliscode-test/commit/fd0c9df568fb5e6ce582f48922528140e2cdfa62))

### [1.0.13](https://gitlab.com/soliscode/soliscode-test/compare/v1.0.12...v1.0.13) (2025-08-26)


#### Bug Fixes

* sync gpg signing for snapshot and release ([3ef8850](https://gitlab.com/soliscode/soliscode-test/commit/3ef885058b3601ac855113f9ac206359c5c9e948))

### [1.0.12](https://gitlab.com/soliscode/soliscode-test/compare/v1.0.11...v1.0.12) (2025-08-26)


#### Bug Fixes

* sync gpg signing for snapshot and release ([7f100d7](https://gitlab.com/soliscode/soliscode-test/commit/7f100d73e2144228a8e51ab9adb234f4f6451acb))

### [1.0.11](https://gitlab.com/soliscode/soliscode-test/compare/v1.0.10...v1.0.11) (2025-08-26)


#### Bug Fixes

* parsing error in ci file ([a860f63](https://gitlab.com/soliscode/soliscode-test/commit/a860f63ae4ec477e1349b02165fe9b9142c9e5cf))
* parsing error in ci file ([d7983d7](https://gitlab.com/soliscode/soliscode-test/commit/d7983d70413a97d33f4a224920791fb9759266fd))
* semantic release needs a git push token ([f5278d9](https://gitlab.com/soliscode/soliscode-test/commit/f5278d9f4b0875c91e915f50ff857b1eecfe9429))

### [1.0.10](https://gitlab.com/soliscode/soliscode-test/compare/v1.0.9...v1.0.10) (2025-08-26)


#### Bug Fixes

* fixed duplicate source artifact creation ([8084e08](https://gitlab.com/soliscode/soliscode-test/commit/8084e085d7e27137f700ecbed09a3d451a45f919))

### [1.0.9](https://gitlab.com/soliscode/soliscode-test/compare/v1.0.8...v1.0.9) (2025-08-25)


#### Bug Fixes

* make sure source plugin only runs once ([921380d](https://gitlab.com/soliscode/soliscode-test/commit/921380d4f002f9aad4602a739f139a9a3981a369))

### [1.0.8](https://gitlab.com/soliscode/soliscode-test/compare/v1.0.7...v1.0.8) (2025-08-25)


#### Bug Fixes

* sign during deploy ([5807f26](https://gitlab.com/soliscode/soliscode-test/commit/5807f26855288baba848cd66f6a6f33668959835))

### [1.0.7](https://gitlab.com/soliscode/soliscode-test/compare/v1.0.6...v1.0.7) (2025-08-25)


#### Bug Fixes

* fixed error in publish release ([91e1aa6](https://gitlab.com/soliscode/soliscode-test/commit/91e1aa6815f97651c177bc278f4bae8730df2c66))

### [1.0.6](https://gitlab.com/soliscode/soliscode-test/compare/v1.0.5...v1.0.6) (2025-08-25)


#### Bug Fixes

* fixed error in publish release ([63abe68](https://gitlab.com/soliscode/soliscode-test/commit/63abe68123f1fa0382fd991d21ae9d4b458a45a2))

### [1.0.5](https://gitlab.com/soliscode/soliscode-test/compare/v1.0.4...v1.0.5) (2025-08-25)


#### Bug Fixes

* pipeline only runs on main and releases ([f7b5d1c](https://gitlab.com/soliscode/soliscode-test/commit/f7b5d1c79dd1c70e13f7a53d5f87117f55aee8b8))
* pipeline only runs on main and releases ([27966e0](https://gitlab.com/soliscode/soliscode-test/commit/27966e02c39a9e6d285e0a0e2489c56fa8d323f2))
* pipeline only runs on main and releases ([f396679](https://gitlab.com/soliscode/soliscode-test/commit/f396679fffa44b2112916bf4fa4c2e50e0542a8d))

### [1.0.4](https://gitlab.com/soliscode/soliscode-test/compare/v1.0.3...v1.0.4) (2025-08-25)


#### Bug Fixes

* remove semantic release config from package json ([724b9fd](https://gitlab.com/soliscode/soliscode-test/commit/724b9fd418991890cd623036b4e0c86dbc2c71b9))

### [1.0.3](https://gitlab.com/soliscode/soliscode-test/compare/v1.0.2...v1.0.3) (2025-08-24)


#### Bug Fixes

* remove skip ci from semantic release commit message ([3ef6c4d](https://gitlab.com/soliscode/soliscode-test/commit/3ef6c4d4735d514ceddf8660e242f40d4d32daac))

### [1.0.2](https://gitlab.com/soliscode/soliscode-test/compare/v1.0.1...v1.0.2) (2025-08-24)


#### Bug Fixes

* trigger publish ([e3f038c](https://gitlab.com/soliscode/soliscode-test/commit/e3f038cbc05fa324f11060c87ada8da8de79b37b))

### [1.0.1](https://gitlab.com/soliscode/soliscode-test/compare/v1.0.0...v1.0.1) (2025-08-24)


#### Bug Fixes

* correct snapshot publishing flow ([b2ba5d2](https://gitlab.com/soliscode/soliscode-test/commit/b2ba5d2f42ddc7058f4dde4c373028aee11cb979))

### 1.0.0 (2025-08-16)


#### Bug Fixes

* added NVD api key to pom ia env variable ([db7e0e8](https://gitlab.com/soliscode/soliscode-test/commit/db7e0e8ad614422857ba6ad1e4e6be61decd1122))
* make test stage always run on commit ([0a1ddb8](https://gitlab.com/soliscode/soliscode-test/commit/0a1ddb8b889bca8d0a35a9f6d59a5407207d3128))
* moved gpg signing to publish stage ([de55e40](https://gitlab.com/soliscode/soliscode-test/commit/de55e4092bb9d50c338b48af1475d2ca6b436ebe))
* removed enable-preview feature Flexible Constructors from code ([7a14263](https://gitlab.com/soliscode/soliscode-test/commit/7a14263e6405726b4a6904e85f04a474d4c48d31))
* removed enable-preview for compile ([a8a2ba0](https://gitlab.com/soliscode/soliscode-test/commit/a8a2ba0dcdde960035ca7c967a9a336785b8d0d0))
* removed gpg signing from maven verify ([4be6310](https://gitlab.com/soliscode/soliscode-test/commit/4be63103b32b0970ffb579b8df5403bb5b5c6d7e))
* removed gpg signing from test stage ([7555b93](https://gitlab.com/soliscode/soliscode-test/commit/7555b93d7c1fba59ed7c6f9cee1fbbf18f662c2a))
* removed unused toolchain maven plugin ([cef3fba](https://gitlab.com/soliscode/soliscode-test/commit/cef3fbab7c355fb04745160042ea8f5ebd3ac33c))
* renamed project gitlab ci file to the correct name ([c85ff3f](https://gitlab.com/soliscode/soliscode-test/commit/c85ff3f97ab7a6955e05f555f90b44fcd96b984f))
* set up ci settings for GitLab ([2799e7b](https://gitlab.com/soliscode/soliscode-test/commit/2799e7b7dab761b11e7d414c039e753072e2dc7f))
* stop tracking build outputs (ignore **/target/) ([bdec081](https://gitlab.com/soliscode/soliscode-test/commit/bdec0812bd974801ceb4c558a81bdf54b1eaf65a))
* updated gpg key handling in test phase ([0d98c54](https://gitlab.com/soliscode/soliscode-test/commit/0d98c54b65d58ee85b8a9f23fb09a90e26cb850b))


#### Features

* update gitlab test stage to save dumps ([275a710](https://gitlab.com/soliscode/soliscode-test/commit/275a710f60f709630eaebc807a62e03b16839523))
