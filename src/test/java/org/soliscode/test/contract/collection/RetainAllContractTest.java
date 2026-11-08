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
public class RetainAllContractTest extends AbstractCollectionContractTest<BreakableCollection<Integer>, Integer> {

    /// Default constructor
    public RetainAllContractTest() {
        super();
    }

    protected static final class DynamicRetainAllContract
            extends DynamicCollectionContract<BreakableCollection<Integer>, Integer>
            implements RetainAllContract<BreakableCollection<Integer>, Integer> {

        public DynamicRetainAllContract(
                final @NonNull BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                final @NonNull CollectionContractConfig config) {
            super(builder, new IntegerProvider(), config);
        }
    }

    @Override
    protected @NonNull DynamicRetainAllContract
        createTest(final BreakableCollectionBuilder<BreakableCollection<Integer>, Integer> builder,
                   final @NonNull CollectionContractConfig config) {

        return new DynamicRetainAllContract(builder, config);
    }

    private BreakableCollection.Builder<Integer> builder() {
        return new BreakableCollection.Builder<>();
    }

    /// Test factory for tests of the retainAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the retainAll(Collection) method.
    /// @see RetainAllContract#retainAll_whenEmpty_returnsFalse
    @TestFactory
    public Collection<DynamicTest> testsForRetainAllWhenEmpty() {
        return Arrays.asList(
                shouldFail(DynamicRetainAllContract::retainAll_whenEmpty_returnsFalse,
                        "retainAll_whenEmpty_returnsFalse fails with RETAIN_ALL_ALWAYS_RETURNS_TRUE break",
                        builder().addBreak(BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_TRUE)),

                shouldFail(DynamicRetainAllContract::retainAll_whenEmpty_returnsFalse,
                        "retainAll_whenEmpty_returnsFalse fails with RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRetainAllContract::retainAll_whenEmpty_returnsFalse,
                         "retainAll_whenEmpty_returnsFalse passes when not supported",
                         builder().doesNotSupportMethod(CollectionMethods.RETAIN_ALL))
        );
    }

    /// Test factory for tests of the retainAll(Collection) method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the retainAll(Collection) method.
    /// @see RetainAllContract#retainAll_whenEmpty_returnsFalse
    @TestFactory
    public Collection<DynamicTest> testsForRetainAllWhenNotEmpty() {
        return Arrays.asList(
                shouldFail(DynamicRetainAllContract::retainAll_whenNotEmpty_returnsExpectedResults,
                        "retainAll_whenNotEmpty_returnsExpectedResults fails with " +
                                "RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS break",
                        builder().addBreak(BreakableCollection.RETAIN_ALL_DOES_NOT_RETAIN_ANY_ELEMENTS)),

                shouldFail(DynamicRetainAllContract::retainAll_whenNotEmpty_returnsExpectedResults, "retainAll" +
                                "retainAll_whenNotEmpty_returnsExpectedResults fails with " +
                                "RETAIN_ALL_SKIPS_FIRST_ELEMENT break",
                        builder().addBreak(BreakableCollection.RETAIN_ALL_SKIPS_FIRST_ELEMENT)),

                shouldFail(DynamicRetainAllContract::retainAll_whenNotEmpty_returnsExpectedResults, "retainAll" +
                                "retainAll_whenNotEmpty_returnsExpectedResults fails with " +
                                "RETAIN_ALL_SKIPS_LAST_ELEMENT break",
                        builder().addBreak(BreakableCollection.RETAIN_ALL_SKIPS_LAST_ELEMENT)),

                shouldFail(DynamicRetainAllContract::retainAll_whenNotEmpty_returnsExpectedResults, "retainAll" +
                                "retainAll_whenNotEmpty_returnsExpectedResults fails with " +
                                "RETAIN_ALL_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicRetainAllContract::retainAll_whenNotEmpty_returnsExpectedResults, "retainAll" +
                                "retainAll_whenNotEmpty_returnsExpectedResults fails with " +
                                "RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE break",
                        builder().addBreak(BreakableCollection.RETAIN_ALL_ALWAYS_RETURNS_OPPOSITE_VALUE)),

                shouldPass(DynamicRetainAllContract::retainAll_whenNotEmpty_returnsExpectedResults,
                        "retainAll_whenNotEmpty_returnsExpectedResults passes when not supported",
                        builder().doesNotSupportMethod(CollectionMethods.RETAIN_ALL)));
    }
}