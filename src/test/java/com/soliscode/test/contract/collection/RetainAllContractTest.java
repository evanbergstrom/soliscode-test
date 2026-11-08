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
public class RetainAllContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public RetainAllContractTest() {
        super();
    }

    protected static final class DynamicRetainAllContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements RetainAllContract<BreakableCollection<Integer>, Integer> {

        public DynamicRetainAllContract(
                final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final CollectionContractConfig config) {
            super(builder, new IntegerProvider(), config);
        }
    }

    @Override
    protected DynamicRetainAllContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final CollectionContractConfig config) {

        return new DynamicRetainAllContract(builder, config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    /// Test factory for tests of the retainAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the retainAll(Collection) method.
    /// @see RetainAllContract#retainAllWhenEmptyReturnsFalse
    @TestFactory
    @DisplayName("retainAll(Collection) contract tests for an empty collection fail for each break")
    public Collection<DynamicTest> testsForRetainAllWhenEmpty() {
        return Arrays.asList(
                shouldFail(DynamicRetainAllContract::retainAllWhenEmptyReturnsFalse,
                        "retainAllWhenEmptyReturnsFalse fails with RETAIN_ALL_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicRetainAllContract::retainAllWhenEmptyReturnsFalse,
                        "retainAllWhenEmptyReturnsFalse fails with RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRetainAllContract::retainAllWhenEmptyReturnsFalse,
                         "retainAllWhenEmptyReturnsFalse passes when not supported",
                         builder().doesNotSupportMethod(CollectionMethods.RETAIN_ALL))
        );
    }

    /// Test factory for tests of the retainAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the retainAll(Collection) method.
    /// @see RetainAllContract#retainAllWhenEmptyReturnsFalse
    @TestFactory
    @DisplayName("retainAll(Collection) contract tests for a collection with elements fail for each break")
    public Collection<DynamicTest> testsForRetainAllWhenNotEmpty() {
        return Arrays.asList(
                shouldFail(DynamicRetainAllContract::retainAllWhenNotEmptyReturnsExpectedResults,
                        "retainAllWhenNotEmptyReturnsExpectedResults fails with " +
                                "RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS break",
                        builder().addBreak(BreakableCollection.RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS)),

                shouldFail(DynamicRetainAllContract::retainAllWhenNotEmptyReturnsExpectedResults, "retainAll" +
                                "retainAllWhenNotEmptyReturnsExpectedResults fails with " +
                                "RETAIN_ALL_SKIPS_FIRST_ELEMENT break",
                        builder().addBreak(BreakableCollection.RETAIN_ALL_SKIPS_FIRST_ELEMENT)),

                shouldFail(DynamicRetainAllContract::retainAllWhenNotEmptyReturnsExpectedResults, "retainAll" +
                                "retainAllWhenNotEmptyReturnsExpectedResults fails with " +
                                "RETAIN_ALL_SKIPS_LAST_ELEMENT break",
                        builder().addBreak(BreakableCollection.RETAIN_ALL_SKIPS_LAST_ELEMENT)),

                shouldFail(DynamicRetainAllContract::retainAllWhenNotEmptyReturnsExpectedResults, "retainAll" +
                                "retainAllWhenNotEmptyReturnsExpectedResults fails with " +
                                "RETAIN_ALL_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicRetainAllContract::retainAllWhenNotEmptyReturnsExpectedResults, "retainAll" +
                                "retainAllWhenNotEmptyReturnsExpectedResults fails with " +
                                "RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRetainAllContract::retainAllWhenNotEmptyReturnsExpectedResults,
                        "retainAllWhenNotEmptyReturnsExpectedResults passes when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.RETAIN_ALL)));
    }
}
