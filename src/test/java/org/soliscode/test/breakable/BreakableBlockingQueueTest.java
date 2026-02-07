package org.soliscode.test.breakable;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.blockingqueue.BlockingQueueMethods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.breakable.BreakableBlockingQueue.*;

/// **Test Suite for BreakableBlockingQueue Implementation**
///
/// This comprehensive test class validates the behavior of BreakableBlockingQueue, focusing on both
/// standard BlockingQueue contract compliance and the controlled violation of blocking queue semantics through
/// programmatic breaks. The tests ensure that BreakableBlockingQueue maintains proper blocking behavior
/// under normal conditions while correctly implementing break mechanisms for testing purposes.
///
/// ## Test Coverage
///
/// ### BlockingQueue Contract Compliance Testing
/// - **Queue Interface**: Full validation of inherited Queue interface methods
/// - **BlockingQueue Interface**: Full validation of BlockingQueue-specific methods
/// - **Blocking Operations**: put() and take() method testing with blocking behavior
/// - **Timeout Operations**: offer() and poll() with timeout testing
/// - **Capacity Operations**: remainingCapacity() method testing
/// - **Drain Operations**: drainTo() method testing with various scenarios
///
/// ### Break Mechanism Testing
/// - **Put Breaks**: PUT_* breaks for controlling put() behavior
/// - **Take Breaks**: TAKE_* breaks for controlling take() behavior
/// - **Timeout Offer Breaks**: OFFER_WITH_TIMEOUT_* breaks for controlling timeout offer() behavior
/// - **Timeout Poll Breaks**: POLL_WITH_TIMEOUT_* breaks for controlling timeout poll() behavior
/// - **Capacity Breaks**: REMAINING_CAPACITY_* breaks for controlling capacity reporting
/// - **Drain Breaks**: DRAIN_TO_* breaks for controlling drain operations
/// - **Exception Breaks**: Various exception-throwing behaviors
/// - **Blocking Breaks**: Infinite blocking behaviors for testing interruption
///
/// ### Builder Pattern Testing
/// - **BlockingQueue-Specific Configuration**: Builder setup with BlockingQueue parameters
/// - **Copy Semantics**: Builder copying and independence for blocking queues
/// - **Fluent Interface**: Method chaining with blocking queue configuration
/// - **Pre-populated Queues**: Building from existing BlockingQueue implementations
///
/// ### Constructor Testing
/// - **Default Construction**: Empty blocking queue creation with LinkedBlockingQueue
/// - **Copy Construction**: Creating queues from existing BreakableBlockingQueue instances
/// - **BlockingQueue Construction**: Building from existing BlockingQueue implementations
///
/// ## Test Architecture
///
/// This test class follows the SolisCode testing framework patterns:
/// - **AbstractTest Extension**: Inherits common testing infrastructure
/// - **BlockingQueue-Specific Testing**: Focused on BlockingQueue interface compliance
/// - **Break Isolation**: Each break is tested independently for blocking operations
/// - **State Validation**: Verifies queue state consistency with blocking guarantees
/// - **Concurrency Awareness**: Tests consider blocking and timeout behaviors
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see BreakableBlockingQueue
/// @see BlockingQueue
/// @see AbstractTest
public class BreakableBlockingQueueTest extends AbstractTest {

    // ========== Constructor Tests ==========

