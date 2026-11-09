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
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.contract.blockingdeque.BlockingDequeMethods;
import com.soliscode.test.contract.blockingqueue.BlockingQueueMethods;

/// A BlockingDeque implementation that can be programmatically broken for testing purposes.
///
/// This class extends BreakableDeque and implements the BlockingDeque interface, providing
/// additional break constants for testing BlockingDeque-specific functionality. It wraps
/// an existing BlockingDeque and allows specific behaviors to be "broken" through the
/// Break mechanism.
///
/// ## Overview
///
/// BreakableBlockingDeque provides comprehensive testing capabilities for code that works with
/// BlockingDeque implementations. It supports all standard BlockingDeque operations while enabling
/// controlled behavioral modifications through break constants. This is particularly useful for:
///
/// - **Testing BlockingDeque Contract Compliance**: Verifying that code correctly handles blocking double-ended
///   operations
/// - **Concurrency Testing**: Simulating blocking, timeout, and interruption scenarios at both ends
/// - **Error Condition Simulation**: Testing how code responds to capacity limits and thread interruption
/// - **Performance Testing**: Simulating slow or failing blocking operations
/// - **Robustness Testing**: Verifying code resilience against unexpected BlockingDeque behaviors
///
/// ## BlockingDeque-Specific Break Constants
///
/// This class provides break constants for all BlockingDeque-specific methods:
///
/// ### Blocking Put Operations
/// - **PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION**: Forces putFirst() to throw InterruptedException
/// - **PUT_FIRST_DOES_NOT_ADD_ELEMENT**: Forces putFirst() to accept but not add elements
/// - **PUT_FIRST_THROWS_EXCEPTION**: Forces putFirst() to throw exceptions instead of normal operation
/// - **PUT_LAST_THROWS_INTERRUPTED_EXCEPTION**: Forces putLast() to throw InterruptedException
/// - **PUT_LAST_DOES_NOT_ADD_ELEMENT**: Forces putLast() to accept but not add elements
/// - **PUT_LAST_THROWS_EXCEPTION**: Forces putLast() to throw exceptions instead of normal operation
/// - **PUT_FIRST_ALWAYS_BLOCKS**, **PUT_LAST_ALWAYS_BLOCKS**: putFirst() and putLast() block until interrupted
/// - **PUT_FIRST_ADDS_TO_END**, **PUT_LAST_ADDS_TO_FRONT**: putFirst() and putLast() add at the other end
///
/// ### Blocking Take Operations
/// - **TAKE_FIRST_THROWS_INTERRUPTED_EXCEPTION**: Forces takeFirst() to throw InterruptedException
/// - **TAKE_FIRST_ALWAYS_RETURNS_NULL**: Control takeFirst() method to always return null
/// - **TAKE_FIRST_DOES_NOT_REMOVE_ELEMENT**: Forces takeFirst() to return element without removing it
/// - **TAKE_LAST_THROWS_INTERRUPTED_EXCEPTION**: Forces takeLast() to throw InterruptedException
/// - **TAKE_LAST_ALWAYS_RETURNS_NULL**: Control takeLast() method to always return null
/// - **TAKE_LAST_DOES_NOT_REMOVE_ELEMENT**: Forces takeLast() to return element without removing it
/// - **TAKE_FIRST_ALWAYS_BLOCKS**, **TAKE_LAST_ALWAYS_BLOCKS**: takeFirst() and takeLast() block until interrupted
/// - **TAKE_FIRST_REMOVES_LAST**, **TAKE_LAST_REMOVES_FIRST**: takeFirst() and takeLast() take from the other end
///
/// ### Timeout Operations
/// - **OFFER_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE**: Control offerFirst(timeout) to always return false
/// - **OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION**: Forces offerFirst(timeout) to throw
///   InterruptedException
/// - **OFFER_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE**: Control offerLast(timeout) to always return false
/// - **OFFER_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION**: Forces offerLast(timeout) to throw InterruptedException
/// - **POLL_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL**: Control pollFirst(timeout) to always return null
/// - **POLL_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION**: Forces pollFirst(timeout) to throw InterruptedException
/// - **POLL_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL**: Control pollLast(timeout) to always return null
/// - **POLL_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION**: Forces pollLast(timeout) to throw InterruptedException
/// - **OFFER_FIRST_WITH_TIMEOUT_DOES_NOT_WAIT**, **OFFER_LAST_WITH_TIMEOUT_DOES_NOT_WAIT**,
///   **POLL_FIRST_WITH_TIMEOUT_DOES_NOT_WAIT**, **POLL_LAST_WITH_TIMEOUT_DOES_NOT_WAIT**: the timed methods try once,
///   without waiting
/// - **OFFER_FIRST_WITH_TIMEOUT_ADDS_TO_END**, **OFFER_LAST_WITH_TIMEOUT_ADDS_TO_FRONT**,
///   **POLL_FIRST_WITH_TIMEOUT_REMOVES_LAST**, **POLL_LAST_WITH_TIMEOUT_REMOVES_FIRST**: the timed methods act on the
///   other end
///
/// ### Capacity and Drain Operations
/// - **REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO**: Forces remainingCapacity() to always return 0
/// - **DRAIN_TO_ALWAYS_RETURNS_ZERO**, **DRAIN_TO_THROWS_EXCEPTION**, **DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS**: as
///   [BreakableBlockingQueue]'s breaks of the same names. Their descriptions start with `BLOCKING_DEQUE_`, so that
///   they are not equal to those breaks ([Break] is a record)
///
/// ### Thread Safety
/// - **TAKE_FIRST_IS_NOT_THREAD_SAFE**, **TAKE_LAST_IS_NOT_THREAD_SAFE**,
///   **POLL_FIRST_WITH_TIMEOUT_IS_NOT_THREAD_SAFE**, **POLL_LAST_WITH_TIMEOUT_IS_NOT_THREAD_SAFE**,
///   **DRAIN_TO_IS_NOT_THREAD_SAFE**: read the store and then remove
///   from it in separate steps, so that two threads can both get one element. The store is a thread-safe JDK
///   `BlockingDeque`, so the other operations stay atomic whether or not the breakable is built thread safe.
///
/// ## Usage Examples
///
/// ### Basic BlockingDeque Testing
/// ```java
/// BreakableBlockingDeque<String> deque = new BreakableBlockingDeque<>();
/// deque.putFirst("first");
/// deque.putLast("last");
///
/// // Normal blocking deque operations
/// assertEquals("first", deque.takeFirst());
/// assertEquals("last", deque.takeLast());
/// ```
///
/// ### BlockingDeque Failure Testing
/// ```java
/// BreakableBlockingDeque<String> brokenDeque = new BreakableBlockingDeque.Builder<String>()
///     .addBreak(PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION)
///     .addBreak(TAKE_LAST_ALWAYS_RETURNS_NULL)
///     .build();
///
/// // Test blocking behavior
/// // putFirst() will throw InterruptedException due to break
/// // takeLast() will return null due to break
/// ```
///
/// ### Builder Pattern with BlockingDeque
/// ```java
/// BreakableBlockingDeque<Integer> deque = new BreakableBlockingDeque.Builder<Integer>()
///     .addBreak(OFFER_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE)
///     .addBreak(POLL_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION)
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
///     ↓
/// BreakableBlockingDeque<E>
/// ```
///
/// This class inherits all Collection, Queue, Deque, and Iterable functionality from its parent classes
/// while adding BlockingDeque-specific capabilities. All inherited break constants and behaviors remain available.
///
/// ## Thread Safety
///
/// Like its parent classes, BreakableBlockingDeque is not thread-safe in terms of break configuration.
/// The underlying BlockingDeque implementation provides the thread safety for normal operations,
/// but break checking adds additional complexity that requires external synchronization for
/// concurrent break modifications.
///
/// ## Performance Considerations
///
/// BlockingDeque operations delegate to the underlying BlockingDeque implementation unless breaks
/// are applied. Break checking adds minimal overhead. The class maintains the performance
/// characteristics of the underlying BlockingDeque for non-broken operations.
///
/// @param <E> the type of elements held in this blocking deque
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableDeque
/// @see BlockingDeque
/// @see Break
public class BreakableBlockingDeque<E> extends BreakableDeque<E> implements BlockingDeque<E>, Serializable {

    // ========== Put First Method Breaks ==========

    /// Break constant that causes putFirst() to throw an InterruptedException.
    ///
    /// When this break is applied, the putFirst() method will throw an InterruptedException
    /// instead of performing the normal blocking operation.
    ///
    /// **Affected Methods:**
    /// - {@link #putFirst(Object)} - Throws InterruptedException
    ///
    /// **Use Cases:**
    /// - Testing thread interruption handling in blocking first-end operations
    /// - Simulating thread interruption scenarios
    /// - Testing exception handling in concurrent deque operations
    public static final Break PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION =
            new Break("PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION");

