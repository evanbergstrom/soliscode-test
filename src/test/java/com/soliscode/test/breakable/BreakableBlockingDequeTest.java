package com.soliscode.test.breakable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.blockingdeque.BlockingDequeMethods;
import com.soliscode.test.contract.blockingqueue.BlockingQueueMethods;

import static com.soliscode.test.breakable.BreakableBlockingDeque.OFFER_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE;
import static com.soliscode.test.breakable.BreakableBlockingDeque.OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION;
import static com.soliscode.test.breakable.BreakableBlockingDeque.OFFER_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE;
import static com.soliscode.test.breakable.BreakableBlockingDeque.OFFER_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION;
import static com.soliscode.test.breakable.BreakableBlockingDeque.POLL_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableBlockingDeque.POLL_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION;
import static com.soliscode.test.breakable.BreakableBlockingDeque.POLL_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableBlockingDeque.POLL_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION;
import static com.soliscode.test.breakable.BreakableBlockingDeque.PUT_FIRST_DOES_NOT_ADD_ELEMENT;
import static com.soliscode.test.breakable.BreakableBlockingDeque.PUT_FIRST_THROWS_EXCEPTION;
import static com.soliscode.test.breakable.BreakableBlockingDeque.PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION;
import static com.soliscode.test.breakable.BreakableBlockingDeque.PUT_LAST_DOES_NOT_ADD_ELEMENT;
import static com.soliscode.test.breakable.BreakableBlockingDeque.PUT_LAST_THROWS_EXCEPTION;
import static com.soliscode.test.breakable.BreakableBlockingDeque.PUT_LAST_THROWS_INTERRUPTED_EXCEPTION;
import static com.soliscode.test.breakable.BreakableBlockingDeque.TAKE_FIRST_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableBlockingDeque.TAKE_FIRST_DOES_NOT_REMOVE_ELEMENT;
import static com.soliscode.test.breakable.BreakableBlockingDeque.TAKE_FIRST_THROWS_INTERRUPTED_EXCEPTION;
import static com.soliscode.test.breakable.BreakableBlockingDeque.TAKE_LAST_ALWAYS_RETURNS_NULL;
import static com.soliscode.test.breakable.BreakableBlockingDeque.TAKE_LAST_DOES_NOT_REMOVE_ELEMENT;
import static com.soliscode.test.breakable.BreakableBlockingDeque.TAKE_LAST_THROWS_INTERRUPTED_EXCEPTION;
import static com.soliscode.test.breakable.BreakableBlockingDeque.builder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Comprehensive test suite for `BreakableBlockingDeque` functionality.
///
/// This test class validates all aspects of the `BreakableBlockingDeque` implementation,
/// including break constants, normal operations, error handling, and builder patterns.
/// The tests ensure that the `BreakableBlockingDeque` correctly implements both the
/// `BlockingDeque` interface and the break mechanism.
///
/// ## Purpose
/// The purpose of this test suite is to ensure that `BreakableBlockingDeque` behaves
/// correctly both under normal conditions and when specific "breaks" are applied.
/// It verifies that the blocking operations (`put`, `take`, `offer`, `poll`) respect
/// the configured breaks and that the builder pattern correctly configures the instance.
///
/// ## Usage Examples
/// ```java
/// BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
/// builder.addBreak(PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION);
/// BreakableBlockingDeque<String> deque = builder.build();
///
/// // This will throw InterruptedException as configured
/// deque.putFirst("item1");
/// ```
///
/// ## Thread Safety
/// These tests are generally execution-thread-confined. While `BreakableBlockingDeque`
/// itself is designed to be thread-safe (wrapping a `LinkedBlockingDeque`), the tests
/// primarily verify state and exception behavior in a single-threaded manner, with
/// some simulation of blocking behavior.
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see BreakableBlockingDeque
/// @see AbstractTest
@DisplayName("Tests for BreakableBlockingDeque functionality")
public class BreakableBlockingDequeTest extends AbstractTest {

    /// Constructs a new BreakableBlockingDequeTest.
    public BreakableBlockingDequeTest() {
    }

    // ========== Break Testing: Put Operations ==========

    /// Verifies that `putFirst` throws `InterruptedException` when the
    /// {@link BreakableBlockingDeque#PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION} break is added.
    @Test
    @DisplayName("putFirst() - when break added - throws InterruptedException")
    public void putFirstWhenBreakAddedThrowsInterruptedException() {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        assertThrows(InterruptedException.class, () -> deque.putFirst("item1"));
    }

