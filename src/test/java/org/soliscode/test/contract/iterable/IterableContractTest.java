package org.soliscode.test.contract.iterable;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.breakable.BreakableIterable;
import org.soliscode.test.contract.ContractTest;
import org.soliscode.test.contract.DynamicContract;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;

import java.util.Arrays;
import java.util.Collection;

/// Tests for the IterableContract class.
///
/// @author evanbergstrom
/// @since 1.0
@DisplayName("Tests for IterableContract class")
public class IterableContractTest extends ContractTest<BreakableIterable<Integer>> {

    /// Verifies that the tests c all pass when testing a working Iterable implementation.
    /// In this case, instances of `BreakableIterable` are used that have no breaks specified.
    @Nested
    public class WorkingIterableTest extends AbstractTest
            implements IterableContract<Integer, BreakableIterable<Integer>>,
                BreakableIterable.WithProvider<Integer>, WithIntegerElement {}


    /// Dynamically created instance of `IterableContract` that will run on instances of `BreakableIterator` with a
    /// specified break. This contract will be expected to fail on certain tests depending on the specific break that
    /// is being used.
    @Disabled("Used only for dynamic test generation")
    protected static class DynamicBrokenIterableContract
            extends DynamicContract<BreakableIterable<Integer>, CollectionProvider<Integer, BreakableIterable<Integer>>>
            implements IterableContract<Integer, BreakableIterable<Integer>>, WithIntegerElement {

        protected DynamicBrokenIterableContract(final @NonNull Break b, final @NonNull InterfaceMethod m) {
            super(b, m, (breaks, statuses, test) ->
                    BreakableIterable.iterableProvider(WithIntegerElement.PROVIDER, breaks, statuses));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected @NonNull DynamicBrokenIterableContract createTest(final @NonNull Break b,
                                                                final @NonNull InterfaceMethod m) {
        return new DynamicBrokenIterableContract(b, null);
    }

    /// Test factory for tests of the forEach() method that should fail for various breaks.
    /// @return a collection of dynamic tests of the forEach() method.
    @TestFactory
    public @NonNull Collection<DynamicTest> dynamicTestsOfForEach() {
        return Arrays.asList(
                failingTestWithBreak("testForeEachOverCollectionWithElements() fails with FOR_EACH_DOES_NOT_CALL_ACTION break",
                        BreakableIterable.FOR_EACH_DOES_NOT_CALL_ACTION,
                        (DynamicBrokenIterableContract t) -> t.testForeEachOverCollectionWithElements()),

                failingTestWithBreak("testForeEachOverCollectionWithElements() fails with FOR_EACH_SKIPS_FIRST_ELEMENT break",
                        BreakableIterable.FOR_EACH_SKIPS_FIRST_ELEMENT,
                        DynamicBrokenIterableContract::testForeEachOverCollectionWithElements),

                failingTestWithBreak("testForeEachOverCollectionWithElements() fails with FOR_EACH_SKIPS_LAST_ELEMENT break",
                        BreakableIterable.FOR_EACH_SKIPS_LAST_ELEMENT,
                        DynamicBrokenIterableContract::testForeEachOverCollectionWithElements)

        );
    }
}
