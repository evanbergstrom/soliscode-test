package com.soliscode.test.contract.collection;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.soliscode.test.breakable.BreakableCollection;
import com.soliscode.test.breakable.BreakableCollectionBuilder;
import com.soliscode.test.contract.AbstractCollectionContractTest;
import com.soliscode.test.contract.CollectionContractConfig;
import com.soliscode.test.contract.DynamicCollectionContract;
import com.soliscode.test.provider.IntegerProvider;


@DisplayName("Tests for CollectionContract class")
public class IsEmptyContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public IsEmptyContractTest() {
        super();
    }

    protected static final class DynamicIsEmptyContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements IsEmptyContract<BreakableCollection<Integer>, Integer> {

        public DynamicIsEmptyContract(
                final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final CollectionContractConfig config) {
            super(builder, new IntegerProvider(), config);
        }
    }

    @Override
    protected DynamicIsEmptyContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final CollectionContractConfig config) {

        return new DynamicIsEmptyContract(builder, config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    /// Test factory for tests of the isEmpty() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the isEmpty() method.
    /// @see IsEmptyContract#isEmptyWhenEmptyReturnsTrue
    /// @see IsEmptyContract#isEmptyWhenNotEmptyReturnsFalse
    @TestFactory
    @DisplayName("isEmpty() contract tests for an empty collection fail for each break")
    public Collection<DynamicTest> testsForIsEmptyWhenEmpty() {
        return Arrays.asList(
                shouldFail(DynamicIsEmptyContract::isEmptyWhenEmptyReturnsTrue,
                        "isEmptyWhenEmptyReturnsTrue fails with IS_EMPTY_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.IS_EMPTY_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicIsEmptyContract::isEmptyWhenEmptyReturnsTrue,
                        "isEmptyWhenEmptyReturnsTrue fails with IS_EMPTY_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.IS_EMPTY_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicIsEmptyContract::isEmptyWhenEmptyReturnsTrue,
                         "isEmptyWhenEmptyReturnsTrue fails when not supported",
                builder().doesNotSupportMethod(CollectionMethods.IS_EMPTY))
        );
    }

    /// Test factory for tests of the isEmpty() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the isEmpty() method.
    /// @see IsEmptyContract#isEmptyWhenEmptyReturnsTrue
    /// @see IsEmptyContract#isEmptyWhenNotEmptyReturnsFalse
    @TestFactory
    @DisplayName("isEmpty() contract tests for a collection with elements fail for each break")
    public Collection<DynamicTest> testsForIsEmptyWhenNotEmpty() {
        return Arrays.asList(
                shouldFail(DynamicIsEmptyContract::isEmptyWhenNotEmptyReturnsFalse,
                        "isEmptyWhenNotEmptyReturnsFalse fails with IS_EMPTY_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.IS_EMPTY_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicIsEmptyContract::isEmptyWhenNotEmptyReturnsFalse,
                        "isEmptyWhenNotEmptyReturnsFalse fails with IS_EMPTY_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.IS_EMPTY_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicIsEmptyContract::isEmptyWhenNotEmptyReturnsFalse,
                        "isEmptyWhenNotEmptyReturnsFalse fails when isEmpty is not supported",
                        builder().doesNotSupportMethod(CollectionMethods.IS_EMPTY))
        );
    }
 }
