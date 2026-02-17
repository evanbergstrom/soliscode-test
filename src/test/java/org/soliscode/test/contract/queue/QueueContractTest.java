package org.soliscode.test.contract.queue;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.breakable.BreakableQueue;
import org.soliscode.test.contract.ContractTest;
import org.soliscode.test.contract.DynamicContract;
import org.soliscode.test.contract.dynamic.DynamicBrokenQueueContract;
import org.soliscode.test.contract.support.WithIntegerElement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.soliscode.test.breakable.BreakableQueue.*;

/// Tests for the [QueueContract] interface.
@DisplayName("Tests for QueueContract class")
public class QueueContractTest extends ContractTest<BreakableQueue<Integer>> {

    /// Verifies that the tests all pass when testing a working Queue implementation.
    @Nested
    class WorkingQueueTest extends AbstractTest
            implements QueueContract<Integer, BreakableQueue<Integer>>,
            BreakableQueue.WithProvider<Integer>, WithIntegerElement {
    }

    @Override
    protected @NonNull DynamicContract<?, ?> createTest(final @NonNull Break b, final @NonNull InterfaceMethod m) {
        return new DynamicBrokenQueueContract(b, m);
    }

    /// Test factory for tests of the offer method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offer` method.
    /// @see OfferContract#offer_singleElement_returnsTrueAndUpdatesSize
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForOffer() {
        return Arrays.asList(
                failsWithBreak(OFFER_DOES_NOT_ADD_ELEMENT, DynamicBrokenQueueContract::offer_singleElement_returnsTrueAndUpdatesSize, "offer() fails with OFFER_DOES_NOT_ADD_ELEMENT break"
                ),

                failsWithBreak(OFFER_ALWAYS_RETURNS_FALSE, DynamicBrokenQueueContract::offer_singleElement_returnsTrueAndUpdatesSize, "offer() fails with OFFER_ALWAYS_RETURNS_FALSE break"
                ),

                passesWhenUnsupported(QueueMethods.OFFER, DynamicBrokenQueueContract::offer_singleElement_returnsTrueAndUpdatesSize, "offer() fails when not supported"
                )
        );
    }

    /// Test factory for tests of the offer_withNullValue_handlesCorrectly() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offer_withNullValue_handlesCorrectly` method.
    /// @see OfferContract#offer_withNullValue_handlesCorrectly
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForOfferWithNullValues() {
        return Arrays.asList(
                failsWithBreak(OFFER_DOES_NOT_ADD_ELEMENT, DynamicBrokenQueueContract::offer_withNullValue_handlesCorrectly, "offerWithNullValue() fails with OFFER_DOES_NOT_ADD_ELEMENT break"
                ),

                failsWithBreak(OFFER_ALWAYS_RETURNS_FALSE, DynamicBrokenQueueContract::offer_withNullValue_handlesCorrectly, "offerWithNullValue() fails with OFFER_ALWAYS_RETURNS_FALSE break"
                ),

                passesWhenUnsupported(QueueMethods.OFFER, DynamicBrokenQueueContract::offer_withNullValue_handlesCorrectly, "offerWithNullValue() fails when not supported"
                )
        );
    }

    /// Test factory for tests of the offer_withDuplicateValue_handlesCorrectly() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offer_withDuplicateValue_handlesCorrectly` method.
    /// @see OfferContract#offer_withDuplicateValue_handlesCorrectly
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForOfferWithDuplicateValues() {
        return Arrays.asList(
                failsWithBreak(OFFER_DOES_NOT_ADD_ELEMENT, DynamicBrokenQueueContract::offer_withDuplicateValue_handlesCorrectly, "offerWithDuplicateValue() fails with OFFER_DOES_NOT_ADD_ELEMENT break"
                ),

                failsWithBreak(OFFER_ALWAYS_RETURNS_FALSE, DynamicBrokenQueueContract::offer_withDuplicateValue_handlesCorrectly, "offerWithDuplicateValue() fails with OFFER_ALWAYS_RETURNS_FALSE break"
                ),

                passesWhenUnsupported(QueueMethods.OFFER, DynamicBrokenQueueContract::offer_withDuplicateValue_handlesCorrectly, "offerWithDuplicateValue() fails when not supported"
                )
        );
    }

    /// Test factory for tests of the remove_whenNotEmpty_returnsAndRemovesHead method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `remove_whenNotEmpty_returnsAndRemovesHead` method.
    /// @see RemoveContract#remove_whenNotEmpty_returnsAndRemovesHead
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForRemove() {
        return Arrays.asList(
                failsWithBreak(REMOVE_ALWAYS_RETURNS_NULL, DynamicBrokenQueueContract::remove_whenNotEmpty_returnsAndRemovesHead, "remove() fails with REMOVE_ALWAYS_RETURNS_NULL break"
                ),

                failsWithBreak(REMOVE_DOES_NOT_REMOVE_ELEMENT, DynamicBrokenQueueContract::remove_whenNotEmpty_returnsAndRemovesHead, "remove() fails with REMOVE_DOES_NOT_REMOVE_ELEMENT break"
                ),

                passesWhenUnsupported(QueueMethods.REMOVE, DynamicBrokenQueueContract::remove_whenNotEmpty_returnsAndRemovesHead, "remove() fails when not supported"
                )
        );
    }

