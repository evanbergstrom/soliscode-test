package org.soliscode.test.contract;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.breakable.BreakableIterable;
import org.soliscode.test.contract.iterable.IterableContract;
import org.soliscode.test.contract.support.WithIntegerElement;

import java.util.Arrays;
import java.util.Collection;

/// Tests for the IterableContract class.
///
/// @author evanbergstrom
/// @since 1.0
@Disabled
@DisplayName("Tests for IterableContract class")
public class IterableContractTest extends ContractTest<Integer, BreakableIterable<Integer>> {

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
    protected static class DynamicBrokenIterableContract extends DynamicContract<Integer, BreakableIterable<Integer>>
            implements IterableContract<Integer, BreakableIterable<Integer>>, WithIntegerElement {

        protected DynamicBrokenIterableContract(final Break b) {
            super(b, BreakableIterable::iterableProvider);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected @NotNull DynamicBrokenIterableContract createTest(Break b) {
        return new DynamicBrokenIterableContract(b);
    }

    /// Test factory for tests of the forEach() method that should fail for various breaks.
    /// @return a collection of dynamic tests of the forEach() method.
    @TestFactory
    public @NotNull Collection<DynamicTest> dynamicTestsOfForEach() {
        return Arrays.asList(
                failingTest("testForeEachOverCollectionWithElements() fails with FOR_EACH_DOES_NOT_CALL_ACTION break",
                        BreakableIterable.FOR_EACH_DOES_NOT_CALL_ACTION,
                        (DynamicBrokenIterableContract t) -> t.testForeEachOverCollectionWithElements()),

                failingTest("testForeEachOverCollectionWithElements() fails with FOR_EACH_SKIPS_FIRST_ELEMENT break",
                        BreakableIterable.FOR_EACH_SKIPS_FIRST_ELEMENT,
                        DynamicBrokenIterableContract::testForeEachOverCollectionWithElements),

                failingTest("testForeEachOverCollectionWithElements() fails with FOR_EACH_SKIPS_LAST_ELEMENT break",
                        BreakableIterable.FOR_EACH_SKIPS_LAST_ELEMENT,
                        DynamicBrokenIterableContract::testForeEachOverCollectionWithElements),

                failingTest("testForEachWithNullAction() fails with FOR_EACH_DOES_NOT_CALL_ACTION break",
                        BreakableIterable.FOR_EACH_DOES_NOT_CALL_ACTION,
                        DynamicBrokenIterableContract::testForEachWithNullAction),

                failingTest("testForEachWithNullAction() fails with FOR_EACH_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT break",
                        BreakableIterable.FOR_EACH_THROWS_WRONG_EXCEPTION_FOR_NULL_ARGUMENT,
                        DynamicBrokenIterableContract::testForEachWithNullAction)

        );
    }
}
