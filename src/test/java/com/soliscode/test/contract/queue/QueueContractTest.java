package com.soliscode.test.contract.queue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.breakable.BreakableCollectionBuilder;
import com.soliscode.test.breakable.BreakableQueue;
import com.soliscode.test.contract.AbstractCollectionContractTest;
import com.soliscode.test.contract.CollectionContractConfig;
import com.soliscode.test.contract.DynamicCollectionContract;
import com.soliscode.test.contract.support.WithIntegerElement;

import static com.soliscode.test.breakable.BreakableQueue.ELEMENT_RETURNS_RANDOM_ELEMENT;
import static com.soliscode.test.breakable.BreakableQueue.OFFER_ALWAYS_RETURNS_FALSE;
import static com.soliscode.test.breakable.BreakableQueue.OFFER_DOES_NOT_ADD_ELEMENT;
import static com.soliscode.test.breakable.BreakableQueue.PEEK_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableQueue.PEEK_RETURNS_RANDOM_ELEMENT;
import static com.soliscode.test.breakable.BreakableQueue.POLL_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableQueue.POLL_DOES_NOT_REMOVE_ELEMENT;
import static com.soliscode.test.breakable.BreakableQueue.POLL_RETURNS_RANDOM_ELEMENT;
import static com.soliscode.test.breakable.BreakableQueue.REMOVE_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableQueue.REMOVE_DOES_NOT_REMOVE_ELEMENT;

/// Tests for the [QueueContract] interface.
@DisplayName("Tests for QueueContract class")
public class QueueContractTest extends AbstractCollectionContractTest<BreakableQueue<Integer>, Integer> {

    /// Default constructor.
    public QueueContractTest() {
        super();
    }

    /// Verifies that the tests all pass when testing a working Queue implementation.
    @Nested
    class WorkingQueueTest extends AbstractTest
            implements QueueContract<BreakableQueue<Integer>, Integer>,
            BreakableQueue.WithProvider<Integer>, WithIntegerElement {
    }

    /// A queue contract for queues built by a builder.
    protected static final class DynamicQueueContract
            extends DynamicCollectionContract<BreakableQueue<Integer>, Integer>
            implements QueueContract<BreakableQueue<Integer>, Integer> {

        /// Creates the contract.
        ///
        /// @param builder the builder of the queues under test
        /// @param config the contract's configuration
        public DynamicQueueContract(
                final BreakableCollectionBuilder<BreakableQueue<Integer>, Integer> builder,
                final CollectionContractConfig config) {
            super(builder, WithIntegerElement.PROVIDER, config);
        }
    }

    @Override
    protected DynamicQueueContract createTest(
            final BreakableCollectionBuilder<BreakableQueue<Integer>, Integer> builder,
            final CollectionContractConfig config) {
        return new DynamicQueueContract(builder, config);
    }

    private static BreakableQueue.Builder<Integer> builder() {
        return new BreakableQueue.Builder<>();
    }

