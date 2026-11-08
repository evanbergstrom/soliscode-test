package org.soliscode.test.contract.coverage;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.deque.DequeMethods;
import org.soliscode.test.contract.queue.QueueMethods;
import org.soliscode.test.contract.sequencedcollection.SequencedCollectionMethods;
import org.soliscode.test.contract.list.ListMethods;
import org.soliscode.test.contract.iterable.IterableMethods;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.annotations.VerySlow;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.breakable.BreakableCollection;
import org.soliscode.test.breakable.BreakableDeque;
import org.soliscode.test.breakable.BreakableIterable;
import org.soliscode.test.breakable.BreakableIterator;
import org.soliscode.test.breakable.BreakableList;
import org.soliscode.test.breakable.BreakableMap;
import org.soliscode.test.breakable.BreakableNavigableMap;
import org.soliscode.test.breakable.BreakableQueue;
import org.soliscode.test.breakable.BreakableSequencedCollection;
import org.soliscode.test.breakable.BreakableSequencedSet;
import org.soliscode.test.breakable.BreakableSet;
import org.soliscode.test.breakable.BreakableSortedMap;
import org.soliscode.test.breakable.BreakableSpliterator;
import org.soliscode.test.contract.CollectionContractConfig;
import org.soliscode.test.contract.DynamicCollectionContract;
import org.soliscode.test.contract.DynamicMapContract;
import org.soliscode.test.contract.collection.CollectionContract;
import org.soliscode.test.contract.collection.CollectionMethods;
import org.soliscode.test.contract.collection.ThreadSafeCollectionContract;
import org.soliscode.test.contract.deque.DequeContract;
import org.soliscode.test.contract.iterable.IterableContract;
import org.soliscode.test.contract.list.ListContract;
import org.soliscode.test.contract.map.MapContract;
import org.soliscode.test.contract.navigablemap.NavigableMapContract;
import org.soliscode.test.contract.object.SerializableContract;
import org.soliscode.test.contract.queue.QueueContract;
import org.soliscode.test.contract.sequencedcollection.SequencedCollectionContract;
import org.soliscode.test.contract.sequencedmap.SequencedMapContract;
import org.soliscode.test.contract.sequencedmap.SequencedMapMethods;
import org.soliscode.test.contract.sequencedset.SequencedSetContract;
import org.soliscode.test.contract.set.SetContract;
import org.soliscode.test.contract.sortedmap.SortedMapContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.contract.support.WithIntegerKeyAndStringValue;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.CollectionProviders;
import org.soliscode.test.provider.MapProvider;
import org.soliscode.test.provider.MapProviders;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/// Shows that each contract can pass and can fail: it passes against a correct JDK implementation and against an
/// unbroken breakable implementation, and fails against the breakable implementation with each of its breaks.
///
/// A break is applied on its own, through the breakable's builder. A break that only shows when a method is not
/// supported, or when incompatible types are refused, is applied with that configuration too (see
/// [#configure]). Thread-safety breaks are checked by the thread-safety contract only; its concurrency test methods are
/// run only there, since they are slow.
///
/// soliscode/soliscode-test#6.
@DisplayName("Each contract passes against a correct implementation and fails for each break")
public class ContractCoverageTest {

    private static final String THREAD_SAFETY_BREAK = "_IS_NOT_THREAD_SAFE";
    private static final String WRONG_UNSUPPORTED_BREAK = "_THROWS_WRONG_UNSUPPORTED_EXCEPTION";
    private static final String WRONG_INCOMPATIBLE_BREAK = "_THROWS_WRONG_INCOMPATIBLE_TYPE_EXCEPTION";
    private static final String WRONG_NULL_BREAK = "_THROWS_WRONG_NULL_EXCEPTION";
    private static final String WRONG_IF_NOT_SUPPORTED_BREAK = "_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED";

    /// The method enums searched for the method a wrong-exception-when-unsupported break names.
    private static final List<Class<? extends Enum<?>>> METHOD_ENUMS = List.of(CollectionMethods.class,
            IterableMethods.class, ListMethods.class, SequencedCollectionMethods.class, QueueMethods.class,
            DequeMethods.class);

    private static final CollectionContractConfig NO_NULLS = new CollectionContractConfig() {
        @Override
        public boolean permitNulls() {
            return false;
        }
    };

