package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.queue.QueueMethods;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.breakable.BreakableQueue.*;

/// **Test Suite for BreakableQueue Implementation**
///
/// This comprehensive test class validates the behavior of BreakableQueue, focusing on both
/// standard Queue contract compliance and the controlled violation of queue semantics through
/// programmatic breaks. The tests ensure that BreakableQueue maintains proper queue behavior
/// under normal conditions while correctly implementing break mechanisms for testing purposes.
///
/// ## Test Coverage
///
/// ### Queue Contract Compliance Testing
/// - **Collection Interface**: Full validation of inherited Collection interface methods
/// - **Queue Interface**: Full validation of Queue-specific methods
/// - **FIFO Operations**: First-in-first-out behavior validation
/// - **Insertion Operations**: offer() method testing with capacity handling
/// - **Removal Operations**: poll() and remove() method testing
/// - **Examination Operations**: peek() and element() method testing
///
/// ### Break Mechanism Testing
/// - **Insertion Breaks**: OFFER_* breaks for controlling offer() behavior
/// - **Removal Breaks**: POLL_* breaks for controlling poll() behavior
/// - **Examination Breaks**: PEEK_* and ELEMENT_* breaks for controlling queue inspection
/// - **Exception Breaks**: Various exception-throwing behaviors
/// - **Return Value Breaks**: Null returns and incorrect return values
///
/// ### Builder Pattern Testing
/// - **Queue-Specific Configuration**: Builder setup with Queue parameters
/// - **Copy Semantics**: Builder copying and independence for queues
/// - **Fluent Interface**: Method chaining with queue configuration
/// - **Pre-populated Queues**: Building from existing Queue implementations
///
/// ### Constructor Testing
/// - **Default Construction**: Empty queue creation with FIFO ordering
/// - **Copy Construction**: Creating queues from existing BreakableQueue instances
/// - **Queue Construction**: Building from existing Queue implementations
///
/// ## Test Architecture
///
/// This test class follows the SolisCode testing framework patterns:
/// - **AbstractTest Extension**: Inherits common testing infrastructure
/// - **Queue-Specific Testing**: Focused on Queue interface compliance
/// - **Break Isolation**: Each break is tested independently for queue operations
/// - **State Validation**: Verifies queue state consistency with FIFO guarantees
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see BreakableQueue
/// @see Queue
/// @see AbstractTest
public class BreakableQueueTest extends AbstractTest {

    // ========== Constructor Tests ==========

    /// Tests the default constructor functionality and initial state validation.
    @Test
    @DisplayName("Test default constructor creates empty queue")
    public void testDefaultConstructor() {
        BreakableQueue<String> queue = new BreakableQueue<>();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertNull(queue.peek());
        assertNull(queue.poll());
    }

    /// Tests the copy constructor behavior and configuration inheritance.
    @Test
    @DisplayName("Test copy constructor")
    public void testCopyConstructor() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(OFFER_ALWAYS_RETURNS_FALSE);
        BreakableQueue<String> original = builder.build();

        original.offer("item1");
        original.offer("item2");

        BreakableQueue<String> copy = new BreakableQueue<>(original);

        // Verify independent copies
        assertNotSame(original, copy);
        assertEquals(original.size(), copy.size());

