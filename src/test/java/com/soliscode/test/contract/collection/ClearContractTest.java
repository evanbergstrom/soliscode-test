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
public class ClearContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public ClearContractTest() {
        super();
    }

    protected static final class DynamicClearContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements ClearContract<BreakableCollection<Integer>, Integer> {

        public DynamicClearContract(
                final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final ObjectProvider<Integer> elementProvider,
                final CollectionContractConfig config) {
            super(builder, elementProvider, config);
        }
    }

    @Override
    protected DynamicClearContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final CollectionContractConfig config) {

        return new DynamicClearContract(builder, new IntegerProvider(), config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    @TestFactory
    @DisplayName("clear() contract tests for an empty collection pass when clear() is not supported")
    public Collection<DynamicTest> testsForClearWhenEmpty() {
        return Arrays.asList(
                shouldPass(DynamicClearContract::clearWhenEmptyIsSuccessful,
                        "clear() works for an empty collection fails when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.CLEAR))
        );
    }

    @TestFactory
    @DisplayName("clear() contract tests for a collection with elements fail for each break")
    public Collection<DynamicTest> testsForClearWhenNotEmpty() {
        return Arrays.asList(
                shouldFail(DynamicClearContract::clearWhenNotEmptyRemovesAllElements,
                        "clear() works for a collection with elements fails with CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS " +
                                "break",
                        builder().addBreak(BreakableCollection.CLEAR_DOES_NOT_REMOVE_ANY_ELEMENTS)),

                shouldFail(DynamicClearContract::clearWhenNotEmptyRemovesAllElements,
                        "clear() works for a collection with elements fails with CLEAR_SKIPS_FIRST_ELEMENT " +
                                "break",
                        builder().addBreak(BreakableCollection.CLEAR_SKIPS_FIRST_ELEMENT)),

                shouldFail(DynamicClearContract::clearWhenNotEmptyRemovesAllElements,
                        "clear() works for a collection with elements fails with CLEAR_SKIPS_LAST_ELEMENT break",
                        builder().addBreak(BreakableCollection.CLEAR_SKIPS_LAST_ELEMENT)),

                shouldPass(DynamicClearContract::clearWhenNotEmptyRemovesAllElements,
                        "clear() works for a collection with elements passes when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.CLEAR))
        );
    }

    @TestFactory
    @DisplayName("clear() contract tests for an unsupported clear() fail for a wrong exception")
    public Collection<DynamicTest> testsForClearWhenNotSupported() {
        return Arrays.asList(
                shouldPass(DynamicClearContract::clearWhenNotSupportedThrowsUnsupportedOperationException,
                        "clear() works for a collection with elements passes when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.CLEAR)),

                shouldFail(DynamicClearContract::clearWhenNotSupportedThrowsUnsupportedOperationException,
                        "clear() throws wrong exception with CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION break",
                        builder().doesNotSupportMethod(CollectionMethods.CLEAR)
                                .addBreak(BreakableCollection.CLEAR_THROWS_WRONG_UNSUPPORTED_EXCEPTION))
        );
    }
 }
