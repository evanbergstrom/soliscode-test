package org.soliscode.test.contract.collection;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.annotations.Nondeterministic;
import org.soliscode.test.annotations.VerySlow;
import org.soliscode.test.breakable.BreakableCollection;
import org.soliscode.test.breakable.BreakableCollectionBuilder;
import org.soliscode.test.contract.AbstractCollectionContractTest;
import org.soliscode.test.contract.CollectionContractConfig;
import org.soliscode.test.contract.DynamicCollectionContract;
import org.soliscode.test.contract.support.WithIntegerElement;

import java.util.Arrays;
import java.util.Collection;

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
                final @NonNull BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final @NonNull CollectionContractConfig config) {
            super(builder, WithIntegerElement.PROVIDER, config);
        }
    }

    @Override
    protected @NonNull DynamicThreadSafeCollectionContract createTest(
            final @NonNull BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
            final @NonNull CollectionContractConfig config) {
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
    /// @see ThreadSafeCollectionContract#add_whenCalledUnderContentionWithItself_isThreadSafe
    /// @see ThreadSafeCollectionContract#add_whenCalledUnderContentionWithRemove_isThreadSafe
    /// @see ThreadSafeCollectionContract#add_whenCalledUnderContentionWithRemoveAll_isThreadSafe
    /// @see ThreadSafeCollectionContract#add_whenCalledUnderContentionWithRemoveIf_isThreadSafe
    @VerySlow
    @Nondeterministic
    @TestFactory
    public Collection<DynamicTest> dynamicTestsFor_add_whenIsNotThreadSafe_testsFail() {
        return Arrays.asList(
                shouldFail(DynamicThreadSafeCollectionContract::add_whenCalledUnderContentionWithItself_isThreadSafe,
                        "add_singleElement_returnsTrueAndUpdatesSize() with ADD_IS_NOT_THREAD_SAFE break fails under " +
                                "contention with itself",
                        builder().addBreak(BreakableCollection.ADD_IS_NOT_THREAD_SAFE)),

                shouldFail(DynamicThreadSafeCollectionContract::add_whenCalledUnderContentionWithRemove_isThreadSafe,
                        "add_singleElement_returnsTrueAndUpdatesSize() with ADD_IS_NOT_THREAD_SAFE break fails under " +
                                "contention with remove(Object)",
                        builder().addBreak(BreakableCollection.ADD_IS_NOT_THREAD_SAFE)),

                shouldFail(DynamicThreadSafeCollectionContract::add_whenCalledUnderContentionWithRemoveAll_isThreadSafe,
                        "add_singleElement_returnsTrueAndUpdatesSize() with ADD_IS_NOT_THREAD_SAFE break fails under " +
                                "contention with removeAll(Collection<?> c)",
                        builder().addBreak(BreakableCollection.ADD_IS_NOT_THREAD_SAFE)),

                shouldFail(DynamicThreadSafeCollectionContract::add_whenCalledUnderContentionWithRemoveIf_isThreadSafe,
                        "add_singleElement_returnsTrueAndUpdatesSize() with ADD_IS_NOT_THREAD_SAFE break fails under " +
                                "contention with removeIf(Predicate<? super E> filter)",
                        builder().addBreak(BreakableCollection.ADD_IS_NOT_THREAD_SAFE))
        );
    }
}
