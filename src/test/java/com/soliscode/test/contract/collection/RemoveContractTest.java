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
public class RemoveContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public RemoveContractTest() {
        super();
    }

    protected static final class DynamicRemoveContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements RemoveContract<BreakableCollection<Integer>, Integer> {

        public DynamicRemoveContract(
                final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final CollectionContractConfig config) {
            super(builder, new IntegerProvider(), config);
        }
    }

    @Override
    protected DynamicRemoveContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final CollectionContractConfig config) {

        return new DynamicRemoveContract(builder, config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    /// Test factory for tests of the remove(Object) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the remove(Object) method.
    /// @see RemoveContract#removeWhenEmptyReturnsFalse
    @TestFactory
    @DisplayName("remove(Object) contract tests for an empty collection fail for each break")
    public Collection<DynamicTest> testsForRemoveWhenEmpty() {
        return Arrays.asList(
                shouldFail(DynamicRemoveContract::removeWhenEmptyReturnsFalse,
                        "removeWhenEmptyReturnsFalse fails with REMOVE_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicRemoveContract::removeWhenEmptyReturnsFalse,
                        "removeWhenEmptyReturnsFalse fails with REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRemoveContract::removeWhenEmptyReturnsFalse,
                        "remove(Object) returns false for an empty collection fails when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.REMOVE))
        );
    }


    /// Test factory for tests of the remove(Object) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the remove(Object) method.
    /// @see RemoveContract#removeWhenNotEmptyReturnsExpectedResults
    @TestFactory
    @DisplayName("remove(Object) contract tests for a collection with elements fail for each break")
    public Collection<DynamicTest> testsForRemoveWhenNotEmpty() {
        return Arrays.asList(
                shouldFail(DynamicRemoveContract::removeWhenNotEmptyReturnsExpectedResults,
                         "removeWhenNotEmptyReturnsExpectedResults fails with REMOVE_DOES_NOT_REMOVE_ELEMENT break",
                        builder().addBreak(BreakableCollection.REMOVE_DOES_NOT_REMOVE_ELEMENT)),

                shouldFail(DynamicRemoveContract::removeWhenNotEmptyReturnsExpectedResults,
                        "removeWhenNotEmptyReturnsExpectedResults fails with REMOVE_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicRemoveContract::removeWhenNotEmptyReturnsExpectedResults,
                         "removeWhenNotEmptyReturnsExpectedResults fails with " +
                                "REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRemoveContract::removeWhenNotEmptyReturnsExpectedResults,
                         "removeWhenNotEmptyReturnsExpectedResults fails when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.REMOVE))
        );
    }

    /// Test factory for tests of the remove(Object) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the remove(Object) method.
    /// @see RemoveContract#removeWithNullValueReturnsExpectedResults
    @TestFactory
    @DisplayName("remove(Object) contract tests with a null value fail for each break")
    public Collection<DynamicTest> dynamicTestsForRemoveWithNullValue() {
        return Arrays.asList(
                shouldFail(DynamicRemoveContract::removeWithNullValueReturnsExpectedResults,
                         "removeWithNullValueReturnsExpectedResults fails with REMOVE_DOES_NOT_REMOVE_ELEMENT break",
                        builder().addBreak(BreakableCollection.REMOVE_DOES_NOT_REMOVE_ELEMENT)),

                shouldFail(DynamicRemoveContract::removeWithNullValueReturnsExpectedResults,
                         "removeWithNullValueReturnsExpectedResults fails with REMOVE_ALWAYS_RETURNS_FALSE break",
                         builder().addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicRemoveContract::removeWithNullValueReturnsExpectedResults,
                         "removeWithNullValueReturnsExpectedResults fails with " +
                                 "REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRemoveContract::removeWithNullValueReturnsExpectedResults,
                        "removeWithNullValueReturnsExpectedResults fails when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.REMOVE))
        );
    }
 }
