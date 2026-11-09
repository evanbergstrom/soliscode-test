package com.soliscode.test.breakable;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.contract.blockingqueue.BlockingQueueMethods;
import com.soliscode.test.contract.queue.QueueMethods;

/// A `BlockingQueue` implementation that can be programmatically broken for testing purposes.
///
/// This class extends `BreakableQueue` and implements the `BlockingQueue` interface, providing
/// additional break constants for testing `BlockingQueue`-specific functionality. It wraps
/// an existing `BlockingQueue` and allows specific behaviors to be "broken" through the
/// `Break` mechanism.
///
/// ## Overview
///
/// `BreakableBlockingQueue` provides comprehensive testing capabilities for code that works with
/// `BlockingQueue` implementations. It supports all standard `BlockingQueue` operations while enabling
/// controlled behavioral modifications through break constants. This is particularly useful for:
///
/// - **Testing BlockingQueue Contract Compliance**: Verifying that code correctly handles blocking operations
/// - **Concurrency Testing**: Simulating blocking, timeout, and interruption scenarios
/// - **Error Condition Simulation**: Testing how code responds to capacity limits and thread interruption
/// - **Performance Testing**: Simulating slow or failing blocking operations
/// - **Robustness Testing**: Verifying code resilience against unexpected `BlockingQueue` behaviors
///
/// ## BlockingQueue-Specific Break Constants
///
/// This class provides break constants for all `BlockingQueue`-specific methods:
///
/// ### Blocking Put Operations
/// - **PUT_ALWAYS_BLOCKS**: Forces `put()` to block indefinitely
/// - **PUT_THROWS_INTERRUPTED_EXCEPTION**: Forces `put()` to throw `InterruptedException`
/// - **PUT_DOES_NOT_ADD_ELEMENT**: Forces `put()` to accept but not add elements
/// - **PUT_THROWS_EXCEPTION**: Forces `put()` to throw exceptions instead of normal operation
///
/// ### Blocking Take Operations
/// - **TAKE_ALWAYS_BLOCKS**: Forces `take()` to block indefinitely
/// - **TAKE_THROWS_INTERRUPTED_EXCEPTION**: Forces `take()` to throw `InterruptedException`
/// - **TAKE_ALWAYS_RETURNS_NULL**: Control `take()` method to always return `null`
/// - **TAKE_DOES_NOT_REMOVE_ELEMENT**: Forces `take()` to return element without removing it
///
/// ### Timeout Operations
/// - **OFFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE**: Control `offer(timeout)` to always return `false`
/// - **OFFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION**: Forces `offer(timeout)` to throw `InterruptedException`
/// - **POLL_WITH_TIMEOUT_ALWAYS_RETURNS_NULL**: Control `poll(timeout)` to always return `null`
/// - **POLL_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION**: Forces `poll(timeout)` to throw `InterruptedException`
/// - **OFFER_WITH_TIMEOUT_DOES_NOT_WAIT**, **POLL_WITH_TIMEOUT_DOES_NOT_WAIT**: the timed `offer` and `poll` try once,
///   without waiting
///
/// ### Capacity and Drain Operations
/// - **REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO**: Forces `remainingCapacity()` to always return 0
/// - **DRAIN_TO_ALWAYS_RETURNS_ZERO**: Forces `drainTo()` to always return 0
/// - **DRAIN_TO_THROWS_EXCEPTION**: Forces `drainTo()` to throw exceptions
/// - **DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS**: Forces `drainTo()` to return count but not remove elements
///
/// ### Thread Safety
/// - **TAKE_IS_NOT_THREAD_SAFE**, **POLL_IS_NOT_THREAD_SAFE**, **DRAIN_TO_IS_NOT_THREAD_SAFE**: `take()`, `poll()`
///   and `drainTo()` read the store and then remove from it in separate steps, so that two threads can both get one
///   element. The store is a thread-safe JDK `BlockingQueue`, so the other operations stay atomic whether or not the
///   breakable is built thread safe.
///
/// ## Usage Examples
///
/// ### Basic BlockingQueue Testing
/// ```java
/// BreakableBlockingQueue<String> queue = new BreakableBlockingQueue<>();
/// queue.put("first");
/// queue.put("second");
///
/// // Normal blocking queue operations
/// assertEquals("first", queue.take());
/// assertEquals("second", queue.poll(1, TimeUnit.SECONDS));
/// ```
///
/// ### BlockingQueue Failure Testing
/// ```java
/// BreakableBlockingQueue<String> brokenQueue = new BreakableBlockingQueue.Builder<String>()
///     .addBreak(PUT_ALWAYS_BLOCKS)
///     .addBreak(TAKE_THROWS_INTERRUPTED_EXCEPTION)
///     .build();
///
/// // Test blocking behavior
/// // put() will block indefinitely due to break
/// // take() will throw InterruptedException due to break
/// ```
///
/// ### Builder Pattern with BlockingQueue
/// ```java
/// BreakableBlockingQueue<Integer> queue = new BreakableBlockingQueue.Builder<Integer>()
///     .addBreak(OFFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE)
///     .addBreak(REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO)
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
/// BreakableBlockingQueue<E>
/// ```
///
/// This class inherits all `Collection`, `Queue`, and `Iterable` functionality from its parent classes
/// while adding `BlockingQueue`-specific capabilities. All inherited break constants and behaviors remain available.
///
/// ## Thread Safety
///
/// Like its parent classes, `BreakableBlockingQueue` is not thread-safe in terms of break configuration.
/// The underlying `BlockingQueue` implementation provides the thread safety for normal operations,
/// but break checking adds additional complexity that requires external synchronization for
/// concurrent break modifications.
///
/// ## Performance Considerations
///
/// `BlockingQueue` operations delegate to the underlying `BlockingQueue` implementation unless breaks
/// are applied. Break checking adds minimal overhead. The class maintains the performance
/// characteristics of the underlying `BlockingQueue` for non-broken operations.
///
/// @param <E> the type of elements held in this blocking queue
/// @since 1.0
/// @see BreakableQueue
/// @see java.util.concurrent.BlockingQueue
/// @see Break
public class BreakableBlockingQueue<E> extends BreakableQueue<E> implements BlockingQueue<E>, Serializable {

    // ========== Put Method Breaks ==========

    /// Break constant that causes `put()` to block indefinitely.
    ///
    /// When this break is applied, the `put()` method will block the calling thread
    /// indefinitely without adding the element to the queue.
    ///
    /// **Affected Methods:**
    /// - {@link #put(Object)} - Blocks indefinitely
    ///
    /// **Use Cases:**
    /// - Testing thread interruption handling
    /// - Simulating queue capacity issues
    /// - Testing deadlock detection
    ///
    /// @since 1.0
    public static final Break PUT_ALWAYS_BLOCKS =
            new Break("PUT_ALWAYS_BLOCKS");