    /// Break constant that causes putFirst() to accept elements but not actually add them.
    ///
    /// When this break is applied, the putFirst() method will return normally but
    /// the element will not be added to the front of the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #putFirst(Object)} - Returns normally but doesn't add element
    ///
    /// **Use Cases:**
    /// - Testing silent putFirst() operation failures
    /// - Simulating memory or storage issues in blocking operations
    /// - Testing deque state consistency
    public static final Break PUT_FIRST_DOES_NOT_ADD_ELEMENT =
            new Break("PUT_FIRST_DOES_NOT_ADD_ELEMENT");

    /// Break constant that causes putFirst() to throw an exception.
    ///
    /// When this break is applied, the putFirst() method will throw an IllegalStateException
    /// instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #putFirst(Object)} - Throws IllegalStateException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in blocking first-end operations
    /// - Simulating blocking operation failures
    /// - Testing robustness against unexpected exceptions
    public static final Break PUT_FIRST_THROWS_EXCEPTION =
            new Break("PUT_FIRST_THROWS_EXCEPTION");

    /// Break constant that causes `putFirst()` to block until interrupted.
    ///
    /// **Purpose:** simulates a `putFirst()` that blocks where it should return at once, though it waits only "if
    /// necessary" ([BlockingDeque#putFirst(Object)]).
    ///
    /// **Effect:** the call waits on a monitor that is never notified, whatever the deque holds, until the thread is
    /// interrupted; then it throws [InterruptedException]. The element is not added.
    ///
    /// **Affected Methods:**
    /// - {@link #putFirst(Object)} - Blocks until interrupted
    ///
    /// @see #putFirst(Object)
    /// @since 0.1.2
    public static final Break PUT_FIRST_ALWAYS_BLOCKS =
            new Break("PUT_FIRST_ALWAYS_BLOCKS");

    /// Break constant that causes `putFirst()` to act on the other end of the deque.
    ///
    /// **Purpose:** simulates a `putFirst()` that confuses the deque's ends.
    ///
    /// **Effect:** the call does what the store's `putLast(Object)` does, at the other end.
    ///
    /// **Affected Methods:**
    /// - {@link #putFirst(Object)} - Acts on the wrong end
    ///
    /// @see #putFirst(Object)
    /// @since 0.1.2
    public static final Break PUT_FIRST_ADDS_TO_END =
            new Break("PUT_FIRST_ADDS_TO_END");

    // ========== Put Last Method Breaks ==========

    /// Break constant that causes putLast() to throw an InterruptedException.
    ///
    /// When this break is applied, the putLast() method will throw an InterruptedException
    /// instead of performing the normal blocking operation.
    ///
    /// **Affected Methods:**
    /// - {@link #putLast(Object)} - Throws InterruptedException
    ///
    /// **Use Cases:**
    /// - Testing thread interruption handling in blocking last-end operations
    /// - Simulating thread interruption scenarios
    /// - Testing exception handling in concurrent deque operations
    public static final Break PUT_LAST_THROWS_INTERRUPTED_EXCEPTION =
            new Break("PUT_LAST_THROWS_INTERRUPTED_EXCEPTION");

    /// Break constant that causes putLast() to accept elements but not actually add them.
    ///
    /// When this break is applied, the putLast() method will return normally but
    /// the element will not be added to the back of the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #putLast(Object)} - Returns normally but doesn't add element
    ///
    /// **Use Cases:**
    /// - Testing silent putLast() operation failures
    /// - Simulating memory or storage issues in blocking operations
    /// - Testing deque state consistency
    public static final Break PUT_LAST_DOES_NOT_ADD_ELEMENT =
            new Break("PUT_LAST_DOES_NOT_ADD_ELEMENT");

    /// Break constant that causes putLast() to throw an exception.
    ///
    /// When this break is applied, the putLast() method will throw an IllegalStateException
    /// instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #putLast(Object)} - Throws IllegalStateException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in blocking last-end operations
    /// - Simulating blocking operation failures
    /// - Testing robustness against unexpected exceptions
    public static final Break PUT_LAST_THROWS_EXCEPTION =
            new Break("PUT_LAST_THROWS_EXCEPTION");

    /// Break constant that causes `putLast()` to block until interrupted.
    ///
    /// **Purpose:** simulates a `putLast()` that blocks where it should return at once, though it waits only "if
    /// necessary" ([BlockingDeque#putLast(Object)]).
    ///
    /// **Effect:** the call waits on a monitor that is never notified, whatever the deque holds, until the thread is
    /// interrupted; then it throws [InterruptedException]. The element is not added.
    ///
    /// **Affected Methods:**
    /// - {@link #putLast(Object)} - Blocks until interrupted
    /// - {@link #put(Object)} - The same, since `put` is `putLast`
    ///
    /// @see #putLast(Object)
    /// @since 0.1.2
    public static final Break PUT_LAST_ALWAYS_BLOCKS =
            new Break("PUT_LAST_ALWAYS_BLOCKS");

    /// Break constant that causes `putLast()` to act on the other end of the deque.
    ///
    /// **Purpose:** simulates a `putLast()` that confuses the deque's ends.
    ///
    /// **Effect:** the call does what the store's `putFirst(Object)` does, at the other end.
    ///
    /// **Affected Methods:**
    /// - {@link #putLast(Object)} - Acts on the wrong end
    /// - {@link #put(Object)} - The same, since `put` is `putLast`
    ///
    /// @see #putLast(Object)
    /// @since 0.1.2
    public static final Break PUT_LAST_ADDS_TO_FRONT =
            new Break("PUT_LAST_ADDS_TO_FRONT");

    // ========== Take First Method Breaks ==========

    /// Break constant that causes takeFirst() to throw an InterruptedException.
    ///
    /// When this break is applied, the takeFirst() method will throw an InterruptedException
    /// instead of performing the normal blocking operation.
    ///
    /// **Affected Methods:**
    /// - {@link #takeFirst()} - Throws InterruptedException
    ///
    /// **Use Cases:**
    /// - Testing thread interruption handling in blocking first-end retrieval
    /// - Simulating thread interruption scenarios
    /// - Testing exception handling in concurrent deque operations
    public static final Break TAKE_FIRST_THROWS_INTERRUPTED_EXCEPTION =
            new Break("TAKE_FIRST_THROWS_INTERRUPTED_EXCEPTION");

    /// Break constant that causes takeFirst() to always return null.
    ///
    /// When this break is applied, the takeFirst() method will return null instead
    /// of blocking or returning an element.
    ///
    /// **Affected Methods:**
    /// - {@link #takeFirst()} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing null return value handling (non-standard behavior)
    /// - Simulating corrupted blocking deque behavior
    /// - Testing robustness against unexpected return values
    public static final Break TAKE_FIRST_ALWAYS_RETURNS_NULL =
            new Break("TAKE_FIRST_ALWAYS_RETURNS_NULL");

    /// Break constant that causes takeFirst() to return an element without removing it.
    ///
    /// When this break is applied, the takeFirst() method will return the first element
    /// but not remove it from the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #takeFirst()} - Returns element but doesn't remove it
    ///
    /// **Use Cases:**
    /// - Testing deque state consistency
    /// - Simulating partial take operation failures
    /// - Testing duplicate element handling
    public static final Break TAKE_FIRST_DOES_NOT_REMOVE_ELEMENT =
            new Break("TAKE_FIRST_DOES_NOT_REMOVE_ELEMENT");

    /// Break constant that causes `takeFirst()` to block until interrupted.
    ///
    /// **Purpose:** simulates a `takeFirst()` that blocks where it should return at once, though it waits only "if
    /// necessary" ([BlockingDeque#takeFirst()]).
    ///
    /// **Effect:** the call waits on a monitor that is never notified, whatever the deque holds, until the thread is
    /// interrupted; then it throws [InterruptedException]. No element is removed.
    ///
    /// **Affected Methods:**
    /// - {@link #takeFirst()} - Blocks until interrupted
    /// - {@link #take()} - The same, since `take` is `takeFirst`
    ///
    /// @see #takeFirst()
    /// @since 0.1.2
    public static final Break TAKE_FIRST_ALWAYS_BLOCKS =
            new Break("TAKE_FIRST_ALWAYS_BLOCKS");

    /// Break constant that causes `takeFirst()` to act on the other end of the deque.
    ///
    /// **Purpose:** simulates a `takeFirst()` that confuses the deque's ends.
    ///
    /// **Effect:** the call does what the store's `takeLast()` does, at the other end.
    ///
    /// **Affected Methods:**
    /// - {@link #takeFirst()} - Acts on the wrong end
    /// - {@link #take()} - The same, since `take` is `takeFirst`
    ///
    /// @see #takeFirst()
    /// @since 0.1.2
    public static final Break TAKE_FIRST_REMOVES_LAST =
            new Break("TAKE_FIRST_REMOVES_LAST");