    private static final CollectionContractConfig STANDARD = new CollectionContractConfig() { };

    private static final CollectionContractConfig NO_INCOMPATIBLE_TYPES = new CollectionContractConfig() {
        @Override
        public boolean permitIncompatibleTypes() {
            return false;
        }
    };

    /// Why a break is not checked against a contract, or `null` if it is. A break is left out only where it is not a
    /// fault for the contract's type, or no test could ever see it; each reason says which.
    ///
    /// @param contract the contract
    /// @param key the break, as `SimpleClassName.FIELD`
    /// @return the reason, or `null`
    static String uncheckedReason(final Class<?> contract, final String key) {
        String field = key.substring(key.indexOf('.') + 1);
        boolean isSet = SetContract.class.isAssignableFrom(contract);
        if (field.endsWith(THREAD_SAFETY_BREAK) && contract != ThreadSafeCollectionContract.class) {
            return "a thread-safety break: ThreadSafeCollectionContract checks it";
        }
        if (field.equals("ADD_ALWAYS_RETURNS_TRUE") && !isSet) {
            return "not a fault here: add(E) always returns true for a collection that permits duplicates "
                    + "(Collection#add); SetContract checks it";
        }
        if (field.equals("OFFER_ALWAYS_RETURNS_TRUE")) {
            return "not a fault for BreakableQueue or BreakableDeque: they have no capacity, so offer(E) always "
                    + "returns true (Queue#offer); no contract here covers a bounded queue";
        }
        if (field.equals("CLEAR_IS_NOT_THREAD_SAFE")) {
            return "seen only by rare interleavings: ThreadSafeClearContract catches it in about 3 runs in 20 "
                    + "(measured 2026-11-07), too few to check here without making the build flaky";
        }
        if (field.equals("IS_EMPTY_IS_NOT_THREAD_SAFE") || field.equals("SIZE_IS_NOT_THREAD_SAFE")) {
            return "no test can see it: without the lock, isEmpty() and size() read one field, and any value read is "
                    + "one some interleaving with the lock could give";
        }
        if (contract == SequencedMapContract.class
                && SORTED_MAP_METHOD_BREAK.matcher(field).lookingAt()) {
            return "a SortedMap method's break, which SequencedMapContract does not test; SortedMapContract checks it";
        }
        return null;
    }

    private static final java.util.regex.Pattern SORTED_MAP_METHOD_BREAK =
            java.util.regex.Pattern.compile("(COMPARATOR|FIRST_KEY|LAST_KEY|HEAD_MAP|TAIL_MAP|SUB_MAP)_");

    /// The comparator the sorted-map contracts use: reverse order, so that a map that ignores it, or a
    /// comparator() that answers natural order (or `null`), shows.
    private static final java.util.Comparator<Integer> REVERSE = java.util.Comparator.reverseOrder();

    /// Default constructor.
    public ContractCoverageTest() {
        super();
    }

    // ========== The contracts ==========

    @TestFactory
    @DisplayName("IterableContract")
    Stream<DynamicNode> iterableContract() {
        return coverage(IterableContract.class, "ArrayList", JdkIterable::new,
                ContractRuns.withSuperclasses(BreakableIterable.class, BreakableIterator.class,
                        BreakableSpliterator.class),
                (key, b) -> {
                    var builder = new BreakableIterable.Builder<Integer>();
                    if (b != null) {
                        builder.addBreak(b);
                    }
                    unsupportedMethod(key.substring(key.indexOf('.') + 1)).ifPresent(builder::doesNotSupport);
                    return () -> new BrokenIterable(builder);
                });
    }

    @TestFactory
    @DisplayName("CollectionContract")
    Stream<DynamicNode> collectionContract() {
        return coverage(CollectionContract.class, "ArrayList", JdkCollection::new,
                collectionBreaks(BreakableCollection.class),
                (key, b) -> {
                    var builder = new BreakableCollection.Builder<Integer>();
                    var config = configure(builder, key, b);
                    return () -> new BrokenCollection(builder, config);
                });
    }

