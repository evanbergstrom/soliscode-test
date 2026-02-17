package org.soliscode.test.contract.deque;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.breakable.BreakableDeque;
import org.soliscode.test.contract.ContractTest;
import org.soliscode.test.contract.DynamicContract;
import org.soliscode.test.contract.dynamic.DynamicBrokenDequeContract;
import org.soliscode.test.contract.support.WithIntegerElement;

import java.util.Arrays;
import java.util.Collection;

import static org.soliscode.test.breakable.BreakableDeque.OFFER_FIRST_ALWAYS_RETURNS_FALSE;
import static org.soliscode.test.breakable.BreakableDeque.OFFER_FIRST_DOES_NOT_ADD_ELEMENT;

/// Tests for the [DequeContract] interface.
@DisplayName("Tests for DequeContract class")
public class DequeContractTest extends ContractTest<BreakableDeque<Integer>> {

    /// Verifies that the tests all pass when testing a working Deque implementation.
    @Nested
    class WorkingDequeTest extends AbstractTest
            implements DequeContract<Integer, BreakableDeque<Integer>>,
            BreakableDeque.WithProvider<Integer>, WithIntegerElement {
    }

    @Override
    protected @NonNull DynamicContract<?, ?> createTest(final @NonNull Break b, final @NonNull InterfaceMethod m) {
        return new DynamicBrokenDequeContract(b, m);
    }

    /// Test factory for tests of the offerFirst method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offerFirst_singleElement_returnsTrueAndUpdatesSize` method.
    /// @see OfferFirstContract#offerFirst_singleElement_returnsTrueAndUpdatesSize
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForOfferFirst() {
        return Arrays.asList(
                failsWithBreak(OFFER_FIRST_DOES_NOT_ADD_ELEMENT, DynamicBrokenDequeContract::offerFirst_singleElement_returnsTrueAndUpdatesSize, "offerFirst(E) adds a single element and updates size fails with OFFER_FIRST_DOES_NOT_ADD_ELEMENT break"
                ),

                failsWithBreak(OFFER_FIRST_ALWAYS_RETURNS_FALSE, DynamicBrokenDequeContract::offerFirst_singleElement_returnsTrueAndUpdatesSize, "offerFirst(E) adds a single element and updates size fails with OFFER_FIRST_ALWAYS_RETURNS_FALSE break"
                ),

                passesWhenUnsupported(DequeMethods.OFFER_FIRST, DynamicBrokenDequeContract::offerFirst_singleElement_returnsTrueAndUpdatesSize, "offerFirst(E) adds a single element and updates size fails when not supported"
                )
        );
    }

    /// Test factory for tests of the offerFirstWithNullValue() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offerFirst_withNullValue_handlesCorrectly` method.
    /// @see OfferFirstContract#offerFirst_withNullValue_handlesCorrectly
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForOfferFirstWithNullValues() {
        return Arrays.asList(
                failsWithBreak(OFFER_FIRST_DOES_NOT_ADD_ELEMENT, DynamicBrokenDequeContract::offerFirst_withNullValue_handlesCorrectly, "offerFirst(E) handles null values based on permission fails with OFFER_FIRST_DOES_NOT_ADD_ELEMENT break"
                ),

                passesWhenUnsupported(DequeMethods.OFFER_FIRST, DynamicBrokenDequeContract::offerFirst_withNullValue_handlesCorrectly, "offerFirst(E) handles null values based on permission fails when not supported"
                )
        );
    }

    /// Test factory for tests of the offerFirstWithDuplicateValue() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offerFirst_withDuplicateValue_handlesCorrectly` method.
    /// @see OfferFirstContract#offerFirst_withDuplicateValue_handlesCorrectly
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForOfferFirstWithDuplicateValues() {
        return Arrays.asList(
                failsWithBreak(OFFER_FIRST_DOES_NOT_ADD_ELEMENT, DynamicBrokenDequeContract::offerFirst_withDuplicateValue_handlesCorrectly, "offerFirst(E) handles duplicate values based on permission fails with OFFER_FIRST_DOES_NOT_ADD_ELEMENT break"
                ),

                failsWithBreak(OFFER_FIRST_ALWAYS_RETURNS_FALSE, DynamicBrokenDequeContract::offerFirst_withDuplicateValue_handlesCorrectly, "offerFirst(E) handles duplicate values based on permission fails with OFFER_FIRST_ALWAYS_RETURNS_FALSE break"
                ),

                passesWhenUnsupported(DequeMethods.OFFER_FIRST, DynamicBrokenDequeContract::offerFirst_withDuplicateValue_handlesCorrectly, "offerFirst(E) handles duplicate values based on permission fails when not supported"
                )
        );
    }
}
