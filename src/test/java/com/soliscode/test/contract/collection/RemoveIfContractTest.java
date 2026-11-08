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
public class RemoveIfContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public RemoveIfContractTest() {
        super();
    }

    protected static final class DynamicRemoveIfContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements RemoveIfContract<BreakableCollection<Integer>, Integer> {

        public DynamicRemoveIfContract(
                final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final CollectionContractConfig config) {
            super(builder, new IntegerProvider(), config);
        }
    }

    @Override
    protected DynamicRemoveIfContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final CollectionContractConfig config) {

        return new DynamicRemoveIfContract(builder, config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    /// Test factory for tests of the removeIf() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeIf() method.
    /// @see RemoveIfContract#removeIfWhenEmptyReturnsFalse
    @TestFactory
    @DisplayName("removeIf(Predicate) contract tests for an empty collection fail for each break")
    public Collection<DynamicTest> testsForRemoveIfWhenEmpty() {
        return Arrays.asList(
                shouldFail(DynamicRemoveIfContract::removeIfWhenEmptyReturnsFalse,
                        "removeIfWhenEmptyReturnsFalse fails with REMOVE_IF_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicRemoveIfContract::removeIfWhenEmptyReturnsFalse,
                        "removeIfWhenEmptyReturnsFalse fails with REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRemoveIfContract::removeIfWhenEmptyReturnsFalse,
                        "removeIfWhenEmptyReturnsFalse fails when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.REMOVE_IF))
        );
    }


    /// Test factory for tests of the removeIf() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the removeIf() method.
    /// @see RemoveIfContract#removeIfWhenEmptyReturnsFalse
    @TestFactory
    @DisplayName("removeIf(Predicate) contract tests for a collection with elements fail for each break")
    public Collection<DynamicTest> testsForRemoveIfWhenNotEmpty() {
        return Arrays.asList(
                shouldFail(DynamicRemoveIfContract::removeIfWhenNotEmptyReturnsExpectedResults,
                        "removeIfWhenNotEmptyReturnsExpectedResults fails with " +
                                "REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS break",
                        builder().addBreak(BreakableCollection.REMOVE_IF_DOES_NOT_REMOVE_ANY_ELEMENTS)),

                shouldFail(DynamicRemoveIfContract::removeIfWhenNotEmptyReturnsExpectedResults,
                        "removeIfWhenNotEmptyReturnsExpectedResults fails with REMOVE_IF_SKIPS_FIRST_ELEMENT break",
                        builder().addBreak(BreakableCollection.REMOVE_IF_SKIPS_FIRST_ELEMENT)),

                shouldFail(DynamicRemoveIfContract::removeIfWhenNotEmptyReturnsExpectedResults,
                        "removeIfWhenNotEmptyReturnsExpectedResults fails with REMOVE_IF_SKIPS_LAST_ELEMENT break",
                        builder().addBreak(BreakableCollection.REMOVE_IF_SKIPS_LAST_ELEMENT)),

                shouldFail(DynamicRemoveIfContract::removeIfWhenNotEmptyReturnsExpectedResults,
                        "removeIfWhenNotEmptyReturnsExpectedResults fails with REMOVE_IF_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_FALSE)),

                shouldFail( DynamicRemoveIfContract::removeIfWhenNotEmptyReturnsExpectedResults,
                        "removeIfWhenNotEmptyReturnsExpectedResults fails with " +
                                "REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.REMOVE_IF_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRemoveIfContract::removeIfWhenNotEmptyReturnsExpectedResults,
                        "removeIfWhenNotEmptyReturnsExpectedResults fails when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.REMOVE_IF)));
    }
 }