    @TestFactory
    @DisplayName("SequencedCollectionContract")
    Stream<DynamicNode> sequencedCollectionContract() {
        return coverage(SequencedCollectionContract.class, "ArrayList", JdkSequencedCollection::new,
                collectionBreaks(BreakableSequencedCollection.class),
                (key, b) -> {
                    var builder = new BreakableSequencedCollection.Builder<Integer>();
                    var config = configure(builder, key, b);
                    return () -> new BrokenSequencedCollection(builder, config);
                });
    }

    @TestFactory
    @DisplayName("ListContract")
    Stream<DynamicNode> listContract() {
        return coverage(ListContract.class, "ArrayList", JdkList::new,
                collectionBreaks(BreakableList.class),
                (key, b) -> {
                    var builder = new BreakableList.Builder<Integer>();
                    var config = configure(builder, key, b);
                    return () -> new BrokenList(builder, config);
                });
    }

    @TestFactory
    @DisplayName("SetContract")
    Stream<DynamicNode> setContract() {
        return coverage(SetContract.class, "HashSet", JdkSet::new,
                collectionBreaks(BreakableSet.class),
                (key, b) -> {
                    var builder = new BreakableSet.Builder<Integer>();
                    var config = configure(builder, key, b);
                    return () -> new BrokenSet(builder, config);
                });
    }

    @TestFactory
    @DisplayName("SequencedSetContract")
    Stream<DynamicNode> sequencedSetContract() {
        return coverage(SequencedSetContract.class, "LinkedHashSet", JdkSequencedSet::new,
                collectionBreaks(BreakableSequencedSet.class, BreakableSequencedCollection.class),
                (key, b) -> {
                    var builder = new BreakableSequencedSet.Builder<Integer>();
                    var config = configure(builder, key, b);
                    return () -> new BrokenSequencedSet(builder, config);
                });
    }

    @TestFactory
    @DisplayName("QueueContract")
    Stream<DynamicNode> queueContract() {
        return coverage(QueueContract.class, "LinkedList", JdkQueue::new,
                collectionBreaks(BreakableQueue.class),
                (key, b) -> {
                    var builder = new BreakableQueue.Builder<Integer>();
                    var config = configure(builder, key, b);
                    return () -> new BrokenQueue(builder, config);
                });
    }

    @TestFactory
    @DisplayName("DequeContract")
    Stream<DynamicNode> dequeContract() {
        return coverage(DequeContract.class, "LinkedList", JdkDeque::new,
                collectionBreaks(BreakableDeque.class),
                (key, b) -> {
                    var builder = new BreakableDeque.Builder<Integer>();
                    var config = configure(builder, key, b);
                    return () -> new BrokenDeque(builder, config);
                });
    }

    @TestFactory
    @DisplayName("MapContract")
    Stream<DynamicNode> mapContract() {
        return coverage(MapContract.class, "HashMap", JdkMap::new,
                ContractRuns.withSuperclasses(BreakableMap.class),
                (key, b) -> () -> new BrokenMap(b == null ? new BreakableMap.Builder<>()
                        : new BreakableMap.Builder<Integer, String>().addBreak(b)));
    }

    @TestFactory
    @DisplayName("SequencedMapContract")
    Stream<DynamicNode> sequencedMapContract() {
        // A sorted map is the breakable sequenced map; like TreeMap, it does not support putFirst or putLast.
        return coverage(SequencedMapContract.class, "LinkedHashMap", JdkSequencedMap::new,
                ContractRuns.withSuperclasses(BreakableSortedMap.class),
                (key, b) -> () -> new BrokenSequencedMap(sortedMapBuilder(b)));
    }

    @TestFactory
    @DisplayName("SortedMapContract")
    Stream<DynamicNode> sortedMapContract() {
        return coverage(SortedMapContract.class, "TreeMap(reverseOrder())", JdkSortedMap::new,
                ContractRuns.withSuperclasses(BreakableSortedMap.class),
                (key, b) -> () -> new BrokenSortedMap(sortedMapBuilder(b)));
    }

    @TestFactory
    @DisplayName("NavigableMapContract")
    Stream<DynamicNode> navigableMapContract() {
        return coverage(NavigableMapContract.class, "TreeMap(reverseOrder())", JdkNavigableMap::new,
                ContractRuns.withSuperclasses(BreakableNavigableMap.class),
                (key, b) -> {
                    var builder = new BreakableNavigableMap.Builder<Integer, String>()
                            .setComparator(REVERSE)
                            .doesNotSupport(SequencedMapMethods.PUT_FIRST)
                            .doesNotSupport(SequencedMapMethods.PUT_LAST);
                    if (b != null) {
                        builder.addBreak(b);
                    }
                    return () -> new BrokenNavigableMap(builder);
                });
    }

