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
    @DisplayName("defaultConstructor: creates empty blocking queue")
    public void defaultConstructor_whenCalled_createsEmptyBlockingQueue() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue<>();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertNull(queue.peek());
        assertNull(queue.poll());
        assertTrue(queue.remainingCapacity() > 0); // LinkedBlockingQueue has capacity
    }

    /// Tests the copy constructor behavior and configuration inheritance.
    @Test
    @DisplayName("copyConstructor: copies elements and configuration")
    public void copyConstructor_whenCalled_copiesElementsAndConfiguration() {
        BreakableBlockingQueue<String> original = new BreakableBlockingQueue.Builder<String>()
                .addBreak(PUT_THROWS_INTERRUPTED_EXCEPTION)
                .build();

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
    @DisplayName("builder: builds configured queue")
    public void builder_withBreaks_buildsConfiguredQueue() {
        try {
            BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                .addBreak(TAKE_THROWS_INTERRUPTED_EXCEPTION)
                .addBreak(PUT_DOES_NOT_ADD_ELEMENT)
                .build();

            queue.put("item"); // Should not add_singleElement_returnsTrueAndUpdatesSize due to break

            assertEquals(0, queue.size()); // Element not added due to break
            assertThrows(InterruptedException.class, queue::take); // Should throw due to break
        } catch (InterruptedException e) {
            fail("An InterruptedException should not be thrown here.", e);
        }
    }

    /// Tests Builder copy functionality and configuration inheritance.
    @Test
    @DisplayName("builder.copy(): produces independent builder with inherited configuration")
    public void builderCopy_whenCalled_producesIndependentBuilderWithInheritedConfiguration() {
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
    @DisplayName("put(E): throws InterruptedException when break active")
    public void put_whenThrowsInterruptedExceptionBreakActive_throwsInterruptedException() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                .addBreak(PUT_THROWS_INTERRUPTED_EXCEPTION)
                .build();

        assertThrows(InterruptedException.class, () -> queue.put("item"));
    }

    /// Tests the PUT_DOES_NOT_ADD_ELEMENT break functionality.
    @Test
    @DisplayName("put(E): does not add element when break active")
    public void put_whenDoesNotAddElementBreakActive_doesNotAddElement() {
        try {
            BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                    .addBreak(PUT_DOES_NOT_ADD_ELEMENT)
                    .build();

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
    @DisplayName("put(E): throws IllegalStateException when break active")
    public void put_whenThrowsExceptionBreakActive_throwsIllegalStateException() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                .addBreak(PUT_THROWS_EXCEPTION)
                .build();

        assertThrows(IllegalStateException.class, () -> queue.put("item"));
    }

    /// Tests that put() throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("put(E): throws UnsupportedOperationException when not supported")
    public void put_whenNotSupported_throwsUnsupportedOperationException() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                .doesNotSupport(BlockingQueueMethods.PUT)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> queue.put("item"));
    }

    // ========== Take Method Break Tests ==========

    /// Tests the TAKE_THROWS_INTERRUPTED_EXCEPTION break functionality.
    @Disabled
    @Test
    @DisplayName("take(): throws InterruptedException when break active")
    public void take_whenThrowsInterruptedExceptionBreakActive_throwsInterruptedException() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                .addBreak(TAKE_THROWS_INTERRUPTED_EXCEPTION)
                .build();

        assertThrows(InterruptedException.class, queue::take);
    }

    /// Tests the TAKE_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("take(): always returns null when break active")
    public void take_whenAlwaysReturnsNullBreakActive_returnsNull() {
        try {
            BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                    .addBreak(TAKE_ALWAYS_RETURNS_NULL)
                    .build();

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
    @DisplayName("take(): does not remove element when break active")
    public void take_whenDoesNotRemoveElementBreakActive_returnsElementWithoutRemoving() {
        try {
            BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                    .addBreak(TAKE_DOES_NOT_REMOVE_ELEMENT)
                    .build();

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
    @DisplayName("take(): throws UnsupportedOperationException when not supported")
    public void take_whenNotSupported_throwsUnsupportedOperationException() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                .doesNotSupport(BlockingQueueMethods.TAKE)
                .build();

        assertThrows(UnsupportedOperationException.class, queue::take);
    }

    // ========== Timeout Offer Method Break Tests ==========

    /// Tests the OFFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE break functionality.
    @Test
    @DisplayName("offer(E, long, TimeUnit): always returns false when break active")
    public void offer_whenTimeoutAlwaysReturnsFalseBreakActive_returnsFalse() {
        try {
            BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                    .addBreak(OFFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE)
                    .build();

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
    @DisplayName("offer(E, long, TimeUnit): throws InterruptedException when break active")
    public void offer_whenTimeoutThrowsInterruptedExceptionBreakActive_throwsInterruptedException() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                .addBreak(OFFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION)
                .build();

        assertThrows(InterruptedException.class, () -> queue.offer("item", 1, TimeUnit.SECONDS));
    }

    /// Tests that offer(timeout) throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("offer(E, long, TimeUnit): throws UnsupportedOperationException when not supported")
    public void offer_whenTimeoutNotSupported_throwsUnsupportedOperationException() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                .doesNotSupport(BlockingQueueMethods.OFFER_TIMEOUT)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> queue.offer("item", 1, TimeUnit.SECONDS));
    }

    // ========== Timeout Poll Method Break Tests ==========

    /// Tests the POLL_WITH_TIMEOUT_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("poll(long, TimeUnit): always returns null when break active")
    public void poll_whenTimeoutAlwaysReturnsNullBreakActive_returnsNull() {
        try {
            BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                    .addBreak(POLL_WITH_TIMEOUT_ALWAYS_RETURNS_NULL)
                    .build();

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
    @DisplayName("poll(long, TimeUnit): throws InterruptedException when break active")
    public void poll_whenTimeoutThrowsInterruptedExceptionBreakActive_throwsInterruptedException() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                .addBreak(POLL_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION)
                .build();

        assertThrows(InterruptedException.class, () -> queue.poll(1, TimeUnit.SECONDS));
    }

    /// Tests that poll(timeout) throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("poll(long, TimeUnit): throws UnsupportedOperationException when not supported")
    public void poll_whenTimeoutNotSupported_throwsUnsupportedOperationException() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
            .doesNotSupport(BlockingQueueMethods.POLL_TIMEOUT)
            .build();

        assertThrows(UnsupportedOperationException.class, () -> queue.poll(1, TimeUnit.SECONDS));
    }

    // ========== Remaining Capacity Method Break Tests ==========

    /// Tests the REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO break functionality.
    /// This test verifies that when the REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO break is applied,
    /// the remainingCapacity method always returns 0, regardless of the number of elements in the queue.
    @Test
    @DisplayName("remainingCapacity(): always returns zero when break active")
    public void remainingCapacity_whenAlwaysReturnsZeroBreakActive_returnsZero() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
            .addBreak(REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO)
            .build();

        assertEquals(0, queue.remainingCapacity()); // Should return 0 due to break

        // Even after adding elements, should still return 0
        queue.offer("item");
        assertEquals(0, queue.remainingCapacity());
    }

    /**
     * Tests that remainingCapacity() throws UnsupportedOperationException when method is not supported.
     *
     * This test verifies that if the remainingCapacity method is not supported, calling it
     * will result in an UnsupportedOperationException being thrown.
     */
    @Test
    @DisplayName("remainingCapacity(): throws UnsupportedOperationException when not supported")
    public void remainingCapacity_whenNotSupported_throwsUnsupportedOperationException() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                .doesNotSupport(BlockingQueueMethods.REMAINING_CAPACITY)
                .build();

        assertThrows(UnsupportedOperationException.class, queue::remainingCapacity);
    }

    // ========== DrainTo Method Break Tests ==========

    /**
     * Tests the DRAIN_TO_ALWAYS_RETURNS_ZERO break functionality.
     *
     * This test verifies that when the DRAIN_TO_ALWAYS_RETURNS_ZERO break is applied,
     * the drainTo method always returns 0, regardless of the number of elements in the queue.
     */
    @Test
    @DisplayName("drainTo(Collection): always returns zero when break active")
    public void drainTo_whenAlwaysReturnsZeroBreakActive_returnsZero() {
        try {
            BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                    .addBreak(DRAIN_TO_ALWAYS_RETURNS_ZERO)
                    .build();

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
    @DisplayName("drainTo(Collection): throws IllegalStateException when break active")
    public void drainTo_whenThrowsExceptionBreakActive_throwsIllegalStateException() {
        try {
            BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                    .addBreak(DRAIN_TO_THROWS_EXCEPTION)
                    .build();

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
    @DisplayName("drainTo(Collection): does not remove elements when break active")
    public void drainTo_whenDoesNotRemoveElementsBreakActive_drainsWithoutRemoving() {
        try {
            BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                    .addBreak(DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS)
                    .build();

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
    @DisplayName("drainTo(Collection): throws UnsupportedOperationException when not supported")
    public void drainTo_whenNotSupported_throwsUnsupportedOperationException() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                .doesNotSupport(BlockingQueueMethods.DRAIN_TO)
                .build();

        List<String> drainedElements = new ArrayList<>();
        assertThrows(UnsupportedOperationException.class, () -> queue.drainTo(drainedElements));
    }

    /// Tests that drainTo(maxElements) throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("drainTo(Collection, int): throws UnsupportedOperationException when not supported")
    public void drainTo_whenMaxElementsNotSupported_throwsUnsupportedOperationException() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                .doesNotSupport(BlockingQueueMethods.DRAIN_TO_MAX_ELEMENTS)
                .build();

        List<String> drainedElements = new ArrayList<>();
        assertThrows(UnsupportedOperationException.class, () -> queue.drainTo(drainedElements, 5));
    }

    // ========== Static Factory Method Tests ==========

    /// Tests the static wrap factory method functionality.
    @Test
    @DisplayName("wrap(): wraps existing blocking queue with breaks")
    public void wrap_whenGivenQueueAndBreaks_wrapsCorrectly() {
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
    @DisplayName("wrap(): wraps existing blocking queue with breaks and characteristics")
    public void wrap_whenGivenQueueBreaksAndCharacteristics_wrapsCorrectly() {
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
    @DisplayName("BlockingQueue: verifies normal blocking operations")
    public void blockingQueue_whenNoBreaks_behavesNormally() {
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
    @DisplayName("BlockingQueue: verifies interaction of multiple breaks")
    public void blockingQueue_withMultipleBreaks_appliesAllBreaks() {
        try {
            BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                    .addBreak(PUT_DOES_NOT_ADD_ELEMENT)
                    .addBreak(TAKE_ALWAYS_RETURNS_NULL)
                    .addBreak(REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO)
                    .build();

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
    @DisplayName("BlockingQueue: verifies inheritance from BreakableQueue and BreakableCollection")
    public void blockingQueue_whenInheritedBreaksActive_appliesInheritedBreaks() {
        try {
            BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
                    .addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_ZERO)  // Collection-level break
                    .addBreak(BreakableQueue.POLL_ALWAYS_RETURNS_NULL)       // Queue-level break
                    .addBreak(PUT_DOES_NOT_ADD_ELEMENT)                      // BlockingQueue-level break
                    .build();

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
    @DisplayName("add(E): adds elements to the queue")
    public void add_whenCalled_addsElements() {
        BreakableBlockingQueue<String> queue = new BreakableBlockingQueue<>();

        assertTrue(queue.add("item1"));
        assertTrue(queue.add("item2"));

        assertEquals(2, queue.size());
        assertEquals("item1", queue.peek()); // FIFO ordering maintained
    }

    /// Tests that capacity constraints are properly handled.
    @Test
    @DisplayName("BlockingQueue: verifies capacity constraints")
    public void blockingQueue_whenAtCapacity_respectsConstraints() {
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
    @DisplayName("drainTo: verifies various drain scenarios")
    public void drainTo_whenCalled_drainsElementsCorrectly() {
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