    /// Test factory for tests of the offer method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offer` method.
    /// @see OfferContract#offerSingleElementReturnsTrueAndUpdatesSize
    @TestFactory
    @DisplayName("offer(E) fails for each break of offer and passes when offer is not supported")
    public Collection<DynamicTest> dynamicTestsForOffer() {
        return Arrays.asList(
                shouldFail(DynamicQueueContract::offerSingleElementReturnsTrueAndUpdatesSize,
                        "offer() fails with OFFER_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(OFFER_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicQueueContract::offerSingleElementReturnsTrueAndUpdatesSize,
                        "offer() fails with OFFER_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(OFFER_ALWAYS_RETURNS_FALSE)),

                shouldPass(DynamicQueueContract::offerSingleElementReturnsTrueAndUpdatesSize,
                        "offer() passes when not supported",
                        builder().doesNotSupport(QueueMethods.OFFER))
        );
    }

    /// Test factory for tests of the offerWithNullValueHandlesCorrectly() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offerWithNullValueHandlesCorrectly` method.
    /// @see OfferContract#offerWithNullValueHandlesCorrectly
    @TestFactory
    @DisplayName("offer(null) fails for each break of offer and passes when offer is not supported")
    public Collection<DynamicTest> dynamicTestsForOfferWithNullValues() {
        return Arrays.asList(
                shouldFail(DynamicQueueContract::offerWithNullValueHandlesCorrectly,
                        "offerWithNullValue() fails with OFFER_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(OFFER_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicQueueContract::offerWithNullValueHandlesCorrectly,
                        "offerWithNullValue() fails with OFFER_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(OFFER_ALWAYS_RETURNS_FALSE)),

                shouldPass(DynamicQueueContract::offerWithNullValueHandlesCorrectly,
                        "offerWithNullValue() passes when not supported",
                        builder().doesNotSupport(QueueMethods.OFFER))
        );
    }

    /// Test factory for tests of the offerWithDuplicateValueHandlesCorrectly() method that should fail for various
    /// breaks.
    ///
    /// @return a collection of dynamic tests of the `offerWithDuplicateValueHandlesCorrectly` method.
    /// @see OfferContract#offerWithDuplicateValueHandlesCorrectly
    @TestFactory
    @DisplayName("offer(E) of a duplicate fails for each break of offer and passes when offer is not supported")
    public Collection<DynamicTest> dynamicTestsForOfferWithDuplicateValues() {
        return Arrays.asList(
                shouldFail(DynamicQueueContract::offerWithDuplicateValueHandlesCorrectly,
                        "offerWithDuplicateValue() fails with OFFER_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(OFFER_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicQueueContract::offerWithDuplicateValueHandlesCorrectly,
                        "offerWithDuplicateValue() fails with OFFER_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(OFFER_ALWAYS_RETURNS_FALSE)),

                shouldPass(DynamicQueueContract::offerWithDuplicateValueHandlesCorrectly,
                        "offerWithDuplicateValue() passes when not supported",
                        builder().doesNotSupport(QueueMethods.OFFER))
        );
    }

    /// Test factory for tests of the removeWhenNotEmptyReturnsAndRemovesHead method that should fail for various
    /// breaks.
    ///
    /// @return a collection of dynamic tests of the `removeWhenNotEmptyReturnsAndRemovesHead` method.
    /// @see RemoveContract#removeWhenNotEmptyReturnsAndRemovesHead
    @TestFactory
    @DisplayName("remove() fails for each break of remove and passes when remove is not supported")
    public Collection<DynamicTest> dynamicTestsForRemove() {
        return Arrays.asList(
                shouldFail(DynamicQueueContract::removeWhenNotEmptyReturnsAndRemovesHead,
                        "remove() fails with REMOVE_ALWAYS_RETURNS_NULL break",
                        builder().addBreak(REMOVE_ALWAYS_RETURNS_NULL)),

                shouldFail(DynamicQueueContract::removeWhenNotEmptyReturnsAndRemovesHead,
                        "remove() fails with REMOVE_DOES_NOT_REMOVE_ELEMENT break",
                        builder().addBreak(REMOVE_DOES_NOT_REMOVE_ELEMENT)),

                shouldPass(DynamicQueueContract::removeWhenNotEmptyReturnsAndRemovesHead,
                        "remove() passes when not supported",
                        builder().doesNotSupport(QueueMethods.REMOVE))
        );
    }

    /// Test factory for tests of the removeWhenEmptyThrowsNoSuchElementException method that should fail for various
    /// breaks.
    ///
    /// @return a collection of dynamic tests of the `removeWhenEmptyThrowsNoSuchElementException` method.
    /// @see RemoveContract#removeWhenEmptyThrowsNoSuchElementException
    @TestFactory
    @DisplayName("remove() on an empty queue fails for REMOVE_ALWAYS_RETURNS_NULL and passes when not supported")
    public Collection<DynamicTest> dynamicTestsForRemoveWhenEmpty() {
        return Arrays.asList(
                shouldFail(DynamicQueueContract::removeWhenEmptyThrowsNoSuchElementException,
                        "removeWhenEmpty() fails with REMOVE_ALWAYS_RETURNS_NULL break",
                        builder().addBreak(REMOVE_ALWAYS_RETURNS_NULL)),

                shouldPass(DynamicQueueContract::removeWhenEmptyThrowsNoSuchElementException,
                        "removeWhenEmpty() passes when not supported",
                        builder().doesNotSupport(QueueMethods.REMOVE))
        );
    }

    /// Test factory for tests of the pollWhenNotEmptyReturnsAndRemovesHead method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `pollWhenNotEmptyReturnsAndRemovesHead` method.
    /// @see PollContract#pollWhenNotEmptyReturnsAndRemovesHead
    @TestFactory
    @DisplayName("poll() fails for each break of poll and passes when poll is not supported")
    public Collection<DynamicTest> dynamicTestsForPoll() {
        return Arrays.asList(
                shouldFail(DynamicQueueContract::pollWhenNotEmptyReturnsAndRemovesHead,
                        "poll() fails with POLL_ALWAYS_RETURNS_NULL break",
                        builder().addBreak(POLL_ALWAYS_RETURNS_NULL)),

                shouldFail(DynamicQueueContract::pollWhenNotEmptyReturnsAndRemovesHead,
                        "poll() fails with POLL_DOES_NOT_REMOVE_ELEMENT break",
                        builder().addBreak(POLL_DOES_NOT_REMOVE_ELEMENT)),

                shouldFail(DynamicQueueContract::pollWhenNotEmptyReturnsAndRemovesHead,
                        "poll() fails with POLL_RETURNS_RANDOM_ELEMENT break",
                        builder().addBreak(POLL_RETURNS_RANDOM_ELEMENT)),

                shouldPass(DynamicQueueContract::pollWhenNotEmptyReturnsAndRemovesHead,
                        "poll() passes when not supported",
                        builder().doesNotSupport(QueueMethods.POLL))
        );
    }

    /// Test factory for tests of the pollWhenEmptyReturnsNull method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `pollWhenEmptyReturnsNull` method.
    /// @see PollContract#pollWhenEmptyReturnsNull
    @TestFactory
    @DisplayName("poll() on an empty queue passes when poll is not supported")
    public Collection<DynamicTest> dynamicTestsForPollWhenEmpty() {
        return Collections.singletonList(
                shouldPass(DynamicQueueContract::pollWhenEmptyReturnsNull,
                        "pollWhenEmpty() passes when not supported",
                        builder().doesNotSupport(QueueMethods.POLL))
        );
    }

    /// Test factory for tests of the elementWhenNotEmptyReturnsHead method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `elementWhenNotEmptyReturnsHead` method.
    /// @see ElementContract#elementWhenNotEmptyReturnsHead
    @TestFactory
    @DisplayName("element() fails for ELEMENT_RETURNS_RANDOM_ELEMENT and passes when element is not supported")
    public Collection<DynamicTest> dynamicTestsForElement() {
        return Arrays.asList(
                shouldFail(DynamicQueueContract::elementWhenNotEmptyReturnsHead,
                        "element() fails with ELEMENT_RETURNS_RANDOM_ELEMENT break",
                        builder().addBreak(ELEMENT_RETURNS_RANDOM_ELEMENT)),

                shouldPass(DynamicQueueContract::elementWhenNotEmptyReturnsHead,
                        "element() passes when not supported",
                        builder().doesNotSupport(QueueMethods.ELEMENT))
        );
    }

    /// Test factory for tests of the elementWhenEmptyThrowsNoSuchElementException method that should fail for various
    /// breaks.
    ///
    /// @return a collection of dynamic tests of the `elementWhenEmptyThrowsNoSuchElementException` method.
    /// @see ElementContract#elementWhenEmptyThrowsNoSuchElementException
    @TestFactory
    @DisplayName("element() on an empty queue passes when element is not supported")
    public Collection<DynamicTest> dynamicTestsForElementWhenEmpty() {
        return Collections.singletonList(
                shouldPass(DynamicQueueContract::elementWhenEmptyThrowsNoSuchElementException,
                        "elementWhenEmpty() passes when not supported",
                        builder().doesNotSupport(QueueMethods.ELEMENT))
        );
    }

    /// Test factory for tests of the peekWhenNotEmptyReturnsHead method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `peekWhenNotEmptyReturnsHead` method.
    /// @see PeekContract#peekWhenNotEmptyReturnsHead
    @TestFactory
    @DisplayName("peek() fails for each break of peek and passes when peek is not supported")
    public Collection<DynamicTest> dynamicTestsForPeek() {
        return Arrays.asList(
                shouldFail(DynamicQueueContract::peekWhenNotEmptyReturnsHead,
                        "peek() fails with PEEK_ALWAYS_RETURNS_NULL break",
                        builder().addBreak(PEEK_ALWAYS_RETURNS_NULL)),

                shouldFail(DynamicQueueContract::peekWhenNotEmptyReturnsHead,
                        "peek() fails with PEEK_RETURNS_RANDOM_ELEMENT break",
                        builder().addBreak(PEEK_RETURNS_RANDOM_ELEMENT)),

                shouldPass(DynamicQueueContract::peekWhenNotEmptyReturnsHead,
                        "peek() passes when not supported",
                        builder().doesNotSupport(QueueMethods.PEEK))
        );
    }

    /// Test factory for tests of the peekWhenEmptyReturnsNull method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `peekWhenEmptyReturnsNull` method.
    /// @see PeekContract#peekWhenEmptyReturnsNull
    @TestFactory
    @DisplayName("peek() on an empty queue passes when peek is not supported")
    public Collection<DynamicTest> dynamicTestsForPeekWhenEmpty() {
        return Collections.singletonList(
                shouldPass(DynamicQueueContract::peekWhenEmptyReturnsNull,
                        "peekWhenEmpty() passes when not supported",
                        builder().doesNotSupport(QueueMethods.PEEK))
        );
    }
}