        // Verify breaks are copied
        assertFalse(copy.offer("item3")); // Should return false due to break
    }

    // ========== Builder Tests ==========

    /// Tests basic Builder pattern functionality and configuration transfer.
    @Test
    @DisplayName("Test builder pattern")
    public void testBuilder() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(POLL_ALWAYS_RETURNS_NULL);
        builder.addBreak(PEEK_THROWS_EXCEPTION);
        BreakableQueue<String> queue = builder.build();

        queue.offer("item");

        // Verify breaks are active
        assertNull(queue.poll()); // Due to break
        assertThrows(NoSuchElementException.class, queue::peek); // Due to break
    }

    /// Tests Builder copy functionality and configuration inheritance.
    @Test
    @DisplayName("Test builder copy")
    public void testBuilderCopy() {
        BreakableQueue.Builder<String> original = new BreakableQueue.Builder<>();
        original.addBreak(OFFER_DOES_NOT_ADD_ELEMENT);

        BreakableQueue.Builder<String> copy = original.copy();
        copy.addBreak(POLL_THROWS_EXCEPTION);

        BreakableQueue<String> originalQueue = original.build();
        BreakableQueue<String> copyQueue = copy.build();

        // Original should have offer break but normal poll
        originalQueue.offer("test");
        assertEquals(0, originalQueue.size()); // Offer didn't add_singleElement_returnsTrueAndUpdatesSize due to break
        assertNull(originalQueue.poll()); // Normal poll behavior

        // Copy should have both breaks
        copyQueue.offer("test");
        assertEquals(0, copyQueue.size()); // Offer didn't add_singleElement_returnsTrueAndUpdatesSize due to break
        assertThrows(NoSuchElementException.class, copyQueue::poll); // Poll throws due to break
    }

    // ========== Offer Method Break Tests ==========

    /// Tests the OFFER_ALWAYS_RETURNS_FALSE break functionality.
    @Test
    @DisplayName("Test OFFER_ALWAYS_RETURNS_FALSE break")
    public void testOfferAlwaysReturnsFalse() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(OFFER_ALWAYS_RETURNS_FALSE);
        BreakableQueue<String> queue = builder.build();

        assertFalse(queue.offer("item1"));
        assertFalse(queue.offer("item2"));

        // Elements should still be added despite false return
        assertFalse(queue.isEmpty());
    }

    /// Tests the OFFER_ALWAYS_RETURNS_TRUE break functionality.
    @Test
    @DisplayName("Test OFFER_ALWAYS_RETURNS_TRUE break")
    public void testOfferAlwaysReturnsTrue() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(OFFER_ALWAYS_RETURNS_TRUE);
        BreakableQueue<String> queue = builder.build();

        assertTrue(queue.offer("item1"));
        assertTrue(queue.offer("item2"));

        // Elements should be added and true returned
        assertEquals(2, queue.size());
    }

    /// Tests the OFFER_DOES_NOT_ADD_ELEMENT break functionality.
    @Test
    @DisplayName("Test OFFER_DOES_NOT_ADD_ELEMENT break")
    public void testOfferDoesNotAddElement() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(OFFER_DOES_NOT_ADD_ELEMENT);
        BreakableQueue<String> queue = builder.build();

        queue.offer("item1");
        queue.offer("item2");

        // Queue should remain empty despite offers
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    /// Tests the OFFER_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test OFFER_THROWS_EXCEPTION break")
    public void testOfferThrowsException() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(OFFER_THROWS_EXCEPTION);
        BreakableQueue<String> queue = builder.build();

        assertThrows(IllegalStateException.class, () -> queue.offer("item"));
    }

    /// Tests that offer() throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test offer() throws when not supported")
    public void testOfferWhenNotSupported() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.doesNotSupport(QueueMethods.OFFER);
        BreakableQueue<String> queue = builder.build();

        assertThrows(UnsupportedOperationException.class, () -> queue.offer("item"));
    }

    // ========== Poll Method Break Tests ==========

    /// Tests the POLL_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test POLL_ALWAYS_RETURNS_NULL break")
    public void testPollAlwaysReturnsNull() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(POLL_ALWAYS_RETURNS_NULL);
        BreakableQueue<String> queue = builder.build();

        queue.offer("item1");
        queue.offer("item2");

        assertNull(queue.poll()); // Should return null due to break
        assertEquals(2, queue.size()); // Elements should still be there
    }

    /// Tests the POLL_DOES_NOT_REMOVE_ELEMENT break functionality.
    @Test
    @DisplayName("Test POLL_DOES_NOT_REMOVE_ELEMENT break")
    public void testPollDoesNotRemoveElement() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(POLL_DOES_NOT_REMOVE_ELEMENT);
        BreakableQueue<String> queue = builder.build();

        queue.offer("item1");
        queue.offer("item2");

        String polled = queue.poll();
        assertEquals("item1", polled); // Should return head element
        assertEquals(2, queue.size()); // But not remove it
        assertEquals("item1", queue.peek()); // Head should still be there
    }

    /// Tests the POLL_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test POLL_THROWS_EXCEPTION break")
    public void testPollThrowsException() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(POLL_THROWS_EXCEPTION);
        BreakableQueue<String> queue = builder.build();

        queue.offer("item");

        assertThrows(NoSuchElementException.class, queue::poll);
    }

    /// Tests the POLL_RETURNS_RANDOM_ELEMENT break functionality.
    @Test
    @DisplayName("Test POLL_RETURNS_RANDOM_ELEMENT break")
    public void testPollReturnsRandomElement() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(POLL_RETURNS_RANDOM_ELEMENT);
        BreakableQueue<String> queue = builder.build();

        queue.offer("item1");
        queue.offer("item2");
        queue.offer("item3");

        String polled = queue.poll();
        assertNotNull(polled);
        assertTrue(List.of("item1", "item2", "item3").contains(polled));
    }

    /// Tests that poll() throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test poll() throws when not supported")
    public void testPollWhenNotSupported() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.doesNotSupport(QueueMethods.POLL);
        BreakableQueue<String> queue = builder.build();

        assertThrows(UnsupportedOperationException.class, queue::poll);
    }

    // ========== Peek Method Break Tests ==========

    /// Tests the PEEK_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test PEEK_ALWAYS_RETURNS_NULL break")
    public void testPeekAlwaysReturnsNull() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(PEEK_ALWAYS_RETURNS_NULL);
        BreakableQueue<String> queue = builder.build();

        queue.offer("item1");
        queue.offer("item2");

        assertNull(queue.peek()); // Should return null due to break
        assertFalse(queue.isEmpty()); // Queue should not be empty
    }

    /// Tests the PEEK_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test PEEK_THROWS_EXCEPTION break")
    public void testPeekThrowsException() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(PEEK_THROWS_EXCEPTION);
        BreakableQueue<String> queue = builder.build();

        queue.offer("item");

        assertThrows(NoSuchElementException.class, queue::peek);
    }

    /// Tests the PEEK_RETURNS_RANDOM_ELEMENT break functionality.
    @Test
    @DisplayName("Test PEEK_RETURNS_RANDOM_ELEMENT break")
    public void testPeekReturnsRandomElement() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(PEEK_RETURNS_RANDOM_ELEMENT);
        BreakableQueue<String> queue = builder.build();

        queue.offer("item1");
        queue.offer("item2");
        queue.offer("item3");

        String peeked = queue.peek();
        assertNotNull(peeked);
        assertTrue(List.of("item1", "item2", "item3").contains(peeked));
    }

    /// Tests that peek() throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test peek() throws when not supported")
    public void testPeekWhenNotSupported() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.doesNotSupport(QueueMethods.PEEK);
        BreakableQueue<String> queue = builder.build();

        assertThrows(UnsupportedOperationException.class, queue::peek);
    }

    // ========== Element Method Break Tests ==========

    /// Tests the ELEMENT_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test ELEMENT_THROWS_EXCEPTION break")
    public void testElementThrowsException() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(ELEMENT_THROWS_EXCEPTION);
        BreakableQueue<String> queue = builder.build();

        queue.offer("item");

        assertThrows(NoSuchElementException.class, queue::element);
    }

    /// Tests the ELEMENT_RETURNS_RANDOM_ELEMENT break functionality.
    @Test
    @DisplayName("Test ELEMENT_RETURNS_RANDOM_ELEMENT break")
    public void testElementReturnsRandomElement() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(ELEMENT_RETURNS_RANDOM_ELEMENT);
        BreakableQueue<String> queue = builder.build();

        queue.offer("item1");
        queue.offer("item2");
        queue.offer("item3");

        String element = queue.element();
        assertNotNull(element);
        assertTrue(List.of("item1", "item2", "item3").contains(element));
    }

    /// Tests that element() throws UnsupportedOperationException when method is not supported.
    @Test
    @DisplayName("Test element() throws when not supported")
    public void testElementWhenNotSupported() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.doesNotSupport(QueueMethods.ELEMENT);
        BreakableQueue<String> queue = builder.build();

        assertThrows(UnsupportedOperationException.class, queue::element);
    }

    /// Tests element() throws NoSuchElementException when queue is empty.
    @Test
    @DisplayName("Test element() throws on empty queue")
    public void testElementThrowsOnEmptyQueue() {
        BreakableQueue<String> queue = new BreakableQueue<>();

        assertThrows(NoSuchElementException.class, queue::element);
    }

    // ========== Remove Method Tests ==========

    /// Tests the remove() method functionality.
    @Test
    @DisplayName("Test remove() method")
    public void testRemoveMethod() {
        BreakableQueue<String> queue = new BreakableQueue<>();

        queue.offer("item1");
        queue.offer("item2");

        String removed = queue.remove();
        assertEquals("item1", removed);
        assertEquals(1, queue.size());
        assertEquals("item2", queue.peek());
    }

    /// Tests remove() throws NoSuchElementException when queue is empty.
    @Test
    @DisplayName("Test remove() throws on empty queue")
    public void testRemoveThrowsOnEmptyQueue() {
        BreakableQueue<String> queue = new BreakableQueue<>();

        assertThrows(NoSuchElementException.class, queue::remove);
    }

    // ========== Remove Method Break Tests ==========

    /// Tests the REMOVE_THROWS_EXCEPTION break functionality.
    ///
    /// This test verifies that when the REMOVE_THROWS_EXCEPTION break is applied,
    /// the remove() method throws IllegalStateException instead of performing
    /// normal queue removal operations.
    @Test
    @DisplayName("Test REMOVE_THROWS_EXCEPTION break")
    public void testRemoveThrowsException() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(REMOVE_THROWS_EXCEPTION);
        BreakableQueue<String> queue = builder.build();

        queue.offer("item1");
        queue.offer("item2");

        // remove() should throw IllegalStateException due to break
        assertThrows(IllegalStateException.class, queue::remove);

        // Verify queue state is unchanged
        assertEquals(2, queue.size());
    }

    /// Tests the REMOVE_ALWAYS_RETURNS_NULL break functionality.
    ///
    /// This test verifies that when the REMOVE_ALWAYS_RETURNS_NULL break is applied,
    /// the remove() method returns null instead of throwing NoSuchElementException
    /// or returning an element. This is non-standard behavior useful for testing.
    @Test
    @DisplayName("Test REMOVE_ALWAYS_RETURNS_NULL break")
    public void testRemoveAlwaysReturnsNull() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(REMOVE_ALWAYS_RETURNS_NULL);
        BreakableQueue<String> queue = builder.build();

        queue.offer("item1");
        queue.offer("item2");

        // remove() should return null due to break (non-standard behavior)
        assertNull(queue.remove());

        // Verify queue state - elements should still be present since null was returned
        assertEquals(2, queue.size());
    }

    /// Tests the REMOVE_ALWAYS_RETURNS_NULL break with empty queue.
    ///
    /// This test verifies that the REMOVE_ALWAYS_RETURNS_NULL break causes
    /// remove() to return null even when called on an empty queue.
    @Test
    @DisplayName("Test REMOVE_ALWAYS_RETURNS_NULL break on empty queue")
    public void testRemoveAlwaysReturnsNullOnEmptyQueue() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(REMOVE_ALWAYS_RETURNS_NULL);
        BreakableQueue<String> queue = builder.build();

        // remove() on empty queue should return null (not throw) due to break
        assertNull(queue.remove());
    }

    /// Tests the REMOVE_DOES_NOT_REMOVE_ELEMENT break functionality.
    ///
    /// This test verifies that when the REMOVE_DOES_NOT_REMOVE_ELEMENT break is applied,
    /// the remove() method returns the head element but does not actually remove it
    /// from the queue, allowing testing of inconsistent queue state.
    @Test
    @DisplayName("Test REMOVE_DOES_NOT_REMOVE_ELEMENT break")
    public void testRemoveDoesNotRemoveElement() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(REMOVE_DOES_NOT_REMOVE_ELEMENT);
        BreakableQueue<String> queue = builder.build();

        queue.offer("item1");
        queue.offer("item2");
        queue.offer("item3");

        // remove() should return head element
        String removed = queue.remove();
        assertEquals("item1", removed);

        // But element should not be removed from queue
        assertEquals(3, queue.size());
        assertEquals("item1", queue.peek()); // Head should still be item1

        // Calling remove() again should return same element
        String removedAgain = queue.remove();
        assertEquals("item1", removedAgain);
        assertEquals(3, queue.size()); // Size unchanged
    }

    /// Tests the REMOVE_DOES_NOT_REMOVE_ELEMENT break on empty queue.
    ///
    /// This test verifies that when the REMOVE_DOES_NOT_REMOVE_ELEMENT break
    /// is applied and the queue is empty, remove() still throws NoSuchElementException.
    @Test
    @DisplayName("Test REMOVE_DOES_NOT_REMOVE_ELEMENT break on empty queue")
    public void testRemoveDoesNotRemoveElementOnEmptyQueue() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(REMOVE_DOES_NOT_REMOVE_ELEMENT);
        BreakableQueue<String> queue = builder.build();

        // remove() on empty queue should still throw NoSuchElementException
        assertThrows(NoSuchElementException.class, queue::remove);
    }

    /// Tests that remove() throws UnsupportedOperationException when method is not supported.
    ///
    /// This test verifies proper method support checking behavior for the remove() method.
    @Test
    @DisplayName("Test remove() throws when not supported")
    public void testRemoveWhenNotSupported() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.doesNotSupport(QueueMethods.REMOVE);
        BreakableQueue<String> queue = builder.build();

        queue.offer("item");

        assertThrows(UnsupportedOperationException.class, queue::remove);
    }

    /// Tests interaction between REMOVE_DOES_NOT_REMOVE_ELEMENT and normal poll.
    ///
    /// This test verifies that when remove() is broken to not remove elements,
    /// normal poll() operations still work correctly.
    @Test
    @DisplayName("Test REMOVE_DOES_NOT_REMOVE_ELEMENT with normal poll")
    public void testRemoveDoesNotRemoveElementWithPoll() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(REMOVE_DOES_NOT_REMOVE_ELEMENT);
        BreakableQueue<String> queue = builder.build();

        queue.offer("item1");
        queue.offer("item2");

        // remove() should not remove element
        String removed = queue.remove();
        assertEquals("item1", removed);
        assertEquals(2, queue.size());

        // But poll() should still work normally
        String polled = queue.poll();
        assertEquals("item1", polled);
        assertEquals(1, queue.size());
        assertEquals("item2", queue.peek());
    }

    // ========== Static Factory Method Tests ==========

    /// Tests the static wrap factory method functionality.
    @Test
    @DisplayName("Test wrap factory method")
    public void testWrapFactoryMethod() {
        Queue<String> existingQueue = new ArrayDeque<>();
        existingQueue.offer("apple");
        existingQueue.offer("banana");

        Set<Break> breaks = Set.of(POLL_ALWAYS_RETURNS_NULL, PEEK_THROWS_EXCEPTION);

        BreakableQueue<String> wrappedQueue = BreakableQueue.wrap(existingQueue, breaks);

        // Verify breaks are active
        assertNull(wrappedQueue.poll()); // Due to POLL_ALWAYS_RETURNS_NULL
        assertThrows(NoSuchElementException.class, wrappedQueue::peek); // Due to PEEK_THROWS_EXCEPTION

        // Verify original data is preserved
        assertTrue(wrappedQueue.contains("apple"));
        assertTrue(wrappedQueue.contains("banana"));
    }

    /// Tests the static wrap factory method with characteristics.
    @Test
    @DisplayName("Test wrap factory method with characteristics")
    public void testWrapFactoryMethodWithCharacteristics() {
        Queue<String> existingQueue = new ArrayDeque<>();
        existingQueue.offer("test");

        Set<Break> breaks = Set.of(OFFER_ALWAYS_RETURNS_FALSE);

        BreakableQueue<String> wrappedQueue = BreakableQueue.wrap(existingQueue, breaks, Collections.emptyMap(), 0);

        // Verify break is active
        assertFalse(wrappedQueue.offer("new_item"));

        // Verify original data is preserved
        assertTrue(wrappedQueue.contains("test"));
    }

    // ========== Queue Behavior and Integration Tests ==========

    /// Tests that BreakableQueue maintains proper FIFO ordering.
    @Test
    @DisplayName("Test FIFO ordering is maintained")
    public void testFIFOOrdering() {
        BreakableQueue<String> queue = new BreakableQueue<>();

        // Add elements in specific order
        queue.offer("first");
        queue.offer("second");
        queue.offer("third");

        // Verify FIFO ordering
        assertEquals("first", queue.poll());
        assertEquals("second", queue.poll());
        assertEquals("third", queue.poll());
        assertNull(queue.poll()); // Queue should be empty
    }

    /// Tests queue operations with multiple breaks applied.
    @Test
    @DisplayName("Test multiple breaks interaction")
    public void testMultipleBreaks() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(OFFER_ALWAYS_RETURNS_FALSE);
        builder.addBreak(PEEK_ALWAYS_RETURNS_NULL);
        builder.addBreak(ELEMENT_THROWS_EXCEPTION);
        BreakableQueue<String> queue = builder.build();

        // Test offer break
        assertFalse(queue.offer("item"));
        assertFalse(queue.isEmpty()); // Element should still be added

        // Test peek break
        assertNull(queue.peek());

        // Test element break
        assertThrows(NoSuchElementException.class, queue::element);
    }

    // ========== Inheritance and Collection Interface Tests ==========

    /// Tests that BreakableQueue properly inherits Collection functionality.
    @Test
    @DisplayName("Test inheritance from BreakableCollection")
    public void testCollectionInheritance() {
        BreakableQueue.Builder<String> builder = new BreakableQueue.Builder<>();
        builder.addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_ZERO); // Collection-level break
        builder.addBreak(POLL_ALWAYS_RETURNS_NULL);                      // Queue-level break
        BreakableQueue<String> queue = builder.build();

        queue.offer("item1");
        queue.offer("item2");

        // Collection-level functionality with break
        assertEquals(0, queue.size()); // Due to SIZE_ALWAYS_RETURNS_ZERO break
        assertFalse(queue.isEmpty());  // But queue is not actually empty

        // Queue-level functionality with break
        assertNull(queue.poll()); // Due to POLL_ALWAYS_RETURNS_NULL break
    }

    /// Tests add_singleElement_returnsTrueAndUpdatesSize() method inherited from Collection interface.
    @Test
    @DisplayName("Test add_singleElement_returnsTrueAndUpdatesSize() method from Collection")
    public void testAddMethod() {
        BreakableQueue<String> queue = new BreakableQueue<>();

        assertTrue(queue.add("item1"));
        assertTrue(queue.add("item2"));

        assertEquals(2, queue.size());
        assertEquals("item1", queue.peek()); // FIFO ordering maintained
    }

    /// Tests normal queue operations without breaks.
    @Test
    @DisplayName("Test normal queue operations")
    public void testNormalQueueOperations() {
        BreakableQueue<Integer> queue = new BreakableQueue<>();

        // Test normal queue workflow
        assertTrue(queue.offer(1));
        assertTrue(queue.offer(2));
        assertTrue(queue.offer(3));

        assertEquals(Integer.valueOf(1), queue.peek());
        assertEquals(Integer.valueOf(1), queue.element());
        assertEquals(Integer.valueOf(1), queue.poll());

        assertEquals(Integer.valueOf(2), queue.remove());
        assertEquals(1, queue.size());

        assertEquals(Integer.valueOf(3), queue.poll());
        assertTrue(queue.isEmpty());
    }
}