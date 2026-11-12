package com.soliscode.test.contract.coverage;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.breakable.Break;
import com.soliscode.test.breakable.BreakableBlockingDeque;
import com.soliscode.test.breakable.BreakableBlockingQueue;
import com.soliscode.test.breakable.BreakableCollection;
import com.soliscode.test.breakable.BreakableConcurrentMap;
import com.soliscode.test.breakable.BreakableConcurrentNavigableMap;
import com.soliscode.test.breakable.BreakableDeque;
import com.soliscode.test.breakable.BreakableIterable;
import com.soliscode.test.breakable.BreakableIterator;
import com.soliscode.test.breakable.BreakableList;
import com.soliscode.test.breakable.BreakableListIterator;
import com.soliscode.test.breakable.BreakableMap;
import com.soliscode.test.breakable.BreakableNavigableMap;
import com.soliscode.test.breakable.BreakableNavigableSet;
import com.soliscode.test.breakable.BreakableQueue;
import com.soliscode.test.breakable.BreakableSequencedCollection;
import com.soliscode.test.breakable.BreakableSequencedSet;
import com.soliscode.test.breakable.BreakableSet;
import com.soliscode.test.breakable.BreakableSortedMap;
import com.soliscode.test.breakable.BreakableSortedSet;
import com.soliscode.test.breakable.BreakableSpliterator;
import com.soliscode.test.breakable.BreakableSpliteratorOfDouble;
import com.soliscode.test.breakable.BreakableSpliteratorOfInt;
import com.soliscode.test.breakable.BreakableSpliteratorOfLong;
import com.soliscode.test.breakable.BreakableTransferQueue;
import com.soliscode.test.contract.CollectionContractConfig;
import com.soliscode.test.contract.DynamicCollectionContract;
import com.soliscode.test.contract.DynamicMapContract;
import com.soliscode.test.contract.blockingdeque.BlockingDequeContract;
import com.soliscode.test.contract.blockingdeque.ThreadSafeBlockingDequeContract;
import com.soliscode.test.contract.blockingqueue.BlockingQueueContract;
import com.soliscode.test.contract.blockingqueue.ThreadSafeBlockingQueueContract;
import com.soliscode.test.contract.collection.CollectionContract;
import com.soliscode.test.contract.collection.CollectionMethods;
import com.soliscode.test.contract.collection.ThreadSafeCollectionContract;
import com.soliscode.test.contract.concurrentmap.ConcurrentMapContract;
import com.soliscode.test.contract.concurrentmap.ThreadSafeConcurrentMapContract;
import com.soliscode.test.contract.concurrentnavigablemap.ConcurrentNavigableMapContract;
import com.soliscode.test.contract.deque.DequeContract;
import com.soliscode.test.contract.deque.DequeMethods;
import com.soliscode.test.contract.iterable.IterableContract;
import com.soliscode.test.contract.iterable.IterableMethods;
import com.soliscode.test.contract.iterator.IteratorContract;
import com.soliscode.test.contract.iterator.WeaklyConsistentIteratorContract;
import com.soliscode.test.contract.list.ListContract;
import com.soliscode.test.contract.list.ListMethods;
import com.soliscode.test.contract.listiterator.ListIteratorContract;
import com.soliscode.test.contract.listiterator.ListIteratorMethods;
import com.soliscode.test.contract.map.MapContract;
import com.soliscode.test.contract.navigablemap.NavigableMapContract;
import com.soliscode.test.contract.navigableset.NavigableSetContract;
import com.soliscode.test.contract.object.SerializableContract;
import com.soliscode.test.contract.queue.QueueContract;
import com.soliscode.test.contract.queue.QueueMethods;
import com.soliscode.test.contract.sequencedcollection.SequencedCollectionContract;
import com.soliscode.test.contract.sequencedcollection.SequencedCollectionMethods;
import com.soliscode.test.contract.sequencedmap.SequencedMapContract;
import com.soliscode.test.contract.sequencedmap.SequencedMapMethods;
import com.soliscode.test.contract.sequencedset.SequencedSetContract;
import com.soliscode.test.contract.set.SetContract;
import com.soliscode.test.contract.sortedmap.SortedMapContract;
import com.soliscode.test.contract.sortedset.SortedSetContract;
import com.soliscode.test.contract.spliterator.SpliteratorContract;
import com.soliscode.test.contract.spliterator.SpliteratorOfDoubleContract;
import com.soliscode.test.contract.spliterator.SpliteratorOfIntContract;
import com.soliscode.test.contract.spliterator.SpliteratorOfLongContract;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.contract.support.WithIntegerKeyAndStringValue;
import com.soliscode.test.contract.transferqueue.ThreadSafeTransferQueueContract;
import com.soliscode.test.contract.transferqueue.TransferQueueContract;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.CollectionProviders;
import com.soliscode.test.provider.IteratorProviders;
import com.soliscode.test.provider.MapProvider;
import com.soliscode.test.provider.MapProviders;
import com.soliscode.test.provider.SpliteratorProviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    /// The breaks that act only on a change to an iterator's source other than through it (#55).
    private static final Set<String> WEAK_CONSISTENCY_BREAKS = Set.of("ITERATOR_IS_FAIL_FAST",
            "ITERATOR_RETURNS_AN_ELEMENT_TWICE_AFTER_A_CHANGE", "ITERATOR_SKIPS_AN_ELEMENT_AFTER_A_CHANGE");
    private static final String WRONG_UNSUPPORTED_BREAK = "_THROWS_WRONG_UNSUPPORTED_EXCEPTION";
    private static final String WRONG_INCOMPATIBLE_BREAK = "_THROWS_WRONG_INCOMPATIBLE_TYPE_EXCEPTION";
    private static final String WRONG_NULL_BREAK = "_THROWS_WRONG_NULL_EXCEPTION";
    private static final String WRONG_IF_NOT_SUPPORTED_BREAK = "_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED";

    /// The method enums searched for the method a wrong-exception-when-unsupported break names.
    private static final List<Class<? extends Enum<?>>> METHOD_ENUMS = List.of(CollectionMethods.class,
            IterableMethods.class, ListMethods.class, SequencedCollectionMethods.class, QueueMethods.class,
            DequeMethods.class, ListIteratorMethods.class);

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

    /// A sorted set's configuration: no nulls, duplicates or elements of other types, whatever the setup a break
    /// needs, since a sorted set refuses them all.
    private static final CollectionContractConfig SORTED = new CollectionContractConfig() {
        @Override
        public boolean permitDuplicates() {
            return false;
        }

        @Override
        public boolean permitNulls() {
            return false;
        }

        @Override
        public boolean permitIncompatibleTypes() {
            return false;
        }
    };

    /// The spliterator breaks that are faults only for a SIZED or ORDERED spliterator.
    private static final java.util.Set<String> BLOCKING_QUEUE_SPLITERATOR_BREAKS = java.util.Set.of(
            "SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE",
            "SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE",
            "SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO");

    /// The breaks of `BreakableSpliterator` added for the spliterator contracts (#38). The Iterable and collection
    /// contracts, whose break sources include `BreakableSpliterator`, are not run with them.
    private static final java.util.Set<String> SPLITERATOR_CONTRACT_BREAKS = java.util.Set.of(
            "SPLITERATOR_TRY_ADVANCE_IGNORES_NULL_ACTION", "SPLITERATOR_TRY_ADVANCE_SWALLOWS_ACTION_EXCEPTION",
            "SPLITERATOR_FOR_EACH_REMAINING_IGNORES_NULL_ACTION",
            "SPLITERATOR_FOR_EACH_REMAINING_SWALLOWS_ACTION_EXCEPTION", "SPLITERATOR_RETURNS_FIRST_ELEMENT_TWICE",
            "SPLITERATOR_FIRST_ELEMENT_IS_NULL", "SPLITERATOR_TRY_SPLIT_RETURNS_SUFFIX",
            "SPLITERATOR_TRY_SPLIT_LOSES_PART", "SPLITERATOR_TRY_SPLIT_NEVER_RETURNS_NULL",
            "SPLITERATOR_TRY_SPLIT_PART_IS_NOT_SIZED", "SPLITERATOR_ESTIMATE_SIZE_GROWS_AFTER_SPLIT",
            "SPLITERATOR_CHARACTERISTICS_CHANGE_AFTER_TRAVERSAL", "SPLITERATOR_REPORTS_IMMUTABLE",
            "SPLITERATOR_REPORTS_CONCURRENT", "SPLITERATOR_GET_COMPARATOR_RETURNS_NATURAL_ORDER",
            "SPLITERATOR_CHARACTERISTICS_LEAVE_OUT_ORDERED", "SPLITERATOR_CHARACTERISTICS_LEAVE_OUT_SIZED",
            "SPLITERATOR_GET_COMPARATOR_ALWAYS_THROWS");

    /// The breaks each primitive spliterator contract's own tests are shown to fail for (#38).
    private static final List<String> PRIMITIVE_SPLITERATOR_BREAKS = List.of(
            "SPLITERATOR_TRY_ADVANCE_DOES_NOT_CALL_ACTION", "SPLITERATOR_TRY_ADVANCE_ALWAYS_RETURNS_TRUE",
            "SPLITERATOR_SKIPS_FIRST_ELEMENT", "SPLITERATOR_TRY_ADVANCE_IGNORES_NULL_ACTION",
            "SPLITERATOR_TRY_ADVANCE_SWALLOWS_ACTION_EXCEPTION", "SPLITERATOR_FOR_EACH_REMAINING_DOES_NOT_CALL_ACTION",
            "SPLITERATOR_FOR_EACH_REMAINING_IGNORES_NULL_ACTION",
            "SPLITERATOR_FOR_EACH_REMAINING_SWALLOWS_ACTION_EXCEPTION", "SPLITERATOR_TRY_SPLIT_LOSES_PART",
            "SPLITERATOR_TRY_SPLIT_RETURNS_SUFFIX");

    /// The capacity of the bounded blocking queues: room for what the inherited contracts add, small enough for the
    /// blocking-queue contracts to fill.
    private static final int QUEUE_CAPACITY = 100;

    private static final java.util.regex.Pattern SORTED_MAP_METHOD_BREAK =
            java.util.regex.Pattern.compile("(COMPARATOR|FIRST_KEY|LAST_KEY|HEAD_MAP|TAIL_MAP|SUB_MAP)_");

    /// The comparator the sorted-map contracts use: reverse order, so that a map that ignores it, or a
    /// comparator() that answers natural order (or `null`), shows.
    private static final java.util.Comparator<Integer> REVERSE = java.util.Comparator.reverseOrder();

    /// Default constructor.
    public ContractCoverageTest() {
        super();
    }

    /// Why a break is not checked against a contract, or `null` if it is. A break is left out only where it is not a
    /// fault for the contract's type, where no test could ever see it, or where another contract, named in the
    /// reason, checks it; each reason says which.
    ///
    /// @param contract the contract
    /// @param key the break, as `SimpleClassName.FIELD`
    /// @return the reason, or `null`
    static String uncheckedReason(final Class<?> contract, final String key) {
        String field = key.substring(key.indexOf('.') + 1);
        boolean isSet = SetContract.class.isAssignableFrom(contract);
        boolean blockingQueueBreak = key.startsWith(BreakableBlockingQueue.class.getSimpleName() + ".");
        boolean blockingDequeBreak = key.startsWith(BreakableBlockingDeque.class.getSimpleName() + ".");
        boolean transferQueueBreak = key.startsWith(BreakableTransferQueue.class.getSimpleName() + ".");
        boolean concurrentMapBreak = key.startsWith(BreakableConcurrentMap.class.getSimpleName() + ".");
        if (field.endsWith(THREAD_SAFETY_BREAK) && contract == ThreadSafeConcurrentMapContract.class) {
            return concurrentMapBreak ? null : "not a fault for a BreakableConcurrentMap: its store is a concurrent "
                    + "map, and the breakable takes no lock of its own, so the break changes nothing there";
        }
        if (field.endsWith(THREAD_SAFETY_BREAK) && concurrentMapBreak) {
            return "a thread-safety break: ThreadSafeConcurrentMapContract checks it, and ConcurrentMapContract checks "
                    + "that it is correct on one thread";
        }
        if (field.endsWith(THREAD_SAFETY_BREAK) && contract == ThreadSafeTransferQueueContract.class) {
            // BreakableBlockingQueue's take, poll and drainTo run on the transfer queue's store: faults for it too.
            return transferQueueBreak || blockingQueueBreak ? null : "not a fault for a BreakableTransferQueue: its "
                    + "store (a LinkedTransferQueue, or its own bounded store on one lock) is thread safe without the "
                    + "breakable's lock, so running without the lock changes nothing; ThreadSafeCollectionContract "
                    + "covers it on BreakableCollection, or gives its reason";
        }
        if (field.endsWith(THREAD_SAFETY_BREAK) && transferQueueBreak) {
            return "a thread-safety break: ThreadSafeTransferQueueContract checks it";
        }
        if (field.endsWith(THREAD_SAFETY_BREAK) && contract == ThreadSafeBlockingDequeContract.class) {
            return blockingDequeBreak ? null : "not a fault for a BreakableBlockingDeque: its store is a JDK "
                    + "BlockingDeque, thread safe without the breakable's lock, so running without the lock changes "
                    + "nothing; ThreadSafeCollectionContract covers it on BreakableCollection, or gives its reason";
        }
        if (field.endsWith(THREAD_SAFETY_BREAK) && blockingDequeBreak) {
            return "a thread-safety break: ThreadSafeBlockingDequeContract checks it";
        }
        if (field.endsWith(THREAD_SAFETY_BREAK) && contract == ThreadSafeBlockingQueueContract.class) {
            return blockingQueueBreak ? null : "not a fault for a BreakableBlockingQueue: its store is a JDK "
                    + "BlockingQueue, thread safe without the breakable's lock, so running without the lock changes "
                    + "nothing; ThreadSafeCollectionContract covers it on BreakableCollection, or gives its reason";
        }
        if (field.endsWith(THREAD_SAFETY_BREAK) && blockingQueueBreak) {
            return "a thread-safety break: ThreadSafeBlockingQueueContract checks it";
        }
        if (field.endsWith(THREAD_SAFETY_BREAK) && contract != ThreadSafeCollectionContract.class) {
            return "a thread-safety break: ThreadSafeCollectionContract checks it";
        }
        if (WEAK_CONSISTENCY_BREAKS.contains(field)) {
            boolean testsWeakConsistency = WeaklyConsistentIteratorContract.class.isAssignableFrom(contract)
                    || BlockingQueueContract.class.isAssignableFrom(contract)
                    || ConcurrentMapContract.class.isAssignableFrom(contract);
            return testsWeakConsistency ? null : "a weak-consistency break: the weakly consistent iterator tests check "
                    + "it, and no other contract's run changes the source during a traversal on one thread";
        }
        if (field.equals("ITERATOR_FOR_EACH_REMAINING_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED")
                && IteratorContract.class.isAssignableFrom(contract)) {
            return "not a fault for IteratorContract: forEachRemaining is not optional (Iterator#forEachRemaining), so "
                    + "the setup this break needs, forEachRemaining unsupported, fails the contract without the break; "
                    + "IterableContract checks it";
        }
        boolean spliteratorContract = SpliteratorContract.class.isAssignableFrom(contract);
        if (SPLITERATOR_CONTRACT_BREAKS.contains(field) && !spliteratorContract) {
            return "a spliterator break added for SpliteratorContract (#38), which checks it; the Iterable and "
                    + "collection contracts are not run with it";
        }
        boolean primitiveSpliteratorContract = SpliteratorOfIntContract.class.isAssignableFrom(contract)
                || SpliteratorOfLongContract.class.isAssignableFrom(contract)
                || SpliteratorOfDoubleContract.class.isAssignableFrom(contract);
        if (field.equals("SPLITERATOR_REPORTS_IMMUTABLE") && primitiveSpliteratorContract) {
            return "no test can see it here: an array's spliterator already reports IMMUTABLE (Arrays#spliterator), "
                    + "so the break changes nothing on these subjects; SpliteratorContract checks it";
        }
        if (field.equals("SPLITERATOR_GET_COMPARATOR_ALWAYS_THROWS") && spliteratorContract) {
            return "no test can see it on these subjects: their spliterators (an ArrayList's, an array's) are not "
                    + "SORTED, so getComparator must throw IllegalStateException, as the break makes it; "
                    + "SpliteratorContract's run on a natural-order TreeSet checks it in sortedCharacteristicHolds";
        }
        if (field.equals("SPLITERATOR_GET_COMPARATOR_RETURNS_NATURAL_ORDER") && spliteratorContract) {
            return "no test can see it on these subjects: their spliterators (an ArrayList's, an array's) are not "
                    + "SORTED, so getComparator throws and the break, which changes only a null comparator, changes "
                    + "nothing; SpliteratorContract's run on a natural-order TreeSet checks it in "
                    + "sortedCharacteristicHolds";
        }
        if (field.equals("ADD_ALWAYS_RETURNS_TRUE") && !isSet) {
            return "not a fault here: add(E) always returns true for a collection that permits duplicates "
                    + "(Collection#add); SetContract checks it";
        }
        if (field.equals("SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL")) {
            return "not a fault: Spliterator#trySplit may return null for any reason, and the contracts accept it "
                    + "(a ConcurrentSkipListSet's spliterator does, where its random index levels allow no split)";
        }
        if (field.equals("OFFER_ALWAYS_RETURNS_TRUE") && !BlockingQueueContract.class.isAssignableFrom(contract)) {
            return "not a fault for BreakableQueue or BreakableDeque: they have no capacity, so offer(E) always "
                    + "returns true (Queue#offer); BlockingQueueContract checks it on a bounded queue";
        }
        if (BLOCKING_QUEUE_SPLITERATOR_BREAKS.contains(field)
                && BlockingQueueContract.class.isAssignableFrom(contract)) {
            return "not a fault for a BlockingQueue: its spliterator need not be SIZED or ORDERED "
                    + "(ArrayBlockingQueue's and LinkedBlockingQueue's report CONCURRENT, ORDERED and NONNULL, not "
                    + "SIZED, and a BlockingQueue need not be FIFO), so an estimate of Long.MAX_VALUE, an exact size "
                    + "of -1 and no characteristics are all allowed (Spliterator#estimateSize, #getExactSizeIfKnown)";
        }
        if (field.equals("CLEAR_IS_NOT_THREAD_SAFE")) {
            return "seen only by rare interleavings: ThreadSafeClearContract catches it in about 3 runs in 20 "
                    + "(measured 2026-11-07), too few to check here without making the build flaky";
        }
        if (field.equals("IS_EMPTY_IS_NOT_THREAD_SAFE") || field.equals("SIZE_IS_NOT_THREAD_SAFE")) {
            return "no test can see it: without the lock, isEmpty() and size() read one field, and any value read is "
                    + "one some interleaving with the lock could give";
        }
        if ((field.equals("ADD_FIRST_ADDS_TO_END") || field.equals("ADD_LAST_ADDS_TO_FRONT"))
                && SortedSetContract.class.isAssignableFrom(contract)) {
            return "not a fault for a sorted set: addFirst and addLast both throw UnsupportedOperationException "
                    + "(SortedSet#addFirst), so the one it would call throws as the one called does";
        }
        if (contract == SequencedMapContract.class
                && SORTED_MAP_METHOD_BREAK.matcher(field).lookingAt()) {
            return "a SortedMap method's break, which SequencedMapContract does not test; SortedMapContract checks it";
        }
        return null;
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
    @DisplayName("IteratorContract")
    Stream<DynamicNode> iteratorContract() {
        List<Method> methods = ContractRuns.testMethods(IteratorContract.class, false);
        return Stream.concat(
                coverage(IteratorContract.class, "ArrayList's iterator",
                        () -> new IteratorSubjects.JdkIterator(IteratorProviders.arrayList()),
                        ContractRuns.withSuperclasses(BreakableIterator.class),
                        (key, b) -> {
                            Optional<InterfaceMethod> unsupported =
                                    unsupportedMethod(key.substring(key.indexOf('.') + 1));
                            return () -> new IteratorSubjects.BrokenIterator(b, unsupported);
                        }),
                Stream.of(passes("LinkedList's iterator",
                                () -> new IteratorSubjects.JdkIterator(IteratorProviders.linkedList()), methods),
                        passes("List.of's iterator, remove() unsupported",
                                () -> new IteratorSubjects.JdkIterator(IteratorProviders.fromList(List::copyOf),
                                        IterableMethods.ITERATOR_REMOVE), methods),
                        passes("Arrays.asList's iterator, remove() unsupported",
                                () -> new IteratorSubjects.JdkIterator(IteratorProviders.fromList(
                                        l -> Arrays.asList(l.toArray(Integer[]::new))),
                                        IterableMethods.ITERATOR_REMOVE), methods),
                        passes("PriorityQueue's iterator, unordered",
                                () -> new IteratorSubjects.JdkIterator(
                                        IteratorProviders.fromCollection(PriorityQueue::new)), methods)));
    }

    @TestFactory
    @DisplayName("WeaklyConsistentIteratorContract")
    Stream<DynamicNode> weaklyConsistentIteratorContract() {
        List<Method> methods = ContractRuns.testMethods(WeaklyConsistentIteratorContract.class, false);
        return Stream.concat(
                coverage(WeaklyConsistentIteratorContract.class, "ConcurrentLinkedQueue's iterator",
                        () -> new IteratorSubjects.JdkWeakIterator(
                                IteratorProviders.fromCollection(ConcurrentLinkedQueue::new)),
                        ContractRuns.withSuperclasses(BreakableIterator.class),
                        (key, b) -> {
                            Optional<InterfaceMethod> unsupported =
                                    unsupportedMethod(key.substring(key.indexOf('.') + 1));
                            return () -> new IteratorSubjects.BrokenWeakIterator(b, unsupported);
                        }),
                Stream.of(passes("ConcurrentHashMap.newKeySet()'s iterator",
                                () -> new IteratorSubjects.JdkWeakIterator(IteratorProviders.fromCollection(l -> {
                                    Set<Integer> set = ConcurrentHashMap.newKeySet();
                                    set.addAll(l);
                                    return set;
                                })), methods),
                        passes("ConcurrentSkipListSet's iterator", () -> new IteratorSubjects.JdkWeakIterator(
                                IteratorProviders.fromCollection(ConcurrentSkipListSet::new)), methods),
                        passes("CopyOnWriteArrayList's iterator (a snapshot), remove() unsupported",
                                () -> new IteratorSubjects.JdkWeakIterator(
                                        IteratorProviders.fromList(CopyOnWriteArrayList::new),
                                        IterableMethods.ITERATOR_REMOVE), methods),
                        passes("LinkedBlockingQueue's iterator", () -> new IteratorSubjects.JdkWeakIterator(
                                IteratorProviders.fromCollection(LinkedBlockingQueue::new)), methods),
                        passes("ArrayBlockingQueue's iterator, the queue full when made",
                                () -> new IteratorSubjects.JdkWeakIterator(IteratorProviders.fromCollection(
                                        l -> new ArrayBlockingQueue<>(Math.max(1, l.size()), false, l))), methods),
                        fails("ArrayList's iterator, which is fail-fast",
                                () -> new IteratorSubjects.JdkWeakIterator(IteratorProviders.arrayList()), methods)));
    }

    /// Each weak-consistency test of the concurrent contracts, by name, fails for each weak-consistency break, and
    /// passes when the test class opts out: another test of the contract fails first for each break (the breaks reach
    /// `iterator()` too), and would otherwise hide one that cannot see it (#55).
    @TestFactory
    @DisplayName("The concurrent contracts' weak-consistency tests")
    Stream<DynamicNode> weakConsistencyTests() {
        List<Break> weak = List.of(BreakableIterator.ITERATOR_IS_FAIL_FAST,
                BreakableIterator.ITERATOR_RETURNS_AN_ELEMENT_TWICE_AFTER_A_CHANGE,
                BreakableIterator.ITERATOR_SKIPS_AN_ELEMENT_AFTER_A_CHANGE);
        List<Method> deque = named(BlockingDequeContract.class,
                "iteratorWhenQueueChangesDuringTraversalIsWeaklyConsistent",
                "descendingIteratorWhenDequeChangesDuringTraversalIsWeaklyConsistent");
        List<Method> map = named(ConcurrentNavigableMapContract.class,
                "keySetIteratorWhenMapChangesDuringTraversalIsWeaklyConsistent",
                "valuesIteratorWhenMapChangesDuringTraversalIsWeaklyConsistent",
                "entrySetIteratorWhenMapChangesDuringTraversalIsWeaklyConsistent",
                "keySetDescendingIteratorWhenMapChangesDuringTraversalIsWeaklyConsistent",
                "navigableKeySetIteratorWhenMapChangesDuringTraversalIsWeaklyConsistent",
                "navigableKeySetDescendingIteratorWhenMapChangesDuringTraversalIsWeaklyConsistent",
                "descendingKeySetIteratorWhenMapChangesDuringTraversalIsWeaklyConsistent",
                "descendingKeySetDescendingIteratorWhenMapChangesDuringTraversalIsWeaklyConsistent");
        Stream<DynamicNode> dequeFails = weak.stream().flatMap(b -> deque.stream().map(m -> eachFails(m, b,
                () -> new BrokenBlockingDeque(boundedDequeBuilder().addBreak(b), noNulls(STANDARD)))));
        Stream<DynamicNode> mapFails = weak.stream().flatMap(b -> map.stream().map(m -> eachFails(m, b,
                () -> new BrokenConcurrentNavigableMap(navigableMapBuilder(b)))));
        Stream<DynamicNode> optedOut = weak.stream().flatMap(b -> Stream.of(
                passes("BlockingDequeContract's opted out, with " + b.description(),
                        () -> new OptedOutBlockingDeque(boundedDequeBuilder().addBreak(b)), deque),
                passes("ConcurrentNavigableMapContract's opted out, with " + b.description(),
                        () -> new OptedOutConcurrentNavigableMap(navigableMapBuilder(b)), map)));
        return Stream.of(dynamicContainer("The concurrent contracts' weak-consistency tests",
                Stream.of(dequeFails, mapFails, optedOut).flatMap(n -> n)));
    }

    private static List<Method> named(final Class<?> contract, final String... names) {
        List<Method> methods = ContractRuns.testMethods(contract, false).stream()
                .filter(m -> List.of(names).contains(m.getName())).toList();
        assertEquals(names.length, methods.size(), () -> "test methods of " + contract.getSimpleName() + " missing: "
                + List.of(names) + " against " + methods);
        return methods;
    }

    private static DynamicNode eachFails(final Method method, final Break b, final Supplier<?> contract) {
        return dynamicTest(method.getName() + " fails with " + b.description(), () -> assertFalse(
                ContractRuns.failures(contract, List.of(method)).isEmpty(),
                () -> method.getName() + " passes with the break: it cannot see it"));
    }

    @TestFactory
    @DisplayName("ListIteratorContract")
    Stream<DynamicNode> listIteratorContract() {
        List<Method> methods = ContractRuns.testMethods(ListIteratorContract.class, false);
        return Stream.concat(
                coverage(ListIteratorContract.class, "ArrayList's list iterator",
                        () -> new IteratorSubjects.JdkListIterator(IteratorProviders.arrayListListIterator()),
                        ContractRuns.withSuperclasses(BreakableListIterator.class),
                        (key, b) -> {
                            Optional<InterfaceMethod> unsupported =
                                    unsupportedMethod(key.substring(key.indexOf('.') + 1));
                            return () -> new IteratorSubjects.BrokenListIterator(b, unsupported);
                        }),
                Stream.of(passes("LinkedList's list iterator",
                                () -> new IteratorSubjects.JdkListIterator(IteratorProviders.linkedListListIterator()),
                                methods),
                        passes("List.of's list iterator, nothing supported",
                                () -> new IteratorSubjects.JdkListIterator(
                                        IteratorProviders.listIteratorsOf(List::copyOf),
                                        IterableMethods.ITERATOR_REMOVE, ListIteratorMethods.LIST_ITERATOR_SET,
                                        ListIteratorMethods.LIST_ITERATOR_ADD), methods),
                        passes("Arrays.asList's list iterator, set() only",
                                () -> new IteratorSubjects.JdkListIterator(IteratorProviders.listIteratorsOf(
                                        l -> Arrays.asList(l.toArray(Integer[]::new))),
                                        IterableMethods.ITERATOR_REMOVE, ListIteratorMethods.LIST_ITERATOR_ADD),
                                methods)));
    }

    @TestFactory
    @DisplayName("SpliteratorContract")
    Stream<DynamicNode> spliteratorContract() {
        List<Method> methods = ContractRuns.testMethods(SpliteratorContract.class, false);
        Stream<DynamicNode> jdk = SpliteratorSubjects.jdkProviders().entrySet().stream()
                .map(e -> passes(e.getKey(), () -> new SpliteratorSubjects.Plain<>(e.getValue()), methods));
        Stream<DynamicNode> bases = Stream.of(SpliteratorSubjects.Base.values())
                .filter(base -> base != SpliteratorSubjects.Base.ARRAY_LIST)
                .map(base -> passes("unbroken breakable over " + base,
                        () -> SpliteratorSubjects.broken(base, null), methods));
        Stream<DynamicNode> named = Stream.of(
                failsIn(SpliteratorSubjects.Base.TREE_SET_REVERSED,
                        BreakableSpliterator.SPLITERATOR_GET_COMPARATOR_ALWAYS_RETURNS_NULL,
                        "sortedCharacteristicHolds"),
                failsIn(SpliteratorSubjects.Base.TREE_SET_NATURAL,
                        BreakableSpliterator.SPLITERATOR_GET_COMPARATOR_RETURNS_NATURAL_ORDER,
                        "sortedCharacteristicHolds"),
                failsIn(SpliteratorSubjects.Base.TREE_SET_REVERSED,
                        BreakableSpliterator.SPLITERATOR_RETURNS_FIRST_ELEMENT_TWICE, "distinctCharacteristicHolds"),
                failsIn(SpliteratorSubjects.Base.INT_RANGE,
                        BreakableSpliterator.SPLITERATOR_FIRST_ELEMENT_IS_NULL, "nonNullCharacteristicHolds"),
                failsIn(SpliteratorSubjects.Base.UNKNOWN_SIZE,
                        BreakableSpliterator.SPLITERATOR_REPORTS_CONCURRENT, "concurrentCharacteristicHolds"),
                failsIn(SpliteratorSubjects.Base.TREE_SET_REVERSED,
                        BreakableSpliterator.SPLITERATOR_CHARACTERISTICS_LEAVE_OUT_ORDERED,
                        "sortedCharacteristicHolds"),
                failsIn(SpliteratorSubjects.Base.TREE_SET_NATURAL,
                        BreakableSpliterator.SPLITERATOR_GET_COMPARATOR_ALWAYS_THROWS, "sortedCharacteristicHolds"),
                failsIn(SpliteratorSubjects.Base.ARRAY_LIST,
                        BreakableSpliterator.SPLITERATOR_CHARACTERISTICS_LEAVE_OUT_SIZED,
                        "subsizedCharacteristicHolds"));
        // Not shown to fail by any break: sizedCharacteristicHolds's check that a SIZED part estimates its own count,
        // and subsizedCharacteristicHolds's checks below the first split. A part is the delegate's own, so no break
        // of the breakable reaches it; the first split's checks are shown, by the breaks above and in coverage.
        return Stream.concat(
                coverage(SpliteratorContract.class, "ArrayList's spliterator",
                        () -> new SpliteratorSubjects.Plain<>(SpliteratorProviders.arrayList()),
                        ContractRuns.withSuperclasses(BreakableSpliterator.class),
                        (key, b) -> () -> SpliteratorSubjects.broken(SpliteratorSubjects.Base.ARRAY_LIST, b)),
                Stream.of(jdk, bases, named).flatMap(n -> n));
    }

    @TestFactory
    @DisplayName("SpliteratorOfIntContract")
    Stream<DynamicNode> spliteratorOfIntContract() {
        List<Method> methods = ContractRuns.testMethods(SpliteratorOfIntContract.class, false);
        return Stream.concat(
                coverage(SpliteratorOfIntContract.class, "IntStream.range's spliterator",
                        () -> new SpliteratorSubjects.OfInt<>(SpliteratorProviders.intRange()),
                        ContractRuns.withSuperclasses(BreakableSpliteratorOfInt.class),
                        (key, b) -> () -> SpliteratorSubjects.brokenInt(b)),
                Stream.concat(Stream.of(passes("an int[]'s spliterator",
                                () -> new SpliteratorSubjects.OfInt<>(SpliteratorProviders.intArray()), methods)),
                        ownTestsFail(SpliteratorOfIntContract.class, SpliteratorSubjects::brokenInt)));
    }

    @TestFactory
    @DisplayName("SpliteratorOfLongContract")
    Stream<DynamicNode> spliteratorOfLongContract() {
        List<Method> methods = ContractRuns.testMethods(SpliteratorOfLongContract.class, false);
        return Stream.concat(
                coverage(SpliteratorOfLongContract.class, "LongStream.range's spliterator",
                        () -> new SpliteratorSubjects.OfLong<>(SpliteratorProviders.longRange()),
                        ContractRuns.withSuperclasses(BreakableSpliteratorOfLong.class),
                        (key, b) -> () -> SpliteratorSubjects.brokenLong(b)),
                Stream.concat(Stream.of(passes("a long[]'s spliterator",
                                () -> new SpliteratorSubjects.OfLong<>(SpliteratorProviders.longArray()), methods)),
                        ownTestsFail(SpliteratorOfLongContract.class, SpliteratorSubjects::brokenLong)));
    }

    @TestFactory
    @DisplayName("SpliteratorOfDoubleContract")
    Stream<DynamicNode> spliteratorOfDoubleContract() {
        List<Method> methods = ContractRuns.testMethods(SpliteratorOfDoubleContract.class, false);
        return Stream.concat(
                coverage(SpliteratorOfDoubleContract.class, "a double[]'s spliterator",
                        () -> new SpliteratorSubjects.OfDouble<>(SpliteratorProviders.doubleArray()),
                        ContractRuns.withSuperclasses(BreakableSpliteratorOfDouble.class),
                        (key, b) -> () -> SpliteratorSubjects.brokenDouble(b)),
                Stream.concat(Stream.of(passes("a filtered DoubleStream's spliterator, not SIZED",
                                () -> new SpliteratorSubjects.OfDouble<>(SpliteratorSubjects.filteredDoubles()),
                                methods)),
                        ownTestsFail(SpliteratorOfDoubleContract.class, SpliteratorSubjects::brokenDouble)));
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
    @DisplayName("SortedSetContract")
    Stream<DynamicNode> sortedSetContract() {
        List<Method> sortedSetMethods = ContractRuns.testMethods(SortedSetContract.class, false);
        return Stream.concat(
                coverage(SortedSetContract.class, "TreeSet(reverseOrder())", JdkSortedSet::new,
                        collectionBreaks(BreakableSortedSet.class, BreakableSequencedSet.class,
                                BreakableSequencedCollection.class),
                        (key, b) -> {
                            var builder = sortedSetBuilder();
                            configure(builder, key, b);
                            return () -> new BrokenSortedSet(builder);
                        }),
                Stream.of(passes("ConcurrentSkipListSet(reverseOrder())", JdkConcurrentSortedSet::new,
                                sortedSetMethods),
                        passes("TreeSet() in natural order", JdkNaturalSortedSet::new, sortedSetMethods),
                        passes("unmodifiableSortedSet(TreeSet(reverseOrder()))", JdkUnmodifiableSortedSet::new,
                                sortedSetMethods)));
    }

    @TestFactory
    @DisplayName("NavigableSetContract")
    Stream<DynamicNode> navigableSetContract() {
        List<Method> methods = ContractRuns.testMethods(NavigableSetContract.class, false);
        return Stream.concat(
                coverage(NavigableSetContract.class, "TreeSet(reverseOrder())", JdkNavigableSet::new,
                        collectionBreaks(BreakableNavigableSet.class, BreakableSequencedSet.class,
                                BreakableSequencedCollection.class),
                        (key, b) -> {
                            var builder = navigableSetBuilder();
                            configure(builder, key, b);
                            return () -> new BrokenNavigableSet(builder);
                        }),
                Stream.of(passes("ConcurrentSkipListSet(reverseOrder())", JdkConcurrentNavigableSet::new, methods),
                        passes("TreeSet() in natural order", JdkNaturalNavigableSet::new, methods),
                        passes("unmodifiableNavigableSet(TreeSet(reverseOrder()))",
                                JdkUnmodifiableNavigableSet::new, methods)));
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
    @DisplayName("BlockingQueueContract")
    Stream<DynamicNode> blockingQueueContract() {
        List<Method> methods = ContractRuns.testMethods(BlockingQueueContract.class, false);
        return Stream.concat(
                coverage(BlockingQueueContract.class, "ArrayBlockingQueue", JdkArrayBlockingQueue::new,
                        collectionBreaks(BreakableBlockingQueue.class),
                        (key, b) -> {
                            var builder = boundedQueueBuilder();
                            var config = configure(builder, key, b);
                            return () -> new BrokenBlockingQueue(builder, noNulls(config));
                        }),
                Stream.of(passes("LinkedBlockingQueue", JdkLinkedBlockingQueue::new, methods),
                        passes("unbroken BreakableBlockingQueue on a LinkedBlockingQueue",
                                () -> new BrokenBlockingQueue(new BreakableBlockingQueue.Builder<Integer>()
                                        .doesNotPermitNulls(), noNulls(STANDARD)), methods)));
    }

    @TestFactory
    @DisplayName("TransferQueueContract")
    Stream<DynamicNode> transferQueueContract() {
        List<Method> methods = ContractRuns.testMethods(TransferQueueContract.class, false);
        return Stream.concat(
                coverage(TransferQueueContract.class, "LinkedTransferQueue", JdkTransferQueue::new,
                        collectionBreaks(BreakableTransferQueue.class),
                        (key, b) -> {
                            var builder = boundedTransferQueueBuilder();
                            var config = configure(builder, key, b);
                            return () -> new BrokenTransferQueue(builder, noNulls(config));
                        }),
                Stream.of(passes("unbroken BreakableTransferQueue on a LinkedTransferQueue",
                                () -> new BrokenTransferQueue(new BreakableTransferQueue.Builder<Integer>()
                                        .doesNotPermitNulls(), noNulls(STANDARD)), methods),
                        passes("unbroken fair BreakableTransferQueue of " + QUEUE_CAPACITY,
                                () -> new BrokenTransferQueue(boundedTransferQueueBuilder().setFair(true),
                                        noNulls(STANDARD)), methods)));
    }

    @TestFactory
    @DisplayName("BlockingDequeContract")
    Stream<DynamicNode> blockingDequeContract() {
        List<Method> methods = ContractRuns.testMethods(BlockingDequeContract.class, false);
        return Stream.concat(
                coverage(BlockingDequeContract.class, "LinkedBlockingDeque(" + QUEUE_CAPACITY + ")",
                        JdkBoundedBlockingDeque::new, collectionBreaks(BreakableBlockingDeque.class),
                        (key, b) -> {
                            var builder = boundedDequeBuilder();
                            var config = configure(builder, key, b);
                            return () -> new BrokenBlockingDeque(builder, noNulls(config));
                        }),
                Stream.of(passes("LinkedBlockingDeque()", JdkBlockingDeque::new, methods),
                        passes("unbroken BreakableBlockingDeque on a LinkedBlockingDeque()",
                                () -> new BrokenBlockingDeque(new BreakableBlockingDeque.Builder<Integer>()
                                        .doesNotPermitNulls(), noNulls(STANDARD)), methods)));
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
    @DisplayName("ConcurrentMapContract")
    Stream<DynamicNode> concurrentMapContract() {
        List<Method> methods = ContractRuns.testMethods(ConcurrentMapContract.class, false);
        Map<String, Break> breaks = withWeakConsistencyBreaks(
                ContractRuns.breaks(ContractRuns.withSuperclasses(BreakableConcurrentMap.class)));
        Stream<DynamicNode> passes = Stream.of(
                passes("ConcurrentHashMap", JdkConcurrentMap::new, methods),
                passes("ConcurrentSkipListMap", JdkConcurrentSkipListMap::new, methods),
                passes("unbroken breakable", () -> new BrokenConcurrentMap(new BreakableConcurrentMap.Builder<>()),
                        methods));
        // A thread-safety break must give the method's results on one thread, so that only the thread-safety
        // contract, under contention, can see it.
        Stream<DynamicNode> singleThreaded = breaks.entrySet().stream()
                .filter(e -> e.getKey().startsWith(BreakableConcurrentMap.class.getSimpleName() + ".")
                        && e.getKey().endsWith(THREAD_SAFETY_BREAK))
                .map(e -> passes("the breakable on one thread, with " + e.getKey(), () -> new BrokenConcurrentMap(
                        new BreakableConcurrentMap.Builder<Integer, String>().addBreak(e.getValue())), methods));
        Stream<DynamicNode> fails = breaks.entrySet().stream()
                .filter(e -> uncheckedReason(ConcurrentMapContract.class, e.getKey()) == null)
                .map(e -> fails(e.getKey(), () -> new BrokenConcurrentMap(
                        new BreakableConcurrentMap.Builder<Integer, String>().addBreak(e.getValue())), methods));
        return Stream.of(dynamicContainer("ConcurrentMapContract",
                Stream.of(passes, singleThreaded, fails).flatMap(n -> n)));
    }

    @TestFactory
    @DisplayName("ConcurrentNavigableMapContract")
    Stream<DynamicNode> concurrentNavigableMapContract() {
        List<Method> methods = ContractRuns.testMethods(ConcurrentNavigableMapContract.class, false);
        Map<String, Break> breaks = withWeakConsistencyBreaks(ContractRuns.breaks(
                ContractRuns.withSuperclasses(BreakableConcurrentNavigableMap.class)));
        // In reverse order, so that a map or view that ignores the comparator shows. The views' bounds hold every
        // Integer key in that order.
        Stream<DynamicNode> passes = Stream.of(
                passes("ConcurrentSkipListMap(reverseOrder())",
                        () -> new JdkConcurrentNavigableMap(skipListViews(REVERSE, m -> m)), methods),
                passes("its descendingMap()", () -> new JdkConcurrentNavigableMap(
                        skipListViews(REVERSE, ConcurrentNavigableMap::descendingMap)), methods),
                passes("its subMap(MAX_VALUE, true, MIN_VALUE, true)", () -> new JdkConcurrentNavigableMap(
                        skipListViews(REVERSE, m -> m.subMap(Integer.MAX_VALUE, true, Integer.MIN_VALUE, true))),
                        methods),
                passes("its headMap(MIN_VALUE, true)", () -> new JdkConcurrentNavigableMap(
                        skipListViews(REVERSE, m -> m.headMap(Integer.MIN_VALUE, true))), methods),
                passes("its tailMap(MAX_VALUE, true)", () -> new JdkConcurrentNavigableMap(
                        skipListViews(REVERSE, m -> m.tailMap(Integer.MAX_VALUE, true))), methods),
                passes("unbroken breakable", () -> new BrokenConcurrentNavigableMap(navigableMapBuilder(null)),
                        methods));
        // As for ConcurrentMapContract: a thread-safety break gives the method's results on one thread.
        Stream<DynamicNode> singleThreaded = breaks.entrySet().stream()
                .filter(e -> e.getKey().startsWith(BreakableConcurrentMap.class.getSimpleName() + ".")
                        && e.getKey().endsWith(THREAD_SAFETY_BREAK))
                .map(e -> passes("the breakable on one thread, with " + e.getKey(),
                        () -> new BrokenConcurrentNavigableMap(navigableMapBuilder(e.getValue())), methods));
        Stream<DynamicNode> fails = breaks.entrySet().stream()
                .filter(e -> uncheckedReason(ConcurrentNavigableMapContract.class, e.getKey()) == null)
                .map(e -> fails(e.getKey(),
                        () -> new BrokenConcurrentNavigableMap(navigableMapBuilder(e.getValue())), methods));
        return Stream.of(dynamicContainer("ConcurrentNavigableMapContract",
                Stream.of(passes, singleThreaded, fails).flatMap(n -> n)));
    }

    @TestFactory
    @VerySlow
    @DisplayName("ThreadSafeConcurrentMapContract")
    Stream<DynamicNode> threadSafeConcurrentMapContract() {
        List<Method> all = ContractRuns.testMethods(ThreadSafeConcurrentMapContract.class, true);
        Map<String, Break> breaks = new java.util.LinkedHashMap<>(
                ContractRuns.breaks(ContractRuns.withSuperclasses(BreakableConcurrentMap.class)));
        breaks.keySet().removeIf(k -> !k.endsWith(THREAD_SAFETY_BREAK)
                || uncheckedReason(ThreadSafeConcurrentMapContract.class, k) != null);
        return Stream.of(dynamicContainer("ThreadSafeConcurrentMapContract", Stream.concat(
                Stream.of(passes("ConcurrentHashMap", JdkThreadSafeConcurrentMap::new, all),
                        passes("ConcurrentSkipListMap", JdkThreadSafeConcurrentSkipListMap::new, all),
                        passes("unbroken BreakableConcurrentMap",
                                () -> new BrokenThreadSafeConcurrentMap(new BreakableConcurrentMap.Builder<>()), all)),
                breaks.entrySet().stream().map(e -> fails(e.getKey(), () -> new BrokenThreadSafeConcurrentMap(
                        new BreakableConcurrentMap.Builder<Integer, String>().addBreak(e.getValue())), all)))));
    }

    @TestFactory
    @VerySlow
    @DisplayName("ThreadSafeConcurrentMapContract on concurrent navigable maps")
    Stream<DynamicNode> threadSafeConcurrentNavigableMap() {
        List<Method> all = ContractRuns.testMethods(ThreadSafeConcurrentMapContract.class, true);
        Map<String, Break> breaks = new java.util.LinkedHashMap<>(
                ContractRuns.breaks(ContractRuns.withSuperclasses(BreakableConcurrentNavigableMap.class)));
        breaks.keySet().removeIf(k -> !k.endsWith(THREAD_SAFETY_BREAK)
                || uncheckedReason(ThreadSafeConcurrentMapContract.class, k) != null);
        // In natural order; the views' bounds hold every Integer key.
        java.util.Comparator<Integer> natural = java.util.Comparator.naturalOrder();
        return Stream.of(dynamicContainer("ThreadSafeConcurrentMapContract on concurrent navigable maps",
                Stream.concat(Stream.of(
                        passes("ConcurrentSkipListMap's descendingMap()", () -> new JdkThreadSafeConcurrentNavigableMap(
                                skipListViews(natural, ConcurrentNavigableMap::descendingMap)), all),
                        passes("ConcurrentSkipListMap's subMap(MIN_VALUE, true, MAX_VALUE, true)",
                                () -> new JdkThreadSafeConcurrentNavigableMap(skipListViews(natural,
                                        m -> m.subMap(Integer.MIN_VALUE, true, Integer.MAX_VALUE, true))), all),
                        passes("ConcurrentSkipListMap's headMap(MAX_VALUE, true)",
                                () -> new JdkThreadSafeConcurrentNavigableMap(skipListViews(natural,
                                        m -> m.headMap(Integer.MAX_VALUE, true))), all),
                        passes("ConcurrentSkipListMap's tailMap(MIN_VALUE, true)",
                                () -> new JdkThreadSafeConcurrentNavigableMap(skipListViews(natural,
                                        m -> m.tailMap(Integer.MIN_VALUE, true))), all),
                        passes("unbroken BreakableConcurrentNavigableMap",
                                () -> new BrokenThreadSafeConcurrentNavigableMap(
                                        new BreakableConcurrentNavigableMap.Builder<>()), all)),
                        breaks.entrySet().stream().map(e -> fails("BreakableConcurrentNavigableMap with " + e.getKey(),
                                () -> new BrokenThreadSafeConcurrentNavigableMap(
                                        new BreakableConcurrentNavigableMap.Builder<Integer, String>()
                                                .addBreak(e.getValue())), all)))));
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
    @VerySlow
    @DisplayName("ThreadSafeBlockingQueueContract")
    Stream<DynamicNode> threadSafeBlockingQueueContract() {
        List<Method> all = ContractRuns.testMethods(ThreadSafeBlockingQueueContract.class, true);
        Map<String, Break> breaks = new java.util.LinkedHashMap<>(
                ContractRuns.breaks(collectionBreaks(BreakableBlockingQueue.class)));
        breaks.keySet().removeIf(k -> !k.endsWith(THREAD_SAFETY_BREAK)
                || uncheckedReason(ThreadSafeBlockingQueueContract.class, k) != null);
        return Stream.of(dynamicContainer("ThreadSafeBlockingQueueContract", Stream.concat(
                Stream.of(passes("ArrayBlockingQueue", JdkThreadSafeArrayBlockingQueue::new, all),
                        passes("LinkedBlockingQueue", JdkThreadSafeLinkedBlockingQueue::new, all),
                        passes("unbroken BreakableBlockingQueue",
                                () -> new BrokenThreadSafeBlockingQueue(boundedQueueBuilder()), all)),
                breaks.entrySet().stream().map(e -> fails(e.getKey(),
                        () -> new BrokenThreadSafeBlockingQueue(boundedQueueBuilder().addBreak(e.getValue())),
                        all)))));
    }

    @TestFactory
    @VerySlow
    @DisplayName("ThreadSafeBlockingDequeContract")
    Stream<DynamicNode> threadSafeBlockingDequeContract() {
        List<Method> all = ContractRuns.testMethods(ThreadSafeBlockingDequeContract.class, true);
        Map<String, Break> breaks = new java.util.LinkedHashMap<>(
                ContractRuns.breaks(collectionBreaks(BreakableBlockingDeque.class)));
        breaks.keySet().removeIf(k -> !k.endsWith(THREAD_SAFETY_BREAK)
                || uncheckedReason(ThreadSafeBlockingDequeContract.class, k) != null);
        return Stream.of(dynamicContainer("ThreadSafeBlockingDequeContract", Stream.concat(
                Stream.of(passes("LinkedBlockingDeque(" + QUEUE_CAPACITY + ")",
                                JdkThreadSafeBoundedBlockingDeque::new, all),
                        passes("LinkedBlockingDeque()", JdkThreadSafeBlockingDeque::new, all),
                        passes("unbroken BreakableBlockingDeque",
                                () -> new BrokenThreadSafeBlockingDeque(boundedDequeBuilder()), all)),
                breaks.entrySet().stream().map(e -> fails(e.getKey(),
                        () -> new BrokenThreadSafeBlockingDeque(boundedDequeBuilder().addBreak(e.getValue())),
                        all)))));
    }

    @TestFactory
    @VerySlow
    @DisplayName("ThreadSafeTransferQueueContract")
    Stream<DynamicNode> threadSafeTransferQueueContract() {
        List<Method> all = ContractRuns.testMethods(ThreadSafeTransferQueueContract.class, true);
        Map<String, Break> breaks = new java.util.LinkedHashMap<>(
                ContractRuns.breaks(collectionBreaks(BreakableTransferQueue.class)));
        breaks.keySet().removeIf(k -> !k.endsWith(THREAD_SAFETY_BREAK)
                || uncheckedReason(ThreadSafeTransferQueueContract.class, k) != null);
        return Stream.of(dynamicContainer("ThreadSafeTransferQueueContract", Stream.concat(
                Stream.of(passes("LinkedTransferQueue", JdkThreadSafeTransferQueue::new, all),
                        passes("unbroken BreakableTransferQueue on a LinkedTransferQueue",
                                () -> new BrokenThreadSafeTransferQueue(new BreakableTransferQueue.Builder<Integer>()
                                        .doesNotPermitNulls()), all),
                        passes("unbroken BreakableTransferQueue of " + QUEUE_CAPACITY,
                                () -> new BrokenThreadSafeTransferQueue(boundedTransferQueueBuilder()), all),
                        passes("unbroken fair BreakableTransferQueue of " + QUEUE_CAPACITY,
                                () -> new BrokenThreadSafeTransferQueue(boundedTransferQueueBuilder().setFair(true)),
                                all)),
                breaks.entrySet().stream().map(e -> fails(e.getKey(),
                        () -> new BrokenThreadSafeTransferQueue(boundedTransferQueueBuilder().addBreak(e.getValue())),
                        all)))));
    }

    @TestFactory
    @DisplayName("SerializableContract")
    Stream<DynamicNode> serializableContract() {
        List<Method> all = ContractRuns.testMethods(SerializableContract.class, false);
        return Stream.of(dynamicContainer("SerializableContract", Stream.of(
                passes("ArrayList", JdkSerializable::new, all),
                passes("unbroken BreakableCollection",
                        () -> new BrokenSerializable(new BreakableCollection.Builder<>()), all),
                fails("BreakableObject.EQUALS_ALWAYS_RETURNS_FALSE", () -> new BrokenSerializable(
                        new BreakableCollection.Builder<Integer>()
                                .addBreak(com.soliscode.test.breakable.BreakableObject.EQUALS_ALWAYS_RETURNS_FALSE)),
                        all))));
    }

    // ========== The harness ==========

    private Stream<DynamicNode> coverage(final Class<?> contract, final String jdkName,
                                         final Supplier<?> jdk, final List<Class<?>> breakSources,
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

    /// A check that one named test method of `SpliteratorContract` fails, on a breakable over a base, with a break:
    /// for the tests that the `ArrayList` subject cannot fail, since it does not report their characteristic.
    private static DynamicNode failsIn(final SpliteratorSubjects.Base base, final Break b, final String method) {
        List<Method> named = ContractRuns.testMethods(SpliteratorContract.class, false).stream()
                .filter(m -> m.getName().equals(method)).toList();
        assertEquals(1, named.size(), () -> "no test method " + method);
        return dynamicTest(method + " fails with " + b.description() + " over " + base, () -> assertFalse(
                ContractRuns.failures(() -> SpliteratorSubjects.broken(base, b), named).isEmpty(),
                () -> method + " passes with the break: it cannot see it"));
    }

    /// Checks that a primitive spliterator contract's own test methods, not those it inherits, fail for each break of
    /// its table: an inherited test would otherwise fail first, and hide one of its own that cannot.
    private static Stream<DynamicNode> ownTestsFail(final Class<?> contract,
                                                    final java.util.function.Function<Break, ?> broken) {
        List<Method> own = ContractRuns.testMethods(contract, false).stream()
                .filter(m -> m.getDeclaringClass() == contract).toList();
        Map<String, Break> breaks = ContractRuns.breaks(List.of(BreakableSpliterator.class));
        return PRIMITIVE_SPLITERATOR_BREAKS.stream().map(field -> {
            Break b = breaks.get(BreakableSpliterator.class.getSimpleName() + "." + field);
            return dynamicTest("its own tests fail with " + field, () -> {
                assertTrue(b != null, () -> "no break " + field);
                List<String> failures = ContractRuns.failures(() -> broken.apply(b), own, true);
                assertFalse(failures.isEmpty(), () -> "no test method of " + contract.getSimpleName()
                        + "'s own fails with " + field);
                assertFalse(ContractRuns.anyHang(failures), () -> "they hang with " + field);
            });
        });
    }

    /// Returns the breaks with `BreakableIterator`'s three weak-consistency breaks added: the map runs take no other
    /// iterator break, and these three reach every view the weak-consistency tests use (#55).
    private static Map<String, Break> withWeakConsistencyBreaks(final Map<String, Break> breaks) {
        Map<String, Break> all = new java.util.LinkedHashMap<>(breaks);
        ContractRuns.breaks(List.of(BreakableIterator.class)).forEach((key, b) -> {
            if (WEAK_CONSISTENCY_BREAKS.contains(key.substring(key.indexOf('.') + 1))) {
                all.put(key, b);
            }
        });
        return all;
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
    private static <B extends BreakableCollection.AbstractBuilder<B, C, Integer>,
            C extends BreakableCollection<Integer>>
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
        for (final String suffix : List.of(WRONG_UNSUPPORTED_BREAK, WRONG_IF_NOT_SUPPORTED_BREAK)) {
            if (field.endsWith(suffix)) {
                method = field.substring(0, field.length() - suffix.length());
            }
        }
        if (method == null) {
            return Optional.empty();
        }
        for (final Class<? extends Enum<?>> methods : METHOD_ENUMS) {
            for (final Enum<?> m : methods.getEnumConstants()) {
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

    /// A sorted set builder in reverse order. Like TreeSet, it refuses null and elements of other types.
    private static BreakableSortedSet.Builder<Integer> sortedSetBuilder() {
        return new BreakableSortedSet.Builder<Integer>()
                .withComparator(REVERSE)
                .doesNotPermitNulls()
                .doesNotPermitIncompatibleTypes(Integer.class);
    }

    /// A navigable set builder in reverse order. Like TreeSet, it refuses null and elements of other types.
    private static BreakableNavigableSet.Builder<Integer> navigableSetBuilder() {
        return new BreakableNavigableSet.Builder<Integer>()
                .withComparator(REVERSE)
                .doesNotPermitNulls()
                .doesNotPermitIncompatibleTypes(Integer.class);
    }

    /// A blocking queue builder on an `ArrayBlockingQueue` of [#QUEUE_CAPACITY]. Like every `BlockingQueue`, it refuses
    /// null.
    private static BreakableBlockingQueue.Builder<Integer> boundedQueueBuilder() {
        return new BreakableBlockingQueue.Builder<Integer>().setCapacity(QUEUE_CAPACITY).doesNotPermitNulls();
    }

    /// A transfer queue builder on the breakable's bounded store of [#QUEUE_CAPACITY]. Like every `BlockingQueue`, it
    /// refuses `null`.
    private static BreakableTransferQueue.Builder<Integer> boundedTransferQueueBuilder() {
        return new BreakableTransferQueue.Builder<Integer>().setCapacity(QUEUE_CAPACITY).doesNotPermitNulls();
    }

    /// A blocking deque builder on a `LinkedBlockingDeque` of [#QUEUE_CAPACITY]. Like every `BlockingDeque`, it
    /// refuses null.
    private static BreakableBlockingDeque.Builder<Integer> boundedDequeBuilder() {
        return new BreakableBlockingDeque.Builder<Integer>().setCapacity(QUEUE_CAPACITY).doesNotPermitNulls();
    }

    private static BreakableCollection.Builder<Integer> threadSafeBuilder() {
        return new BreakableCollection.Builder<Integer>().setSafe(true);
    }

    // ========== Contracts against JDK implementations ==========

    static final class JdkIterable extends AbstractTest
            implements IterableContract<ArrayList<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<ArrayList<Integer>, Integer> provider() {
            return CollectionProviders.provideArrayList(PROVIDER);
        }
    }

    static final class JdkCollection extends AbstractTest
            implements CollectionContract<ArrayList<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<ArrayList<Integer>, Integer> provider() {
            return CollectionProviders.provideArrayList(PROVIDER);
        }
    }

    static final class JdkSequencedCollection extends AbstractTest
            implements SequencedCollectionContract<ArrayList<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<ArrayList<Integer>, Integer> provider() {
            return CollectionProviders.provideArrayList(PROVIDER);
        }
    }

    static final class JdkList extends AbstractTest
            implements ListContract<ArrayList<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<ArrayList<Integer>, Integer> provider() {
            return CollectionProviders.provideArrayList(PROVIDER);
        }
    }

    static final class JdkSet extends AbstractTest
            implements SetContract<HashSet<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<HashSet<Integer>, Integer> provider() {
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
        public CollectionProvider<LinkedHashSet<Integer>, Integer> provider() {
            return CollectionProviders.from(LinkedHashSet::new, LinkedHashSet::new, LinkedHashSet::new, PROVIDER);
        }

        @Override
        public boolean permitDuplicates() {
            return false;
        }
    }

    /// A sorted set's contract: no nulls, duplicates or elements of other types.
    abstract static class SortedSetTest<S extends SortedSet<Integer>> extends AbstractTest
            implements SortedSetContract<S, Integer>, WithIntegerElement {
        @Override
        public boolean permitNulls() {
            return false;
        }

        @Override
        public boolean permitDuplicates() {
            return false;
        }

        @Override
        public boolean permitIncompatibleTypes() {
            return false;
        }
    }

    static final class JdkSortedSet extends SortedSetTest<TreeSet<Integer>> {
        @Override
        public CollectionProvider<TreeSet<Integer>, Integer> provider() {
            return CollectionProviders.from(() -> new TreeSet<>(REVERSE), TreeSet::new, c -> {
                TreeSet<Integer> set = new TreeSet<>(REVERSE);
                set.addAll(c);
                return set;
            }, PROVIDER);
        }
    }

    static final class JdkConcurrentSortedSet extends SortedSetTest<ConcurrentSkipListSet<Integer>> {
        @Override
        public CollectionProvider<ConcurrentSkipListSet<Integer>, Integer> provider() {
            return CollectionProviders.from(() -> new ConcurrentSkipListSet<>(REVERSE),
                    s -> new ConcurrentSkipListSet<>(s), c -> {
                        ConcurrentSkipListSet<Integer> set = new ConcurrentSkipListSet<>(REVERSE);
                        set.addAll(c);
                        return set;
                    }, PROVIDER);
        }
    }

    static final class JdkNaturalSortedSet extends SortedSetTest<TreeSet<Integer>> {
        @Override
        public CollectionProvider<TreeSet<Integer>, Integer> provider() {
            return CollectionProviders.from(TreeSet::new, TreeSet::new, TreeSet::new, PROVIDER);
        }
    }

    static final class JdkUnmodifiableSortedSet extends SortedSetTest<SortedSet<Integer>> {
        JdkUnmodifiableSortedSet() {
            doesNotSupportModification();
        }

        @Override
        public CollectionProvider<SortedSet<Integer>, Integer> provider() {
            return CollectionProviders.from(() -> Collections.unmodifiableSortedSet(new TreeSet<>(REVERSE)),
                    s -> Collections.unmodifiableSortedSet(new TreeSet<>(s)), c -> {
                        TreeSet<Integer> set = new TreeSet<>(REVERSE);
                        set.addAll(c);
                        return Collections.unmodifiableSortedSet(set);
                    }, PROVIDER);
        }
    }

    /// A navigable set's contract: no nulls, duplicates or elements of other types.
    abstract static class NavigableSetTest<S extends NavigableSet<Integer>> extends AbstractTest
            implements NavigableSetContract<S, Integer>, WithIntegerElement {
        @Override
        public boolean permitNulls() {
            return false;
        }

        @Override
        public boolean permitDuplicates() {
            return false;
        }

        @Override
        public boolean permitIncompatibleTypes() {
            return false;
        }
    }

    static final class JdkNavigableSet extends NavigableSetTest<TreeSet<Integer>> {
        @Override
        public CollectionProvider<TreeSet<Integer>, Integer> provider() {
            return CollectionProviders.from(() -> new TreeSet<>(REVERSE), TreeSet::new, c -> {
                TreeSet<Integer> set = new TreeSet<>(REVERSE);
                set.addAll(c);
                return set;
            }, PROVIDER);
        }
    }

    static final class JdkConcurrentNavigableSet extends NavigableSetTest<ConcurrentSkipListSet<Integer>> {
        @Override
        public CollectionProvider<ConcurrentSkipListSet<Integer>, Integer> provider() {
            return CollectionProviders.from(() -> new ConcurrentSkipListSet<>(REVERSE),
                    s -> new ConcurrentSkipListSet<>(s), c -> {
                        ConcurrentSkipListSet<Integer> set = new ConcurrentSkipListSet<>(REVERSE);
                        set.addAll(c);
                        return set;
                    }, PROVIDER);
        }
    }

    static final class JdkNaturalNavigableSet extends NavigableSetTest<TreeSet<Integer>> {
        @Override
        public CollectionProvider<TreeSet<Integer>, Integer> provider() {
            return CollectionProviders.from(TreeSet::new, TreeSet::new, TreeSet::new, PROVIDER);
        }
    }

    static final class JdkUnmodifiableNavigableSet extends NavigableSetTest<NavigableSet<Integer>> {
        JdkUnmodifiableNavigableSet() {
            doesNotSupportModification();
        }

        @Override
        public CollectionProvider<NavigableSet<Integer>, Integer> provider() {
            return CollectionProviders.from(() -> Collections.unmodifiableNavigableSet(new TreeSet<>(REVERSE)),
                    s -> Collections.unmodifiableNavigableSet(new TreeSet<>(s)), c -> {
                        TreeSet<Integer> set = new TreeSet<>(REVERSE);
                        set.addAll(c);
                        return Collections.unmodifiableNavigableSet(set);
                    }, PROVIDER);
        }
    }

    static final class JdkQueue extends AbstractTest
            implements QueueContract<LinkedList<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<LinkedList<Integer>, Integer> provider() {
            return CollectionProviders.provideLinkedList(PROVIDER);
        }
    }

    static final class JdkDeque extends AbstractTest
            implements DequeContract<LinkedList<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<LinkedList<Integer>, Integer> provider() {
            return CollectionProviders.provideLinkedList(PROVIDER);
        }
    }

    static final class JdkArrayBlockingQueue extends AbstractTest
            implements BlockingQueueContract<ArrayBlockingQueue<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<ArrayBlockingQueue<Integer>, Integer> provider() {
            return arrayBlockingQueues();
        }
    }

    static final class JdkLinkedBlockingQueue extends AbstractTest
            implements BlockingQueueContract<LinkedBlockingQueue<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<LinkedBlockingQueue<Integer>, Integer> provider() {
            return CollectionProviders.from(LinkedBlockingQueue::new, LinkedBlockingQueue::new,
                    LinkedBlockingQueue::new, PROVIDER);
        }
    }

    static final class JdkTransferQueue extends AbstractTest
            implements TransferQueueContract<LinkedTransferQueue<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<LinkedTransferQueue<Integer>, Integer> provider() {
            return CollectionProviders.from(LinkedTransferQueue::new, LinkedTransferQueue::new,
                    LinkedTransferQueue::new, PROVIDER);
        }
    }

    static final class JdkThreadSafeTransferQueue extends AbstractTest
            implements ThreadSafeTransferQueueContract<LinkedTransferQueue<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<LinkedTransferQueue<Integer>, Integer> provider() {
            return CollectionProviders.from(LinkedTransferQueue::new, LinkedTransferQueue::new,
                    LinkedTransferQueue::new, PROVIDER);
        }
    }

    static final class JdkThreadSafeArrayBlockingQueue extends AbstractTest
            implements ThreadSafeBlockingQueueContract<ArrayBlockingQueue<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<ArrayBlockingQueue<Integer>, Integer> provider() {
            return arrayBlockingQueues();
        }
    }

    static final class JdkThreadSafeLinkedBlockingQueue extends AbstractTest
            implements ThreadSafeBlockingQueueContract<LinkedBlockingQueue<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<LinkedBlockingQueue<Integer>, Integer> provider() {
            return CollectionProviders.from(LinkedBlockingQueue::new, LinkedBlockingQueue::new,
                    LinkedBlockingQueue::new, PROVIDER);
        }
    }

    static final class JdkBoundedBlockingDeque extends AbstractTest
            implements BlockingDequeContract<LinkedBlockingDeque<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<LinkedBlockingDeque<Integer>, Integer> provider() {
            return boundedBlockingDeques();
        }
    }

    static final class JdkBlockingDeque extends AbstractTest
            implements BlockingDequeContract<LinkedBlockingDeque<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<LinkedBlockingDeque<Integer>, Integer> provider() {
            return CollectionProviders.from(LinkedBlockingDeque::new, LinkedBlockingDeque::new,
                    LinkedBlockingDeque::new, PROVIDER);
        }
    }

    static final class JdkThreadSafeBoundedBlockingDeque extends AbstractTest
            implements ThreadSafeBlockingDequeContract<LinkedBlockingDeque<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<LinkedBlockingDeque<Integer>, Integer> provider() {
            return boundedBlockingDeques();
        }
    }

    static final class JdkThreadSafeBlockingDeque extends AbstractTest
            implements ThreadSafeBlockingDequeContract<LinkedBlockingDeque<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<LinkedBlockingDeque<Integer>, Integer> provider() {
            return CollectionProviders.from(LinkedBlockingDeque::new, LinkedBlockingDeque::new,
                    LinkedBlockingDeque::new, PROVIDER);
        }
    }

    static final class JdkThreadSafeCollection extends AbstractTest
            implements ThreadSafeCollectionContract<List<Integer>, Integer>, WithIntegerElement {
        @Override
        public CollectionProvider<List<Integer>, Integer> provider() {
            return CollectionProviders.from(() -> Collections.synchronizedList(new ArrayList<>()),
                    (c) -> Collections.synchronizedList(new ArrayList<>(c)),
                    (Collection<Integer> c) -> Collections.synchronizedList(new ArrayList<>(c)), PROVIDER);
        }
    }

    static final class JdkSerializable extends AbstractTest
            implements SerializableContract<ArrayList<Integer>>, WithIntegerElement {
        @Override
        public CollectionProvider<ArrayList<Integer>, Integer> provider() {
            return CollectionProviders.provideArrayList(PROVIDER);
        }
    }

    static final class JdkMap extends AbstractTest
            implements MapContract<HashMap<Integer, String>, Integer, String>,
            WithIntegerKeyAndStringValue<HashMap<Integer, String>> {
        @Override
        public MapProvider<Integer, String, HashMap<Integer, String>> provider() {
            return MapProviders.provideHashMap(KEY_PROVIDER, VALUE_PROVIDER);
        }
    }

    static final class JdkSequencedMap extends AbstractTest
            implements SequencedMapContract<LinkedHashMap<Integer, String>, Integer, String>,
            WithIntegerKeyAndStringValue<LinkedHashMap<Integer, String>> {
        @Override
        public MapProvider<Integer, String, LinkedHashMap<Integer, String>> provider() {
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
        public MapProvider<Integer, String, TreeMap<Integer, String>> provider() {
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
        public MapProvider<Integer, String, TreeMap<Integer, String>> provider() {
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

    static final class BrokenSortedSet extends DynamicCollectionContract<BreakableSortedSet<Integer>, Integer>
            implements SortedSetContract<BreakableSortedSet<Integer>, Integer> {
        BrokenSortedSet(final BreakableSortedSet.Builder<Integer> builder) {
            super(builder, WithIntegerElement.PROVIDER, SORTED);
        }
    }

    static final class BrokenNavigableSet extends DynamicCollectionContract<BreakableNavigableSet<Integer>, Integer>
            implements NavigableSetContract<BreakableNavigableSet<Integer>, Integer> {
        BrokenNavigableSet(final BreakableNavigableSet.Builder<Integer> builder) {
            super(builder, WithIntegerElement.PROVIDER, SORTED);
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

    static final class BrokenBlockingQueue extends DynamicCollectionContract<BreakableBlockingQueue<Integer>, Integer>
            implements BlockingQueueContract<BreakableBlockingQueue<Integer>, Integer> {
        BrokenBlockingQueue(final BreakableBlockingQueue.Builder<Integer> builder,
                            final CollectionContractConfig config) {
            super(builder, WithIntegerElement.PROVIDER, config);
        }
    }

    static final class BrokenTransferQueue
            extends DynamicCollectionContract<BreakableTransferQueue<Integer>, Integer>
            implements TransferQueueContract<BreakableTransferQueue<Integer>, Integer> {
        BrokenTransferQueue(final BreakableTransferQueue.Builder<Integer> builder,
                            final CollectionContractConfig config) {
            super(builder, WithIntegerElement.PROVIDER, config);
        }
    }

    static final class BrokenThreadSafeTransferQueue
            extends DynamicCollectionContract<BreakableTransferQueue<Integer>, Integer>
            implements ThreadSafeTransferQueueContract<BreakableTransferQueue<Integer>, Integer> {
        BrokenThreadSafeTransferQueue(final BreakableTransferQueue.Builder<Integer> builder) {
            super(builder, WithIntegerElement.PROVIDER, noNulls(STANDARD));
        }
    }

    static final class BrokenThreadSafeBlockingQueue
            extends DynamicCollectionContract<BreakableBlockingQueue<Integer>, Integer>
            implements ThreadSafeBlockingQueueContract<BreakableBlockingQueue<Integer>, Integer> {
        BrokenThreadSafeBlockingQueue(final BreakableBlockingQueue.Builder<Integer> builder) {
            super(builder, WithIntegerElement.PROVIDER, noNulls(STANDARD));
        }
    }

    static final class BrokenBlockingDeque extends DynamicCollectionContract<BreakableBlockingDeque<Integer>, Integer>
            implements BlockingDequeContract<BreakableBlockingDeque<Integer>, Integer> {
        BrokenBlockingDeque(final BreakableBlockingDeque.Builder<Integer> builder,
                            final CollectionContractConfig config) {
            super(builder, WithIntegerElement.PROVIDER, config);
        }
    }

    /// `BlockingDequeContract` on a breakable deque, with its weak-consistency tests turned off.
    static final class OptedOutBlockingDeque extends DynamicCollectionContract<BreakableBlockingDeque<Integer>, Integer>
            implements BlockingDequeContract<BreakableBlockingDeque<Integer>, Integer> {
        OptedOutBlockingDeque(final BreakableBlockingDeque.Builder<Integer> builder) {
            super(builder, WithIntegerElement.PROVIDER, noNulls(STANDARD));
        }

        @Override
        public boolean iteratorsAreWeaklyConsistent() {
            return false;
        }
    }

    static final class BrokenThreadSafeBlockingDeque
            extends DynamicCollectionContract<BreakableBlockingDeque<Integer>, Integer>
            implements ThreadSafeBlockingDequeContract<BreakableBlockingDeque<Integer>, Integer> {
        BrokenThreadSafeBlockingDeque(final BreakableBlockingDeque.Builder<Integer> builder) {
            super(builder, WithIntegerElement.PROVIDER, noNulls(STANDARD));
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

    static final class JdkConcurrentMap extends AbstractTest
            implements ConcurrentMapContract<ConcurrentHashMap<Integer, String>, Integer, String>,
            WithIntegerKeyAndStringValue<ConcurrentHashMap<Integer, String>> {
        @Override
        public MapProvider<Integer, String, ConcurrentHashMap<Integer, String>> provider() {
            return concurrentHashMaps();
        }
    }

    static final class JdkConcurrentSkipListMap extends AbstractTest
            implements ConcurrentMapContract<ConcurrentSkipListMap<Integer, String>, Integer, String>,
            WithIntegerKeyAndStringValue<ConcurrentSkipListMap<Integer, String>> {
        @Override
        public MapProvider<Integer, String, ConcurrentSkipListMap<Integer, String>> provider() {
            return concurrentSkipListMaps();
        }
    }

    static final class JdkThreadSafeConcurrentMap extends AbstractTest
            implements ThreadSafeConcurrentMapContract<ConcurrentHashMap<Integer, String>, Integer, String>,
            WithIntegerKeyAndStringValue<ConcurrentHashMap<Integer, String>> {
        @Override
        public MapProvider<Integer, String, ConcurrentHashMap<Integer, String>> provider() {
            return concurrentHashMaps();
        }
    }

    static final class JdkThreadSafeConcurrentSkipListMap extends AbstractTest
            implements ThreadSafeConcurrentMapContract<ConcurrentSkipListMap<Integer, String>, Integer, String>,
            WithIntegerKeyAndStringValue<ConcurrentSkipListMap<Integer, String>> {
        @Override
        public MapProvider<Integer, String, ConcurrentSkipListMap<Integer, String>> provider() {
            return concurrentSkipListMaps();
        }
    }

    static final class BrokenConcurrentMap
            extends DynamicMapContract<BreakableConcurrentMap<Integer, String>, Integer, String>
            implements ConcurrentMapContract<BreakableConcurrentMap<Integer, String>, Integer, String> {
        BrokenConcurrentMap(final BreakableConcurrentMap.Builder<Integer, String> builder) {
            super(builder, WithIntegerKeyAndStringValue.KEY_PROVIDER, WithIntegerKeyAndStringValue.VALUE_PROVIDER);
        }
    }

    static final class BrokenThreadSafeConcurrentMap
            extends DynamicMapContract<BreakableConcurrentMap<Integer, String>, Integer, String>
            implements ThreadSafeConcurrentMapContract<BreakableConcurrentMap<Integer, String>, Integer, String> {
        BrokenThreadSafeConcurrentMap(final BreakableConcurrentMap.Builder<Integer, String> builder) {
            super(builder, WithIntegerKeyAndStringValue.KEY_PROVIDER, WithIntegerKeyAndStringValue.VALUE_PROVIDER);
        }
    }

    static final class JdkConcurrentNavigableMap extends AbstractTest
            implements ConcurrentNavigableMapContract<ConcurrentNavigableMap<Integer, String>, Integer, String>,
            WithIntegerKeyAndStringValue<ConcurrentNavigableMap<Integer, String>> {
        private final MapProvider<Integer, String, ConcurrentNavigableMap<Integer, String>> maps;

        JdkConcurrentNavigableMap(final MapProvider<Integer, String, ConcurrentNavigableMap<Integer, String>> mapsArg) {
            this.maps = mapsArg;
            doesNotSupportMethod(SequencedMapMethods.PUT_FIRST);
            doesNotSupportMethod(SequencedMapMethods.PUT_LAST);
        }

        @Override
        public MapProvider<Integer, String, ConcurrentNavigableMap<Integer, String>> provider() {
            return maps;
        }
    }

    static final class JdkThreadSafeConcurrentNavigableMap extends AbstractTest
            implements ThreadSafeConcurrentMapContract<ConcurrentNavigableMap<Integer, String>, Integer, String>,
            WithIntegerKeyAndStringValue<ConcurrentNavigableMap<Integer, String>> {
        private final MapProvider<Integer, String, ConcurrentNavigableMap<Integer, String>> maps;

        JdkThreadSafeConcurrentNavigableMap(
                final MapProvider<Integer, String, ConcurrentNavigableMap<Integer, String>> mapsArg) {
            this.maps = mapsArg;
        }

        @Override
        public MapProvider<Integer, String, ConcurrentNavigableMap<Integer, String>> provider() {
            return maps;
        }
    }

    static final class BrokenConcurrentNavigableMap
            extends DynamicMapContract<BreakableConcurrentNavigableMap<Integer, String>, Integer, String>
            implements ConcurrentNavigableMapContract<BreakableConcurrentNavigableMap<Integer, String>, Integer,
            String> {
        BrokenConcurrentNavigableMap(final BreakableConcurrentNavigableMap.Builder<Integer, String> builder) {
            super(builder, WithIntegerKeyAndStringValue.KEY_PROVIDER, WithIntegerKeyAndStringValue.VALUE_PROVIDER);
        }
    }

    /// `ConcurrentNavigableMapContract` on a breakable map, with its weak-consistency tests turned off.
    static final class OptedOutConcurrentNavigableMap
            extends DynamicMapContract<BreakableConcurrentNavigableMap<Integer, String>, Integer, String>
            implements ConcurrentNavigableMapContract<BreakableConcurrentNavigableMap<Integer, String>, Integer,
            String> {
        OptedOutConcurrentNavigableMap(final BreakableConcurrentNavigableMap.Builder<Integer, String> builder) {
            super(builder, WithIntegerKeyAndStringValue.KEY_PROVIDER, WithIntegerKeyAndStringValue.VALUE_PROVIDER);
        }

        @Override
        public boolean iteratorsAreWeaklyConsistent() {
            return false;
        }
    }

    static final class BrokenThreadSafeConcurrentNavigableMap
            extends DynamicMapContract<BreakableConcurrentNavigableMap<Integer, String>, Integer, String>
            implements ThreadSafeConcurrentMapContract<BreakableConcurrentNavigableMap<Integer, String>, Integer,
            String> {
        BrokenThreadSafeConcurrentNavigableMap(final BreakableConcurrentNavigableMap.Builder<Integer, String> builder) {
            super(builder, WithIntegerKeyAndStringValue.KEY_PROVIDER, WithIntegerKeyAndStringValue.VALUE_PROVIDER);
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

    /// Array blocking queues of [#QUEUE_CAPACITY], with elements from the usual provider.
    private static CollectionProvider<ArrayBlockingQueue<Integer>, Integer> arrayBlockingQueues() {
        return CollectionProviders.from(() -> new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                q -> new ArrayBlockingQueue<>(QUEUE_CAPACITY, false, q),
                c -> new ArrayBlockingQueue<>(QUEUE_CAPACITY, false, c), WithIntegerElement.PROVIDER);
    }

    /// Linked blocking deques of [#QUEUE_CAPACITY], with elements from the usual provider.
    private static CollectionProvider<LinkedBlockingDeque<Integer>, Integer> boundedBlockingDeques() {
        return CollectionProviders.from(() -> new LinkedBlockingDeque<>(QUEUE_CAPACITY),
                q -> {
                    LinkedBlockingDeque<Integer> copy = new LinkedBlockingDeque<>(QUEUE_CAPACITY);
                    copy.addAll(q);
                    return copy;
                },
                c -> {
                    LinkedBlockingDeque<Integer> deque = new LinkedBlockingDeque<>(QUEUE_CAPACITY);
                    deque.addAll(c);
                    return deque;
                }, WithIntegerElement.PROVIDER);
    }

    private static MapProvider<Integer, String, ConcurrentHashMap<Integer, String>> concurrentHashMaps() {
        return MapProviders.from(ConcurrentHashMap::new, (m) -> new ConcurrentHashMap<>(m),
                (m) -> new ConcurrentHashMap<>(m),
                WithIntegerKeyAndStringValue.KEY_PROVIDER, WithIntegerKeyAndStringValue.VALUE_PROVIDER);
    }

    private static MapProvider<Integer, String, ConcurrentSkipListMap<Integer, String>> concurrentSkipListMaps() {
        return MapProviders.from(ConcurrentSkipListMap::new, (m) -> new ConcurrentSkipListMap<>(m),
                (m) -> new ConcurrentSkipListMap<>(m),
                WithIntegerKeyAndStringValue.KEY_PROVIDER, WithIntegerKeyAndStringValue.VALUE_PROVIDER);
    }

    /// A view of a new `ConcurrentSkipListMap` in the given order, for each create, copy and from-map: the map is
    /// filled, and the same view of it is returned.
    private static MapProvider<Integer, String, ConcurrentNavigableMap<Integer, String>> skipListViews(
            final java.util.Comparator<Integer> order,
            final java.util.function.UnaryOperator<ConcurrentNavigableMap<Integer, String>> view) {
        java.util.function.Function<Map<Integer, String>, ConcurrentNavigableMap<Integer, String>> from = m -> {
            ConcurrentSkipListMap<Integer, String> backing = new ConcurrentSkipListMap<>(order);
            backing.putAll(m);
            return view.apply(backing);
        };
        return MapProviders.from(() -> from.apply(Map.of()), m -> from.apply(m), from,
                WithIntegerKeyAndStringValue.KEY_PROVIDER, WithIntegerKeyAndStringValue.VALUE_PROVIDER);
    }

    /// A concurrent navigable map builder in reverse order, `putFirst` and `putLast` unsupported, as for
    /// `ConcurrentSkipListMap`, with a break or none.
    private static BreakableConcurrentNavigableMap.Builder<Integer, String> navigableMapBuilder(final Break b) {
        var builder = new BreakableConcurrentNavigableMap.Builder<Integer, String>()
                .setComparator(REVERSE)
                .doesNotSupport(SequencedMapMethods.PUT_FIRST)
                .doesNotSupport(SequencedMapMethods.PUT_LAST);
        return b == null ? builder : builder.addBreak(b);
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

    private static CollectionContractConfig noNulls(final CollectionContractConfig config) {
        return new CollectionContractConfig() {
            @Override
            public boolean permitNulls() {
                return false;
            }

            @Override
            public boolean permitDuplicates() {
                return config.permitDuplicates();
            }

            @Override
            public boolean permitIncompatibleTypes() {
                return config.permitIncompatibleTypes();
            }
        };
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
