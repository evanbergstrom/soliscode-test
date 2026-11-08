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
public class ContainsAllContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public ContainsAllContractTest() {
        super();
    }

    private static final class DynamicContainsAllContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements ContainsAllContract<BreakableCollection<Integer>, Integer> {

        public DynamicContainsAllContract(
                final @NonNull BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final @NonNull ObjectProvider<Integer> elementProvider,
                final @NonNull CollectionContractConfig config) {
            super(builder, elementProvider, config);
        }
    }

    @Override
    protected @NonNull DynamicContainsAllContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final @NonNull CollectionContractConfig config) {

        return new DynamicContainsAllContract(builder, new IntegerProvider(), config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    /// Test factory for tests of the containsAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the containsAll(Collection) method.
    /// @see ContainsAllContract#containsAll_whenNotEmpty_returnsExpectedResults
    @TestFactory
    public Collection<DynamicTest> testsForContainsAll() {
        return Arrays.asList(
                shouldFail(DynamicContainsAllContract::containsAll_whenNotEmpty_returnsExpectedResults,
                        "containsAll(Collection) works for a collection with elements fails with " +
                                "CONTAINS_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.CONTAINS_ALL_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicContainsAllContract::containsAll_whenNotEmpty_returnsExpectedResults,
                        "containsAll(Collection) works for a collection with elements fails with " +
                                "CONTAINS_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.CONTAINS_ALL_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicContainsAllContract::containsAll_whenNotEmpty_returnsExpectedResults,
                        "containsAll(Collection) works for a collection with elements fails with " +
                                "CONTAINS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.CONTAINS_ALL_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicContainsAllContract::containAll_whenNotSupported_throwsUnsupportedOperationException,
                        "containsAll(Collection) works for a collection with elements fails when not supported",
                        builder().doesNotSupport(CollectionMethods.CONTAINS_ALL)));
    }
 }