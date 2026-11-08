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
public class IsEmptyContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public IsEmptyContractTest() {
        super();
    }

    protected static final class DynamicIsEmptyContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements IsEmptyContract<BreakableCollection<Integer>, Integer> {

        public DynamicIsEmptyContract(
                final @NonNull BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final @NonNull CollectionContractConfig config) {
            super(builder, new IntegerProvider(), config);
        }
    }

    @Override
    protected @NonNull DynamicIsEmptyContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final @NonNull CollectionContractConfig config) {

        return new DynamicIsEmptyContract(builder, config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    /// Test factory for tests of the isEmpty() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the isEmpty() method.
    /// @see IsEmptyContract#isEmpty_whenEmpty_returnsTrue
    /// @see IsEmptyContract#isEmpty_whenNotEmpty_returnsFalse
    @TestFactory
    public Collection<DynamicTest> testsForIsEmptyWhenEmpty() {
        return Arrays.asList(
                shouldFail(DynamicIsEmptyContract::isEmpty_whenEmpty_returnsTrue,
                        "isEmpty_whenEmpty_returnsTrue fails with IS_EMPTY_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.IS_EMPTY_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicIsEmptyContract::isEmpty_whenEmpty_returnsTrue,
                        "isEmpty_whenEmpty_returnsTrue fails with IS_EMPTY_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.IS_EMPTY_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicIsEmptyContract::isEmpty_whenEmpty_returnsTrue,
                         "isEmpty_whenEmpty_returnsTrue fails when not supported",
                builder().doesNotSupportMethod(CollectionMethods.IS_EMPTY))
        );
    }

    /// Test factory for tests of the isEmpty() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the isEmpty() method.
    /// @see IsEmptyContract#isEmpty_whenEmpty_returnsTrue
    /// @see IsEmptyContract#isEmpty_whenNotEmpty_returnsFalse
    @TestFactory
    public Collection<DynamicTest> testsForIsEmptyWhenNotEmpty() {
        return Arrays.asList(
                shouldFail(DynamicIsEmptyContract::isEmpty_whenNotEmpty_returnsFalse,
                        "isEmpty_whenNotEmpty_returnsFalse fails with IS_EMPTY_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.IS_EMPTY_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicIsEmptyContract::isEmpty_whenNotEmpty_returnsFalse,
                        "isEmpty_whenNotEmpty_returnsFalse fails with IS_EMPTY_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.IS_EMPTY_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicIsEmptyContract::isEmpty_whenNotEmpty_returnsFalse,
                        "isEmpty_whenNotEmpty_returnsFalse fails when isEmpty is not supported",
                        builder().doesNotSupportMethod(CollectionMethods.IS_EMPTY))
        );
    }
 }