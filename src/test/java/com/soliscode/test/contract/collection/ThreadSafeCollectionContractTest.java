package com.soliscode.test.contract.collection;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.annotations.Nondeterministic;
import com.soliscode.test.annotations.Slow;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.breakable.BreakableCollection;
import com.soliscode.test.breakable.BreakableCollectionBuilder;
import com.soliscode.test.breakable.BreakableIterable;
import com.soliscode.test.contract.AbstractCollectionContractTest;
import com.soliscode.test.contract.CollectionContractConfig;
import com.soliscode.test.contract.DynamicCollectionContract;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.CollectionProviders;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract-based tests for thread-safe `Collection` implementations**
///
/// This class provides a comprehensive suite of tests to verify the thread safety
/// and correctness of [java.util.Collection] implementations under concurrent access.
/// It uses [BreakableCollection] to simulate various thread-safety violations and
/// ensures that the [ThreadSafeCollectionContract] correctly identifies these issues.
///
/// ## Test Scope
/// The tests in this class cover:
/// - Concurrent modifications (add, remove, clear)
/// - Thread safety of bulk operations under contention
/// - Consistency of collection state across multiple threads
///
/// ## Configuration
/// - Uses [Integer] elements for testing.
/// - Employs [BreakableCollection] as the primary implementation under test.
///
/// @author evanbergstrom
/// @see ThreadSafeCollectionContract
/// @see BreakableCollection
/// @since 1.0.0
@DisplayName("Tests for ThreadSafeCollectionContract class")
public class ThreadSafeCollectionContractTest
        extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// How many times ({@value}) the clear-during-forEach tests run the contract's test against each subject.
    private static final int CLEAR_FOR_EACH_RUNS = 100;

    /// How many times ({@value}) they run it against [SpinLockedCollection], each run taking
    /// [ThreadSafeClearContract#CLEAR_RETURN_WAIT_MILLIS].
    private static final int SPIN_LOCKED_RUNS = 2;

    /// Default constructor
    public ThreadSafeCollectionContractTest() {
        super();
    }

    /// Tests for working thread-safe collection implementations.
    ///
    /// This nested class verifies that [ThreadSafeCollectionContract] correctly
    /// passes when applied to a [BreakableCollection] that is configured as safe
    /// and has no active breaks.
    @Nested
    class WorkingCollectionTest extends AbstractTest
            implements ThreadSafeCollectionContract<BreakableCollection<Integer>, Integer>,
            BreakableCollection.WithThreadSafeProvider<Integer>, WithIntegerElement {
    }

    /// A thread-safe collection contract for collections built by a builder.
    protected static final class DynamicThreadSafeCollectionContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements ThreadSafeCollectionContract<BreakableCollection<Integer>, Integer> {

        /// Creates the contract.
        ///
        /// @param builder the builder of the collections under test
        /// @param config the contract's configuration
        public DynamicThreadSafeCollectionContract(
                final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final CollectionContractConfig config) {
            super(builder, WithIntegerElement.PROVIDER, config);
        }
    }

    @Override
    protected DynamicThreadSafeCollectionContract createTest(
            final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
            final CollectionContractConfig config) {
        return new DynamicThreadSafeCollectionContract(builder, config);
    }

    private static BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<Integer>().setSafe(true);
    }

    /// Test factory for verifying that `add(E)` thread-safety checks correctly identify failures.
    ///
    /// This method generates dynamic tests that apply the [BreakableCollection#ADD_IS_NOT_THREAD_SAFE]
    /// break and verify that the `add` related thread-safety tests fail under contention.
    ///
    /// @return a collection of dynamic tests for `add(E)` thread safety
    /// @see ThreadSafeCollectionContract#addWhenCalledUnderContentionWithItselfIsThreadSafe
    /// @see ThreadSafeCollectionContract#addWhenCalledUnderContentionWithRemoveIsThreadSafe
    /// @see ThreadSafeCollectionContract#addWhenCalledUnderContentionWithRemoveAllIsThreadSafe
    /// @see ThreadSafeCollectionContract#addWhenCalledUnderContentionWithRemoveIfIsThreadSafe
    @VerySlow
    @Nondeterministic
    @TestFactory
    @DisplayName("add(E) thread-safety contract tests fail with the ADD_IS_NOT_THREAD_SAFE break")
    public Collection<DynamicTest> dynamicTestsForAddWhenIsNotThreadSafeTestsFail() {
        return Arrays.asList(
                shouldFail(DynamicThreadSafeCollectionContract::addWhenCalledUnderContentionWithItselfIsThreadSafe,
                        "add(E) with ADD_IS_NOT_THREAD_SAFE break fails under " +
                                "contention with itself",
                        builder().addBreak(BreakableCollection.ADD_IS_NOT_THREAD_SAFE)),

                shouldFail(DynamicThreadSafeCollectionContract::addWhenCalledUnderContentionWithRemoveIsThreadSafe,
                        "add(E) with ADD_IS_NOT_THREAD_SAFE break fails under " +
                                "contention with remove(Object)",
                        builder().addBreak(BreakableCollection.ADD_IS_NOT_THREAD_SAFE)),

                shouldFail(DynamicThreadSafeCollectionContract::addWhenCalledUnderContentionWithRemoveAllIsThreadSafe,
                        "add(E) with ADD_IS_NOT_THREAD_SAFE break fails under " +
                                "contention with removeAll(Collection<?> c)",
                        builder().addBreak(BreakableCollection.ADD_IS_NOT_THREAD_SAFE)),

                shouldFail(DynamicThreadSafeCollectionContract::addWhenCalledUnderContentionWithRemoveIfIsThreadSafe,
                        "add(E) with ADD_IS_NOT_THREAD_SAFE break fails under " +
                                "contention with removeIf(Predicate<? super E> filter)",
                        builder().addBreak(BreakableCollection.ADD_IS_NOT_THREAD_SAFE))
        );
    }

    /// Tests that the clear-during-forEach test fails on every run against a breakable with
    /// [BreakableCollection#CLEAR_IS_NOT_THREAD_SAFE], and for the reason it is meant to.
    @Test
    @Slow
    @DisplayName("clear() during forEach(Consumer) fails every run with the CLEAR_IS_NOT_THREAD_SAFE break")
    public void clearDuringForEachFailsEveryRunWithTheBreak() {
        for (int i = 0; i < CLEAR_FOR_EACH_RUNS; i++) {
            final int run = i;
            DynamicThreadSafeCollectionContract contract = new DynamicThreadSafeCollectionContract(
                    builder().addBreak(BreakableCollection.CLEAR_IS_NOT_THREAD_SAFE), STANDARD_CONFIG);
            AssertionError e = assertThrows(AssertionError.class,
                    contract::clearWhenCalledDuringForEachDoesNotDisturbIt, "run " + run);
            assertTrue(String.valueOf(e.getMessage()).contains("clear() ran inside forEach"),
                    () -> "run " + run + " failed for another reason: " + e);
        }
    }

    /// Tests that the clear-during-forEach test fails, with its own message, when `forEach` never calls its action.
    @Test
    @DisplayName("clear() during forEach(Consumer) fails when forEach never calls its action")
    public void clearDuringForEachFailsWhenForEachNeverCallsItsAction() {
        DynamicThreadSafeCollectionContract contract = new DynamicThreadSafeCollectionContract(
                builder().addBreak(BreakableIterable.FOR_EACH_DOES_NOT_CALL_ACTION), STANDARD_CONFIG);
        AssertionError e = assertThrows(AssertionError.class, contract::clearWhenCalledDuringForEachDoesNotDisturbIt);
        assertTrue(String.valueOf(e.getMessage()).contains("never called its action"), e::toString);
    }

    /// Tests that the clear-during-forEach test passes on every run against the unbroken thread-safe breakable.
    @Test
    @Slow
    @DisplayName("clear() during forEach(Consumer) passes every run on the unbroken thread-safe breakable")
    public void clearDuringForEachPassesEveryRunOnTheUnbrokenBreakable() {
        assertPassesEveryRun(() -> new DynamicThreadSafeCollectionContract(builder(), STANDARD_CONFIG),
                CLEAR_FOR_EACH_RUNS);
    }

    /// Tests that the clear-during-forEach test passes on every run against `synchronizedList(ArrayList)`, whose
    /// `clear` waits for `forEach`.
    @Test
    @Slow
    @DisplayName("clear() during forEach(Consumer) passes every run on synchronizedList(ArrayList)")
    public void clearDuringForEachPassesEveryRunOnSynchronizedList() {
        assertPassesEveryRun(() -> new JdkClearContract<>(c -> Collections.synchronizedList(new ArrayList<>(c))),
                CLEAR_FOR_EACH_RUNS);
    }

    /// Tests that the clear-during-forEach test passes on every run against the weakly consistent
    /// [CopyOnWriteArrayList] and [ConcurrentLinkedQueue], whose `clear` returns while `forEach` goes on.
    @Test
    @Slow
    @DisplayName("clear() during forEach(Consumer) passes every run on CopyOnWriteArrayList and ConcurrentLinkedQueue")
    public void clearDuringForEachPassesEveryRunOnConcurrentCollections() {
        assertPassesEveryRun(() -> new JdkClearContract<>(CopyOnWriteArrayList::new), CLEAR_FOR_EACH_RUNS);
        assertPassesEveryRun(() -> new JdkClearContract<>(ConcurrentLinkedQueue::new), CLEAR_FOR_EACH_RUNS);
    }

    /// Tests that the clear-during-forEach test passes against a collection whose `clear` spins, rather than
    /// waits, on a lock its `forEach` holds: the reader lets go after
    /// [ThreadSafeClearContract#CLEAR_RETURN_WAIT_MILLIS].
    @Test
    @VerySlow
    @DisplayName("clear() during forEach(Consumer) passes on a collection whose clear() spins on forEach's lock")
    public void clearDuringForEachPassesWhenClearSpins() {
        assertPassesEveryRun(() -> new JdkClearContract<>(SpinLockedCollection::new), SPIN_LOCKED_RUNS);
    }

    private static void assertPassesEveryRun(final Supplier<? extends ThreadSafeClearContract<?, Integer>> contract,
                                             final int runs) {
        for (int i = 0; i < runs; i++) {
            assertDoesNotThrow(contract.get()::clearWhenCalledDuringForEachDoesNotDisturbIt, "run " + i);
        }
    }

    /// The clear contract against a collection that `factory` makes from the elements it is given.
    ///
    /// @param <C> the collection type
    static final class JdkClearContract<C extends Collection<Integer>> extends AbstractTest
            implements ThreadSafeClearContract<C, Integer>, WithIntegerElement {
        private final Function<Collection<Integer>, C> factory;

        JdkClearContract(final Function<Collection<Integer>, C> factoryArg) {
            this.factory = factoryArg;
        }

        @Override
        public CollectionProvider<C, Integer> provider() {
            return CollectionProviders.from(() -> factory.apply(List.of()), factory::apply, factory, PROVIDER);
        }
    }

    /// A collection whose `forEach`, `add` and `clear` take one spin lock: a `clear` called during `forEach` spins,
    /// as no [Thread.State] the reader waits for shows, until `forEach` lets go.
    static final class SpinLockedCollection extends AbstractCollection<Integer> {
        private final List<Integer> elements;
        private final AtomicBoolean locked = new AtomicBoolean();

        SpinLockedCollection(final Collection<Integer> c) {
            this.elements = new ArrayList<>(c);
        }

        @Override
        public Iterator<Integer> iterator() {
            return elements.iterator();
        }

        @Override
        public int size() {
            return elements.size();
        }

        @Override
        public boolean add(final Integer e) {
            lock();
            try {
                return elements.add(e);
            } finally {
                locked.set(false);
            }
        }

        @Override
        public void forEach(final Consumer<? super Integer> action) {
            lock();
            try {
                elements.forEach(action);
            } finally {
                locked.set(false);
            }
        }

        @Override
        public void clear() {
            lock();
            try {
                elements.clear();
            } finally {
                locked.set(false);
            }
        }

        private void lock() {
            while (!locked.compareAndSet(false, true)) {
                Thread.onSpinWait();
            }
        }
    }
}
