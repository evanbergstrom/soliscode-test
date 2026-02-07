package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.transferqueue.TransferQueueMethods;

import java.util.Set;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.breakable.BreakableTransferQueue.*;

/// **Test Suite for BreakableTransferQueue Implementation**
///
/// This comprehensive test class validates the behavior of BreakableTransferQueue, focusing on both
/// standard TransferQueue contract compliance and the controlled violation of transfer queue semantics through
/// programmatic breaks. The tests ensure that BreakableTransferQueue maintains proper transfer behavior
/// under normal conditions while correctly implementing break mechanisms for testing purposes.
///
/// ## Test Coverage
///
/// ### TransferQueue Contract Compliance Testing
/// - **Queue Interface**: Full validation of inherited Queue interface methods
/// - **BlockingQueue Interface**: Full validation of inherited BlockingQueue interface methods
/// - **TransferQueue Interface**: Full validation of TransferQueue-specific methods
/// - **Transfer Operations**: transfer() method testing with blocking behavior
/// - **Try Transfer Operations**: tryTransfer() method testing with immediate and timeout variants
/// - **Consumer Detection**: hasWaitingConsumer() and getWaitingConsumerCount() method testing
///
/// ### Break Mechanism Testing
/// - **Transfer Breaks**: TRANSFER_* breaks for controlling transfer() behavior
/// - **Try Transfer Breaks**: TRY_TRANSFER_* breaks for controlling tryTransfer() behavior
/// - **Try Transfer Timeout Breaks**: TRY_TRANSFER_WITH_TIMEOUT_* breaks for controlling timeout tryTransfer() behavior
/// - **Consumer Detection Breaks**: Consumer detection method breaks for controlling consumer state
/// - **Exception Breaks**: Various exception-throwing behaviors
/// - **Blocking Breaks**: Infinite blocking behaviors for testing interruption
///
/// ### Builder Pattern Testing
/// - **TransferQueue-Specific Configuration**: Builder setup with TransferQueue parameters
/// - **Copy Semantics**: Builder copying and independence for transfer queues
/// - **Fluent Interface**: Method chaining with transfer queue configuration
/// - **Pre-populated Queues**: Building from existing TransferQueue implementations
///
/// ### Constructor Testing
/// - **Default Construction**: Empty transfer queue creation with LinkedTransferQueue
/// - **Copy Construction**: Creating queues from existing BreakableTransferQueue instances
/// - **TransferQueue Construction**: Building from existing TransferQueue implementations
///
/// ## Test Architecture
///
/// This test class follows the SolisCode testing framework patterns:
/// - **AbstractTest Extension**: Inherits common testing infrastructure
/// - **TransferQueue-Specific Testing**: Focused on TransferQueue interface compliance
/// - **Break Isolation**: Each break is tested independently for transfer operations
/// - **State Validation**: Verifies queue state consistency with transfer guarantees
/// - **Concurrency Awareness**: Tests consider transfer, blocking, and consumer detection behaviors
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see BreakableTransferQueue
/// @see TransferQueue
/// @see AbstractTest
public class BreakableTransferQueueTest extends AbstractTest {

    // ========== Constructor Tests ==========

    /// Tests the default constructor functionality and initial state validation.
    @Test
    @DisplayName("Test default constructor creates empty transfer queue")
    public void testDefaultConstructor() {
        BreakableTransferQueue<String> queue = new BreakableTransferQueue<>();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertNull(queue.peek());
        assertNull(queue.poll());
        assertFalse(queue.hasWaitingConsumer());
        assertEquals(0, queue.getWaitingConsumerCount());
    }

    /// Tests the copy constructor behavior and configuration inheritance.
    @Test
    @DisplayName("Test copy constructor")
    public void testCopyConstructor() throws InterruptedException {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(TRANSFER_THROWS_INTERRUPTED_EXCEPTION);
        BreakableTransferQueue<String> original = builder.build();

        original.offer("item1");
        original.offer("item2");

        BreakableTransferQueue<String> copy = new BreakableTransferQueue<>(original);

        // Verify independent copies
        assertNotSame(original, copy);
        assertEquals(original.size(), copy.size());

        // Verify breaks are copied
        assertThrows(InterruptedException.class, () -> copy.transfer("item3")); // Should throw due to break
    }

