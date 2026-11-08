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
public class RemoveAllContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public RemoveAllContractTest() {
        super();
    }

    protected static final class DynamicRemoveAllContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements RemoveAllContract<BreakableCollection<Integer>, Integer> {

        public DynamicRemoveAllContract(
                final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final CollectionContractConfig config) {
            super(builder, new IntegerProvider(), config);
        }
    }

    @Override
    protected DynamicRemoveAllContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final CollectionContractConfig config) {

        return new DynamicRemoveAllContract(builder, config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    /// Test factory for tests of the removeAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeAll(Collection) method.
    /// @see RemoveAllContract#removeAllWhenEmptyReturnsFalse
    @TestFactory
    @DisplayName("removeAll(Collection) contract tests for an empty collection fail for each break")
    public Collection<DynamicTest> testsForRemoveAllWhenEmpty() {
        return Arrays.asList(
                shouldFail(DynamicRemoveAllContract::removeAllWhenEmptyReturnsFalse,
                        "removeAllWhenEmptyReturnsFalse fails with REMOVE_ALL_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicRemoveAllContract::removeAllWhenEmptyReturnsFalse,
                        "removeAllWhenEmptyReturnsFalse fails with REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRemoveAllContract::removeAllWhenEmptyReturnsFalse,
                        "removeAll(Collection) returns false for an empty collection fails when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.REMOVE_ALL))
        );
    }


    /// Test factory for tests of the removeAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeAll(Collection) method.
    /// @see RemoveAllContract#removeAllWhenNotEmptyRemovesElements
    @TestFactory
    @DisplayName("removeAll(Collection) contract tests for a collection with elements fail for each break")
    public Collection<DynamicTest> testsForRemoveAllWhenNotEmpty() {
        return Arrays.asList(
                shouldFail(DynamicRemoveAllContract::removeAllWhenNotEmptyRemovesElements,
                        "removeAllWhenNotEmptyRemovesElements fails with " +
                                "REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS)),

                shouldFail(DynamicRemoveAllContract::removeAllWhenNotEmptyRemovesElements,
                        "removeAllWhenNotEmptyRemovesElements fails with REMOVE_ALL_SKIPS_FIRST_ELEMENT break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_SKIPS_FIRST_ELEMENT)),

                shouldFail(DynamicRemoveAllContract::removeAllWhenNotEmptyRemovesElements,
                        "removeAllWhenNotEmptyRemovesElements fails with REMOVE_ALL_SKIPS_LAST_ELEMENT break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_SKIPS_LAST_ELEMENT)),

                shouldFail(DynamicRemoveAllContract::removeAllWhenNotEmptyRemovesElements,
                        "removeAllWhenNotEmptyRemovesElements fails with REMOVE_ALL_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicRemoveAllContract::removeAllWhenNotEmptyRemovesElements,
                        "removeAllWhenNotEmptyRemovesElements fails with " +
                                "REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRemoveAllContract::removeAllWhenNotEmptyRemovesElements,
                        "removeAllWhenNotEmptyRemovesElements fails when removeAll is not supported",
                        builder().doesNotSupportMethod(CollectionMethods.REMOVE_ALL))
        );
    }

    /// Test factory for tests of the removeAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeAll(Collection) method.
    /// @see RemoveAllContract#removeAllWithNullElementHandlesCorrectly
    @TestFactory
    @DisplayName("removeAll(Collection) contract tests with a null element fail for each break")
    public Collection<DynamicTest> testsForRemoveAllWithNullElement() {
        return Arrays.asList(
                shouldFail(DynamicRemoveAllContract::removeAllWithNullElementHandlesCorrectly,
                        "removeAllWithNullElementHandlesCorrectly fails with REMOVE_ALL_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicRemoveAllContract::removeAllWithNullElementHandlesCorrectly,
                        "removeAllWithNullElementHandlesCorrectly fails with " +
                                "REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRemoveAllContract::removeAllWithNullElementHandlesCorrectly,
                        "removeAllWithNullElementHandlesCorrectly fails when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.REMOVE_ALL))
        );
    }

    /// Test factory for tests of the removeAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeAll(Collection) method.
    /// @see RemoveAllContract#removeAllWithIncompatibleTypeReturnsFalse
    @TestFactory
    @DisplayName("removeAll(Collection) contract tests with an incompatible type fail for each break")
    public Collection<DynamicTest> testsForRemoveAllWithIncompatibleType() {
        return Arrays.asList(
                shouldFail(DynamicRemoveAllContract::removeAllWithIncompatibleTypeReturnsFalse,
                         "removeAllWithIncompatibleTypeReturnsFalse fails with REMOVE_ALL_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicRemoveAllContract::removeAllWithIncompatibleTypeReturnsFalse,
                        "removeAllWithIncompatibleTypeReturnsFalse fails with " +
                                "REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE))
        );
    }
}
