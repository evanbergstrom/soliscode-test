package org.soliscode.test.contract.deque;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.soliscode.test.breakable.BreakableCollectionBuilder;
import org.soliscode.test.breakable.BreakableDeque;
import org.soliscode.test.contract.AbstractCollectionContractTest;
import org.soliscode.test.contract.CollectionContractConfig;
import org.soliscode.test.contract.DynamicCollectionContract;
import org.soliscode.test.contract.dynamic.DynamicBrokenDequeContract;
import org.soliscode.test.provider.IntegerProvider;

import java.util.Arrays;
import java.util.Collection;

import static org.soliscode.test.breakable.BreakableDeque.OFFER_FIRST_ALWAYS_RETURNS_FALSE;
import static org.soliscode.test.breakable.BreakableDeque.OFFER_FIRST_DOES_NOT_ADD_ELEMENT;

/// Tests for the [DequeContract] interface.
@DisplayName("Tests for DequeContract class")
public class OfferFirstContractTest extends AbstractCollectionContractTest<BreakableDeque<Integer>, Integer> {

    /// Default constructor.
    public OfferFirstContractTest() {
        super();
    }

    protected static final class DynamicOfferFirstContract
            extends DynamicCollectionContract<BreakableDeque<Integer>, Integer>
            implements OfferFirstContract<BreakableDeque<Integer>, Integer> {

        public DynamicOfferFirstContract(
                final @NonNull BreakableCollectionBuilder<BreakableDeque<Integer>, Integer> builder,
                final @NonNull CollectionContractConfig config) {
            super(builder, new IntegerProvider(), config);
        }
    }

    @Override
    protected @NonNull DynamicOfferFirstContract
        createTest(final BreakableCollectionBuilder<BreakableDeque<Integer>, Integer> builder,
               final @NonNull CollectionContractConfig config) {

        return new DynamicOfferFirstContract(builder, config);
    }

    private BreakableDeque.Builder<Integer> builder() {
        return new BreakableDeque.Builder<>();
    }

    /// Test factory for tests of the offerFirst method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offerFirst_singleElement_returnsTrueAndUpdatesSize` method.
    /// @see OfferFirstContract#offerFirst_singleElement_returnsTrueAndUpdatesSize
    @TestFactory
    public Collection<DynamicTest> testsForOfferFirstWithSingleElement() {
        return Arrays.asList(
                shouldFail(DynamicOfferFirstContract::offerFirst_singleElement_returnsTrueAndUpdatesSize,
                        "offerFirst_singleElement_returnsTrueAndUpdatesSize fails with " +
                                "OFFER_FIRST_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(OFFER_FIRST_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicOfferFirstContract::offerFirst_singleElement_returnsTrueAndUpdatesSize,
                        "offerFirst_singleElement_returnsTrueAndUpdatesSize fails with " +
                                "OFFER_FIRST_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(OFFER_FIRST_ALWAYS_RETURNS_FALSE)),

                shouldFail(DynamicOfferFirstContract::offerFirst_singleElement_returnsTrueAndUpdatesSize,
                        "offerFirst_singleElement_returnsTrueAndUpdatesSize fails when not supported",
                        builder().doesNotSupportMethod(DequeMethods.OFFER_FIRST)
                )
        );
    }

    /// Test factory for tests of the offerFirstWithNullValue() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offerFirst_withNullValue_handlesCorrectly` method.
    /// @see OfferFirstContract#offerFirst_withNullValue_handlesCorrectly
    @TestFactory
    public Collection<DynamicTest> testsForOfferFirstWithNullValues() {
        return Arrays.asList(
                shouldFail(DynamicBrokenDequeContract::offerFirst_withNullValue_handlesCorrectly,
                        "offerFirst_withNullValue_handlesCorrectly fails with OFFER_FIRST_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(OFFER_FIRST_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicBrokenDequeContract::offerFirst_withNullValue_handlesCorrectly,
                        "offerFirst_withNullValue_handlesCorrectly fails when not supported",
                        builder().addBreak(OFFER_FIRST_DOES_NOT_ADD_ELEMENT))
        );
    }

    /// Test factory for tests of the offerFirstWithDuplicateValue() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offerFirst_withDuplicateValue_handlesCorrectly` method.
    /// @see OfferFirstContract#offerFirst_withDuplicateValue_handlesCorrectly
    @TestFactory
    public Collection<DynamicTest> testsForOfferFirstWithDuplicateValues() {
        return Arrays.asList(
                shouldFail(DynamicBrokenDequeContract::offerFirst_withDuplicateValue_handlesCorrectly,
                        "offerFirst_withDuplicateValue_handlesCorrectly fails with OFFER_FIRST_DOES_NOT_ADD_ELEMENT " +
                                "break",
                        builder().addBreak(OFFER_FIRST_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicBrokenDequeContract::offerFirst_withDuplicateValue_handlesCorrectly,
                        "offerFirst_withDuplicateValue_handlesCorrectly fails with OFFER_FIRST_ALWAYS_RETURNS_FALSE " +
                                "break",
                        builder().addBreak(OFFER_FIRST_ALWAYS_RETURNS_FALSE)
                ),

                shouldFail(DynamicBrokenDequeContract::offerFirst_withDuplicateValue_handlesCorrectly,
                        "offerFirst_withDuplicateValue_handlesCorrectly fails when not supported",
                        builder().doesNotSupport(DequeMethods.OFFER_FIRST))
        );
    }
}