    @TestFactory
    @VerySlow
    @DisplayName("ThreadSafeCollectionContract")
    Stream<DynamicNode> threadSafeCollectionContract() {
        List<Method> all = ContractRuns.testMethods(ThreadSafeCollectionContract.class, true);
        Map<String, Break> breaks = new java.util.LinkedHashMap<>(
                ContractRuns.breaks(collectionBreaks(BreakableCollection.class)));
        breaks.keySet().removeIf(k -> !k.endsWith(THREAD_SAFETY_BREAK)
                || uncheckedReason(ThreadSafeCollectionContract.class, k) != null);
        return Stream.of(dynamicContainer("ThreadSafeCollectionContract", Stream.concat(
                Stream.of(passes("synchronizedList(ArrayList)", JdkThreadSafeCollection::new, all),
                        passes("unbroken thread-safe BreakableCollection",
                                () -> new BrokenThreadSafeCollection(threadSafeBuilder()), all)),
                breaks.entrySet().stream().map(e -> fails(e.getKey(),
                        () -> new BrokenThreadSafeCollection(threadSafeBuilder().addBreak(e.getValue())), all)))));
    }

    @TestFactory
    @DisplayName("SerializableContract")
    Stream<DynamicNode> serializableContract() {
        List<Method> all = ContractRuns.testMethods(SerializableContract.class, false);
        return Stream.of(dynamicContainer("SerializableContract", Stream.of(
                passes("ArrayList", JdkSerializable::new, all),
                passes("unbroken BreakableCollection", () -> new BrokenSerializable(new BreakableCollection.Builder<>()),
                        all),
                fails("BreakableObject.EQUALS_ALWAYS_RETURNS_FALSE", () -> new BrokenSerializable(
                        new BreakableCollection.Builder<Integer>()
                                .addBreak(org.soliscode.test.breakable.BreakableObject.EQUALS_ALWAYS_RETURNS_FALSE)),
                        all))));
    }

    // ========== The harness ==========

    private Stream<DynamicNode> coverage(final @NonNull Class<?> contract, final @NonNull String jdkName,
                                         final @NonNull Supplier<?> jdk, final @NonNull List<Class<?>> breakSources,
                                         final BiFunction<String, Break, Supplier<?>> broken) {
        List<Method> methods = ContractRuns.testMethods(contract, false);
        Stream<DynamicNode> passes = Stream.of(passes(jdkName, jdk, methods));
        if (broken == null) {
            return Stream.of(dynamicContainer(contract.getSimpleName(), passes));
        }
        Map<String, Break> breaks = ContractRuns.breaks(breakSources);
        Predicate<String> checked = k -> uncheckedReason(contract, k) == null;
        Stream<DynamicNode> unbroken = Stream.of(passes("unbroken breakable", broken.apply("", null), methods));
        // A break that needs a setup to show is checked only if the setup alone passes: then a failure with the
        // break is the break's. One check per setup, with no break.
        Map<String, String> setups = new java.util.TreeMap<>();
        breaks.keySet().stream().filter(checked).forEach(k -> {
            String setup = setupOf(k);
            if (!setup.isEmpty()) {
                setups.putIfAbsent(setup, k);
            }
        });
        Stream<DynamicNode> setupPasses = setups.entrySet().stream()
                .map(e -> passes("unbroken breakable with " + e.getKey(), broken.apply(e.getValue(), null), methods));
        Stream<DynamicNode> fails = breaks.entrySet().stream()
                .filter(e -> checked.test(e.getKey()))
                .map(e -> fails(e.getKey(), broken.apply(e.getKey(), e.getValue()), methods));
        return Stream.of(dynamicContainer(contract.getSimpleName(),
                Stream.of(passes, unbroken, setupPasses, fails).flatMap(s -> s)));
    }

