package com.soliscode.test.contract.collection;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.annotations.Nondeterministic;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.breakable.BreakableCollection;
import com.soliscode.test.breakable.BreakableCollectionBuilder;
import com.soliscode.test.contract.AbstractCollectionContractTest;
import com.soliscode.test.contract.CollectionContractConfig;
import com.soliscode.test.contract.DynamicCollectionContract;
import com.soliscode.test.contract.support.WithIntegerElement;

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
}
