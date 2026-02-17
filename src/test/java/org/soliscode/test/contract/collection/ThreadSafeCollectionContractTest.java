package org.soliscode.test.contract.collection;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.annotations.Nondeterministic;
import org.soliscode.test.annotations.VerySlow;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.breakable.BreakableCollection;
import org.soliscode.test.contract.ContractTest;
import org.soliscode.test.contract.DynamicContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;

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
public class ThreadSafeCollectionContractTest extends ContractTest<BreakableCollection<Integer>> {

    /// Tests for working thread-safe collection implementations.
    ///
    /// This nested class verifies that [ThreadSafeCollectionContract] correctly
    /// passes when applied to a [BreakableCollection] that is configured as safe
    /// and has no active breaks.
    @Nested
    class WorkingCollectionTest extends AbstractTest
            implements ThreadSafeCollectionContract<Integer, BreakableCollection<Integer>>,
            BreakableCollection.WithThreadSafeProvider<Integer>, WithIntegerElement {
    }

    /// A dynamic contract for testing broken thread-safe collection implementations.
    ///
    /// This class extends [DynamicContract] to create test instances with specific
    /// [Break] configurations. It is used to verify that the contract tests fail
    /// as expected when thread-safety violations are introduced.
    private static final class DynamicBrokenCollectionContract // private so it is not discovered by JUnit
            extends DynamicContract<BreakableCollection<Integer>, CollectionProvider<Integer, BreakableCollection<Integer>>>
            implements ThreadSafeCollectionContract<Integer, BreakableCollection<Integer>>, WithIntegerElement {

        /// Creates a new instance of `DynamicBrokenCollectionContract` with the specified break and method.
        ///
        /// @param b the break to apply to the collection
        /// @param m the method being tested
        DynamicBrokenCollectionContract(final @NonNull Break b, final @NonNull InterfaceMethod m) {
            super(b, m, (breaks, statuses, test) -> {
                        var builder = new BreakableCollection.Builder<Integer>()
                                .addBreaks(breaks)
                                .setMethodStatuses(statuses)
                                .setSafe(true);
                        return BreakableCollection.collectionProvider(builder, WithIntegerElement.PROVIDER);
                });
        }

        @Override
        public boolean supportsMethod(final @NonNull InterfaceMethod method) {
            return super.supportsMethod(method);
        }

        @Override
        public void doesNotSupportMethod(final @NonNull InterfaceMethod method) {
            super.doesNotSupportMethod(method);
        }
    }

    /// {@inheritDoc}
    ///
    /// @param b the break to apply to the test object
    /// @param m the optional method to configure as unsupported
    /// @return a new [DynamicBrokenCollectionContract] instance
    @Override
    protected @NonNull DynamicContract<?, ?> createTest(final @NonNull Break b, final @NonNull InterfaceMethod m) {
        return new DynamicBrokenCollectionContract(b, m);
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
                failsWithBreak(BreakableCollection.ADD_IS_NOT_THREAD_SAFE, DynamicBrokenCollectionContract::add_whenCalledUnderContentionWithItself_isThreadSafe, "add_singleElement_returnsTrueAndUpdatesSize() with ADD_IS_NOT_THREAD_SAFE break fails under contention with itself"
                ),

                failsWithBreak(BreakableCollection.ADD_IS_NOT_THREAD_SAFE, DynamicBrokenCollectionContract::add_whenCalledUnderContentionWithRemove_isThreadSafe, "add_singleElement_returnsTrueAndUpdatesSize() with ADD_IS_NOT_THREAD_SAFE break fails under contention with remove(Object)"
                ),

                failsWithBreak(BreakableCollection.ADD_IS_NOT_THREAD_SAFE, DynamicBrokenCollectionContract::add_whenCalledUnderContentionWithRemoveAll_isThreadSafe, "add_singleElement_returnsTrueAndUpdatesSize() with ADD_IS_NOT_THREAD_SAFE break fails under contention with removeAll(Collection<?> c)"
                ),

                failsWithBreak(BreakableCollection.ADD_IS_NOT_THREAD_SAFE, DynamicBrokenCollectionContract::add_whenCalledUnderContentionWithRemoveIf_isThreadSafe, "add_singleElement_returnsTrueAndUpdatesSize() with ADD_IS_NOT_THREAD_SAFE break fails under contention with removeIf(Predicate<? super E> filter)"
                )
        );
    }
}