    /// Break constant that causes `put()` to throw `InterruptedException`.
    ///
    /// When this break is applied, the `put()` method will immediately throw
    /// `InterruptedException` instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #put(Object)} - Throws `InterruptedException`
    ///
    /// **Use Cases:**
    /// - Testing interruption handling in blocking operations
    /// - Simulating thread interruption scenarios
    /// - Testing cleanup code in exception handlers
    ///
    /// @since 1.0
    public static final Break PUT_THROWS_INTERRUPTED_EXCEPTION =
            new Break("PUT_THROWS_INTERRUPTED_EXCEPTION");

    /// Break constant that causes `put()` to accept elements but not actually add them.
    ///
    /// When this break is applied, the `put()` method will return normally but
    /// the element will not be added to the queue.
    ///
    /// **Affected Methods:**
    /// - {@link #put(Object)} - Returns normally but doesn't add element
    ///
    /// **Use Cases:**
    /// - Testing silent blocking queue operation failures
    /// - Simulating memory or storage issues
    /// - Testing queue state consistency
    ///
    /// @since 1.0
    public static final Break PUT_DOES_NOT_ADD_ELEMENT =
            new Break("PUT_DOES_NOT_ADD_ELEMENT");

    /// Break constant that causes `put()` to throw an exception.
    ///
    /// When this break is applied, the `put()` method will throw an `IllegalStateException`
    /// instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #put(Object)} - Throws `IllegalStateException`
    ///
    /// **Use Cases:**
    /// - Testing exception handling in blocking operations
    /// - Simulating blocking queue operation failures
    /// - Testing robustness against unexpected exceptions
    ///
    /// @since 1.0
    public static final Break PUT_THROWS_EXCEPTION =
            new Break("PUT_THROWS_EXCEPTION");

    // ========== Take Method Breaks ==========

    /// Break constant that causes `take()` to block indefinitely.
    ///
    /// When this break is applied, the `take()` method will block the calling thread
    /// indefinitely without removing any element from the queue.
    ///
    /// **Affected Methods:**
    /// - {@link #take()} - Blocks indefinitely
    ///
    /// **Use Cases:**
    /// - Testing thread interruption handling
    /// - Simulating empty queue scenarios
    /// - Testing deadlock detection
    ///
    /// @since 1.0
    public static final Break TAKE_ALWAYS_BLOCKS =
            new Break("TAKE_ALWAYS_BLOCKS");

    /// Break constant that causes `take()` to throw `InterruptedException`.
    ///
    /// When this break is applied, the `take()` method will immediately throw
    /// `InterruptedException` instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #take()} - Throws `InterruptedException`
    ///
    /// **Use Cases:**
    /// - Testing interruption handling in blocking operations
    /// - Simulating thread interruption scenarios
    /// - Testing cleanup code in exception handlers
    ///
    /// @since 1.0
    public static final Break TAKE_THROWS_INTERRUPTED_EXCEPTION =
            new Break("TAKE_THROWS_INTERRUPTED_EXCEPTION");

    /// Break constant that causes `take()` to always return `null`.
    ///
    /// When this break is applied, the `take()` method will return `null` instead
    /// of blocking or returning an element.
    ///
    /// **Affected Methods:**
    /// - {@link #take()} - Returns `null`
    ///
    /// **Use Cases:**
    /// - Testing null return value handling (non-standard behavior)
    /// - Simulating corrupted blocking queue behavior
    /// - Testing robustness against unexpected return values
    ///
    /// @since 1.0
    public static final Break TAKE_ALWAYS_RETURNS_NULL =
            new Break("TAKE_ALWAYS_RETURNS_NULL");

    /// Break constant that causes `take()` to return an element without removing it.
    ///
    /// When this break is applied, the `take()` method will return the head element
    /// but not remove it from the queue.
    ///
    /// **Affected Methods:**
    /// - {@link #take()} - Returns element but doesn't remove it
    ///
    /// **Use Cases:**
    /// - Testing queue state consistency
    /// - Simulating removal operation failures
    /// - Testing duplicate element handling
    ///
    /// @since 1.0
    public static final Break TAKE_DOES_NOT_REMOVE_ELEMENT =
            new Break("TAKE_DOES_NOT_REMOVE_ELEMENT");

    // ========== Timeout Offer Method Breaks ==========

    /// Break constant that causes `offer(timeout)` to always return `false`.
    ///
    /// When this break is applied, the `offer()` method with timeout will return `false`
    /// regardless of timeout value or whether the element was actually added.
    ///
    /// **Affected Methods:**
    /// - {@link #offer(Object, long, TimeUnit)} - Returns `false`
    ///
    /// **Use Cases:**
    /// - Testing timeout failure handling
    /// - Simulating queue capacity issues
    /// - Testing `offer()` return value handling
    ///
    /// @since 1.0
    public static final Break OFFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE =
            new Break("OFFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE");

    /// Break constant that causes `offer(timeout)` to throw `InterruptedException`.
    ///
    /// When this break is applied, the `offer()` method with timeout will throw
    /// `InterruptedException` instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #offer(Object, long, TimeUnit)} - Throws `InterruptedException`
    ///
    /// **Use Cases:**
    /// - Testing interruption handling in timeout operations
    /// - Simulating thread interruption scenarios
    /// - Testing cleanup code in exception handlers
    ///
    /// @since 1.0
    public static final Break OFFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION =
            new Break("OFFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION");

    /// Break constant that causes `offer(timeout)` not to wait for space.
    ///
    /// **Purpose:** simulates a timed offer that gives up at once on a full queue, instead of "waiting up to the
    /// specified wait time if necessary for space to become available" ([BlockingQueue#offer(Object, long, TimeUnit)]).
    ///
    /// **Effect:** the element is offered once, as [#offer(Object)] does, and the timeout is ignored; on a full queue
    /// the call returns `false` at once, even with the thread's interrupt status set.
    ///
    /// **Affected Methods:**
    /// - {@link #offer(Object, long, TimeUnit)} - Does not wait
    ///
    /// @see #offer(Object, long, TimeUnit)
    /// @since 0.1.2
    public static final Break OFFER_WITH_TIMEOUT_DOES_NOT_WAIT =
            new Break("OFFER_WITH_TIMEOUT_DOES_NOT_WAIT");

    // ========== Timeout Poll Method Breaks ==========

    /// Break constant that causes `poll(timeout)` to always return `null`.
    ///
    /// When this break is applied, the `poll()` method with timeout will return `null`
    /// regardless of timeout value or whether elements are available.
    ///
    /// **Affected Methods:**
    /// - {@link #poll(long, TimeUnit)} - Returns `null`
    ///
    /// **Use Cases:**
    /// - Testing timeout null handling
    /// - Simulating empty queue scenarios
    /// - Testing null return value handling
    ///
    /// @since 1.0
    public static final Break POLL_WITH_TIMEOUT_ALWAYS_RETURNS_NULL =
            new Break("POLL_WITH_TIMEOUT_ALWAYS_RETURNS_NULL");

    /// Break constant that causes `poll(timeout)` to throw `InterruptedException`.
    ///
    /// When this break is applied, the `poll()` method with timeout will throw
    /// `InterruptedException` instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #poll(long, TimeUnit)} - Throws `InterruptedException`
    ///
    /// **Use Cases:**
    /// - Testing interruption handling in timeout operations
    /// - Simulating thread interruption scenarios
    /// - Testing cleanup code in exception handlers
    ///
    /// @since 1.0
    public static final Break POLL_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION =
            new Break("POLL_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION");

