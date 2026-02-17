package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.MethodStatus;
import org.soliscode.test.contract.deque.DequeMethods;
import org.soliscode.test.contract.sequencedcollection.SequencedCollectionMethods;
import org.soliscode.test.contract.support.CollectionProviderSupport;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.CollectionProviders;
import org.soliscode.test.provider.ObjectProvider;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/// A Deque implementation that can be programmatically broken for testing purposes.
///
/// This class extends BreakableQueue and implements the Deque interface, providing
/// additional break constants for testing Deque-specific functionality. It wraps
/// an existing Deque and allows specific behaviors to be "broken" through the
/// Break mechanism.
///
/// ## Overview
///
/// BreakableDeque provides comprehensive testing capabilities for code that works with
/// Deque implementations. It supports all standard Deque operations while enabling
/// controlled behavioral modifications through break constants. This is particularly useful for:
///
/// - **Testing Deque Contract Compliance**: Verifying that code correctly handles double-ended operations
/// - **Stack and Queue Testing**: Simulating both stack (LIFO) and queue (FIFO) operation failures
/// - **Error Condition Simulation**: Testing how code responds to deque operation failures
/// - **Performance Testing**: Simulating slow or failing deque operations
/// - **Robustness Testing**: Verifying code resilience against unexpected Deque behaviors
///
/// ## Deque-Specific Break Constants
///
/// This class provides break constants for all Deque-specific methods:
///
/// ### First-End Operations
/// - **OFFER_FIRST_ALWAYS_RETURNS_FALSE**: Forces offerFirst() to always return false
/// - **OFFER_FIRST_DOES_NOT_ADD_ELEMENT**: Forces offerFirst() to accept but not add_singleElement_returnsTrueAndUpdatesSize elements
/// - **POLL_FIRST_ALWAYS_RETURNS_NULL**: Forces pollFirst() to always return null
/// - **POLL_FIRST_DOES_NOT_REMOVE_ELEMENT**: Forces pollFirst() to return element without removing it
/// - **PEEK_FIRST_ALWAYS_RETURNS_NULL**: Forces peekFirst() to always return null
/// - **PEEK_FIRST_RETURNS_RANDOM_ELEMENT**: Forces peekFirst() to return arbitrary elements
///
/// ### Last-End Operations
/// - **OFFER_LAST_ALWAYS_RETURNS_FALSE**: Forces offerLast() to always return false
/// - **OFFER_LAST_DOES_NOT_ADD_ELEMENT**: Forces offerLast() to accept but not add_singleElement_returnsTrueAndUpdatesSize elements
/// - **POLL_LAST_ALWAYS_RETURNS_NULL**: Forces pollLast() to always return null
/// - **POLL_LAST_DOES_NOT_REMOVE_ELEMENT**: Forces pollLast() to return element without removing it
/// - **PEEK_LAST_ALWAYS_RETURNS_NULL**: Forces peekLast() to always return null
/// - **PEEK_LAST_RETURNS_RANDOM_ELEMENT**: Forces peekLast() to return arbitrary elements
///
/// ### Stack Operations
/// - **PUSH_THROWS_EXCEPTION**: Forces push() to throw exceptions
/// - **PUSH_DOES_NOT_ADD_ELEMENT**: Forces push() to accept but not add_singleElement_returnsTrueAndUpdatesSize elements
/// - **POP_THROWS_EXCEPTION**: Forces pop() to throw exceptions instead of normal operation
/// - **POP_ALWAYS_RETURNS_NULL**: Forces pop() to return null (non-standard behavior)
///
/// ### Search Operations
/// - **REMOVE_FIRST_OCCURRENCE_ALWAYS_RETURNS_FALSE**: Forces removeFirstOccurrence() to always return false
/// - **REMOVE_LAST_OCCURRENCE_ALWAYS_RETURNS_FALSE**: Forces removeLastOccurrence() to always return false
/// - **REMOVE_FIRST_OCCURRENCE_DOES_NOT_REMOVE**: Forces removeFirstOccurrence() to return true but not remove
/// - **REMOVE_LAST_OCCURRENCE_DOES_NOT_REMOVE**: Forces removeLastOccurrence() to return true but not remove
///
/// ### Iterator Operations
/// - **DESCENDING_ITERATOR_THROWS_EXCEPTION**: Forces descendingIterator() to throw exceptions
/// - **DESCENDING_ITERATOR_RETURNS_EMPTY**: Forces descendingIterator() to return empty iterator
///
/// ### Element Access Breaks
/// - **GET_FIRST_RETURNS_NULL**: Forces getFirst() to return null instead of element
/// - **GET_FIRST_ALWAYS_THROWS**: Forces getFirst() to throw NoSuchElementException even when elements exist
/// - **GET_FIRST_SKIPS_FIRST_ELEMENT**: Forces getFirst() to return second element instead of first
/// - **GET_LAST_RETURNS_NULL**: Forces getLast() to return null instead of element
/// - **GET_LAST_ALWAYS_THROWS**: Forces getLast() to throw NoSuchElementException even when elements exist
/// - **GET_LAST_SKIPS_LAST_ELEMENT**: Forces getLast() to return second-to-last element instead of last
///
/// ### Positional Addition Breaks
/// - **ADD_FIRST_DOES_NOT_ADD_ELEMENT**: Forces addFirst() to accept elements but not add_singleElement_returnsTrueAndUpdatesSize them
/// - **ADD_FIRST_ADDS_TO_END**: Forces addFirst() to add_singleElement_returnsTrueAndUpdatesSize elements to end instead of beginning
/// - **ADD_LAST_DOES_NOT_ADD_ELEMENT**: Forces addLast() to accept elements but not add_singleElement_returnsTrueAndUpdatesSize them
/// - **ADD_LAST_ADDS_TO_FRONT**: Forces addLast() to add_singleElement_returnsTrueAndUpdatesSize elements to beginning instead of end
///
/// ### Positional Removal Breaks
/// - **REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT**: Forces removeFirst() to return element without removing it
/// - **REMOVE_FIRST_RETURNS_NULL**: Forces removeFirst() to return null instead of element
/// - **REMOVE_FIRST_ALWAYS_THROWS**: Forces removeFirst() to throw NoSuchElementException even when elements exist
/// - **REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT**: Forces removeLast() to return element without removing it
/// - **REMOVE_LAST_RETURNS_NULL**: Forces removeLast() to return null instead of element
/// - **REMOVE_LAST_ALWAYS_THROWS**: Forces removeLast() to throw NoSuchElementException even when elements exist
///
/// ## Usage Examples
///
/// ### Basic Deque Testing
/// ```java
/// BreakableDeque<String> deque = new BreakableDeque<>();
/// deque.addFirst("first");
/// deque.addLast("last");
/// deque.push("stack-top");
///
/// // Normal deque operations
/// assertEquals("stack-top", deque.peekFirst());
/// assertEquals("last", deque.peekLast());
/// assertEquals("stack-top", deque.pop());
/// ```
///
/// ### Deque Failure Testing
/// ```java
/// BreakableDeque<String> brokenDeque = new BreakableDeque.Builder<String>()
///     .addBreak(OFFER_FIRST_ALWAYS_RETURNS_FALSE)
///     .addBreak(POP_THROWS_EXCEPTION)
///     .build();
///
/// // Test failure behavior
/// // offerFirst() will return false due to break
/// // pop() will throw exception due to break
/// ```
///
/// ### Builder Pattern with Deque
/// ```java
/// BreakableDeque<Integer> deque = new BreakableDeque.Builder<Integer>()
///     .addBreak(PEEK_FIRST_ALWAYS_RETURNS_NULL)
///     .addBreak(POLL_LAST_DOES_NOT_REMOVE_ELEMENT)
///     .doesNotPermitNulls()
///     .build();
/// ```
///
/// ## Inheritance Hierarchy
///
/// ```
/// BreakableIterable<E>
///     ↓
/// BreakableCollection<E>
///     ↓
/// BreakableQueue<E>
///     ↓
/// BreakableDeque<E>
/// ```
///
/// This class inherits all Collection, Queue, and Iterable functionality from its parent classes
/// while adding Deque-specific capabilities. All inherited break constants and behaviors remain available.
///
/// ## Thread Safety
///
/// Like its parent classes, BreakableDeque is not thread-safe in terms of break configuration.
/// The underlying Deque implementation provides the thread safety for normal operations,
/// but break checking adds additional complexity that requires external synchronization for
/// concurrent break modifications.
///
/// ## Performance Considerations
///
/// Deque operations delegate to the underlying Deque implementation unless breaks
/// are applied. Break checking adds minimal overhead. The class maintains the performance
/// characteristics of the underlying Deque for non-broken operations.
///
/// @param <E> the type of elements held in this deque
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableQueue
/// @see Deque
/// @see Break
public class BreakableDeque<E> extends BreakableQueue<E> implements Deque<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ========== First-End Offer Method Breaks ==========

    /// Break constant that causes offerFirst() to always return false.
    ///
    /// When this break is applied, the offerFirst() method will return false
    /// regardless of whether the element was actually added to the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #offerFirst(Object)} - Returns false
    ///
    /// **Use Cases:**
    /// - Testing offerFirst() failure handling
    /// - Simulating capacity constraints at the front
    /// - Testing deque offer return value handling
    public static final Break OFFER_FIRST_ALWAYS_RETURNS_FALSE =
            new Break("OFFER_FIRST_ALWAYS_RETURNS_FALSE");

    /// Break constant that causes offerFirst() to accept elements but not actually add_singleElement_returnsTrueAndUpdatesSize them.
    ///
    /// When this break is applied, the offerFirst() method will return normally but
    /// the element will not be added to the front of the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #offerFirst(Object)} - Returns normally but doesn't add_singleElement_returnsTrueAndUpdatesSize element
    ///
    /// **Use Cases:**
    /// - Testing silent offerFirst() operation failures
    /// - Simulating memory or storage issues
    /// - Testing deque state consistency
    public static final Break OFFER_FIRST_DOES_NOT_ADD_ELEMENT =
            new Break("OFFER_FIRST_DOES_NOT_ADD_ELEMENT");

    // ========== Last-End Offer Method Breaks ==========

    /// Break constant that causes offerLast() to always return false.
    ///
    /// When this break is applied, the offerLast() method will return false
    /// regardless of whether the element was actually added to the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #offerLast(Object)} - Returns false
    ///
    /// **Use Cases:**
    /// - Testing offerLast() failure handling
    /// - Simulating capacity constraints at the back
    /// - Testing deque offer return value handling
    public static final Break OFFER_LAST_ALWAYS_RETURNS_FALSE =
            new Break("OFFER_LAST_ALWAYS_RETURNS_FALSE");

    /// Break constant that causes offerLast() to accept elements but not actually add_singleElement_returnsTrueAndUpdatesSize them.
    ///
    /// When this break is applied, the offerLast() method will return normally but
    /// the element will not be added to the back of the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #offerLast(Object)} - Returns normally but doesn't add_singleElement_returnsTrueAndUpdatesSize element
    ///
    /// **Use Cases:**
    /// - Testing silent offerLast() operation failures
    /// - Simulating memory or storage issues
    /// - Testing deque state consistency
    public static final Break OFFER_LAST_DOES_NOT_ADD_ELEMENT =
            new Break("OFFER_LAST_DOES_NOT_ADD_ELEMENT");

    // ========== First-End Poll Method Breaks ==========

    /// Break constant that causes pollFirst() to always return null.
    ///
    /// When this break is applied, the pollFirst() method will return null
    /// regardless of whether elements are available in the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #pollFirst()} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing pollFirst() null handling
    /// - Simulating empty deque scenarios
    /// - Testing null return value handling
    public static final Break POLL_FIRST_ALWAYS_RETURNS_NULL =
            new Break("POLL_FIRST_ALWAYS_RETURNS_NULL");

    /// Break constant that causes pollFirst() to return an element without removing it.
    ///
    /// When this break is applied, the pollFirst() method will return the first element
    /// but not remove it from the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #pollFirst()} - Returns element but doesn't remove it
    ///
    /// **Use Cases:**
    /// - Testing deque state consistency
    /// - Simulating removal operation failures
    /// - Testing duplicate element handling
    public static final Break POLL_FIRST_DOES_NOT_REMOVE_ELEMENT =
            new Break("POLL_FIRST_DOES_NOT_REMOVE_ELEMENT");

    // ========== Last-End Poll Method Breaks ==========

    /// Break constant that causes pollLast() to always return null.
    ///
    /// When this break is applied, the pollLast() method will return null
    /// regardless of whether elements are available in the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #pollLast()} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing pollLast() null handling
    /// - Simulating empty deque scenarios
    /// - Testing null return value handling
    public static final Break POLL_LAST_ALWAYS_RETURNS_NULL =
            new Break("POLL_LAST_ALWAYS_RETURNS_NULL");

    /// Break constant that causes pollLast() to return an element without removing it.
    ///
    /// When this break is applied, the pollLast() method will return the last element
    /// but not remove it from the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #pollLast()} - Returns element but doesn't remove it
    ///
    /// **Use Cases:**
    /// - Testing deque state consistency
    /// - Simulating removal operation failures
    /// - Testing duplicate element handling
    public static final Break POLL_LAST_DOES_NOT_REMOVE_ELEMENT =
            new Break("POLL_LAST_DOES_NOT_REMOVE_ELEMENT");

    // ========== First-End Peek Method Breaks ==========

    /// Break constant that causes peekFirst() to always return null.
    ///
    /// When this break is applied, the peekFirst() method will return null
    /// regardless of whether elements are available in the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #peekFirst()} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing peekFirst() null handling
    /// - Simulating empty deque scenarios
    /// - Testing null return value handling
    public static final Break PEEK_FIRST_ALWAYS_RETURNS_NULL =
            new Break("PEEK_FIRST_ALWAYS_RETURNS_NULL");

    /// Break constant that causes peekFirst() to return arbitrary elements.
    ///
    /// When this break is applied, the peekFirst() method will return a
    /// deterministic but arbitrary element instead of the actual first element.
    ///
    /// **Affected Methods:**
    /// - {@link #peekFirst()} - Returns arbitrary element
    ///
    /// **Use Cases:**
    /// - Testing robustness against incorrect peek values
    /// - Simulating corrupted deque state
    /// - Testing peek value validation logic
    public static final Break PEEK_FIRST_RETURNS_RANDOM_ELEMENT =
            new Break("PEEK_FIRST_RETURNS_RANDOM_ELEMENT");

    // ========== Last-End Peek Method Breaks ==========

    /// Break constant that causes peekLast() to always return null.
    ///
    /// When this break is applied, the peekLast() method will return null
    /// regardless of whether elements are available in the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #peekLast()} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing peekLast() null handling
    /// - Simulating empty deque scenarios
    /// - Testing null return value handling
    public static final Break PEEK_LAST_ALWAYS_RETURNS_NULL =
            new Break("PEEK_LAST_ALWAYS_RETURNS_NULL");

    /// Break constant that causes peekLast() to return arbitrary elements.
    ///
    /// When this break is applied, the peekLast() method will return a
    /// deterministic but arbitrary element instead of the actual last element.
    ///
    /// **Affected Methods:**
    /// - {@link #peekLast()} - Returns arbitrary element
    ///
    /// **Use Cases:**
    /// - Testing robustness against incorrect peek values
    /// - Simulating corrupted deque state
    /// - Testing peek value validation logic
    public static final Break PEEK_LAST_RETURNS_RANDOM_ELEMENT =
            new Break("PEEK_LAST_RETURNS_RANDOM_ELEMENT");

    // ========== Stack Operation Breaks ==========

    /// Break constant that causes push() to throw an exception.
    ///
    /// When this break is applied, the push() method will throw an IllegalStateException
    /// instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #push(Object)} - Throws IllegalStateException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in stack operations
    /// - Simulating stack operation failures
    /// - Testing robustness against unexpected exceptions
    public static final Break PUSH_THROWS_EXCEPTION =
            new Break("PUSH_THROWS_EXCEPTION");

    /// Break constant that causes push() to accept elements but not actually add_singleElement_returnsTrueAndUpdatesSize them.
    ///
    /// When this break is applied, the push() method will return normally but
    /// the element will not be added to the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #push(Object)} - Returns normally but doesn't add_singleElement_returnsTrueAndUpdatesSize element
    ///
    /// **Use Cases:**
    /// - Testing silent push() operation failures
    /// - Simulating memory or storage issues
    /// - Testing stack state consistency
    public static final Break PUSH_DOES_NOT_ADD_ELEMENT =
            new Break("PUSH_DOES_NOT_ADD_ELEMENT");

    /// Break constant that causes pop() to throw an exception.
    ///
    /// When this break is applied, the pop() method will throw an IllegalStateException
    /// instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #pop()} - Throws IllegalStateException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in stack operations
    /// - Simulating stack operation failures
    /// - Testing robustness against unexpected exceptions
    public static final Break POP_THROWS_EXCEPTION =
            new Break("POP_THROWS_EXCEPTION");

    /// Break constant that causes pop() to always return null.
    ///
    /// When this break is applied, the pop() method will return null instead
    /// of throwing NoSuchElementException or returning an element.
    ///
    /// **Affected Methods:**
    /// - {@link #pop()} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing null return value handling (non-standard behavior)
    /// - Simulating corrupted stack behavior
    /// - Testing robustness against unexpected return values
    public static final Break POP_ALWAYS_RETURNS_NULL =
            new Break("POP_ALWAYS_RETURNS_NULL");

    // ========== Occurrence Removal Method Breaks ==========

    /// Break constant that causes removeFirstOccurrence() to always return false.
    ///
    /// When this break is applied, the removeFirstOccurrence() method will return false
    /// regardless of whether the element was found and removed.
    ///
    /// **Affected Methods:**
    /// - {@link #removeFirstOccurrence(Object)} - Returns false
    ///
    /// **Use Cases:**
    /// - Testing removeFirstOccurrence() failure handling
    /// - Simulating search operation failures
    /// - Testing removal operation result handling
    public static final Break REMOVE_FIRST_OCCURRENCE_ALWAYS_RETURNS_FALSE =
            new Break("REMOVE_FIRST_OCCURRENCE_ALWAYS_RETURNS_FALSE");

    /// Break constant that causes removeLastOccurrence() to always return false.
    ///
    /// When this break is applied, the removeLastOccurrence() method will return false
    /// regardless of whether the element was found and removed.
    ///
    /// **Affected Methods:**
    /// - {@link #removeLastOccurrence(Object)} - Returns false
    ///
    /// **Use Cases:**
    /// - Testing removeLastOccurrence() failure handling
    /// - Simulating search operation failures
    /// - Testing removal operation result handling
    public static final Break REMOVE_LAST_OCCURRENCE_ALWAYS_RETURNS_FALSE =
            new Break("REMOVE_LAST_OCCURRENCE_ALWAYS_RETURNS_FALSE");

    /// Break constant that causes removeFirstOccurrence() to return true but not remove.
    ///
    /// When this break is applied, the removeFirstOccurrence() method will return true
    /// but not actually remove the element from the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #removeFirstOccurrence(Object)} - Returns true but doesn't remove element
    ///
    /// **Use Cases:**
    /// - Testing deque state consistency
    /// - Simulating partial removal operation failures
    /// - Testing duplicate element handling
    public static final Break REMOVE_FIRST_OCCURRENCE_DOES_NOT_REMOVE =
            new Break("REMOVE_FIRST_OCCURRENCE_DOES_NOT_REMOVE");

    /// Break constant that causes removeLastOccurrence() to return true but not remove.
    ///
    /// When this break is applied, the removeLastOccurrence() method will return true
    /// but not actually remove the element from the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #removeLastOccurrence(Object)} - Returns true but doesn't remove element
    ///
    /// **Use Cases:**
    /// - Testing deque state consistency
    /// - Simulating partial removal operation failures
    /// - Testing duplicate element handling
    public static final Break REMOVE_LAST_OCCURRENCE_DOES_NOT_REMOVE =
            new Break("REMOVE_LAST_OCCURRENCE_DOES_NOT_REMOVE");

    // ========== Iterator Method Breaks ==========

    /// Break constant that causes descendingIterator() to throw an exception.
    ///
    /// When this break is applied, the descendingIterator() method will throw an IllegalStateException
    /// instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #descendingIterator()} - Throws IllegalStateException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in iterator operations
    /// - Simulating iterator creation failures
    /// - Testing robustness against unexpected exceptions
    public static final Break DESCENDING_ITERATOR_THROWS_EXCEPTION =
            new Break("DESCENDING_ITERATOR_THROWS_EXCEPTION");

    /// Break constant that causes descendingIterator() to return an empty iterator.
    ///
    /// When this break is applied, the descendingIterator() method will return an
    /// empty iterator regardless of the deque contents.
    ///
    /// **Affected Methods:**
    /// - {@link #descendingIterator()} - Returns empty iterator
    ///
    /// **Use Cases:**
    /// - Testing empty iterator handling
    /// - Simulating corrupted iterator behavior
    /// - Testing iteration logic robustness
    public static final Break DESCENDING_ITERATOR_RETURNS_EMPTY =
            new Break("DESCENDING_ITERATOR_RETURNS_EMPTY");

    // ========== GetFirst/GetLast Method Breaks ==========

    /// Break constant that causes getFirst() to always return null.
    ///
    /// When this break is applied, the getFirst() method will return null instead
    /// of the first element or throwing NoSuchElementException. This violates the
    /// Deque contract for non-empty deques.
    ///
    /// **Affected Methods:**
    /// - {@link #getFirst()} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing null return value handling (non-standard behavior)
    /// - Simulating corrupted deque behavior
    /// - Testing robustness against unexpected return values
    public static final Break GET_FIRST_RETURNS_NULL =
            new Break("GET_FIRST_RETURNS_NULL");

    /// Break constant that causes getFirst() to always throw NoSuchElementException.
    ///
    /// When this break is applied, the getFirst() method will throw NoSuchElementException
    /// even when the deque contains elements.
    ///
    /// **Affected Methods:**
    /// - {@link #getFirst()} - Throws NoSuchElementException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in element access
    /// - Simulating element access failures
    /// - Testing robustness against unexpected exceptions
    public static final Break GET_FIRST_ALWAYS_THROWS =
            new Break("GET_FIRST_ALWAYS_THROWS");

    /// Break constant that causes getFirst() to skip the first element and return the second.
    ///
    /// When this break is applied, the getFirst() method will return the second element
    /// instead of the first element.
    ///
    /// **Affected Methods:**
    /// - {@link #getFirst()} - Returns second element instead of first
    ///
    /// **Use Cases:**
    /// - Testing positional access assumptions
    /// - Simulating corrupted deque order
    /// - Testing robustness against incorrect ordering
    public static final Break GET_FIRST_SKIPS_FIRST_ELEMENT =
            new Break("GET_FIRST_SKIPS_FIRST_ELEMENT");

    /// Break constant that causes getLast() to always return null.
    ///
    /// When this break is applied, the getLast() method will return null instead
    /// of the last element or throwing NoSuchElementException. This violates the
    /// Deque contract for non-empty deques.
    ///
    /// **Affected Methods:**
    /// - {@link #getLast()} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing null return value handling (non-standard behavior)
    /// - Simulating corrupted deque behavior
    /// - Testing robustness against unexpected return values
    public static final Break GET_LAST_RETURNS_NULL =
            new Break("GET_LAST_RETURNS_NULL");

    /// Break constant that causes getLast() to always throw NoSuchElementException.
    ///
    /// When this break is applied, the getLast() method will throw NoSuchElementException
    /// even when the deque contains elements.
    ///
    /// **Affected Methods:**
    /// - {@link #getLast()} - Throws NoSuchElementException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in element access
    /// - Simulating element access failures
    /// - Testing robustness against unexpected exceptions
    public static final Break GET_LAST_ALWAYS_THROWS =
            new Break("GET_LAST_ALWAYS_THROWS");

    /// Break constant that causes getLast() to skip the last element and return the second-to-last.
    ///
    /// When this break is applied, the getLast() method will return the second-to-last element
    /// instead of the last element.
    ///
    /// **Affected Methods:**
    /// - {@link #getLast()} - Returns second-to-last element instead of last
    ///
    /// **Use Cases:**
    /// - Testing positional access assumptions
    /// - Simulating corrupted deque order
    /// - Testing robustness against incorrect ordering
    public static final Break GET_LAST_SKIPS_LAST_ELEMENT =
            new Break("GET_LAST_SKIPS_LAST_ELEMENT");

    // ========== AddFirst/AddLast Method Breaks ==========

    /// Break constant that causes addFirst() to accept elements but not actually add_singleElement_returnsTrueAndUpdatesSize them.
    ///
    /// When this break is applied, the addFirst() method will return normally but
    /// the element will not be added to the front of the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #addFirst(Object)} - Returns normally but doesn't add_singleElement_returnsTrueAndUpdatesSize element
    ///
    /// **Use Cases:**
    /// - Testing silent addFirst() operation failures
    /// - Simulating memory or storage issues
    /// - Testing deque state consistency
    public static final Break ADD_FIRST_DOES_NOT_ADD_ELEMENT =
            new Break("ADD_FIRST_DOES_NOT_ADD_ELEMENT");

    /// Break constant that causes addFirst() to add_singleElement_returnsTrueAndUpdatesSize elements to the end instead of the front.
    ///
    /// When this break is applied, the addFirst() method will add_singleElement_returnsTrueAndUpdatesSize the element to the
    /// end of the deque instead of the front, violating deque ordering semantics.
    ///
    /// **Affected Methods:**
    /// - {@link #addFirst(Object)} - Adds element to end instead of front
    ///
    /// **Use Cases:**
    /// - Testing deque ordering assumptions
    /// - Simulating corrupted deque behavior
    /// - Testing robustness against incorrect positioning
    public static final Break ADD_FIRST_ADDS_TO_END =
            new Break("ADD_FIRST_ADDS_TO_END");

    /// Break constant that causes addLast() to accept elements but not actually add_singleElement_returnsTrueAndUpdatesSize them.
    ///
    /// When this break is applied, the addLast() method will return normally but
    /// the element will not be added to the end of the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #addLast(Object)} - Returns normally but doesn't add_singleElement_returnsTrueAndUpdatesSize element
    ///
    /// **Use Cases:**
    /// - Testing silent addLast() operation failures
    /// - Simulating memory or storage issues
    /// - Testing deque state consistency
    public static final Break ADD_LAST_DOES_NOT_ADD_ELEMENT =
            new Break("ADD_LAST_DOES_NOT_ADD_ELEMENT");

    /// Break constant that causes addLast() to add_singleElement_returnsTrueAndUpdatesSize elements to the front instead of the end.
    ///
    /// When this break is applied, the addLast() method will add_singleElement_returnsTrueAndUpdatesSize the element to the
    /// front of the deque instead of the end, violating deque ordering semantics.
    ///
    /// **Affected Methods:**
    /// - {@link #addLast(Object)} - Adds element to front instead of end
    ///
    /// **Use Cases:**
    /// - Testing deque ordering assumptions
    /// - Simulating corrupted deque behavior
    /// - Testing robustness against incorrect positioning
    public static final Break ADD_LAST_ADDS_TO_FRONT =
            new Break("ADD_LAST_ADDS_TO_FRONT");

    // ========== RemoveFirst/RemoveLast Method Breaks ==========

    /// Break constant that causes removeFirst() to return an element without removing it.
    ///
    /// When this break is applied, the removeFirst() method will return the first element
    /// but not remove it from the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #removeFirst()} - Returns element but doesn't remove it
    ///
    /// **Use Cases:**
    /// - Testing deque state consistency
    /// - Simulating removal operation failures
    /// - Testing duplicate element handling
    public static final Break REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT =
            new Break("REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT");

    /// Break constant that causes removeFirst() to always return null.
    ///
    /// When this break is applied, the removeFirst() method will return null instead
    /// of removing and returning an element. This violates the Deque contract.
    ///
    /// **Affected Methods:**
    /// - {@link #removeFirst()} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing null return value handling (non-standard behavior)
    /// - Simulating corrupted deque behavior
    /// - Testing robustness against unexpected return values
    public static final Break REMOVE_FIRST_RETURNS_NULL =
            new Break("REMOVE_FIRST_RETURNS_NULL");

    /// Break constant that causes removeFirst() to always throw NoSuchElementException.
    ///
    /// When this break is applied, the removeFirst() method will throw NoSuchElementException
    /// even when the deque contains elements.
    ///
    /// **Affected Methods:**
    /// - {@link #removeFirst()} - Throws NoSuchElementException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in removal operations
    /// - Simulating removal operation failures
    /// - Testing robustness against unexpected exceptions
    public static final Break REMOVE_FIRST_ALWAYS_THROWS =
            new Break("REMOVE_FIRST_ALWAYS_THROWS");

    /// Break constant that causes removeLast() to return an element without removing it.
    ///
    /// When this break is applied, the removeLast() method will return the last element
    /// but not remove it from the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #removeLast()} - Returns element but doesn't remove it
    ///
    /// **Use Cases:**
    /// - Testing deque state consistency
    /// - Simulating removal operation failures
    /// - Testing duplicate element handling
    public static final Break REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT =
            new Break("REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT");

    /// Break constant that causes removeLast() to always return null.
    ///
    /// When this break is applied, the removeLast() method will return null instead
    /// of removing and returning an element. This violates the Deque contract.
    ///
    /// **Affected Methods:**
    /// - {@link #removeLast()} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing null return value handling (non-standard behavior)
    /// - Simulating corrupted deque behavior
    /// - Testing robustness against unexpected return values
    public static final Break REMOVE_LAST_RETURNS_NULL =
            new Break("REMOVE_LAST_RETURNS_NULL");

    /// Break constant that causes removeLast() to always throw NoSuchElementException.
    ///
    /// When this break is applied, the removeLast() method will throw NoSuchElementException
    /// even when the deque contains elements.
    ///
    /// **Affected Methods:**
    /// - {@link #removeLast()} - Throws NoSuchElementException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in removal operations
    /// - Simulating removal operation failures
    /// - Testing robustness against unexpected exceptions
    public static final Break REMOVE_LAST_ALWAYS_THROWS =
            new Break("REMOVE_LAST_ALWAYS_THROWS");

    // ========== Instance Fields ==========

    /// The underlying Deque that this BreakableDeque wraps.
    /// All operations delegate to this deque unless breaks are applied.
    private final @NonNull Deque<E> deque;

    // ========== Constructors ==========

    /// Creates an empty BreakableDeque backed by an ArrayDeque.
    ///
    /// This constructor creates a new BreakableDeque backed by an empty ArrayDeque.
    /// The deque will have default null policies and no breaks applied.
    ///
    /// **Default Configuration:**
    /// - Empty Deque (ArrayDeque implementation)
    /// - Permits null elements: true
    /// - No breaks applied
    ///
    /// **Usage:**
    /// ```java
    /// BreakableDeque<String> deque = new BreakableDeque<>();
    /// deque.addFirst("first");
    /// deque.addLast("last");
    /// ```
    public BreakableDeque() {
        this(new LinkedList<>(), new HashSet<>(), new HashMap<>(), DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS,
                DEFAULT_SAFETY, Object.class);
    }

    /// Creates a BreakableDeque with the initial elements.
    ///
    /// @param elements the initial elements to add_singleElement_returnsTrueAndUpdatesSize to the deque; must not be null
    /// @throws NullPointerException if elements is null
    public BreakableDeque(final @NonNull Collection<E> elements) {
        this(new LinkedList<>(elements), new HashSet<>(), new HashMap<>(), DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS,
                DEFAULT_SAFETY, Object.class);
    }

    /// Creates a BreakableDeque by copying another BreakableDeque.
    ///
    /// This constructor creates a new instance that shares the underlying deque data
    /// and inherits all configuration from the source deque.
    ///
    /// **Inherited Configuration:**
    /// - All break settings from source
    /// - Null element policies
    /// - Method support configuration
    /// - Underlying Deque reference (shared, not copied)
    ///
    /// **Usage:**
    /// ```java
    /// BreakableDeque<String> original = new BreakableDeque<>();
    /// BreakableDeque<String> copy = new BreakableDeque<>(original);
    /// ```
    ///
    /// @param other the BreakableDeque to copy configuration from
    /// @throws NullPointerException if other is null
    public BreakableDeque(final @NonNull BreakableDeque<E> other) {
        this(new LinkedList<>(other.deque), new HashSet<>(other.breaks()), new HashMap<>(other.methodStatuses()),
                other.characteristics(), other.permits(), other.isSafe(), other.compatibleType());
        other.unsupportedMethods().forEach(this::doesNotSupportMethod);
    }

    /// Creates a BreakableDeque with the specified configuration.
    ///
    /// This constructor allows full control over the BreakableDeque configuration,
    /// including the underlying Deque, breaks, and spliterator characteristics.
    ///
    /// **Usage:**
    /// ```java
    /// ArrayDeque<String> arrayDeque = new ArrayDeque<>();
    /// Set<Break> breaks = Set.of(OFFER_FIRST_ALWAYS_RETURNS_FALSE);
    /// BreakableDeque<String> deque = new BreakableDeque<>(
    ///     arrayDeque, breaks, 0);
    /// ```
    ///
    /// @param deque           the Deque to wrap
    /// @param breaks          the breaks to apply
    /// @param methodStatuses  the method status configuration.
    /// @param characteristics the spliterator characteristics
    /// @param permits         the flags that indicate what types of values are supported by the collection.
    /// @param isSafe          whether the resulting object is safe for concurrent access.
    /// @param componentType   the type of the elements in the collection.
    /// @throws NullPointerException if deque or breaks is null
    protected BreakableDeque(
            final @NonNull Deque<E> deque,
            final @NonNull Set<Break> breaks,
            final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses,
            final int characteristics,
            final int permits,
            final boolean isSafe,
            final Class<?> componentType) {
        super(this.deque = deque, breaks, methodStatuses, characteristics, permits, isSafe, componentType);
    }

    @Override
    protected boolean valueEquals(final BreakableObject<?> other) {
        if (other instanceof BreakableDeque<?> that) {
            return deque.equals(that.deque);
        }
        return false;
    }

    // ========== Deque Interface Implementation ==========

    /// {@inheritDoc}
    ///
    /// Inserts the specified element at the front of this deque if it is possible to do so
    /// immediately without violating capacity restrictions.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #OFFER_FIRST_ALWAYS_RETURNS_FALSE} - Returns false regardless of success
    /// - {@link #OFFER_FIRST_DOES_NOT_ADD_ELEMENT} - Returns normally but doesn't add_singleElement_returnsTrueAndUpdatesSize element
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    @Override
    public boolean offerFirst(final @NonNull E e) {
        checkMethodSupport(DequeMethods.OFFER_FIRST);

        if (hasBreak(OFFER_FIRST_DOES_NOT_ADD_ELEMENT)) {
            checkNewElement(e);
            return true; // Return true but don't actually add_singleElement_returnsTrueAndUpdatesSize the element
        }

        // Add the element first
        checkNewElement(e);
        boolean result = deque.offerFirst(e);

        if (hasBreak(OFFER_FIRST_ALWAYS_RETURNS_FALSE)) {
            return false;
        }

        return result;
    }

    /// {@inheritDoc}
    ///
    /// Inserts the specified element at the end of this deque if it is possible to do so
    /// immediately without violating capacity restrictions.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #OFFER_LAST_ALWAYS_RETURNS_FALSE} - Returns false regardless of success
    /// - {@link #OFFER_LAST_DOES_NOT_ADD_ELEMENT} - Returns normally but doesn't add_singleElement_returnsTrueAndUpdatesSize element
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    @Override
    public boolean offerLast(final @NonNull E e) {
        checkMethodSupport(DequeMethods.OFFER_LAST);

        if (hasBreak(OFFER_LAST_DOES_NOT_ADD_ELEMENT)) {
            checkNewElement(e);
            return true; // Return true but don't add_singleElement_returnsTrueAndUpdatesSize the element
        }

        // Add the element first
        checkNewElement(e);
        boolean result = deque.offerLast(e);

        if (hasBreak(OFFER_LAST_ALWAYS_RETURNS_FALSE)) {
            return false;
        }

        return result;
    }

    /// {@inheritDoc}
    ///
    /// Retrieves and removes the first element of this deque, or returns null if this deque is empty.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #POLL_FIRST_ALWAYS_RETURNS_NULL} - Returns null regardless of content
    /// - {@link #POLL_FIRST_DOES_NOT_REMOVE_ELEMENT} - Returns element but doesn't remove it
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    @Override
    public @Nullable E pollFirst() {
        checkMethodSupport(DequeMethods.POLL_FIRST);

        if (hasBreak(POLL_FIRST_ALWAYS_RETURNS_NULL)) {
            return null;
        }

        if (hasBreak(POLL_FIRST_DOES_NOT_REMOVE_ELEMENT)) {
            return deque.peekFirst(); // Return element but don't remove it
        }

        return deque.pollFirst();
    }

    /// {@inheritDoc}
    ///
    /// Retrieves and removes the last element of this deque, or returns null if this deque is empty.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #POLL_LAST_ALWAYS_RETURNS_NULL} - Returns null regardless of content
    /// - {@link #POLL_LAST_DOES_NOT_REMOVE_ELEMENT} - Returns element but doesn't remove it
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    @Override
    public @Nullable E pollLast() {
        checkMethodSupport(DequeMethods.POLL_LAST);

        if (hasBreak(POLL_LAST_ALWAYS_RETURNS_NULL)) {
            return null;
        }

        if (hasBreak(POLL_LAST_DOES_NOT_REMOVE_ELEMENT)) {
            return deque.peekLast(); // Return element but don't remove it
        }

        return deque.pollLast();
    }

    /// {@inheritDoc}
    ///
    /// Retrieves, but does not remove, the first element of this deque, or returns null if this deque is empty.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #PEEK_FIRST_ALWAYS_RETURNS_NULL} - Returns null regardless of content
    /// - {@link #PEEK_FIRST_RETURNS_RANDOM_ELEMENT} - Returns arbitrary element
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    @Override
    public @Nullable E peekFirst() {
        checkMethodSupport(DequeMethods.PEEK_FIRST);

        if (hasBreak(PEEK_FIRST_ALWAYS_RETURNS_NULL)) {
            return null;
        }

        if (hasBreak(PEEK_FIRST_RETURNS_RANDOM_ELEMENT) && deque.size() > 1) {
            randomElementExcludingIndex(0);
        }

        return deque.peekFirst();
    }

    /// {@inheritDoc}
    ///
    /// Retrieves, but does not remove, the last element of this deque, or returns null if this deque is empty.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #PEEK_LAST_ALWAYS_RETURNS_NULL} - Returns null regardless of content
    /// - {@link #PEEK_LAST_RETURNS_RANDOM_ELEMENT} - Returns arbitrary element
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    @Override
    public @Nullable E peekLast() {
        checkMethodSupport(DequeMethods.PEEK_LAST);

        if (hasBreak(PEEK_LAST_ALWAYS_RETURNS_NULL)) {
            return null;
        }

        if (hasBreak(PEEK_LAST_RETURNS_RANDOM_ELEMENT) && !deque.isEmpty()) {
            return randomElementExcludingIndex(size() - 1);
        }

        return deque.peekLast();
    }

    /// {@inheritDoc}
    ///
    /// Removes the first occurrence of the specified element from this deque.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #REMOVE_FIRST_OCCURRENCE_ALWAYS_RETURNS_FALSE} - Returns false regardless of removal success
    /// - {@link #REMOVE_FIRST_OCCURRENCE_DOES_NOT_REMOVE} - Returns true but doesn't remove element
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    @Override
    public boolean removeFirstOccurrence(final Object o) {
        checkMethodSupport(DequeMethods.REMOVE_FIRST_OCCURRENCES);

        if (hasBreak(REMOVE_FIRST_OCCURRENCE_ALWAYS_RETURNS_FALSE)) {
            return false;
        }

        if (hasBreak(REMOVE_FIRST_OCCURRENCE_DOES_NOT_REMOVE)) {
            return deque.contains(o); // Return true if found but don't actually remove
        }

        return deque.removeFirstOccurrence(o);
    }

    /// {@inheritDoc}
    ///
    /// Removes the last occurrence of the specified element from this deque.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #REMOVE_LAST_OCCURRENCE_ALWAYS_RETURNS_FALSE} - Returns false regardless of removal success
    /// - {@link #REMOVE_LAST_OCCURRENCE_DOES_NOT_REMOVE} - Returns true but doesn't remove element
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    @Override
    public boolean removeLastOccurrence(final Object o) {
        checkMethodSupport(DequeMethods.REMOVE_LAST_OCCURRENCES);

        if (hasBreak(REMOVE_LAST_OCCURRENCE_ALWAYS_RETURNS_FALSE)) {
            return false;
        }

        if (hasBreak(REMOVE_LAST_OCCURRENCE_DOES_NOT_REMOVE)) {
            return deque.contains(o); // Return true if found but don't actually remove
        }

        return deque.removeLastOccurrence(o);
    }

    /// {@inheritDoc}
    ///
    /// Pushes an element onto the stack represented by this deque (in other words, at the head of this deque).
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #PUSH_THROWS_EXCEPTION} - Throws IllegalStateException
    /// - {@link #PUSH_DOES_NOT_ADD_ELEMENT} - Returns normally but doesn't add_singleElement_returnsTrueAndUpdatesSize element
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw IllegalStateException due to PUSH_THROWS_EXCEPTION break
    @Override
    public void push(final E e) {
        checkMethodSupport(DequeMethods.PUSH);
        checkNewElement(e);

        if (hasBreak(PUSH_THROWS_EXCEPTION)) {
            throw new IllegalStateException("Push operation failed due to break");
        }

        if (hasBreak(PUSH_DOES_NOT_ADD_ELEMENT)) {
            return; // Return without adding the element
        }

        deque.push(e);
    }

    /// {@inheritDoc}
    ///
    /// Pops an element from the stack represented by this deque (in other words, removes and returns the element at the head of this deque).
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #POP_THROWS_EXCEPTION} - Throws IllegalStateException
    /// - {@link #POP_ALWAYS_RETURNS_NULL} - Returns null instead of element
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw IllegalStateException due to POP_THROWS_EXCEPTION break
    @SuppressWarnings("DataFlowIssue") // enable NonNull method to return null when broken
    @Override
    public @NonNull E pop() {
        if (!supportsMethod(DequeMethods.POP)) {
            throw new UnsupportedOperationException("pop() method is not supported");
        }

        if (hasBreak(POP_THROWS_EXCEPTION)) {
            throw new IllegalStateException("Pop operation failed due to break");
        }

        if (hasBreak(POP_ALWAYS_RETURNS_NULL)) {
            return null; // Non-standard behavior for testing
        }

        return deque.pop();
    }

    /// {@inheritDoc}
    ///
    /// Returns an iterator over the elements in this deque in reverse sequential order.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #DESCENDING_ITERATOR_THROWS_EXCEPTION} - Throws IllegalStateException
    /// - {@link #DESCENDING_ITERATOR_RETURNS_EMPTY} - Returns empty iterator
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw IllegalStateException due to DESCENDING_ITERATOR_THROWS_EXCEPTION break
    @Override
    public @NonNull Iterator<E> descendingIterator() {
        if (!supportsMethod(DequeMethods.DESCENDING_ITERATOR)) {
            throw new UnsupportedOperationException("descendingIterator() method is not supported");
        }

        if (hasBreak(DESCENDING_ITERATOR_THROWS_EXCEPTION)) {
            throw new IllegalStateException("DescendingIterator operation failed due to break");
        }

        if (hasBreak(DESCENDING_ITERATOR_RETURNS_EMPTY)) {
            return Collections.emptyIterator(); // Return empty iterator
        }

        return deque.descendingIterator();
    }

    /// {@inheritDoc}
    ///
    /// Returns the first element of this deque.
    /// This method is inherited from SequencedCollection and required by the Deque interface.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #GET_FIRST_RETURNS_NULL} - Returns null instead of element
    /// - {@link #GET_FIRST_ALWAYS_THROWS} - Throws NoSuchElementException even when elements exist
    /// - {@link #GET_FIRST_SKIPS_FIRST_ELEMENT} - Returns second element instead of first
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - Throws NoSuchElementException if this deque is empty (or due to breaks)
    @SuppressWarnings("DataFlowIssue") // enable NonNull method to return null when broken
    @Override
    public @NonNull E getFirst() {
        if (!supportsMethod(SequencedCollectionMethods.GET_FIRST)) {
            throw new UnsupportedOperationException("getFirst() method is not supported");
        }

        if (hasBreak(GET_FIRST_RETURNS_NULL)) {
            return null; // Non-standard behavior for testing
        }

        if (hasBreak(GET_FIRST_ALWAYS_THROWS)) {
            throw new NoSuchElementException("getFirst() failed due to break");
        }

        if (hasBreak(GET_FIRST_SKIPS_FIRST_ELEMENT)) {
            Iterator<E> iter = deque.iterator();
            if (iter.hasNext()) {
                iter.next(); // Skip first element
                if (iter.hasNext()) {
                    return iter.next(); // Return second element
                }
            }
            throw new NoSuchElementException("No second element available");
        }

        return deque.getFirst();
    }

    /// {@inheritDoc}
    ///
    /// Returns the last element of this deque.
    /// This method is inherited from SequencedCollection and required by the Deque interface.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #GET_LAST_RETURNS_NULL} - Returns null instead of element
    /// - {@link #GET_LAST_ALWAYS_THROWS} - Throws NoSuchElementException even when elements exist
    /// - {@link #GET_LAST_SKIPS_LAST_ELEMENT} - Returns second-to-last element instead of last
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - Throws NoSuchElementException if this deque is empty (or due to breaks)
    @SuppressWarnings("DataFlowIssue") // enable NonNull method to return null when broken
    @Override
    public @NonNull E getLast() {
        if (!supportsMethod(SequencedCollectionMethods.GET_LAST)) {
            throw new UnsupportedOperationException("getLast() method is not supported");
        }

        if (hasBreak(GET_LAST_RETURNS_NULL)) {
            return null; // Non-standard behavior for testing
        }

        if (hasBreak(GET_LAST_ALWAYS_THROWS)) {
            throw new NoSuchElementException("getLast() failed due to break");
        }

        if (hasBreak(GET_LAST_SKIPS_LAST_ELEMENT)) {
            Iterator<E> iter = deque.descendingIterator();
            if (iter.hasNext()) {
                iter.next(); // Skip last element
                if (iter.hasNext()) {
                    return iter.next(); // Return second-to-last element
                }
            }
            throw new NoSuchElementException("No second-to-last element available");
        }

        return deque.getLast();
    }

    /// {@inheritDoc}
    ///
    /// Inserts the specified element at the beginning of this deque.
    /// This method is inherited from SequencedCollection and required by the Deque interface.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #ADD_FIRST_DOES_NOT_ADD_ELEMENT} - Returns normally but doesn't add_singleElement_returnsTrueAndUpdatesSize element
    /// - {@link #ADD_FIRST_ADDS_TO_END} - Adds element to end instead of beginning
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    @Override
    public void addFirst(final @NonNull E e) {
        if (!supportsMethod(SequencedCollectionMethods.ADD_FIRST)) {
            throw new UnsupportedOperationException("addFirst() method is not supported");
        }

        checkNewElement(e);

        if (hasBreak(ADD_FIRST_DOES_NOT_ADD_ELEMENT)) {
            return; // Return without adding the element
        }

        if (hasBreak(ADD_FIRST_ADDS_TO_END)) {
            deque.addLast(e); // Add to end instead of beginning
            return;
        }

        deque.addFirst(e);
    }

    /// {@inheritDoc}
    ///
    /// Inserts the specified element at the end of this deque.
    /// This method is inherited from SequencedCollection and required by the Deque interface.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #ADD_LAST_DOES_NOT_ADD_ELEMENT} - Returns normally but doesn't add_singleElement_returnsTrueAndUpdatesSize element
    /// - {@link #ADD_LAST_ADDS_TO_FRONT} - Adds element to beginning instead of end
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    @Override
    public void addLast(final @NonNull E e) {
        if (!supportsMethod(SequencedCollectionMethods.ADD_LAST)) {
            throw new UnsupportedOperationException("addLast() method is not supported");
        }

        checkNewElement(e);

        if (hasBreak(ADD_LAST_DOES_NOT_ADD_ELEMENT)) {
            return; // Return without adding the element
        }

        if (hasBreak(ADD_LAST_ADDS_TO_FRONT)) {
            deque.addFirst(e); // Add to beginning instead of end
            return;
        }

        deque.addLast(e);
    }

    /// {@inheritDoc}
    ///
    /// Removes and returns the first element from this deque.
    /// This method is inherited from SequencedCollection and required by the Deque interface.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT} - Returns element but doesn't remove it
    /// - {@link #REMOVE_FIRST_RETURNS_NULL} - Returns null instead of element
    /// - {@link #REMOVE_FIRST_ALWAYS_THROWS} - Throws NoSuchElementException even when elements exist
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - Throws NoSuchElementException if this deque is empty (or due to breaks)
    @SuppressWarnings("DataFlowIssue") // enable NonNull method to return null when broken
    @Override
    public @NonNull E removeFirst() {
        if (!supportsMethod(SequencedCollectionMethods.REMOVE_FIRST)) {
            throw new UnsupportedOperationException("removeFirst() method is not supported");
        }

        if (hasBreak(REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT)) {
            return deque.getFirst(); // Return element but don't remove it
        }

        if (hasBreak(REMOVE_FIRST_RETURNS_NULL)) {
            return null; // Non-standard behavior for testing
        }

        if (hasBreak(REMOVE_FIRST_ALWAYS_THROWS)) {
            throw new NoSuchElementException("removeFirst() failed due to break");
        }

        return deque.removeFirst();
    }

    /// {@inheritDoc}
    ///
    /// Removes and returns the last element from this deque.
    /// This method is inherited from SequencedCollection and required by the Deque interface.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT} - Returns element but doesn't remove it
    /// - {@link #REMOVE_LAST_RETURNS_NULL} - Returns null instead of element
    /// - {@link #REMOVE_LAST_ALWAYS_THROWS} - Throws NoSuchElementException even when elements exist
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - Throws NoSuchElementException if this deque is empty (or due to breaks)
    @SuppressWarnings("DataFlowIssue") // enable NonNull method to return null when broken
    @Override
    public @NonNull E removeLast() {
        if (!supportsMethod(SequencedCollectionMethods.REMOVE_LAST)) {
            throw new UnsupportedOperationException("removeLast() method is not supported");
        }

        if (hasBreak(REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT)) {
            return deque.getLast(); // Return element but don't remove it
        }

        if (hasBreak(REMOVE_LAST_RETURNS_NULL)) {
            return null; // Non-standard behavior for testing
        }

        if (hasBreak(REMOVE_LAST_ALWAYS_THROWS)) {
            throw new NoSuchElementException("removeLast() failed due to break");
        }

        return deque.removeLast();
    }

    // ========== Builder Class ==========

    /// Builder class for creating BreakableDeque instances with fluent configuration.
    ///
    /// This builder extends BreakableQueue.Builder and provides additional configuration
    /// options specific to Deque functionality.
    ///
    /// @param <E> the type of elements held in this deque
    public static class Builder<E> extends AbstractBuilder<Builder<E>, BreakableDeque<E>, E> {

        /// Creates a new Builder with default configuration.
        public Builder() {
            super();
        }

        /// Creates a new Builder by copying configuration from another Builder.
        ///
        /// This copy constructor creates a new Builder instance with all configuration
        /// copied from the source builder. This is useful for creating variations of
        /// builder configurations without modifying the original.
        ///
        /// **Copied Configuration:**
        /// - All elements in the deque
        /// - All applied breaks
        /// - Method status configuration
        /// - Spliterator characteristics
        /// - Permit settings (null policies, etc.)
        ///
        /// **Usage:**
        /// ```java
        /// Builder<String> original = new Builder<String>()
        ///     .addBreak(OFFER_FIRST_ALWAYS_RETURNS_FALSE)
        ///     .addElement("item1");
        ///
        /// Builder<String> copy = new Builder<>(original);
        /// copy.addBreak(PUSH_THROWS_EXCEPTION); // Add additional break to copy
        ///
        /// BreakableDeque<String> deque1 = original.build();
        /// BreakableDeque<String> deque2 = copy.build(); // Has both breaks
        /// ```
        ///
        /// **Performance Considerations:**
        /// Creates defensive copies of all collections to ensure independence between
        /// builder instances.
        ///
        /// **Thread Safety:**
        /// This constructor is not thread-safe. External synchronization is required
        /// if the source builder is being accessed concurrently.
        ///
        /// @param other the Builder to copy configuration from; must not be null
        /// @throws NullPointerException if other is null
        /// @see #Builder()
        /// @see #copy()
        public Builder(final @NonNull Builder<E> other) {
            super(other);
        }

        /// Creates a new Builder that will wrap the specified Deque.
        ///
        /// This constructor allows you to create a BreakableDeque that wraps an existing
        /// Deque implementation. The builder will use the provided deque as the underlying
        /// storage, allowing you to apply breaks to an existing deque instance.
        ///
        /// **Configuration:**
        /// - Uses the provided Deque as underlying storage
        /// - Default null policies apply
        /// - No breaks applied initially
        /// - Default spliterator characteristics
        ///
        /// **Usage:**
        /// ```java
        /// ArrayDeque<String> existingDeque = new ArrayDeque<>();
        /// existingDeque.add_singleElement_returnsTrueAndUpdatesSize("item1");
        /// existingDeque.add_singleElement_returnsTrueAndUpdatesSize("item2");
        ///
        /// BreakableDeque<String> breakable = new BreakableDeque.Builder<>(existingDeque)
        ///     .addBreak(POLL_FIRST_ALWAYS_RETURNS_NULL)
        ///     .build();
        /// ```
        ///
        /// **Thread Safety:**
        /// This constructor is not thread-safe. External synchronization is required
        /// if the provided deque is being accessed concurrently.
        ///
        /// @param elements The elements to initialize the collection with
        /// @throws NullPointerException if deque is null
        /// @see #Builder()
        /// @see #Builder(Builder)
        public Builder(final @NonNull Collection<E> elements) {
            super(elements);
        }

        /// {@inheritDoc}
        @Override
        public Builder<E> self() {
            return this;
        }

        /// {@inheritDoc}
        @Override
        public @NonNull Builder<E> copy() {
            return new Builder<>(this);
        }

        /// Builds a new BreakableDeque instance with the configured settings.
        ///
        /// This method creates and returns a new BreakableDeque instance configured with
        /// all the settings specified through the builder methods. The builder can be reused
        /// to create multiple BreakableDeque instances with the same configuration.
        ///
        /// **Applied Configuration:**
        /// - All breaks added via addBreak()
        /// - Method support configuration via doesNotSupport()
        /// - Null element policies via permitNulls() or doesNotPermitNulls()
        /// - Spliterator characteristics
        /// - Underlying deque instance
        ///
        /// **Usage:**
        /// ```java
        /// Builder<String> builder = new Builder<String>()
        ///     .addBreak(OFFER_FIRST_ALWAYS_RETURNS_FALSE)
        ///     .addBreak(PUSH_THROWS_EXCEPTION)
        ///     .doesNotPermitNulls();
        ///
        /// BreakableDeque<String> deque1 = builder.build();
        /// BreakableDeque<String> deque2 = builder.build(); // Reuse builder
        /// ```
        ///
        /// **Thread Safety:**
        /// This method is not thread-safe. External synchronization is required
        /// if the builder is being accessed concurrently.
        ///
        /// @return a new BreakableDeque instance with the configured settings
        /// @see #copy()
        @Override
        public @NonNull BreakableDeque<E> build() {
            return new BreakableDeque<>(new LinkedList<>(elements()), breaks(), methodStatuses(), characteristics(),
                    permits(), isSafe(), compatibleType());
        }
    }

    // ========== Static Factory Methods ==========

    /// Creates a BreakableDeque that wraps the specified ArrayDeque with the given breaks.
    ///
    /// This static factory method provides a convenient way to create a BreakableDeque
    /// that wraps an existing ArrayDeque implementation with specified break behaviors.
    /// The resulting BreakableDeque will delegate all operations to the wrapped deque
    /// unless modified by the applied breaks.
    ///
    /// **Configuration:**
    /// - Uses default spliterator characteristics
    /// - Uses default permit settings (allows nulls by default)
    /// - Applies all specified breaks immediately
    /// - All Deque methods are supported unless explicitly disabled
    ///
    /// **Usage Examples:**
    /// ```java
    /// // Wrap an ArrayDeque with specific breaks
    /// ArrayDeque<String> arrayDeque = new ArrayDeque<>();
    /// arrayDeque.addFirst("first");
    /// arrayDeque.addLast("last");
    ///
    /// Set<Break> breaks = Set.of(
    ///     POLL_FIRST_ALWAYS_RETURNS_NULL,
    ///     PUSH_THROWS_EXCEPTION
    /// );
    ///
    /// BreakableDeque<String> brokenDeque = BreakableDeque.wrap(arrayDeque, breaks);
    /// assertNull(brokenDeque.pollFirst()); // Returns null due to break
    /// assertThrows(IllegalStateException.class, () -> brokenDeque.push("item")); // Throws due to break
    /// ```
    ///
    /// **Break Application:**
    /// The breaks are applied immediately upon creation and will affect the behavior
    /// of corresponding Deque methods. See individual break constants for specific effects.
    ///
    /// **Thread Safety:**
    /// The created BreakableDeque is not thread-safe regardless of the underlying
    /// deque's thread safety properties. External synchronization is required for
    /// concurrent access.
    ///
    /// @param <E> the type of elements held in the deque
    /// @param deque the ArrayDeque implementation to wrap; must not be null
    /// @param breaks the set of breaks to apply; must not be null, may be empty
    /// @return a new BreakableDeque wrapping the specified deque with the given breaks
    /// @throws NullPointerException if deque or breaks is null
    /// @see #wrap(Deque, Set, int)
    /// @see Break
    /// @see Builder
    public static <E> @NonNull BreakableDeque<E> wrap(
            final @NonNull LinkedList<E> deque,
            final @NonNull Set<Break> breaks) {
        return new BreakableDeque<>(deque, breaks, DEFAULT_METHOD_STATUSES, DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS,
                DEFAULT_SAFETY, Object.class);
    }

    /// Creates a BreakableDeque that wraps the specified Deque with full configuration.
    ///
    /// This static factory method provides complete control over BreakableDeque creation,
    /// allowing specification of the underlying Deque, breaks to apply, and spliterator
    /// characteristics. This method is useful when precise control over spliterator
    /// behavior is required for advanced use cases.
    ///
    /// **Configuration:**
    /// - Uses specified spliterator characteristics
    /// - Uses default permit settings (allows nulls by default)
    /// - Applies all specified breaks immediately
    /// - All Deque methods are supported unless explicitly disabled
    ///
    /// **Usage Examples:**
    /// ```java
    /// // Create with custom spliterator characteristics
    /// LinkedList<Integer> linkedList = new LinkedList<>();
    /// linkedList.add_singleElement_returnsTrueAndUpdatesSize(1);
    /// linkedList.add_singleElement_returnsTrueAndUpdatesSize(2);
    /// linkedList.add_singleElement_returnsTrueAndUpdatesSize(3);
    ///
    /// Set<Break> breaks = Set.of(OFFER_FIRST_ALWAYS_RETURNS_FALSE);
    /// int characteristics = Spliterator.ORDERED | Spliterator.SIZED;
    ///
    /// BreakableDeque<Integer> deque = BreakableDeque.wrap(
    ///     linkedList, breaks, characteristics);
    /// assertFalse(deque.offerFirst(4)); // Returns false due to break
    /// ```
    ///
    /// **Spliterator Characteristics:**
    /// The characteristics parameter controls the behavior of spliterators created
    /// from this deque. Common characteristics include:
    /// - `Spliterator.ORDERED` - Elements have a defined encounter order
    /// - `Spliterator.SIZED` - Size is known and finite
    /// - `Spliterator.NONNULL` - No null elements
    /// - `Spliterator.DISTINCT` - All elements are distinct
    ///
    /// **Thread Safety:**
    /// The created BreakableDeque is not thread-safe regardless of the underlying
    /// deque's thread safety properties. External synchronization is required for
    /// concurrent access.
    ///
    /// @param <E> the type of elements held in the deque
    /// @param deque the Deque implementation to wrap; must not be null
    /// @param breaks the set of breaks to apply; must not be null, may be empty
    /// @param characteristics the spliterator characteristics for this deque
    /// @return a new BreakableDeque wrapping the specified deque with the given configuration
    /// @throws NullPointerException if deque or breaks is null
    /// @see #wrap(LinkedList, Set)
    /// @see Break
    /// @see Builder
    /// @see java.util.Spliterator
    public static <E> @NonNull BreakableDeque<E> wrap(
            final @NonNull Deque<E> deque,
            final @NonNull Set<Break> breaks,
            final int characteristics) {
        return new BreakableDeque<>(deque, breaks, DEFAULT_METHOD_STATUSES, characteristics, DEFAULT_PERMITS,
                DEFAULT_SAFETY, Object.class);
    }
    /// Creates a collection provider for instances of BreakableDeque, given an element provider.
    /// @param <E> the element type.
    /// @param elementProvider the element provider to use.
    /// @return a collection provider for breakable deques.
    public static <E> @NonNull CollectionProvider<E, BreakableDeque<E>> dequeProvider(
            final @NonNull ObjectProvider<E> elementProvider) {
        return CollectionProviders.from(
                BreakableDeque::new,
                BreakableDeque::new,
                (c) -> new BreakableDeque<>(new LinkedList<>(c), DEFAULT_BREAKS, DEFAULT_METHOD_STATUSES,
                        DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS, DEFAULT_SAFETY, Object.class),
                elementProvider
        );
    }

    /// Creates a collection provider for instances of `BreakableDeque`, given an element provider and a set of
    /// breaks.
    /// @param <E> the element type.
    /// @param elementProvider the element provider to use.
    /// @param breaks the breaks to apply to each instance of `BreakableDeque`.
    /// @param methodStatuses the method statuses to apply to each instance of `BreakableDeque`.
    /// @return a collection provider for breakable deques.
    public static <E> @NonNull CollectionProvider<E, BreakableDeque<E>> dequeProvider(
            final @NonNull ObjectProvider<E> elementProvider,
            final @NonNull Set<Break> breaks,
            final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses) {
        return CollectionProviders.from(
                () -> new BreakableDeque<>(new LinkedList<>(), breaks, methodStatuses,
                        DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS, DEFAULT_SAFETY, Object.class),
                (o) -> new BreakableDeque<>(new LinkedList<>(o), breaks,
                        new HashMap<>(o.methodStatuses()), o.characteristics(), o.permits(), o.isSafe(),
                        o.compatibleType()),
                (c) -> new BreakableDeque<>(new LinkedList<>(c), breaks, methodStatuses,
                        DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS, DEFAULT_SAFETY, Object.class),
                elementProvider
        );
    }

    /// Creates and returns a new instance of the `Builder` class.
    /// This method initializes a builder instance that can be used to configure
    /// and construct objects of the enclosing class. The builder pattern helps
    /// simplify object creation and provides flexibility in handling complex
    /// construction scenarios.
    ///
    /// @param <E> The type of the objects that the builder will construct.
    /// @return A new `Builder` instance for the enclosing class.
    public static <E> Builder<E> builder() {
        return new Builder<>();
    }

    /// Mixin interface that adds an implementation of the `provider()` method that provides instances of
    /// `BreakableDeque` that do not have any breaks applied.
    /// @param <E> element type
    public interface WithProvider<E> extends CollectionProviderSupport<E, BreakableDeque<E>> {
        @Override
        default @NonNull CollectionProvider<E, BreakableDeque<E>> provider() {
            return BreakableDeque.dequeProvider(elementProvider());
        }
    }
}
