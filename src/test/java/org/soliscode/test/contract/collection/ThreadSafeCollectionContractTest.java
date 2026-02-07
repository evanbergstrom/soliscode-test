package org.soliscode.test.contract.collection;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.annotations.Slow;
import org.soliscode.test.annotations.VerySlow;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.breakable.BreakableCollection;
import org.soliscode.test.contract.ContractTest;
import org.soliscode.test.contract.DynamicContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;

import java.util.Arrays;
import java.util.Collection;

@DisplayName("Tests for ThreadSafeCollectionContract class")
public class ThreadSafeCollectionContractTest extends ContractTest<BreakableCollection<Integer>> {

    /// Verifies that the tests all pass when testing a working Collection implementation.
    /// In this case, instances of `BreakableCollection` are used that have no breaks specified.
    @Nested
    class WorkingCollectionTest extends AbstractTest
            implements ThreadSafeCollectionContract<Integer, BreakableCollection<Integer>>,
            BreakableCollection.WithThreadSafeProvider<Integer>, WithIntegerElement {
    }

    /// Dynamically created instance of `CollectionContract` that will run on instances of `BreakableCollection` with a
    /// specified break. This contract will be expected to fail on certain tests depending on the specific break that
    /// is being used.
    @Disabled("Used only for dynamic test generation")
    protected static final class DynamicBrokenCollectionContract
            extends DynamicContract<BreakableCollection<Integer>, CollectionProvider<Integer, BreakableCollection<Integer>>>
            implements ThreadSafeCollectionContract<Integer, BreakableCollection<Integer>>, WithIntegerElement {

        protected DynamicBrokenCollectionContract(final @NonNull Break b, final @NonNull InterfaceMethod m) {
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

    @SuppressWarnings({"unchecked"})
    @Override
    protected @NonNull DynamicBrokenCollectionContract createTest(final @NonNull Break b,
                                                                  final @NonNull InterfaceMethod m) {
        return new DynamicBrokenCollectionContract(b, m);
    }


    @Disabled
    @Slow
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForClearIsSafeUnderContention() {
        return Arrays.asList(
                failingTestWithBreak("add_singleElement_returnsTrueAndUpdatesSize() fails with CLEAR_IS_NOT_THREAD_SAFE break",
                        BreakableCollection.CLEAR_IS_NOT_THREAD_SAFE,
                        DynamicBrokenCollectionContract::clearIsSafeUnderContention)
        );
    }

    @VerySlow
    @TestFactory
    public Collection<DynamicTest> dynamicTestsFor_add_whenIsNotThreadSafe_testsFail() {
        return Arrays.asList(
                failingTestWithBreak("add_singleElement_returnsTrueAndUpdatesSize() with ADD_IS_NOT_THREAD_SAFE break fails under contention with itself",
                        BreakableCollection.ADD_IS_NOT_THREAD_SAFE,
                        DynamicBrokenCollectionContract::add_whenCalledUnderContentionWithItself_IsThreadSafe),

                failingTestWithBreak("add_singleElement_returnsTrueAndUpdatesSize() with ADD_IS_NOT_THREAD_SAFE break fails under contention with remove(Object)",
                        BreakableCollection.ADD_IS_NOT_THREAD_SAFE,
                        DynamicBrokenCollectionContract::add_whenCalledUnderContentionWithRemove_IsThreadSafe),

                failingTestWithBreak("add_singleElement_returnsTrueAndUpdatesSize() with ADD_IS_NOT_THREAD_SAFE break fails under contention with removeAll(Collection<?> c)",
                        BreakableCollection.ADD_IS_NOT_THREAD_SAFE,
                        DynamicBrokenCollectionContract::add_whenCalledUnderContentionWithRemoveAll_IsThreadSafe),

                failingTestWithBreak("add_singleElement_returnsTrueAndUpdatesSize() with ADD_IS_NOT_THREAD_SAFE break fails under contention with removeIf(Predicate<? super E> filter)",
                        BreakableCollection.ADD_IS_NOT_THREAD_SAFE,
                        DynamicBrokenCollectionContract::add_whenCalledUnderContentionWithRemoveIf_IsThreadSafe)
        );
    }
}