    /// Break constant that causes `poll(timeout)` not to wait for an element.
    ///
    /// **Purpose:** simulates a timed poll that gives up at once on an empty queue, instead of "waiting up to the
    /// specified wait time if necessary for an element to become available" ([BlockingQueue#poll(long, TimeUnit)]).
    ///
    /// **Effect:** the queue is polled once, as [#poll()] does, and the timeout is ignored; on an empty queue the
    /// call returns `null` at once, even with the thread's interrupt status set.
    ///
    /// **Affected Methods:**
    /// - {@link #poll(long, TimeUnit)} - Does not wait
    ///
    /// @see #poll(long, TimeUnit)
    /// @since 0.1.2
    public static final Break POLL_WITH_TIMEOUT_DOES_NOT_WAIT =
            new Break("POLL_WITH_TIMEOUT_DOES_NOT_WAIT");

    // ========== Thread-Safety Breaks ==========

    /// Break constant that makes `take()` not atomic.
    ///
    /// **Purpose:** simulates a `take` that is not thread safe, though "All queuing methods achieve their effects
    /// atomically" ([BlockingQueue]).
    ///
    /// **Effect:** on a non-empty queue, `take()` reads the head with `peek()` and then removes it with
    /// `remove(Object)`, two steps another thread can come between, so two takers can both return one element. On an
    /// empty queue it waits as normal.
    ///
    /// **Affected Methods:**
    /// - {@link #take()} - Not atomic
    ///
    /// @see #take()
    /// @since 0.1.2
    public static final Break TAKE_IS_NOT_THREAD_SAFE =
            new Break("TAKE_IS_NOT_THREAD_SAFE");

    /// Break constant that makes `poll()` not atomic.
    ///
    /// **Purpose:** simulates a `poll` that is not thread safe, though "All queuing methods achieve their effects
    /// atomically" ([BlockingQueue]).
    ///
    /// **Effect:** `poll()` reads the head with `peek()` and then removes it with `remove(Object)`, two steps another
    /// thread can come between, so two pollers can both return one element.
    ///
    /// **Affected Methods:**
    /// - {@link #poll()} - Not atomic
    ///
    /// @see #poll()
    /// @since 0.1.2
    public static final Break POLL_IS_NOT_THREAD_SAFE =
            new Break("POLL_IS_NOT_THREAD_SAFE");

    /// Break constant that makes `drainTo` not atomic.
    ///
    /// **Purpose:** simulates a `drainTo` that is not thread safe, though "All queuing methods achieve their effects
    /// atomically" ([BlockingQueue]).
    ///
    /// **Effect:** `drainTo` copies the elements it would move, adds the copy to the collection, and then removes
    /// each from the queue, steps another thread can come between, so two drainers can both move one element.
    ///
    /// **Affected Methods:**
    /// - {@link #drainTo(Collection)} - Not atomic
    /// - {@link #drainTo(Collection, int)} - Not atomic
    ///
    /// @see #drainTo(Collection)
    /// @since 0.1.2
    public static final Break DRAIN_TO_IS_NOT_THREAD_SAFE =
            new Break("DRAIN_TO_IS_NOT_THREAD_SAFE");

    // ========== Remaining Capacity Method Breaks ==========

    /// Break constant that causes `remainingCapacity()` to always return zero.
    ///
    /// When this break is applied, the `remainingCapacity()` method will return 0
    /// regardless of the actual remaining capacity.
    ///
    /// **Affected Methods:**
    /// - {@link #remainingCapacity()} - Returns 0
    ///
    /// **Use Cases:**
    /// - Testing capacity-based logic
    /// - Simulating full queue scenarios
    /// - Testing capacity checking code
    ///
    /// @since 1.0
    public static final Break REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO =
            new Break("REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO");

    // ========== DrainTo Method Breaks ==========

    /// Break constant that causes `drainTo()` to always return zero.
    ///
    /// When this break is applied, the `drainTo()` method will return 0
    /// regardless of how many elements were actually drained.
    ///
    /// **Affected Methods:**
    /// - {@link #drainTo(Collection)} - Returns 0
    /// - {@link #drainTo(Collection, int)} - Returns 0
    ///
    /// **Use Cases:**
    /// - Testing drain operation result handling
    /// - Simulating empty queue scenarios
    /// - Testing batch operation logic
    ///
    /// @since 1.0
    public static final Break DRAIN_TO_ALWAYS_RETURNS_ZERO =
            new Break("DRAIN_TO_ALWAYS_RETURNS_ZERO");

    /// Break constant that causes `drainTo()` to throw an exception.
    ///
    /// When this break is applied, the `drainTo()` method will throw an `IllegalStateException`
    /// instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #drainTo(Collection)} - Throws `IllegalStateException`
    /// - {@link #drainTo(Collection, int)} - Throws `IllegalStateException`
    ///
    /// **Use Cases:**
    /// - Testing exception handling in drain operations
    /// - Simulating drain operation failures
    /// - Testing robustness against unexpected exceptions
    ///
    /// @since 1.0
    public static final Break DRAIN_TO_THROWS_EXCEPTION =
            new Break("DRAIN_TO_THROWS_EXCEPTION");

    /// Break constant that causes `drainTo()` to return count but not remove elements.
    ///
    /// When this break is applied, the `drainTo()` method will return the number of
    /// elements that would be drained but not actually remove them from the queue.
    ///
    /// **Affected Methods:**
    /// - {@link #drainTo(Collection)} - Returns count but doesn't remove elements
    /// - {@link #drainTo(Collection, int)} - Returns count but doesn't remove elements
    ///
    /// **Use Cases:**
    /// - Testing queue state consistency
    /// - Simulating partial drain operation failures
    /// - Testing duplicate element handling
    ///
    /// @since 1.0
    public static final Break DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS =
            new Break("DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS");

    @Serial
    private static final long serialVersionUID = 1L;

    // ========== Instance Fields ==========

    /// The underlying `BlockingQueue` that this `BreakableBlockingQueue` wraps.
    /// All operations delegate to this queue unless breaks are applied.
    ///
    /// @since 1.0
    private final BlockingQueue<E> blockingQueue;

    // ========== Constructors ==========

    /// Creates an empty `BreakableBlockingQueue` backed by a `LinkedBlockingQueue`.
    ///
    /// This constructor creates a new `BreakableBlockingQueue` backed by an empty `LinkedBlockingQueue`.
    /// The queue will have default null policies and no breaks applied.
    ///
    /// **Default Configuration:**
    /// - Empty `BlockingQueue` (`LinkedBlockingQueue` implementation)
    /// - Permits null elements: true
    /// - No breaks applied
    ///
    /// **Usage:**
    /// ```java
    /// BreakableBlockingQueue<String> queue = new BreakableBlockingQueue<>();
    /// queue.put("first");
    /// queue.put("second");
    /// ```
    ///
    /// @since 1.0
    public BreakableBlockingQueue() {
        this(new LinkedBlockingQueue<>(), DEFAULT_BREAKS, DEFAULT_METHOD_STATUSES, DEFAULT_CHARACTERISTICS,
                DEFAULT_PERMITS, DEFAULT_SAFETY, Object.class);
    }

