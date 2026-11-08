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
public class RemoveAllContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public RemoveAllContractTest() {
        super();
    }

    protected static final class DynamicRemoveAllContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements RemoveAllContract<BreakableCollection<Integer>, Integer> {

        public DynamicRemoveAllContract(
                final @NonNull BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final @NonNull CollectionContractConfig config) {
            super(builder, new IntegerProvider(), config);
        }
    }

    @Override
    protected @NonNull DynamicRemoveAllContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final @NonNull CollectionContractConfig config) {

        return new DynamicRemoveAllContract(builder, config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    /// Test factory for tests of the removeAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeAll(Collection) method.
    /// @see RemoveAllContract#removeAll_whenEmpty_returnsFalse
    @TestFactory
    public Collection<DynamicTest> testsForRemoveAllWhenEmpty() {
        return Arrays.asList(
                shouldFail(DynamicRemoveAllContract::removeAll_whenEmpty_returnsFalse,
                        "removeAll_whenEmpty_returnsFalse fails with REMOVE_ALL_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicRemoveAllContract::removeAll_whenEmpty_returnsFalse,
                        "removeAll_whenEmpty_returnsFalse fails with REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRemoveAllContract::removeAll_whenEmpty_returnsFalse,
                        "removeAll(Collection) returns false for an empty collection fails when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.REMOVE_ALL))
        );
    }


    /// Test factory for tests of the removeAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeAll(Collection) method.
    /// @see RemoveAllContract#removeAll_whenNotEmpty_removesElements
    @TestFactory
    public Collection<DynamicTest> testsForRemoveAllWhenNotEmpty() {
        return Arrays.asList(
                shouldFail(DynamicRemoveAllContract::removeAll_whenNotEmpty_removesElements,
                        "removeAll_whenNotEmpty_removesElements fails with " +
                                "REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_DOES_NOT_REMOVE_ANY_ELEMENTS)),

                shouldFail(DynamicRemoveAllContract::removeAll_whenNotEmpty_removesElements,
                        "removeAll_whenNotEmpty_removesElements fails with REMOVE_ALL_SKIPS_FIRST_ELEMENT break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_SKIPS_FIRST_ELEMENT)),

                shouldFail(DynamicRemoveAllContract::removeAll_whenNotEmpty_removesElements,
                        "removeAll_whenNotEmpty_removesElements fails with REMOVE_ALL_SKIPS_LAST_ELEMENT break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_SKIPS_LAST_ELEMENT)),

                shouldFail(DynamicRemoveAllContract::removeAll_whenNotEmpty_removesElements,
                        "removeAll_whenNotEmpty_removesElements fails with REMOVE_ALL_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicRemoveAllContract::removeAll_whenNotEmpty_removesElements,
                        "removeAll_whenNotEmpty_removesElements fails with " +
                                "REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRemoveAllContract::removeAll_whenNotEmpty_removesElements,
                        "removeAll_whenNotEmpty_removesElements fails when removeAll is not supported",
                        builder().doesNotSupportMethod(CollectionMethods.REMOVE_ALL))
        );
    }

    /// Test factory for tests of the removeAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeAll(Collection) method.
    /// @see RemoveAllContract#removeAll_withNullElement_handlesCorrectly
    @TestFactory
    public Collection<DynamicTest> testsForRemoveAllWithNullElement() {
        return Arrays.asList(
                shouldFail(DynamicRemoveAllContract::removeAll_withNullElement_handlesCorrectly,
                        "removeAll_withNullElement_handlesCorrectly fails with REMOVE_ALL_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicRemoveAllContract::removeAll_withNullElement_handlesCorrectly,
                        "removeAll_withNullElement_handlesCorrectly fails with " +
                                "REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRemoveAllContract::removeAll_withNullElement_handlesCorrectly,
                        "removeAll_withNullElement_handlesCorrectly fails when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.REMOVE_ALL))
        );
    }

    /// Test factory for tests of the removeAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeAll(Collection) method.
    /// @see RemoveAllContract#removeAll_withIncompatibleType_returnsFalse
    @TestFactory
    public Collection<DynamicTest> testsForRemoveAllWithIncompatibleType() {
        return Arrays.asList(
                shouldFail(DynamicRemoveAllContract::removeAll_withIncompatibleType_returnsFalse,
                         "removeAll_withIncompatibleType_returnsFalse fails with REMOVE_ALL_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicRemoveAllContract::removeAll_withIncompatibleType_returnsFalse,
                        "removeAll_withIncompatibleType_returnsFalse fails with" +
                                "REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE))
        );
    }
}