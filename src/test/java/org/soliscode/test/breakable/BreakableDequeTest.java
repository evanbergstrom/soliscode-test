package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.deque.DequeMethods;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.soliscode.test.assertions.collection.CollectionAssertions.*;
import static org.soliscode.test.breakable.BreakableDeque.*;

/// **Test Suite for BreakableDeque Implementation**
///
/// This comprehensive test class validates the behavior of BreakableDeque, focusing on both
/// standard Deque contract compliance and the controlled violation of deque semantics through
/// programmatic breaks. The tests ensure that BreakableDeque maintains proper double-ended behavior
/// under normal conditions while correctly implementing break mechanisms for testing purposes.
///
/// ## Test Coverage
///
/// ### Deque Contract Compliance Testing
/// - **Queue Interface**: Full validation of inherited Queue interface methods
/// - **Collection Interface**: Full validation of inherited Collection interface methods
/// - **Deque Interface**: Full validation of Deque-specific methods
/// - **Double-Ended Operations**: addFirst/addLast, offerFirst/offerLast, removeFirst/removeLast
/// - **Poll Operations**: pollFirst/pollLast with null handling
/// - **Peek Operations**: peekFirst/peekLast with null handling
/// - **Stack Operations**: push/pop method testing
/// - **Search Operations**: removeFirstOccurrence/removeLastOccurrence method testing
/// - **Iterator Operations**: descendingIterator method testing
///
/// ### Break Mechanism Testing
/// - **First-End Breaks**: OFFER_FIRST_*, POLL_FIRST_*, PEEK_FIRST_* breaks
/// - **Last-End Breaks**: OFFER_LAST_*, POLL_LAST_*, PEEK_LAST_* breaks
/// - **Stack Operation Breaks**: PUSH_*, POP_* breaks for stack behavior
/// - **Search Operation Breaks**: REMOVE_*_OCCURRENCE_* breaks for search operations
/// - **Iterator Breaks**: DESCENDING_ITERATOR_* breaks for iteration behavior
/// - **Exception Breaks**: Various exception-throwing behaviors
///
/// ### Builder Pattern Testing
/// - **Deque-Specific Configuration**: Builder setup with Deque parameters
/// - **Copy Semantics**: Builder copying and independence for deques
/// - **Fluent Interface**: Method chaining with deque configuration
/// - **Pre-populated Deques**: Building from existing Deque implementations
///
/// ### Constructor Testing
/// - **Default Construction**: Empty deque creation with ArrayDeque
/// - **Copy Construction**: Creating deques from existing BreakableDeque instances
/// - **Deque Construction**: Building from existing Deque implementations
///
/// ## Test Architecture
///
/// This test class follows the SolisCode testing framework patterns:
/// - **AbstractTest Extension**: Inherits common testing infrastructure
/// - **Deque-Specific Testing**: Focused on Deque interface compliance
/// - **Break Isolation**: Each break is tested independently for deque operations
/// - **State Validation**: Verifies deque state consistency with FIFO/LIFO guarantees
/// - **Double-Ended Awareness**: Tests consider both front and back operations
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see BreakableDeque
/// @see Deque
/// @see AbstractTest
public class BreakableDequeTest extends AbstractTest {

    // ========== Constructor Tests ==========

    /// Tests the default constructor functionality and initial state validation.
    @Test
    @DisplayName("default constructor creates empty breakable deque with the correct default values")
    public void defaultConstructor_createsObjectWithDefaultValues_whenCalled() {
        BreakableDeque<String> deque = new BreakableDeque<>();
        assertIsEmpty(deque.breaks());
        assertTrue(deque.supportsAllMethods());
        assertTrue(deque.breaks().isEmpty());
        assertTrue(deque.permitsNulls());
        assertTrue(deque.permitsDuplicates());
        assertTrue(deque.permitsIncompatibleTypes());
        assertFalse(deque.isSafe());
        assertTrue(deque.isEmpty());
    }

    /// Tests the copy constructor behavior and configuration inheritance.
    @Test
    @DisplayName("copy constructor creates an exact copy when called")
    public void copyConstructor_createsAnExactCopy_whenCalled() {
        // Create instance to copy with ALL NON-DEFAULT values.
        BreakableDeque<String> original = BreakableDeque.<String>builder()
                .addBreak(OFFER_FIRST_ALWAYS_RETURNS_FALSE)
                .doesNotSupport(DequeMethods.POLL_LAST)
                .doesNotPermitNulls()
                .doesNotPermitDuplicates()
                .doesNotPermitIncompatibleTypes(String.class)
                .setSafe(true)
                .addElements("item1", "item2")
                .build();

        BreakableDeque<String> copy = new BreakableDeque<>(original);

        assertNotSame(original, copy);
        assertEquals(original.size(), copy.size());
        assertContainsSame(original.breaks(), copy.breaks());
        assertTrue(copy.hasBreak(OFFER_FIRST_ALWAYS_RETURNS_FALSE));
        assertFalse(copy.supportsMethod(DequeMethods.POLL_LAST));
        assertFalse(copy.permitsNulls());
        assertFalse(copy.permitsDuplicates());
        assertFalse(copy.permitsIncompatibleTypes());
        assertEquals(original.isSafe(), copy.isSafe());
    }

