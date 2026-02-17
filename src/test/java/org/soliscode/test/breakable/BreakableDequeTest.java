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
    @DisplayName("builder() creates a deque with specified breaks and normal behavior")
    public void builder_whenUsed_configuresBreaks() {
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
    @DisplayName("builder(Deque) creates a deque with existing elements and specified breaks")
    public void builder_withExistingDeque_configuresBreaksAndElements() {
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
    @DisplayName("builder.copy() creates an independent builder with same configuration")
    public void builder_copy_createsIndependentBuilder() {
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
    /// to return `false` even when the element is successfully added to the underlying deque.
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

    /// Tests the [BreakableDeque#OFFER_FIRST_DOES_NOT_ADD_ELEMENT] break functionality.
    ///
    /// Verifies that [BreakableDeque#offerFirst] returns `true` but does not actually add the
    /// element to the deque when this break is applied.
    @Test
    @DisplayName("offerFirst(Object) does not add_singleElement_returnsTrueAndUpdatesSize element when OFFER_FIRST_DOES_NOT_ADD_ELEMENT break is applied")
    public void offerFirst_whenOfferFirstDoesNotAddElementBreakIsApplied_doesNotAddElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(OFFER_FIRST_DOES_NOT_ADD_ELEMENT)
                .build();

        assertTrue(deque.offerFirst("item1")); // Should return true
        assertIsEmpty(deque); // But deque should still be empty
    }

    /// Verifies that [BreakableDeque#offerFirst] throws [UnsupportedOperationException]
    /// when the method is configured as not supported.
    @Test
    @DisplayName("offerFirst(Object) throws UnsupportedOperationException when it is not supported")
    public void offerFirst_whenItIsNotSuppoerted_throwsUnsupportedOperationException() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .doesNotSupport(DequeMethods.OFFER_FIRST)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> deque.offerFirst("item1"));
    }

    /// Tests the [BreakableDeque#POLL_FIRST_ALWAYS_RETURNS_NULL] break functionality.
    @Test
    @DisplayName("pollFirst() always returns null when POLL_FIRST_ALWAYS_RETURNS_NULL break is applied")
    public void pollFirst_whenPollFirstAlwaysReturnsNullBreakIsApplied_returnsNull() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(POLL_FIRST_ALWAYS_RETURNS_NULL)
                .addElements("item1", "item2")
                .build();

        assertNull(deque.pollFirst()); // Should return null due to break
        assertFalse(deque.isEmpty()); // Deque should not be empty
    }

    /// Tests the [BreakableDeque#POLL_FIRST_DOES_NOT_REMOVE_ELEMENT] break functionality.
    @Test
    @DisplayName("pollFirst() returns head but does not remove it when POLL_FIRST_DOES_NOT_REMOVE_ELEMENT break is applied")
    public void pollFirst_whenPollFirstDoesNotRemoveElementBreakIsApplied_returnsHeadButDoesNotRemove() {
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


    /// Verifies that [BreakableDeque#pollFirst] throws [UnsupportedOperationException]
    /// when the method is configured as not supported.
    @Test
    @DisplayName("pollFirst() throws UnsupportedOperationException when it is not supported")
    public void pollFirst_whenItIsNotSuppoerted_throwsUnsupportedOperationException() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .doesNotSupport(DequeMethods.POLL_FIRST)
                .build();

        deque.offerFirst("1");

        assertThrows(UnsupportedOperationException.class, deque::pollFirst);
    }

    /// Tests the [BreakableDeque#PEEK_FIRST_ALWAYS_RETURNS_NULL] break functionality.
    @Test
    @DisplayName("peekFirst() always returns null when PEEK_FIRST_ALWAYS_RETURNS_NULL break is applied")
    public void peekFirst_whenPeekFirstAlwaysReturnsNullBreakIsApplied_returnsNull() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(PEEK_FIRST_ALWAYS_RETURNS_NULL)
                .build();

        deque.offerFirst("item");
        assertNull(deque.peekFirst()); // Should return null due to break

        // Deque should not be empty
        assertFalse(deque.isEmpty());
    }

    /// Tests the [BreakableDeque#PEEK_FIRST_RETURNS_RANDOM_ELEMENT] break functionality.
    @Test
    @DisplayName("peekFirst() returns random element when PEEK_FIRST_RETURNS_RANDOM_ELEMENT break is applied")
    public void peekFirst_whenPeekFirstReturnsRandomElementBreakIsApplied_returnsRandomElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(PEEK_FIRST_RETURNS_RANDOM_ELEMENT)
                .addElements("item", "item2", "item3")
                .build();

        String peeked = deque.peekFirst(); // For deque, this should be the last item offered: "item3"
        assertNotNull(peeked);
        assertNotEquals("item3", peeked); // Should return different element due to break
    }

    /// Verifies that [BreakableDeque#peekFirst] throws [UnsupportedOperationException]
    /// when the method is configured as not supported.
    @Test
    @DisplayName("peekFirst() throws UnsupportedOperationException when it is not supported")
    public void peekFirst_whenItIsNotSuppoerted_throwsUnsupportedOperationException() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .doesNotSupport(DequeMethods.PEEK_FIRST)
                .addElements("item1")
                .build();

        assertThrows(UnsupportedOperationException.class, deque::peekFirst);
    }

    /// Tests the [BreakableDeque#OFFER_LAST_ALWAYS_RETURNS_FALSE] break functionality.
    ///
    /// Verifies that [BreakableDeque#offerLast] returns `false` even when the element
    /// is successfully added to the underlying deque.
    @Test
    @DisplayName("offerLast() always returns false when OFFER_LAST_ALWAYS_RETURNS_FALSE break is applied")
    public void offerLast_whenOfferLastAlwaysReturnsFalseBreakIsApplied_returnsFalse() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(OFFER_LAST_ALWAYS_RETURNS_FALSE)
                .build();

        assertFalse(deque.offerLast("item1"));
        assertFalse(deque.offerLast("item2"));

        // Elements should still be added despite false return
        assertFalse(deque.isEmpty());
    }

    /// Tests the [BreakableDeque#OFFER_LAST_DOES_NOT_ADD_ELEMENT] break functionality.
    ///
    /// Verifies that [BreakableDeque#offerLast] returns `true` but does not actually add the
    /// element to the deque when this break is applied.
    @Test
    @DisplayName("offerLast() returns true but does not add element when OFFER_LAST_DOES_NOT_ADD_ELEMENT break is applied")
    public void offerLast_whenOfferLastDoesNotAddElementBreakIsApplied_doesNotAddElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(OFFER_LAST_DOES_NOT_ADD_ELEMENT)
                .build();

        assertTrue(deque.offerLast("item1")); // Should return true
        assertTrue(deque.offerLast("item2")); // Should return true

        // But deque should remain empty
        assertEquals(0, deque.size());
        assertTrue(deque.isEmpty());
    }

    /// Tests the [BreakableDeque#POLL_LAST_ALWAYS_RETURNS_NULL] break functionality.
    ///
    /// Verifies that [BreakableDeque#pollLast] always returns `null` regardless of deque content.
    @Test
    @DisplayName("pollLast() always returns null when POLL_LAST_ALWAYS_RETURNS_NULL break is applied")
    public void pollLast_whenPollLastAlwaysReturnsNullBreakIsApplied_returnsNull() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(POLL_LAST_ALWAYS_RETURNS_NULL)
                .build();

        deque.offerLast("item1");
        deque.offerLast("item2");

        assertNull(deque.pollLast()); // Should return null due to break
        assertFalse(deque.isEmpty()); // Deque should not be empty
    }

    /// Tests the [BreakableDeque#POLL_LAST_DOES_NOT_REMOVE_ELEMENT] break functionality.
    ///
    /// Verifies that [BreakableDeque#pollLast] returns the tail element but does not
    /// remove it from the deque.
    @Test
    @DisplayName("pollLast() returns tail but does not remove it when POLL_LAST_DOES_NOT_REMOVE_ELEMENT break is applied")
    public void pollLast_whenPollLastDoesNotRemoveElementBreakIsApplied_returnsTailButDoesNotRemove() {
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

    /// Tests the [BreakableDeque#PEEK_LAST_ALWAYS_RETURNS_NULL] break functionality.
    ///
    /// Verifies that [BreakableDeque#peekLast] always returns `null` regardless of deque content.
    @Test
    @DisplayName("peekLast() always returns null when PEEK_LAST_ALWAYS_RETURNS_NULL break is applied")
    public void peekLast_whenPeekLastAlwaysReturnsNullBreakIsApplied_returnsNull() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(PEEK_LAST_ALWAYS_RETURNS_NULL)
                .build();

        deque.offerLast("item");
        assertNull(deque.peekLast()); // Should return null due to break

        // Deque should not be empty
        assertFalse(deque.isEmpty());
    }

    /// Tests the [BreakableDeque#PEEK_LAST_RETURNS_RANDOM_ELEMENT] break functionality.
    ///
    /// Verifies that [BreakableDeque#peekLast] returns an arbitrary element from the
    /// deque instead of the actual tail element.
    @Test
    @DisplayName("peekLast() returns random element when PEEK_LAST_RETURNS_RANDOM_ELEMENT break is applied")
    public void peekLast_whenPeekLastReturnsRandomElementBreakIsApplied_returnsRandomElement() {
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

    /// Tests the [BreakableDeque#PUSH_THROWS_EXCEPTION] break functionality.
    ///
    /// Verifies that [BreakableDeque#push] throws [IllegalStateException] when this break is applied.
    @Test
    @DisplayName("push() throws IllegalStateException when PUSH_THROWS_EXCEPTION break is applied")
    public void push_whenPushThrowsExceptionBreakIsApplied_throwsIllegalStateException() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(PUSH_THROWS_EXCEPTION)
                .build();

        assertThrows(IllegalStateException.class, () -> deque.push("item"));
    }

    /// Tests the [BreakableDeque#PUSH_DOES_NOT_ADD_ELEMENT] break functionality.
    ///
    /// Verifies that [BreakableDeque#push] does not actually add the element to the deque
    /// when this break is applied.
    @Test
    @DisplayName("push() does not add element when PUSH_DOES_NOT_ADD_ELEMENT break is applied")
    public void push_whenPushDoesNotAddElementBreakIsApplied_doesNotAddElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(PUSH_DOES_NOT_ADD_ELEMENT)
                .build();

        deque.push("item1");
        deque.push("item2");

        // Deque should remain empty despite pushes
        assertEquals(0, deque.size());
        assertTrue(deque.isEmpty());
    }

    /// Tests the [BreakableDeque#POP_THROWS_EXCEPTION] break functionality.
    ///
    /// Verifies that [BreakableDeque#pop] throws [IllegalStateException] when this break is applied.
    @Test
    @DisplayName("pop() throws IllegalStateException when POP_THROWS_EXCEPTION break is applied")
    public void pop_whenPopThrowsExceptionBreakIsApplied_throwsIllegalStateException() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(POP_THROWS_EXCEPTION)
                .build();

        deque.push("item");
        assertThrows(IllegalStateException.class, deque::pop);
    }

    /// Tests the [BreakableDeque#POP_ALWAYS_RETURNS_NULL] break functionality.
    ///
    /// Verifies that [BreakableDeque#pop] always returns `null` regardless of deque content.
    @Test
    @DisplayName("pop() always returns null when POP_ALWAYS_RETURNS_NULL break is applied")
    public void pop_whenPopAlwaysReturnsNullBreakIsApplied_returnsNull() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(POP_ALWAYS_RETURNS_NULL)
                .build();

        deque.push("item1");
        deque.push("item2");

        assertNull(deque.pop()); // Should return null due to break
        assertEquals(2, deque.size()); // Elements should still be there
    }

    // ========== Occurrence Removal Tests ==========

    /// Tests the [BreakableDeque#REMOVE_FIRST_OCCURRENCE_ALWAYS_RETURNS_FALSE] break functionality.
    ///
    /// Verifies that [BreakableDeque#removeFirstOccurrence] returns `false` even if the
    /// element exists in the deque.
    @Test
    @DisplayName("removeFirstOccurrence() always returns false when REMOVE_FIRST_OCCURRENCE_ALWAYS_RETURNS_FALSE break is applied")
    public void removeFirstOccurrence_whenAlwaysReturnsFalseBreakIsApplied_returnsFalse() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(REMOVE_FIRST_OCCURRENCE_ALWAYS_RETURNS_FALSE)
                .build();

        deque.offerLast("item");
        deque.offerLast("item");
        deque.offerLast("other");

        assertFalse(deque.removeFirstOccurrence("item")); // Should return false due to break
        assertEquals(3, deque.size()); // Elements should still be there
    }

    /// Tests the [BreakableDeque#REMOVE_FIRST_OCCURRENCE_DOES_NOT_REMOVE] break functionality.
    ///
    /// Verifies that [BreakableDeque#removeFirstOccurrence] returns `true` but does not
    /// actually remove the element from the deque.
    @Test
    @DisplayName("removeFirstOccurrence() returns true but does not remove element when REMOVE_FIRST_OCCURRENCE_DOES_NOT_REMOVE break is applied")
    public void removeFirstOccurrence_whenDoesNotRemoveBreakIsApplied_doesNotRemoveElement() {
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

    /// Tests the [BreakableDeque#REMOVE_LAST_OCCURRENCE_ALWAYS_RETURNS_FALSE] break functionality.
    ///
    /// Verifies that [BreakableDeque#removeLastOccurrence] returns `false` even if the
    /// element exists in the deque.
    @Test
    @DisplayName("removeLastOccurrence() always returns false when REMOVE_LAST_OCCURRENCE_ALWAYS_RETURNS_FALSE break is applied")
    public void removeLastOccurrence_whenAlwaysReturnsFalseBreakIsApplied_returnsFalse() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(REMOVE_LAST_OCCURRENCE_ALWAYS_RETURNS_FALSE)
                .build();

        deque.offerLast("item");
        deque.offerLast("other");
        deque.offerLast("item");

        assertFalse(deque.removeLastOccurrence("item")); // Should return false due to break
        assertEquals(3, deque.size()); // Elements should still be there
    }

    /// Tests the [BreakableDeque#REMOVE_LAST_OCCURRENCE_DOES_NOT_REMOVE] break functionality.
    ///
    /// Verifies that [BreakableDeque#removeLastOccurrence] returns `true` but does not
    /// actually remove the element from the deque.
    @Test
    @DisplayName("removeLastOccurrence() returns true but does not remove element when REMOVE_LAST_OCCURRENCE_DOES_NOT_REMOVE break is applied")
    public void removeLastOccurrence_whenDoesNotRemoveBreakIsApplied_doesNotRemoveElement() {
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

    /// Tests the [BreakableDeque#DESCENDING_ITERATOR_THROWS_EXCEPTION] break functionality.
    ///
    /// Verifies that [BreakableDeque#descendingIterator] throws [IllegalStateException] when this break is applied.
    @Test
    @DisplayName("descendingIterator() throws IllegalStateException when DESCENDING_ITERATOR_THROWS_EXCEPTION break is applied")
    public void descendingIterator_whenThrowsExceptionBreakIsApplied_throwsIllegalStateException() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(DESCENDING_ITERATOR_THROWS_EXCEPTION)
                .build();

        assertThrows(IllegalStateException.class, deque::descendingIterator);
    }

    /// Tests the [BreakableDeque#DESCENDING_ITERATOR_RETURNS_EMPTY] break functionality.
    @Test
    @DisplayName("descendingIterator() returns empty iterator when DESCENDING_ITERATOR_RETURNS_EMPTY break is applied")
    public void descendingIterator_whenReturnsEmptyBreakIsApplied_returnsEmptyIterator() {
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
    @DisplayName("unsupported methods throw UnsupportedOperationException")
    public void unsupportedMethods_whenCalled_throwUnsupportedOperationException() {
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

    /// Tests the static [BreakableDeque#wrap(Deque, Set)] factory method functionality.
    @Test
    @DisplayName("wrap(Deque, Set) creates a breakable deque wrapping an existing one")
    public void wrap_withDequeAndBreaks_createsWrappedDeque() {
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

    /// Tests the static [BreakableDeque#wrap(Deque, Set, int)] factory method with characteristics.
    @Test
    @DisplayName("wrap(Deque, Set, int) creates a breakable deque with specified characteristics")
    public void wrap_withDequeBreaksAndCharacteristics_createsWrappedDeque() {
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

    /// Tests normal deque operations when no breaks are applied.
    @Test
    @DisplayName("deque performs normal operations correctly when no breaks are applied")
    public void deque_whenNoBreaks_performsNormalOperations() {
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

    /// Tests deque operations when multiple breaks are applied.
    @Test
    @DisplayName("multiple breaks correctly interact and apply to deque operations")
    public void multipleBreaks_whenApplied_correctlyInteract() {
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

    /// Tests inheritance from [BreakableQueue] and [BreakableCollection].
    @Test
    @DisplayName("breakable deque correctly inherits and applies breaks from queue and collection")
    public void inheritance_whenBreaksApplied_appliesBreaksFromAllLevels() {
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

    /// Tests the add(E) method inherited from [java.util.Collection].
    @Test
    @DisplayName("add(E) correctly adds element to the end of the deque")
    public void add_whenCalled_addsToEndOfDeque() {
        BreakableDeque<String> deque = new BreakableDeque<>();

        assertTrue(deque.add("item1"));
        assertTrue(deque.add("item2"));

        assertEquals(2, deque.size());
        assertEquals("item1", deque.peekFirst()); // FIFO ordering maintained
        assertEquals("item2", deque.peekLast());
    }

    /// Tests integration with [java.util.LinkedList].
    @Test
    @DisplayName("breakable deque correctly integrates with LinkedList and preserves its behavior")
    public void linkedListIntegration_whenUsed_preservesBehavior() {
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

    /// Tests basic occurrence removal operations.
    @Test
    @DisplayName("occurrence removal methods correctly remove specified elements")
    public void occurrenceRemoval_whenCalled_removesCorrectElements() {
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

    /// Tests basic descending iterator behavior.
    @Test
    @DisplayName("descendingIterator() provides elements in reverse order")
    public void descendingIterator_whenCalled_returnsElementsInReverse() {
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

    /// Tests LIFO stack behavior when using Deque as a stack.
    @Test
    @DisplayName("deque performs correct LIFO stack operations")
    public void stackBehavior_whenUsedAsStack_performsLIFOOperations() {
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

    /// Tests the interaction and priority between different breaks.
    @Test
    @DisplayName("higher priority breaks correctly override or interact with lower priority ones")
    public void breakPriority_whenMultipleBreaksConflict_respectsPriority() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(PUSH_THROWS_EXCEPTION) // Should throw before other breaks
                .addBreak(PUSH_DOES_NOT_ADD_ELEMENT)
                .build();

        // Exception break should take priority
        assertThrows(IllegalStateException.class, () -> deque.push("item"));
    }

    /// Tests that deque state remains consistent across various operations.
    @Test
    @DisplayName("deque maintains consistent internal state across mixed operations")
    public void stateConsistency_acrossMultipleOperations_remainsConsistent() {
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

    /// Tests the [BreakableDeque#GET_FIRST_RETURNS_NULL] break functionality.
    ///
    /// Verifies that [BreakableDeque#getFirst] always returns `null` regardless of deque content.
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

    /// Tests the [BreakableDeque#GET_FIRST_ALWAYS_THROWS] break functionality.
    @Test
    @DisplayName("getFirst() always throws NoSuchElementException when GET_FIRST_ALWAYS_THROWS break is applied")
    public void getFirst_whenAlwaysThrowsBreakIsApplied_throwsNoSuchElementException() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(GET_FIRST_ALWAYS_THROWS)
                .build();

        deque.addFirst("item");

        assertThrows(NoSuchElementException.class, deque::getFirst);
    }

    /// Tests the [BreakableDeque#GET_FIRST_SKIPS_FIRST_ELEMENT] break functionality.
    @Test
    @DisplayName("getFirst() skips first element and returns second when GET_FIRST_SKIPS_FIRST_ELEMENT break is applied")
    public void getFirst_whenSkipsFirstElementBreakIsApplied_returnsSecondElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(GET_FIRST_SKIPS_FIRST_ELEMENT)
                .build();

        deque.addFirst("item1");
        deque.addLast("item2");
        deque.addLast("item3");

        // Should return second element (item2) instead of first (item1)
        assertEquals("item2", deque.getFirst());
    }

    /// Tests the [BreakableDeque#GET_FIRST_SKIPS_FIRST_ELEMENT] break functionality with a single element.
    @Test
    @DisplayName("getFirst() throws NoSuchElementException when GET_FIRST_SKIPS_FIRST_ELEMENT break is applied on deque with one element")
    public void getFirst_whenSkipsFirstElementBreakIsAppliedOnOneElement_throwsNoSuchElementException() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(GET_FIRST_SKIPS_FIRST_ELEMENT)
                .build();

        deque.addFirst("item1");

        // Should throw since there's no second element
        assertThrows(NoSuchElementException.class, deque::getFirst);
    }

    // ========== GetLast Method Break Tests ==========

    /// Tests the [BreakableDeque#GET_LAST_RETURNS_NULL] break functionality.
    ///
    /// Verifies that [BreakableDeque#getLast] always returns `null` regardless of deque content.
    @Test
    @DisplayName("getLast() returns null when GET_LAST_RETURNS_NULL break is applied")
    public void getLast_whenReturnsNullBreakIsApplied_returnsNull() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(GET_LAST_RETURNS_NULL)
                .build();

        deque.addLast("item1");
        deque.addLast("item2");

        assertNull(deque.getLast()); // Should return null due to break
        assertEquals(2, deque.size()); // Deque should still have elements
    }

    /// Tests the [BreakableDeque#GET_LAST_ALWAYS_THROWS] break functionality.
    @Test
    @DisplayName("getLast() always throws NoSuchElementException when GET_LAST_ALWAYS_THROWS break is applied")
    public void getLast_whenAlwaysThrowsBreakIsApplied_throwsNoSuchElementException() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(GET_LAST_ALWAYS_THROWS)
                .build();

        deque.addLast("item");

        assertThrows(NoSuchElementException.class, deque::getLast);
    }

    /// Tests the [BreakableDeque#GET_LAST_SKIPS_LAST_ELEMENT] break functionality.
    @Test
    @DisplayName("getLast() skips last element and returns second-to-last when GET_LAST_SKIPS_LAST_ELEMENT break is applied")
    public void getLast_whenSkipsLastElementBreakIsApplied_returnsSecondToLastElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(GET_LAST_SKIPS_LAST_ELEMENT)
                .build();

        deque.addFirst("item1");
        deque.addLast("item2");
        deque.addLast("item3");

        // Should return second-to-last element (item2) instead of last (item3)
        assertEquals("item2", deque.getLast());
    }

    /// Tests the [BreakableDeque#GET_LAST_SKIPS_LAST_ELEMENT] break functionality with a single element.
    @Test
    @DisplayName("getLast() throws NoSuchElementException when GET_LAST_SKIPS_LAST_ELEMENT break is applied on deque with one element")
    public void getLast_whenSkipsLastElementBreakIsAppliedOnOneElement_throwsNoSuchElementException() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(GET_LAST_SKIPS_LAST_ELEMENT)
                .build();

        deque.addLast("item1");

        // Should throw since there's no second-to-last element
        assertThrows(NoSuchElementException.class, deque::getLast);
    }

    // ========== AddFirst Method Break Tests ==========

    /// Tests the [BreakableDeque#ADD_FIRST_DOES_NOT_ADD_ELEMENT] break functionality.
    ///
    /// Verifies that [BreakableDeque#addFirst] does not actually add the element to the
    /// deque when this break is applied.
    @Test
    @DisplayName("addFirst() does not add element when ADD_FIRST_DOES_NOT_ADD_ELEMENT break is applied")
    public void addFirst_whenDoesNotAddElementBreakIsApplied_doesNotAddElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(ADD_FIRST_DOES_NOT_ADD_ELEMENT)
                .build();

        deque.addFirst("item1");
        deque.addFirst("item2");

        // Deque should remain empty despite addFirst calls
        assertEquals(0, deque.size());
        assertTrue(deque.isEmpty());
    }

    /// Tests the [BreakableDeque#ADD_FIRST_ADDS_TO_END] break functionality.
    @Test
    @DisplayName("addFirst() adds to the end of the deque when ADD_FIRST_ADDS_TO_END break is applied")
    public void addFirst_whenAddsToEndBreakIsApplied_addsToEnd() {
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

    /// Tests the [BreakableDeque#ADD_LAST_DOES_NOT_ADD_ELEMENT] break functionality.
    ///
    /// Verifies that [BreakableDeque#addLast] does not actually add the element to the
    /// deque when this break is applied.
    @Test
    @DisplayName("addLast() does not add element when ADD_LAST_DOES_NOT_ADD_ELEMENT break is applied")
    public void addLast_whenDoesNotAddElementBreakIsApplied_doesNotAddElement() {
        BreakableDeque<String> deque = BreakableDeque.<String>builder()
                .addBreak(ADD_LAST_DOES_NOT_ADD_ELEMENT)
                .build();

        deque.addLast("item1");
        deque.addLast("item2");

        // Deque should remain empty despite addLast calls
        assertEquals(0, deque.size());
        assertTrue(deque.isEmpty());
    }

    /// Tests the [BreakableDeque#ADD_LAST_ADDS_TO_FRONT] break functionality.
    @Test
    @DisplayName("addLast() adds to the front of the deque when ADD_LAST_ADDS_TO_FRONT break is applied")
    public void addLast_whenAddsToFrontBreakIsApplied_addsToFront() {
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

    /// Tests the [BreakableDeque#REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT] break functionality.
    ///
    /// Verifies that [BreakableDeque#removeFirst] returns the head element but does
    /// not remove it from the deque.
    @Test
    @DisplayName("removeFirst() returns head but does not remove it when REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT break is applied")
    public void removeFirst_whenDoesNotRemoveBreakIsApplied_returnsHeadButDoesNotRemove() {
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

    /// Tests the [BreakableDeque#REMOVE_FIRST_RETURNS_NULL] break functionality.
    ///
    /// Verifies that [BreakableDeque#removeFirst] always returns `null` regardless of deque content.
    @Test
    @DisplayName("removeFirst() returns null when REMOVE_FIRST_RETURNS_NULL break is applied")
    public void removeFirst_whenReturnsNullBreakIsApplied_returnsNull() {
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

    /// Tests the [BreakableDeque#REMOVE_FIRST_ALWAYS_THROWS] break functionality.
    ///
    /// Verifies that [BreakableDeque#removeFirst] throws [NoSuchElementException]
    /// even when the deque is not empty.
    @Test
    @DisplayName("removeFirst() always throws NoSuchElementException when REMOVE_FIRST_ALWAYS_THROWS break is applied")
    public void removeFirst_whenAlwaysThrowsBreakIsApplied_throwsNoSuchElementException() {
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

    /// Tests the [BreakableDeque#REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT] break functionality.
    ///
    /// Verifies that [BreakableDeque#removeLast] returns the tail element but does
    /// not remove it from the deque.
    @Test
    @DisplayName("removeLast() returns tail but does not remove it when REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT break is applied")
    public void removeLast_whenDoesNotRemoveBreakIsApplied_returnsTailButDoesNotRemove() {
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

    /// Tests the [BreakableDeque#REMOVE_LAST_RETURNS_NULL] break functionality.
    ///
    /// Verifies that [BreakableDeque#removeLast] always returns `null` regardless of deque content.
    @Test
    @DisplayName("removeLast() returns null when REMOVE_LAST_RETURNS_NULL break is applied")
    public void removeLast_whenReturnsNullBreakIsApplied_returnsNull() {
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

    /// Tests the [BreakableDeque#REMOVE_LAST_ALWAYS_THROWS] break functionality.
    ///
    /// Verifies that [BreakableDeque#removeLast] throws [NoSuchElementException]
    /// even when the deque is not empty.
    @Test
    @DisplayName("removeLast() always throws NoSuchElementException when REMOVE_LAST_ALWAYS_THROWS break is applied")
    public void removeLast_whenAlwaysThrowsBreakIsApplied_throwsNoSuchElementException() {
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

    /// Tests the interaction between [BreakableDeque#ADD_FIRST_ADDS_TO_END] and [BreakableDeque#getFirst].
    @Test
    @DisplayName("addFirst(E) adds to end and getFirst() returns current head when ADD_FIRST_ADDS_TO_END is applied")
    public void addFirst_whenAddsToEndWithGetFirst_respectsBothBehaviors() {
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

    /// Tests the interaction between [BreakableDeque#REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT] and [BreakableDeque#pollFirst].
    @Test
    @DisplayName("removeFirst() does not remove element but pollFirst() still works normally when break is applied")
    public void removeFirst_whenDoesNotRemoveWithPollFirst_respectsBothBehaviors() {
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

    /// Tests multiple concurrent breaks on various deque methods.
    @Test
    @DisplayName("multiple breaks correctly apply to mixed deque operations")
    public void multipleBreaks_whenAppliedTogether_correctlyApplyToOperations() {
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