    private static DynamicNode passes(final String name, final Supplier<?> contract, final List<Method> methods) {
        return dynamicTest("passes against " + name, () -> {
            List<String> failures = ContractRuns.failures(contract, methods);
            assertTrue(failures.isEmpty(), () -> failures.size() + " of " + methods.size()
                    + " test methods failed:\n  " + String.join("\n  ", failures));
        });
    }

    private static DynamicNode fails(final String breakKey, final Supplier<?> contract, final List<Method> methods) {
        return dynamicTest("fails with " + breakKey, () -> {
            List<String> failures = ContractRuns.failures(contract, methods, true);
            assertFalse(failures.isEmpty(),
                    () -> "no test method of the contract fails with " + breakKey + ": the contract cannot see it");
            assertFalse(ContractRuns.anyHang(failures),
                    () -> "the contract hangs with " + breakKey + ":\n  " + String.join("\n  ", failures));
        });
    }

    private static List<Class<?>> collectionBreaks(final Class<?> breakable, final Class<?>... extra) {
        List<Class<?>> sources = new ArrayList<>(List.of(extra));
        sources.add(BreakableIterator.class);
        sources.add(BreakableSpliterator.class);
        return ContractRuns.withSuperclasses(breakable, sources.toArray(Class<?>[]::new));
    }

    /// Describes the setup a break needs to show, or returns "" if it needs none.
    ///
    /// @param key the break, as `SimpleClassName.FIELD`
    /// @return the setup, as text
    static String setupOf(final String key) {
        String field = key.substring(key.indexOf('.') + 1);
        if (field.endsWith(WRONG_NULL_BREAK)) {
            return "nulls refused";
        } else if (field.endsWith(WRONG_INCOMPATIBLE_BREAK)) {
            return "incompatible types refused";
        }
        return unsupportedMethod(field).map(m -> ((Enum<?>) m).name() + " not supported").orElse("");
    }

    /// Applies a break to a collection builder, with the configuration it needs to show, and returns the
    /// configuration the contract must have. `b` null applies the configuration alone, with no break.
    ///
    /// - `X_THROWS_WRONG_UNSUPPORTED_EXCEPTION` and `X_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED`: method `X` is not
    ///   supported, so that the wrong exception replaces [UnsupportedOperationException].
    /// - `X_THROWS_WRONG_NULL_EXCEPTION`: nulls are refused, and the contract expects that.
    /// - `X_THROWS_WRONG_INCOMPATIBLE_TYPE_EXCEPTION`: incompatible types are refused, and the contract expects that.
    private static <B extends BreakableCollection.AbstractBuilder<B, C, Integer>, C extends BreakableCollection<Integer>>
        CollectionContractConfig configure(final B builder, final String key, final Break b) {
        String field = key.substring(key.indexOf('.') + 1);
        if (b != null) {
            builder.addBreak(b);
        }
        unsupportedMethod(field).ifPresent(builder::doesNotSupport);
        if (field.endsWith(WRONG_NULL_BREAK)) {
            builder.doesNotPermitNulls();
            return NO_NULLS;
        } else if (field.endsWith(WRONG_INCOMPATIBLE_BREAK)) {
            builder.doesNotPermitIncompatibleTypes(Integer.class);
            return NO_INCOMPATIBLE_TYPES;
        }
        return STANDARD;
    }

    /// The method a wrong-exception-when-unsupported break needs to be unsupported, found by name in the method
    /// enums, or empty for any other break.
    static Optional<InterfaceMethod> unsupportedMethod(final String field) {
        String method = null;
        for (String suffix : List.of(WRONG_UNSUPPORTED_BREAK, WRONG_IF_NOT_SUPPORTED_BREAK)) {
            if (field.endsWith(suffix)) {
                method = field.substring(0, field.length() - suffix.length());
            }
        }
        if (method == null) {
            return Optional.empty();
        }
        for (Class<? extends Enum<?>> methods : METHOD_ENUMS) {
            for (Enum<?> m : methods.getEnumConstants()) {
                if (m.name().equals(method)) {
                    return Optional.of((InterfaceMethod) m);
                }
            }
        }
        throw new AssertionError("no method " + method + " for break " + field);
    }