    /// Break constant that makes `takeFirst()` not atomic.
    ///
    /// **Purpose:** simulates a `takeFirst()` that is not thread safe, though a BlockingDeque, "Like BlockingQueue",
    /// is thread safe ([BlockingDeque]).
    ///
    /// **Effect:** on a non-empty deque, the call reads the end with `peekFirst()` and then removes it with
    /// `removeFirstOccurrence(Object)`, two steps another thread can come between, so two callers can both return one
    /// element. On an empty deque it waits as normal.
    ///
    /// **Affected Methods:**
    /// - {@link #takeFirst()} - Not atomic
    /// - {@link #take()} - The same, since `take` is `takeFirst`
    ///
    /// @see #takeFirst()
    /// @since 0.1.2
    public static final Break TAKE_FIRST_IS_NOT_THREAD_SAFE =
            new Break("TAKE_FIRST_IS_NOT_THREAD_SAFE");

    // ========== Take Last Method Breaks ==========

    /// Break constant that causes takeLast() to throw an InterruptedException.
    ///
    /// When this break is applied, the takeLast() method will throw an InterruptedException
    /// instead of performing the normal blocking operation.
    ///
    /// **Affected Methods:**
    /// - {@link #takeLast()} - Throws InterruptedException
    ///
    /// **Use Cases:**
    /// - Testing thread interruption handling in blocking last-end retrieval
    /// - Simulating thread interruption scenarios
    /// - Testing exception handling in concurrent deque operations
    public static final Break TAKE_LAST_THROWS_INTERRUPTED_EXCEPTION =
            new Break("TAKE_LAST_THROWS_INTERRUPTED_EXCEPTION");

    /// Break constant that causes takeLast() to always return null.
    ///
    /// When this break is applied, the takeLast() method will return null instead
    /// of blocking or returning an element.
    ///
    /// **Affected Methods:**
    /// - {@link #takeLast()} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing null return value handling (non-standard behavior)
    /// - Simulating corrupted blocking deque behavior
    /// - Testing robustness against unexpected return values
    public static final Break TAKE_LAST_ALWAYS_RETURNS_NULL =
            new Break("TAKE_LAST_ALWAYS_RETURNS_NULL");

    /// Break constant that causes takeLast() to return an element without removing it.
    ///
    /// When this break is applied, the takeLast() method will return the last element
    /// but not remove it from the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #takeLast()} - Returns element but doesn't remove it
    ///
    /// **Use Cases:**
    /// - Testing deque state consistency
    /// - Simulating partial take operation failures
    /// - Testing duplicate element handling
    public static final Break TAKE_LAST_DOES_NOT_REMOVE_ELEMENT =
            new Break("TAKE_LAST_DOES_NOT_REMOVE_ELEMENT");

    /// Break constant that causes `takeLast()` to block until interrupted.
    ///
    /// **Purpose:** simulates a `takeLast()` that blocks where it should return at once, though it waits only "if
    /// necessary" ([BlockingDeque#takeLast()]).
    ///
    /// **Effect:** the call waits on a monitor that is never notified, whatever the deque holds, until the thread is
    /// interrupted; then it throws [InterruptedException]. No element is removed.
    ///
    /// **Affected Methods:**
    /// - {@link #takeLast()} - Blocks until interrupted
    ///
    /// @see #takeLast()
    /// @since 0.1.2
    public static final Break TAKE_LAST_ALWAYS_BLOCKS =
            new Break("TAKE_LAST_ALWAYS_BLOCKS");

    /// Break constant that causes `takeLast()` to act on the other end of the deque.
    ///
    /// **Purpose:** simulates a `takeLast()` that confuses the deque's ends.
    ///
    /// **Effect:** the call does what the store's `takeFirst()` does, at the other end.
    ///
    /// **Affected Methods:**
    /// - {@link #takeLast()} - Acts on the wrong end
    ///
    /// @see #takeLast()
    /// @since 0.1.2
    public static final Break TAKE_LAST_REMOVES_FIRST =
            new Break("TAKE_LAST_REMOVES_FIRST");

    /// Break constant that makes `takeLast()` not atomic.
    ///
    /// **Purpose:** simulates a `takeLast()` that is not thread safe, though a BlockingDeque, "Like BlockingQueue", is
    /// thread safe ([BlockingDeque]).
    ///
    /// **Effect:** on a non-empty deque, the call reads the end with `peekLast()` and then removes it with
    /// `removeLastOccurrence(Object)`, two steps another thread can come between, so two callers can both return one
    /// element. On an empty deque it waits as normal.
    ///
    /// **Affected Methods:**
    /// - {@link #takeLast()} - Not atomic
    ///
    /// @see #takeLast()
    /// @since 0.1.2
    public static final Break TAKE_LAST_IS_NOT_THREAD_SAFE =
            new Break("TAKE_LAST_IS_NOT_THREAD_SAFE");

    // ========== Offer First With Timeout Method Breaks ==========

    /// Break constant that causes offerFirst(timeout) to always return false.
    ///
    /// When this break is applied, the offerFirst() method with timeout will always return false
    /// regardless of whether the element was actually added to the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #offerFirst(Object, long, TimeUnit)} - Returns false
    ///
    /// **Use Cases:**
    /// - Testing offerFirst() timeout failure handling
    /// - Simulating capacity constraints at the front with timeout
    /// - Testing timeout return value handling
    public static final Break OFFER_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE =
            new Break("OFFER_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE");

    /// Break constant that causes offerFirst(timeout) to throw an InterruptedException.
    ///
    /// When this break is applied, the offerFirst() method with timeout will throw an InterruptedException
    /// instead of performing the normal timed operation.
    ///
    /// **Affected Methods:**
    /// - {@link #offerFirst(Object, long, TimeUnit)} - Throws InterruptedException
    ///
    /// **Use Cases:**
    /// - Testing thread interruption handling in timed first-end operations
    /// - Simulating thread interruption during timeout
    /// - Testing exception handling in concurrent timed operations
    public static final Break OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION =
            new Break("OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION");

    /// Break constant that causes the timed `offerFirst` not to wait.
    ///
    /// **Purpose:** simulates a timed `offerFirst` that gives up at once, instead of waiting up to the specified wait
    /// time ([BlockingDeque#offerFirst(Object, long, TimeUnit)]).
    ///
    /// **Effect:** the store's `offerFirst(Object)` is called once and the timeout is ignored; on a full deque the
    /// call returns `false` at once, even with the thread's interrupt status set.
    ///
    /// **Affected Methods:**
    /// - {@link #offerFirst(Object, long, TimeUnit)} - Does not wait
    ///
    /// @see #offerFirst(Object, long, TimeUnit)
    /// @since 0.1.2
    public static final Break OFFER_FIRST_WITH_TIMEOUT_DOES_NOT_WAIT =
            new Break("OFFER_FIRST_WITH_TIMEOUT_DOES_NOT_WAIT");

    /// Break constant that causes `offerFirst(E, long, TimeUnit)` to act on the other end of the deque.
    ///
    /// **Purpose:** simulates an `offerFirst(E, long, TimeUnit)` that confuses the deque's ends.
    ///
    /// **Effect:** the call does what the store's `offerLast(Object, long, TimeUnit)` does, at the other end.
    ///
    /// **Affected Methods:**
    /// - {@link #offerFirst(Object, long, TimeUnit)} - Acts on the wrong end
    ///
    /// @see #offerFirst(Object, long, TimeUnit)
    /// @since 0.1.2
    public static final Break OFFER_FIRST_WITH_TIMEOUT_ADDS_TO_END =
            new Break("OFFER_FIRST_WITH_TIMEOUT_ADDS_TO_END");

    // ========== Offer Last With Timeout Method Breaks ==========

    /// Break constant that causes offerLast(timeout) to always return false.
    ///
    /// When this break is applied, the offerLast() method with timeout will always return false
    /// regardless of whether the element was actually added to the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #offerLast(Object, long, TimeUnit)} - Returns false
    ///
    /// **Use Cases:**
    /// - Testing offerLast() timeout failure handling
    /// - Simulating capacity constraints at the back with timeout
    /// - Testing timeout return value handling
    public static final Break OFFER_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE =
            new Break("OFFER_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE");

    /// Break constant that causes offerLast(timeout) to throw an InterruptedException.
    ///
    /// When this break is applied, the offerLast() method with timeout will throw an InterruptedException
    /// instead of performing the normal timed operation.
    ///
    /// **Affected Methods:**
    /// - {@link #offerLast(Object, long, TimeUnit)} - Throws InterruptedException
    ///
    /// **Use Cases:**
    /// - Testing thread interruption handling in timed last-end operations
    /// - Simulating thread interruption during timeout
    /// - Testing exception handling in concurrent timed operations
    public static final Break OFFER_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION =
            new Break("OFFER_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION");