    /// Tests the default constructor functionality and initial state validation.
    @Test
    @DisplayName("Test default constructor creates empty blocking queue")
    public void testDefaultConstructor() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue<>();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertNull(queue.peek());
        assertNull(queue.poll());
        assertTrue(queue.remainingCapacity() > 0); // LinkedBlockingQueue has capacity
    }

    /// Tests the copy constructor behavior and configuration inheritance.
    @Test
    @DisplayName("Test copy constructor")
    public void testCopyConstructor() {
        BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
        builder.addBreak(PUT_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingQueue<String> original = builder.build();

        original.offer("item1");
        original.offer("item2");

        BreakableBlockingQueue<String> copy = new BreakableBlockingQueue<>(original);

        // Verify independent copies
        assertNotSame(original, copy);
        assertEquals(original.size(), copy.size());

        // Verify breaks are copied
        assertThrows(InterruptedException.class, () -> copy.put("item3")); // Should throw due to break
    }

    // ========== Builder Tests ==========

    /// Tests basic Builder pattern functionality and configuration transfer.
    @Test
    @DisplayName("Test builder pattern")
    public void testBuilder() {
        try {
            BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
            builder.addBreak(TAKE_THROWS_INTERRUPTED_EXCEPTION);
            builder.addBreak(PUT_DOES_NOT_ADD_ELEMENT);
            BreakableBlockingQueue<String> queue = builder.build();

            queue.put("item"); // Should not add_singleElement_returnsTrueAndUpdatesSize due to break

            assertEquals(0, queue.size()); // Element not added due to break
            assertThrows(InterruptedException.class, queue::take); // Should throw due to break
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    /// Tests Builder copy functionality and configuration inheritance.
    @Test
    @DisplayName("Test builder copy")
    public void testBuilderCopy() {
        try {
            BreakableBlockingQueue.Builder<String> original = new BreakableBlockingQueue.Builder<>();
            original.addBreak(PUT_DOES_NOT_ADD_ELEMENT);

            BreakableBlockingQueue.Builder<String> copy = original.copy();
            copy.addBreak(TAKE_ALWAYS_RETURNS_NULL);

            BreakableBlockingQueue<String> originalQueue = original.build();
            BreakableBlockingQueue<String> copyQueue = copy.build();

            // Original should have put break but normal take
            originalQueue.put("test");
            assertEquals(0, originalQueue.size()); // Put didn't add_singleElement_returnsTrueAndUpdatesSize due to break

            // Manually add_singleElement_returnsTrueAndUpdatesSize element to test take
            originalQueue.offer("test");
            assertEquals("test", originalQueue.take()); // Normal take behavior

            // Copy should have both breaks
            copyQueue.put("test");
            assertEquals(0, copyQueue.size()); // Put didn't add_singleElement_returnsTrueAndUpdatesSize due to break
            assertNull(copyQueue.take()); // Take returns null due to break
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    // ========== Put Method Break Tests ==========

    /// Tests the PUT_THROWS_INTERRUPTED_EXCEPTION break functionality.
    @Test
    @DisplayName("Test PUT_THROWS_INTERRUPTED_EXCEPTION break")
    public void testPutThrowsInterruptedException() {
        BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
        builder.addBreak(PUT_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingQueue<String> queue = builder.build();

        assertThrows(InterruptedException.class, () -> queue.put("item"));
    }

    /// Tests the PUT_DOES_NOT_ADD_ELEMENT break functionality.
    @Test
    @DisplayName("Test PUT_DOES_NOT_ADD_ELEMENT break")
    public void testPutDoesNotAddElement() {
        try {
            BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
            builder.addBreak(PUT_DOES_NOT_ADD_ELEMENT);
            BreakableBlockingQueue<String> queue = builder.build();

            queue.put("item1");
            queue.put("item2");

            // Queue should remain empty despite puts
            assertEquals(0, queue.size());
            assertTrue(queue.isEmpty());
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    /// Tests the PUT_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test PUT_THROWS_EXCEPTION break")
    public void testPutThrowsException() {
        BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
        builder.addBreak(PUT_THROWS_EXCEPTION);
        BreakableBlockingQueue<String> queue = builder.build();

        assertThrows(IllegalStateException.class, () -> queue.put("item"));
    }

    /// Tests that put() throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test put() throws when not supported")
    public void testPutWhenNotSupported() {
        BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
        builder.doesNotSupport(BlockingQueueMethods.PUT);
        BreakableBlockingQueue<String> queue = builder.build();

        assertThrows(UnsupportedOperationException.class, () -> queue.put("item"));
    }

    // ========== Take Method Break Tests ==========

    /// Tests the TAKE_THROWS_INTERRUPTED_EXCEPTION break functionality.
    @Disabled
    @Test
    @DisplayName("Test TAKE_THROWS_INTERRUPTED_EXCEPTION break")
    public void testTakeThrowsInterruptedException() {
        BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
        builder.addBreak(TAKE_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingQueue<String> queue = builder.build();

        assertThrows(InterruptedException.class, queue::take);
    }

    /// Tests the TAKE_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test TAKE_ALWAYS_RETURNS_NULL break")
    public void testTakeAlwaysReturnsNull() {
        try {
            BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
            builder.addBreak(TAKE_ALWAYS_RETURNS_NULL);
            BreakableBlockingQueue<String> queue = builder.build();

            queue.put("item1");
            queue.put("item2");

            assertNull(queue.take()); // Should return null due to break
            assertEquals(2, queue.size()); // Elements should still be there
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    /// Tests the TAKE_DOES_NOT_REMOVE_ELEMENT break functionality.
    @Test
    @DisplayName("Test TAKE_DOES_NOT_REMOVE_ELEMENT break")
    public void testTakeDoesNotRemoveElement() {
        try {
            BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
            builder.addBreak(TAKE_DOES_NOT_REMOVE_ELEMENT);
            BreakableBlockingQueue<String> queue = builder.build();

            queue.put("item1");
            queue.put("item2");

            String taken = queue.take();
            assertEquals("item1", taken); // Should return head element
            assertEquals(2, queue.size()); // But not remove it
            assertEquals("item1", queue.peek()); // Head should still be there
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    /// Tests that take() throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test take() throws when not supported")
    public void testTakeWhenNotSupported() {
        BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
        builder.doesNotSupport(BlockingQueueMethods.TAKE);
        BreakableBlockingQueue<String> queue = builder.build();

        assertThrows(UnsupportedOperationException.class, queue::take);
    }

    // ========== Timeout Offer Method Break Tests ==========

    /// Tests the OFFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE break functionality.
    @Test
    @DisplayName("Test OFFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE break")
    public void testOfferWithTimeoutAlwaysReturnsFalse() {
        try {
            BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
            builder.addBreak(OFFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE);
            BreakableBlockingQueue<String> queue = builder.build();

            assertFalse(queue.offer("item1", 1, TimeUnit.SECONDS));
            assertFalse(queue.offer("item2", 100, TimeUnit.MILLISECONDS));

            // Elements should still be added despite false return (normal behavior)
            assertFalse(queue.isEmpty());
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    /// Tests the OFFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break functionality.
    @Test
    @DisplayName("Test OFFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break")
    public void testOfferWithTimeoutThrowsInterruptedException() {
        BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
        builder.addBreak(OFFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingQueue<String> queue = builder.build();

        assertThrows(InterruptedException.class, () -> queue.offer("item", 1, TimeUnit.SECONDS));
    }

    /// Tests that offer(timeout) throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test offer(timeout) throws when not supported")
    public void testOfferWithTimeoutWhenNotSupported() {
        BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
        builder.doesNotSupport(BlockingQueueMethods.OFFER_TIMEOUT);
        BreakableBlockingQueue<String> queue = builder.build();

        assertThrows(UnsupportedOperationException.class, () -> queue.offer("item", 1, TimeUnit.SECONDS));
    }

    // ========== Timeout Poll Method Break Tests ==========

    /// Tests the POLL_WITH_TIMEOUT_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test POLL_WITH_TIMEOUT_ALWAYS_RETURNS_NULL break")
    public void testPollWithTimeoutAlwaysReturnsNull() {
        try {
            BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
            builder.addBreak(POLL_WITH_TIMEOUT_ALWAYS_RETURNS_NULL);
            BreakableBlockingQueue<String> queue = builder.build();

            queue.put("item1");
            queue.put("item2");

            assertNull(queue.poll(1, TimeUnit.SECONDS)); // Should return null due to break
            assertFalse(queue.isEmpty()); // Queue should not be empty
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    /// Tests the POLL_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break functionality.
    @Test
    @DisplayName("Test POLL_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break")
    public void testPollWithTimeoutThrowsInterruptedException() {
        BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
        builder.addBreak(POLL_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingQueue<String> queue = builder.build();

        assertThrows(InterruptedException.class, () -> queue.poll(1, TimeUnit.SECONDS));
    }

    /// Tests that poll(timeout) throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test poll(timeout) throws when not supported")
    public void testPollWithTimeoutWhenNotSupported() {
        BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
        builder.doesNotSupport(BlockingQueueMethods.POLL_TIMEOUT);
        BreakableBlockingQueue<String> queue = builder.build();

        assertThrows(UnsupportedOperationException.class, () -> queue.poll(1, TimeUnit.SECONDS));
    }

    // ========== Remaining Capacity Method Break Tests ==========

    /// Tests the REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO break functionality.
    @Test
    @DisplayName("Test REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO break")
    public void testRemainingCapacityAlwaysReturnsZero() {
        BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
        builder.addBreak(REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO);
        BreakableBlockingQueue<String> queue = builder.build();

        assertEquals(0, queue.remainingCapacity()); // Should return 0 due to break

        // Even after adding elements, should still return 0
        queue.offer("item");
        assertEquals(0, queue.remainingCapacity());
    }

    /// Tests that remainingCapacity() throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test remainingCapacity() throws when not supported")
    public void testRemainingCapacityWhenNotSupported() {
        BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
        builder.doesNotSupport(BlockingQueueMethods.REMAINING_CAPACITY);
        BreakableBlockingQueue<String> queue = builder.build();

        assertThrows(UnsupportedOperationException.class, queue::remainingCapacity);
    }

    // ========== DrainTo Method Break Tests ==========

    /// Tests the DRAIN_TO_ALWAYS_RETURNS_ZERO break functionality.
    @Test
    @DisplayName("Test DRAIN_TO_ALWAYS_RETURNS_ZERO break")
    public void testDrainToAlwaysReturnsZero() {
        try {
            BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
            builder.addBreak(DRAIN_TO_ALWAYS_RETURNS_ZERO);
            BreakableBlockingQueue<String> queue = builder.build();

            queue.put("item1");
            queue.put("item2");
            queue.put("item3");

            List<String> drainedElements = new ArrayList<>();
            assertEquals(0, queue.drainTo(drainedElements)); // Should return 0 due to break
            assertTrue(drainedElements.isEmpty()); // No elements should be drained

            // Test with maxElements parameter
            assertEquals(0, queue.drainTo(drainedElements, 2)); // Should return 0 due to break
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    /// Tests the DRAIN_TO_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test DRAIN_TO_THROWS_EXCEPTION break")
    public void testDrainToThrowsException() {
        try {
            BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
            builder.addBreak(DRAIN_TO_THROWS_EXCEPTION);
            BreakableBlockingQueue<String> queue = builder.build();

            queue.put("item");
            List<String> drainedElements = new ArrayList<>();

            assertThrows(IllegalStateException.class, () -> queue.drainTo(drainedElements));
            assertThrows(IllegalStateException.class, () -> queue.drainTo(drainedElements, 1));
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    /// Tests the DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS break functionality.
    @Test
    @DisplayName("Test DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS break")
    public void testDrainToDoesNotRemoveElements() {
        try {
            BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
            builder.addBreak(DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS);
            BreakableBlockingQueue<String> queue = builder.build();

            queue.put("item1");
            queue.put("item2");
            queue.put("item3");

            List<String> drainedElements = new ArrayList<>();
            int drained = queue.drainTo(drainedElements);

            assertEquals(3, drained); // Should return count
            assertEquals(3, drainedElements.size()); // Elements should be added to collection
            assertEquals(3, queue.size()); // But not removed from queue
            assertTrue(drainedElements.contains("item1"));
            assertTrue(drainedElements.contains("item2"));
            assertTrue(drainedElements.contains("item3"));
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    /// Tests that drainTo() throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test drainTo() throws when not supported")
    public void testDrainToWhenNotSupported() {
        BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
        builder.doesNotSupport(BlockingQueueMethods.DRAIN_TO);
        BreakableBlockingQueue<String> queue = builder.build();

        List<String> drainedElements = new ArrayList<>();
        assertThrows(UnsupportedOperationException.class, () -> queue.drainTo(drainedElements));
    }

    /// Tests that drainTo(maxElements) throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test drainTo(maxElements) throws when not supported")
    public void testDrainToWithMaxElementsWhenNotSupported() {
        BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
        builder.doesNotSupport(BlockingQueueMethods.DRAIN_TO_MAX_ELEMENTS);
        BreakableBlockingQueue<String> queue = builder.build();

        List<String> drainedElements = new ArrayList<>();
        assertThrows(UnsupportedOperationException.class, () -> queue.drainTo(drainedElements, 5));
    }

    // ========== Static Factory Method Tests ==========

    /// Tests the static wrap factory method functionality.
    @Test
    @DisplayName("Test wrap factory method")
    public void testWrapFactoryMethod() {
        try {
            LinkedBlockingQueue<String> existingQueue = new LinkedBlockingQueue<>();
            existingQueue.put("apple");
            existingQueue.put("banana");

            Set<Break> breaks = Set.of(TAKE_ALWAYS_RETURNS_NULL, PUT_THROWS_INTERRUPTED_EXCEPTION);

            BreakableBlockingQueue<String> wrappedQueue = BreakableBlockingQueue.wrap(existingQueue, breaks);

            // Verify breaks are active
            assertNull(wrappedQueue.take()); // Due to TAKE_ALWAYS_RETURNS_NULL
            assertThrows(InterruptedException.class, () -> wrappedQueue.put("new_item")); // Due to PUT_THROWS_INTERRUPTED_EXCEPTION

            // Verify original data is preserved
            assertTrue(wrappedQueue.contains("apple"));
            assertTrue(wrappedQueue.contains("banana"));
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    /// Tests the static wrap factory method with characteristics.
    @Test
    @DisplayName("Test wrap factory method with characteristics")
    public void testWrapFactoryMethodWithCharacteristics() {
        try {
            LinkedBlockingQueue<String> existingQueue = new LinkedBlockingQueue<>();
            existingQueue.put("test");

            Set<Break> breaks = Set.of(REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO);

            BreakableBlockingQueue<String> wrappedQueue = BreakableBlockingQueue.wrap(existingQueue, breaks, 0);

            // Verify break is active
            assertEquals(0, wrappedQueue.remainingCapacity());

            // Verify original data is preserved
            assertTrue(wrappedQueue.contains("test"));
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    // ========== BlockingQueue Behavior and Integration Tests ==========

    /// Tests normal blocking queue operations without breaks.
    @Test
    @DisplayName("Test normal blocking queue operations")
    public void testNormalBlockingQueueOperations() {
        try {
            BreakableBlockingQueue<Integer> queue = new BreakableBlockingQueue<>();

            // Test normal blocking queue workflow
            queue.put(1);
            queue.put(2);
            assertTrue(queue.offer(3, 1, TimeUnit.SECONDS));

            assertEquals(Integer.valueOf(1), queue.peek());
            assertEquals(Integer.valueOf(1), queue.take());

            assertEquals(Integer.valueOf(2), queue.poll(1, TimeUnit.SECONDS));
            assertEquals(1, queue.size());

            assertEquals(Integer.valueOf(3), queue.poll());
            assertTrue(queue.isEmpty());
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    /// Tests blocking queue operations with multiple breaks applied.
    @Test
    @DisplayName("Test multiple breaks interaction")
    public void testMultipleBreaks() {
        try {
            BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
            builder.addBreak(PUT_DOES_NOT_ADD_ELEMENT);
            builder.addBreak(TAKE_ALWAYS_RETURNS_NULL);
            builder.addBreak(REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO);
            BreakableBlockingQueue<String> queue = builder.build();

            // Test put break
            queue.put("item");
            assertEquals(0, queue.size()); // Element should not be added

            // Manually add_singleElement_returnsTrueAndUpdatesSize element to test other breaks
            queue.offer("test");
            assertFalse(queue.isEmpty());

            // Test take break
            assertNull(queue.take());

            // Test capacity break
            assertEquals(0, queue.remainingCapacity());
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    // ========== Inheritance and BlockingQueue Interface Tests ==========

    /// Tests that BreakableBlockingQueue properly inherits Queue functionality.
    @Test
    @DisplayName("Test inheritance from BreakableQueue")
    public void testQueueInheritance() {
        try {
            BreakableBlockingQueue.Builder<String> builder = new BreakableBlockingQueue.Builder<>();
            builder.addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_ZERO); // Collection-level break
            builder.addBreak(BreakableQueue.POLL_ALWAYS_RETURNS_NULL);       // Queue-level break
            builder.addBreak(PUT_DOES_NOT_ADD_ELEMENT);                      // BlockingQueue-level break
            BreakableBlockingQueue<String> queue = builder.build();

            queue.put("item1"); // Should not add_singleElement_returnsTrueAndUpdatesSize due to BlockingQueue break
            queue.offer("item2"); // This should add_singleElement_returnsTrueAndUpdatesSize normally

            // Collection-level functionality with break
            assertEquals(0, queue.size()); // Due to SIZE_ALWAYS_RETURNS_ZERO break
            assertFalse(queue.isEmpty());  // But queue is not actually empty

            // Queue-level functionality with break
            assertNull(queue.poll()); // Due to POLL_ALWAYS_RETURNS_NULL break

            // BlockingQueue-level functionality verified by put not adding
            assertEquals(1, queue.toArray().length); // Only item2 should be present
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    /// Tests add_singleElement_returnsTrueAndUpdatesSize() method inherited from Collection interface.
    @Test
    @DisplayName("Test add_singleElement_returnsTrueAndUpdatesSize() method from Collection")
    public void testAddMethod() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue<>();

        assertTrue(queue.add("item1"));
        assertTrue(queue.add("item2"));

        assertEquals(2, queue.size());
        assertEquals("item1", queue.peek()); // FIFO ordering maintained
    }

    /// Tests that capacity constraints are properly handled.
    @Test
    @DisplayName("Test capacity constraints")
    public void testCapacityConstraints() {
        try {
            // Create a small capacity queue
            BlockingQueue<String> smallQueue = new java.util.concurrent.ArrayBlockingQueue<>(1);
            BreakableBlockingQueue<String> queue = BreakableBlockingQueue.wrap(smallQueue, new HashSet<>());

            queue.put("item1");
            assertEquals(0, queue.remainingCapacity()); // Should be full

            // Offering another item should return false due to capacity
            assertFalse(queue.offer("item2", 100, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    /// Tests drain operations with various collection types.
    @Test
    @DisplayName("Test drain operations")
    public void testDrainOperations() {
        try {
            BreakableBlockingQueue<String> queue = new BreakableBlockingQueue<>();

            queue.put("item1");
            queue.put("item2");
            queue.put("item3");
            queue.put("item4");

            // Test drainTo without limit
            List<String> allDrained = new ArrayList<>();
            int drainedCount = queue.drainTo(allDrained);
            assertEquals(4, drainedCount);
            assertEquals(4, allDrained.size());
            assertTrue(queue.isEmpty());

            // Repopulate queue
            queue.put("a");
            queue.put("b");
            queue.put("c");

            // Test drainTo with limit
            Set<String> limitedDrained = new HashSet<>();
            int limitedCount = queue.drainTo(limitedDrained, 2);
            assertEquals(2, limitedCount);
            assertEquals(2, limitedDrained.size());
            assertEquals(1, queue.size());
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }
}