    /// Tests constructor with ArrayDeque integration.
    @Test
    @DisplayName("value constructor with ArrayDeque create a deque with the same elements")
    public void valueConstructor_createsDequeWithSameElements_whenCalled() {
        LinkedList<String> arrayDeque = new LinkedList<>();
        arrayDeque.addFirst("third");
        arrayDeque.addFirst("second");
        arrayDeque.addFirst("first");

        BreakableDeque<String> deque = new BreakableDeque.Builder<>(arrayDeque).build();

        assertContainsSame(arrayDeque, deque);
        assertIsEmpty(deque.breaks());
        assertTrue(deque.supportsAllMethods());
        assertTrue(deque.breaks().isEmpty());
        assertTrue(deque.permitsNulls());
        assertTrue(deque.permitsDuplicates());
        assertTrue(deque.permitsIncompatibleTypes());
        assertFalse(deque.isSafe());
    }

    // ========== Builder Tests ==========

    /// Tests basic Builder pattern functionality and configuration transfer.
    @Test
    @DisplayName("Test builder pattern")
    public void testBuilder() {
        BreakableDeque.Builder<String> builder = new BreakableDeque.Builder<>();
        builder.addBreak(POLL_FIRST_ALWAYS_RETURNS_NULL);
        builder.addBreak(PEEK_LAST_ALWAYS_RETURNS_NULL);
        BreakableDeque<String> deque = builder.build();

        deque.offerFirst("item");

        // Test pollFirst break
        assertNull(deque.pollFirst()); // Should return null due to break

        // Test peekLast break
        assertNull(deque.peekLast()); // Should return null due to break
    }

    /// Tests Builder creation with pre-existing Deque data.
    @Test
    @DisplayName("Test builder with existing Deque")
    public void testBuilderWithExistingDeque() {
        LinkedList<String> existingDeque = new LinkedList<>();
        existingDeque.addLast("apple");
        existingDeque.addLast("banana");
        existingDeque.addLast("cherry");

        BreakableDeque.Builder<String> builder = new BreakableDeque.Builder<>(existingDeque);
        builder.addBreak(REMOVE_FIRST_OCCURRENCE_ALWAYS_RETURNS_FALSE);
        BreakableDeque<String> deque = builder.build();

        // Original data should be present
        assertEquals("apple", deque.peekFirst());
        assertEquals(3, deque.size());

        // Break should be active
        assertFalse(deque.removeFirstOccurrence("apple")); // Should return false due to break
    }

    /// Tests Builder copy functionality and configuration inheritance.
    @Test
    @DisplayName("Test builder copy")
    public void testBuilderCopy() {
        BreakableDeque.Builder<String> original = new BreakableDeque.Builder<>();
        original.addBreak(PUSH_DOES_NOT_ADD_ELEMENT);

        BreakableDeque.Builder<String> copy = original.copy();
        copy.addBreak(POP_ALWAYS_RETURNS_NULL);

        BreakableDeque<String> originalDeque = original.build();
        BreakableDeque<String> copyDeque = copy.build();

        // Test original - should have push break but normal pop
        originalDeque.push("test1");
        assertEquals(0, originalDeque.size()); // Element not added due to break

        // Manually add_singleElement_returnsTrueAndUpdatesSize element to test pop
        originalDeque.offerFirst("test2");
        assertEquals("test2", originalDeque.pop()); // Normal pop behavior

        // Test copy - should have both breaks
        copyDeque.push("test3");
        assertEquals(0, copyDeque.size()); // Element not added due to break

        // Manually add_singleElement_returnsTrueAndUpdatesSize element to test pop
        copyDeque.offerFirst("test4");
        assertNull(copyDeque.pop()); // Pop returns null due to break
    }


    /// Tests that the [BreakableDeque#OFFER_FIRST_ALWAYS_RETURNS_FALSE] break forces [BreakableDeque#offerFirst]
    /// to return `false' even when the element is successfully added to the underlying deque.
    @Test
    @DisplayName("offerFirst() always return false with OFFER_FIRST_ALWAYS_RETURNS_FALSE break applied")
    public void offerFirst_whenOfferFirstAlwaysReturnsFalseBreakIsApplied_returnsFalse() {
        BreakableDeque<String> deque =  BreakableDeque.<String>builder()
                .addBreak(OFFER_FIRST_ALWAYS_RETURNS_FALSE)
                .build();

        assertFalse(deque.offerFirst("item1"));
        assertFalse(deque.offerFirst("item2"));

        // Elements should still be added despite false return
        assertFalse(deque.isEmpty());
        assertContains("item1", deque);
        assertContains("item2", deque);
    }

    /// Tests the OFFER_FIRST_DOES_NOT_ADD_ELEMENT break functionality.
    @Test
    @DisplayName("offerFirst(Object) does not add_singleElement_returnsTrueAndUpdatesSize element when OFFER_FIRST_DOES_NOT_ADD_ELEMENT break is applied")
    public void offerFirst_whenOfferFirstDoesNotAddElementBreakIsApplied_doesNotAddElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(OFFER_FIRST_DOES_NOT_ADD_ELEMENT)
                .build();