    /// Break constant that causes the timed `offerLast` not to wait.
    ///
    /// **Purpose:** simulates a timed `offerLast` that gives up at once, instead of waiting up to the specified wait
    /// time ([BlockingDeque#offerLast(Object, long, TimeUnit)]).
    ///
    /// **Effect:** the store's `offerLast(Object)` is called once and the timeout is ignored; on a full deque the call
    /// returns `false` at once, even with the thread's interrupt status set.
    ///
    /// **Affected Methods:**
    /// - {@link #offerLast(Object, long, TimeUnit)} - Does not wait
    /// - {@link #offer(Object, long, TimeUnit)} - The same, since the timed `offer` is the timed `offerLast`
    ///
    /// @see #offerLast(Object, long, TimeUnit)
    /// @since 0.1.2
    public static final Break OFFER_LAST_WITH_TIMEOUT_DOES_NOT_WAIT =
            new Break("OFFER_LAST_WITH_TIMEOUT_DOES_NOT_WAIT");

    /// Break constant that causes `offerLast(E, long, TimeUnit)` to act on the other end of the deque.
    ///
    /// **Purpose:** simulates an `offerLast(E, long, TimeUnit)` that confuses the deque's ends.
    ///
    /// **Effect:** the call does what the store's `offerFirst(Object, long, TimeUnit)` does, at the other end.
    ///
    /// **Affected Methods:**
    /// - {@link #offerLast(Object, long, TimeUnit)} - Acts on the wrong end
    /// - {@link #offer(Object, long, TimeUnit)} - The same, since the timed `offer` is the timed `offerLast`
    ///
    /// @see #offerLast(Object, long, TimeUnit)
    /// @since 0.1.2
    public static final Break OFFER_LAST_WITH_TIMEOUT_ADDS_TO_FRONT =
            new Break("OFFER_LAST_WITH_TIMEOUT_ADDS_TO_FRONT");

    // ========== Poll First With Timeout Method Breaks ==========

    /// Break constant that causes pollFirst(timeout) to always return null.
    ///
    /// When this break is applied, the pollFirst() method with timeout will always return null
    /// regardless of whether elements are available in the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #pollFirst(long, TimeUnit)} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing pollFirst() timeout null handling
    /// - Simulating empty deque scenarios with timeout
    /// - Testing timeout null return value handling
    public static final Break POLL_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL =
            new Break("POLL_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL");

    /// Break constant that causes pollFirst(timeout) to throw an InterruptedException.
    ///
    /// When this break is applied, the pollFirst() method with timeout will throw an InterruptedException
    /// instead of performing the normal timed operation.
    ///
    /// **Affected Methods:**
    /// - {@link #pollFirst(long, TimeUnit)} - Throws InterruptedException
    ///
    /// **Use Cases:**
    /// - Testing thread interruption handling in timed first-end retrieval
    /// - Simulating thread interruption during timeout
    /// - Testing exception handling in concurrent timed operations
    public static final Break POLL_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION =
            new Break("POLL_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION");

    /// Break constant that causes the timed `pollFirst` not to wait.
    ///
    /// **Purpose:** simulates a timed `pollFirst` that gives up at once, instead of waiting up to the specified wait
    /// time ([BlockingDeque#pollFirst(long, TimeUnit)]).
    ///
    /// **Effect:** the store's `pollFirst()` is called once and the timeout is ignored; on an empty deque the call
    /// returns `null` at once, even with the thread's interrupt status set.
    ///
    /// **Affected Methods:**
    /// - {@link #pollFirst(long, TimeUnit)} - Does not wait
    /// - {@link #poll(long, TimeUnit)} - The same, since the timed `poll` is the timed `pollFirst`
    ///
    /// @see #pollFirst(long, TimeUnit)
    /// @since 0.1.2
    public static final Break POLL_FIRST_WITH_TIMEOUT_DOES_NOT_WAIT =
            new Break("POLL_FIRST_WITH_TIMEOUT_DOES_NOT_WAIT");

    /// Break constant that causes `pollFirst(long, TimeUnit)` to act on the other end of the deque.
    ///
    /// **Purpose:** simulates a `pollFirst(long, TimeUnit)` that confuses the deque's ends.
    ///
    /// **Effect:** the call does what the store's `pollLast(long, TimeUnit)` does, at the other end.
    ///
    /// **Affected Methods:**
    /// - {@link #pollFirst(long, TimeUnit)} - Acts on the wrong end
    /// - {@link #poll(long, TimeUnit)} - The same, since the timed `poll` is the timed `pollFirst`
    ///
    /// @see #pollFirst(long, TimeUnit)
    /// @since 0.1.2
    public static final Break POLL_FIRST_WITH_TIMEOUT_REMOVES_LAST =
            new Break("POLL_FIRST_WITH_TIMEOUT_REMOVES_LAST");

    /// Break constant that makes `pollFirst(long, TimeUnit)` not atomic.
    ///
    /// **Purpose:** simulates a `pollFirst(long, TimeUnit)` that is not thread safe, though a BlockingDeque, "Like
    /// BlockingQueue", is thread safe ([BlockingDeque]).
    ///
    /// **Effect:** on a non-empty deque, the call reads the end with `peekFirst()` and then removes it with
    /// `removeFirstOccurrence(Object)`, two steps another thread can come between, so two callers can both return one
    /// element. On an empty deque it waits as normal.
    ///
    /// **Affected Methods:**
    /// - {@link #pollFirst(long, TimeUnit)} - Not atomic
    /// - {@link #poll(long, TimeUnit)} - The same, since the timed `poll` is the timed `pollFirst`
    ///
    /// @see #pollFirst(long, TimeUnit)
    /// @since 0.1.2
    public static final Break POLL_FIRST_WITH_TIMEOUT_IS_NOT_THREAD_SAFE =
            new Break("POLL_FIRST_WITH_TIMEOUT_IS_NOT_THREAD_SAFE");

    // ========== Poll Last With Timeout Method Breaks ==========

    /// Break constant that causes pollLast(timeout) to always return null.
    ///
    /// When this break is applied, the pollLast() method with timeout will always return null
    /// regardless of whether elements are available in the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #pollLast(long, TimeUnit)} - Returns null
    ///
    /// **Use Cases:**
    /// - Testing pollLast() timeout null handling
    /// - Simulating empty deque scenarios with timeout
    /// - Testing timeout null return value handling
    public static final Break POLL_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL =
            new Break("POLL_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL");

    /// Break constant that causes pollLast(timeout) to throw an InterruptedException.
    ///
    /// When this break is applied, the pollLast() method with timeout will throw an InterruptedException
    /// instead of performing the normal timed operation.
    ///
    /// **Affected Methods:**
    /// - {@link #pollLast(long, TimeUnit)} - Throws InterruptedException
    ///
    /// **Use Cases:**
    /// - Testing thread interruption handling in timed last-end retrieval
    /// - Simulating thread interruption during timeout
    /// - Testing exception handling in concurrent timed operations
    public static final Break POLL_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION =
            new Break("POLL_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION");

    /// Break constant that causes the timed `pollLast` not to wait.
    ///
    /// **Purpose:** simulates a timed `pollLast` that gives up at once, instead of waiting up to the specified wait
    /// time ([BlockingDeque#pollLast(long, TimeUnit)]).
    ///
    /// **Effect:** the store's `pollLast()` is called once and the timeout is ignored; on an empty deque the call
    /// returns `null` at once, even with the thread's interrupt status set.
    ///
    /// **Affected Methods:**
    /// - {@link #pollLast(long, TimeUnit)} - Does not wait
    ///
    /// @see #pollLast(long, TimeUnit)
    /// @since 0.1.2
    public static final Break POLL_LAST_WITH_TIMEOUT_DOES_NOT_WAIT =
            new Break("POLL_LAST_WITH_TIMEOUT_DOES_NOT_WAIT");

    /// Break constant that causes `pollLast(long, TimeUnit)` to act on the other end of the deque.
    ///
    /// **Purpose:** simulates a `pollLast(long, TimeUnit)` that confuses the deque's ends.
    ///
    /// **Effect:** the call does what the store's `pollFirst(long, TimeUnit)` does, at the other end.
    ///
    /// **Affected Methods:**
    /// - {@link #pollLast(long, TimeUnit)} - Acts on the wrong end
    ///
    /// @see #pollLast(long, TimeUnit)
    /// @since 0.1.2
    public static final Break POLL_LAST_WITH_TIMEOUT_REMOVES_FIRST =
            new Break("POLL_LAST_WITH_TIMEOUT_REMOVES_FIRST");

