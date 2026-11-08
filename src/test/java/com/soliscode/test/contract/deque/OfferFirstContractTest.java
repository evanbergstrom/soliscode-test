package com.soliscode.test.contract.deque;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.soliscode.test.breakable.BreakableCollectionBuilder;
import com.soliscode.test.breakable.BreakableDeque;
import com.soliscode.test.contract.AbstractCollectionContractTest;
import com.soliscode.test.contract.CollectionContractConfig;
import com.soliscode.test.contract.DynamicCollectionContract;
import com.soliscode.test.provider.IntegerProvider;

import static com.soliscode.test.breakable.BreakableDeque.OFFER_FIRST_ALWAYS_RETURNS_FALSE;
import static com.soliscode.test.breakable.BreakableDeque.OFFER_FIRST_DOES_NOT_ADD_ELEMENT;

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
                final BreakableCollectionBuilder<BreakableDeque<Integer>, Integer> builder,
                final CollectionContractConfig config) {
            super(builder, new IntegerProvider(), config);
        }
    }

    @Override
    protected DynamicOfferFirstContract
        createTest(final BreakableCollectionBuilder<BreakableDeque<Integer>, Integer> builder,
               final CollectionContractConfig config) {

        return new DynamicOfferFirstContract(builder, config);
    }

    private BreakableDeque.Builder<Integer> builder() {
        return new BreakableDeque.Builder<>();
    }

    /// Test factory for tests of the offerFirst method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offerFirstSingleElementReturnsTrueAndUpdatesSize` method.
    /// @see OfferFirstContract#offerFirstSingleElementReturnsTrueAndUpdatesSize
    @DisplayName("offerFirstSingleElementReturnsTrueAndUpdatesSize fails for each offerFirst break "
            + "and passes when unsupported")
    @TestFactory
    public Collection<DynamicTest> testsForOfferFirstWithSingleElement() {
        return Arrays.asList(
                shouldFail(DynamicOfferFirstContract::offerFirstSingleElementReturnsTrueAndUpdatesSize,
                        "offerFirstSingleElementReturnsTrueAndUpdatesSize fails with " +
                                "OFFER_FIRST_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(OFFER_FIRST_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicOfferFirstContract::offerFirstSingleElementReturnsTrueAndUpdatesSize,
                        "offerFirstSingleElementReturnsTrueAndUpdatesSize fails with " +
                                "OFFER_FIRST_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(OFFER_FIRST_ALWAYS_RETURNS_FALSE)),

                shouldPass(DynamicOfferFirstContract::offerFirstSingleElementReturnsTrueAndUpdatesSize,
                        "offerFirstSingleElementReturnsTrueAndUpdatesSize passes when not supported",
                        builder().doesNotSupportMethod(DequeMethods.OFFER_FIRST)
                )
        );
    }

    /// Test factory for tests of the offerFirstWithNullValue() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offerFirstWithNullValueHandlesCorrectly` method.
    /// @see OfferFirstContract#offerFirstWithNullValueHandlesCorrectly
    @DisplayName("offerFirstWithNullValueHandlesCorrectly fails when offerFirst does not add "
            + "and passes when unsupported")
    @TestFactory
    public Collection<DynamicTest> testsForOfferFirstWithNullValues() {
        return Arrays.asList(
                shouldFail(DynamicOfferFirstContract::offerFirstWithNullValueHandlesCorrectly,
                        "offerFirstWithNullValueHandlesCorrectly fails with OFFER_FIRST_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(OFFER_FIRST_DOES_NOT_ADD_ELEMENT)),

                shouldPass(DynamicOfferFirstContract::offerFirstWithNullValueHandlesCorrectly,
                        "offerFirstWithNullValueHandlesCorrectly passes when not supported",
                        builder().doesNotSupport(DequeMethods.OFFER_FIRST))
        );
    }

    /// Test factory for tests of the offerFirstWithDuplicateValue() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offerFirstWithDuplicateValueHandlesCorrectly` method.
    /// @see OfferFirstContract#offerFirstWithDuplicateValueHandlesCorrectly
    @DisplayName("offerFirstWithDuplicateValueHandlesCorrectly fails for each offerFirst break "
            + "and passes when unsupported")
    @TestFactory
    public Collection<DynamicTest> testsForOfferFirstWithDuplicateValues() {
        return Arrays.asList(
                shouldFail(DynamicOfferFirstContract::offerFirstWithDuplicateValueHandlesCorrectly,
                        "offerFirstWithDuplicateValueHandlesCorrectly fails with OFFER_FIRST_DOES_NOT_ADD_ELEMENT " +
                                "break",
                        builder().addBreak(OFFER_FIRST_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicOfferFirstContract::offerFirstWithDuplicateValueHandlesCorrectly,
                        "offerFirstWithDuplicateValueHandlesCorrectly fails with OFFER_FIRST_ALWAYS_RETURNS_FALSE " +
                                "break",
                        builder().addBreak(OFFER_FIRST_ALWAYS_RETURNS_FALSE)
                ),

                shouldPass(DynamicOfferFirstContract::offerFirstWithDuplicateValueHandlesCorrectly,
                        "offerFirstWithDuplicateValueHandlesCorrectly passes when not supported",
                        builder().doesNotSupport(DequeMethods.OFFER_FIRST))
        );
    }
}
