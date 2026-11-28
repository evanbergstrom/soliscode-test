# Java conventions

The conventions the code follows, and the rules the gates enforce. Each section says whether the rule is **Built** (a gate enforces it and the code passes), **Partly built** (a gate enforces it but the code does not pass yet, with the task that fixes it), **Designed** (decided, with the task that builds it), or a **habit** of the code with no gate.

Related: [build-and-test.md](build-and-test.md) for how the gates run, [dependency-management.md](dependency-management.md) for versions. The Checkstyle rules are soliscode-parent's: [its build-and-test.md](https://gitlab.com/soliscode/soliscode-parent/-/blob/main/docs/design/reference/build-and-test.md), "Checkstyle", and `soliscode/checkstyle.xml` in soliscode-build-tools is the full list.

## Language level

- Built (#9): Java 25, from the parent (`maven.compiler.release` 25), with no `--enable-preview`: no preview feature is used.
- Records, sealed types, pattern matching, switch expressions and text blocks may be used. `Break` and `MethodStatus` are records.
- Built (#10): 4-space indentation, no tabs, lines at most 120 characters, files at most 2,000 lines, no trailing whitespace, one top-level type per file. `BreakableCollection` and `BreakableMap` were over 2,000 lines; #10 trimmed repetition in their Javadoc (the user's decision), and split `BreakableMapTest` in two. The copyright header (#71) took `BreakableMapTest` to 2,001 lines, so its `equals`, `hashCode`, `toString` and `clone` tests moved to `BreakableMapObjectMethodsTest`.
- Built (#10): a type's members are in Checkstyle's `DeclarationOrder`: static fields, then instance fields, each public, protected, package-private, private; then constructors, together; then methods. Nested types may stand anywhere. A field is never moved above one its initializer reads.

## Packages

Built (#9). Every package is under `com.soliscode.test` (moved from `org.soliscode.test` by #9). Package names are lowercase. Every main package has a `package-info.java` (49 on 2026-11-26, #46, with `contract.lock` and `contract.readwritelock`; this said 45 until then, a count #69's performance packages had left behind) that says what it holds and is `@NullMarked`; a new package gets one. Test packages have none, except `contract.coverage`.

## Public API

- There is no `module-info.java`, so every public type in `com.soliscode.test.*` is public API. That includes the contracts' test methods: a user's test class implements a contract, and its methods' names are what the user's reports show.
- A change that breaks callers (a type, method or signature removed, renamed or changed, a result that changes) is recorded under `## Unreleased` in [RELEASES.md](../../../RELEASES.md) by the task that makes it. #10's renaming of the contract test methods was one.
- The assertions in `com.soliscode.test.assertions` use two JUnit classes that JUnit marks internal (`org.junit.platform.commons.util.StringUtils`, `UnrecoverableExceptions`), and `contract.support.AutoCloseableContractSupport` the second (#41): see [dependency-management.md](dependency-management.md).

## Naming

| Element | Convention |
|---------|------------|
| Types | PascalCase, no `I` prefix, no `Impl` suffix |
| Methods, parameters, locals, fields | camelCase |
| Constants | `UPPER_SNAKE_CASE`; a `Break` constant names the fault (`ADD_DOES_NOT_ADD_ELEMENT`) |
| Contracts | `XxxContract`: an interface of default test methods, in `com.soliscode.test.contract.<interface>` (`SizeContract`, `ListContract`). The `XxxMethods` enum of an interface with optional methods, in `com.soliscode.test.methods` (#67; until then in the contract's package), names the methods its contracts test |
| Breakables | `BreakableXxx`, an implementation of the JDK interface `Xxx` whose faults are its `Break` constants (`BreakableList`, `BreakableDeque`), built with its `BreakableXxxBuilder` where it has one |
| Providers | `XxxProvider` for a provider of one type; `CollectionProviders`, `MapProviders` and `Providers` for the static factories |
| Contract support | `XxxSupport` and `WithXxx` in `contract.support`: the mix-ins that give a contract its provider; `WithString`, which gives `ObjectContract` its, is beside it in `contract.object` (#67) |
| Test classes | `*Test`; an abstract base class of tests `Abstract*Test` (`AbstractContractTest`); a helper that is not a test does not end in `Test` (`DynamicContract`, `ContractRuns`) |

Built (#10): Checkstyle `TypeName`, `MethodName`, `ParameterName`, `LocalVariableName`, `MemberName`, `ConstantName`, `HiddenField`. A constructor parameter that would hide a field ends in `Arg`, as soliscode-quant's do (`this.breaks = breaksArg`), and its `@param` uses that name; an ordinary method parameter or local that would hide one takes a different word. An unnamed variable is `ignored`, not `_`, which `LocalVariableName` refuses.

### Test method names

The user's decision (2026-11-08, on #3): **a test method has a plain camelCase name, with no underscores** (`toArrayWhenEmptyReturnsEmptyArray`), **and its `@DisplayName` holds the readable sentence** ("toArray() returns an empty array for an empty collection"). This holds for the contracts in `src/main` and for the tests in `src/test`. No `test` prefix in new names.

Built (#10, #14). #10 renamed every underscore name, in the contracts and the tests, by joining its parts in camelCase (`size_whenEmpty_returnsZero` became `sizeWhenEmptyReturnsZero`), and recorded the contracts' renames in `RELEASES.md` as a breaking change. The tests' older `testXxx` names are plain camelCase and stay; a new name has no `test` prefix. Since soliscode-parent 0.1.2 (soliscode/soliscode-parent#2, #14), Checkstyle reports every method annotated `@Test`, `@ParameterizedTest`, `@RepeatedTest`, `@TestFactory` or `@TestTemplate` whose name is not plain camelCase (`^[a-z][a-zA-Z0-9]*$`) as `[TestMethodName]`, in main and test sources; a method marked `@Override` is checked where it is declared. `MethodName` now holds in test sources too, so helpers, lifecycle and factory methods there are camelCase as well.

The old rules (`methodName_condition_expectedResult`, and `size_withAlwaysReturnsZeroBreak_returnsZero` for breakables) are withdrawn.

## `final`

- Built (#10): parameters of methods, constructors, `catch` clauses and for-each loops are `final` (Checkstyle `FinalParameters`). Abstract and interface method parameters, lambda parameters and try-with-resources variables are not.
- Built: a utility class is `final` with a private constructor, `private Foo() { }`. Every class in main code with a private no-argument constructor is `final` today (`Assertions`, `Providers`, `CollectionProviders`, `IterableTestUtils`, the `Assert*` classes). Checkstyle's `FinalClass` and `HideUtilityClassConstructor` check it, in test code too (Built, #10).
- Habit: fields are `private final` unless they must change; visibility is as narrow as it can be.

## Nullness

- Built (#9): JSpecify. Every main package is `@NullMarked`, so everything is non-null unless it is `@Nullable` (`org.jspecify.annotations.Nullable`). There is no `@NonNull`: non-null is the default, and #9 removed every one.
- JSpecify's `@Nullable` is a type annotation, so it sits next to the type it qualifies: a nested type takes it after the outer name (`Map.@Nullable Entry<K, V>`), and a nullable array is `T @Nullable []`, while `@Nullable T[]` is an array of nullable elements. On a parameter it follows `final`.
- Built (#9): NullAway checks main sources at ERROR (build-and-test.md, "Gates"). Test sources are not checked.
- Arguments that must not be null are checked with `Objects.requireNonNull`, at the start. Habit: return no `null` from public API where an empty value or `Optional` will do; `Optional` only as a return type.
- Designed (#13): type parameters stand for non-null types today (no `E extends @Nullable Object`), though the breakables and contracts handle null elements on purpose. #13 decides how they are typed.

## Suppressions

The user's decision (2026-11-08, on #9): where the code does on purpose what an ErrorProne or NullAway check warns against, it gets a suppression instead of a workaround that hides the finding. The rule:
- one `@SuppressWarnings` per finding, naming the one check;
- on the smallest declaration that can carry it (the method, or the local variable; the class only where the finding is on the class itself, such as its `implements` clause);
- with a one-line comment directly above it, `// Check: reason`;
- no disguised workaround (a cast, a helper or a narrowed type whose only purpose is to quiet the check). An agent once narrowed the providers' return types to interfaces to avoid `NonApiType`; that removed providers users had (`provideLinkedList` could no longer back a `Queue` or `Deque` contract), and was reverted.

A finding that points at a fault is fixed, not suppressed. Built (#9, #30, #32, #33, #34, #35, #36, #37, #38, #44, #45, #46, #59, #70, #81): main code has 109 such suppressions (annotations; the table counts each check an annotation names, 110), of these kinds:

| Check | Count | Where, and why |
|-------|-------|----------------|
| `JdkObsolete` | 12 | `LinkedList` in `BreakableDeque`, `BreakableQueue` and `CollectionProviders.provideLinkedList` (8): it is the one JDK `Deque` that permits null elements, which these types must allow. `IteratorProviders.linkedList` and `linkedListListIterator` (2, #37): they provide `LinkedList`'s own iterators, to test them. `BreakableSortedMap` and `BreakableSortedSet` (2): they are breakable `SortedMap` and `SortedSet` classes, and the finding is on the `implements` clause, so the class carries it |
| `NonApiType` | 8 | The providers of `ArrayList`, `LinkedList`, `HashSet`, `HashMap`, `TreeMap` and `LinkedHashMap`, and `WithArrayList` and `WithLinkedList`: they provide those concrete classes, to test them |
| `UndefinedEquals` | 4 | Wrappers whose equality is the wrapped collection's (`CollectionOnly`, `IterableOnly`, `BreakableIterable`, `BreakableQueue`) |
| `NullAway` | 60 | Code that passes null on purpose: contract tests that pass it to a JDK method (`ToArrayContract`, `PushContract`, `OfferFirstContract`, `OfferLastContract`; by #32, `PutContract`, `OfferWithTimeoutContract`, `DrainToContract`, `DrainToMaxElementsContract`; by #33, `PutFirstContract`, `PutLastContract`, `OfferFirstWithTimeoutContract`, `OfferLastWithTimeoutContract`; by #34, `TransferContract`, `TryTransferContract`, `TryTransferWithTimeoutContract`; and, by #35, 21 in `contract.concurrentmap`'s method contracts, one per null argument tested, and `BreakableConcurrentMap.forEach`, whose `FOR_EACH_IS_NOT_THREAD_SAFE` passes `null` for a key removed meanwhile, on purpose; and, by #36, 6 in `contract.concurrentnavigablemap`'s range-view contracts, one per null-bound test; and, by #38, `BreakableSpliterator.acceptNull`, whose `SPLITERATOR_FIRST_ELEMENT_IS_NULL` passes `null` in place of an element, on purpose; and, by #44, `BreakableFuture`'s `get`, timed `get`, `resultNow` and `exceptionNow`, whose breaks return `null` for a result or an exception, on purpose, and whose unbroken paths return a `null` result, which `Future` permits and the non-null `V` cannot say until #13, or the exception field, set whenever the state is `FAILED`; and, by #45, 6 in `contract.executorservice`'s method contracts, one per test of a null task or collection, and 4 in `BreakableExecutorService` (`submit(Callable)`, `submit(Runnable, Object)`, its `invokeAny` and the future of a null task), whose breaks return `null` for a result, on purpose, which the non-null `T` cannot say until #13; and, by #70, `BreakableObject.toString`, whose `TO_STRING_RETURNS_NULL` returns `null` on purpose; and, by #81, `BreakableCollection.clearElement`, which writes `null` into a `T[]` for `TO_ARRAY_STORE_MISSING_FIRST_ELEMENT` and `TO_ARRAY_STORE_MISSING_LAST_ELEMENT`, on purpose, which NullAway's JSpecify mode refuses and no annotation can allow, since the array's type is `toArray`'s) |
| `ReferenceEquality` | 8 | Identity comparisons: `AssertEqualsByIdentity`, `AssertEquals` (the same iterable is equal to itself without being iterated), `IterableTestUtils`' `containsByIdentity`, and `BreakableBlockingQueue`'s, `BreakableBlockingDeque`'s and `BoundedTransferQueue`'s refusals to drain into themselves (#32, #33, #34), and `ExecutorServiceContractSupport.assertCauseIs`, whose cause must be the task's exception itself, or a copy whose cause is it (#45), and `BreakableObject.serialsAllowEquality`, whose breaks act only between distinct instances (#70) |
| `DoNotCall` | 6 | Contract tests that call `addFirst` or `addLast` on a `SortedSet` on purpose, to check that they throw (`SortedAddFirstContract`, `SortedAddLastContract`; the user's decision on #30) |
| `UnusedVariable` | 1 | `AbstractBreakable.serialPersistentFields`, which serialization reads by reflection, and which keeps the serialized form compatible with 0.1.4 (the user's decision on #59) |
| `EqualsGetClass` | 1 | `BreakableObject`'s exact-class equality, which keeps `equals` symmetric |
| `ThreadPriorityCheck` | 9 | `Thread.yield()` in `ContentionCoordinator.yieldToScheduler` (#12), which makes contention likelier, and which its phase wait also calls on a machine with no more cores than workers, so as not to keep the awaited worker off its core (#54); in `HandOffRun.yieldToScheduler` (#54), the transfer tests' own loops, for the same reason, at the same rate; in `BreakableFuture`'s `cancel` and thread-unsafe wait (#44), where it widens the window of the fault its thread-unsafe breaks simulate; and in `ThreadSafeGetContract`'s completer (#44), whose spin for a getter must not starve it on a single core; and in `BreakableExecutorService.enqueue` (#45), where it widens the window of the faults `SUBMIT_IS_NOT_THREAD_SAFE` and `SHUTDOWN_IS_NOT_THREAD_SAFE` simulate; and in `BreakableLock.takeAfterYielding` and `BreakableReadWriteLock`'s `enterAfterYielding` and `releaseReadUnsafely` (#46), for the faults their thread-unsafe breaks simulate |
| `JavaUtilDate` | 1 | `BreakableLock`'s condition's `awaitUntil(Date)`, whose `Date` parameter is `Condition`'s own signature (the user's decision on #46, 2026-11-26) |

A new suppression of one of these kinds follows the rule; a new kind is the user's decision.

Not covered by the decision, and left from before #9: javac's own `unchecked`, `rawtypes` and `varargs` (13 in main code, without a reason), and IntelliJ inspection names that no gate reads (`DataFlowIssue`, `SameReturnValue`, `ConstantValue`, `MagicConstant`, `SortedCollectionWithNonComparableKeys` and others, in main and test code, and 20 `//noinspection` comments, 15 of them in main code). The one `@SuppressWarnings("checkstyle:MagicNumber")`, in `HashCodeMethodContract`, went with #70. Never add a `@SuppressWarnings("checkstyle:...")`.

## Javadoc and comments

- Built (#9): every public and protected type, method, constructor and field has a doc comment; the Javadoc gate (doclint all, `failOnWarnings`) fails a missing one, a broken link, a missing `@param` or bad HTML. The custom block tags are `@apiNote`, `@implSpec`, `@implNote` and `@complexity`; any other fails.
- Markdown `///` comments (JEP 467), with Markdown rather than HTML. 628 of the 631 main files use them (2026-11-26, #46); 14 still have a classic `/** ... */` comment. A new comment uses `///`.
- Checkstyle cannot read `///` comments, so it counts an import that only a `///` comment uses as unused. Write such a reference fully qualified (`@throws org.opentest4j.AssertionFailedError`).
- Habit: a public class's comment has a summary sentence, a description, and, where they apply, `## Usage Examples` (a ```` ```java ```` block), `## Thread Safety` and `## Implementation Notes`. `@param` and `@throws` name each argument's constraint and each exception's cause.
- Habit: a `Break` constant's comment gives its **Purpose** (the fault it simulates), **Effect** (how the method's behaviour changes), and **Affected Methods**, with a `@see` to the method (`BreakableCollection`, `BreakableList`, `BreakableSequencedCollection` follow it; not every breakable does yet).
- Habit: a contract's comment names the JDK method it tests, with a `@see`, and what its provider must supply.
- Inline comments say why, not what. Built (#10): `FIXME:`, `HACK:` and `XXX:` fail Checkstyle; empty blocks need a comment.
- Every Java file, main and test, starts with the SolisCode copyright header, before the `package` line and before a `package-info.java`'s `///` comment (833 files on 2026-11-26, #46: the 776 #71 gave it to and every file added since, among them the test class #71 split off, the 15 of the user guide's examples, #74, the README's, #76, and #46's 34; `LICENSE.txt`). Built (#71, parent 0.1.13): Checkstyle's `[CopyrightHeader]` fails a file without it. Put it on new files, exactly:

  ```java
  /*
   * Copyright 2025 Evan Bergstrom
   * SPDX-License-Identifier: Apache-2.0
   */
  ```

  The year is when the project began, not when the file was written (the parent guide's Checkstyle page, "The copyright header"). The old 15-line Apache header is gone (#71).

## Imports

Built (#10): Checkstyle `ImportOrder`, in the groups `java`, `javax`, `jakarta`, `net`, `org`, `com`, others, each separated by a blank line and sorted, then every static import in one sorted block. No wildcard imports, static ones included (`AvoidStarImport`; #10 expanded the 153 there were), and no unused or redundant imports. A class named only in a `///` comment is written fully qualified there, not imported ("Javadoc and comments").

## Tests

- JUnit 6 (Jupiter). No AssertJ and no mocking library: the assertions are JUnit's and this library's own.
- Built (#9): Surefire runs only `**/*Test.java`, and JUnit runs no abstract class (the parent's configuration; build-and-test.md, "Tests").
- Every test method has a `@DisplayName` with the readable sentence ("Test method names" above). Built (#10): every one has; #10 added the 210 that were missing. No gate checks it.
- A collection is tested with the contracts: implement the contract interfaces for it, with a provider (`WithXxx`, `XxxSupport`), rather than writing the same tests again. Each contract is shown to fail for each break of its breakable (build-and-test.md, "Contract coverage").
- Habit: for each method a contract tests, it covers the empty and non-empty cases, a null argument where nulls are and are not permitted, duplicates where they are and are not permitted, an argument of an incompatible type, and an unsupported method. A method that takes a collection is tested with an empty one, one with elements, one with a null, with duplicates and with an incompatible type. For an unsupported method that throws the wrong exception, use `assertThrowsDifferent`.
- Built (#10): no `Thread.sleep` in tests (Checkstyle); there is none. Tests do not depend on timing.
- Concurrency tests are bounded: they use `ContentionCoordinator`, whose workers have a start and a finish timeout and run on daemon threads, and `@ConcurrencyTest`, which adds a JUnit timeout. A deadlock fails the test instead of hanging the build (build-and-test.md, "Bounds").
- Tests are tagged with the annotations in `com.soliscode.test.annotations` (`@Slow`, `@VerySlow`, `@ExtremelySlow`, `@ConcurrencyTest`, `@Nondeterministic`); build-and-test.md, "Test tags", says what each means and which run by default.

## Errors and validation

- Habit: unchecked exceptions for programming errors. A refused argument throws `IllegalArgumentException` (or `NullPointerException` for a null) with a message that names the argument.
- An exception caught and rethrown keeps its cause. No exception is swallowed; an empty `catch` is allowed only if its variable is named `expected`, `ignore` or `ignored` (Checkstyle, Built, #10).
- No `System.out` or `System.err` in main or test code, doc-comment examples included (Checkstyle, Built, #10).