        assertTrue(deque.offerFirst("item1")); // Should return true
        assertIsEmpty(deque); // But deque should still be empty
    }

    @Test
    @DisplayName("offerFirst(Object) throws UnsupportedOperationException when it is not supported")
    public void offerFirst_whenItIsNotSuppoerted_throwsUnsupportedOperationException() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .doesNotSupport(DequeMethods.OFFER_FIRST)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> deque.offerFirst("item1"));
    }

    /// Tests the POLL_FIRST_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test POLL_FIRST_ALWAYS_RETURNS_NULL break")
    public void testPollFirstAlwaysReturnsNull() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(POLL_FIRST_ALWAYS_RETURNS_NULL)
                .addElements("item1", "item2")
                .build();

        assertNull(deque.pollFirst()); // Should return null due to break
        assertFalse(deque.isEmpty()); // Deque should not be empty
    }

    /// Tests the POLL_FIRST_DOES_NOT_REMOVE_ELEMENT break functionality.
    @Test
    @DisplayName("Test POLL_FIRST_DOES_NOT_REMOVE_ELEMENT break")
    public void testPollFirstDoesNotRemoveElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(POLL_FIRST_DOES_NOT_REMOVE_ELEMENT)
                .build();

        deque.offerFirst("1");
        deque.offerFirst("2");

        String polled = deque.pollFirst();
        assertEquals("2", polled); // Should return head element

        assertEquals(2, deque.size()); // But not remove it
        assertEquals("2", deque.peekFirst()); // Head should still be there
    }


    @Test
    @DisplayName("pollFirst() throws UnsupportedOperationException when it is not supported")
    public void pollFirst_whenItIsNotSuppoerted_throwsUnsupportedOperationException() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .doesNotSupport(DequeMethods.POLL_FIRST)
                .build();

        deque.offerFirst("1");

        assertThrows(UnsupportedOperationException.class, () -> deque.pollFirst());
    }

    /// Tests the PEEK_FIRST_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test PEEK_FIRST_ALWAYS_RETURNS_NULL break")
    public void testPeekFirstAlwaysReturnsNull() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(PEEK_FIRST_ALWAYS_RETURNS_NULL)
                .build();

        deque.offerFirst("item");
        assertNull(deque.peekFirst()); // Should return null due to break

        // Deque should not be empty
        assertFalse(deque.isEmpty());
    }

    /// Tests the PEEK_FIRST_RETURNS_RANDOM_ELEMENT break functionality.
    @Test
    @DisplayName("Test PEEK_FIRST_RETURNS_RANDOM_ELEMENT break")
    public void testPeekFirstReturnsRandomElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(PEEK_FIRST_RETURNS_RANDOM_ELEMENT)
                .addElements("item", "item2", "item3")
                .build();

        String peeked = deque.peekFirst(); // For deque, this should be the last item offered: "item3"
        assertNotNull(peeked);
        assertNotEquals("item3", peeked); // Should return different element due to break
    }

    @Test
    @DisplayName("peekFirst() throws UnsupportedOperationException when it is not supported")
    public void peekFirst_whenItIsNotSuppoerted_throwsUnsupportedOperationException() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .doesNotSupport(DequeMethods.PEEK_FIRST)
                .addElements("item1")
                .build();

        assertThrows(UnsupportedOperationException.class, deque::peekFirst);
    }

    /// Tests the OFFER_LAST_ALWAYS_RETURNS_FALSE break functionality.
    @Test
    @DisplayName("Test OFFER_LAST_ALWAYS_RETURNS_FALSE break")
    public void testOfferLastAlwaysReturnsFalse() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(OFFER_LAST_ALWAYS_RETURNS_FALSE)
                .build();

        assertFalse(deque.offerLast("item1"));
        assertFalse(deque.offerLast("item2"));

        // Elements should still be added despite false return
        assertFalse(deque.isEmpty());
    }

    /// Tests the OFFER_LAST_DOES_NOT_ADD_ELEMENT break functionality.
    @Test
    @DisplayName("Test OFFER_LAST_DOES_NOT_ADD_ELEMENT break")
    public void testOfferLastDoesNotAddElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(OFFER_LAST_DOES_NOT_ADD_ELEMENT)
                .build();

        assertTrue(deque.offerLast("item1")); // Should return true
        assertTrue(deque.offerLast("item2")); // Should return true

        // But deque should remain empty
        assertEquals(0, deque.size());
        assertTrue(deque.isEmpty());
    }

    /// Tests the POLL_LAST_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test POLL_LAST_ALWAYS_RETURNS_NULL break")
    public void testPollLastAlwaysReturnsNull() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(POLL_LAST_ALWAYS_RETURNS_NULL)
                .build();

        deque.offerLast("item1");
        deque.offerLast("item2");

        assertNull(deque.pollLast()); // Should return null due to break
        assertFalse(deque.isEmpty()); // Deque should not be empty
    }

    /// Tests the POLL_LAST_DOES_NOT_REMOVE_ELEMENT break functionality.
    @Test
    @DisplayName("Test POLL_LAST_DOES_NOT_REMOVE_ELEMENT break")
    public void testPollLastDoesNotRemoveElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(POLL_LAST_DOES_NOT_REMOVE_ELEMENT)
                .build();

        deque.offerLast("item1");
        deque.offerLast("item2");

        String polled = deque.pollLast();
        assertEquals("item2", polled); // Should return tail element
        assertEquals(2, deque.size()); // But not remove it
        assertEquals("item2", deque.peekLast()); // Tail should still be there
    }

    /// Tests the PEEK_LAST_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test PEEK_LAST_ALWAYS_RETURNS_NULL break")
    public void testPeekLastAlwaysReturnsNull() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(PEEK_LAST_ALWAYS_RETURNS_NULL)
                .build();

        deque.offerLast("item");
        assertNull(deque.peekLast()); // Should return null due to break

        // Deque should not be empty
        assertFalse(deque.isEmpty());
    }

    /// Tests the PEEK_LAST_RETURNS_RANDOM_ELEMENT break functionality.
    @Test
    @DisplayName("Test PEEK_LAST_RETURNS_RANDOM_ELEMENT break")
    public void testPeekLastReturnsRandomElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(PEEK_LAST_RETURNS_RANDOM_ELEMENT)
                .build();

        deque.offerLast("item3");
        deque.offerLast("item2");
        deque.offerLast("item");
        String peeked = deque.peekLast();

        assertNotNull(peeked);
        assertNotEquals("item", peeked); // Should return different element due to break
        assertContains(peeked, deque);
    }

    // ========== Stack Operation Tests ==========

    /// Tests the PUSH_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test PUSH_THROWS_EXCEPTION break")
    public void testPushThrowsException() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(PUSH_THROWS_EXCEPTION)
                .build();

        assertThrows(IllegalStateException.class, () -> deque.push("item"));
    }

    /// Tests the PUSH_DOES_NOT_ADD_ELEMENT break functionality.
    @Test
    @DisplayName("Test PUSH_DOES_NOT_ADD_ELEMENT break")
    public void testPushDoesNotAddElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(PUSH_DOES_NOT_ADD_ELEMENT)
                .build();

        deque.push("item1");
        deque.push("item2");

        // Deque should remain empty despite pushes
        assertEquals(0, deque.size());
        assertTrue(deque.isEmpty());
    }

    /// Tests the POP_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test POP_THROWS_EXCEPTION break")
    public void testPopThrowsException() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(POP_THROWS_EXCEPTION)
                .build();

        deque.push("item");
        assertThrows(IllegalStateException.class, deque::pop);
    }

    /// Tests the POP_ALWAYS_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test POP_ALWAYS_RETURNS_NULL break")
    public void testPopAlwaysReturnsNull() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(POP_ALWAYS_RETURNS_NULL)
                .build();

        deque.push("item1");
        deque.push("item2");

        assertNull(deque.pop()); // Should return null due to break
        assertEquals(2, deque.size()); // Elements should still be there
    }

    // ========== Occurrence Removal Tests ==========

    /// Tests the REMOVE_FIRST_OCCURRENCE_ALWAYS_RETURNS_FALSE break functionality.
    @Test
    @DisplayName("Test REMOVE_FIRST_OCCURRENCE_ALWAYS_RETURNS_FALSE break")
    public void testRemoveFirstOccurrenceAlwaysReturnsFalse() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(REMOVE_FIRST_OCCURRENCE_ALWAYS_RETURNS_FALSE)
                .build();

        deque.offerLast("item");
        deque.offerLast("item");
        deque.offerLast("other");

        assertFalse(deque.removeFirstOccurrence("item")); // Should return false due to break
        assertEquals(3, deque.size()); // Elements should still be there
    }

    /// Tests the REMOVE_FIRST_OCCURRENCE_DOES_NOT_REMOVE break functionality.
    @Test
    @DisplayName("Test REMOVE_FIRST_OCCURRENCE_DOES_NOT_REMOVE break")
    public void testRemoveFirstOccurrenceDoesNotRemove() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(REMOVE_FIRST_OCCURRENCE_DOES_NOT_REMOVE)
                .build();

        deque.offerLast("item");
        deque.offerLast("other");
        deque.offerLast("item");

        assertTrue(deque.removeFirstOccurrence("item")); // Should return true
        assertEquals(3, deque.size()); // But element should not be removed
        assertTrue(deque.contains("item")); // Item should still be present
    }

    /// Tests the REMOVE_LAST_OCCURRENCE_ALWAYS_RETURNS_FALSE break functionality.
    @Test
    @DisplayName("Test REMOVE_LAST_OCCURRENCE_ALWAYS_RETURNS_FALSE break")
    public void testRemoveLastOccurrenceAlwaysReturnsFalse() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(REMOVE_LAST_OCCURRENCE_ALWAYS_RETURNS_FALSE)
                .build();

        deque.offerLast("item");
        deque.offerLast("other");
        deque.offerLast("item");

        assertFalse(deque.removeLastOccurrence("item")); // Should return false due to break
        assertEquals(3, deque.size()); // Elements should still be there
    }

    /// Tests the REMOVE_LAST_OCCURRENCE_DOES_NOT_REMOVE break functionality.
    @Test
    @DisplayName("Test REMOVE_LAST_OCCURRENCE_DOES_NOT_REMOVE break")
    public void testRemoveLastOccurrenceDoesNotRemove() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(REMOVE_LAST_OCCURRENCE_DOES_NOT_REMOVE)
                .build();

        deque.offerLast("item");
        deque.offerLast("other");
        deque.offerLast("item");

        assertTrue(deque.removeLastOccurrence("item")); // Should return true
        assertEquals(3, deque.size()); // But element should not be removed
        assertTrue(deque.contains("item")); // Item should still be present
    }

    // ========== Iterator Operation Tests ==========

    /// Tests the DESCENDING_ITERATOR_THROWS_EXCEPTION break functionality.
    @Test
    @DisplayName("Test DESCENDING_ITERATOR_THROWS_EXCEPTION break")
    public void testDescendingIteratorThrowsException() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(DESCENDING_ITERATOR_THROWS_EXCEPTION)
                .build();

        assertThrows(IllegalStateException.class, deque::descendingIterator);
    }

    /// Tests the DESCENDING_ITERATOR_RETURNS_EMPTY break functionality.
    @Test
    @DisplayName("Test DESCENDING_ITERATOR_RETURNS_EMPTY break")
    public void testDescendingIteratorReturnsEmpty() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(DESCENDING_ITERATOR_RETURNS_EMPTY)
                .build();

        deque.offerLast("item1");
        deque.offerLast("item2");
        deque.offerLast("item3");

        Iterator<String> iterator = deque.descendingIterator();
        assertFalse(iterator.hasNext()); // Should be empty due to break
    }

    // ========== UnsupportedOperationException Tests ==========

    /// Tests that methods throw UnsupportedOperationException when not supported.
    @Test
    @DisplayName("Test unsupported method exceptions")
    public void testUnsupportedMethodExceptions() {
        BreakableDeque<String> deque = new BreakableDeque.Builder<String>()
                .doesNotSupport(DequeMethods.PUSH)
                .doesNotSupport(DequeMethods.POP)
                .doesNotSupport(DequeMethods.DESCENDING_ITERATOR)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> deque.push("item"));
        assertThrows(UnsupportedOperationException.class, deque::pop);
        assertThrows(UnsupportedOperationException.class, deque::descendingIterator);
    }

    // ========== Static Factory Method Tests ==========

    /// Tests the static wrap factory method functionality.
    @Test
    @DisplayName("Test wrap factory method")
    public void testWrapFactoryMethod() {
        LinkedList<String> existingDeque = new LinkedList<>();
        existingDeque.addLast("apple");
        existingDeque.addLast("banana");

        Set<Break> breaks = Set.of(OFFER_FIRST_ALWAYS_RETURNS_FALSE, PEEK_LAST_ALWAYS_RETURNS_NULL);

        BreakableDeque<String> wrappedDeque = BreakableDeque.wrap(existingDeque, breaks);

        // Verify breaks are active
        assertFalse(wrappedDeque.offerFirst("new_item")); // Due to OFFER_FIRST_ALWAYS_RETURNS_FALSE
        assertNull(wrappedDeque.peekLast()); // Due to PEEK_LAST_ALWAYS_RETURNS_NULL

        // Verify original data is preserved
        assertTrue(wrappedDeque.contains("apple"));
        assertTrue(wrappedDeque.contains("banana"));
    }

    /// Tests the static wrap factory method with characteristics.
    @Test
    @DisplayName("Test wrap factory method with characteristics")
    public void testWrapFactoryMethodWithCharacteristics() {
        LinkedList<String> existingDeque = new LinkedList<>();
        existingDeque.addLast("test");

        Set<Break> breaks = Set.of(POLL_FIRST_ALWAYS_RETURNS_NULL);

        BreakableDeque<String> wrappedDeque = BreakableDeque.wrap(existingDeque, breaks, 0);

        // Verify break is active
        assertNull(wrappedDeque.pollFirst());

        // Verify original data is preserved
        assertTrue(wrappedDeque.contains("test"));
    }

    // ========== Deque Behavior and Integration Tests ==========

    /// Tests normal deque operations without breaks.
    @Test
    @DisplayName("Test normal deque operations")
    public void testNormalDequeOperations() {
        BreakableDeque<Integer> deque = new BreakableDeque<>();

        // Test double-ended operations
        deque.addFirst(2);
        deque.addLast(3);
        deque.addFirst(1);
        deque.addLast(4);

        assertEquals(Integer.valueOf(1), deque.peekFirst());
        assertEquals(Integer.valueOf(4), deque.peekLast());

        // Test stack operations
        deque.push(0); // Should add_singleElement_returnsTrueAndUpdatesSize to front
        assertEquals(Integer.valueOf(0), deque.peekFirst());
        assertEquals(Integer.valueOf(0), deque.pop());

        // Test poll operations
        assertEquals(Integer.valueOf(1), deque.pollFirst());
        assertEquals(Integer.valueOf(4), deque.pollLast());

        assertEquals(2, deque.size());
        assertEquals(Integer.valueOf(2), deque.peekFirst());
        assertEquals(Integer.valueOf(3), deque.peekLast());
    }

    /// Tests deque operations with multiple breaks applied.
    @Test
    @DisplayName("Test multiple breaks interaction")
    public void testMultipleBreaks() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(OFFER_FIRST_DOES_NOT_ADD_ELEMENT)
                .addBreak(POLL_LAST_ALWAYS_RETURNS_NULL)
                .addBreak(PUSH_THROWS_EXCEPTION)
                .build();

        // Test offerFirst break (should not add_singleElement_returnsTrueAndUpdatesSize element)
        assertTrue(deque.offerFirst("item1"));
        assertEquals(0, deque.size()); // Element should not be added

        // Add element normally to test other breaks
        deque.offerLast("item2");
        assertEquals(1, deque.size());

        // Test pollLast break (should return null)
        assertNull(deque.pollLast());

        // Test push break (should throw exception)
        assertThrows(IllegalStateException.class, () -> deque.push("item3"));
    }

    /// Tests BreakableDeque inheritance from BreakableQueue.
    @Test
    @DisplayName("Test inheritance from BreakableQueue")
    public void testQueueInheritance() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(BreakableCollection.SIZE_ALWAYS_RETURNS_ZERO) // Collection-level break
                .addBreak(BreakableQueue.POLL_ALWAYS_RETURNS_NULL)       // Queue-level break
                .addBreak(OFFER_FIRST_DOES_NOT_ADD_ELEMENT)              // Deque-level break
                .build();

        deque.offer("item1"); // Should add_singleElement_returnsTrueAndUpdatesSize normally
        deque.offerFirst("item2"); // Should not add_singleElement_returnsTrueAndUpdatesSize due to Deque break
        deque.offerLast("item3"); // Should add_singleElement_returnsTrueAndUpdatesSize normally

        // Collection-level functionality with break
        assertEquals(0, deque.size()); // Due to SIZE_ALWAYS_RETURNS_ZERO break
        assertFalse(deque.isEmpty());  // But deque is not actually empty

        // Queue-level functionality with break
        assertNull(deque.poll()); // Due to POLL_ALWAYS_RETURNS_NULL break

        // Deque-level functionality verified by offerFirst not adding
        assertEquals(2, deque.toArray().length); // Only item1 and item3 should be present
    }

    /// Tests add_singleElement_returnsTrueAndUpdatesSize() method inherited from Collection interface.
    @Test
    @DisplayName("Test add_singleElement_returnsTrueAndUpdatesSize() method from Collection")
    public void testAddMethod() {
        BreakableDeque<String> deque = new BreakableDeque<>();

        assertTrue(deque.add("item1"));
        assertTrue(deque.add("item2"));

        assertEquals(2, deque.size());
        assertEquals("item1", deque.peekFirst()); // FIFO ordering maintained
        assertEquals("item2", deque.peekLast());
    }

    /// Tests deque operations with various deque implementations.
    @Test
    @DisplayName("Test LinkedList integration")
    public void testLinkedListIntegration() {
        LinkedList<String> linkedList = new LinkedList<>();
        linkedList.addFirst("beta");
        linkedList.addFirst("alpha");
        linkedList.addLast("gamma");

        BreakableDeque<String> deque = new BreakableDeque.Builder<>(linkedList)
                .addBreak(REMOVE_FIRST_OCCURRENCE_DOES_NOT_REMOVE)
                .build();

        // Verify LinkedList ordering is preserved
        assertEquals("alpha", deque.peekFirst());
        assertEquals("gamma", deque.peekLast());

        // Test removeFirstOccurrence break - should return true but not remove
        assertTrue(deque.removeFirstOccurrence("alpha"));
        assertEquals(3, deque.size()); // Should still have all elements
        assertEquals("alpha", deque.peekFirst()); // Head should still be alpha
    }

    /// Tests occurrence removal functionality.
    @Test
    @DisplayName("Test occurrence removal operations")
    public void testOccurrenceRemovalOperations() {
        BreakableDeque<String> deque = new BreakableDeque<>();

        deque.addLast("item");
        deque.addLast("other");
        deque.addLast("item");
        deque.addLast("item");

        // Test normal removeFirstOccurrence
        assertTrue(deque.removeFirstOccurrence("item"));
        assertEquals(3, deque.size());
        assertEquals("other", deque.peekFirst()); // First "item" should be removed

        // Test normal removeLastOccurrence
        assertTrue(deque.removeLastOccurrence("item"));
        assertEquals(2, deque.size());
        assertEquals("item", deque.peekLast()); // Last "item" should be removed

        // Test removal of non-existent element
        assertFalse(deque.removeFirstOccurrence("nonexistent"));
        assertFalse(deque.removeLastOccurrence("nonexistent"));
    }

    /// Tests descending iterator functionality.
    @Test
    @DisplayName("Test descending iterator operations")
    public void testDescendingIteratorOperations() {
        BreakableDeque<String> deque = new BreakableDeque<>();

        deque.addLast("first");
        deque.addLast("second");
        deque.addLast("third");

        Iterator<String> descendingIter = deque.descendingIterator();

        assertTrue(descendingIter.hasNext());
        assertEquals("third", descendingIter.next());
        assertEquals("second", descendingIter.next());
        assertEquals("first", descendingIter.next());
        assertFalse(descendingIter.hasNext());
    }

    /// Tests stack behavior using deque as stack.
    @Test
    @DisplayName("Test stack behavior")
    public void testStackBehavior() {
        BreakableDeque<String> stack = new BreakableDeque<>();

        // Test push/pop LIFO behavior
        stack.push("bottom");
        stack.push("middle");
        stack.push("top");

        assertEquals("top", stack.peek());
        assertEquals("top", stack.pop());
        assertEquals("middle", stack.pop());
        assertEquals("bottom", stack.pop());

        assertTrue(stack.isEmpty());
    }

    /// Tests break priority and interaction.
    @Test
    @DisplayName("Test break priority and interaction")
    public void testBreakPriorityAndInteraction() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(PUSH_THROWS_EXCEPTION) // Should throw before other breaks
                .addBreak(PUSH_DOES_NOT_ADD_ELEMENT)
                .build();

        // Exception break should take priority
        assertThrows(IllegalStateException.class, () -> deque.push("item"));
    }

    /// Tests state consistency across different deque operations.
    @Test
    @DisplayName("Test state consistency across deque operations")
    public void testStateConsistencyAcrossDequeOperations() {
        BreakableDeque<String> deque = new BreakableDeque<>();

        deque.offerFirst("first");
        deque.offerLast("last");
        deque.push("stack-top");

        assertEquals(3, deque.size());
        assertEquals("stack-top", deque.peekFirst());
        assertEquals("last", deque.peekLast());

        // Test that operations maintain consistency
        assertEquals("stack-top", deque.pollFirst());
        assertEquals("last", deque.pollLast());
        assertEquals(1, deque.size());
        assertEquals("first", deque.peek());
    }

    // ========== GetFirst Method Break Tests ==========

    /// Tests the GET_FIRST_RETURNS_NULL break functionality.
    @Test
    @DisplayName("getFirst() always returns null when GET_FIRST_RETURNS_NULL break is applied")
    public void getFirst_whenGetFirstReturnsNullBreakIsApplied_returnsNull() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(GET_FIRST_RETURNS_NULL)
                .build();

        deque.addFirst("item1");
        deque.addFirst("item2");

        assertNull(deque.getFirst()); // Should return null due to break
        assertEquals(2, deque.size()); // Deque should still have elements
    }

    /// Tests the GET_FIRST_ALWAYS_THROWS break functionality.
    @Test
    @DisplayName("Test GET_FIRST_ALWAYS_THROWS break")
    public void testGetFirstAlwaysThrows() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(GET_FIRST_ALWAYS_THROWS)
                .build();

        deque.addFirst("item");

        assertThrows(NoSuchElementException.class, deque::getFirst);
    }

    /// Tests the GET_FIRST_SKIPS_FIRST_ELEMENT break functionality.
    @Test
    @DisplayName("Test GET_FIRST_SKIPS_FIRST_ELEMENT break")
    public void testGetFirstSkipsFirstElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(GET_FIRST_SKIPS_FIRST_ELEMENT)
                .build();

        deque.addFirst("item1");
        deque.addLast("item2");
        deque.addLast("item3");

        // Should return second element (item2) instead of first (item1)
        assertEquals("item2", deque.getFirst());
    }

    /// Tests GET_FIRST_SKIPS_FIRST_ELEMENT break with insufficient elements.
    @Test
    @DisplayName("Test GET_FIRST_SKIPS_FIRST_ELEMENT break with one element")
    public void testGetFirstSkipsFirstElementWithOneElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(GET_FIRST_SKIPS_FIRST_ELEMENT)
                .build();

        deque.addFirst("item1");

        // Should throw since there's no second element
        assertThrows(NoSuchElementException.class, deque::getFirst);
    }

    // ========== GetLast Method Break Tests ==========

    /// Tests the GET_LAST_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test GET_LAST_RETURNS_NULL break")
    public void testGetLastReturnsNull() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(GET_LAST_RETURNS_NULL)
                .build();

        deque.addLast("item1");
        deque.addLast("item2");

        assertNull(deque.getLast()); // Should return null due to break
        assertEquals(2, deque.size()); // Deque should still have elements
    }

    /// Tests the GET_LAST_ALWAYS_THROWS break functionality.
    @Test
    @DisplayName("Test GET_LAST_ALWAYS_THROWS break")
    public void testGetLastAlwaysThrows() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(GET_LAST_ALWAYS_THROWS)
                .build();

        deque.addLast("item");

        assertThrows(NoSuchElementException.class, deque::getLast);
    }

    /// Tests the GET_LAST_SKIPS_LAST_ELEMENT break functionality.
    @Test
    @DisplayName("Test GET_LAST_SKIPS_LAST_ELEMENT break")
    public void testGetLastSkipsLastElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(GET_LAST_SKIPS_LAST_ELEMENT)
                .build();

        deque.addFirst("item1");
        deque.addLast("item2");
        deque.addLast("item3");

        // Should return second-to-last element (item2) instead of last (item3)
        assertEquals("item2", deque.getLast());
    }

    /// Tests GET_LAST_SKIPS_LAST_ELEMENT break with insufficient elements.
    @Test
    @DisplayName("Test GET_LAST_SKIPS_LAST_ELEMENT break with one element")
    public void testGetLastSkipsLastElementWithOneElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(GET_LAST_SKIPS_LAST_ELEMENT)
                .build();

        deque.addLast("item1");

        // Should throw since there's no second-to-last element
        assertThrows(NoSuchElementException.class, deque::getLast);
    }

    // ========== AddFirst Method Break Tests ==========

    /// Tests the ADD_FIRST_DOES_NOT_ADD_ELEMENT break functionality.
    @Test
    @DisplayName("Test ADD_FIRST_DOES_NOT_ADD_ELEMENT break")
    public void testAddFirstDoesNotAddElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(ADD_FIRST_DOES_NOT_ADD_ELEMENT)
                .build();

        deque.addFirst("item1");
        deque.addFirst("item2");

        // Deque should remain empty despite addFirst calls
        assertEquals(0, deque.size());
        assertTrue(deque.isEmpty());
    }

    /// Tests the ADD_FIRST_ADDS_TO_END break functionality.
    @Test
    @DisplayName("Test ADD_FIRST_ADDS_TO_END break")
    public void testAddFirstAddsToEnd() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(ADD_FIRST_ADDS_TO_END)
                .build();

        deque.addFirst("item1");
        deque.addFirst("item2");
        deque.addFirst("item3");

        // Elements should be at the end, not the beginning
        assertEquals(3, deque.size());
        assertEquals("item1", deque.getFirst()); // First added should be at front
        assertEquals("item3", deque.getLast()); // Last added should be at back
    }

    // ========== AddLast Method Break Tests ==========

    /// Tests the ADD_LAST_DOES_NOT_ADD_ELEMENT break functionality.
    @Test
    @DisplayName("Test ADD_LAST_DOES_NOT_ADD_ELEMENT break")
    public void testAddLastDoesNotAddElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(ADD_LAST_DOES_NOT_ADD_ELEMENT)
                .build();

        deque.addLast("item1");
        deque.addLast("item2");

        // Deque should remain empty despite addLast calls
        assertEquals(0, deque.size());
        assertTrue(deque.isEmpty());
    }

    /// Tests the ADD_LAST_ADDS_TO_FRONT break functionality.
    @Test
    @DisplayName("Test ADD_LAST_ADDS_TO_FRONT break")
    public void testAddLastAddsToFront() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(ADD_LAST_ADDS_TO_FRONT)
                .build();

        deque.addLast("item1");
        deque.addLast("item2");
        deque.addLast("item3");

        // Elements should be at the front, not the end
        assertEquals(3, deque.size());
        assertEquals("item3", deque.getFirst()); // Last added should be at front
        assertEquals("item1", deque.getLast()); // First added should be at back
    }

    // ========== RemoveFirst Method Break Tests ==========

    /// Tests the REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT break functionality.
    @Test
    @DisplayName("Test REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT break")
    public void testRemoveFirstDoesNotRemoveElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT)
                .build();

        deque.addFirst("item1");
        deque.addLast("item2");
        deque.addLast("item3");

        // Should return first element
        String removed = deque.removeFirst();
        assertEquals("item1", removed);

        // But element should not be removed
        assertEquals(3, deque.size());
        assertEquals("item1", deque.getFirst()); // First should still be there

        // Calling removeFirst again should return same element
        String removedAgain = deque.removeFirst();
        assertEquals("item1", removedAgain);
        assertEquals(3, deque.size());
    }

    /// Tests the REMOVE_FIRST_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test REMOVE_FIRST_RETURNS_NULL break")
    public void testRemoveFirstReturnsNull() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(REMOVE_FIRST_RETURNS_NULL)
                .build();

        deque.addFirst("item1");
        deque.addFirst("item2");

        // Should return null due to break
        assertNull(deque.removeFirst());

        // Deque should still have elements
        assertEquals(2, deque.size());
    }

    /// Tests the REMOVE_FIRST_ALWAYS_THROWS break functionality.
    @Test
    @DisplayName("Test REMOVE_FIRST_ALWAYS_THROWS break")
    public void testRemoveFirstAlwaysThrows() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(REMOVE_FIRST_ALWAYS_THROWS)
                .build();

        deque.addFirst("item1");
        deque.addFirst("item2");

        // Should throw even though deque has elements
        assertThrows(NoSuchElementException.class, deque::removeFirst);

        // Verify queue state is unchanged
        assertEquals(2, deque.size());
    }

    // ========== RemoveLast Method Break Tests ==========

    /// Tests the REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT break functionality.
    @Test
    @DisplayName("Test REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT break")
    public void testRemoveLastDoesNotRemoveElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT)
                .build();

        deque.addFirst("item1");
        deque.addLast("item2");
        deque.addLast("item3");

        // Should return last element
        String removed = deque.removeLast();
        assertEquals("item3", removed);

        // But element should not be removed
        assertEquals(3, deque.size());
        assertEquals("item3", deque.getLast()); // Last should still be there

        // Calling removeLast again should return same element
        String removedAgain = deque.removeLast();
        assertEquals("item3", removedAgain);
        assertEquals(3, deque.size());
    }

    /// Tests the REMOVE_LAST_RETURNS_NULL break functionality.
    @Test
    @DisplayName("Test REMOVE_LAST_RETURNS_NULL break")
    public void testRemoveLastReturnsNull() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(REMOVE_LAST_RETURNS_NULL)
                .build();

        deque.addLast("item1");
        deque.addLast("item2");

        // Should return null due to break
        assertNull(deque.removeLast());

        // Deque should still have elements
        assertEquals(2, deque.size());
    }

    /// Tests the REMOVE_LAST_ALWAYS_THROWS break functionality.
    @Test
    @DisplayName("Test REMOVE_LAST_ALWAYS_THROWS break")
    public void testRemoveLastAlwaysThrows() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(REMOVE_LAST_ALWAYS_THROWS)
                .build();

        deque.addLast("item1");
        deque.addLast("item2");

        // Should throw even though deque has elements
        assertThrows(NoSuchElementException.class, deque::removeLast);

        // Verify queue state is unchanged
        assertEquals(2, deque.size());
    }

    // ========== Integration Tests for New Breaks ==========

    /// Tests interaction between ADD_FIRST_ADDS_TO_END and GET_FIRST.
    @Test
    @DisplayName("Test ADD_FIRST_ADDS_TO_END with getFirst")
    public void testAddFirstAddsToEndWithGetFirst() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(ADD_FIRST_ADDS_TO_END)
                .build();

        deque.addFirst("first");
        deque.addFirst("second");

        // "first" was added first, so it's at the front
        // "second" was added second, but went to the end due to break
        assertEquals("first", deque.getFirst());
        assertEquals("second", deque.getLast());
    }

    /// Tests interaction between REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT and normal poll.
    @Test
    @DisplayName("Test REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT with pollFirst")
    public void testRemoveFirstDoesNotRemoveElementWithPoll() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT)
                .build();

        deque.addFirst("item1");
        deque.addLast("item2");

        // removeFirst() should not remove element
        String removed = deque.removeFirst();
        assertEquals("item1", removed);
        assertEquals(2, deque.size());

        // But pollFirst() should still work normally
        String polled = deque.pollFirst();
        assertEquals("item1", polled);
        assertEquals(1, deque.size());
        assertEquals("item2", deque.getFirst());
    }

    /// Tests multiple breaks applied to deque methods.
    @Test
    @DisplayName("Test multiple new breaks together")
    public void testMultipleNewBreaksTogether() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(GET_FIRST_RETURNS_NULL)
                .addBreak(GET_LAST_RETURNS_NULL)
                .addBreak(ADD_FIRST_ADDS_TO_END)
                .build();

        deque.addFirst("item1");
        deque.addLast("item2");

        // getFirst and getLast should return null due to breaks
        assertNull(deque.getFirst());
        assertNull(deque.getLast());

        // But deque should have elements
        assertEquals(2, deque.size());
        assertFalse(deque.isEmpty());
    }
}