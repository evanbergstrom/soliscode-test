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
public class SizeContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public SizeContractTest() {
        super();
    }

    protected static final class DynamicSizeContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements SizeContract<BreakableCollection<Integer>, Integer> {

        public DynamicSizeContract(
                final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final CollectionContractConfig config) {
            super(builder, new IntegerProvider(), config);
        }
    }

    @Override
    protected DynamicSizeContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final CollectionContractConfig config) {

        return new DynamicSizeContract(builder, config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    /// Test factory for tests of the size() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the size() method.
    /// @see SizeContract#sizeWhenEmptyReturnsZero
    @TestFactory
    @DisplayName("size() contract tests for an empty collection fail for each break")
    public Collection<DynamicTest> testsForSizeWhenEmpty() {
        return Arrays.asList(
                shouldFail(DynamicSizeContract::sizeWhenEmptyReturnsZero,
                        "sizeWhenEmptyReturnsZero fails with SIZE_ALWAYS_RETURNS_CONSTANT_VALUE break",
                        builder().addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_CONSTANT_VALUE))
        );
    }

    /// Test factory for tests of the size() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the size() method.
    /// @see SizeContract#sizeWhenNotEmptyReturnsCorrectSize
    @TestFactory
    @DisplayName("size() contract tests for a collection with elements fail for each break")
    public Collection<DynamicTest> testsForSizeWhenNotEmpty() {
        return Arrays.asList(
                shouldFail(DynamicSizeContract::sizeWhenNotEmptyReturnsCorrectSize,
                        "sizeWhenNotEmptyReturnsCorrectSize fails with SIZE_ALWAYS_RETURNS_ZERO break",
                        builder().addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_ZERO)),

                shouldFail(DynamicSizeContract::sizeWhenNotEmptyReturnsCorrectSize,
                        "sizeWhenNotEmptyReturnsCorrectSize fails with SIZE_ALWAYS_RETURNS_CONSTANT_VALUE break",
                        builder().addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_CONSTANT_VALUE))
        );
    }
}