    /// A sorted map builder with a break (or none, for `b` null). A sorted map does not support putFirst or putLast
    /// (SortedMap#putFirst), so the builder says so and the contract expects it.
    private static BreakableSortedMap.Builder<Integer, String> sortedMapBuilder(final Break b) {
        var builder = new BreakableSortedMap.Builder<Integer, String>()
                .setComparator(REVERSE)
                .doesNotSupport(SequencedMapMethods.PUT_FIRST)
                .doesNotSupport(SequencedMapMethods.PUT_LAST);
        return b == null ? builder : builder.addBreak(b);
    }

    private static BreakableCollection.Builder<Integer> threadSafeBuilder() {
        return new BreakableCollection.Builder<Integer>().setSafe(true);
    }

    // ========== Contracts against JDK implementations ==========

    static final class JdkIterable extends AbstractTest
            implements IterableContract<ArrayList<Integer>, Integer>, WithIntegerElement {
        @Override
        public @NonNull CollectionProvider<ArrayList<Integer>, Integer> provider() {
            return CollectionProviders.provideArrayList(PROVIDER);
        }
    }

    static final class JdkCollection extends AbstractTest
            implements CollectionContract<ArrayList<Integer>, Integer>, WithIntegerElement {
        @Override
        public @NonNull CollectionProvider<ArrayList<Integer>, Integer> provider() {
            return CollectionProviders.provideArrayList(PROVIDER);
        }
    }

    static final class JdkSequencedCollection extends AbstractTest
            implements SequencedCollectionContract<ArrayList<Integer>, Integer>, WithIntegerElement {
        @Override
        public @NonNull CollectionProvider<ArrayList<Integer>, Integer> provider() {
            return CollectionProviders.provideArrayList(PROVIDER);
        }
    }

    static final class JdkList extends AbstractTest
            implements ListContract<ArrayList<Integer>, Integer>, WithIntegerElement {
        @Override
        public @NonNull CollectionProvider<ArrayList<Integer>, Integer> provider() {
            return CollectionProviders.provideArrayList(PROVIDER);
        }
    }

    static final class JdkSet extends AbstractTest
            implements SetContract<HashSet<Integer>, Integer>, WithIntegerElement {
        @Override
        public @NonNull CollectionProvider<HashSet<Integer>, Integer> provider() {
            return CollectionProviders.provideHashSet(PROVIDER);
        }

        @Override
        public boolean permitDuplicates() {
            return false;
        }
    }

    static final class JdkSequencedSet extends AbstractTest
            implements SequencedSetContract<LinkedHashSet<Integer>, Integer>, WithIntegerElement {
        @Override
        public @NonNull CollectionProvider<LinkedHashSet<Integer>, Integer> provider() {
            return CollectionProviders.from(LinkedHashSet::new, LinkedHashSet::new, LinkedHashSet::new, PROVIDER);
        }

        @Override
        public boolean permitDuplicates() {
            return false;
        }
    }

    static final class JdkQueue extends AbstractTest
            implements QueueContract<LinkedList<Integer>, Integer>, WithIntegerElement {
        @Override
        public @NonNull CollectionProvider<LinkedList<Integer>, Integer> provider() {
            return CollectionProviders.provideLinkedList(PROVIDER);
        }
    }

    static final class JdkDeque extends AbstractTest
            implements DequeContract<LinkedList<Integer>, Integer>, WithIntegerElement {
        @Override
        public @NonNull CollectionProvider<LinkedList<Integer>, Integer> provider() {
            return CollectionProviders.provideLinkedList(PROVIDER);
        }
    }

    static final class JdkThreadSafeCollection extends AbstractTest
            implements ThreadSafeCollectionContract<List<Integer>, Integer>, WithIntegerElement {
        @Override
        public @NonNull CollectionProvider<List<Integer>, Integer> provider() {
            return CollectionProviders.from(() -> Collections.synchronizedList(new ArrayList<>()),
                    (c) -> Collections.synchronizedList(new ArrayList<>(c)),
                    (Collection<Integer> c) -> Collections.synchronizedList(new ArrayList<>(c)), PROVIDER);
        }
    }

    static final class JdkSerializable extends AbstractTest
            implements SerializableContract<ArrayList<Integer>>, WithIntegerElement {
        @Override
        public @NonNull CollectionProvider<ArrayList<Integer>, Integer> provider() {
            return CollectionProviders.provideArrayList(PROVIDER);
        }
    }