    /// Break constant that makes `pollLast(long, TimeUnit)` not atomic.
    ///
    /// **Purpose:** simulates a `pollLast(long, TimeUnit)` that is not thread safe, though a BlockingDeque, "Like
    /// BlockingQueue", is thread safe ([BlockingDeque]).
    ///
    /// **Effect:** on a non-empty deque, the call reads the end with `peekLast()` and then removes it with
    /// `removeLastOccurrence(Object)`, two steps another thread can come between, so two callers can both return one
    /// element. On an empty deque it waits as normal.
    ///
    /// **Affected Methods:**
    /// - {@link #pollLast(long, TimeUnit)} - Not atomic
    ///
    /// @see #pollLast(long, TimeUnit)
    /// @since 0.1.2
    public static final Break POLL_LAST_WITH_TIMEOUT_IS_NOT_THREAD_SAFE =
            new Break("POLL_LAST_WITH_TIMEOUT_IS_NOT_THREAD_SAFE");

    // ========== Capacity and Drain Method Breaks ==========

    /// Break constant that causes `remainingCapacity()` to always return zero.
    ///
    /// **Purpose:** simulates a deque that reports no room, whatever it holds.
    ///
    /// **Effect:** the call returns `0`.
    ///
    /// **Affected Methods:**
    /// - {@link #remainingCapacity()} - Returns 0
    ///
    /// @see #remainingCapacity()
    /// @since 0.1.2
    public static final Break REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO =
            new Break("BLOCKING_DEQUE_REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO");

    /// Break constant that causes `drainTo` to always return zero.
    ///
    /// **Purpose:** simulates a drain that moves nothing.
    ///
    /// **Effect:** both forms return `0` and move nothing.
    ///
    /// **Affected Methods:**
    /// - {@link #drainTo(Collection)} - Returns 0
    /// - {@link #drainTo(Collection, int)} - Returns 0
    ///
    /// @see #drainTo(Collection)
    /// @since 0.1.2
    public static final Break DRAIN_TO_ALWAYS_RETURNS_ZERO =
            new Break("BLOCKING_DEQUE_DRAIN_TO_ALWAYS_RETURNS_ZERO");

    /// Break constant that causes `drainTo` to throw an exception.
    ///
    /// **Purpose:** simulates a drain that fails.
    ///
    /// **Effect:** both forms throw [IllegalStateException] and move nothing.
    ///
    /// **Affected Methods:**
    /// - {@link #drainTo(Collection)} - Throws IllegalStateException
    /// - {@link #drainTo(Collection, int)} - Throws IllegalStateException
    ///
    /// @see #drainTo(Collection)
    /// @since 0.1.2
    public static final Break DRAIN_TO_THROWS_EXCEPTION =
            new Break("BLOCKING_DEQUE_DRAIN_TO_THROWS_EXCEPTION");

    /// Break constant that causes `drainTo` to copy elements without removing them.
    ///
    /// **Purpose:** simulates a drain that leaves what it moved in the deque.
    ///
    /// **Effect:** both forms add the elements to the collection and return their count, but leave them in the deque.
    ///
    /// **Affected Methods:**
    /// - {@link #drainTo(Collection)} - Does not remove
    /// - {@link #drainTo(Collection, int)} - Does not remove
    ///
    /// @see #drainTo(Collection)
    /// @since 0.1.2
    public static final Break DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS =
            new Break("BLOCKING_DEQUE_DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS");

    /// Break constant that makes `drainTo` not atomic.
    ///
    /// **Purpose:** simulates a `drainTo` that is not thread safe, though a BlockingDeque, "Like BlockingQueue", is
    /// thread safe ([BlockingDeque]).
    ///
    /// **Effect:** `drainTo` copies the elements it would move, adds the copy to the collection, and then removes each
    /// from the deque, steps another thread can come between, so two drainers can both move one element.
    ///
    /// **Affected Methods:**
    /// - {@link #drainTo(Collection)} - Not atomic
    /// - {@link #drainTo(Collection, int)} - Not atomic
    ///
    /// @see #drainTo(Collection)
    /// @since 0.1.2
    public static final Break DRAIN_TO_IS_NOT_THREAD_SAFE =
            new Break("BLOCKING_DEQUE_DRAIN_TO_IS_NOT_THREAD_SAFE");

    @Serial
    private static final long serialVersionUID = 1L;

    // ========== Instance Fields ==========

    /// The underlying BlockingDeque that this BreakableBlockingDeque wraps.
    /// All operations delegate to this deque unless breaks are applied.
    private final BlockingDeque<E> blockingDeque;

    // ========== Constructors ==========

    /// Creates an empty BreakableBlockingDeque backed by a LinkedBlockingDeque.
    ///
    /// This constructor creates a new BreakableBlockingDeque backed by an empty LinkedBlockingDeque.
    /// The deque will have default null policies and no breaks applied.
    ///
    /// **Default Configuration:**
    /// - Empty BlockingDeque (LinkedBlockingDeque implementation)
    /// - Permits null elements: true
    /// - No breaks applied
    ///
    /// **Usage:**
    /// ```java
    /// BreakableBlockingDeque<String> deque = new BreakableBlockingDeque<>();
    /// deque.putFirst("first");
    /// deque.putLast("last");
    /// ```
    public BreakableBlockingDeque() {
        this(new LinkedBlockingDeque<>(), DEFAULT_BREAKS, DEFAULT_METHOD_STATUSES, DEFAULT_CHARACTERISTICS,
                DEFAULT_PERMITS, DEFAULT_SAFETY, Object.class);
    }

    /// Creates a BreakableBlockingDeque by copying another BreakableBlockingDeque.
    ///
    /// This constructor creates a new instance that shares the underlying deque data
    /// and inherits all configuration from the source deque.
    ///
    /// **Inherited Configuration:**
    /// - All break settings from source
    /// - Null element policies
    /// - Method support configuration
    /// - Underlying BlockingDeque reference (shared, not copied)
    ///
    /// **Usage:**
    /// ```java
    /// BreakableBlockingDeque<String> original = new BreakableBlockingDeque<>();
    /// BreakableBlockingDeque<String> copy = new BreakableBlockingDeque<>(original);
    /// ```
    ///
    /// @param other the BreakableBlockingDeque to copy configuration from
    /// @throws NullPointerException if other is null
    public BreakableBlockingDeque(final BreakableBlockingDeque<E> other) {
        this(new LinkedBlockingDeque<>(other.blockingDeque), new HashSet<>(other.breaks()),
                new HashMap<>(other.methodStatuses()), other.characteristics(), other.permits(), other.isSafe(),
                other.compatibleType());
    }

    /// Constructs a new `BreakableBlockingDeque` which allows enhanced control over a blocking deque
    /// by introducing breakpoints and status tracking for method executions. This facilitates flexible,
    /// interruptible operations in concurrent programming scenarios.
    ///
    /// @param blockingDequeArg   The underlying [BlockingDeque] instance that this class wraps. It must
    ///                        not be `null` and should be correctly initialized for proper functioning.
    /// @param breaks          A [Set] of `Break` instances that define interruption points for certain
    ///                        operations on the deque. This enables controlled interruptions during deque usage.
    /// @param methodStatuses  A [Map] mapping `InterfaceMethod` enums to corresponding [MethodStatus]
    ///                        values. This provides a mechanism to configure and track the execution behavior
    ///                        of specific methods.
    /// @param characteristics Integer flags that define configurable properties of this deque, such as
    ///                        performance characteristics or customization for specific use cases.
    /// @param permits         The maximum number of permits available to control access to the deque. This parameter
    ///                        is used to handle concurrency limits during deque operations.
    /// @param isSafe          A `boolean` flag indicating whether this deque implementation guarantees thread-safety.
    ///                        If `true`, additional internal mechanisms ensure safe concurrent usage.
    /// @param compatibleType  The [Class] type that this deque is compatible with. This is typically
    ///                        used for ensuring type safety and compatibility during deque processing.
    protected BreakableBlockingDeque(
            final BlockingDeque<E> blockingDequeArg,
            final Set<Break> breaks,
            final Map<InterfaceMethod, MethodStatus> methodStatuses,
            final int characteristics,
            final int permits,
            final boolean isSafe,
            final Class<?> compatibleType) {
        super(blockingDequeArg, breaks, methodStatuses, characteristics, permits, isSafe,
                compatibleType);
        this.blockingDeque = blockingDequeArg;
    }

    // BreakableBlockingDeque cannot extend both BreakableDeque and BreakableBlockingQueue, so we need to
    // re-resolve that annotation differences that were handled in BreakableBlockingQueue.

    /// {@inheritDoc}
    @Override
    public boolean add(final E e) {   // resolve annotation conflict between Collection and BlockingDeque
        return super.add(e);
    }

