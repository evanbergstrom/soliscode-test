package org.soliscode.test.contract.queue;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Disabled;
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
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;

import java.util.Arrays;
import java.util.Collection;

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

    /// Dynamically created instance of `QueueContract` that will run on instances of `BreakableQueue` with a
    /// specified break.
    @Disabled("Used only for dynamic test generation")
    protected static final class DynamicBrokenQueueContract
            extends DynamicContract<BreakableQueue<Integer>, CollectionProvider<Integer, BreakableQueue<Integer>>>
            implements QueueContract<Integer, BreakableQueue<Integer>>, WithIntegerElement {

        protected DynamicBrokenQueueContract(final @NonNull Break b, final @NonNull InterfaceMethod m) {
            super(b, m, (breaks, statuses, test) ->
                    BreakableQueue.queueProvider(WithIntegerElement.PROVIDER, breaks, statuses));
        }

        @Override
        public boolean supportsMethod(final @NonNull InterfaceMethod method) {
            return super.supportsMethod(method);
        }

        @Override
        public void doesNotSupportMethod(final @NonNull InterfaceMethod method) {
            super.doesNotSupportMethod(method);
        }
    }

    @Override
    protected @NonNull DynamicBrokenQueueContract createTest(final @NonNull Break b,
                                                            final @NonNull InterfaceMethod m) {
        return new DynamicBrokenQueueContract(b, m);
    }

    /// Test factory for tests of the offer method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offer` method.
    /// @see OfferContract#offer
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForOffer() {
        return Arrays.asList(
                failingTestWithBreak("offer() fails with OFFER_DOES_NOT_ADD_ELEMENT break",
                        OFFER_DOES_NOT_ADD_ELEMENT,
                        DynamicBrokenQueueContract::offer),

                failingTestWithBreak("offer() fails with OFFER_ALWAYS_RETURNS_FALSE break",
                        OFFER_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenQueueContract::offer),

                passingTestWithUnsupportedMethod("offer() fails when not supported",
                        QueueMethods.OFFER,
                        DynamicBrokenQueueContract::offer)
        );
    }

    /// Test factory for tests of the offerWithNullValue() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offerWithNullValue` method.
    /// @see OfferContract#offerWithNullValue
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForOfferWithNullValues() {
        return Arrays.asList(
                failingTestWithBreak("offerWithNullValue() fails with OFFER_DOES_NOT_ADD_ELEMENT break",
                        OFFER_DOES_NOT_ADD_ELEMENT,
                        DynamicBrokenQueueContract::offerWithNullValue),

                failingTestWithBreak("offerWithNullValue() fails with OFFER_ALWAYS_RETURNS_FALSE break",
                        OFFER_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenQueueContract::offerWithNullValue),

                passingTestWithUnsupportedMethod("offerWithNullValue() fails when not supported",
                        QueueMethods.OFFER,
                        DynamicBrokenQueueContract::offerWithNullValue)
        );
    }

    /// Test factory for tests of the offerWithDuplicateValue() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offerWithDuplicateValue` method.
    /// @see OfferContract#offerWithDuplicateValue
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForOfferWithDuplicateValues() {
        return Arrays.asList(
                failingTestWithBreak("offerWithDuplicateValue() fails with OFFER_DOES_NOT_ADD_ELEMENT break",
                        OFFER_DOES_NOT_ADD_ELEMENT,
                        DynamicBrokenQueueContract::offerWithDuplicateValue),

                failingTestWithBreak("offerWithDuplicateValue() fails with OFFER_ALWAYS_RETURNS_FALSE break",
                        OFFER_ALWAYS_RETURNS_FALSE,
                        DynamicBrokenQueueContract::offerWithDuplicateValue),

                passingTestWithUnsupportedMethod("offerWithDuplicateValue() fails when not supported",
                        QueueMethods.OFFER,
                        DynamicBrokenQueueContract::offerWithDuplicateValue)
        );
    }

    /// Test factory for tests of the remove method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `remove` method.
    /// @see RemoveContract#remove
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForRemove() {
        return Arrays.asList(
                failingTestWithBreak("remove() fails with REMOVE_ALWAYS_RETURNS_NULL break",
                        REMOVE_ALWAYS_RETURNS_NULL,
                        DynamicBrokenQueueContract::remove),

                failingTestWithBreak("remove() fails with REMOVE_DOES_NOT_REMOVE_ELEMENT break",
                        REMOVE_DOES_NOT_REMOVE_ELEMENT,
                        DynamicBrokenQueueContract::remove),

                passingTestWithUnsupportedMethod("remove() fails when not supported",
                        QueueMethods.REMOVE,
                        DynamicBrokenQueueContract::remove)
        );
    }

    /// Test factory for tests of the removeWhenEmpty method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `removeWhenEmpty` method.
    /// @see RemoveContract#removeWhenEmpty
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForRemoveWhenEmpty() {
        return Arrays.asList(
                failingTestWithBreak("removeWhenEmpty() fails with REMOVE_ALWAYS_RETURNS_NULL break",
                        REMOVE_ALWAYS_RETURNS_NULL,
                        DynamicBrokenQueueContract::removeWhenEmpty),

                passingTestWithUnsupportedMethod("removeWhenEmpty() fails when not supported",
                        QueueMethods.REMOVE,
                        DynamicBrokenQueueContract::removeWhenEmpty)
        );
    }

    /// Test factory for tests of the poll method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `poll` method.
    /// @see PollContract#poll
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForPoll() {
        return Arrays.asList(
                failingTestWithBreak("poll() fails with POLL_ALWAYS_RETURNS_NULL break",
                        POLL_ALWAYS_RETURNS_NULL,
                        DynamicBrokenQueueContract::poll),

                failingTestWithBreak("poll() fails with POLL_DOES_NOT_REMOVE_ELEMENT break",
                        POLL_DOES_NOT_REMOVE_ELEMENT,
                        DynamicBrokenQueueContract::poll),

                failingTestWithBreak("poll() fails with POLL_RETURNS_RANDOM_ELEMENT break",
                        POLL_RETURNS_RANDOM_ELEMENT,
                        DynamicBrokenQueueContract::poll),

                passingTestWithUnsupportedMethod("poll() fails when not supported",
                        QueueMethods.POLL,
                        DynamicBrokenQueueContract::poll)
        );
    }

    /// Test factory for tests of the pollWhenEmpty method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `pollWhenEmpty` method.
    /// @see PollContract#pollWhenEmpty
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForPollWhenEmpty() {
        return Arrays.asList(
                passingTestWithUnsupportedMethod("pollWhenEmpty() fails when not supported",
                        QueueMethods.POLL,
                        DynamicBrokenQueueContract::pollWhenEmpty)
        );
    }

    /// Test factory for tests of the element method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `element` method.
    /// @see ElementContract#element
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForElement() {
        return Arrays.asList(
                failingTestWithBreak("element() fails with ELEMENT_RETURNS_RANDOM_ELEMENT break",
                        ELEMENT_RETURNS_RANDOM_ELEMENT,
                        DynamicBrokenQueueContract::element),

                passingTestWithUnsupportedMethod("element() fails when not supported",
                        QueueMethods.ELEMENT,
                        DynamicBrokenQueueContract::element)
        );
    }

    /// Test factory for tests of the elementWhenEmpty method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `elementWhenEmpty` method.
    /// @see ElementContract#elementWhenEmpty
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForElementWhenEmpty() {
        return Arrays.asList(
                passingTestWithUnsupportedMethod("elementWhenEmpty() fails when not supported",
                        QueueMethods.ELEMENT,
                        DynamicBrokenQueueContract::elementWhenEmpty)
        );
    }

    /// Test factory for tests of the peek method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `peek` method.
    /// @see PeekContract#peek
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForPeek() {
        return Arrays.asList(
                failingTestWithBreak("peek() fails with PEEK_ALWAYS_RETURNS_NULL break",
                        PEEK_ALWAYS_RETURNS_NULL,
                        DynamicBrokenQueueContract::peek),

                failingTestWithBreak("peek() fails with PEEK_RETURNS_RANDOM_ELEMENT break",
                        PEEK_RETURNS_RANDOM_ELEMENT,
                        DynamicBrokenQueueContract::peek),

                passingTestWithUnsupportedMethod("peek() fails when not supported",
                        QueueMethods.PEEK,
                        DynamicBrokenQueueContract::peek)
        );
    }

    /// Test factory for tests of the peekWhenEmpty method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `peekWhenEmpty` method.
    /// @see PeekContract#peekWhenEmpty
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForPeekWhenEmpty() {
        return Arrays.asList(
                passingTestWithUnsupportedMethod("peekWhenEmpty() fails when not supported",
                        QueueMethods.PEEK,
                        DynamicBrokenQueueContract::peekWhenEmpty)
        );
    }
}
