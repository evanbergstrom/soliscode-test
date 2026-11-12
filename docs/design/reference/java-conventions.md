# Java conventions

The conventions the code follows, and the rules the gates enforce. Each section says whether the rule is **Built** (a gate enforces it and the code passes), **Partly built** (a gate enforces it but the code does not pass yet, with the task that fixes it), **Designed** (decided, with the task that builds it), or a **habit** of the code with no gate.

Related: [build-and-test.md](build-and-test.md) for how the gates run, [dependency-management.md](dependency-management.md) for versions. The Checkstyle rules are soliscode-parent's: [its build-and-test.md](https://gitlab.com/soliscode/soliscode-parent/-/blob/main/docs/design/reference/build-and-test.md), "Checkstyle", and `soliscode/checkstyle.xml` in soliscode-build-tools is the full list.

## Language level

- Built (#9): Java 25, from the parent (`maven.compiler.release` 25), with no `--enable-preview`: no preview feature is used.
- Records, sealed types, pattern matching, switch expressions and text blocks may be used. `Break` and `MethodStatus` are records.
- Built (#10): 4-space indentation, no tabs, lines at most 120 characters, files at most 2,000 lines, no trailing whitespace, one top-level type per file. `BreakableCollection` and `BreakableMap` were over 2,000 lines; #10 trimmed repetition in their Javadoc (the user's decision), and split `BreakableMapTest` in two.
- Built (#10): a type's members are in Checkstyle's `DeclarationOrder`: static fields, then instance fields, each public, protected, package-private, private; then constructors, together; then methods. Nested types may stand anywhere. A field is never moved above one its initializer reads.

## Packages

Built (#9). Every package is under `com.soliscode.test` (moved from `org.soliscode.test` by #9). Package names are lowercase. Every main package has a `package-info.java` (37 today) that says what it holds and is `@NullMarked`; a new package gets one. Test packages have none, except `contract.coverage`.

## Public API

- There is no `module-info.java`, so every public type in `com.soliscode.test.*` is public API. That includes the contracts' test methods: a user's test class implements a contract, and its methods' names are what the user's reports show.
- A change that breaks callers (a type, method or signature removed, renamed or changed, a result that changes) is recorded under `## Unreleased` in [RELEASES.md](../../../RELEASES.md) by the task that makes it. #10's renaming of the contract test methods was one.
- The assertions in `com.soliscode.test.assertions` use two JUnit classes that JUnit marks internal (`org.junit.platform.commons.util.StringUtils`, `UnrecoverableExceptions`): see [dependency-management.md](dependency-management.md).

## Naming

| Element | Convention |
|---------|------------|
| Types | PascalCase, no `I` prefix, no `Impl` suffix |
| Methods, parameters, locals, fields | camelCase |
| Constants | `UPPER_SNAKE_CASE`; a `Break` constant names the fault (`ADD_DOES_NOT_ADD_ELEMENT`) |
| Contracts | `XxxContract`: an interface of default test methods, in `com.soliscode.test.contract.<interface>` (`SizeContract`, `ListContract`). Each package's `XxxMethods` enum names the methods its contracts test |
| Breakables | `BreakableXxx`, an implementation of the JDK interface `Xxx` whose faults are its `Break` constants (`BreakableList`, `BreakableDeque`), built with its `BreakableXxxBuilder` where it has one |
| Providers | `XxxProvider` for a provider of one type; `CollectionProviders`, `MapProviders` and `Providers` for the static factories |
| Contract support | `XxxSupport` and `WithXxx` in `contract.support`: the mix-ins that give a contract its provider |
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

A finding that points at a fault is fixed, not suppressed. Built (#9, #30, #32, #33, #34, #35, #36, #37, #38): main code has 82 such suppressions, of these kinds:

| Check | Count | Where, and why |
|-------|-------|----------------|
| `JdkObsolete` | 12 | `LinkedList` in `BreakableDeque`, `BreakableQueue` and `CollectionProviders.provideLinkedList` (8): it is the one JDK `Deque` that permits null elements, which these types must allow. `IteratorProviders.linkedList` and `linkedListListIterator` (2, #37): they provide `LinkedList`'s own iterators, to test them. `BreakableSortedMap` and `BreakableSortedSet` (2): they are breakable `SortedMap` and `SortedSet` classes, and the finding is on the `implements` clause, so the class carries it |
| `NonApiType` | 8 | The providers of `ArrayList`, `LinkedList`, `HashSet`, `HashMap`, `TreeMap` and `LinkedHashMap`, and `WithArrayList` and `WithLinkedList`: they provide those concrete classes, to test them |
| `UndefinedEquals` | 4 | Wrappers whose equality is the wrapped collection's (`CollectionOnly`, `IterableOnly`, `BreakableIterable`, `BreakableQueue`) |
| `NullAway` | 44 | Code that passes null on purpose: contract tests that pass it to a JDK method (`ToArrayContract`, `PushContract`, `OfferFirstContract`, `OfferLastContract`; by #32, `PutContract`, `OfferWithTimeoutContract`, `DrainToContract`, `DrainToMaxElementsContract`; by #33, `PutFirstContract`, `PutLastContract`, `OfferFirstWithTimeoutContract`, `OfferLastWithTimeoutContract`; by #34, `TransferContract`, `TryTransferContract`, `TryTransferWithTimeoutContract`; and, by #35, 21 in `contract.concurrentmap`'s method contracts, one per null argument tested, and `BreakableConcurrentMap.forEach`, whose `FOR_EACH_IS_NOT_THREAD_SAFE` passes `null` for a key removed meanwhile, on purpose; and, by #36, 6 in `contract.concurrentnavigablemap`'s range-view contracts, one per null-bound test; and, by #38, `BreakableSpliterator.acceptNull`, whose `SPLITERATOR_FIRST_ELEMENT_IS_NULL` passes `null` in place of an element, on purpose) |
| `ReferenceEquality` | 6 | Identity comparisons: `AssertEqualsByIdentity`, `AssertEquals` (the same iterable is equal to itself without being iterated), `IterableTestUtils`' `containsByIdentity`, and `BreakableBlockingQueue`'s, `BreakableBlockingDeque`'s and `BoundedTransferQueue`'s refusals to drain into themselves (#32, #33, #34) |
| `DoNotCall` | 6 | Contract tests that call `addFirst` or `addLast` on a `SortedSet` on purpose, to check that they throw (`SortedAddFirstContract`, `SortedAddLastContract`; the user's decision on #30) |
| `EqualsGetClass` | 1 | `BreakableObject`'s exact-class equality, which keeps `equals` symmetric |
| `ThreadPriorityCheck` | 1 | `Thread.yield()` in `ContentionCoordinator`, which makes contention likelier |

A new suppression of one of these kinds follows the rule; a new kind is the user's decision.

Not covered by the decision, and left from before #9: javac's own `unchecked`, `rawtypes` and `varargs` (13 in main code, without a reason), and IntelliJ inspection names that no gate reads (`DataFlowIssue`, `SameReturnValue`, `ConstantValue`, `MagicConstant`, `SortedCollectionWithNonComparableKeys` and others, in main and test code, and 20 `//noinspection` comments, 15 of them in main code), and one `@SuppressWarnings("checkstyle:MagicNumber")` in `HashCodeMethodContract`. Never add a `@SuppressWarnings("checkstyle:...")`.

## Javadoc and comments

- Built (#9): every public and protected type, method, constructor and field has a doc comment; the Javadoc gate (doclint all, `failOnWarnings`) fails a missing one, a broken link, a missing `@param` or bad HTML. The custom block tags are `@apiNote`, `@implSpec`, `@implNote` and `@complexity`; any other fails.
- Markdown `///` comments (JEP 467), with Markdown rather than HTML. 456 of the 459 main files use them; 14 still have a classic `/** ... */` comment. A new comment uses `///`.
- Checkstyle cannot read `///` comments, so it counts an import that only a `///` comment uses as unused. Write such a reference fully qualified (`@throws org.opentest4j.AssertionFailedError`).
- Habit: a public class's comment has a summary sentence, a description, and, where they apply, `## Usage Examples` (a ```` ```java ```` block), `## Thread Safety` and `## Implementation Notes`. `@param` and `@throws` name each argument's constraint and each exception's cause.
- Habit: a `Break` constant's comment gives its **Purpose** (the fault it simulates), **Effect** (how the method's behaviour changes), and **Affected Methods**, with a `@see` to the method (`BreakableCollection`, `BreakableList`, `BreakableSequencedCollection` follow it; not every breakable does yet).
- Habit: a contract's comment names the JDK method it tests, with a `@see`, and what its provider must supply.
- Inline comments say why, not what. Built (#10): `FIXME:`, `HACK:` and `XXX:` fail Checkstyle; empty blocks need a comment.
- 256 of the 585 Java files carry the Apache 2.0 licence header (`LICENSE.txt`). Put it on new files.

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
