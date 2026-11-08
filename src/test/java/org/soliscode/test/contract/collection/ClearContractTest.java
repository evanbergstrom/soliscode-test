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
public class ClearContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public ClearContractTest() {
        super();
    }

    protected static final class DynamicClearContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements ClearContract<BreakableCollection<Integer>, Integer> {

        public DynamicClearContract(
                final @NonNull BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final @NonNull ObjectProvider<Integer> elementProvider,
                final @NonNull CollectionContractConfig config) {
            super(builder, elementProvider, config);
        }
    }

    @Override
    protected @NonNull DynamicClearContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final @NonNull CollectionContractConfig config) {

        return new DynamicClearContract(builder, new IntegerProvider(), config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    @TestFactory
    public Collection<DynamicTest> testsForClearWhenEmpty() {
        return Arrays.asList(
                shouldPass(DynamicClearContract::clear_whenEmpty_isSuccessful,
                        "clear() works for an empty collection fails when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.CLEAR))
        );
    }

    @TestFactory
    public Collection<DynamicTest> testsForClearWhenNotEmpty() {
        return Arrays.asList(
                shouldFail(DynamicClearContract::clear_whenNotEmpty_removesAllElements,
                        "clear() works for a collection with elements fails with CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS " +
                                "break",
                        builder().addBreak(BreakableCollection.CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS)),

                shouldFail(DynamicClearContract::clear_whenNotEmpty_removesAllElements,
                        "clear() works for a collection with elements fails with CLEAR_SKIPS_FIRST_ELEMENT " +
                                "break",
                        builder().addBreak(BreakableCollection.CLEAR_SKIPS_FIRST_ELEMENT)),

                shouldFail(DynamicClearContract::clear_whenNotEmpty_removesAllElements,
                        "clear() works for a collection with elements fails with CLEAR_SKIPS_LAST_ELEMENT break",
                        builder().addBreak(BreakableCollection.CLEAR_SKIPS_LAST_ELEMENT)),

                shouldPass(DynamicClearContract::clear_whenNotEmpty_removesAllElements,
                        "clear() works for a collection with elements passes when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.CLEAR))
        );
    }

    @TestFactory
    public Collection<DynamicTest> testsForClearWhenNotSupported() {
        return Arrays.asList(
                shouldPass(DynamicClearContract::clear_whenNotSupported_throwsUnsupportedOperationException,
                        "clear() works for a collection with elements passes when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.CLEAR)),

                shouldFail(DynamicClearContract::clear_whenNotSupported_throwsUnsupportedOperationException,
                        "clear() throws wrong exception with CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION break",
                        builder().doesNotSupportMethod(CollectionMethods.CLEAR)
                                .addBreak(BreakableCollection.CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION))
        );
    }
 }