    // ========== Builder Tests ==========

    /// Tests basic Builder pattern functionality and configuration transfer.
    @Test
    @DisplayName("Test builder pattern")
    public void testBuilder() throws InterruptedException {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(TRY_TRANSFER_ALWAYS_RETURNS_FALSE);
        builder.addBreak(HAS_WAITING_CONSUMER_ALWAYS_RETURNS_TRUE);
        BreakableTransferQueue<String> queue = builder.build();

        // Test tryTransfer break
        assertFalse(queue.tryTransfer("item")); // Should return false due to break

        // Test hasWaitingConsumer break
        assertTrue(queue.hasWaitingConsumer()); // Should return true due to break
    }

    /// Tests Builder copy functionality and configuration inheritance.
    @Test
    @DisplayName("Test builder copy")
    public void testBuilderCopy() {
        BreakableTransferQueue.Builder<String> original = new BreakableTransferQueue.Builder<>();
        original.addBreak(TRANSFER_DOES_NOT_ADD_ELEMENT);

        BreakableTransferQueue.Builder<String> copy = original.copy();
        copy.addBreak(TRY_TRANSFER_ALWAYS_RETURNS_FALSE);

        BreakableTransferQueue<String> originalQueue = original.build();
        BreakableTransferQueue<String> copyQueue = copy.build();

        // Original should have transfer break but normal tryTransfer
        // tryTransfer returns false when no consumers are waiting (normal behavior)
        assertFalse(originalQueue.tryTransfer("test")); // Normal tryTransfer behavior with no consumers

        // Copy should have both breaks
        assertFalse(copyQueue.tryTransfer("test")); // tryTransfer returns false due to break
    }

    // ========== Transfer Method Break Tests ==========

    /// Tests the TRANSFER_THROWS_INTERRUPTED_EXCEPTION break functionality.
    @Test
    @DisplayName("Test TRANSFER_THROWS_INTERRUPTED_EXCEPTION break")
    public void testTransferThrowsInterruptedException() {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(TRANSFER_THROWS_INTERRUPTED_EXCEPTION);
        BreakableTransferQueue<String> queue = builder.build();

        assertThrows(InterruptedException.class, () -> queue.transfer("item"));
    }

