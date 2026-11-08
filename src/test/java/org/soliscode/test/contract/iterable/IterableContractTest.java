package org.soliscode.test.contract.iterable;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.breakable.BreakableCollectionBuilder;
import org.soliscode.test.breakable.BreakableIterable;
import org.soliscode.test.contract.AbstractCollectionContractTest;
import org.soliscode.test.contract.CollectionContractConfig;
import org.soliscode.test.contract.DynamicCollectionContract;
import org.soliscode.test.contract.support.WithIntegerElement;

import java.util.Arrays;
import java.util.Collection;

/// Tests for the IterableContract class.
///
/// @author evanbergstrom
/// @since 1.0
@DisplayName("Tests for IterableContract class")
public class IterableContractTest extends AbstractCollectionContractTest<BreakableIterable<Integer>, Integer> {

    /// Default constructor.
    public IterableContractTest() {
        super();
    }

    /// Verifies that the tests c all pass when testing a working Iterable implementation.
    /// In this case, instances of `BreakableIterable` are used that have no breaks specified.
    @Nested
    public class WorkingIterableTest extends AbstractTest
            implements IterableContract<BreakableIterable<Integer>, Integer>,
                BreakableIterable.WithProvider<Integer>, WithIntegerElement {

        /// Default constructor.
        public WorkingIterableTest() {
            super();
        }
    }


    /// An iterable contract for iterables built by a builder.
    protected static final class DynamicIterableContract
            extends DynamicCollectionContract<BreakableIterable<Integer>, Integer>
            implements IterableContract<BreakableIterable<Integer>, Integer> {

        /// Creates the contract.
        ///
        /// @param builder the builder of the iterables under test
        /// @param config the contract's configuration
        public DynamicIterableContract(
                final @NonNull BreakableCollectionBuilder<BreakableIterable<Integer>, Integer> builder,
                final @NonNull CollectionContractConfig config) {
            super(builder, WithIntegerElement.PROVIDER, config);
        }
    }

    @Override
    protected @NonNull DynamicIterableContract createTest(
            final @NonNull BreakableCollectionBuilder<BreakableIterable<Integer>, Integer> builder,
            final @NonNull CollectionContractConfig config) {
        return new DynamicIterableContract(builder, config);
    }

    private static BreakableIterable.Builder<Integer> builder() {
        return new BreakableIterable.Builder<>();
    }

    /// Test factory for tests of the forEach() method that should fail for various breaks.
    /// @return a collection of dynamic tests of the forEach() method.
    @TestFactory
    public @NonNull Collection<DynamicTest> dynamicTestsOfForEach() {
        return Arrays.asList(
                shouldFail(DynamicIterableContract::forEach_whenNotEmpty_callsActionForEachElement,
                        "forEach_whenNotEmpty_callsActionForEachElement() fails with FOR_EACH_DOES_NOT_CALL_ACTION break",
                        builder().addBreak(BreakableIterable.FOR_EACH_DOES_NOT_CALL_ACTION)),

                shouldFail(DynamicIterableContract::forEach_whenNotEmpty_callsActionForEachElement,
                        "forEach_whenNotEmpty_callsActionForEachElement() fails with FOR_EACH_SKIPS_FIRST_ELEMENT break",
                        builder().addBreak(BreakableIterable.FOR_EACH_SKIPS_FIRST_ELEMENT)),

                shouldFail(DynamicIterableContract::forEach_whenNotEmpty_callsActionForEachElement,
                        "forEach_whenNotEmpty_callsActionForEachElement() fails with FOR_EACH_SKIPS_LAST_ELEMENT break",
                        builder().addBreak(BreakableIterable.FOR_EACH_SKIPS_LAST_ELEMENT))

        );
    }
}