    static final class JdkMap extends AbstractTest
            implements MapContract<HashMap<Integer, String>, Integer, String>,
            WithIntegerKeyAndStringValue<HashMap<Integer, String>> {
        @Override
        public @NonNull MapProvider<Integer, String, HashMap<Integer, String>> provider() {
            return MapProviders.provideHashMap(KEY_PROVIDER, VALUE_PROVIDER);
        }
    }

    static final class JdkSequencedMap extends AbstractTest
            implements SequencedMapContract<LinkedHashMap<Integer, String>, Integer, String>,
            WithIntegerKeyAndStringValue<LinkedHashMap<Integer, String>> {
        @Override
        public @NonNull MapProvider<Integer, String, LinkedHashMap<Integer, String>> provider() {
            return MapProviders.provideLinkedHashMap(KEY_PROVIDER, VALUE_PROVIDER);
        }
    }

    static final class JdkSortedMap extends AbstractTest
            implements SortedMapContract<TreeMap<Integer, String>, Integer, String>,
            WithIntegerKeyAndStringValue<TreeMap<Integer, String>> {
        JdkSortedMap() {
            doesNotSupportMethod(SequencedMapMethods.PUT_FIRST);
            doesNotSupportMethod(SequencedMapMethods.PUT_LAST);
        }

        @Override
        public @NonNull MapProvider<Integer, String, TreeMap<Integer, String>> provider() {
            return reverseTreeMaps();
        }
    }

    static final class JdkNavigableMap extends AbstractTest
            implements NavigableMapContract<TreeMap<Integer, String>, Integer, String>,
            WithIntegerKeyAndStringValue<TreeMap<Integer, String>> {
        JdkNavigableMap() {
            doesNotSupportMethod(SequencedMapMethods.PUT_FIRST);
            doesNotSupportMethod(SequencedMapMethods.PUT_LAST);
        }

        @Override
        public @NonNull MapProvider<Integer, String, TreeMap<Integer, String>> provider() {
            return reverseTreeMaps();
        }
    }

    // ========== Contracts against breakable implementations ==========

    static final class BrokenIterable extends DynamicCollectionContract<BreakableIterable<Integer>, Integer>
            implements IterableContract<BreakableIterable<Integer>, Integer> {
        BrokenIterable(final BreakableIterable.Builder<Integer> builder) {
            super(builder, WithIntegerElement.PROVIDER, STANDARD);
        }
    }

    static final class BrokenCollection extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements CollectionContract<BreakableCollection<Integer>, Integer> {
        BrokenCollection(final BreakableCollection.Builder<Integer> builder, final CollectionContractConfig config) {
            super(builder, WithIntegerElement.PROVIDER, config);
        }
    }

    static final class BrokenSequencedCollection
            extends DynamicCollectionContract<BreakableSequencedCollection<Integer>, Integer>
            implements SequencedCollectionContract<BreakableSequencedCollection<Integer>, Integer> {
        BrokenSequencedCollection(final BreakableSequencedCollection.Builder<Integer> builder,
                                  final CollectionContractConfig config) {
            super(builder, WithIntegerElement.PROVIDER, config);
        }
    }

    static final class BrokenList extends DynamicCollectionContract<BreakableList<Integer>, Integer>
            implements ListContract<BreakableList<Integer>, Integer> {
        BrokenList(final BreakableList.Builder<Integer> builder, final CollectionContractConfig config) {
            super(builder, WithIntegerElement.PROVIDER, config);
        }
    }

    static final class BrokenSet extends DynamicCollectionContract<BreakableSet<Integer>, Integer>
            implements SetContract<BreakableSet<Integer>, Integer> {
        BrokenSet(final BreakableSet.Builder<Integer> builder, final CollectionContractConfig config) {
            super(builder, WithIntegerElement.PROVIDER, noDuplicates(config));
        }
    }

    static final class BrokenSequencedSet extends DynamicCollectionContract<BreakableSequencedSet<Integer>, Integer>
            implements SequencedSetContract<BreakableSequencedSet<Integer>, Integer> {
        BrokenSequencedSet(final BreakableSequencedSet.Builder<Integer> builder,
                           final CollectionContractConfig config) {
            super(builder, WithIntegerElement.PROVIDER, noDuplicates(config));
        }
    }