    /// {@inheritDoc}
    @Override
    public boolean offer(final E e) { // resolve annotation conflict between queue and BlockingQueue
        return super.offer(e);
    }

    /// {@inheritDoc}
    @Override
    public void push(final E e) { // resolve annotation conflict between Deque and BlockingDeque
        super.push(e);
    }

    // ========== BlockingDeque Interface Implementation ==========

    /// {@inheritDoc}
    ///
    /// Inserts the specified element at the front of this deque, waiting if necessary for space to become available.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    /// - {@link #PUT_FIRST_DOES_NOT_ADD_ELEMENT} - Returns normally but doesn't add element
    /// - {@link #PUT_FIRST_THROWS_EXCEPTION} - Throws IllegalStateException
    /// - {@link #PUT_FIRST_ALWAYS_BLOCKS} - Blocks until interrupted
    /// - {@link #PUT_FIRST_ADDS_TO_END} - Adds at the end
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION break
    /// - May throw IllegalStateException due to PUT_FIRST_THROWS_EXCEPTION break
    @Override
    public void putFirst(final E e) throws InterruptedException {
        checkMethodSupport(BlockingDequeMethods.PUT_FIRST);

        if (hasBreak(PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("PutFirst operation interrupted due to break");
        }

        if (hasBreak(PUT_FIRST_THROWS_EXCEPTION)) {
            throw new IllegalStateException("PutFirst operation failed due to break");
        }

        if (hasBreak(PUT_FIRST_DOES_NOT_ADD_ELEMENT)) {
            return; // Return without adding the element
        }

        if (hasBreak(PUT_FIRST_ALWAYS_BLOCKS)) {
            blockUntilInterrupted();
        }

        if (hasBreak(PUT_FIRST_ADDS_TO_END)) {
            blockingDeque.putLast(e);
            return;
        }

        blockingDeque.putFirst(e);
    }

    /// {@inheritDoc}
    ///
    /// Inserts the specified element at the end of this deque, waiting if necessary for space to become available.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #PUT_LAST_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    /// - {@link #PUT_LAST_DOES_NOT_ADD_ELEMENT} - Returns normally but doesn't add element
    /// - {@link #PUT_LAST_THROWS_EXCEPTION} - Throws IllegalStateException
    /// - {@link #PUT_LAST_ALWAYS_BLOCKS} - Blocks until interrupted
    /// - {@link #PUT_LAST_ADDS_TO_FRONT} - Adds at the front
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to PUT_LAST_THROWS_INTERRUPTED_EXCEPTION break
    /// - May throw IllegalStateException due to PUT_LAST_THROWS_EXCEPTION break
    @Override
    public void putLast(final E e) throws InterruptedException {
        checkMethodSupport(BlockingDequeMethods.PUT_LAST);

        if (hasBreak(PUT_LAST_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("PutLast operation interrupted due to break");
        }

        if (hasBreak(PUT_LAST_THROWS_EXCEPTION)) {
            throw new IllegalStateException("PutLast operation failed due to break");
        }

        if (hasBreak(PUT_LAST_DOES_NOT_ADD_ELEMENT)) {
            return; // Return without adding the element
        }

        if (hasBreak(PUT_LAST_ALWAYS_BLOCKS)) {
            blockUntilInterrupted();
        }

        if (hasBreak(PUT_LAST_ADDS_TO_FRONT)) {
            blockingDeque.putFirst(e);
            return;
        }

        blockingDeque.putLast(e);
    }

    /// {@inheritDoc}
    ///
    /// Retrieves and removes the first element of this deque, waiting if necessary until an element becomes available.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #TAKE_FIRST_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    /// - {@link #TAKE_FIRST_ALWAYS_RETURNS_NULL} - Returns null
    /// - {@link #TAKE_FIRST_DOES_NOT_REMOVE_ELEMENT} - Returns element but doesn't remove it
    /// - {@link #TAKE_FIRST_ALWAYS_BLOCKS} - Blocks until interrupted
    /// - {@link #TAKE_FIRST_REMOVES_LAST} - Takes the last element
    /// - {@link #TAKE_FIRST_IS_NOT_THREAD_SAFE} - Reads and removes the first element in two steps
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to TAKE_FIRST_THROWS_INTERRUPTED_EXCEPTION break
    @Override
    public @Nullable E takeFirst() throws InterruptedException {
        checkMethodSupport(BlockingDequeMethods.TAKE_FIRST);

        if (hasBreak(TAKE_FIRST_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("TakeFirst operation interrupted due to break");
        }

        if (hasBreak(TAKE_FIRST_ALWAYS_RETURNS_NULL)) {
            return null; // Non-standard behavior for testing
        }

        if (hasBreak(TAKE_FIRST_ALWAYS_BLOCKS)) {
            blockUntilInterrupted();
        }

        if (hasBreak(TAKE_FIRST_DOES_NOT_REMOVE_ELEMENT)) {
            E e = blockingDeque.takeFirst();
            blockingDeque.offerFirst(e);
            return e;
        }

        if (hasBreak(TAKE_FIRST_REMOVES_LAST)) {
            return blockingDeque.takeLast();
        }

        if (hasBreak(TAKE_FIRST_IS_NOT_THREAD_SAFE)) {
            E first = blockingDeque.peekFirst();
            if (first != null) {
                blockingDeque.removeFirstOccurrence(first);
                return first;
            }
        }

        return blockingDeque.takeFirst();
    }

    /// {@inheritDoc}
    ///
    /// Retrieves and removes the last element of this deque, waiting if necessary until an element becomes available.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #TAKE_LAST_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    /// - {@link #TAKE_LAST_ALWAYS_RETURNS_NULL} - Returns null
    /// - {@link #TAKE_LAST_DOES_NOT_REMOVE_ELEMENT} - Returns element but doesn't remove it
    /// - {@link #TAKE_LAST_ALWAYS_BLOCKS} - Blocks until interrupted
    /// - {@link #TAKE_LAST_REMOVES_FIRST} - Takes the first element
    /// - {@link #TAKE_LAST_IS_NOT_THREAD_SAFE} - Reads and removes the last element in two steps
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to TAKE_LAST_THROWS_INTERRUPTED_EXCEPTION break
    @Override
    public @Nullable E takeLast() throws InterruptedException {
        checkMethodSupport(BlockingDequeMethods.TAKE_LAST);

        if (hasBreak(TAKE_LAST_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("TakeLast operation interrupted due to break");
        }

        if (hasBreak(TAKE_LAST_ALWAYS_RETURNS_NULL)) {
            return null; // Non-standard behavior for testing
        }

        if (hasBreak(TAKE_LAST_ALWAYS_BLOCKS)) {
            blockUntilInterrupted();
        }

        if (hasBreak(TAKE_LAST_DOES_NOT_REMOVE_ELEMENT)) {
            E e = blockingDeque.takeLast();
            blockingDeque.offerLast(e);
            return e;
        }

        if (hasBreak(TAKE_LAST_REMOVES_FIRST)) {
            return blockingDeque.takeFirst();
        }

        if (hasBreak(TAKE_LAST_IS_NOT_THREAD_SAFE)) {
            E last = blockingDeque.peekLast();
            if (last != null) {
                blockingDeque.removeLastOccurrence(last);
                return last;
            }
        }

        return blockingDeque.takeLast();
    }

    /// {@inheritDoc}
    ///
    /// Inserts the specified element at the front of this deque, waiting up to the specified wait time if necessary for
    /// space to become available. The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #OFFER_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE} - Returns false regardless of success
    /// - {@link #OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    /// - {@link #OFFER_FIRST_WITH_TIMEOUT_DOES_NOT_WAIT} - Offers once, without waiting
    /// - {@link #OFFER_FIRST_WITH_TIMEOUT_ADDS_TO_END} - Adds at the end
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break
    @Override
    public boolean offerFirst(final E e, final long timeout, final TimeUnit unit)
            throws InterruptedException {
        checkMethodSupport(BlockingDequeMethods.OFFER_FIRST_TIMEOUT);

        if (hasBreak(OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("OfferFirst with timeout operation interrupted due to break");
        }

        if (hasBreak(OFFER_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE)) {
            return false;
        }

        if (hasBreak(OFFER_FIRST_WITH_TIMEOUT_DOES_NOT_WAIT)) {
            return blockingDeque.offerFirst(e);
        }

        if (hasBreak(OFFER_FIRST_WITH_TIMEOUT_ADDS_TO_END)) {
            return blockingDeque.offerLast(e, timeout, unit);
        }

        return blockingDeque.offerFirst(e, timeout, unit);
    }

    /// {@inheritDoc}
    ///
    /// Inserts the specified element at the end of this deque, waiting up to the specified wait time if necessary for
    /// space to become available. The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #OFFER_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE} - Returns false regardless of success
    /// - {@link #OFFER_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    /// - {@link #OFFER_LAST_WITH_TIMEOUT_DOES_NOT_WAIT} - Offers once, without waiting
    /// - {@link #OFFER_LAST_WITH_TIMEOUT_ADDS_TO_FRONT} - Adds at the front
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to OFFER_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break
    @Override
    public boolean offerLast(final E e, final long timeout, final TimeUnit unit)
            throws InterruptedException {
        checkMethodSupport(BlockingDequeMethods.OFFER_LAST_TIMEOUT);

        if (hasBreak(OFFER_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("OfferLast with timeout operation interrupted due to break");
        }

        if (hasBreak(OFFER_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE)) {
            return false;
        }

        if (hasBreak(OFFER_LAST_WITH_TIMEOUT_DOES_NOT_WAIT)) {
            return blockingDeque.offerLast(e);
        }

        if (hasBreak(OFFER_LAST_WITH_TIMEOUT_ADDS_TO_FRONT)) {
            return blockingDeque.offerFirst(e, timeout, unit);
        }

        return blockingDeque.offerLast(e, timeout, unit);
    }

    /// {@inheritDoc}
    ///
    /// Retrieves and removes the first element of this deque, waiting up to the specified wait time if necessary for an
    /// element to become available. The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #POLL_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL} - Returns null regardless of content
    /// - {@link #POLL_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    /// - {@link #POLL_FIRST_WITH_TIMEOUT_DOES_NOT_WAIT} - Polls once, without waiting
    /// - {@link #POLL_FIRST_WITH_TIMEOUT_REMOVES_LAST} - Polls the last element
    /// - {@link #POLL_FIRST_WITH_TIMEOUT_IS_NOT_THREAD_SAFE} - Reads and removes the first element in two steps
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to POLL_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break
    @Override
    public @Nullable E pollFirst(final long timeout, final TimeUnit unit) throws InterruptedException {
        checkMethodSupport(BlockingDequeMethods.POLL_FIRST_TIMEOUT);

        if (hasBreak(POLL_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("PollFirst with timeout operation interrupted due to break");
        }

        if (hasBreak(POLL_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL)) {
            return null;
        }

        if (hasBreak(POLL_FIRST_WITH_TIMEOUT_DOES_NOT_WAIT)) {
            return blockingDeque.pollFirst();
        }

        if (hasBreak(POLL_FIRST_WITH_TIMEOUT_REMOVES_LAST)) {
            return blockingDeque.pollLast(timeout, unit);
        }

        if (hasBreak(POLL_FIRST_WITH_TIMEOUT_IS_NOT_THREAD_SAFE)) {
            E first = blockingDeque.peekFirst();
            if (first != null) {
                blockingDeque.removeFirstOccurrence(first);
                return first;
            }
        }

        return blockingDeque.pollFirst(timeout, unit);
    }

    /// {@inheritDoc}
    ///
    /// Retrieves and removes the last element of this deque, waiting up to the specified wait time if necessary for an
    /// element to become available. The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #POLL_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL} - Returns null regardless of content
    /// - {@link #POLL_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    /// - {@link #POLL_LAST_WITH_TIMEOUT_DOES_NOT_WAIT} - Polls once, without waiting
    /// - {@link #POLL_LAST_WITH_TIMEOUT_REMOVES_FIRST} - Polls the first element
    /// - {@link #POLL_LAST_WITH_TIMEOUT_IS_NOT_THREAD_SAFE} - Reads and removes the last element in two steps
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to POLL_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break
    @Override
    public @Nullable E pollLast(final long timeout, final TimeUnit unit) throws InterruptedException {
        checkMethodSupport(BlockingDequeMethods.POLL_LAST_TIMEOUT);

        if (hasBreak(POLL_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("PollLast with timeout operation interrupted due to break");
        }

        if (hasBreak(POLL_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL)) {
            return null;
        }

        if (hasBreak(POLL_LAST_WITH_TIMEOUT_DOES_NOT_WAIT)) {
            return blockingDeque.pollLast();
        }

        if (hasBreak(POLL_LAST_WITH_TIMEOUT_REMOVES_FIRST)) {
            return blockingDeque.pollFirst(timeout, unit);
        }

        if (hasBreak(POLL_LAST_WITH_TIMEOUT_IS_NOT_THREAD_SAFE)) {
            E last = blockingDeque.peekLast();
            if (last != null) {
                blockingDeque.removeLastOccurrence(last);
                return last;
            }
        }

        return blockingDeque.pollLast(timeout, unit);
    }

    // ========== BlockingQueue Interface Methods (inherited from BlockingQueue) ==========

    /// {@inheritDoc}
    ///
    /// Inserts the specified element into this deque, waiting if necessary for space to become available.
    /// This method is equivalent to putLast(E).
    ///
    /// **Exception Handling:**
    /// - Delegates to putLast() which may throw exceptions based on configured breaks
    @Override
    public void put(final E e) throws InterruptedException {
        checkMethodSupport(BlockingQueueMethods.PUT);
        putLast(e);
    }

    /// {@inheritDoc}
    ///
    /// Retrieves and removes the head of this deque, waiting if necessary until an element becomes available.
    /// This method is equivalent to takeFirst().
    ///
    /// **Exception Handling:**
    /// - Delegates to takeFirst() which may throw exceptions based on configured breaks
    @Override
    public @Nullable E take() throws InterruptedException {
        checkMethodSupport(BlockingQueueMethods.TAKE);
        return takeFirst();
    }

    /// {@inheritDoc}
    ///
    /// Inserts the specified element into this deque, waiting up to the specified wait time if necessary for space to
    /// become available. This method is equivalent to offerLast(E, long, TimeUnit).
    ///
    /// **Exception Handling:**
    /// - Delegates to offerLast() which may throw exceptions based on configured breaks
    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit)
            throws InterruptedException {
        checkMethodSupport(BlockingQueueMethods.OFFER_TIMEOUT);
        return offerLast(e, timeout, unit);
    }

    /// {@inheritDoc}
    ///
    /// Retrieves and removes the head of this deque, waiting up to the specified wait time if necessary for an element
    /// to become available. This method is equivalent to pollFirst(long, TimeUnit).
    ///
    /// **Exception Handling:**
    /// - Delegates to pollFirst() which may throw exceptions based on configured breaks
    @Override
    public @Nullable E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        checkMethodSupport(BlockingQueueMethods.POLL_TIMEOUT);
        return pollFirst(timeout, unit);
    }

    /// {@inheritDoc}
    ///
    /// Returns the number of additional elements that this deque can ideally accept without blocking.
    ///
    /// **Supported Breaks:**
    /// - {@link #REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO} - Returns 0 regardless of actual capacity
    ///
    /// @throws UnsupportedOperationException if `remainingCapacity` is not supported
    @Override
    public int remainingCapacity() {
        checkMethodSupport(BlockingQueueMethods.REMAINING_CAPACITY);

        if (hasBreak(REMAINING_CAPACITY_ALWAYS_RETURNS_ZERO)) {
            return 0;
        }

        return blockingDeque.remainingCapacity();
    }

    /// {@inheritDoc}
    ///
    /// Removes all available elements from this deque and adds them to the given collection.
    ///
    /// **Supported Breaks:**
    /// - {@link #DRAIN_TO_ALWAYS_RETURNS_ZERO} - Returns 0 and moves nothing
    /// - {@link #DRAIN_TO_THROWS_EXCEPTION} - Throws IllegalStateException
    /// - {@link #DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS} - Returns the count but doesn't remove the elements
    /// - {@link #DRAIN_TO_IS_NOT_THREAD_SAFE} - Copies, adds and removes in separate steps
    ///
    /// Without a break, a drain into this deque throws [IllegalArgumentException], as
    /// [BlockingDeque#drainTo(Collection)] says.
    ///
    /// @throws UnsupportedOperationException if `drainTo` is not supported
    /// @throws IllegalStateException if `DRAIN_TO_THROWS_EXCEPTION` is set
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
            int size = blockingDeque.size();
            c.addAll(blockingDeque);
            return size;
        }

        return blockingDeque.drainTo(c);
    }

    /// {@inheritDoc}
    ///
    /// Removes at most the given number of available elements from this deque and adds them to the given collection.
    ///
    /// **Supported Breaks:**
    /// - {@link #DRAIN_TO_ALWAYS_RETURNS_ZERO} - Returns 0 and moves nothing
    /// - {@link #DRAIN_TO_THROWS_EXCEPTION} - Throws IllegalStateException
    /// - {@link #DRAIN_TO_DOES_NOT_REMOVE_ELEMENTS} - Returns the count but doesn't remove the elements
    /// - {@link #DRAIN_TO_IS_NOT_THREAD_SAFE} - Copies, adds and removes in separate steps
    ///
    /// Without a break, a drain into this deque throws [IllegalArgumentException], as
    /// [BlockingDeque#drainTo(Collection, int)] says.
    ///
    /// @throws UnsupportedOperationException if `drainTo(Collection, int)` is not supported
    /// @throws IllegalStateException if `DRAIN_TO_THROWS_EXCEPTION` is set
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
            List<E> copy = firstElements(maxElements);
            c.addAll(copy);
            return copy.size();
        }

        return blockingDeque.drainTo(c, maxElements);
    }

    /// Refuses a drain into this deque, as [BlockingDeque#drainTo(Collection)] says: the store would compare the
    /// collection with itself, not with this deque, and drain into it.
    // ReferenceEquality: the check is for this very deque, by identity, as the JDK's deques make it.
    @SuppressWarnings("ReferenceEquality")
    private void checkNotThis(final Collection<? super E> c) {
        if (c == this) {
            throw new IllegalArgumentException("drainTo: the collection is this deque");
        }
    }

    /// Moves at most `maxElements` elements in steps that are not atomic, for [#DRAIN_TO_IS_NOT_THREAD_SAFE]: copies
    /// them, adds the copy to `c`, then removes each from the store.
    private int drainInSteps(final Collection<? super E> c, final int maxElements) {
        List<E> copy = firstElements(maxElements);
        c.addAll(copy);
        for (final E element : copy) {
            blockingDeque.removeFirstOccurrence(element);
        }
        return copy.size();
    }

    /// Returns at most `maxElements` of the store's elements, from the first.
    private List<E> firstElements(final int maxElements) {
        List<E> copy = new ArrayList<>();
        for (final E element : blockingDeque) {
            if (copy.size() >= maxElements) {
                break;
            }
            copy.add(element);
        }
        return copy;
    }

    /// Blocks until the thread is interrupted, for the `*_ALWAYS_BLOCKS` breaks; never returns normally. It waits on
    /// a monitor that is never notified, in a loop, so that a spurious wakeup does not end the block.
    ///
    /// @throws InterruptedException when the thread is interrupted; its interrupt status is set again
    private void blockUntilInterrupted() throws InterruptedException {
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

    // ========== Builder Class ==========

    /// Abstract base builder class for creating [BreakableBlockingDeque] and its subclasses.
    ///
    /// This class provides common configuration options for blocking deques, such as capacity control.
    ///
    /// @param <B> the type of the builder subclass
    /// @param <C> the type of the blocking deque being built
    /// @param <E> the type of elements held in the blocking deque
    /// @since 1.0.0
    public abstract static class AbstractBuilder<B extends BreakableCollection.AbstractBuilder<B, C, E>,
            C extends BreakableBlockingDeque<E>, E>
            extends BreakableCollection.AbstractBuilder<B, C, E> {

        /// The capacity of the deque. If negative, the deque has no explicit capacity limit.
        private int capacity;

        /// Creates a new AbstractBuilder with default configuration.
        public AbstractBuilder() {
            this.capacity = -1;
        }

        /// Creates a new AbstractBuilder pre-populated with elements.
        ///
        /// @param elements the elements to be placed in the builder
        public AbstractBuilder(final Collection<E> elements) {
            super(elements);
            this.capacity = -1;
        }

        /// Creates a new AbstractBuilder by copying configuration from another builder.
        ///
        /// @param other the builder to copy configuration from
        public AbstractBuilder(final AbstractBuilder<B, C, E> other) {
            super(other);
            this.capacity = other.capacity;
        }

        /// Sets the capacity of the blocking deque.
        ///
        /// @param newCapacity the capacity to set; negative for no limit
        /// @return this builder for method chaining
        public B setCapacity(final int newCapacity) {
            this.capacity = newCapacity;
            return self();
        }

        /// Returns the configured capacity of the blocking deque.
        ///
        /// @return the configured capacity
        public int capacity() {
            return capacity;
        }
    }

    /// Builder class for creating BreakableBlockingDeque instances with fluent configuration.
    ///
    /// This builder extends BreakableDeque.Builder and provides additional configuration
    /// options specific to BlockingDeque functionality.
    /// @param <E> the element type for the collection.
    public static class Builder<E> extends AbstractBuilder<Builder<E>, BreakableBlockingDeque<E>, E> {

        /// Creates a new Builder with default configuration.
        ///
        /// This constructor creates a new Builder instance that will create a BreakableBlockingDeque
        /// backed by a LinkedBlockingDeque. The deque will have default configuration with no breaks
        /// applied and default null policies.
        ///
        /// **Default Configuration:**
        /// - Empty LinkedBlockingDeque
        /// - Permits null elements: true
        /// - No breaks applied
        /// - Default spliterator characteristics
        ///
        /// **Usage:**
        /// ```java
        /// BreakableBlockingDeque<String> deque = new BreakableBlockingDeque.Builder<String>()
        ///     .addBreak(PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION)
        ///     .build();
        /// ```
        ///
        /// **Thread Safety:**
        /// This constructor is not thread-safe. External synchronization is required
        /// if the builder is being accessed concurrently.
        ///
        /// @see #Builder(Builder)
        /// @see #build()
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
        /// - All elements in the blocking deque
        /// - All applied breaks
        /// - Method status configuration
        /// - Spliterator characteristics
        /// - Permit settings (null policies, etc.)
        ///
        /// **Usage:**
        /// ```java
        /// Builder<String> original = new Builder<String>()
        ///     .addBreak(PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION)
        ///     .addElements("item1");
        ///
        /// Builder<String> copy = new Builder<>(original);
        /// copy.addBreak(TAKE_LAST_ALWAYS_RETURNS_NULL); // Add additional break to copy
        ///
        /// BreakableBlockingDeque<String> deque1 = original.build();
        /// BreakableBlockingDeque<String> deque2 = copy.build(); // Has both breaks
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
        public Builder(final Builder<E> other) {
            super(other);
        }

        /// Creates a new Builder pre-populated with the specified elements.
        ///
        /// @param elements the initial elements to be placed in the blocking deque
        public Builder(final Collection<E> elements) {
            super(elements);
        }

        /// {@inheritDoc}
        @Override
        public Builder<E> self() {
            return this;
        }

        /// {@inheritDoc}
        @Override
        public Builder<E> copy() {
            return new Builder<>(this);
        }

        @Override
        public BreakableBlockingDeque<E> copy(final BreakableBlockingDeque<E> other) {
            return new BreakableBlockingDeque<>(other);
        }

        /// Builds a new [BreakableBlockingDeque] instance with the configured settings.
        ///
        /// @return a new BreakableBlockingDeque instance
        @Override
        public BreakableBlockingDeque<E> build(final Iterable<E> moreElements) {
            List<E> list = new ArrayList<>(elements());
            moreElements.forEach(list::add);

            BlockingDeque<E> queue;
            if (capacity() < 0) {
                queue = new LinkedBlockingDeque<>(list);
            } else {
                queue = new LinkedBlockingDeque<>(capacity());
                queue.addAll(list);
            }
            return new BreakableBlockingDeque<>(queue, new HashSet<>(breaks()), new HashMap<>(methodStatuses()),
                    characteristics(), permits(), isSafe(), compatibleType());
        }
    }

    // ========== Static Factory Methods ==========

    /// Creates a BreakableBlockingDeque that wraps the specified BlockingDeque with the given breaks.
    ///
    /// @param <E> the type of elements held in the blocking deque
    /// @param blockingDeque the BlockingDeque to wrap
    /// @param breaks the collection of breaks to apply
    /// @return a new BreakableBlockingDeque wrapping the specified deque with the given breaks
    /// @throws NullPointerException if blockingDeque or breaks is null
    public static <E> BreakableBlockingDeque<E> wrap(
            final BlockingDeque<E> blockingDeque,
            final Set<Break> breaks) {
        return new BreakableBlockingDeque<>(blockingDeque, breaks, DEFAULT_METHOD_STATUSES,
                DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS, DEFAULT_SAFETY, Object.class);
    }

    /// Creates a BreakableBlockingDeque that wraps the specified BlockingDeque with full configuration.
    ///
    /// @param <E> the type of elements held in the blocking deque
    /// @param blockingDeque the BlockingDeque to wrap
    /// @param breaks the collection of breaks to apply
    /// @param characteristics the spliterator characteristics for the deque
    /// @return a new BreakableBlockingDeque wrapping the specified deque with the given configuration
    /// @throws NullPointerException if blockingDeque or breaks is null
    public static <E> BreakableBlockingDeque<E> wrap(
            final BlockingDeque<E> blockingDeque,
            final Set<Break> breaks,
            final int characteristics) {
        return new BreakableBlockingDeque<>(blockingDeque, breaks, DEFAULT_METHOD_STATUSES,
                characteristics, DEFAULT_PERMITS, DEFAULT_SAFETY, Object.class);
    }
}