    /// Verifies that `putFirst` does not add the element to the deque when the
    /// {@link BreakableBlockingDeque#PUT_FIRST_DOES_NOT_ADD_ELEMENT} break is added.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("putFirst() - when break added - does not add element")
    public void putFirstWhenBreakAddedDoesNotAddElement() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(PUT_FIRST_DOES_NOT_ADD_ELEMENT);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putFirst("item1");
        assertTrue(deque.isEmpty());
    }

    /// Verifies that `putFirst` throws an exception when the
    /// {@link BreakableBlockingDeque#PUT_FIRST_THROWS_EXCEPTION} break is added.
    @Test
    @DisplayName("putFirst() - when break added - throws Exception")
    public void putFirstWhenBreakAddedThrowsException() {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(PUT_FIRST_THROWS_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        assertThrows(IllegalStateException.class, () -> deque.putFirst("item1"));
    }

    /// Verifies that `putLast` throws `InterruptedException` when the
    /// {@link BreakableBlockingDeque#PUT_LAST_THROWS_INTERRUPTED_EXCEPTION} break is added.
    @Test
    @DisplayName("putLast() - when break added - throws InterruptedException")
    public void putLastWhenBreakAddedThrowsInterruptedException() {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(PUT_LAST_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        assertThrows(InterruptedException.class, () -> deque.putLast("item1"));
    }

    /// Verifies that `putLast` does not add the element to the deque when the
    /// {@link BreakableBlockingDeque#PUT_LAST_DOES_NOT_ADD_ELEMENT} break is added.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("putLast() - when break added - does not add element")
    public void putLastWhenBreakAddedDoesNotAddElement() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(PUT_LAST_DOES_NOT_ADD_ELEMENT);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putLast("item1");
        assertTrue(deque.isEmpty());
    }

    /// Verifies that `putLast` throws an exception when the
    /// {@link BreakableBlockingDeque#PUT_LAST_THROWS_EXCEPTION} break is added.
    @Test
    @DisplayName("putLast() - when break added - throws Exception")
    public void putLastWhenBreakAddedThrowsException() {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(PUT_LAST_THROWS_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        assertThrows(IllegalStateException.class, () -> deque.putLast("item1"));
    }

    // ========== Break Testing: Take Operations ==========

    /// Verifies that `takeFirst` throws `InterruptedException` when the
    /// {@link BreakableBlockingDeque#TAKE_FIRST_THROWS_INTERRUPTED_EXCEPTION} break is added.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("takeFirst() - when break added - throws InterruptedException")
    public void takeFirstWhenBreakAddedThrowsInterruptedException() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(TAKE_FIRST_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putFirst("item1");
        assertThrows(InterruptedException.class, deque::takeFirst);
    }

    /// Verifies that `takeFirst` returns `null` when the
    /// {@link BreakableBlockingDeque#TAKE_FIRST_ALWAYS_RETURNS_NULL} break is added.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("takeFirst() - when break added - returns null")
    public void takeFirstWhenBreakAddedReturnsNull() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(TAKE_FIRST_ALWAYS_RETURNS_NULL);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putFirst("item1");
        assertNull(deque.takeFirst());
    }

    /// Verifies that `takeFirst` does not remove the element from the deque when the
    /// {@link BreakableBlockingDeque#TAKE_FIRST_DOES_NOT_REMOVE_ELEMENT} break is added.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("takeFirst() - when break added - does not remove element")
    public void takeFirstWhenBreakAddedDoesNotRemoveElement() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(TAKE_FIRST_DOES_NOT_REMOVE_ELEMENT);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putFirst("item1");
        assertEquals("item1", deque.takeFirst());
        assertEquals(1, deque.size()); // Element should still be there
    }

    /// Verifies that `takeLast` throws `InterruptedException` when the
    /// {@link BreakableBlockingDeque#TAKE_LAST_THROWS_INTERRUPTED_EXCEPTION} break is added.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("takeLast() - when break added - throws InterruptedException")
    public void takeLastWhenBreakAddedThrowsInterruptedException() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(TAKE_LAST_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putLast("item1");
        assertThrows(InterruptedException.class, deque::takeLast);
    }

    /// Verifies that `takeLast` returns `null` when the
    /// {@link BreakableBlockingDeque#TAKE_LAST_ALWAYS_RETURNS_NULL} break is added.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("takeLast() - when break added - returns null")
    public void takeLastWhenBreakAddedReturnsNull() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(TAKE_LAST_ALWAYS_RETURNS_NULL);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putLast("item1");
        assertNull(deque.takeLast());
    }

    /// Verifies that `takeLast` does not remove the element from the deque when the
    /// {@link BreakableBlockingDeque#TAKE_LAST_DOES_NOT_REMOVE_ELEMENT} break is added.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("takeLast() - when break added - does not remove element")
    public void takeLastWhenBreakAddedDoesNotRemoveElement() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(TAKE_LAST_DOES_NOT_REMOVE_ELEMENT);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putLast("item1");
        assertEquals("item1", deque.takeLast());
        assertEquals(1, deque.size()); // Element should still be there
    }

    // ========== Break Testing: Timeout Operations ==========

    /// Verifies that `offerFirst` with timeout returns `false` when the
    /// {@link BreakableBlockingDeque#OFFER_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE} break is added.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("offerFirst() - with timeout and break added - returns false")
    public void offerFirstWithTimeoutAndBreakAddedReturnsFalse() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(OFFER_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE);
        BreakableBlockingDeque<String> deque = builder.build();

        assertFalse(deque.offerFirst("item1", 1, TimeUnit.SECONDS));
    }

    /// Verifies that `offerFirst` with timeout throws `InterruptedException` when the
    /// {@link BreakableBlockingDeque#OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION} break is added.
    @Test
    @DisplayName("offerFirst() - with timeout and break added - throws InterruptedException")
    public void offerFirstWithTimeoutAndBreakAddedThrowsInterruptedException() {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        assertThrows(InterruptedException.class, () ->
            deque.offerFirst("item1", 1, TimeUnit.SECONDS));
    }

    /// Verifies that `offerLast` with timeout returns `false` when the
    /// {@link BreakableBlockingDeque#OFFER_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE} break is added.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("offerLast() - with timeout and break added - returns false")
    public void offerLastWithTimeoutAndBreakAddedReturnsFalse() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(OFFER_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE);
        BreakableBlockingDeque<String> deque = builder.build();

        assertFalse(deque.offerLast("item1", 1, TimeUnit.SECONDS));
    }

    /// Verifies that `offerLast` with timeout throws `InterruptedException` when the
    /// {@link BreakableBlockingDeque#OFFER_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION} break is added.
    @Test
    @DisplayName("offerLast() - with timeout and break added - throws InterruptedException")
    public void offerLastWithTimeoutAndBreakAddedThrowsInterruptedException() {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(OFFER_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        assertThrows(InterruptedException.class, () ->
            deque.offerLast("item1", 1, TimeUnit.SECONDS));
    }

    /// Verifies that `pollFirst` with timeout returns `null` when the
    /// {@link BreakableBlockingDeque#POLL_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL} break is added.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("pollFirst() - with timeout and break added - returns null")
    public void pollFirstWithTimeoutAndBreakAddedReturnsNull() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(POLL_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putFirst("item1");
        assertNull(deque.pollFirst(1, TimeUnit.SECONDS));
    }

    /// Verifies that `pollFirst` with timeout throws `InterruptedException` when the
    /// {@link BreakableBlockingDeque#POLL_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION} break is added.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("pollFirst() - with timeout and break added - throws InterruptedException")
    public void pollFirstWithTimeoutAndBreakAddedThrowsInterruptedException() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(POLL_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putFirst("item1");
        assertThrows(InterruptedException.class, () ->
            deque.pollFirst(1, TimeUnit.SECONDS));
    }

    /// Verifies that `pollLast` with timeout returns `null` when the
    /// {@link BreakableBlockingDeque#POLL_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL} break is added.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("pollLast() - with timeout and break added - returns null")
    public void pollLastWithTimeoutAndBreakAddedReturnsNull() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(POLL_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putLast("item1");
        assertNull(deque.pollLast(1, TimeUnit.SECONDS));
    }

    /// Verifies that `pollLast` with timeout throws `InterruptedException` when the
    /// {@link BreakableBlockingDeque#POLL_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION} break is added.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("pollLast() - with timeout and break added - throws InterruptedException")
    public void pollLastWithTimeoutAndBreakAddedThrowsInterruptedException() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(POLL_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION);
        BreakableBlockingDeque<String> deque = builder.build();

        deque.putLast("item1");
        assertThrows(InterruptedException.class, () ->
            deque.pollLast(1, TimeUnit.SECONDS));
    }

    // ========== Normal Operations Testing ==========

    /// Tests basic `put` and `take` operations under normal conditions (no breaks).
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("blockingDeque - when called - executes normal operations")
    public void blockingDequeWhenCalledExecutesNormalOperations() throws InterruptedException {
        BreakableBlockingDeque<String> deque = new BreakableBlockingDeque<>();

        // Test basic put/take operations
        deque.putFirst("first");
        deque.putLast("last");

        assertEquals("first", deque.takeFirst());
        assertEquals("last", deque.takeLast());
        assertTrue(deque.isEmpty());
    }

    /// Tests timeout operations (`offer` and `poll`) under normal conditions.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("timeoutOperations - when called without breaks - executes normally")
    public void timeoutOperationsWhenCalledWithoutBreaksExecutesNormally() throws InterruptedException {
        BreakableBlockingDeque<String> deque = new BreakableBlockingDeque<>();

        // Test timeout operations
        assertTrue(deque.offerFirst("first", 1, TimeUnit.SECONDS));
        assertTrue(deque.offerLast("last", 1, TimeUnit.SECONDS));

        assertEquals("first", deque.pollFirst(1, TimeUnit.SECONDS));
        assertEquals("last", deque.pollLast(1, TimeUnit.SECONDS));
        assertNull(deque.pollFirst(100, TimeUnit.MILLISECONDS)); // Empty deque
    }

    /// Verifies that inherited `Deque` operations function correctly.
    ///
    @Test
    @DisplayName("inheritance - from BreakableDeque - functions correctly")
    public void inheritanceFromBreakableDequeFunctionsCorrectly() {
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

    /// Tests the builder pattern for configuring multiple breaks.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("builderPattern - when used - configures breaks")
    public void builderPatternWhenUsedConfiguresBreaks() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION);
        builder.addBreak(TAKE_LAST_ALWAYS_RETURNS_NULL);
        BreakableBlockingDeque<String> deque = builder.build();

        assertThrows(InterruptedException.class, () -> deque.putFirst("item1"));
        deque.putLast("item1");
        assertNull(deque.takeLast());
    }

    /// Tests the builder when wrapping an existing {@link java.util.concurrent.BlockingDeque}.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("builder - with existing BlockingDeque - wraps correctly")
    public void builderWithExistingBlockingDequeWrapsCorrectly() throws InterruptedException {
        LinkedBlockingDeque<String> linkedDeque = new LinkedBlockingDeque<>();
        linkedDeque.putFirst("existing");

        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>(linkedDeque);
        builder.addBreak(TAKE_FIRST_DOES_NOT_REMOVE_ELEMENT);
        BreakableBlockingDeque<String> deque = builder.build();

        assertEquals("existing", deque.takeFirst());
        assertEquals(1, deque.size()); // Element should still be there due to break
    }

    /// Tests the copy constructor to ensure configuration and breaks are inherited.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("copyConstructor - when called - copies configuration")
    public void copyConstructorWhenCalledCopiesConfiguration() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> originalBuilder = new BreakableBlockingDeque.Builder<>();
        originalBuilder.addBreak(PUT_FIRST_DOES_NOT_ADD_ELEMENT);
        BreakableBlockingDeque<String> original = originalBuilder.build();

        BreakableBlockingDeque<String> copy = new BreakableBlockingDeque<>(original);

        copy.putFirst("item1");
        assertTrue(copy.isEmpty()); // Break should be inherited
    }

    /// Tests the `copy` method of the builder.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("builderCopy - when called - creates independent builder with same configuration")
    public void builderCopyWhenCalledCreatesIndependentBuilderWithSameConfiguration() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> originalBuilder = new BreakableBlockingDeque.Builder<>();
        originalBuilder.addBreak(PUT_LAST_DOES_NOT_ADD_ELEMENT);

        BreakableBlockingDeque.Builder<String> copyBuilder = originalBuilder.copy();
        BreakableBlockingDeque<String> deque = copyBuilder.build();

        deque.putLast("item1");
        assertTrue(deque.isEmpty()); // Break should be copied
    }

    /// Tests the `wrap` static factory method.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("wrap - static factory method - creates wrapped instance")
    public void wrapStaticFactoryMethodCreatesWrappedInstance() throws InterruptedException {
        LinkedBlockingDeque<String> linkedDeque = new LinkedBlockingDeque<>();
        BreakableBlockingDeque<String> deque = BreakableBlockingDeque.wrap(
            linkedDeque, Set.of(TAKE_FIRST_ALWAYS_RETURNS_NULL));

        deque.putFirst("item1");
        assertNull(deque.takeFirst());
    }

    /// Tests the `wrap` static factory method with characteristics.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("wrap - static factory method with characteristics - creates wrapped instance")
    public void wrapStaticFactoryMethodWithCharacteristicsCreatesWrappedInstance() throws InterruptedException {
        LinkedBlockingDeque<String> linkedDeque = new LinkedBlockingDeque<>();
        BreakableBlockingDeque<String> deque = BreakableBlockingDeque.wrap(
            linkedDeque, Set.of(TAKE_LAST_ALWAYS_RETURNS_NULL), 0);

        deque.putLast("item1");
        assertNull(deque.takeLast());
    }

    // ========== Error Handling Testing ==========

    /// Verifies that methods configured as unsupported throw {@link UnsupportedOperationException}.
    @Test
    @DisplayName("unsupportedMethods - when configured - throw UnsupportedOperationException")
    public void unsupportedMethodsWhenConfiguredThrowUnsupportedOperationException() {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
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

    /// Tests integration with {@link java.util.concurrent.LinkedBlockingDeque} and capacity limits.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("linkedBlockingDequeIntegration - when used - functions correctly")
    public void linkedBlockingDequeIntegrationWhenUsedFunctionsCorrectly() throws InterruptedException {
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

    /// Verifies that multiple breaks added to the same instance all take effect.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("multipleBreaksInteraction - when added - all breaks take effect")
    public void multipleBreaksInteractionWhenAddedAllBreaksTakeEffect() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(PUT_FIRST_DOES_NOT_ADD_ELEMENT);
        builder.addBreak(PUT_LAST_DOES_NOT_ADD_ELEMENT);
        builder.addBreak(TAKE_FIRST_ALWAYS_RETURNS_NULL);
        builder.addBreak(TAKE_LAST_ALWAYS_RETURNS_NULL);
        BreakableBlockingDeque<String> deque = builder.build();

        // Add operations should not add elements
        deque.putFirst("first");
        deque.putLast("last");
        assertTrue(deque.isEmpty());

        // Take operations should return null
        assertNull(deque.takeFirst());
        assertNull(deque.takeLast());
    }

    /// Tests the constructor that uses a default {@link java.util.concurrent.LinkedBlockingDeque}.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("constructor - with LinkedBlockingDeque - functions correctly")
    public void constructorWithLinkedBlockingDequeFunctionsCorrectly() throws InterruptedException {
        BreakableBlockingDeque<String> deque = new BreakableBlockingDeque<>();

        // Test that it works with default LinkedBlockingDeque backing
        deque.putFirst("test1");
        deque.putLast("test2");

        assertEquals("test1", deque.takeFirst());
        assertEquals("test2", deque.takeLast());
    }

    /// Tests the default constructor of {@link BreakableBlockingDeque}.
    @Test
    @DisplayName("defaultConstructor - when called - creates empty deque")
    public void defaultConstructorWhenCalledCreatesEmptyDeque() {
        BreakableBlockingDeque<String> deque = new BreakableBlockingDeque<>();

        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
    }

    /// Tests normal blocking behavior simulation.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("blockingBehaviorSimulation - when called - executes normally")
    public void blockingBehaviorSimulationWhenCalledExecutesNormally() throws InterruptedException {
        BreakableBlockingDeque<String> deque = new BreakableBlockingDeque<>();

        // Normal blocking behavior - these should not block since deque is unlimited
        deque.putFirst("item1");
        deque.putLast("item2");

        assertEquals("item1", deque.takeFirst());
        assertEquals("item2", deque.takeLast());
    }

    /// Tests capacity-constrained behavior.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("capacityConstrainedBehavior - when full - respects constraints")
    public void capacityConstrainedBehaviorWhenFullRespectsConstraints() throws InterruptedException {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.setCapacity(1);
        BreakableBlockingDeque<String> deque = builder.build();

        // Fill the deque to capacity
        deque.putFirst("item1");

        // This should return false due to capacity constraint
        assertFalse(deque.offerFirst("item2", 100, TimeUnit.MILLISECONDS));

        // After taking an element, we should be able to add
        assertEquals("item1", deque.takeFirst());
        assertTrue(deque.offerLast("item2", 100, TimeUnit.MILLISECONDS));
    }

    /// Verifies break priority when multiple conflicting breaks are applied.
    ///
    @Test
    @DisplayName("breakPriorityAndInteraction - when multiple breaks applied - respects priority")
    public void breakPriorityAndInteractionWhenMultipleBreaksAppliedRespectsPriority() {
        BreakableBlockingDeque.Builder<String> builder = new BreakableBlockingDeque.Builder<>();
        builder.addBreak(OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION);
        builder.addBreak(OFFER_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE);
        BreakableBlockingDeque<String> deque = builder.build();

        // Exception break should take priority over return value break
        assertThrows(InterruptedException.class, () ->
            deque.offerFirst("item1", 1, TimeUnit.SECONDS));
    }

    /// Verifies state consistency across multiple heterogeneous operations.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("stateConsistency - across multiple operations - maintains consistency")
    public void stateConsistencyAcrossMultipleOperationsMaintainsConsistency() throws InterruptedException {
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

    /// Demonstrates how to wait for the deque to be non-empty before peeking.
    /// Since BlockingDeque does not have a blocking peek, we use takeFirst()
    /// and then put it back if we only wanted to peek.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("blockingPeek - wait for element then peek - demonstrates pattern")
    public void blockingPeekWhenCalledWaitsAndPeeks() throws InterruptedException {
        BreakableBlockingDeque<String> deque = new BreakableBlockingDeque<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();

        // Pattern to wait for element then peek:
        // 1. takeFirst() blocks until non-empty
        // 2. put it back immediately
        Thread consumer = new Thread(() -> {
            try {
                String element = deque.takeFirst();
                deque.putFirst(element);
            } catch (final Throwable t) {
                failure.set(t);
            }
        });
        consumer.setDaemon(true);
        consumer.start();

        // Produce only once the consumer is blocked in takeFirst(), with a bound instead of a sleep.
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (consumer.getState() != Thread.State.WAITING) {
            assertTrue(System.nanoTime() < deadline, "consumer did not block in takeFirst()");
            Thread.onSpinWait();
        }
        deque.putFirst("produced");
        consumer.join(TimeUnit.SECONDS.toMillis(5));
        assertFalse(consumer.isAlive(), "consumer did not finish");
        assertNull(failure.get());

        // 3. now we can peek safely
        assertEquals("produced", deque.peekFirst());
    }

    // ========== The fixes and the breaks added by T-0033 ==========

    private static BreakableBlockingDeque<String> dequeOf(final Break b, final String... elements) {
        return new BreakableBlockingDeque.Builder<>(List.of(elements)).addBreak(b).build();
    }

    private static List<String> contents(final BreakableBlockingDeque<String> deque) {
        return new ArrayList<>(deque);
    }

    /// A drain into the deque itself throws IllegalArgumentException and leaves it unchanged. Before T-0033 the
    /// breakable passed the call to its store, which compared the collection with itself, not with the breakable,
    /// and drained the deque into itself.
    @Test
    @DisplayName("drainTo(Collection) into the deque itself throws IllegalArgumentException")
    public void drainToItselfThrowsIllegalArgumentException() {
        BreakableBlockingDeque<String> deque =
                new BreakableBlockingDeque.Builder<>(List.of("a", "b")).build();
        assertThrows(IllegalArgumentException.class, () -> deque.drainTo(deque));
        assertThrows(IllegalArgumentException.class, () -> deque.drainTo(deque, 1));
        assertEquals(List.of("a", "b"), contents(deque));
    }

    /// With TAKE_LAST_DOES_NOT_REMOVE_ELEMENT, takeLast() returns the last element, not the first, and leaves it.
    /// Before T-0033 it returned the first.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("takeLast(): returns the last element and leaves it when TAKE_LAST_DOES_NOT_REMOVE_ELEMENT")
    public void takeLastWhenDoesNotRemoveBreakActiveReturnsLastElement() throws InterruptedException {
        BreakableBlockingDeque<String> deque = dequeOf(TAKE_LAST_DOES_NOT_REMOVE_ELEMENT, "a", "b");
        assertEquals("b", deque.takeLast());
        assertEquals(List.of("a", "b"), contents(deque));
    }

    /// The position breaks act on the other end of the deque.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("the position breaks act on the other end of the deque")
    public void positionBreaksActOnOtherEnd() throws InterruptedException {
        BreakableBlockingDeque<String> deque = dequeOf(BreakableBlockingDeque.PUT_FIRST_ADDS_TO_END, "a");
        deque.putFirst("b");
        assertEquals(List.of("a", "b"), contents(deque));
        deque = dequeOf(BreakableBlockingDeque.PUT_LAST_ADDS_TO_FRONT, "a");
        deque.putLast("b");
        deque.put("c");
        assertEquals(List.of("c", "b", "a"), contents(deque));
        deque = dequeOf(BreakableBlockingDeque.OFFER_FIRST_WITH_TIMEOUT_ADDS_TO_END, "a");
        assertTrue(deque.offerFirst("b", 1, TimeUnit.SECONDS));
        assertEquals(List.of("a", "b"), contents(deque));
        deque = dequeOf(BreakableBlockingDeque.OFFER_LAST_WITH_TIMEOUT_ADDS_TO_FRONT, "a");
        assertTrue(deque.offerLast("b", 1, TimeUnit.SECONDS));
        assertEquals(List.of("b", "a"), contents(deque));
        assertEquals("b", dequeOf(BreakableBlockingDeque.TAKE_FIRST_REMOVES_LAST, "a", "b").takeFirst());
        assertEquals("b", dequeOf(BreakableBlockingDeque.TAKE_FIRST_REMOVES_LAST, "a", "b").take());
        assertEquals("a", dequeOf(BreakableBlockingDeque.TAKE_LAST_REMOVES_FIRST, "a", "b").takeLast());
        assertEquals("b", dequeOf(BreakableBlockingDeque.POLL_FIRST_WITH_TIMEOUT_REMOVES_LAST, "a", "b")
                .pollFirst(1, TimeUnit.SECONDS));
        assertEquals("a", dequeOf(BreakableBlockingDeque.POLL_LAST_WITH_TIMEOUT_REMOVES_FIRST, "a", "b")
                .pollLast(1, TimeUnit.SECONDS));
    }

    /// With the DOES_NOT_WAIT breaks, the timed methods return at once on a full or empty deque, though the timeout
    /// is an hour.
    @Test
    @DisplayName("the timed methods do not wait when their DOES_NOT_WAIT break is set")
    public void timedMethodsWhenDoesNotWaitBreakActiveReturnAtOnce() {
        Duration limit = Duration.ofSeconds(5);
        BreakableBlockingDeque<String> full = new BreakableBlockingDeque.Builder<>(List.of("a"))
                .setCapacity(1).addBreak(BreakableBlockingDeque.OFFER_FIRST_WITH_TIMEOUT_DOES_NOT_WAIT)
                .addBreak(BreakableBlockingDeque.OFFER_LAST_WITH_TIMEOUT_DOES_NOT_WAIT).build();
        assertFalse(assertTimeoutPreemptively(limit,
                () -> full.offerFirst("b", 1, TimeUnit.HOURS)));
        assertFalse(assertTimeoutPreemptively(limit,
                () -> full.offerLast("b", 1, TimeUnit.HOURS)));
        assertEquals(List.of("a"), contents(full));
        BreakableBlockingDeque<String> empty = new BreakableBlockingDeque.Builder<String>()
                .addBreak(BreakableBlockingDeque.POLL_FIRST_WITH_TIMEOUT_DOES_NOT_WAIT)
                .addBreak(BreakableBlockingDeque.POLL_LAST_WITH_TIMEOUT_DOES_NOT_WAIT).build();
        assertNull(assertTimeoutPreemptively(limit,
                () -> empty.pollFirst(1, TimeUnit.HOURS)));
        assertNull(assertTimeoutPreemptively(limit,
                () -> empty.pollLast(1, TimeUnit.HOURS)));
    }

    /// With an ALWAYS_BLOCKS break, the call blocks on a deque that has room or elements, until interrupted.
    @Test
    @DisplayName("the ALWAYS_BLOCKS breaks block until interrupted")
    public void alwaysBlocksBreaksBlockUntilInterrupted() {
        assertBlocksUntilInterrupted(BreakableBlockingDeque.PUT_FIRST_ALWAYS_BLOCKS, d -> d.putFirst("b"));
        assertBlocksUntilInterrupted(BreakableBlockingDeque.PUT_LAST_ALWAYS_BLOCKS, d -> d.putLast("b"));
        assertBlocksUntilInterrupted(BreakableBlockingDeque.PUT_LAST_ALWAYS_BLOCKS, d -> d.put("b"));
        assertBlocksUntilInterrupted(BreakableBlockingDeque.TAKE_FIRST_ALWAYS_BLOCKS,
                BreakableBlockingDeque::takeFirst);
        assertBlocksUntilInterrupted(BreakableBlockingDeque.TAKE_FIRST_ALWAYS_BLOCKS, BreakableBlockingDeque::take);
        assertBlocksUntilInterrupted(BreakableBlockingDeque.TAKE_LAST_ALWAYS_BLOCKS, BreakableBlockingDeque::takeLast);
    }

    /// A call on a deque holding one element, with a break set.
    @FunctionalInterface
    private interface DequeCall {
        /// Makes the call.
        ///
        /// @param deque the deque
        /// @throws InterruptedException if the call is interrupted
        void call(BreakableBlockingDeque<String> deque) throws InterruptedException;
    }

    /// Asserts that the call, with the break set, waits on a deque that has room and an element, until its thread is
    /// interrupted; then it throws InterruptedException and leaves the deque unchanged. A call that returns at once
    /// never reaches the waiting state, and fails the test after 5 s.
    private static void assertBlocksUntilInterrupted(final Break b, final DequeCall call) {
        BreakableBlockingDeque<String> deque = dequeOf(b, "a");
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                call.call(deque);
            } catch (final InterruptedException | RuntimeException e) {
                thrown.set(e);
            }
        });
        thread.setDaemon(true);
        thread.start();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (thread.getState() != Thread.State.WAITING && System.nanoTime() - deadline < 0) {
            Thread.onSpinWait();
        }
        assertEquals(Thread.State.WAITING, thread.getState(), b.description() + ": the call did not block");
        thread.interrupt();
        assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> thread.join());
        assertTrue(thrown.get() instanceof InterruptedException, b.description() + ": " + thrown.get());
        assertEquals(List.of("a"), contents(deque), b.description());
    }

    /// The thread-safety breaks still return and remove the right element on one thread: they show only under
    /// contention.
    ///
    /// @throws InterruptedException if the thread is interrupted
    @Test
    @DisplayName("the thread-safety breaks still act correctly on one thread")
    public void threadSafetyBreaksActCorrectlyOnOneThread() throws InterruptedException {
        BreakableBlockingDeque<String> deque =
                dequeOf(BreakableBlockingDeque.TAKE_FIRST_IS_NOT_THREAD_SAFE, "a", "b");
        assertEquals("a", deque.takeFirst());
        assertEquals(List.of("b"), contents(deque));
        deque = dequeOf(BreakableBlockingDeque.TAKE_LAST_IS_NOT_THREAD_SAFE, "a", "b", "a");
        assertEquals("a", deque.takeLast());
        assertEquals(List.of("a", "b"), contents(deque));
        deque = dequeOf(BreakableBlockingDeque.POLL_FIRST_WITH_TIMEOUT_IS_NOT_THREAD_SAFE, "a", "b");
        assertEquals("a", deque.pollFirst(1, TimeUnit.SECONDS));
        assertEquals("b", deque.pollFirst(1, TimeUnit.SECONDS));
        assertNull(deque.pollFirst(1, TimeUnit.MILLISECONDS));
        deque = dequeOf(BreakableBlockingDeque.POLL_LAST_WITH_TIMEOUT_IS_NOT_THREAD_SAFE, "a", "b");
        assertEquals("b", deque.pollLast(1, TimeUnit.SECONDS));
        assertEquals(List.of("a"), contents(deque));
        deque = dequeOf(BreakableBlockingDeque.DRAIN_TO_IS_NOT_THREAD_SAFE, "a", "b", "c");
        List<String> first = new ArrayList<>();
        assertEquals(1, deque.drainTo(first, 1));
        assertEquals(List.of("a"), first);
        List<String> rest = new ArrayList<>();
        assertEquals(2, deque.drainTo(rest));
        assertEquals(List.of("b", "c"), rest);
        assertTrue(deque.isEmpty());
    }

    /// The capacity and drain breaks copied from BreakableBlockingQueue.
    @Test
    @DisplayName("the remainingCapacity and drainTo breaks")
    public void capacityAndDrainBreaks() {
        assertEquals(0,
                dequeOf(BreakableBlockingDeque.REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO).remainingCapacity());
        BreakableBlockingDeque<String> deque = dequeOf(BreakableBlockingDeque.DRAIN_TO_ALWAYS_RETURNS_ZERO, "a");
        List<String> target = new ArrayList<>();
        assertEquals(0, deque.drainTo(target));
        assertEquals(0, deque.drainTo(target, 1));
        assertTrue(target.isEmpty());
        BreakableBlockingDeque<String> throwing = dequeOf(BreakableBlockingDeque.DRAIN_TO_THROWS_EXCEPTION, "a");
        assertThrows(IllegalStateException.class, () -> throwing.drainTo(target));
        assertThrows(IllegalStateException.class, () -> throwing.drainTo(target, 1));
        deque = dequeOf(BreakableBlockingDeque.DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS, "a", "b");
        assertEquals(2, deque.drainTo(target));
        assertEquals(1, deque.drainTo(target, 1));
        assertEquals(List.of("a", "b", "a"), target);
        assertEquals(List.of("a", "b"), contents(deque));
    }

    /// put, take and the timed offer and poll check their own method support, not only the deque method they call.
    @Test
    @DisplayName("put, take and the timed offer and poll check their own method support")
    public void blockingQueueMethodsCheckTheirOwnSupport() {
        BreakableBlockingDeque<String> deque = new BreakableBlockingDeque.Builder<String>()
                .doesNotSupport(BlockingQueueMethods.PUT)
                .doesNotSupport(BlockingQueueMethods.TAKE)
                .doesNotSupport(BlockingQueueMethods.OFFER_TIMEOUT)
                .doesNotSupport(BlockingQueueMethods.POLL_TIMEOUT)
                .build();
        assertThrows(UnsupportedOperationException.class, () -> deque.put("a"));
        assertThrows(UnsupportedOperationException.class, () -> deque.offer("a", 1, TimeUnit.SECONDS));
        assertTrue(deque.isEmpty());
        deque.add("a");
        assertThrows(UnsupportedOperationException.class, deque::take);
        assertThrows(UnsupportedOperationException.class, () -> deque.poll(1, TimeUnit.SECONDS));
        assertEquals(List.of("a"), contents(deque));
    }
}
