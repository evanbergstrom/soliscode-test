package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.blockingdeque.BlockingDequeMethods;

import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.soliscode.test.breakable.BreakableBlockingDeque.*;

/// Comprehensive test suite for BreakableBlockingDeque functionality.
///
/// This test class validates all aspects of the BreakableBlockingDeque implementation,
/// including break constants, normal operations, error handling, and builder patterns.
/// The tests ensure that the BreakableBlockingDeque correctly implements both the
/// BlockingDeque interface and the Break mechanism.
///
/// ## Test Categories
///
/// ### Break Testing
/// - Tests for all 16 break constants covering blocking operations
/// - Verification of break behavior for putFirst/putLast operations
/// - Testing break behavior for takeFirst/takeLast operations
/// - Timeout operation break testing
/// - InterruptedException handling
///
/// ### Normal Operations
/// - Basic BlockingDeque functionality
/// - Integration with LinkedBlockingDeque
/// - Thread safety verification
/// - Capacity and blocking behavior
///
/// ### Builder and Factory Methods
/// - Builder pattern functionality
/// - Copy constructor behavior
/// - Static factory methods
/// - Configuration inheritance
///
/// ### Error Handling
/// - UnsupportedOperationException testing
/// - Method support configuration
/// - Exception propagation
///
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableBlockingDeque
/// @see AbstractTest
public class BreakableBlockingDequeTest extends AbstractTest {

    // ========== Break Testing: Put Operations ==========

    @Test
    @DisplayName("Test PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION break")
    public void testPutFirstThrowsInterruptedException() {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        assertThrows(InterruptedException.class, () -> deque.putFirst("item1"));
    }

