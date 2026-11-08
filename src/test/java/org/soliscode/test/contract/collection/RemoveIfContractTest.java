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
public class RemoveIfContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public RemoveIfContractTest() {
        super();
    }

    protected static final class DynamicRemoveIfContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements RemoveIfContract<BreakableCollection<Integer>, Integer> {

        public DynamicRemoveIfContract(
                final @NonNull BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final @NonNull CollectionContractConfig config) {
            super(builder, new IntegerProvider(), config);
        }
    }

    @Override
    protected @NonNull DynamicRemoveIfContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final @NonNull CollectionContractConfig config) {

        return new DynamicRemoveIfContract(builder, config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    /// Test factory for tests of the removeIf() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeIf() method.
    /// @see RemoveIfContract#removeIf_whenEmpty_returnsFalse
    @TestFactory
    public Collection<DynamicTest> testsForRemoveIfWhenEmpty() {
        return Arrays.asList(
                shouldFail(DynamicRemoveIfContract::removeIf_whenEmpty_returnsFalse,
                        "removeIf_whenEmpty_returnsFalse fails with REMOVE_IF_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicRemoveIfContract::removeIf_whenEmpty_returnsFalse,
                        "removeIf_whenEmpty_returnsFalse fails with REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRemoveIfContract::removeIf_whenEmpty_returnsFalse,
                        "removeIf_whenEmpty_returnsFalse fails when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.REMOVE_IF))
        );
    }


    /// Test factory for tests of the removeIf() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeIf() method.
    /// @see RemoveIfContract#removeIf_whenEmpty_returnsFalse
    @TestFactory
    public Collection<DynamicTest> testsForRemoveIfWhenNotEmpty() {
        return Arrays.asList(
                shouldFail(DynamicRemoveIfContract::removeIf_whenNotEmpty_returnsExpectedResults,
                        "removeIf_whenNotEmpty_returnsExpectedResults fails with " +
                                "REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS break",
                        builder().addBreak(BreakableCollection.REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS)),

                shouldFail(DynamicRemoveIfContract::removeIf_whenNotEmpty_returnsExpectedResults,
                        "removeIf_whenNotEmpty_returnsExpectedResults fails with REMOVE_IF_SKIPS_FIRST_ELEMENT break",
                        builder().addBreak(BreakableCollection.REMOVE_IF_SKIPS_FIRST_ELEMENT)),

                shouldFail(DynamicRemoveIfContract::removeIf_whenNotEmpty_returnsExpectedResults,
                        "removeIf_whenNotEmpty_returnsExpectedResults fails with REMOVE_IF_SKIPS_LAST_ELEMENT break",
                        builder().addBreak(BreakableCollection.REMOVE_IF_SKIPS_LAST_ELEMENT)),

                shouldFail(DynamicRemoveIfContract::removeIf_whenNotEmpty_returnsExpectedResults,
                        "removeIf_whenNotEmpty_returnsExpectedResults fails with REMOVE_IF_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_FALSE)),

                shouldFail( DynamicRemoveIfContract::removeIf_whenNotEmpty_returnsExpectedResults,
                        "removeIf_whenNotEmpty_returnsExpectedResults fails with " +
                                "REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRemoveIfContract::removeIf_whenNotEmpty_returnsExpectedResults,
                        "removeIf_whenNotEmpty_returnsExpectedResults fails when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.REMOVE_IF)));
    }
 }