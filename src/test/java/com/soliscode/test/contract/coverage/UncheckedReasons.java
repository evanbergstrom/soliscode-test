/*
 * Copyright 2026 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.soliscode.test.contract.coverage;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.soliscode.test.breakable.BreakableBlockingDeque;
import com.soliscode.test.breakable.BreakableBlockingQueue;
import com.soliscode.test.breakable.BreakableConcurrentMap;
import com.soliscode.test.breakable.BreakableExecutorService;
import com.soliscode.test.breakable.BreakableFuture;
import com.soliscode.test.breakable.BreakableSequencedCollection;
import com.soliscode.test.breakable.BreakableTransferQueue;
import com.soliscode.test.contract.blockingdeque.ThreadSafeBlockingDequeContract;
import com.soliscode.test.contract.blockingqueue.BlockingQueueContract;
import com.soliscode.test.contract.blockingqueue.ThreadSafeBlockingQueueContract;
import com.soliscode.test.contract.collection.ThreadSafeCollectionContract;
import com.soliscode.test.contract.concurrentmap.ConcurrentMapContract;
import com.soliscode.test.contract.concurrentmap.ThreadSafeConcurrentMapContract;
import com.soliscode.test.contract.executorservice.ThreadSafeExecutorServiceContract;
import com.soliscode.test.contract.future.ThreadSafeFutureContract;
import com.soliscode.test.contract.iterator.IteratorContract;
import com.soliscode.test.contract.iterator.WeaklyConsistentIteratorContract;
import com.soliscode.test.contract.sequencedcollection.ThreadSafeSequencedCollectionContract;
import com.soliscode.test.contract.sequencedmap.SequencedMapContract;
import com.soliscode.test.contract.set.SetContract;
import com.soliscode.test.contract.sortedset.SortedSetContract;
import com.soliscode.test.contract.spliterator.SpliteratorContract;
import com.soliscode.test.contract.spliterator.SpliteratorOfDoubleContract;
import com.soliscode.test.contract.spliterator.SpliteratorOfIntContract;
import com.soliscode.test.contract.spliterator.SpliteratorOfLongContract;
import com.soliscode.test.contract.transferqueue.ThreadSafeTransferQueueContract;

/// Why [ContractCoverageTest] leaves a break out of a contract's run, rule by rule (#64: split out of
/// `ContractCoverageTest`, whose one method held every rule).
///
/// Each rule method returns a reason, [#CHECKED] for a break the contract checks, or `null` to leave the break to
/// the next rule; [#uncheckedReason] asks them in turn.
final class UncheckedReasons {

    /// The suffix of a thread-safety break's name.
    static final String THREAD_SAFETY_BREAK = "_IS_NOT_THREAD_SAFE";

    /// The breaks that act only on a change to an iterator's source other than through it (#55).
    static final Set<String> WEAK_CONSISTENCY_BREAKS = Set.of("ITERATOR_IS_FAIL_FAST",
            "ITERATOR_RETURNS_AN_ELEMENT_TWICE_AFTER_A_CHANGE", "ITERATOR_SKIPS_AN_ELEMENT_AFTER_A_CHANGE");

    /// What a reason method returns for a break the contract checks, where `null` means it has no reason and the
    /// next is asked.
    private static final String CHECKED = "";

    /// The spliterator breaks that are faults only for a SIZED or ORDERED spliterator.
    private static final Set<String> BLOCKING_QUEUE_SPLITERATOR_BREAKS = Set.of(
            "SPLITERATOR_ESTIMATE_SIZE_ALWAYS_RETURNS_MAX_VALUE",
            "SPLITERATOR_GET_EXACT_SIZE_IF_KNOWN_ALWAYS_RETURNS_NEGATIVE_ONE",
            "SPLITERATOR_CHARACTERISTICS_ALWAYS_RETURNS_ZERO");

    /// The breaks of `BreakableSpliterator` added for the spliterator contracts (#38). The Iterable and collection
    /// contracts, whose break sources include `BreakableSpliterator`, are not run with them.
    private static final Set<String> SPLITERATOR_CONTRACT_BREAKS = Set.of(
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

    private static final Pattern SORTED_MAP_METHOD_BREAK =
            Pattern.compile("(COMPARATOR|FIRST_KEY|LAST_KEY|HEAD_MAP|TAIL_MAP|SUB_MAP)_");


    private UncheckedReasons() {
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
        String reason = Stream.of(threadSafetyReason(contract, key), weakConsistencyReason(contract, field),
                        spliteratorReason(contract, field), spliteratorComparatorReason(contract, field),
                        returnValueReason(contract, field), interleavingReason(field), orderReason(contract, field))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        return CHECKED.equals(reason) ? null : reason;
    }

    /// The reason for a thread-safety break, [#CHECKED] if the contract checks it, or `null` to look further.
    ///
    /// @param contract the contract
    /// @param key the break, as `SimpleClassName.FIELD`
    /// @return the reason, [#CHECKED] or `null`
    private static String threadSafetyReason(final Class<?> contract, final String key) {
        if (!key.endsWith(THREAD_SAFETY_BREAK)) {
            return null;
        }
        return Stream.of(futureOrExecutorReason(contract, key),
                        ownThreadSafetyReason(contract, key, ThreadSafeConcurrentMapContract.class,
                                BreakableConcurrentMap.class, List.of(BreakableConcurrentMap.class),
                                "not a fault for a BreakableConcurrentMap: its store is a concurrent map, and the "
                                        + "breakable takes no lock of its own, so the break changes nothing there",
                                "a thread-safety break: ThreadSafeConcurrentMapContract checks it, and "
                                        + "ConcurrentMapContract checks that it is correct on one thread"),
                        // BreakableBlockingQueue's take, poll and drainTo run on the transfer queue's store: faults
                        // for it too.
                        ownThreadSafetyReason(contract, key, ThreadSafeTransferQueueContract.class,
                                BreakableTransferQueue.class,
                                List.of(BreakableTransferQueue.class, BreakableBlockingQueue.class),
                                "not a fault for a BreakableTransferQueue: its store (a LinkedTransferQueue, or its "
                                        + "own bounded store on one lock) is thread safe without the breakable's "
                                        + "lock, so running without the lock changes nothing; "
                                        + "ThreadSafeCollectionContract covers it on BreakableCollection, or gives "
                                        + "its reason",
                                "a thread-safety break: ThreadSafeTransferQueueContract checks it"),
                        ownThreadSafetyReason(contract, key, ThreadSafeBlockingDequeContract.class,
                                BreakableBlockingDeque.class, List.of(BreakableBlockingDeque.class),
                                "not a fault for a BreakableBlockingDeque: its store is a JDK BlockingDeque, thread "
                                        + "safe without the breakable's lock, so running without the lock changes "
                                        + "nothing; ThreadSafeCollectionContract covers it on BreakableCollection, "
                                        + "or gives its reason",
                                "a thread-safety break: ThreadSafeBlockingDequeContract checks it"),
                        ownThreadSafetyReason(contract, key, ThreadSafeBlockingQueueContract.class,
                                BreakableBlockingQueue.class, List.of(BreakableBlockingQueue.class),
                                "not a fault for a BreakableBlockingQueue: its store is a JDK BlockingQueue, thread "
                                        + "safe without the breakable's lock, so running without the lock changes "
                                        + "nothing; ThreadSafeCollectionContract covers it on BreakableCollection, "
                                        + "or gives its reason",
                                "a thread-safety break: ThreadSafeBlockingQueueContract checks it"),
                        ownThreadSafetyReason(contract, key, ThreadSafeSequencedCollectionContract.class,
                                BreakableSequencedCollection.class, List.of(BreakableSequencedCollection.class),
                                "not a fault of the sequenced methods: ThreadSafeCollectionContract checks it",
                                "a thread-safety break: ThreadSafeSequencedCollectionContract checks it, on "
                                        + "BreakableSequencedCollection; BreakableSequencedSet and the sets below it "
                                        + "run the sequenced methods under their lock, or refuse them, whatever the "
                                        + "break; BreakableList inherits the six others, and its reversed() honours "
                                        + "REVERSED_IS_NOT_THREAD_SAFE as BreakableSequencedCollection's does "
                                        + "(BreakableViewSafetyTest checks it)"),
                        contract == ThreadSafeCollectionContract.class ? null
                                : "a thread-safety break: ThreadSafeCollectionContract checks it")
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /// The reason for a thread-safety break of a [BreakableFuture] or a [BreakableExecutorService]: their own
    /// thread-safety contract checks it, and no other.
    ///
    /// @param contract the contract
    /// @param key the break, as `SimpleClassName.FIELD`
    /// @return the reason, [#CHECKED] or `null` to look further
    private static String futureOrExecutorReason(final Class<?> contract, final String key) {
        boolean futureBreak = isBreakOf(key, BreakableFuture.class);
        if (!futureBreak && !isBreakOf(key, BreakableExecutorService.class)) {
            return null;
        }
        Class<?> own = futureBreak ? ThreadSafeFutureContract.class : ThreadSafeExecutorServiceContract.class;
        return contract == own ? CHECKED : "a thread-safety break: " + own.getSimpleName() + " checks it";
    }

    /// The reason for a thread-safety break against a breakable's own thread-safety contract, or for that
    /// breakable's break against another contract.
    ///
    /// @param contract the contract
    /// @param key the break, as `SimpleClassName.FIELD`
    /// @param own the breakable's own thread-safety contract
    /// @param breakable the breakable
    /// @param faults the breakables whose breaks are faults for `own`
    /// @param notAFault why another breakable's break is no fault for `own`
    /// @param checkedByOwn why `breakable`'s break is left out of another contract
    /// @return the reason, [#CHECKED] or `null` to look further
    private static String ownThreadSafetyReason(final Class<?> contract, final String key, final Class<?> own,
            final Class<?> breakable, final List<Class<?>> faults, final String notAFault,
            final String checkedByOwn) {
        if (contract == own) {
            return faults.stream().anyMatch(b -> isBreakOf(key, b)) ? CHECKED : notAFault;
        }
        return isBreakOf(key, breakable) ? checkedByOwn : null;
    }

    /// Whether a break is one of a breakable's own.
    ///
    /// @param key the break, as `SimpleClassName.FIELD`
    /// @param breakable the breakable
    /// @return whether `key` names a break of `breakable`
    private static boolean isBreakOf(final String key, final Class<?> breakable) {
        return key.startsWith(breakable.getSimpleName() + ".");
    }

    /// The reason for a weak-consistency break, and for an iterator break that [IteratorContract] cannot set up.
    ///
    /// @param contract the contract
    /// @param field the break's field name
    /// @return the reason, [#CHECKED] or `null` to look further
    private static String weakConsistencyReason(final Class<?> contract, final String field) {
        if (WEAK_CONSISTENCY_BREAKS.contains(field)) {
            boolean testsWeakConsistency = WeaklyConsistentIteratorContract.class.isAssignableFrom(contract)
                    || BlockingQueueContract.class.isAssignableFrom(contract)
                    || ConcurrentMapContract.class.isAssignableFrom(contract);
            return testsWeakConsistency ? CHECKED : "a weak-consistency break: the weakly consistent iterator tests "
                    + "check it, and no other contract's run changes the source during a traversal on one thread";
        }
        if (field.equals("ITERATOR_FOR_EACH_REMAINING_THROWS_WRONG_EXCEPTION_IF_NOT_SUPPORTED")
                && IteratorContract.class.isAssignableFrom(contract)) {
            return "not a fault for IteratorContract: forEachRemaining is not optional (Iterator#forEachRemaining), so "
                    + "the setup this break needs, forEachRemaining unsupported, fails the contract without the break; "
                    + "IterableContract checks it";
        }
        return null;
    }

    /// The reason for a spliterator break added for [SpliteratorContract], or one no primitive subject can show.
    ///
    /// @param contract the contract
    /// @param field the break's field name
    /// @return the reason, or `null` to look further
    private static String spliteratorReason(final Class<?> contract, final String field) {
        if (SPLITERATOR_CONTRACT_BREAKS.contains(field) && !SpliteratorContract.class.isAssignableFrom(contract)) {
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
        return null;
    }

    /// The reason for a `getComparator` break, which [SpliteratorContract]'s subjects cannot show.
    ///
    /// @param contract the contract
    /// @param field the break's field name
    /// @return the reason, or `null` to look further
    private static String spliteratorComparatorReason(final Class<?> contract, final String field) {
        if (!SpliteratorContract.class.isAssignableFrom(contract)) {
            return null;
        }
        if (field.equals("SPLITERATOR_GET_COMPARATOR_ALWAYS_THROWS")) {
            return "no test can see it on these subjects: their spliterators (an ArrayList's, an array's) are not "
                    + "SORTED, so getComparator must throw IllegalStateException, as the break makes it; "
                    + "SpliteratorContract's run on a natural-order TreeSet checks it in sortedCharacteristicHolds";
        }
        if (field.equals("SPLITERATOR_GET_COMPARATOR_RETURNS_NATURAL_ORDER")) {
            return "no test can see it on these subjects: their spliterators (an ArrayList's, an array's) are not "
                    + "SORTED, so getComparator throws and the break, which changes only a null comparator, changes "
                    + "nothing; SpliteratorContract's run on a natural-order TreeSet checks it in "
                    + "sortedCharacteristicHolds";
        }
        return null;
    }

    /// The reason for a break whose return value, or spliterator, the contract's type allows.
    ///
    /// @param contract the contract
    /// @param field the break's field name
    /// @return the reason, or `null` to look further
    private static String returnValueReason(final Class<?> contract, final String field) {
        boolean blockingQueue = BlockingQueueContract.class.isAssignableFrom(contract);
        if (field.equals("ADD_ALWAYS_RETURNS_TRUE") && !SetContract.class.isAssignableFrom(contract)) {
            return "not a fault here: add(E) always returns true for a collection that permits duplicates "
                    + "(Collection#add); SetContract checks it";
        }
        if (field.equals("SPLITERATOR_TRY_SPLIT_ALWAYS_RETURNS_NULL")) {
            return "not a fault: Spliterator#trySplit may return null for any reason, and the contracts accept it "
                    + "(a ConcurrentSkipListSet's spliterator does, where its random index levels allow no split)";
        }
        if (field.equals("OFFER_ALWAYS_RETURNS_TRUE") && !blockingQueue) {
            return "not a fault for BreakableQueue or BreakableDeque: they have no capacity, so offer(E) always "
                    + "returns true (Queue#offer); BlockingQueueContract checks it on a bounded queue";
        }
        if (BLOCKING_QUEUE_SPLITERATOR_BREAKS.contains(field) && blockingQueue) {
            return "not a fault for a BlockingQueue: its spliterator need not be SIZED or ORDERED "
                    + "(ArrayBlockingQueue's and LinkedBlockingQueue's report CONCURRENT, ORDERED and NONNULL, not "
                    + "SIZED, and a BlockingQueue need not be FIFO), so an estimate of Long.MAX_VALUE, an exact size "
                    + "of -1 and no characteristics are all allowed (Spliterator#estimateSize, #getExactSizeIfKnown)";
        }
        return null;
    }

    /// The reason for a thread-safety break that no interleaving a test can force shows, or shows often enough.
    ///
    /// @param field the break's field name
    /// @return the reason, or `null` to look further
    private static String interleavingReason(final String field) {
        if (field.equals("IS_EMPTY_IS_NOT_THREAD_SAFE") || field.equals("SIZE_IS_NOT_THREAD_SAFE")) {
            return "no test can see it: without the lock, isEmpty() and size() read one field, and any value read is "
                    + "one some interleaving with the lock could give";
        }
        return null;
    }

    /// The reason for an ordering break the contract's type does not have.
    ///
    /// @param contract the contract
    /// @param field the break's field name
    /// @return the reason, or `null` if the contract checks the break
    private static String orderReason(final Class<?> contract, final String field) {
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
}
