package org.soliscode.test.contract.deque;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Disabled;
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
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;

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

    /// Dynamically created instance of `DequeContract` that will run on instances of `BreakableDeque` with a
    /// specified break.
    @Disabled("Used only for dynamic test generation")
    protected static final class DynamicBrokenDequeContract
            extends DynamicContract<BreakableDeque<Integer>, CollectionProvider<Integer, BreakableDeque<Integer>>>
            implements DequeContract<Integer, BreakableDeque<Integer>>, WithIntegerElement {

        protected DynamicBrokenDequeContract(final @NonNull Break b, final @NonNull InterfaceMethod m) {
            super(b, m, (breaks, statuses, test) ->
                    BreakableDeque.dequeProvider(WithIntegerElement.PROVIDER, breaks, statuses));
        }
    }

    @Override
    protected @NonNull DynamicBrokenDequeContract createTest(final @NonNull Break b,
                                                            final @NonNull InterfaceMethod m) {
        return new DynamicBrokenDequeContract(b, m);
    }

    /// Test factory for tests of the offerFirst method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offerFirst` method.
    /// @see OfferFirstContract#offerFirst
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForOfferFirst() {
        return Arrays.asList(
                failingTestWithBreak("offerFirst() fails with OFFER_FIRST_DOES_NOT_ADD_ELEMENT break",
                        OFFER_FIRST_DOES_NOT_ADD_ELEMENT,
                        DynamicBrokenDequeContract::offerFirst),

                failingTestWithBreak("offerFirst() fails with OFFER_FIRST_ALWAYS_RETURNS_FALSE break",
                        OFFER_FIRST_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenDequeContract::offerFirst),

                passingTestWithUnsupportedMethod("offerFirst() fails when not supported",
                        DequeMethods.OFFER_FIRST,
                        DynamicBrokenDequeContract::offerFirst)
        );
    }

    /// Test factory for tests of the offerFirstWithNullValue() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offerFirstWithNullValue` method.
    /// @see OfferFirstContract#offerFirstWithNullValue
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForOfferFirstWithNullValues() {
        return Arrays.asList(
                failingTestWithBreak("offerFirstWithNullValue() fails with OFFER_FIRST_DOES_NOT_ADD_ELEMENT break",
                        OFFER_FIRST_DOES_NOT_ADD_ELEMENT,
                        DynamicBrokenDequeContract::offerFirstWithNullValue),

                passingTestWithUnsupportedMethod("offerFirstWithNullValue() fails when not supported",
                        DequeMethods.OFFER_FIRST,
                        DynamicBrokenDequeContract::offerFirstWithNullValue)
        );
    }

    /// Test factory for tests of the offerFirstWithDuplicateValue() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offerFirstWithDuplicateValue` method.
    /// @see OfferFirstContract#offerFirstWithDuplicateValue
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForOfferFirstWithDuplicateValues() {
        return Arrays.asList(
                failingTestWithBreak("offerFirstWithDuplicateValue() fails with OFFER_FIRST_DOES_NOT_ADD_ELEMENT break",
                        OFFER_FIRST_DOES_NOT_ADD_ELEMENT,
                        DynamicBrokenDequeContract::offerFirstWithDuplicateValue),

                failingTestWithBreak("offerFirstWithDuplicateValue() fails with OFFER_FIRST_ALWAYS_RETURNS_FALSE break",
                        OFFER_FIRST_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenDequeContract::offerFirstWithDuplicateValue),

                passingTestWithUnsupportedMethod("offerFirstWithDuplicateValue() fails when not supported",
                        DequeMethods.OFFER_FIRST,
                        DynamicBrokenDequeContract::offerFirstWithDuplicateValue)
        );
    }
}