    /// Test factory for tests of the remove_whenEmpty_throwsNoSuchElementException method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `remove_whenEmpty_throwsNoSuchElementException` method.
    /// @see RemoveContract#remove_whenEmpty_throwsNoSuchElementException
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForRemoveWhenEmpty() {
        return Arrays.asList(
                failsWithBreak(REMOVE_ALWAYS_RETURNS_NULL, DynamicBrokenQueueContract::remove_whenEmpty_throwsNoSuchElementException, "removeWhenEmpty() fails with REMOVE_ALWAYS_RETURNS_NULL break"
                ),

                passesWhenUnsupported(QueueMethods.REMOVE, DynamicBrokenQueueContract::remove_whenEmpty_throwsNoSuchElementException, "removeWhenEmpty() fails when not supported"
                )
        );
    }

    /// Test factory for tests of the poll_whenNotEmpty_returnsAndRemovesHead method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `poll_whenNotEmpty_returnsAndRemovesHead` method.
    /// @see PollContract#poll_whenNotEmpty_returnsAndRemovesHead
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForPoll() {
        return Arrays.asList(
                failsWithBreak(POLL_ALWAYS_RETURNS_NULL, DynamicBrokenQueueContract::poll_whenNotEmpty_returnsAndRemovesHead, "poll() fails with POLL_ALWAYS_RETURNS_NULL break"
                ),

                failsWithBreak(POLL_DOES_NOT_REMOVE_ELEMENT, DynamicBrokenQueueContract::poll_whenNotEmpty_returnsAndRemovesHead, "poll() fails with POLL_DOES_NOT_REMOVE_ELEMENT break"
                ),

                failsWithBreak(POLL_RETURNS_RANDOM_ELEMENT, DynamicBrokenQueueContract::poll_whenNotEmpty_returnsAndRemovesHead, "poll() fails with POLL_RETURNS_RANDOM_ELEMENT break"
                ),

                passesWhenUnsupported(QueueMethods.POLL, DynamicBrokenQueueContract::poll_whenNotEmpty_returnsAndRemovesHead, "poll() fails when not supported"
                )
        );
    }

    /// Test factory for tests of the poll_whenEmpty_returnsNull method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `poll_whenEmpty_returnsNull` method.
    /// @see PollContract#poll_whenEmpty_returnsNull
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForPollWhenEmpty() {
        return Collections.singletonList(
                passesWhenUnsupported(QueueMethods.POLL, DynamicBrokenQueueContract::poll_whenEmpty_returnsNull, "pollWhenEmpty() fails when not supported"
                )
        );
    }

    /// Test factory for tests of the element_whenNotEmpty_returnsHead method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `element_whenNotEmpty_returnsHead` method.
    /// @see ElementContract#element_whenNotEmpty_returnsHead
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForElement() {
        return Arrays.asList(
                failsWithBreak(ELEMENT_RETURNS_RANDOM_ELEMENT, DynamicBrokenQueueContract::element_whenNotEmpty_returnsHead, "element() fails with ELEMENT_RETURNS_RANDOM_ELEMENT break"
                ),

                passesWhenUnsupported(QueueMethods.ELEMENT, DynamicBrokenQueueContract::element_whenNotEmpty_returnsHead, "element() fails when not supported"
                )
        );
    }

    /// Test factory for tests of the element_whenEmpty_throwsNoSuchElementException method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `element_whenEmpty_throwsNoSuchElementException` method.
    /// @see ElementContract#element_whenEmpty_throwsNoSuchElementException
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForElementWhenEmpty() {
        return Collections.singletonList(
                passesWhenUnsupported(QueueMethods.ELEMENT, DynamicBrokenQueueContract::element_whenEmpty_throwsNoSuchElementException, "elementWhenEmpty() fails when not supported"
                )
        );
    }

    /// Test factory for tests of the peek_whenNotEmpty_returnsHead method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `peek_whenNotEmpty_returnsHead` method.
    /// @see PeekContract#peek_whenNotEmpty_returnsHead
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForPeek() {
        return Arrays.asList(
                failsWithBreak(PEEK_ALWAYS_RETURNS_NULL, DynamicBrokenQueueContract::peek_whenNotEmpty_returnsHead, "peek() fails with PEEK_ALWAYS_RETURNS_NULL break"
                ),

                failsWithBreak(PEEK_RETURNS_RANDOM_ELEMENT, DynamicBrokenQueueContract::peek_whenNotEmpty_returnsHead, "peek() fails with PEEK_RETURNS_RANDOM_ELEMENT break"
                ),

                passesWhenUnsupported(QueueMethods.PEEK, DynamicBrokenQueueContract::peek_whenNotEmpty_returnsHead, "peek() fails when not supported"
                )
        );
    }

    /// Test factory for tests of the peek_whenEmpty_returnsNull method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `peek_whenEmpty_returnsNull` method.
    /// @see PeekContract#peek_whenEmpty_returnsNull
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForPeekWhenEmpty() {
        return Collections.singletonList(
                passesWhenUnsupported(QueueMethods.PEEK, DynamicBrokenQueueContract::peek_whenEmpty_returnsNull, "peekWhenEmpty() fails when not supported"
                )
        );
    }
}