    static final class BrokenQueue extends DynamicCollectionContract<BreakableQueue<Integer>, Integer>
            implements QueueContract<BreakableQueue<Integer>, Integer> {
        BrokenQueue(final BreakableQueue.Builder<Integer> builder, final CollectionContractConfig config) {
            super(builder, WithIntegerElement.PROVIDER, config);
        }
    }

    static final class BrokenDeque extends DynamicCollectionContract<BreakableDeque<Integer>, Integer>
            implements DequeContract<BreakableDeque<Integer>, Integer> {
        BrokenDeque(final BreakableDeque.Builder<Integer> builder, final CollectionContractConfig config) {
            super(builder, WithIntegerElement.PROVIDER, config);
        }
    }

    static final class BrokenThreadSafeCollection
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements ThreadSafeCollectionContract<BreakableCollection<Integer>, Integer> {
        BrokenThreadSafeCollection(final BreakableCollection.Builder<Integer> builder) {
            super(builder, WithIntegerElement.PROVIDER, STANDARD);
        }
    }

    static final class BrokenSerializable extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements SerializableContract<BreakableCollection<Integer>> {
        BrokenSerializable(final BreakableCollection.Builder<Integer> builder) {
            super(builder, WithIntegerElement.PROVIDER, STANDARD);
        }
    }

    static final class BrokenMap extends DynamicMapContract<BreakableMap<Integer, String>, Integer, String>
            implements MapContract<BreakableMap<Integer, String>, Integer, String> {
        BrokenMap(final BreakableMap.Builder<Integer, String> builder) {
            super(builder, WithIntegerKeyAndStringValue.KEY_PROVIDER, WithIntegerKeyAndStringValue.VALUE_PROVIDER);
        }
    }

    static final class BrokenSequencedMap
            extends DynamicMapContract<BreakableSortedMap<Integer, String>, Integer, String>
            implements SequencedMapContract<BreakableSortedMap<Integer, String>, Integer, String> {
        BrokenSequencedMap(final BreakableSortedMap.Builder<Integer, String> builder) {
            super(builder, WithIntegerKeyAndStringValue.KEY_PROVIDER, WithIntegerKeyAndStringValue.VALUE_PROVIDER);
        }
    }

    static final class BrokenSortedMap
            extends DynamicMapContract<BreakableSortedMap<Integer, String>, Integer, String>
            implements SortedMapContract<BreakableSortedMap<Integer, String>, Integer, String> {
        BrokenSortedMap(final BreakableSortedMap.Builder<Integer, String> builder) {
            super(builder, WithIntegerKeyAndStringValue.KEY_PROVIDER, WithIntegerKeyAndStringValue.VALUE_PROVIDER);
        }
    }

    static final class BrokenNavigableMap
            extends DynamicMapContract<BreakableNavigableMap<Integer, String>, Integer, String>
            implements NavigableMapContract<BreakableNavigableMap<Integer, String>, Integer, String> {
        BrokenNavigableMap(final BreakableNavigableMap.Builder<Integer, String> builder) {
            super(builder, WithIntegerKeyAndStringValue.KEY_PROVIDER, WithIntegerKeyAndStringValue.VALUE_PROVIDER);
        }
    }

    /// Tree maps in reverse key order, with keys and values from the usual providers.
    private static MapProvider<Integer, String, TreeMap<Integer, String>> reverseTreeMaps() {
        return MapProviders.from(() -> new TreeMap<>(REVERSE),
                (m) -> new TreeMap<>(m),
                (m) -> {
                    TreeMap<Integer, String> map = new TreeMap<>(REVERSE);
                    map.putAll(m);
                    return map;
                },
                WithIntegerKeyAndStringValue.KEY_PROVIDER, WithIntegerKeyAndStringValue.VALUE_PROVIDER);
    }

    private static CollectionContractConfig noDuplicates(final CollectionContractConfig config) {
        return new CollectionContractConfig() {
            @Override
            public boolean permitDuplicates() {
                return false;
            }

            @Override
            public boolean permitNulls() {
                return config.permitNulls();
            }

            @Override
            public boolean permitIncompatibleTypes() {
                return config.permitIncompatibleTypes();
            }
        };
    }
}
