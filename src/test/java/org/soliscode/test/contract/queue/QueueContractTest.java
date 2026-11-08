package org.soliscode.test.contract.queue;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.breakable.BreakableCollectionBuilder;
import org.soliscode.test.breakable.BreakableQueue;
import org.soliscode.test.contract.AbstractCollectionContractTest;
import org.soliscode.test.contract.CollectionContractConfig;
import org.soliscode.test.contract.DynamicCollectionContract;
import org.soliscode.test.contract.support.WithIntegerElement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.soliscode.test.breakable.BreakableQueue.*;

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
                final @NonNull BreakableCollectionBuilder<BreakableQueue<Integer>, Integer> builder,
                final @NonNull CollectionContractConfig config) {
            super(builder, WithIntegerElement.PROVIDER, config);
        }
    }

    @Override
    protected @NonNull DynamicQueueContract createTest(
            final @NonNull BreakableCollectionBuilder<BreakableQueue<Integer>, Integer> builder,
            final @NonNull CollectionContractConfig config) {
        return new DynamicQueueContract(builder, config);
    }

    private static BreakableQueue.Builder<Integer> builder() {
        return new BreakableQueue.Builder<>();
    }

    /// Test factory for tests of the offer method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offer` method.
    /// @see OfferContract#offer_singleElement_returnsTrueAndUpdatesSize
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForOffer() {
        return Arrays.asList(
                shouldFail(DynamicQueueContract::offer_singleElement_returnsTrueAndUpdatesSize,
                        "offer() fails with OFFER_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(OFFER_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicQueueContract::offer_singleElement_returnsTrueAndUpdatesSize,
                        "offer() fails with OFFER_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(OFFER_ALWAYS_RETURNS_FALSE)),

                shouldPass(DynamicQueueContract::offer_singleElement_returnsTrueAndUpdatesSize,
                        "offer() passes when not supported",
                        builder().doesNotSupport(QueueMethods.OFFER))
        );
    }

    /// Test factory for tests of the offer_withNullValue_handlesCorrectly() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offer_withNullValue_handlesCorrectly` method.
    /// @see OfferContract#offer_withNullValue_handlesCorrectly
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForOfferWithNullValues() {
        return Arrays.asList(
                shouldFail(DynamicQueueContract::offer_withNullValue_handlesCorrectly,
                        "offerWithNullValue() fails with OFFER_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(OFFER_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicQueueContract::offer_withNullValue_handlesCorrectly,
                        "offerWithNullValue() fails with OFFER_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(OFFER_ALWAYS_RETURNS_FALSE)),

                shouldPass(DynamicQueueContract::offer_withNullValue_handlesCorrectly,
                        "offerWithNullValue() passes when not supported",
                        builder().doesNotSupport(QueueMethods.OFFER))
        );
    }

    /// Test factory for tests of the offer_withDuplicateValue_handlesCorrectly() method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `offer_withDuplicateValue_handlesCorrectly` method.
    /// @see OfferContract#offer_withDuplicateValue_handlesCorrectly
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForOfferWithDuplicateValues() {
        return Arrays.asList(
                shouldFail(DynamicQueueContract::offer_withDuplicateValue_handlesCorrectly,
                        "offerWithDuplicateValue() fails with OFFER_DOES_NOT_ADD_ELEMENT break",
                        builder().addBreak(OFFER_DOES_NOT_ADD_ELEMENT)),

                shouldFail(DynamicQueueContract::offer_withDuplicateValue_handlesCorrectly,
                        "offerWithDuplicateValue() fails with OFFER_ALWAYS_RETURNS_FALSE break",
                        builder().addBreak(OFFER_ALWAYS_RETURNS_FALSE)),

                shouldPass(DynamicQueueContract::offer_withDuplicateValue_handlesCorrectly,
                        "offerWithDuplicateValue() passes when not supported",
                        builder().doesNotSupport(QueueMethods.OFFER))
        );
    }

    /// Test factory for tests of the remove_whenNotEmpty_returnsAndRemovesHead method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `remove_whenNotEmpty_returnsAndRemovesHead` method.
    /// @see RemoveContract#remove_whenNotEmpty_returnsAndRemovesHead
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForRemove() {
        return Arrays.asList(
                shouldFail(DynamicQueueContract::remove_whenNotEmpty_returnsAndRemovesHead,
                        "remove() fails with REMOVE_ALWAYS_RETURNS_NULL break",
                        builder().addBreak(REMOVE_ALWAYS_RETURNS_NULL)),

                shouldFail(DynamicQueueContract::remove_whenNotEmpty_returnsAndRemovesHead,
                        "remove() fails with REMOVE_DOES_NOT_REMOVE_ELEMENT break",
                        builder().addBreak(REMOVE_DOES_NOT_REMOVE_ELEMENT)),

                shouldPass(DynamicQueueContract::remove_whenNotEmpty_returnsAndRemovesHead,
                        "remove() passes when not supported",
                        builder().doesNotSupport(QueueMethods.REMOVE))
        );
    }

    /// Test factory for tests of the remove_whenEmpty_throwsNoSuchElementException method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `remove_whenEmpty_throwsNoSuchElementException` method.
    /// @see RemoveContract#remove_whenEmpty_throwsNoSuchElementException
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForRemoveWhenEmpty() {
        return Arrays.asList(
                shouldFail(DynamicQueueContract::remove_whenEmpty_throwsNoSuchElementException,
                        "removeWhenEmpty() fails with REMOVE_ALWAYS_RETURNS_NULL break",
                        builder().addBreak(REMOVE_ALWAYS_RETURNS_NULL)),

                shouldPass(DynamicQueueContract::remove_whenEmpty_throwsNoSuchElementException,
                        "removeWhenEmpty() passes when not supported",
                        builder().doesNotSupport(QueueMethods.REMOVE))
        );
    }

    /// Test factory for tests of the poll_whenNotEmpty_returnsAndRemovesHead method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `poll_whenNotEmpty_returnsAndRemovesHead` method.
    /// @see PollContract#poll_whenNotEmpty_returnsAndRemovesHead
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForPoll() {
        return Arrays.asList(
                shouldFail(DynamicQueueContract::poll_whenNotEmpty_returnsAndRemovesHead,
                        "poll() fails with POLL_ALWAYS_RETURNS_NULL break",
                        builder().addBreak(POLL_ALWAYS_RETURNS_NULL)),

                shouldFail(DynamicQueueContract::poll_whenNotEmpty_returnsAndRemovesHead,
                        "poll() fails with POLL_DOES_NOT_REMOVE_ELEMENT break",
                        builder().addBreak(POLL_DOES_NOT_REMOVE_ELEMENT)),

                shouldFail(DynamicQueueContract::poll_whenNotEmpty_returnsAndRemovesHead,
                        "poll() fails with POLL_RETURNS_RANDOM_ELEMENT break",
                        builder().addBreak(POLL_RETURNS_RANDOM_ELEMENT)),

                shouldPass(DynamicQueueContract::poll_whenNotEmpty_returnsAndRemovesHead,
                        "poll() passes when not supported",
                        builder().doesNotSupport(QueueMethods.POLL))
        );
    }

    /// Test factory for tests of the poll_whenEmpty_returnsNull method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `poll_whenEmpty_returnsNull` method.
    /// @see PollContract#poll_whenEmpty_returnsNull
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForPollWhenEmpty() {
        return Collections.singletonList(
                shouldPass(DynamicQueueContract::poll_whenEmpty_returnsNull,
                        "pollWhenEmpty() passes when not supported",
                        builder().doesNotSupport(QueueMethods.POLL))
        );
    }

    /// Test factory for tests of the element_whenNotEmpty_returnsHead method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `element_whenNotEmpty_returnsHead` method.
    /// @see ElementContract#element_whenNotEmpty_returnsHead
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForElement() {
        return Arrays.asList(
                shouldFail(DynamicQueueContract::element_whenNotEmpty_returnsHead,
                        "element() fails with ELEMENT_RETURNS_RANDOM_ELEMENT break",
                        builder().addBreak(ELEMENT_RETURNS_RANDOM_ELEMENT)),

                shouldPass(DynamicQueueContract::element_whenNotEmpty_returnsHead,
                        "element() passes when not supported",
                        builder().doesNotSupport(QueueMethods.ELEMENT))
        );
    }

    /// Test factory for tests of the element_whenEmpty_throwsNoSuchElementException method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `element_whenEmpty_throwsNoSuchElementException` method.
    /// @see ElementContract#element_whenEmpty_throwsNoSuchElementException
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForElementWhenEmpty() {
        return Collections.singletonList(
                shouldPass(DynamicQueueContract::element_whenEmpty_throwsNoSuchElementException,
                        "elementWhenEmpty() passes when not supported",
                        builder().doesNotSupport(QueueMethods.ELEMENT))
        );
    }

    /// Test factory for tests of the peek_whenNotEmpty_returnsHead method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `peek_whenNotEmpty_returnsHead` method.
    /// @see PeekContract#peek_whenNotEmpty_returnsHead
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForPeek() {
        return Arrays.asList(
                shouldFail(DynamicQueueContract::peek_whenNotEmpty_returnsHead,
                        "peek() fails with PEEK_ALWAYS_RETURNS_NULL break",
                        builder().addBreak(PEEK_ALWAYS_RETURNS_NULL)),

                shouldFail(DynamicQueueContract::peek_whenNotEmpty_returnsHead,
                        "peek() fails with PEEK_RETURNS_RANDOM_ELEMENT break",
                        builder().addBreak(PEEK_RETURNS_RANDOM_ELEMENT)),

                shouldPass(DynamicQueueContract::peek_whenNotEmpty_returnsHead,
                        "peek() passes when not supported",
                        builder().doesNotSupport(QueueMethods.PEEK))
        );
    }

    /// Test factory for tests of the peek_whenEmpty_returnsNull method that should fail for various breaks.
    ///
    /// @return a collection of dynamic tests of the `peek_whenEmpty_returnsNull` method.
    /// @see PeekContract#peek_whenEmpty_returnsNull
    @TestFactory
    public Collection<DynamicTest> dynamicTestsForPeekWhenEmpty() {
        return Collections.singletonList(
                shouldPass(DynamicQueueContract::peek_whenEmpty_returnsNull,
                        "peekWhenEmpty() passes when not supported",
                        builder().doesNotSupport(QueueMethods.PEEK))
        );
    }
}
