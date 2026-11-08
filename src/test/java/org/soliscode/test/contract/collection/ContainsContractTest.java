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
import org.soliscode.test.provider.ObjectProvider;

import java.util.Arrays;
import java.util.Collection;


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
                final @NonNull BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final @NonNull ObjectProvider<Integer> elementProvider,
                final @NonNull CollectionContractConfig config) {
            super(builder, elementProvider, config);
        }
    }

    @Override
    protected @NonNull DynamicContainsContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final @NonNull CollectionContractConfig config) {

        return new DynamicContainsContract(builder, new IntegerProvider(), config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    @TestFactory
    public Collection<DynamicTest> testsForContainsWhenEmpty() {
        return Arrays.asList(
                shouldFail(DynamicContainsContract::contains_whenEmpty_returnsFalse,
                        "contains(Object) returns false for an empty collection fails with " +
                                "CONTAINS_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.CONTAINS_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicContainsContract::contains_whenEmpty_returnsFalse,
                        "contains(Object) returns false for an empty collection fails with " +
                                "CONTAINS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.CONTAINS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicContainsContract::contains_whenEmpty_returnsFalse,
                        "contains(Object) returns false for an empty collection fails when not supported",
                        builder().doesNotSupport(CollectionMethods.CONTAINS))
        );
    }

    public Collection<DynamicTest> testsForContainsWhenNotEmpty() {
        return Arrays.asList(
                shouldFail(DynamicContainsContract::contains_whenNotEmpty_returnsExpectedResults,
                        "contains(Object) returns expected results for a collection with elements fails with " +
                                "CONTAINS_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.CONTAINS_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicContainsContract::contains_whenNotEmpty_returnsExpectedResults,
                        "contains (Object) returns expected results for a collection with elements fails with " +
                                "CONTAINS_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.CONTAINS_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicContainsContract::contains_whenNotEmpty_returnsExpectedResults,
                        "contains (Object) returns expected results for a collection with elements fails with " +
                                "CONTAINS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.CONTAINS_RETURNS_OPPOSITE_VALUE)),

                shouldFail(DynamicContainsContract::contains_whenNotEmpty_returnsExpectedResults,
                        "contains (Object) returns expected results for a collection with elements fails when not " +
                                "supported",
                        builder().doesNotSupport(CollectionMethods.CONTAINS))
        );
    }

}