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
public class ContainsContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public ContainsContractTest() {
        super();
    }

    private static final class DynamicContainsContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements ContainsContract<BreakableCollection<Integer>, Integer> {

        public DynamicContainsContract(
                final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final ObjectProvider<Integer> elementProvider,
                final CollectionContractConfig config) {
            super(builder, elementProvider, config);
        }
    }

    @Override
    protected DynamicContainsContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final CollectionContractConfig config) {

        return new DynamicContainsContract(builder, new IntegerProvider(), config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    @TestFactory
    @DisplayName("contains(Object) contract tests for an empty collection fail for each break")
    public Collection<DynamicTest> testsForContainsWhenEmpty() {
        return Arrays.asList(
                shouldFail(DynamicContainsContract::containsWhenEmptyReturnsFalse,
                        "contains(Object) returns false for an empty collection fails with " +
                                "CONTAINS_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.CONTAINS_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicContainsContract::containsWhenEmptyReturnsFalse,
                        "contains(Object) returns false for an empty collection fails with " +
                                "CONTAINS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.CONTAINS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicContainsContract::containsWhenEmptyReturnsFalse,
                        "contains(Object) returns false for an empty collection fails when not supported",
                        builder().doesNotSupport(CollectionMethods.CONTAINS))
        );
    }

    public Collection<DynamicTest> testsForContainsWhenNotEmpty() {
        return Arrays.asList(
                shouldFail(DynamicContainsContract::containsWhenNotEmptyReturnsExpectedResults,
                        "contains(Object) returns expected results for a collection with elements fails with " +
                                "CONTAINS_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.CONTAINS_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicContainsContract::containsWhenNotEmptyReturnsExpectedResults,
                        "contains (Object) returns expected results for a collection with elements fails with " +
                                "CONTAINS_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.CONTAINS_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicContainsContract::containsWhenNotEmptyReturnsExpectedResults,
                        "contains (Object) returns expected results for a collection with elements fails with " +
                                "CONTAINS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.CONTAINS_RETURNS_OPPOSITE_VALUE)),

                shouldFail(DynamicContainsContract::containsWhenNotEmptyReturnsExpectedResults,
                        "contains (Object) returns expected results for a collection with elements fails when not " +
                                "supported",
                        builder().doesNotSupport(CollectionMethods.CONTAINS))
        );
    }

}