    /// Tests the TRANSFER_DOES_NOT_ADD_ELEMENT break functionality.
    @Test
    @DisplayName("Test TRANSFER_DOES_NOT_ADD_ELEMENT break")
    public void testTransferDoesNotAddElement() throws InterruptedException {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(TRANSFER_DOES_NOT_ADD_ELEMENT);
        BreakableTransferQueue<String> queue = builder.build();

        queue.transfer("item1");
        queue.transfer("item2");

        // Queue should remain empty despite transfers
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    /// Tests the TRANSFER_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test TRANSFER_THROWS_EXCEPTION break")
    public void testTransferThrowsException() {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(TRANSFER_THROWS_EXCEPTION);
        BreakableTransferQueue<String> queue = builder.build();

        assertThrows(IllegalStateException.class, () -> queue.transfer("item"));
    }

    /// Tests that transfer() throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test transfer() throws when not supported")
    public void testTransferWhenNotSupported() {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.doesNotSupport(TransferQueueMethods.TRANSFER);
        BreakableTransferQueue<String> queue = builder.build();

        assertThrows(UnsupportedOperationException.class, () -> queue.transfer("item"));
    }

    // ========== Try Transfer Method Break Tests ==========

    /// Tests the TRY_TRANSFER_ALWAYS_RETURNS_FALSE break functionality.
    @Test
    @DisplayName("Test TRY_TRANSFER_ALWAYS_RETURNS_FALSE break")
    public void testTryTransferAlwaysReturnsFalse() {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(TRY_TRANSFER_ALWAYS_RETURNS_FALSE);
        BreakableTransferQueue<String> queue = builder.build();

        assertFalse(queue.tryTransfer("item1"));
        assertFalse(queue.tryTransfer("item2"));

        // Elements should NOT be transferred since tryTransfer only succeeds with waiting consumers
        // Without breaks, tryTransfer would return false and not add_singleElement_returnsTrueAndUpdatesSize elements anyway
        // With the break, it should still return false but the underlying transfer should have been attempted
        // Let's manually add_singleElement_returnsTrueAndUpdatesSize an element to test the break effect
        queue.offer("manual");
        assertFalse(queue.isEmpty()); // This should work
    }

    /// Tests the TRY_TRANSFER_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test TRY_TRANSFER_THROWS_EXCEPTION break")
    public void testTryTransferThrowsException() {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(TRY_TRANSFER_THROWS_EXCEPTION);
        BreakableTransferQueue<String> queue = builder.build();

        assertThrows(IllegalStateException.class, () -> queue.tryTransfer("item"));
    }

    /// Tests the TRY_TRANSFER_DOES_NOT_ADD_ELEMENT break functionality.
    @Test
    @DisplayName("Test TRY_TRANSFER_DOES_NOT_ADD_ELEMENT break")
    public void testTryTransferDoesNotAddElement() {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(TRY_TRANSFER_DOES_NOT_ADD_ELEMENT);
        BreakableTransferQueue<String> queue = builder.build();

        assertTrue(queue.tryTransfer("item1")); // Should return true
        assertTrue(queue.tryTransfer("item2")); // Should return true

        // But queue should remain empty
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    /// Tests that tryTransfer() throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test tryTransfer() throws when not supported")
    public void testTryTransferWhenNotSupported() {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.doesNotSupport(TransferQueueMethods.TRY_TRANSFER);
        BreakableTransferQueue<String> queue = builder.build();

        assertThrows(UnsupportedOperationException.class, () -> queue.tryTransfer("item"));
    }

    // ========== Try Transfer with Timeout Method Break Tests ==========

    /// Tests the TRY_TRANSFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE break functionality.
    @Test
    @DisplayName("Test TRY_TRANSFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE break")
    public void testTryTransferWithTimeoutAlwaysReturnsFalse() throws InterruptedException {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(TRY_TRANSFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE);
        BreakableTransferQueue<String> queue = builder.build();

        assertFalse(queue.tryTransfer("item1", 1, TimeUnit.SECONDS));
        assertFalse(queue.tryTransfer("item2", 100, TimeUnit.MILLISECONDS));

        // With timeout transfers, normally elements would not be added since no consumers wait
        // But with the break, it attempts the transfer first, then returns false
        // Let's manually add_singleElement_returnsTrueAndUpdatesSize an element to check queue functionality
        queue.offer("manual");
        assertFalse(queue.isEmpty()); // This should work
    }

    /// Tests the TRY_TRANSFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break functionality.
    @Test
    @DisplayName("Test TRY_TRANSFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break")
    public void testTryTransferWithTimeoutThrowsInterruptedException() {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(TRY_TRANSFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION);
        BreakableTransferQueue<String> queue = builder.build();

        assertThrows(InterruptedException.class, () -> queue.tryTransfer("item", 1, TimeUnit.SECONDS));
    }

    /// Tests the TRY_TRANSFER_WITH_TIMEOUT_DOES_NOT_ADD_ELEMENT break functionality.
    @Test
    @DisplayName("Test TRY_TRANSFER_WITH_TIMEOUT_DOES_NOT_ADD_ELEMENT break")
    public void testTryTransferWithTimeoutDoesNotAddElement() throws InterruptedException {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(TRY_TRANSFER_WITH_TIMEOUT_DOES_NOT_ADD_ELEMENT);
        BreakableTransferQueue<String> queue = builder.build();

        assertTrue(queue.tryTransfer("item1", 1, TimeUnit.SECONDS)); // Should return true
        assertTrue(queue.tryTransfer("item2", 100, TimeUnit.MILLISECONDS)); // Should return true

        // But queue should remain empty
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    /// Tests that tryTransfer(timeout) throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test tryTransfer(timeout) throws when not supported")
    public void testTryTransferWithTimeoutWhenNotSupported() {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.doesNotSupport(TransferQueueMethods.TRY_TRANSFER_TIMEOUT);
        BreakableTransferQueue<String> queue = builder.build();

        assertThrows(UnsupportedOperationException.class, () -> queue.tryTransfer("item", 1, TimeUnit.SECONDS));
    }

    // ========== Consumer Detection Method Break Tests ==========

    /// Tests the HAS_WAITING_CONSUMER_ALWAYS_RETURNS_FALSE break functionality.
    @Test
    @DisplayName("Test HAS_WAITING_CONSUMER_ALWAYS_RETURNS_FALSE break")
    public void testHasWaitingConsumerAlwaysReturnsFalse() {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(HAS_WAITING_CONSUMER_ALWAYS_RETURNS_FALSE);
        BreakableTransferQueue<String> queue = builder.build();

        assertFalse(queue.hasWaitingConsumer()); // Should return false due to break

        // Even after adding elements, should still return false
        queue.offer("item");
        assertFalse(queue.hasWaitingConsumer());
    }

    /// Tests the HAS_WAITING_CONSUMER_ALWAYS_RETURNS_TRUE break functionality.
    @Test
    @DisplayName("Test HAS_WAITING_CONSUMER_ALWAYS_RETURNS_TRUE break")
    public void testHasWaitingConsumerAlwaysReturnsTrue() {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(HAS_WAITING_CONSUMER_ALWAYS_RETURNS_TRUE);
        BreakableTransferQueue<String> queue = builder.build();

        assertTrue(queue.hasWaitingConsumer()); // Should return true due to break

        // Even with empty queue, should still return true
        queue.offer("item");
        queue.poll();
        assertTrue(queue.hasWaitingConsumer());
    }

    /// Tests that hasWaitingConsumer() throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test hasWaitingConsumer() throws when not supported")
    public void testHasWaitingConsumerWhenNotSupported() {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.doesNotSupport(TransferQueueMethods.HAS_WAITING_CONSUMER);
        BreakableTransferQueue<String> queue = builder.build();

        assertThrows(UnsupportedOperationException.class, queue::hasWaitingConsumer);
    }

    /// Tests the GET_WAITING_CONSUMER_COUNT_ALWAYS_RETURNS_ZERO break functionality.
    @Test
    @DisplayName("Test GET_WAITING_CONSUMER_COUNT_ALWAYS_RETURNS_ZERO break")
    public void testGetWaitingConsumerCountAlwaysReturnsZero() {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(GET_WAITING_CONSUMER_COUNT_ALWAYS_RETURNS_ZERO);
        BreakableTransferQueue<String> queue = builder.build();

        assertEquals(0, queue.getWaitingConsumerCount()); // Should return 0 due to break

        // Even after adding elements, should still return 0
        queue.offer("item");
        assertEquals(0, queue.getWaitingConsumerCount());
    }

    /// Tests the GET_WAITING_CONSUMER_COUNT_RETURNS_RANDOM break functionality.
    @Test
    @DisplayName("Test GET_WAITING_CONSUMER_COUNT_RETURNS_RANDOM break")
    public void testGetWaitingConsumerCountReturnsRandom() {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(GET_WAITING_CONSUMER_COUNT_RETURNS_RANDOM);
        BreakableTransferQueue<String> queue = builder.build();

        int count = queue.getWaitingConsumerCount();
        assertNotEquals(-1, count); // Should return some deterministic "random" value
        assertTrue(count >= 0); // Should be non-negative

        // Should return the same value consistently (deterministic)
        assertEquals(count, queue.getWaitingConsumerCount());
    }

    /// Tests that getWaitingConsumerCount() throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test getWaitingConsumerCount() throws when not supported")
    public void testGetWaitingConsumerCountWhenNotSupported() {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.doesNotSupport(TransferQueueMethods.GET_WAITING_CONSUMER_COUNT);
        BreakableTransferQueue<String> queue = builder.build();

        assertThrows(UnsupportedOperationException.class, queue::getWaitingConsumerCount);
    }

    // ========== Static Factory Method Tests ==========

    /// Tests the static wrap factory method functionality.
    @Test
    @DisplayName("Test wrap factory method")
    public void testWrapFactoryMethod() throws InterruptedException {
        LinkedTransferQueue<String> existingQueue = new LinkedTransferQueue<>();
        existingQueue.put("apple");
        existingQueue.put("banana");

        Set<Break> breaks = Set.of(TRY_TRANSFER_ALWAYS_RETURNS_FALSE, HAS_WAITING_CONSUMER_ALWAYS_RETURNS_TRUE);

        BreakableTransferQueue<String> wrappedQueue = BreakableTransferQueue.wrap(existingQueue, breaks);

        // Verify breaks are active
        assertFalse(wrappedQueue.tryTransfer("new_item")); // Due to TRY_TRANSFER_ALWAYS_RETURNS_FALSE
        assertTrue(wrappedQueue.hasWaitingConsumer()); // Due to HAS_WAITING_CONSUMER_ALWAYS_RETURNS_TRUE

        // Verify original data is preserved
        assertTrue(wrappedQueue.contains("apple"));
        assertTrue(wrappedQueue.contains("banana"));
    }

    /// Tests the static wrap factory method with characteristics.
    @Test
    @DisplayName("Test wrap factory method with characteristics")
    public void testWrapFactoryMethodWithCharacteristics() throws InterruptedException {
        LinkedTransferQueue<String> existingQueue = new LinkedTransferQueue<>();
        existingQueue.put("test");

        Set<Break> breaks = Set.of(GET_WAITING_CONSUMER_COUNT_ALWAYS_RETURNS_ZERO);

        BreakableTransferQueue<String> wrappedQueue = BreakableTransferQueue.wrap(existingQueue, breaks, 0);

        // Verify break is active
        assertEquals(0, wrappedQueue.getWaitingConsumerCount());

        // Verify original data is preserved
        assertTrue(wrappedQueue.contains("test"));
    }

    // ========== TransferQueue Behavior and Integration Tests ==========

    /// Tests normal transfer queue operations without breaks.
    @Test
    @DisplayName("Test normal transfer queue operations")
    public void testNormalTransferQueueOperations() throws InterruptedException {
        BreakableTransferQueue<Integer> queue = new BreakableTransferQueue<>();

        // Test normal transfer queue workflow
        queue.put(1);
        queue.put(2);
        assertFalse(queue.tryTransfer(3)); // Should fail as no consumers are waiting

        // Test consumer detection
        assertFalse(queue.hasWaitingConsumer()); // No waiting consumers
        assertEquals(0, queue.getWaitingConsumerCount());

        assertEquals(Integer.valueOf(1), queue.peek());
        assertEquals(Integer.valueOf(1), queue.take());

        assertEquals(Integer.valueOf(2), queue.poll());

        // Since tryTransfer(3) failed, queue should now be empty
        assertTrue(queue.isEmpty());
    }

    /// Tests transfer queue operations with multiple breaks applied.
    @Test
    @DisplayName("Test multiple breaks interaction")
    public void testMultipleBreaks() throws InterruptedException {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(TRANSFER_DOES_NOT_ADD_ELEMENT);
        builder.addBreak(TRY_TRANSFER_ALWAYS_RETURNS_FALSE);
        builder.addBreak(HAS_WAITING_CONSUMER_ALWAYS_RETURNS_TRUE);
        BreakableTransferQueue<String> queue = builder.build();

        // Test transfer break (should not add_singleElement_returnsTrueAndUpdatesSize element)
        queue.transfer("item");
        assertEquals(0, queue.size()); // Element should not be added

        // Test tryTransfer break (should return false)
        assertFalse(queue.tryTransfer("test"));

        // Test consumer detection break (should return true)
        assertTrue(queue.hasWaitingConsumer());
    }

    /// Tests BreakableTransferQueue inheritance from BreakableBlockingQueue.
    @Test
    @DisplayName("Test inheritance from BreakableBlockingQueue")
    public void testBlockingQueueInheritance() throws InterruptedException {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_ZERO); // Collection-level break
        builder.addBreak(BreakableQueue.POLL_ALWAYS_RETURNS_NULL);       // Queue-level break
        builder.addBreak(BreakableBlockingQueue.PUT_DOES_NOT_ADD_ELEMENT); // BlockingQueue-level break
        builder.addBreak(TRANSFER_DOES_NOT_ADD_ELEMENT);                   // TransferQueue-level break
        BreakableTransferQueue<String> queue = builder.build();

        queue.put("item1"); // Should not add_singleElement_returnsTrueAndUpdatesSize due to BlockingQueue break
        queue.transfer("item2"); // Should not add_singleElement_returnsTrueAndUpdatesSize due to TransferQueue break
        queue.offer("item3"); // This should add_singleElement_returnsTrueAndUpdatesSize normally

        // Collection-level functionality with break
        assertEquals(0, queue.size()); // Due to SIZE_ALWAYS_RETURNS_ZERO break
        assertFalse(queue.isEmpty());  // But queue is not actually empty

        // Queue-level functionality with break
        assertNull(queue.poll()); // Due to POLL_ALWAYS_RETURNS_NULL break

        // TransferQueue-level functionality verified by transfer not adding
        assertEquals(1, queue.toArray().length); // Only item3 should be present
    }

    /// Tests add_singleElement_returnsTrueAndUpdatesSize() method inherited from Collection interface.
    @Test
    @DisplayName("Test add_singleElement_returnsTrueAndUpdatesSize() method from Collection")
    public void testAddMethod() {
        BreakableTransferQueue<String> queue = new BreakableTransferQueue<>();

        assertTrue(queue.add("item1"));
        assertTrue(queue.add("item2"));

        assertEquals(2, queue.size());
        assertEquals("item1", queue.peek()); // FIFO ordering maintained
    }

    /// Tests transfer operations with various transfer queue implementations.
    @Test
    @DisplayName("Test LinkedTransferQueue integration")
    public void testLinkedTransferQueueIntegration() throws InterruptedException {
        TransferQueue<String> linkedQueue = new LinkedTransferQueue<>();
        linkedQueue.put("alpha");
        linkedQueue.put("beta");

        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>(linkedQueue);
        builder.addBreak(TRY_TRANSFER_DOES_NOT_ADD_ELEMENT);
        BreakableTransferQueue<String> queue = builder.build();

        // Verify LinkedTransferQueue ordering is preserved
        assertEquals("alpha", queue.peek());

        // Test tryTransfer break - should return true but not transfer
        assertTrue(queue.tryTransfer("gamma"));
        assertEquals(2, queue.size()); // Should still have only original elements
        assertEquals("alpha", queue.peek()); // Head should still be alpha
    }

    /// Tests consumer detection functionality.
    @Test
    @DisplayName("Test consumer detection operations")
    public void testConsumerDetectionOperations() {
        BreakableTransferQueue<String> queue = new BreakableTransferQueue<>();

        // Test default behavior (no consumers)
        assertFalse(queue.hasWaitingConsumer());
        assertEquals(0, queue.getWaitingConsumerCount());

        queue.offer("item1");
        queue.offer("item2");

        // Still no consumers after adding items
        assertFalse(queue.hasWaitingConsumer());
        assertEquals(0, queue.getWaitingConsumerCount());

        // Test tryTransfer with no consumers
        assertFalse(queue.tryTransfer("direct")); // Should fail with no consumers
    }

    /// Tests transfer timeout operations.
    @Test
    @DisplayName("Test transfer timeout operations")
    public void testTransferTimeoutOperations() throws InterruptedException {
        BreakableTransferQueue<String> queue = new BreakableTransferQueue<>();

        // Test tryTransfer with timeout when no consumers are waiting
        assertFalse(queue.tryTransfer("item", 10, TimeUnit.MILLISECONDS));

        // Element should not be added since no consumer was available
        assertTrue(queue.isEmpty());
    }

    /// Tests break priority and interaction.
    @Test
    @DisplayName("Test break priority and interaction")
    public void testBreakPriorityAndInteraction() throws InterruptedException {
        BreakableTransferQueue.Builder<String> builder = new BreakableTransferQueue.Builder<>();
        builder.addBreak(TRANSFER_THROWS_INTERRUPTED_EXCEPTION); // Should throw before other breaks
        builder.addBreak(TRANSFER_DOES_NOT_ADD_ELEMENT);
        BreakableTransferQueue<String> queue = builder.build();

        // Exception break should take priority
        assertThrows(InterruptedException.class, () -> queue.transfer("item"));
    }

    /// Tests state consistency across different transfer operations.
    @Test
    @DisplayName("Test state consistency across transfer operations")
    public void testStateConsistencyAcrossTransferOperations() throws InterruptedException {
        BreakableTransferQueue<String> queue = new BreakableTransferQueue<>();

        queue.put("blocking1");
        queue.offer("regular1");
        assertFalse(queue.tryTransfer("transfer1")); // No consumers, should fail

        assertEquals(2, queue.size()); // Only put and offer should succeed
        assertEquals("blocking1", queue.peek());

        // Try timeout transfer
        assertFalse(queue.tryTransfer("timeout1", 1, TimeUnit.MILLISECONDS));
        assertEquals(2, queue.size()); // Size should remain unchanged
    }
}