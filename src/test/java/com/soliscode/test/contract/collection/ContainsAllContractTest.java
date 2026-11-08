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
import com.soliscode.test.provider.ObjectProvider;


@DisplayName("Tests for CollectionContract class")
public class ContainsAllContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public ContainsAllContractTest() {
        super();
    }

    private static final class DynamicContainsAllContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements ContainsAllContract<BreakableCollection<Integer>, Integer> {

        public DynamicContainsAllContract(
                final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final ObjectProvider<Integer> elementProvider,
                final CollectionContractConfig config) {
            super(builder, elementProvider, config);
        }
    }

    @Override
    protected DynamicContainsAllContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final CollectionContractConfig config) {

        return new DynamicContainsAllContract(builder, new IntegerProvider(), config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    /// Test factory for tests of the containsAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the containsAll(Collection) method.
    /// @see ContainsAllContract#containsAllWhenNotEmptyReturnsExpectedResults
    @TestFactory
    @DisplayName("containsAll(Collection) contract tests for a collection with elements fail for each break")
    public Collection<DynamicTest> testsForContainsAll() {
        return Arrays.asList(
                shouldFail(DynamicContainsAllContract::containsAllWhenNotEmptyReturnsExpectedResults,
                        "containsAll(Collection) works for a collection with elements fails with " +
                                "CONTAINS_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.CONTAINS_ALL_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicContainsAllContract::containsAllWhenNotEmptyReturnsExpectedResults,
                        "containsAll(Collection) works for a collection with elements fails with " +
                                "CONTAINS_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.CONTAINS_ALL_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicContainsAllContract::containsAllWhenNotEmptyReturnsExpectedResults,
                        "containsAll(Collection) works for a collection with elements fails with " +
                                "CONTAINS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.CONTAINS_ALL_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicContainsAllContract::containAllWhenNotSupportedThrowsUnsupportedOperationException,
                        "containsAll(Collection) works for a collection with elements fails when not supported",
                        builder().doesNotSupport(CollectionMethods.CONTAINS_ALL)));
    }
 }
