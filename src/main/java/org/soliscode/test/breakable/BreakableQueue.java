package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.MethodStatus;
import org.soliscode.test.contract.queue.QueueMethods;
import org.soliscode.test.contract.support.CollectionProviderSupport;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.CollectionProviders;
import org.soliscode.test.provider.ObjectProvider;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

/// A `Queue` implementation that can be programmatically broken for testing purposes.
///
/// This class extends `BreakableCollection` and implements the `Queue` interface, providing
/// additional break constants for testing `Queue`-specific functionality. It wraps
/// an existing `Queue` and allows specific behaviors to be "broken" through the
/// `Break` mechanism.
///
/// ## Overview
///
/// `BreakableQueue` provides comprehensive testing capabilities for code that works with
/// `Queue` implementations. It supports all standard `Queue` operations while enabling
/// controlled behavioral modifications through break constants. This is particularly useful for:
///
/// - **Testing Queue Contract Compliance**: Verifying that code correctly handles `Queue` operations
/// - **Error Condition Simulation**: Testing how code responds to queue failures and edge cases
/// - **Boundary Testing**: Ensuring proper handling of empty queues and capacity limits
/// - **Performance Testing**: Simulating slow or failing queue operations
/// - **Robustness Testing**: Verifying code resilience against unexpected `Queue` behaviors
///
/// ## Queue-Specific Break Constants
///
/// This class provides break constants for all `Queue`-specific methods:
///
/// ### Insertion Breaks
/// - **OFFER_ALWAYS_RETURNS_FALSE**: Control `offer()` method to always return `false`
/// - **OFFER_ALWAYS_RETURNS_TRUE**: Control `offer()` method to always return `true`
/// - **OFFER_DOES_NOT_ADD_ELEMENT**: Forces `offer()` to accept but not add_singleElement_returnsTrueAndUpdatesSize elements
/// - **OFFER_THROWS_EXCEPTION**: Forces `offer()` to throw exceptions instead of normal operation
///
/// ### Retrieval Breaks
/// - **POLL_ALWAYS_RETURNS_NULL**: Control `poll()` method to always return `null`
/// - **POLL_DOES_NOT_REMOVE_ELEMENT**: Forces `poll()` to return element without removing it
/// - **POLL_THROWS_EXCEPTION**: Forces `poll()` to throw exceptions
/// - **POLL_RETURNS_RANDOM_ELEMENT**: Forces `poll()` to return arbitrary elements
///
/// ### Examination Breaks
/// - **PEEK_ALWAYS_RETURNS_NULL**: Control `peek()` method to always return `null`
/// - **PEEK_THROWS_EXCEPTION**: Forces `peek()` to throw exceptions
/// - **PEEK_RETURNS_RANDOM_ELEMENT**: Forces `peek()` to return arbitrary elements
/// - **ELEMENT_THROWS_EXCEPTION**: Forces `element()` to throw exceptions even when queue has elements
/// - **ELEMENT_RETURNS_RANDOM_ELEMENT**: Forces `element()` to return arbitrary elements
///
/// ### Removal Breaks
/// - **REMOVE_THROWS_EXCEPTION**: Forces `remove()` to throw exceptions
/// - **REMOVE_ALWAYS_RETURNS_NULL**: Forces `remove()` to return `null` (non-standard behavior)
/// - **REMOVE_DOES_NOT_REMOVE_ELEMENT**: Forces `remove()` to return element without removing it
///
/// ## Usage Examples
///
/// ### Basic Queue Testing
/// ```java
/// BreakableQueue<String> queue = new BreakableQueue<>();
/// queue.offer("first");
/// queue.offer("second");
///
/// // Normal queue operations
/// assertEquals("first", queue.peek());
/// assertEquals("first", queue.poll());
/// assertEquals("second", queue.element());
/// ```
///
/// ### Queue Failure Testing
/// ```java
/// BreakableQueue<String> brokenQueue = new BreakableQueue.Builder<String>()
///     .addBreak(OFFER_ALWAYS_RETURNS_FALSE)
///     .addBreak(POLL_ALWAYS_RETURNS_NULL)
///     .build();
///
/// assertFalse(brokenQueue.offer("item")); // Break causes false return
/// assertNull(brokenQueue.poll()); // Break causes null return
/// ```
///
/// ### Builder Pattern with Queue
/// ```java
/// BreakableQueue<Integer> queue = new BreakableQueue.Builder<Integer>()
///     .addBreak(PEEK_THROWS_EXCEPTION)
///     .addBreak(ELEMENT_RETURNS_RANDOM_ELEMENT)
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
/// ```
///
/// This class inherits all `Collection` and `Iterable` functionality from its parent classes while adding
/// `Queue`-specific capabilities. All inherited break constants and behaviors remain available.
///
/// ## Thread Safety
///
/// Like its parent classes, `BreakableQueue` is not thread-safe. External synchronization
/// is required for concurrent access, particularly when queue operations are involved.
///
/// ## Performance Considerations
///
/// `Queue` operations delegate to the underlying `Queue` implementation unless breaks
/// are applied. Break checking adds minimal overhead. The class maintains the performance
/// characteristics of the underlying `Queue` for non-broken operations.
///
/// @param <E> the type of elements held in this queue
/// @since 1.0
/// @see BreakableCollection
/// @see Queue
/// @see Break
public class BreakableQueue<E> extends BreakableCollection<E> implements Queue<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ========== Offer Method Breaks ==========

    /// Break constant that causes `offer()` to always return `false`.
    ///
    /// When this break is applied, the `offer()` method will return `false` regardless
    /// of whether the element was actually added to the queue.
    ///
    /// **Affected Methods:**
    /// - {@link #offer(Object)} - Returns `false`
    ///
    /// **Use Cases:**
    /// - Testing queue capacity failure handling
    /// - Simulating queue rejection conditions
    /// - Testing `offer()` return value handling
    ///
    /// @since 1.0
    public static final Break OFFER_ALWAYS_RETURNS_FALSE =
            new Break("OFFER_ALWAYS_RETURNS_FALSE");

    /// Break constant that causes `offer()` to always return `true`.
    ///
    /// When this break is applied, the `offer()` method will return `true` regardless
    /// of whether the element was actually added to the queue.
    ///
    /// **Affected Methods:**
    /// - {@link #offer(Object)} - Returns `true`
    ///
    /// **Use Cases:**
    /// - Testing incorrect success indication
    /// - Simulating unreliable `offer()` feedback
    /// - Testing code that assumes `offer()` success
    ///
    /// @since 1.0
    public static final Break OFFER_ALWAYS_RETURNS_TRUE =
            new Break("OFFER_ALWAYS_RETURNS_TRUE");

    /// Break constant that causes `offer()` to accept elements but not actually add_singleElement_returnsTrueAndUpdatesSize them.
    ///
    /// When this break is applied, the `offer()` method will return normally but
    /// the element will not be added to the queue.
    ///
    /// **Affected Methods:**
    /// - {@link #offer(Object)} - Returns normally but doesn't add_singleElement_returnsTrueAndUpdatesSize element
    ///
    /// **Use Cases:**
    /// - Testing silent queue operation failures
    /// - Simulating memory or storage issues
    /// - Testing queue state consistency
    ///
    /// @since 1.0
    public static final Break OFFER_DOES_NOT_ADD_ELEMENT =
            new Break("OFFER_DOES_NOT_ADD_ELEMENT");

    /// Break constant that causes `offer()` to throw an exception.
    ///
    /// When this break is applied, the `offer()` method will throw an `IllegalStateException`
    /// instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #offer(Object)} - Throws `IllegalStateException`
    ///
    /// **Use Cases:**
    /// - Testing exception handling in queue operations
    /// - Simulating queue operation failures
    /// - Testing robustness against unexpected exceptions
    ///
    /// @since 1.0
    public static final Break OFFER_THROWS_EXCEPTION =
            new Break("OFFER_THROWS_EXCEPTION");

    // ========== Poll Method Breaks ==========

    /// Break constant that causes `poll()` to always return `null`.
    ///
    /// When this break is applied, the `poll()` method will return `null` regardless
    /// of whether the queue contains elements.
    ///
    /// **Affected Methods:**
    /// - {@link #poll()} - Returns `null`
    ///
    /// **Use Cases:**
    /// - Testing empty queue handling
    /// - Simulating polling failures
    /// - Testing null return value handling
    ///
    /// @since 1.0
    public static final Break POLL_ALWAYS_RETURNS_NULL =
            new Break("POLL_ALWAYS_RETURNS_NULL");

    /// Break constant that causes `poll()` to return an element without removing it.
    ///
    /// When this break is applied, the `poll()` method will return the head element
    /// but not remove it from the queue.
    ///
    /// **Affected Methods:**
    /// - {@link #poll()} - Returns element but doesn't remove it
    ///
    /// **Use Cases:**
    /// - Testing queue state consistency
    /// - Simulating removal operation failures
    /// - Testing duplicate element handling
    ///
    /// @since 1.0
    public static final Break POLL_DOES_NOT_REMOVE_ELEMENT =
            new Break("POLL_DOES_NOT_REMOVE_ELEMENT");

    /// Break constant that causes `poll()` to throw an exception.
    ///
    /// When this break is applied, the `poll()` method will throw a `NoSuchElementException`
    /// instead of returning `null` for empty queues.
    ///
    /// **Affected Methods:**
    /// - {@link #poll()} - Throws `NoSuchElementException`
    ///
    /// **Use Cases:**
    /// - Testing exception handling in polling
    /// - Simulating polling operation failures
    /// - Testing robustness against unexpected exceptions
    ///
    /// @since 1.0
    public static final Break POLL_THROWS_EXCEPTION =
            new Break("POLL_THROWS_EXCEPTION");

    /// Break constant that causes `poll()` to return a random element from the queue.
    ///
    /// When this break is applied, the `poll()` method will return an arbitrary
    /// element from the queue instead of the head element.
    ///
    /// **Affected Methods:**
    /// - {@link #poll()} - Returns random element
    ///
    /// **Use Cases:**
    /// - Testing queue ordering assumptions
    /// - Simulating corrupted queue order
    /// - Testing robustness against incorrect ordering
    ///
    /// @since 1.0
    public static final Break POLL_RETURNS_RANDOM_ELEMENT =
            new Break("POLL_RETURNS_RANDOM_ELEMENT");

    // ========== Peek Method Breaks ==========

    /// Break constant that causes `peek()` to always return `null`.
    ///
    /// When this break is applied, the `peek()` method will return `null` regardless
    /// of whether the queue contains elements.
    ///
    /// **Affected Methods:**
    /// - {@link #peek()} - Returns `null`
    ///
    /// **Use Cases:**
    /// - Testing empty queue handling
    /// - Simulating `peek` failures
    /// - Testing null return value handling
    ///
    /// @since 1.0
    public static final Break PEEK_ALWAYS_RETURNS_NULL =
            new Break("PEEK_ALWAYS_RETURNS_NULL");

    /// Break constant that causes `peek()` to throw an exception.
    ///
    /// When this break is applied, the `peek()` method will throw a `NoSuchElementException`
    /// instead of returning the head element or `null`.
    ///
    /// **Affected Methods:**
    /// - {@link #peek()} - Throws `NoSuchElementException`
    ///
    /// **Use Cases:**
    /// - Testing exception handling in peeking
    /// - Simulating `peek` operation failures
    /// - Testing robustness against unexpected exceptions
    ///
    /// @since 1.0
    public static final Break PEEK_THROWS_EXCEPTION =
            new Break("PEEK_THROWS_EXCEPTION");

    /// Break constant that causes `peek()` to return a random element from the queue.
    ///
    /// When this break is applied, the `peek()` method will return an arbitrary
    /// element from the queue instead of the head element.
    ///
    /// **Affected Methods:**
    /// - {@link #peek()} - Returns random element
    ///
    /// **Use Cases:**
    /// - Testing queue ordering assumptions
    /// - Simulating corrupted queue order
    /// - Testing robustness against incorrect ordering
    ///
    /// @since 1.0
    public static final Break PEEK_RETURNS_RANDOM_ELEMENT =
            new Break("PEEK_RETURNS_RANDOM_ELEMENT");

    // ========== Element Method Breaks ==========

    /// Break constant that causes `element()` to throw `NoSuchElementException` even when elements exist.
    ///
    /// When this break is applied, the `element()` method will throw `NoSuchElementException`
    /// regardless of whether the queue contains elements.
    ///
    /// **Affected Methods:**
    /// - {@link #element()} - Throws `NoSuchElementException`
    ///
    /// **Use Cases:**
    /// - Testing exception handling in element access
    /// - Simulating element access failures
    /// - Testing robustness against unexpected exceptions
    ///
    /// @since 1.0
    public static final Break ELEMENT_THROWS_EXCEPTION =
            new Break("ELEMENT_THROWS_EXCEPTION");

    /// Break constant that causes `element()` to return a random element from the queue.
    ///
    /// When this break is applied, the `element()` method will return an arbitrary
    /// element from the queue instead of the head element.
    ///
    /// **Affected Methods:**
    /// - {@link #element()} - Returns random element
    ///
    /// **Use Cases:**
    /// - Testing queue ordering assumptions
    /// - Simulating corrupted queue order
    /// - Testing robustness against incorrect ordering
    ///
    /// @since 1.0
    public static final Break ELEMENT_RETURNS_RANDOM_ELEMENT =
            new Break("ELEMENT_RETURNS_RANDOM_ELEMENT");

    // ========== Remove Method Breaks ==========

    /// Break constant that causes `remove()` to throw an exception.
    ///
    /// When this break is applied, the `remove()` method will throw an `IllegalStateException`
    /// instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #remove()} - Throws `IllegalStateException`
    ///
    /// **Use Cases:**
    /// - Testing exception handling in queue removal operations
    /// - Simulating queue operation failures
    /// - Testing robustness against unexpected exceptions
    ///
    /// @since 1.0
    public static final Break REMOVE_THROWS_EXCEPTION =
            new Break("REMOVE_THROWS_EXCEPTION");

    /// Break constant that causes `remove()` to always return `null`.
    ///
    /// When this break is applied, the `remove()` method will return `null` instead
    /// of throwing `NoSuchElementException` or returning an element. This is non-standard
    /// behavior useful for testing incorrect implementations.
    ///
    /// **Affected Methods:**
    /// - {@link #remove()} - Returns `null`
    ///
    /// **Use Cases:**
    /// - Testing null return value handling (non-standard behavior)
    /// - Simulating corrupted queue behavior
    /// - Testing robustness against unexpected return values
    ///
    /// @since 1.0
    public static final Break REMOVE_ALWAYS_RETURNS_NULL =
            new Break("REMOVE_ALWAYS_RETURNS_NULL");

    /// Break constant that causes `remove()` to return an element without removing it.
    ///
    /// When this break is applied, the `remove()` method will return the head element
    /// but not remove it from the queue.
    ///
    /// **Affected Methods:**
    /// - {@link #remove()} - Returns element but doesn't remove it
    ///
    /// **Use Cases:**
    /// - Testing queue state consistency
    /// - Simulating removal operation failures
    /// - Testing duplicate element handling
    ///
    /// @since 1.0
    public static final Break REMOVE_DOES_NOT_REMOVE_ELEMENT =
            new Break("REMOVE_DOES_NOT_REMOVE_ELEMENT");

    // ========== Instance Fields ==========

    /// The underlying Queue that this BreakableQueue wraps.
    /// All operations delegate to this queue unless breaks are applied.
    private final @NonNull Queue<E> queue;

    // ========== Constructors ==========

    /// Creates an empty `BreakableQueue` backed by an `ArrayDeque`.
    ///
    /// This constructor creates a new `BreakableQueue` backed by an empty `ArrayDeque`.
    /// The queue will have default null policies and no breaks applied.
    ///
    /// **Default Configuration:**
    /// - Empty Queue (`ArrayDeque` implementation)
    /// - Permits null elements: true
    /// - No breaks applied
    ///
    /// **Usage:**
    /// ```java
    /// BreakableQueue<String> queue = new BreakableQueue<>();
    /// queue.offer("first");
    /// queue.offer("second");
    /// ```
    ///
    /// @since 1.0
    public BreakableQueue() {
        this(new LinkedList<>(), DEFAULT_BREAKS, DEFAULT_METHOD_STATUSES, DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS,
                DEFAULT_SAFETY, Object.class);
    }

    /// Creates a `BreakableQueue` by copying another `BreakableQueue`.
    ///
    /// This constructor creates a new instance that shares the underlying queue data
    /// and inherits all configuration from the source queue.
    ///
    /// **Inherited Configuration:**
    /// - All break settings from source
    /// - Null element policies
    /// - Method support configuration
    /// - Underlying `Queue` reference (shared, not copied)
    ///
    /// **Usage:**
    /// ```java
    /// BreakableQueue<String> original = new BreakableQueue<>();
    /// BreakableQueue<String> copy = new BreakableQueue<>(original);
    /// ```
    ///
    /// @param other the `BreakableQueue` to copy configuration from; must not be null
    /// @throws NullPointerException if `other` is null
    /// @since 1.0
    public BreakableQueue(final @NonNull BreakableQueue<E> other) {
        this(new LinkedList<>(other.queue), new HashSet<>(other.breaks()), new HashMap<>(other.methodStatuses()),
                other.characteristics(), other.permits(), other.isSafe(), other.compatibleType());
    }

    /// Creates a `BreakableQueue` with the specified configuration.
    ///
    /// This constructor allows full control over the `BreakableQueue` configuration,
    /// including the underlying `Queue`, breaks, and spliterator characteristics.
    ///
    /// **Usage:**
    /// ```java
    /// ArrayDeque<String> arrayDeque = new ArrayDeque<>();
    /// Set<Break> breaks = Set.of(OFFER_ALWAYS_RETURNS_FALSE);
    /// BreakableQueue<String> queue = new BreakableQueue<>(
    ///     arrayDeque, breaks, new HashMap<>(), 0, DEFAULT_PERMITS, true);
    /// ```
    ///
    /// @param queue the `Queue` to wrap; must not be null
    /// @param breaks the breaks to apply; must not be null
    /// @param methodStatuses the method status configuration; must not be null
    /// @param characteristics the spliterator characteristics
    /// @param permits the flags that indicate what types of values are supported by the collection
    /// @param isSafe  whether the collection is safe for concurrent access.
    /// @throws NullPointerException if `queue`, `breaks`, or `methodStatuses` is null
    /// @since 1.0
    protected BreakableQueue(
            final @NonNull Queue<E> queue,
            final @NonNull Set<Break> breaks,
            final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses,
            final int characteristics,
            final int permits,
            final boolean isSafe,
            final Class<?> compatibleType) {
        super(this.queue = queue, breaks, methodStatuses, characteristics, permits, isSafe, compatibleType);
    }

    /// Compares the specified `BreakableObject` with this queue for value equality.
    ///
    /// @param other the object to compare with
    /// @return true if the specified object is a `BreakableQueue` with an equal underlying queue
    /// @since 1.0
    @Override
    protected boolean valueEquals(final BreakableObject<?> other) {
        if (other instanceof BreakableQueue<?> that) {
            return queue.equals(that.queue);
        }
        return false;
    }

    // ========== Queue Interface Implementation ==========

    /// Inserts the specified element into this queue if it is possible to do so
    /// immediately without violating capacity restrictions.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #OFFER_ALWAYS_RETURNS_FALSE} - Returns `false` regardless of actual result
    /// - {@link #OFFER_ALWAYS_RETURNS_TRUE} - Returns `true` regardless of actual result
    /// - {@link #OFFER_DOES_NOT_ADD_ELEMENT} - Returns normally but doesn't add_singleElement_returnsTrueAndUpdatesSize element
    /// - {@link #OFFER_THROWS_EXCEPTION} - Throws `IllegalStateException`
    ///
    /// **Performance Characteristics:**
    /// Delegates to the underlying `Queue`, typically O(1) for most implementations.
    ///
    /// **Thread Safety:**
    /// Not thread-safe.
    ///
    /// @param e the element to add_singleElement_returnsTrueAndUpdatesSize
    /// @return `true` if the element was added to this queue, else `false`
    /// @throws UnsupportedOperationException if the `offer()` method is not supported
    /// @throws IllegalStateException if `OFFER_THROWS_EXCEPTION` break is applied
    /// @throws ClassCastException if the class of the specified element prevents it from being added to this queue
    /// @throws NullPointerException if the specified element is `null` and this queue does not permit `null` elements
    /// @throws IllegalArgumentException if some property of the specified element prevents it from being added to this queue
    /// @since 1.0
    @Override
    public boolean offer(final E e) {
        checkMethodSupport(QueueMethods.OFFER);

        if (hasBreak(OFFER_THROWS_EXCEPTION)) {
            throw new IllegalStateException("Offer operation failed due to break");
        }

        if (hasBreak(OFFER_DOES_NOT_ADD_ELEMENT)) {
            return true; // Return true but don't actually add_singleElement_returnsTrueAndUpdatesSize the element
        }

        // Add the element first
        boolean result = queue.offer(e);

        if (hasBreak(OFFER_ALWAYS_RETURNS_FALSE)) {
            return false;
        }

        if (hasBreak(OFFER_ALWAYS_RETURNS_TRUE)) {
            return true;
        }

        return result;
    }

    /// Retrieves and removes the head of this queue, or returns `null` if this queue is empty.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #POLL_ALWAYS_RETURNS_NULL} - Returns `null` regardless of queue content
    /// - {@link #POLL_DOES_NOT_REMOVE_ELEMENT} - Returns element but doesn't remove it
    /// - {@link #POLL_THROWS_EXCEPTION} - Throws `NoSuchElementException`
    /// - {@link #POLL_RETURNS_RANDOM_ELEMENT} - Returns arbitrary element from queue
    ///
    /// **Performance Characteristics:**
    /// Delegates to the underlying `Queue`, typically O(1) for most implementations.
    ///
    /// **Thread Safety:**
    /// Not thread-safe.
    ///
    /// @return the head of this queue, or `null` if this queue is empty
    /// @throws UnsupportedOperationException if the `poll()` method is not supported
    /// @throws NoSuchElementException if `POLL_THROWS_EXCEPTION` break is applied
    /// @since 1.0
    @Override
    public @Nullable E poll() {
        checkMethodSupport(QueueMethods.POLL);

        if (hasBreak(POLL_ALWAYS_RETURNS_NULL)) {
            return null;
        }

        if (hasBreak(POLL_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("Poll operation failed due to break");
        }

        if (hasBreak(POLL_RETURNS_RANDOM_ELEMENT) && queue.size() > 1) {
            return randomElementExcludingIndex(0);
        }

        if (hasBreak(POLL_DOES_NOT_REMOVE_ELEMENT)) {
            return queue.peek(); // Return head but don't remove
        }

        return queue.poll();
    }

    /// Retrieves, but does not remove, the head of this queue, or returns `null` if this queue is empty.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #PEEK_ALWAYS_RETURNS_NULL} - Returns `null` regardless of queue content
    /// - {@link #PEEK_THROWS_EXCEPTION} - Throws `NoSuchElementException`
    /// - {@link #PEEK_RETURNS_RANDOM_ELEMENT} - Returns arbitrary element from queue
    ///
    /// **Performance Characteristics:**
    /// Delegates to the underlying `Queue`, typically O(1) for most implementations.
    ///
    /// **Thread Safety:**
    /// Not thread-safe.
    ///
    /// @return the head of this queue, or `null` if this queue is empty
    /// @throws UnsupportedOperationException if the `peek()` method is not supported
    /// @throws NoSuchElementException if `PEEK_THROWS_EXCEPTION` break is applied
    /// @since 1.0
    @Override
    public @Nullable E peek() {
        checkMethodSupport(QueueMethods.PEEK);

        if (hasBreak(PEEK_ALWAYS_RETURNS_NULL)) {
            return null;
        }

        if (hasBreak(PEEK_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("Peek operation failed due to break");
        }

        if (hasBreak(PEEK_RETURNS_RANDOM_ELEMENT) && !queue.isEmpty()) {
            return randomElementExcludingIndex(0);
        }

        return queue.peek();
    }

    /// Retrieves, but does not remove, the head of this queue. This method differs
    /// from `peek` only in that it throws an exception if this queue is empty.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #ELEMENT_THROWS_EXCEPTION} - Throws `NoSuchElementException` even when elements exist
    /// - {@link #ELEMENT_RETURNS_RANDOM_ELEMENT} - Returns arbitrary element from queue
    ///
    /// **Performance Characteristics:**
    /// Delegates to the underlying `Queue`, typically O(1) for most implementations.
    ///
    /// **Thread Safety:**
    /// Not thread-safe.
    ///
    /// @return the head of this queue
    /// @throws UnsupportedOperationException if the `element()` method is not supported
    /// @throws NoSuchElementException if this queue is empty or due to `ELEMENT_THROWS_EXCEPTION` break
    /// @since 1.0
    @SuppressWarnings("unchecked")
    @Override
    public @NonNull E element() {
        checkMethodSupport(QueueMethods.ELEMENT);

        if (hasBreak(ELEMENT_THROWS_EXCEPTION)) {
            throw new NoSuchElementException("Element access failed due to break");
        }

        if (hasBreak(ELEMENT_RETURNS_RANDOM_ELEMENT) && !queue.isEmpty()) {
            Object[] elements = queue.toArray();
            //noinspection ConstantValue
            if (elements.length > 0) {
                // unchecked cast
                return (E) elements[Math.abs(randomInt()) % elements.length];
            }
        }

        return queue.element();
    }

    /// Retrieves and removes the head of this queue. Unlike `poll()`, this method
    /// throws an exception if this queue is empty.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #REMOVE_THROWS_EXCEPTION} - Throws `IllegalStateException`
    /// - {@link #REMOVE_ALWAYS_RETURNS_NULL} - Returns `null` instead of element (non-standard)
    /// - {@link #REMOVE_DOES_NOT_REMOVE_ELEMENT} - Returns element but doesn't remove it
    ///
    /// **Performance Characteristics:**
    /// Delegates to the underlying `Queue`, typically O(1) for most implementations.
    ///
    /// **Thread Safety:**
    /// Not thread-safe.
    ///
    /// @return the head of this queue
    /// @throws UnsupportedOperationException if the `remove()` method is not supported
    /// @throws NoSuchElementException if this queue is empty (unless breaks modify behavior)
    /// @throws IllegalStateException if `REMOVE_THROWS_EXCEPTION` break is applied
    /// @since 1.0
    @SuppressWarnings("DataFlowIssue") // enable NonNull method to return null when broken
    @Override
    public @NonNull E remove() {
        checkMethodSupport(QueueMethods.REMOVE);

        if (hasBreak(REMOVE_THROWS_EXCEPTION)) {
            throw new IllegalStateException("Remove operation failed due to break");
        }

        if (hasBreak(REMOVE_ALWAYS_RETURNS_NULL)) {
            return null; // Non-standard behavior for testing
        }

        if (hasBreak(REMOVE_DOES_NOT_REMOVE_ELEMENT)) {
            E element = queue.peek();
            if (element == null) {
                throw new NoSuchElementException("Queue is empty");
            }
            return element; // Return element but don't remove it
        }

        E element = queue.poll();
        if (element == null) {
            throw new NoSuchElementException("Queue is empty");
        }
        return element;
    }

    // ========== Builder Class ==========

    /// Builder class for creating `BreakableQueue` instances with fluent configuration.
    ///
    /// This builder extends `BreakableCollection.AbstractBuilder` and provides additional configuration
    /// options specific to `Queue` functionality.
    ///
    /// **Example Usage:**
    /// ```java
    /// BreakableQueue<String> queue = new BreakableQueue.Builder<String>()
    ///     .addBreak(OFFER_ALWAYS_RETURNS_FALSE)
    ///     .addElement("item1")
    ///     .build();
    /// ```
    ///
    /// @param <E> the type of elements held in the queue
    /// @since 1.0
    public static class Builder<E> extends BreakableCollection.AbstractBuilder<Builder<E>, BreakableQueue<E>, E> {

        /// Creates a new `Builder` with default configuration.
        ///
        /// This constructor creates a new `Builder` instance that will create a `BreakableQueue`
        /// backed by an `ArrayDeque`. The queue will have default configuration with no breaks
        /// applied and default null policies.
        ///
        /// **Default Configuration:**
        /// - Empty `ArrayDeque`
        /// - Permits null elements: true
        /// - No breaks applied
        /// - Default spliterator characteristics
        ///
        /// **Usage:**
        /// ```java
        /// BreakableQueue<String> queue = new BreakableQueue.Builder<String>()
        ///     .addBreak(OFFER_ALWAYS_RETURNS_FALSE)
        ///     .build();
        /// ```
        ///
        /// **Thread Safety:**
        /// This constructor is not thread-safe. External synchronization is required
        /// if the builder is being accessed concurrently.
        ///
        /// @see #Builder(Builder)
        /// @see #build()
        /// @since 1.0
        public Builder() {
            super(new ArrayDeque<>());
        }

        /// Creates a new `Builder` by copying configuration from another `Builder`.
        ///
        /// This copy constructor creates a new `Builder` instance with all configuration
        /// copied from the source builder. This is useful for creating variations of
        /// builder configurations without modifying the original.
        ///
        /// **Copied Configuration:**
        /// - All elements in the queue
        /// - All applied breaks
        /// - Method status configuration
        /// - Spliterator characteristics
        /// - Permit settings (null policies, etc.)
        ///
        /// **Usage:**
        /// ```java
        /// Builder<String> original = new Builder<String>()
        ///     .addBreak(OFFER_ALWAYS_RETURNS_FALSE)
        ///     .addElement("item1");
        ///
        /// Builder<String> copy = new Builder<>(original);
        /// copy.addBreak(POLL_THROWS_EXCEPTION); // Add additional break to copy
        ///
        /// BreakableQueue<String> queue1 = original.build();
        /// BreakableQueue<String> queue2 = copy.build(); // Has both breaks
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
        /// @param other the `Builder` to copy configuration from; must not be null
        /// @throws NullPointerException if `other` is null
        /// @see #Builder()
        /// @see #copy()
        /// @since 1.0
        public Builder(final @NonNull Builder<E> other) {
            super(other);
        }

        /// Creates a new `Builder` with the given initial elements.
        ///
        /// The created builder will produce a `BreakableQueue` containing the
        /// specified elements.
        ///
        /// @param elements the initial elements to add_singleElement_returnsTrueAndUpdatesSize to the queue; must not be null
        /// @throws NullPointerException if `elements` is null
        /// @since 1.0
        public Builder(final @NonNull Collection<E> elements) {
            super(elements);
        }

        /// {@inheritDoc}
        /// @return this builder instance
        /// @since 1.0
        @Override
        public Builder<E> self() {
            return this;
        }

        /// {@inheritDoc}
        /// @return a new builder instance that is a copy of this one
        /// @since 1.0
        @Override
        public @NonNull Builder<E> copy() {
            return new Builder<>(this);
        }

        /// Builds a new `BreakableQueue` instance with the configured settings.
        ///
        /// This method creates and returns a new `BreakableQueue` instance configured with
        /// all the settings specified through the builder methods. The builder can be reused
        /// to create multiple `BreakableQueue` instances with the same configuration.
        ///
        /// **Applied Configuration:**
        /// - All breaks added via `addBreak()`
        /// - Method support configuration via `doesNotSupport()`
        /// - Null element policies via `permitNulls()` or `doesNotPermitNulls()`
        /// - Spliterator characteristics
        /// - Underlying queue instance
        ///
        /// **Usage:**
        /// ```java
        /// Builder<String> builder = new Builder<String>()
        ///     .addBreak(OFFER_ALWAYS_RETURNS_FALSE)
        ///     .addBreak(POLL_THROWS_EXCEPTION)
        ///     .doesNotPermitNulls();
        ///
        /// BreakableQueue<String> queue1 = builder.build();
        /// BreakableQueue<String> queue2 = builder.build(); // Reuse builder
        /// ```
        ///
        /// **Thread Safety:**
        /// This method is not thread-safe. External synchronization is required
        /// if the builder is being accessed concurrently.
        ///
        /// @return a new `BreakableQueue` instance with the configured settings
        /// @see #copy()
        /// @since 1.0
        @Override
        public @NonNull BreakableQueue<E> build() {
            return new BreakableQueue<>(new ArrayDeque<>(elements()), breaks(), methodStatuses(), characteristics(),
                    permits(), isSafe(), compatibleType());
        }
    }

    // ========== Static Factory Methods ==========

    /// Creates a `BreakableQueue` that wraps the specified `Queue` with the given breaks.
    ///
    /// This static factory method provides a convenient way to create a `BreakableQueue`
    /// that wraps an existing `Queue` implementation with specified break behaviors.
    /// The resulting `BreakableQueue` will delegate all operations to the wrapped `Queue`
    /// unless modified by the applied breaks.
    ///
    /// **Configuration:**
    /// - Uses default spliterator characteristics
    /// - Uses default permit settings (allows nulls by default)
    /// - Applies all specified breaks immediately
    /// - All `Queue` methods are supported unless explicitly disabled
    ///
    /// **Usage Examples:**
    /// ```java
    /// // Wrap an ArrayDeque with specific breaks
    /// ArrayDeque<String> arrayDeque = new ArrayDeque<>();
    /// arrayDeque.offer("item1");
    /// arrayDeque.offer("item2");
    ///
    /// Collection<Break> breaks = List.of(
    ///     POLL_ALWAYS_RETURNS_NULL,
    ///     PEEK_THROWS_EXCEPTION
    /// );
    ///
    /// BreakableQueue<String> brokenQueue = BreakableQueue.wrap(arrayDeque, breaks);
    /// assertNull(brokenQueue.poll()); // Returns null due to break
    /// assertThrows(NoSuchElementException.class, brokenQueue::peek); // Throws due to break
    /// ```
    ///
    /// **Break Application:**
    /// The breaks are applied immediately upon creation and will affect the behavior
    /// of corresponding `Queue` methods. See individual break constants for specific effects.
    ///
    /// **Thread Safety:**
    /// The created `BreakableQueue` is not thread-safe regardless of the underlying
    /// `Queue`'s thread safety properties. External synchronization is required for
    /// concurrent access.
    ///
    /// @param <E> the type of elements held in the queue
    /// @param queue the `Queue` implementation to wrap; must not be null
    /// @param breaks the collection of breaks to apply; must not be null, may be empty
    /// @return a new `BreakableQueue` wrapping the specified queue with the given breaks
    /// @throws NullPointerException if `queue` or `breaks` is null
    /// @see #wrap(Queue, Set, Map, int)
    /// @see Break
    /// @see Builder
    /// @since 1.0
    public static <E> @NonNull BreakableQueue<E> wrap(
            final @NonNull Queue<E> queue,
            final @NonNull Set<Break> breaks) {
        return new BreakableQueue<>(queue, breaks, new HashMap<>(), DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS,
                DEFAULT_SAFETY, Object.class);
    }

    /// Creates a `BreakableQueue` that wraps the specified `Queue` with full configuration.
    ///
    /// This static factory method provides complete control over `BreakableQueue` creation,
    /// allowing specification of the underlying `Queue`, breaks to apply, and spliterator
    /// characteristics. This method is useful when precise control over spliterator
    /// behavior is required for advanced use cases.
    ///
    /// **Configuration:**
    /// - Uses specified spliterator characteristics
    /// - Uses default permit settings (allows nulls by default)
    /// - Applies all specified breaks immediately
    /// - All `Queue` methods are supported unless explicitly disabled
    ///
    /// **Usage Examples:**
    /// ```java
    /// // Create with custom spliterator characteristics
    /// PriorityQueue<Integer> priorityQueue = new PriorityQueue<>();
    /// priorityQueue.offer(3);
    /// priorityQueue.offer(1);
    /// priorityQueue.offer(2);
    ///
    /// Collection<Break> breaks = List.of(OFFER_ALWAYS_RETURNS_FALSE);
    /// int characteristics = Spliterator.ORDERED | Spliterator.SIZED;
    ///
    /// BreakableQueue<Integer> queue = BreakableQueue.wrap(
    ///     priorityQueue, breaks, characteristics);
    /// assertFalse(queue.offer(4)); // Returns false due to break
    /// ```
    ///
    /// **Spliterator Characteristics:**
    /// The characteristics parameter controls the behavior of spliterators created
    /// from this queue. Common characteristics include:
    /// - `Spliterator.ORDERED` - Elements have a defined encounter order
    /// - `Spliterator.SIZED` - Size is known and finite
    /// - `Spliterator.NONNULL` - No null elements
    /// - `Spliterator.DISTINCT` - All elements are distinct
    ///
    /// **Thread Safety:**
    /// The created `BreakableQueue` is not thread-safe regardless of the underlying
    /// `Queue`'s thread safety properties. External synchronization is required for
    /// concurrent access.
    ///
    /// @param <E> the type of elements held in the queue
    /// @param queue the `Queue` implementation to wrap; must not be null
    /// @param breaks the collection of breaks to apply; must not be null, may be empty
    /// @param methodStatuses the method status configuration; must not be null
    /// @param characteristics the spliterator characteristics for this queue
    /// @return a new `BreakableQueue` wrapping the specified queue with the given configuration
    /// @throws NullPointerException if `queue`, `breaks`, or `methodStatuses` is null
    /// @see #wrap(Queue, Set)
    /// @see Break
    /// @see Builder
    /// @see java.util.Spliterator
    /// @since 1.0
    public static <E> @NonNull BreakableQueue<E> wrap(
            final @NonNull Queue<E> queue,
            final @NonNull Set<Break> breaks,
            final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses,
            final int characteristics) {
        return new BreakableQueue<>(queue, breaks, methodStatuses, characteristics, DEFAULT_PERMITS, DEFAULT_SAFETY,
                Object.class);
    }

    /// Creates a collection provider for instances of BreakableQueue, given an element provider.
    /// @param <E> the element type.
    /// @param elementProvider the element provider to use.
    /// @return a collection provider for breakable queues.
    public static <E> @NonNull CollectionProvider<E, BreakableQueue<E>> queueProvider(
            final @NonNull ObjectProvider<E> elementProvider) {
        return CollectionProviders.from(
                BreakableQueue::new,
                BreakableQueue::new,
                (c) -> new BreakableQueue<>(new LinkedList<>(c), new HashSet<>(), new HashMap<>(),
                        DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS, DEFAULT_SAFETY, Object.class),
                elementProvider
        );
    }

    /// Creates a collection provider for instances of `BreakableQueue`, given an element provider and a set of
    /// breaks.
    /// @param <E> the element type.
    /// @param elementProvider the element provider to use.
    /// @param breaks the breaks to apply to each instance of `BreakableQueue`.
    /// @param methodStatuses the method statuses to apply to each instance of `BreakableQueue`.
    /// @return a collection provider for breakable queues.
    public static <E> @NonNull CollectionProvider<E, BreakableQueue<E>> queueProvider(
            final @NonNull ObjectProvider<E> elementProvider,
            final @NonNull Set<Break> breaks,
            final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses) {
        return CollectionProviders.from(
                () -> new BreakableQueue<>(new LinkedList<>(), breaks, methodStatuses,
                        DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS, DEFAULT_SAFETY, Object.class),
                (o) -> new BreakableQueue<>(new LinkedList<>(o.queue), breaks,
                        new HashMap<>(o.methodStatuses()), DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS, o.isSafe(),
                        o.compatibleType()),
                (c) -> new BreakableQueue<>(new LinkedList<>(c), breaks, methodStatuses,
                        DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS, DEFAULT_SAFETY, Object.class),
                elementProvider
        );
    }

    /// Mixin interface that adds an implementation of the `provider()` method that provides instances of
    /// `BreakableQueue` that do not have any breaks applied.
    /// @param <E> element type
    public interface WithProvider<E> extends CollectionProviderSupport<E, BreakableQueue<E>> {
        @Override
        default @NonNull CollectionProvider<E, BreakableQueue<E>> provider() {
            return BreakableQueue.queueProvider(elementProvider());
        }
    }
}
