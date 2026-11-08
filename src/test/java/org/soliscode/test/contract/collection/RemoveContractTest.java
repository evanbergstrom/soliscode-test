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
public class RemoveContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public RemoveContractTest() {
        super();
    }

    protected static final class DynamicRemoveContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements RemoveContract<BreakableCollection<Integer>, Integer> {

        public DynamicRemoveContract(
                final @NonNull BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final @NonNull CollectionContractConfig config) {
            super(builder, new IntegerProvider(), config);
        }
    }

    @Override
    protected @NonNull DynamicRemoveContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final @NonNull CollectionContractConfig config) {

        return new DynamicRemoveContract(builder, config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    /// Test factory for tests of the remove(Object) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the remove(Object) method.
    /// @see RemoveContract#remove_whenEmpty_returnsFalse
    @TestFactory
    public Collection<DynamicTest> testsForRemoveWhenEmpty() {
        return Arrays.asList(
                shouldFail(DynamicRemoveContract::remove_whenEmpty_returnsFalse,
                        "remove_whenEmpty_returnsFalse fails with REMOVE_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicRemoveContract::remove_whenEmpty_returnsFalse,
                        "remove_whenEmpty_returnsFalse fails with REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRemoveContract::remove_whenEmpty_returnsFalse,
                        "remove(Object) returns false for an empty collection fails when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.REMOVE))
        );
    }


    /// Test factory for tests of the remove(Object) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the remove(Object) method.
    /// @see RemoveContract#remove_whenNotEmpty_returnsExpectedResults
    @TestFactory
    public Collection<DynamicTest> testsForRemoveWhenNotEmpty() {
        return Arrays.asList(
                shouldFail(DynamicRemoveContract::remove_whenNotEmpty_returnsExpectedResults,
                         "remove_whenNotEmpty_returnsExpectedResults fails with REMOVE_DOES_NOT_REMOVE_ELEMENT break",
                        builder().addBreak(BreakableCollection.REMOVE_DOES_NOT_REMOVE_ELEMENT)),

                shouldFail(DynamicRemoveContract::remove_whenNotEmpty_returnsExpectedResults,
                        "remove_whenNotEmpty_returnsExpectedResults fails with REMOVE_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicRemoveContract::remove_whenNotEmpty_returnsExpectedResults,
                         "remove_whenNotEmpty_returnsExpectedResults fails with " +
                                "REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRemoveContract::remove_whenNotEmpty_returnsExpectedResults,
                         "remove_whenNotEmpty_returnsExpectedResults fails when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.REMOVE))
        );
    }

    /// Test factory for tests of the remove(Object) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the remove(Object) method.
    /// @see RemoveContract#remove_withNullValue_returnsExpectedResults
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForRemoveWithNullValue() {
        return Arrays.asList(
                shouldFail(DynamicRemoveContract::remove_withNullValue_returnsExpectedResults,
                         "remove_withNullValue_returnsExpectedResults fails with REMOVE_DOES_NOT_REMOVE_ELEMENT break",
                        builder().addBreak(BreakableCollection.REMOVE_DOES_NOT_REMOVE_ELEMENT)),

                shouldFail(DynamicRemoveContract::remove_withNullValue_returnsExpectedResults,
                         "remove_withNullValue_returnsExpectedResults fails with REMOVE_ALWAYS_RETURNS_FALSE break",
                         builder().addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicRemoveContract::remove_withNullValue_returnsExpectedResults,
                         "remove_withNullValue_returnsExpectedResults fails with " +
                                 "REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRemoveContract::remove_withNullValue_returnsExpectedResults,
                        "remove_withNullValue_returnsExpectedResults fails when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.REMOVE))
        );
    }
 }