    @Test
    @DisplayName("Test PUT_FIRST_DOES_NOT_ADD_ELEMENT break")
    public void testPutFirstDoesNotAddElement() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(PUT_FIRST_DOES_NOT_ADD_ELEMENT);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putFirst("item1");
        assertTrue(deque.isEmpty());
    }

    @Test
    @DisplayName("Test PUT_FIRST_THROWS_EXCEPTION break")
    public void testPutFirstThrowsException() {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(PUT_FIRST_THROWS_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        assertThrows(IllegalStateException.class, () -> deque.putFirst("item1"));
    }

    @Test
    @DisplayName("Test PUT_LAST_THROWS_INTERRUPTED_EXCEPTION break")
    public void testPutLastThrowsInterruptedException() {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(PUT_LAST_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        assertThrows(InterruptedException.class, () -> deque.putLast("item1"));
    }

    @Test
    @DisplayName("Test PUT_LAST_DOES_NOT_ADD_ELEMENT break")
    public void testPutLastDoesNotAddElement() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(PUT_LAST_DOES_NOT_ADD_ELEMENT);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putLast("item1");
        assertTrue(deque.isEmpty());
    }

    @Test
    @DisplayName("Test PUT_LAST_THROWS_EXCEPTION break")
    public void testPutLastThrowsException() {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(PUT_LAST_THROWS_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        assertThrows(IllegalStateException.class, () -> deque.putLast("item1"));
    }

    // ========== Break Testing: Take Operations ==========

    @Test
    @DisplayName("Test TAKE_FIRST_THROWS_INTERRUPTED_EXCEPTION break")
    public void testTakeFirstThrowsInterruptedException() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(TAKE_FIRST_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putFirst("item1");
        assertThrows(InterruptedException.class, deque::takeFirst);
    }

    @Test
    @DisplayName("Test TAKE_FIRST_ALWAYS_RETURNS_NULL break")
    public void testTakeFirstAlwaysReturnsNull() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(TAKE_FIRST_ALWAYS_RETURNS_NULL);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putFirst("item1");
        assertNull(deque.takeFirst());
    }

    @Test
    @DisplayName("Test TAKE_FIRST_DOES_NOT_REMOVE_ELEMENT break")
    public void testTakeFirstDoesNotRemoveElement() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(TAKE_FIRST_DOES_NOT_REMOVE_ELEMENT);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putFirst("item1");
        assertEquals("item1", deque.takeFirst());
        assertEquals(1, deque.size()); // Element should still be there
    }

    @Test
    @DisplayName("Test TAKE_LAST_THROWS_INTERRUPTED_EXCEPTION break")
    public void testTakeLastThrowsInterruptedException() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(TAKE_LAST_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putLast("item1");
        assertThrows(InterruptedException.class, deque::takeLast);
    }

    @Test
    @DisplayName("Test TAKE_LAST_ALWAYS_RETURNS_NULL break")
    public void testTakeLastAlwaysReturnsNull() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(TAKE_LAST_ALWAYS_RETURNS_NULL);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putLast("item1");
        assertNull(deque.takeLast());
    }

    @Test
    @DisplayName("Test TAKE_LAST_DOES_NOT_REMOVE_ELEMENT break")
    public void testTakeLastDoesNotRemoveElement() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(TAKE_LAST_DOES_NOT_REMOVE_ELEMENT);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putLast("item1");
        assertEquals("item1", deque.takeLast());
        assertEquals(1, deque.size()); // Element should still be there
    }

    // ========== Break Testing: Timeout Operations ==========

    @Test
    @DisplayName("Test OFFER_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE break")
    public void testOfferFirstWithTimeoutAlwaysReturnsFalse() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(OFFER_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE);
        BreakableBlockingDeque<String> deque = builder.build();

        assertFalse(deque.offerFirst("item1", 1, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break")
    public void testOfferFirstWithTimeoutThrowsInterruptedException() {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        assertThrows(InterruptedException.class, () ->
            deque.offerFirst("item1", 1, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test OFFER_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE break")
    public void testOfferLastWithTimeoutAlwaysReturnsFalse() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(OFFER_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE);
        BreakableBlockingDeque<String> deque = builder.build();

        assertFalse(deque.offerLast("item1", 1, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test OFFER_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break")
    public void testOfferLastWithTimeoutThrowsInterruptedException() {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(OFFER_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        assertThrows(InterruptedException.class, () ->
            deque.offerLast("item1", 1, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test POLL_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL break")
    public void testPollFirstWithTimeoutAlwaysReturnsNull() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(POLL_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putFirst("item1");
        assertNull(deque.pollFirst(1, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test POLL_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break")
    public void testPollFirstWithTimeoutThrowsInterruptedException() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(POLL_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putFirst("item1");
        assertThrows(InterruptedException.class, () ->
            deque.pollFirst(1, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test POLL_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL break")
    public void testPollLastWithTimeoutAlwaysReturnsNull() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(POLL_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putLast("item1");
        assertNull(deque.pollLast(1, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test POLL_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break")
    public void testPollLastWithTimeoutThrowsInterruptedException() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(POLL_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putLast("item1");
        assertThrows(InterruptedException.class, () ->
            deque.pollLast(1, TimeUnit.SECONDS));
    }

    // ========== Normal Operations Testing ==========

    @Test
    @DisplayName("Test normal blocking deque operations")
    public void testNormalBlockingDequeOperations() throws InterruptedException {
        BreakableBlockingDeque<String> deque = new BreakableBlockingDeque<>();

        // Test basic put/take operations
        deque.putFirst("first");
        deque.putLast("last");

        assertEquals("first", deque.takeFirst());
        assertEquals("last", deque.takeLast());
        assertTrue(deque.isEmpty());
    }

    @Test
    @DisplayName("Test timeout operations without breaks")
    public void testTimeoutOperationsWithoutBreaks() throws InterruptedException {
        BreakableBlockingDeque<String> deque = new BreakableBlockingDeque<>();

        // Test timeout operations
        assertTrue(deque.offerFirst("first", 1, TimeUnit.SECONDS));
        assertTrue(deque.offerLast("last", 1, TimeUnit.SECONDS));

        assertEquals("first", deque.pollFirst(1, TimeUnit.SECONDS));
        assertEquals("last", deque.pollLast(1, TimeUnit.SECONDS));
        assertNull(deque.pollFirst(100, TimeUnit.MILLISECONDS)); // Empty deque
    }

    @Test
    @DisplayName("Test inheritance from BreakableDeque")
    public void testInheritanceFromBreakableDeque() throws InterruptedException {
        BreakableBlockingDeque<String> deque = new BreakableBlockingDeque<>();

        // Test inherited Deque operations
        deque.addFirst("first");
        deque.addLast("last");
        deque.push("stack-top");

        assertEquals("stack-top", deque.peekFirst());
        assertEquals("last", deque.peekLast());
        assertEquals("stack-top", deque.pop());
    }

    // ========== Builder and Factory Testing ==========

    @Test
    @DisplayName("Test builder pattern")
    public void testBuilderPattern() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION);
        builder.addBreak(TAKE_LAST_ALWAYS_RETURNS_NULL);
        BreakableBlockingDeque<String> deque = builder.build();

        assertThrows(InterruptedException.class, () -> deque.putFirst("item1"));
        deque.putLast("item1");
        assertNull(deque.takeLast());
    }

    @Test
    @DisplayName("Test builder with existing BlockingDeque")
    public void testBuilderWithExistingBlockingDeque() throws InterruptedException {
        LinkedBlockingDeque<String> linkedDeque = new LinkedBlockingDeque<>();
        linkedDeque.putFirst("existing");

        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>(linkedDeque);
        builder.addBreak(TAKE_FIRST_DOES_NOT_REMOVE_ELEMENT);
        BreakableBlockingDeque<String> deque = builder.build();

        assertEquals("existing", deque.takeFirst());
        assertEquals(1, deque.size()); // Element should still be there due to break
    }

    @Test
    @DisplayName("Test copy constructor")
    public void testCopyConstructor() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> originalBuilder = new BreakableBlockingDeque.Builder<>();
        originalBuilder.addBreak(PUT_FIRST_DOES_NOT_ADD_ELEMENT);
        BreakableBlockingDeque<String> original = originalBuilder.build();

        BreakableBlockingDeque<String> copy = new BreakableBlockingDeque<>(original);

        copy.putFirst("item1");
        assertTrue(copy.isEmpty()); // Break should be inherited
    }

    @Test
    @DisplayName("Test builder copy")
    public void testBuilderCopy() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> originalBuilder = new BreakableBlockingDeque.Builder<>();
        originalBuilder.addBreak(PUT_LAST_DOES_NOT_ADD_ELEMENT);

        BreakableBlockingDeque.Builder<String> copyBuilder = originalBuilder.copy();
        BreakableBlockingDeque<String> deque = copyBuilder.build();

        deque.putLast("item1");
        assertTrue(deque.isEmpty()); // Break should be copied
    }

    @Test
    @DisplayName("Test wrap factory method")
    public void testWrapFactoryMethod() throws InterruptedException {
        LinkedBlockingDeque<String> linkedDeque = new LinkedBlockingDeque<>();
        BreakableBlockingDeque<String> deque = BreakableBlockingDeque.wrap(
            linkedDeque, Set.of(TAKE_FIRST_ALWAYS_RETURNS_NULL));

        deque.putFirst("item1");
        assertNull(deque.takeFirst());
    }

    @Test
    @DisplayName("Test wrap factory method with characteristics")
    public void testWrapFactoryMethodWithCharacteristics() throws InterruptedException {
        LinkedBlockingDeque<String> linkedDeque = new LinkedBlockingDeque<>();
        BreakableBlockingDeque<String> deque = BreakableBlockingDeque.wrap(
            linkedDeque, Set.of(TAKE_LAST_ALWAYS_RETURNS_NULL), 0);

        deque.putLast("item1");
        assertNull(deque.takeLast());
    }

    // ========== Error Handling Testing ==========

    @Test
    @DisplayName("Test unsupported method exceptions")
    public void testUnsupportedMethodExceptions() {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<String>();
        builder.doesNotSupport(BlockingDequeMethods.PUT_FIRST);
        builder.doesNotSupport(BlockingDequeMethods.TAKE_FIRST);
        builder.doesNotSupport(BlockingDequeMethods.OFFER_FIRST_TIMEOUT);
        builder.doesNotSupport(BlockingDequeMethods.POLL_FIRST_TIMEOUT);
        BreakableBlockingDeque<String> deque = builder.build();

        assertThrows(UnsupportedOperationException.class, () -> deque.putFirst("item1"));
        assertThrows(UnsupportedOperationException.class, deque::takeFirst);
        assertThrows(UnsupportedOperationException.class, () ->
            deque.offerFirst("item1", 1, TimeUnit.SECONDS));
        assertThrows(UnsupportedOperationException.class, () ->
            deque.pollFirst(1, TimeUnit.SECONDS));
    }

    // ========== Integration Testing ==========

    @Test
    @DisplayName("Test LinkedBlockingDeque integration")
    public void testLinkedBlockingDequeIntegration() throws InterruptedException {
        LinkedBlockingDeque<Integer> linkedDeque = new LinkedBlockingDeque<>(10);
        BreakableBlockingDeque.Builder<Integer> builder = new BreakableBlockingDeque.Builder<>(linkedDeque);
        BreakableBlockingDeque<Integer> deque = builder.build();

        // Test capacity behavior
        for (int i = 0; i < 10; i++) {
            deque.putLast(i);
        }

        assertEquals(10, deque.size());
        assertEquals(Integer.valueOf(0), deque.takeFirst());
        assertEquals(Integer.valueOf(9), deque.takeLast());
    }

    @Test
    @DisplayName("Test multiple breaks interaction")
    public void testMultipleBreaksInteraction() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(PUT_FIRST_DOES_NOT_ADD_ELEMENT);
        builder.addBreak(PUT_LAST_DOES_NOT_ADD_ELEMENT);
        builder.addBreak(TAKE_FIRST_ALWAYS_RETURNS_NULL);
        builder.addBreak(TAKE_LAST_ALWAYS_RETURNS_NULL);
        BreakableBlockingDeque<String> deque = builder.build();

        // Add operations should not add_singleElement_returnsTrueAndUpdatesSize elements
        deque.putFirst("first");
        deque.putLast("last");
        assertTrue(deque.isEmpty());

        // Take operations should return null
        assertNull(deque.takeFirst());
        assertNull(deque.takeLast());
    }

    @Test
    @DisplayName("Test constructor with LinkedBlockingDeque")
    public void testConstructorWithLinkedBlockingDeque() throws InterruptedException {
        BreakableBlockingDeque<String> deque = new BreakableBlockingDeque<>();

        // Test that it works with default LinkedBlockingDeque backing
        deque.putFirst("test1");
        deque.putLast("test2");

        assertEquals("test1", deque.takeFirst());
        assertEquals("test2", deque.takeLast());
    }

    @Test
    @DisplayName("Test default constructor creates empty blocking deque")
    public void testDefaultConstructorCreatesEmptyBlockingDeque() {
        BreakableBlockingDeque<String> deque = new BreakableBlockingDeque<>();

        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
    }

    @Test
    @DisplayName("Test blocking behavior simulation")
    public void testBlockingBehaviorSimulation() throws InterruptedException {
        BreakableBlockingDeque<String> deque = new BreakableBlockingDeque<>();

        // Normal blocking behavior - these should not block since deque is unlimited
        deque.putFirst("item1");
        deque.putLast("item2");

        assertEquals("item1", deque.takeFirst());
        assertEquals("item2", deque.takeLast());
    }

    @Test
    @DisplayName("Test capacity-constrained behavior")
    public void testCapacityConstrainedBehavior() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.setCapacity(1);
        BreakableBlockingDeque<String> deque = builder.build();

        // Fill the deque to capacity
        deque.putFirst("item1");

        // This should return false due to capacity constraint
        assertFalse(deque.offerFirst("item2", 100, TimeUnit.MILLISECONDS));

        // After taking an element, we should be able to add_singleElement_returnsTrueAndUpdatesSize
        assertEquals("item1", deque.takeFirst());
        assertTrue(deque.offerLast("item2", 100, TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("Test break priority and interaction")
    public void testBreakPriorityAndInteraction() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION);
        builder.addBreak(OFFER_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE);
        BreakableBlockingDeque<String> deque = builder.build();

        // Exception break should take priority over return value break
        assertThrows(InterruptedException.class, () ->
            deque.offerFirst("item1", 1, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test state consistency across blocking deque operations")
    public void testStateConsistencyAcrossBlockingDequeOperations() throws InterruptedException {
        BreakableBlockingDeque<String> deque = new BreakableBlockingDeque<>();

        // Perform a mix of operations
        deque.putFirst("first");
        deque.putLast("last");
        deque.addFirst("new-first");
        deque.addLast("new-last");

        assertEquals(4, deque.size());
        assertEquals("new-first", deque.peekFirst());
        assertEquals("new-last", deque.peekLast());

        // Mix of take and poll operations
        assertEquals("new-first", deque.takeFirst());
        assertEquals("new-last", deque.takeLast());
        assertEquals(2, deque.size());
    }
}