    /// Creates a `BreakableBlockingQueue` by copying another `BreakableBlockingQueue`.
    ///
    /// This constructor creates a new instance that shares the underlying blocking queue data
    /// and inherits all configuration from the source queue.
    ///
    /// **Inherited Configuration:**
    /// - All break settings from source
    /// - Null element policies
    /// - Method support configuration
    /// - Underlying `BlockingQueue` reference (shared, not copied)
    ///
    /// **Usage:**
    /// ```java
    /// BreakableBlockingQueue<String> original = new BreakableBlockingQueue<>();
    /// BreakableBlockingQueue<String> copy = new BreakableBlockingQueue<>(original);
    /// ```
    ///
    /// @param other the `BreakableBlockingQueue` to copy configuration from; must not be null
    /// @throws NullPointerException if `other` is null
    /// @since 1.0
    public BreakableBlockingQueue(final BreakableBlockingQueue<E> other) {
        this(new LinkedBlockingQueue<>(other.blockingQueue), new HashSet<>(other.breaks()),
                new HashMap<>(other.methodStatuses()), other.characteristics(), other.permits(), other.isSafe(),
                other.compatibleType());
    }

    /// Constructs a `BreakableBlockingQueue` that wraps a specified [BlockingQueue]
    /// to provide additional functionality, including the ability to introduce breaks in the
    /// queue and manage method-level states.
    ///
    /// @param blockingQueueArg   The [BlockingQueue] instance to be wrapped. Must not be `null`.
    /// @param breaks          A [Set] of `Break` configurations defining break conditions.
    ///                        Must not be `null`.
    /// @param methodStatuses  A [Map] associating [InterfaceMethod] to its [MethodStatus] instances,
    ///                        specifying the status or state of each method. Must not be `null`.
    /// @param characteristics An `int` representing the characteristics of the queue, such as thread safety
    ///                        or ordering guarantees.
    /// @param permits         The number of permits for the internal semaphore controlling access.
    ///                        Must be a non-negative integer.
    /// @param isSafe          A `boolean` indicating whether the queue operations are thread-safe.
    ///                        `true` if thread-safe; otherwise, `false`.
    /// @param compatibleType  The [Class] representing the type of objects that the queue can operate on.
    ///                        Used for type compatibility checks. Must not be `null`.
    protected BreakableBlockingQueue(
            final BlockingQueue<E> blockingQueueArg,
            final Set<Break> breaks,
            final Map<InterfaceMethod, MethodStatus> methodStatuses,
            final int characteristics,
            final int permits,
            final boolean isSafe,
            final Class<?> compatibleType) {
        super(blockingQueueArg, breaks, methodStatuses, characteristics, permits, isSafe,
                compatibleType);
        this.blockingQueue = blockingQueueArg;
    }

    /// Inserts the specified element into this queue if it is possible to do so immediately
    /// without violating capacity restrictions, returning true upon success and throwing
    /// an IllegalStateException if no space is currently available.
    ///
    /// This implementation delegates to the superclass `add` method, which in turn
    /// delegates to the underlying `blockingQueue`.
    ///
    /// **Performance Characteristics:**
    /// Delegates to the underlying `BlockingQueue`, typically O(1) for most implementations.
    ///
    /// **Thread Safety:**
    /// Depends on the underlying `BlockingQueue` implementation.
    ///
    /// @param e the element to add
    /// @return true (as specified by {@link Collection#add})
    /// @throws IllegalStateException if the element cannot be added at this time due to capacity restrictions
    /// @throws ClassCastException if the class of the specified element prevents it from being added to this queue
    /// @throws NullPointerException if the specified element is null and this queue does not permit null elements
    /// @throws IllegalArgumentException if some property of the specified element prevents it from being added to this
    ///     queue
    /// @since 1.0
    @Override
    public boolean add(final E e) {
        return super.add(e);
    }

    // ========== BlockingQueue Interface Implementation ==========

