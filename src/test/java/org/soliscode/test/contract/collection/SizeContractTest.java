package org.soliscode.test.contract.collection;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.soliscode.test.breakable.BreakableCollection;
import org.soliscode.test.breakable.BreakableCollectionBuilder;
import org.soliscode.test.contract.AbstractCollectionContractTest;
import org.soliscode.test.contract.CollectionContractConfig;
import org.soliscode.test.contract.DynamicCollectionContract;
import org.soliscode.test.provider.IntegerProvider;

import java.util.Arrays;
import java.util.Collection;


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
                final @NonNull BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final @NonNull CollectionContractConfig config) {
            super(builder, new IntegerProvider(), config);
        }
    }

    @Override
    protected @NonNull DynamicSizeContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final @NonNull CollectionContractConfig config) {

        return new DynamicSizeContract(builder, config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    /// Test factory for tests of the size() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the size() method.
    /// @see SizeContract#size_whenEmpty_returnsZero
    @TestFactory
    public Collection<DynamicTest> testsForSizeWhenEmpty() {
        return Arrays.asList(
                shouldFail(DynamicSizeContract::size_whenEmpty_returnsZero,
                        "size_whenEmpty_returnsZero fails with SIZE_ALWAYS_RETURNS_CONSTANT_VALUE break",
                        builder().addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_CONSTANT_VALUE))
        );
    }

    /// Test factory for tests of the size() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the size() method.
    /// @see SizeContract#size_whenNotEmpty_returnsCorrectSize
    @TestFactory
    public Collection<DynamicTest> testsForSizeWhenNotEmpty() {
        return Arrays.asList(
                shouldFail(DynamicSizeContract::size_whenNotEmpty_returnsCorrectSize,
                        "size_whenNotEmpty_returnsCorrectSize fails with SIZE_ALWAYS_RETURNS_ZERO break",
                        builder().addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_ZERO)),

                shouldFail(DynamicSizeContract::size_whenNotEmpty_returnsCorrectSize,
                        "size_whenNotEmpty_returnsCorrectSize fails with SIZE_ALWAYS_RETURNS_CONSTANT_VALUE break",
                        builder().addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_CONSTANT_VALUE))
        );
    }
}