    /// Inserts the specified element into this queue, waiting if necessary for space to become available.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #PUT_ALWAYS_BLOCKS} - Blocks indefinitely without adding element
    /// - {@link #PUT_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    /// - {@link #PUT_DOES_NOT_ADD_ELEMENT} - Returns normally but doesn't add element
    /// - {@link #PUT_THROWS_EXCEPTION} - Throws IllegalStateException
    ///
    /// **Performance Characteristics:**
    /// Delegates to the underlying `BlockingQueue`. If `PUT_ALWAYS_BLOCKS` is applied, this method
    /// will block until the thread is interrupted.
    ///
    /// **Thread Safety:**
    /// The underlying `BlockingQueue` provides thread safety for the actual insertion. The break check
    /// itself is thread-safe for reading, but concurrent modification of breaks should be externally synchronized.
    ///
    /// @param e the element to add
    /// @throws InterruptedException if interrupted while waiting, or if `PUT_THROWS_INTERRUPTED_EXCEPTION` break is
    ///     applied
    /// @throws UnsupportedOperationException if the `put` method is not supported
    /// @throws IllegalStateException if `PUT_THROWS_EXCEPTION` break is applied, or if the element cannot be added due
    ///     to capacity restrictions
    /// @throws ClassCastException if the class of the specified element prevents it from being added to this queue
    /// @throws NullPointerException if the specified element is null and this queue does not permit null elements
    /// @throws IllegalArgumentException if some property of the specified element prevents it from being added to this
    ///     queue
    /// @since 1.0
    @Override
    public void put(final E e) throws InterruptedException {
        checkMethodSupport(BlockingQueueMethods.PUT);

        if (hasBreak(PUT_THROWS_EXCEPTION)) {
            throw new IllegalStateException("Put operation failed due to break");
        }

        if (hasBreak(PUT_DOES_NOT_ADD_ELEMENT)) {
            return; // Return without adding the element
        }

        if (hasBreak(PUT_ALWAYS_BLOCKS)) {
            // Block indefinitely - wait on a never-notified object, in a loop so that a
            // spurious wakeup does not end the block; only an interrupt ends it.
            synchronized (this) {
                while (true) {
                    try {
                        wait();
                    } catch (final InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw ie;
                    }
                }
            }
        }

        if (hasBreak(PUT_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("Thrown by " + PUT_THROWS_INTERRUPTED_EXCEPTION.name() +  " break ");
        }

        blockingQueue.put(e);
    }

    /// Retrieves and removes the head of this queue, waiting if necessary until an element becomes available.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #TAKE_ALWAYS_BLOCKS} - Blocks indefinitely without returning element
    /// - {@link #TAKE_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    /// - {@link #TAKE_ALWAYS_RETURNS_NULL} - Returns null instead of blocking
    /// - {@link #TAKE_DOES_NOT_REMOVE_ELEMENT} - Returns element but doesn't remove it
    /// - {@link #TAKE_IS_NOT_THREAD_SAFE} - Reads and removes the head in two steps
    ///
    /// **Performance Characteristics:**
    /// Delegates to the underlying `BlockingQueue`. If `TAKE_ALWAYS_BLOCKS` is applied, this method
    /// will block until the thread is interrupted.
    ///
    /// **Thread Safety:**
    /// The underlying `BlockingQueue` provides thread safety for the actual removal. The break check
    /// itself is thread-safe for reading, but concurrent modification of breaks should be externally synchronized.
    ///
    /// @return the head of this queue
    /// @throws InterruptedException if interrupted while waiting, or if `TAKE_THROWS_INTERRUPTED_EXCEPTION` break is
    ///     applied
    /// @throws UnsupportedOperationException if the `take` method is not supported
    /// @since 1.0
    @Override
    public @Nullable E take() throws InterruptedException {
        checkMethodSupport(BlockingQueueMethods.TAKE);

        if (hasBreak(TAKE_ALWAYS_RETURNS_NULL)) {
            return null; // Non-standard behavior for testing
        }

        if (hasBreak(TAKE_DOES_NOT_REMOVE_ELEMENT)) {
            E element = blockingQueue.peek();
            if (element != null) {
                return element; // Return element but don't remove it
            }
        }

        if (hasBreak(TAKE_ALWAYS_BLOCKS)) {
            // Block indefinitely - wait on a never-notified object, in a loop so that a
            // spurious wakeup does not end the block; only an interrupt ends it.
            synchronized (this) {
                while (true) {
                    try {
                        wait();
                    } catch (final InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw ie;
                    }
                }
            }
        }

        if (hasBreak(TAKE_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("Thrown by " + TAKE_THROWS_INTERRUPTED_EXCEPTION.name() +  " break ");
        }

        if (hasBreak(TAKE_IS_NOT_THREAD_SAFE)) {
            E head = blockingQueue.peek();
            if (head != null) {
                blockingQueue.remove(head);
                return head;
            }
        }

        return blockingQueue.take();
    }

    /// Inserts the specified element into this queue if it is possible to do so
    /// immediately without violating capacity restrictions, returning true upon
    /// success and false if no space is currently available.
    ///
    /// This implementation delegates to the superclass `offer` method, which in turn
    /// delegates to the underlying `blockingQueue`.
    ///
    /// **Performance Characteristics:**
    /// Delegates to the underlying `BlockingQueue`, typically O(1) for most implementations.
    ///
    /// **Thread Safety:**
    /// Depends on the underlying `BlockingQueue` implementation.
    ///
    /// @param e the element to add
    /// @return true if the element was added to this queue, else false
    /// @throws ClassCastException if the class of the specified element prevents it from being added to this queue
    /// @throws NullPointerException if the specified element is null and this queue does not permit null elements
    /// @throws IllegalArgumentException if some property of the specified element prevents it from being added to this
    ///     queue
    /// @since 1.0
    @Override
    public boolean offer(final E e) {
        return super.offer(e);
    }

    /// Retrieves and removes the head of this queue, or returns `null` if it is empty, as [BreakableQueue#poll()]
    /// does, unless [#POLL_IS_NOT_THREAD_SAFE] is set: then it reads the head and removes it in two steps.
    ///
    /// @return the head of this queue, or `null` if it is empty
    /// @throws UnsupportedOperationException if `poll` is not supported
    /// @since 0.1.2
    @Override
    public @Nullable E poll() {
        if (hasBreak(POLL_IS_NOT_THREAD_SAFE)) {
            checkMethodSupport(QueueMethods.POLL);
            E head = blockingQueue.peek();
            if (head != null) {
                blockingQueue.remove(head);
            }
            return head;
        }
        return super.poll();
    }

    /// Inserts the specified element into this queue, waiting up to the specified wait time
    /// if necessary for space to become available.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #OFFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE} - Returns false regardless of timeout
    /// - {@link #OFFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    /// - {@link #OFFER_WITH_TIMEOUT_DOES_NOT_WAIT} - Offers once, without waiting
    ///
    /// **Performance Characteristics:**
    /// Delegates to the underlying `BlockingQueue`.
    ///
    /// **Thread Safety:**
    /// The underlying `BlockingQueue` provides thread safety for the actual insertion. The break check
    /// itself is thread-safe for reading, but concurrent modification of breaks should be externally synchronized.
    ///
    /// @param e the element to add
    /// @param timeout how long to wait before giving up, in units of unit
    /// @param unit a TimeUnit determining how to interpret the timeout parameter
    /// @return true if successful, or false if the specified waiting time elapses before space is available
    /// @throws InterruptedException if interrupted while waiting, or if
    ///     `OFFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION` break is applied
    /// @throws UnsupportedOperationException if the `offer(timeout)` method is not supported
    /// @throws ClassCastException if the class of the specified element prevents it from being added to this queue
    /// @throws NullPointerException if the specified element is null and this queue does not permit null elements
    /// @throws IllegalArgumentException if some property of the specified element prevents it from being added to this
    ///     queue
    /// @since 1.0
    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit)
            throws InterruptedException {
        checkMethodSupport(BlockingQueueMethods.OFFER_TIMEOUT);

        if (hasBreak(OFFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("Offer with timeout operation interrupted due to break");
        }

        // Add the element first
        boolean result = hasBreak(OFFER_WITH_TIMEOUT_DOES_NOT_WAIT) ? blockingQueue.offer(e)
                : blockingQueue.offer(e, timeout, unit);

        if (hasBreak(OFFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE)) {
            return false;
        }

        return result;
    }

    /// Retrieves and removes the head of this queue, waiting up to the specified wait time
    /// if necessary for an element to become available.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #POLL_WITH_TIMEOUT_ALWAYS_RETURNS_NULL} - Returns null regardless of timeout
    /// - {@link #POLL_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    /// - {@link #POLL_WITH_TIMEOUT_DOES_NOT_WAIT} - Polls once, without waiting
    ///
    /// **Performance Characteristics:**
    /// Delegates to the underlying `BlockingQueue`.
    ///
    /// **Thread Safety:**
    /// The underlying `BlockingQueue` provides thread safety for the actual removal. The break check
    /// itself is thread-safe for reading, but concurrent modification of breaks should be externally synchronized.
    ///
    /// @param timeout how long to wait before giving up, in units of unit
    /// @param unit a TimeUnit determining how to interpret the timeout parameter
    /// @return the head of this queue, or null if the specified waiting time elapses before an element is available
    /// @throws InterruptedException if interrupted while waiting, or if
    ///     `POLL_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION` break is applied
    /// @throws UnsupportedOperationException if the `poll(timeout)` method is not supported
    /// @since 1.0
    @Override
    public @Nullable E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        checkMethodSupport(BlockingQueueMethods.POLL_TIMEOUT);

        if (hasBreak(POLL_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("Poll with timeout operation interrupted due to break");
        }

        if (hasBreak(POLL_WITH_TIMEOUT_ALWAYS_RETURNS_NULL)) {
            return null;
        }

        if (hasBreak(POLL_WITH_TIMEOUT_DOES_NOT_WAIT)) {
            return blockingQueue.poll();
        }

        return blockingQueue.poll(timeout, unit);
    }

    /// Returns the number of additional elements that this queue can ideally accept without blocking.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO} - Returns 0 regardless of actual capacity
    ///
    /// **Performance Characteristics:**
    /// Delegates to the underlying `BlockingQueue`.
    ///
    /// **Thread Safety:**
    /// The underlying `BlockingQueue` provides thread safety for the actual operation. The break check
    /// itself is thread-safe for reading, but concurrent modification of breaks should be externally synchronized.
    ///
    /// @return the remaining capacity
    /// @throws UnsupportedOperationException if the `remainingCapacity` method is not supported
    /// @since 1.0
    @Override
    public int remainingCapacity() {
        checkMethodSupport(BlockingQueueMethods.REMAINING_CAPACITY);

        if (hasBreak(REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO)) {
            return 0;
        }

        return blockingQueue.remainingCapacity();
    }

    /// Removes all available elements from this queue and adds them to the given collection.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #DRAIN_TO_ALWAYS_RETURNS_ZERO} - Returns 0 regardless of elements drained
    /// - {@link #DRAIN_TO_THROWS_EXCEPTION} - Throws IllegalStateException
    /// - {@link #DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS} - Returns count but doesn't remove elements
    /// - {@link #DRAIN_TO_IS_NOT_THREAD_SAFE} - Copies, adds and removes in separate steps
    ///
    /// Without a break, a drain into this queue throws [IllegalArgumentException].
    ///
    /// **Performance Characteristics:**
    /// Delegates to the underlying `BlockingQueue`. Typically O(N) where N is the number of elements in the queue.
    ///
    /// **Thread Safety:**
    /// The underlying `BlockingQueue` provides thread safety for the actual operation. The break check
    /// itself is thread-safe for reading, but concurrent modification of breaks should be externally synchronized.
    ///
    /// @param c the collection to transfer elements into
    /// @return the number of elements transferred
    /// @throws UnsupportedOperationException if the `drainTo` method is not supported
    /// @throws ClassCastException if the class of an element in this queue prevents it from being added to the
    ///     specified collection
    /// @throws NullPointerException if the specified collection is null
    /// @throws IllegalArgumentException if the specified collection is this queue, or some property of an element in
    ///     this queue prevents it from being added to the specified collection
    /// @throws IllegalStateException if `DRAIN_TO_THROWS_EXCEPTION` break is applied
    /// @since 1.0
    @Override
    public int drainTo(final Collection<? super E> c) {
        checkMethodSupport(BlockingQueueMethods.DRAIN_TO);

        if (hasBreak(DRAIN_TO_THROWS_EXCEPTION)) {
            throw new IllegalStateException("DrainTo operation failed due to break");
        }

        if (hasBreak(DRAIN_TO_ALWAYS_RETURNS_ZERO)) {
            return 0;
        }

        checkNotThis(c);

        if (hasBreak(DRAIN_TO_IS_NOT_THREAD_SAFE)) {
            return drainInSteps(c, Integer.MAX_VALUE);
        }

        if (hasBreak(DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS)) {
            int size = blockingQueue.size();
            // Add elements to collection but don't remove them from queue
            c.addAll(blockingQueue);
            return size;
        }

        return blockingQueue.drainTo(c);
    }

    /// Removes at most the given number of available elements from this queue and adds them to the given collection.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #DRAIN_TO_ALWAYS_RETURNS_ZERO} - Returns 0 regardless of elements drained
    /// - {@link #DRAIN_TO_THROWS_EXCEPTION} - Throws IllegalStateException
    /// - {@link #DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS} - Returns count but doesn't remove elements
    /// - {@link #DRAIN_TO_IS_NOT_THREAD_SAFE} - Copies, adds and removes in separate steps
    ///
    /// Without a break, a drain into this queue throws [IllegalArgumentException].
    ///
    /// **Performance Characteristics:**
    /// Delegates to the underlying `BlockingQueue`. Typically O(N) where N is the number of elements drained.
    ///
    /// **Thread Safety:**
    /// The underlying `BlockingQueue` provides thread safety for the actual operation. The break check
    /// itself is thread-safe for reading, but concurrent modification of breaks should be externally synchronized.
    ///
    /// @param c the collection to transfer elements into
    /// @param maxElements the maximum number of elements to transfer
    /// @return the number of elements transferred
    /// @throws UnsupportedOperationException if the `drainTo(maxElements)` method is not supported
    /// @throws ClassCastException if the class of an element in this queue prevents it from being added to the
    ///     specified collection
    /// @throws NullPointerException if the specified collection is null
    /// @throws IllegalArgumentException if the specified collection is this queue, or some property of an element in
    ///     this queue prevents it from being added to the specified collection
    /// @throws IllegalStateException if `DRAIN_TO_THROWS_EXCEPTION` break is applied
    /// @since 1.0
    @Override
    public int drainTo(final Collection<? super E> c, final int maxElements) {
        checkMethodSupport(BlockingQueueMethods.DRAIN_TO_MAX_ELEMENTS);

        if (hasBreak(DRAIN_TO_THROWS_EXCEPTION)) {
            throw new IllegalStateException("DrainTo operation failed due to break");
        }

        if (hasBreak(DRAIN_TO_ALWAYS_RETURNS_ZERO)) {
            return 0;
        }

        checkNotThis(c);

        if (hasBreak(DRAIN_TO_IS_NOT_THREAD_SAFE)) {
            return drainInSteps(c, maxElements);
        }

        if (hasBreak(DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS)) {
            int count = Math.min(blockingQueue.size(), maxElements);
            int added = 0;
            // Add elements to collection but don't remove them from queue
            for (final E element : blockingQueue) {
                if (added >= maxElements) {
                    break;
                }
                c.add(element);
                added++;
            }
            return count;
        }

        return blockingQueue.drainTo(c, maxElements);
    }

    /// Refuses a drain into this queue, as [BlockingQueue#drainTo(Collection)] says: the store would compare the
    /// collection with itself, not with this queue, and drain into it.
    // ReferenceEquality: the check is for this very queue, by identity, as the JDK's queues make it.
    @SuppressWarnings("ReferenceEquality")
    private void checkNotThis(final Collection<? super E> c) {
        if (c == this) {
            throw new IllegalArgumentException("drainTo: the collection is this queue");
        }
    }

    /// Moves at most `maxElements` elements in steps that are not atomic, for [#DRAIN_TO_IS_NOT_THREAD_SAFE]: copies
    /// them, adds the copy to `c`, then removes each from the store.
    private int drainInSteps(final Collection<? super E> c, final int maxElements) {
        List<E> copy = new ArrayList<>();
        for (final E element : blockingQueue) {
            if (copy.size() >= maxElements) {
                break;
            }
            copy.add(element);
        }
        c.addAll(copy);
        for (final E element : copy) {
            blockingQueue.remove(element);
        }
        return copy.size();
    }

    // ========== Builder Class ==========

    /// Base builder class for creating `BreakableBlockingQueue` instances.
    ///
    /// This abstract class provides common configuration options for all `BreakableBlockingQueue` builders.
    /// It handles capacity and fairness settings for the underlying `BlockingQueue`.
    ///
    /// @param <B> the type of the builder itself (for fluent API)
    /// @param <C> the type of the `BreakableBlockingQueue` being built
    /// @param <E> the type of elements in the queue
    /// @since 1.0
    public abstract static class AbstractBuilder<B extends BreakableCollection.AbstractBuilder<B, C, E>,
            C extends BreakableBlockingQueue<E>, E>
            extends BreakableCollection.AbstractBuilder<B, C, E> {

        private static final boolean DEFAULT_FAIRNESS = false;

        private int capacity;
        private boolean fair;

        /// Creates a new `AbstractBuilder` with default configuration.
        ///
        /// Default configuration has no capacity limit (-1) and fairness disabled.
        ///
        /// @since 1.0
        public AbstractBuilder() {
            this.capacity = -1;
            this.fair = DEFAULT_FAIRNESS;
        }

        /// Creates a new `AbstractBuilder` with the given initial elements.
        ///
        /// @param elements the initial elements to add to the queue
        /// @since 1.0
        public AbstractBuilder(final Collection<E> elements) {
            super(elements);
            this.capacity = -1;
            this.fair = DEFAULT_FAIRNESS;
        }

        /// Creates a new `AbstractBuilder` by copying configuration from another builder.
        ///
        /// @param other the builder to copy from
        /// @since 1.0
        public AbstractBuilder(final AbstractBuilder<B, C, E> other) {
            super(other);
            this.capacity = other.capacity;
            this.fair = other.fair;
        }

        /// Sets the capacity for the underlying `BlockingQueue`.
        ///
        /// If capacity is set to a non-negative value, an `ArrayBlockingQueue` will be used.
        /// If capacity is negative (the default), a `LinkedBlockingQueue` will be used.
        ///
        /// @param newCapacity the capacity of the queue
        /// @return this builder for method chaining
        /// @since 1.0
        public B setCapacity(final int newCapacity) {
            this.capacity = newCapacity;
            return self();
        }

        /// Returns the configured capacity.
        ///
        /// @return the capacity, or -1 if no capacity limit is set
        /// @since 1.0
        public int capacity() {
            return capacity;
        }

        /// Sets the fairness policy for the underlying `BlockingQueue`.
        ///
        /// This is only applicable when a capacity is set (using `ArrayBlockingQueue`).
        ///
        /// @param newFair true if this queue should use a fair ordering policy
        /// @return this builder for method chaining
        /// @since 1.0
        public B setFair(final boolean newFair) {
            this.fair = newFair;
            return self();
        }

        /// Returns the configured fairness policy.
        ///
        /// @return true if fairness is enabled
        /// @since 1.0
        public boolean fair() {
            return fair;
        }
    }



    /// Builder class for creating `BreakableBlockingQueue` instances with fluent configuration.
    ///
    /// This builder extends `BreakableQueue.Builder` and provides additional configuration
    /// options specific to `BlockingQueue` functionality, such as capacity and fairness.
    ///
    /// **Example Usage:**
    /// ```java
    /// BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
    ///     .setCapacity(10)
    ///     .setFair(true)
    ///     .addBreak(PUT_ALWAYS_BLOCKS)
    ///     .build();
    /// ```
    ///
    /// @param <E> the type of elements held in the blocking queue
    /// @since 1.0
    public static class Builder<E> extends AbstractBuilder<Builder<E>, BreakableBlockingQueue<E>, E> {

        /// Creates a new `Builder` with default configuration.
        ///
        /// This constructor creates a new `Builder` instance that will create a `BreakableBlockingQueue`
        /// backed by a `LinkedBlockingQueue` (since capacity defaults to -1).
        /// The queue will have default configuration with no breaks applied and default null policies.
        ///
        /// **Default Configuration:**
        /// - Empty `LinkedBlockingQueue`
        /// - Permits null elements: true
        /// - No breaks applied
        /// - Default spliterator characteristics
        ///
        /// **Usage:**
        /// ```java
        /// BreakableBlockingQueue<String> queue = new BreakableBlockingQueue.Builder<String>()
        ///     .addBreak(PUT_ALWAYS_BLOCKS)
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
            super();
        }

        /// Creates a new `Builder` by copying configuration from another `Builder`.
        ///
        /// This copy constructor creates a new `Builder` instance with all configuration
        /// copied from the source builder. This is useful for creating variations of
        /// builder configurations without modifying the original.
        ///
        /// **Copied Configuration:**
        /// - All elements in the blocking queue
        /// - All applied breaks
        /// - Method status configuration
        /// - Spliterator characteristics
        /// - Permit settings (null policies, etc.)
        /// - Capacity and fairness settings
        ///
        /// **Usage:**
        /// ```java
        /// Builder<String> original = new Builder<String>()
        ///     .addBreak(PUT_ALWAYS_BLOCKS)
        ///     .addElements("item1");
        ///
        /// Builder<String> copy = new Builder<>(original);
        /// copy.addBreak(TAKE_THROWS_INTERRUPTED_EXCEPTION); // Add additional break to copy
        ///
        /// BreakableBlockingQueue<String> queue1 = original.build();
        /// BreakableBlockingQueue<String> queue2 = copy.build(); // Has both breaks
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
        /// @throws NullPointerException if other is null
        /// @see #Builder()
        /// @see #copy()
        /// @since 1.0
        public Builder(final Builder<E> other) {
            super(other);
        }

        /// Creates a new `Builder` with the given initial elements.
        ///
        /// The created builder will produce a `BreakableBlockingQueue` containing the
        /// specified elements.
        ///
        /// @param elements the initial elements to add to the queue; must not be null
        /// @throws NullPointerException if elements is null
        /// @since 1.0
        public Builder(final Collection<E> elements) {
            super(elements);
        }

        /// {@inheritDoc}
        @Override
        public Builder<E> copy() {
            return new Builder<>(this);
        }

        @Override
        public BreakableBlockingQueue<E> copy(final BreakableBlockingQueue<E> other) {
            return new BreakableBlockingQueue<>(other);
        }

        /// {@inheritDoc}
        @Override
        public Builder<E> self() {
            return this;
        }

        /// Builds a new BreakableBlockingQueue instance with the configured settings.
        ///
        /// This method creates and returns a new BreakableBlockingQueue instance configured with
        /// all the settings specified through the builder methods. The builder can be reused
        /// to create multiple BreakableBlockingQueue instances with the same configuration.
        ///
        /// **Applied Configuration:**
        /// - All breaks added via addBreak()
        /// - Method support configuration via doesNotSupport()
        /// - Null element policies via permitNulls() or doesNotPermitNulls()
        /// - Spliterator characteristics
        /// - Underlying blocking queue instance
        ///
        /// **Usage:**
        /// ```java
        /// Builder<String> builder = new Builder<String>()
        ///     .addBreak(PUT_ALWAYS_BLOCKS)
        ///     .addBreak(TAKE_THROWS_INTERRUPTED_EXCEPTION)
        ///     .doesNotPermitNulls();
        ///
        /// BreakableBlockingQueue<String> queue1 = builder.build();
        /// BreakableBlockingQueue<String> queue2 = builder.build(); // Reuse builder
        /// ```
        ///
        /// **Thread Safety:**
        /// This method is not thread-safe. External synchronization is required
        /// if the builder is being accessed concurrently.
        ///
        /// @return a new BreakableBlockingQueue instance with the configured settings
        /// @see #copy()
        @Override
        public BreakableBlockingQueue<E> build(final Iterable<E> moreElements) {
            List<E> list = new ArrayList<>(elements());
            moreElements.forEach(list::add);
            BlockingQueue<E> queue;
            if (capacity() < 0) {
                queue = new LinkedBlockingQueue<>(list);
            } else {
                queue = new ArrayBlockingQueue<>(capacity(), fair(), list);
            }
            return new BreakableBlockingQueue<>(queue, new HashSet<>(breaks()), new HashMap<>(methodStatuses()),
                    characteristics(), permits(), isSafe(), compatibleType());
        }
    }

    // ========== Static Factory Methods ==========

    /// Creates a `BreakableBlockingQueue` that wraps the specified `BlockingQueue` with the given breaks.
    ///
    /// This static factory method provides a convenient way to create a `BreakableBlockingQueue`
    /// that wraps an existing `BlockingQueue` implementation with specified break behaviors.
    /// The resulting `BreakableBlockingQueue` will delegate all operations to the wrapped queue
    /// unless modified by the applied breaks.
    ///
    /// **Configuration:**
    /// - Uses default spliterator characteristics
    /// - Uses default permit settings (allows nulls by default)
    /// - Applies all specified breaks immediately
    /// - All `BlockingQueue` methods are supported unless explicitly disabled
    ///
    /// **Usage Examples:**
    /// ```java
    /// // Wrap a LinkedBlockingQueue with specific breaks
    /// LinkedBlockingQueue<String> linkedQueue = new LinkedBlockingQueue<>();
    /// linkedQueue.put("first");
    /// linkedQueue.put("second");
    ///
    /// Set<Break> breaks = Set.of(
    ///     PUT_ALWAYS_BLOCKS,
    ///     TAKE_THROWS_INTERRUPTED_EXCEPTION
    /// );
    ///
    /// BreakableBlockingQueue<String> brokenQueue = BreakableBlockingQueue.wrap(linkedQueue, breaks);
    /// // put() will block indefinitely due to break
    /// // take() will throw InterruptedException due to break
    /// ```
    ///
    /// **Break Application:**
    /// The breaks are applied immediately upon creation and will affect the behavior
    /// of corresponding `BlockingQueue` methods. See individual break constants for specific effects.
    ///
    /// **Thread Safety:**
    /// The created `BreakableBlockingQueue` is not thread-safe in terms of break configuration.
    /// The underlying `BlockingQueue`'s thread safety properties are maintained for normal operations,
    /// but external synchronization is required for concurrent break modifications.
    ///
    /// @param <E> the type of elements held in the blocking queue
    /// @param blockingQueue the `BlockingQueue` implementation to wrap; must not be null
    /// @param breaks the set of breaks to apply; must not be null, may be empty
    /// @return a new `BreakableBlockingQueue` wrapping the specified queue with the given breaks
    /// @throws NullPointerException if `blockingQueue` or `breaks` is null
    /// @see #wrap(BlockingQueue, Set, int)
    /// @see Break
    /// @see Builder
    /// @since 1.0
    public static <E> BreakableBlockingQueue<E> wrap(
            final BlockingQueue<E> blockingQueue,
            final Set<Break> breaks) {
        return new BreakableBlockingQueue<>(blockingQueue, breaks, DEFAULT_METHOD_STATUSES,
                DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS, DEFAULT_SAFETY, Object.class);
    }

    /// Creates a `BreakableBlockingQueue` that wraps the specified `BlockingQueue` with full configuration.
    ///
    /// This static factory method provides complete control over `BreakableBlockingQueue` creation,
    /// allowing specification of the underlying `BlockingQueue`, breaks to apply, and spliterator
    /// characteristics. This method is useful when precise control over spliterator
    /// behavior is required for advanced use cases.
    ///
    /// **Configuration:**
    /// - Uses specified spliterator characteristics
    /// - Uses default permit settings (allows nulls by default)
    /// - Applies all specified breaks immediately
    /// - All `BlockingQueue` methods are supported unless explicitly disabled
    ///
    /// **Usage Examples:**
    /// ```java
    /// // Create with custom spliterator characteristics
    /// ArrayBlockingQueue<Integer> arrayQueue = new ArrayBlockingQueue<>(100);
    /// arrayQueue.put(1);
    /// arrayQueue.put(2);
    /// arrayQueue.put(3);
    ///
    /// Set<Break> breaks = Set.of(REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO);
    /// int characteristics = Spliterator.ORDERED | Spliterator.SIZED | Spliterator.NONNULL;
    ///
    /// BreakableBlockingQueue<Integer> queue = BreakableBlockingQueue.wrap(
    ///     arrayQueue, breaks, characteristics);
    /// assertEquals(0, queue.remainingCapacity()); // Returns 0 due to break
    /// ```
    ///
    /// **Spliterator Characteristics:**
    /// The characteristics parameter controls the behavior of spliterators created
    /// from this queue. Common characteristics include:
    /// - `Spliterator.ORDERED` - Elements have a defined encounter order
    /// - `Spliterator.SIZED` - Size is known and finite
    /// - `Spliterator.NONNULL` - No null elements
    /// - `Spliterator.CONCURRENT` - Source may be safely concurrently modified
    ///
    /// **Thread Safety:**
    /// The created `BreakableBlockingQueue` is not thread-safe in terms of break configuration.
    /// The underlying `BlockingQueue`'s thread safety properties are maintained for normal operations,
    /// but external synchronization is required for concurrent break modifications.
    ///
    /// @param <E> the type of elements held in the blocking queue
    /// @param blockingQueue the `BlockingQueue` implementation to wrap; must not be null
    /// @param breaks the set of breaks to apply; must not be null, may be empty
    /// @param characteristics the spliterator characteristics for this queue
    /// @return a new `BreakableBlockingQueue` wrapping the specified queue with the given configuration
    /// @throws NullPointerException if `blockingQueue` or `breaks` is null
    /// @see #wrap(BlockingQueue, Set)
    /// @see Break
    /// @see Builder
    /// @see java.util.Spliterator
    /// @since 1.0
    public static <E> BreakableBlockingQueue<E> wrap(
            final BlockingQueue<E> blockingQueue,
            final Set<Break> breaks,
            final int characteristics) {
        return new BreakableBlockingQueue<>(blockingQueue, breaks, new HashMap<>(),
                characteristics, DEFAULT_PERMITS, DEFAULT_SAFETY, Object.class);
    }
}
