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
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.contract.transferqueue.TransferQueueMethods;

/// A TransferQueue implementation that can be programmatically broken for testing purposes.
///
/// This class extends BreakableBlockingQueue and implements the TransferQueue interface, providing
/// additional break constants for testing TransferQueue-specific functionality. It wraps
/// an existing TransferQueue and allows specific behaviors to be "broken" through the
/// Break mechanism.
///
/// ## Overview
///
/// BreakableTransferQueue provides comprehensive testing capabilities for code that works with
/// TransferQueue implementations. It supports all standard TransferQueue operations while enabling
/// controlled behavioral modifications through break constants. This is particularly useful for:
///
/// - **Testing TransferQueue Contract Compliance**: Verifying that code correctly handles direct transfer operations
/// - **Producer-Consumer Testing**: Simulating transfer scenarios with waiting consumers
/// - **Concurrency Testing**: Testing blocking transfers, timeouts, and consumer detection
/// - **Error Condition Simulation**: Testing how code responds to transfer failures and consumer state changes
/// - **Performance Testing**: Simulating slow or failing transfer operations
/// - **Robustness Testing**: Verifying code resilience against unexpected TransferQueue behaviors
///
/// ## TransferQueue-Specific Break Constants
///
/// This class provides break constants for all TransferQueue-specific methods:
///
/// ### Transfer Operations
/// - **TRANSFER_ALWAYS_BLOCKS**: Forces transfer() to block indefinitely
/// - **TRANSFER_THROWS_INTERRUPTED_EXCEPTION**: Forces transfer() to throw InterruptedException
/// - **TRANSFER_DOES_NOT_ADD_ELEMENT**: Forces transfer() to accept but not add elements
/// - **TRANSFER_THROWS_EXCEPTION**: Forces transfer() to throw exceptions instead of normal operation
///
/// ### Try Transfer Operations
/// - **TRY_TRANSFER_ALWAYS_RETURNS_FALSE**: Control tryTransfer() to always return false
/// - **TRY_TRANSFER_THROWS_EXCEPTION**: Forces tryTransfer() to throw exceptions
/// - **TRY_TRANSFER_DOES_NOT_ADD_ELEMENT**: Forces tryTransfer() to accept but not add elements
///
/// ### Try Transfer with Timeout Operations
/// - **TRY_TRANSFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE**: Control tryTransfer(timeout) to always return false
/// - **TRY_TRANSFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION**: Forces tryTransfer(timeout) to throw
///   InterruptedException
/// - **TRY_TRANSFER_WITH_TIMEOUT_DOES_NOT_ADD_ELEMENT**: Forces tryTransfer(timeout) to accept but not add elements
///
/// ### Consumer Detection Operations
/// - **HAS_WAITING_CONSUMER_ALWAYS_RETURNS_FALSE**: Forces hasWaitingConsumer() to always return false
/// - **HAS_WAITING_CONSUMER_ALWAYS_RETURNS_TRUE**: Forces hasWaitingConsumer() to always return true
/// - **GET_WAITING_CONSUMER_COUNT_ALWAYS_RETURNS_ZERO**: Forces getWaitingConsumerCount() to always return 0
/// - **GET_WAITING_CONSUMER_COUNT_RETURNS_RANDOM**: Forces getWaitingConsumerCount() to return arbitrary values
///
/// ### Breaks the TransferQueue contract checks (0.1.2)
/// - **TRANSFER_DOES_NOT_WAIT**, **TRANSFER_DOES_NOT_WAIT_FOR_SPACE**, **TRY_TRANSFER_WITH_TIMEOUT_DOES_NOT_WAIT**:
///   a call that should wait, for a consumer or for room, does not
/// - **TRANSFER_ENQUEUES_ELEMENT_WHEN_INTERRUPTED**, **TRY_TRANSFER_ENQUEUES_ELEMENT**,
///   **TRY_TRANSFER_WITH_TIMEOUT_ENQUEUES_ELEMENT**: an element that was not transferred is left enqueued
/// - **TRANSFER_IS_NOT_THREAD_SAFE**, **TRY_TRANSFER_IS_NOT_THREAD_SAFE**: check-then-act on the store
///
/// ## Capacity and fairness
///
/// Built with a capacity or fairness ([Builder#build(Iterable)]), the queue's store is a bounded `TransferQueue` of
/// this package's own, on one `ReentrantLock`, fair when asked; otherwise it is a `LinkedTransferQueue`. The JDK has
/// no bounded `TransferQueue`.
///
/// ## Usage Examples
///
/// ### Basic TransferQueue Testing
/// ```java
/// BreakableTransferQueue<String> queue = new BreakableTransferQueue<>();
/// queue.transfer("message"); // Direct transfer to waiting consumer
///
/// // Check for waiting consumers
/// if (queue.hasWaitingConsumer()) {
///     queue.tryTransfer("priority-message");
/// }
/// ```
///
/// ### TransferQueue Failure Testing
/// ```java
/// BreakableTransferQueue<String> brokenQueue = new BreakableTransferQueue.Builder<String>()
///     .addBreak(TRANSFER_ALWAYS_BLOCKS)
///     .addBreak(TRY_TRANSFER_ALWAYS_RETURNS_FALSE)
///     .build();
///
/// // Test blocking behavior
/// // transfer() will block indefinitely due to break
/// // tryTransfer() will return false due to break
/// ```
///
/// ### Builder Pattern with TransferQueue
/// ```java
/// BreakableTransferQueue<Integer> queue = new BreakableTransferQueue.Builder<Integer>()
///     .addBreak(HAS_WAITING_CONSUMER_ALWAYS_RETURNS_TRUE)
///     .addBreak(GET_WAITING_CONSUMER_COUNT_ALWAYS_RETURNS_ZERO)
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
///     ↓
/// BreakableTransferQueue<E>
/// ```
///
/// This class inherits all Collection, Queue, BlockingQueue, and Iterable functionality from its parent classes
/// while adding TransferQueue-specific capabilities. All inherited break constants and behaviors remain available.
///
/// ## Thread Safety
///
/// Like its parent classes, BreakableTransferQueue is not thread-safe in terms of break configuration.
/// The underlying TransferQueue implementation provides the thread safety for normal operations,
/// but break checking adds additional complexity that requires external synchronization for
/// concurrent break modifications.
///
/// ## Performance Considerations
///
/// TransferQueue operations delegate to the underlying TransferQueue implementation unless breaks
/// are applied. Break checking adds minimal overhead. The class maintains the performance
/// characteristics of the underlying TransferQueue for non-broken operations.
///
/// @param <E> the type of elements held in this transfer queue
/// @author evanbergstrom
/// @since 1.0
/// @see BreakableBlockingQueue
/// @see TransferQueue
/// @see Break
public class BreakableTransferQueue<E> extends BreakableBlockingQueue<E> implements TransferQueue<E>, Serializable {

    // ========== Transfer Method Breaks ==========

    /// Break constant that causes transfer() to block indefinitely.
    ///
    /// When this break is applied, the transfer() method will block the calling thread
    /// indefinitely without transferring the element to any consumer.
    ///
    /// **Affected Methods:**
    /// - {@link #transfer(Object)} - Blocks indefinitely
    ///
    /// **Use Cases:**
    /// - Testing thread interruption handling during transfers
    /// - Simulating scenarios with no available consumers
    /// - Testing deadlock detection in transfer operations
    public static final Break TRANSFER_ALWAYS_BLOCKS =
            new Break("TRANSFER_ALWAYS_BLOCKS");

    /// Break constant that causes transfer() to throw InterruptedException.
    ///
    /// When this break is applied, the transfer() method will immediately throw
    /// InterruptedException instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #transfer(Object)} - Throws InterruptedException
    ///
    /// **Use Cases:**
    /// - Testing interruption handling in transfer operations
    /// - Simulating thread interruption scenarios
    /// - Testing cleanup code in exception handlers
    public static final Break TRANSFER_THROWS_INTERRUPTED_EXCEPTION =
            new Break("TRANSFER_THROWS_INTERRUPTED_EXCEPTION");

    /// Break constant that causes transfer() to accept elements but not actually transfer them.
    ///
    /// When this break is applied, the transfer() method will return normally but
    /// the element will not be transferred to any consumer or added to the queue.
    ///
    /// **Affected Methods:**
    /// - {@link #transfer(Object)} - Returns normally but doesn't transfer element
    ///
    /// **Use Cases:**
    /// - Testing silent transfer operation failures
    /// - Simulating consumer communication failures
    /// - Testing transfer state consistency
    public static final Break TRANSFER_DOES_NOT_ADD_ELEMENT =
            new Break("TRANSFER_DOES_NOT_ADD_ELEMENT");

    /// Break constant that causes transfer() to throw an exception.
    ///
    /// When this break is applied, the transfer() method will throw an IllegalStateException
    /// instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #transfer(Object)} - Throws IllegalStateException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in transfer operations
    /// - Simulating transfer operation failures
    /// - Testing robustness against unexpected exceptions
    public static final Break TRANSFER_THROWS_EXCEPTION =
            new Break("TRANSFER_THROWS_EXCEPTION");

    // ========== Try Transfer Method Breaks ==========

    /// Break constant that causes tryTransfer() to always return false.
    ///
    /// When this break is applied, the tryTransfer() method will return false
    /// regardless of whether there are waiting consumers or if the element was actually transferred.
    ///
    /// **Affected Methods:**
    /// - {@link #tryTransfer(Object)} - Returns false
    ///
    /// **Use Cases:**
    /// - Testing non-blocking transfer failure handling
    /// - Simulating scenarios with no waiting consumers
    /// - Testing tryTransfer() return value handling
    public static final Break TRY_TRANSFER_ALWAYS_RETURNS_FALSE =
            new Break("TRY_TRANSFER_ALWAYS_RETURNS_FALSE");

    /// Break constant that causes tryTransfer() to throw an exception.
    ///
    /// When this break is applied, the tryTransfer() method will throw an IllegalStateException
    /// instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #tryTransfer(Object)} - Throws IllegalStateException
    ///
    /// **Use Cases:**
    /// - Testing exception handling in non-blocking transfer operations
    /// - Simulating tryTransfer operation failures
    /// - Testing robustness against unexpected exceptions
    public static final Break TRY_TRANSFER_THROWS_EXCEPTION =
            new Break("TRY_TRANSFER_THROWS_EXCEPTION");

    /// Break constant that causes tryTransfer() to accept elements but not actually transfer them.
    ///
    /// When this break is applied, the tryTransfer() method will return normally but
    /// the element will not be transferred to any consumer.
    ///
    /// **Affected Methods:**
    /// - {@link #tryTransfer(Object)} - Returns normally but doesn't transfer element
    ///
    /// **Use Cases:**
    /// - Testing silent tryTransfer operation failures
    /// - Simulating consumer communication failures
    /// - Testing tryTransfer state consistency
    public static final Break TRY_TRANSFER_DOES_NOT_ADD_ELEMENT =
            new Break("TRY_TRANSFER_DOES_NOT_ADD_ELEMENT");

    // ========== Try Transfer with Timeout Method Breaks ==========

    /// Break constant that causes tryTransfer(timeout) to always return false.
    ///
    /// When this break is applied, the tryTransfer() method with timeout will return false
    /// regardless of timeout value or whether there are waiting consumers.
    ///
    /// **Affected Methods:**
    /// - {@link #tryTransfer(Object, long, TimeUnit)} - Returns false
    ///
    /// **Use Cases:**
    /// - Testing timeout transfer failure handling
    /// - Simulating scenarios with no waiting consumers during timeout
    /// - Testing tryTransfer() timeout return value handling
    public static final Break TRY_TRANSFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE =
            new Break("TRY_TRANSFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE");

    /// Break constant that causes tryTransfer(timeout) to throw InterruptedException.
    ///
    /// When this break is applied, the tryTransfer() method with timeout will throw
    /// InterruptedException instead of performing the normal operation.
    ///
    /// **Affected Methods:**
    /// - {@link #tryTransfer(Object, long, TimeUnit)} - Throws InterruptedException
    ///
    /// **Use Cases:**
    /// - Testing interruption handling in timeout transfer operations
    /// - Simulating thread interruption scenarios
    /// - Testing cleanup code in exception handlers
    public static final Break TRY_TRANSFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION =
            new Break("TRY_TRANSFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION");

    /// Break constant that causes tryTransfer(timeout) to accept elements but not actually transfer them.
    ///
    /// When this break is applied, the tryTransfer() method with timeout will return normally but
    /// the element will not be transferred to any consumer.
    ///
    /// **Affected Methods:**
    /// - {@link #tryTransfer(Object, long, TimeUnit)} - Returns normally but doesn't transfer element
    ///
    /// **Use Cases:**
    /// - Testing silent timeout transfer operation failures
    /// - Simulating consumer communication failures
    /// - Testing timeout transfer state consistency
    public static final Break TRY_TRANSFER_WITH_TIMEOUT_DOES_NOT_ADD_ELEMENT =
            new Break("TRY_TRANSFER_WITH_TIMEOUT_DOES_NOT_ADD_ELEMENT");

    // ========== Consumer Detection Method Breaks ==========

    /// Break constant that causes hasWaitingConsumer() to always return false.
    ///
    /// When this break is applied, the hasWaitingConsumer() method will return false
    /// regardless of whether there are actually waiting consumers.
    ///
    /// **Affected Methods:**
    /// - {@link #hasWaitingConsumer()} - Returns false
    ///
    /// **Use Cases:**
    /// - Testing consumer detection logic
    /// - Simulating scenarios with no waiting consumers
    /// - Testing consumer-based decision making code
    public static final Break HAS_WAITING_CONSUMER_ALWAYS_RETURNS_FALSE =
            new Break("HAS_WAITING_CONSUMER_ALWAYS_RETURNS_FALSE");

    /// Break constant that causes hasWaitingConsumer() to always return true.
    ///
    /// When this break is applied, the hasWaitingConsumer() method will return true
    /// regardless of whether there are actually waiting consumers.
    ///
    /// **Affected Methods:**
    /// - {@link #hasWaitingConsumer()} - Returns true
    ///
    /// **Use Cases:**
    /// - Testing consumer detection logic
    /// - Simulating scenarios with waiting consumers
    /// - Testing optimistic transfer logic
    public static final Break HAS_WAITING_CONSUMER_ALWAYS_RETURNS_TRUE =
            new Break("HAS_WAITING_CONSUMER_ALWAYS_RETURNS_TRUE");

    /// Break constant that causes getWaitingConsumerCount() to always return zero.
    ///
    /// When this break is applied, the getWaitingConsumerCount() method will return 0
    /// regardless of the actual number of waiting consumers.
    ///
    /// **Affected Methods:**
    /// - {@link #getWaitingConsumerCount()} - Returns 0
    ///
    /// **Use Cases:**
    /// - Testing consumer count-based logic
    /// - Simulating scenarios with no waiting consumers
    /// - Testing consumer counting code
    public static final Break GET_WAITING_CONSUMER_COUNT_ALWAYS_RETURNS_ZERO =
            new Break("GET_WAITING_CONSUMER_COUNT_ALWAYS_RETURNS_ZERO");

    /// Break constant that causes getWaitingConsumerCount() to return arbitrary values.
    ///
    /// When this break is applied, the getWaitingConsumerCount() method will return
    /// a value that is not the count of waiting consumers: the count, plus one, plus an arbitrary value from 0 to 999.
    ///
    /// **Affected Methods:**
    /// - {@link #getWaitingConsumerCount()} - Returns a value that is not the count
    ///
    /// **Use Cases:**
    /// - Testing robustness against incorrect consumer counts
    /// - Simulating corrupted consumer tracking
    /// - Testing consumer count validation logic
    public static final Break GET_WAITING_CONSUMER_COUNT_RETURNS_RANDOM =
            new Break("GET_WAITING_CONSUMER_COUNT_RETURNS_RANDOM");

    // ========== Breaks the TransferQueue contract checks (0.1.2) ==========

    /// Break constant that makes `transfer` return without waiting for a consumer.
    ///
    /// **Purpose:** simulates a `transfer` that acts as `put`, though it "waits until the element is received by a
    /// consumer" ([TransferQueue#transfer]).
    ///
    /// **Effect:** `transfer(e)` offers the element to the store and returns at once.
    ///
    /// **Affected Methods:**
    /// - {@link #transfer(Object)} - Does not wait for reception
    ///
    /// @since 0.1.2
    public static final Break TRANSFER_DOES_NOT_WAIT =
            new Break("TRANSFER_DOES_NOT_WAIT");

    /// Break constant that makes `transfer` on a full queue return at once, without adding the element.
    ///
    /// **Purpose:** simulates a `transfer` that does not wait for room on a bounded queue, though "an attempted
    /// transfer operation may initially block waiting for available space" ([TransferQueue]).
    ///
    /// **Effect:** when the store has no room left (`remainingCapacity() == 0`), `transfer(e)` returns without adding
    /// or transferring the element. With room, it transfers as normal.
    ///
    /// **Affected Methods:**
    /// - {@link #transfer(Object)} - Does not wait for room
    ///
    /// @since 0.1.2
    public static final Break TRANSFER_DOES_NOT_WAIT_FOR_SPACE =
            new Break("TRANSFER_DOES_NOT_WAIT_FOR_SPACE");

    /// Break constant that makes an interrupted `transfer` leave its element enqueued.
    ///
    /// **Purpose:** `transfer` throws `InterruptedException` "if interrupted while waiting, in which case the element
    /// is not left enqueued" ([TransferQueue#transfer]); this break leaves it enqueued.
    ///
    /// **Effect:** when the store's `transfer` throws `InterruptedException`, the element is offered to the store
    /// before the exception is thrown again.
    ///
    /// **Affected Methods:**
    /// - {@link #transfer(Object)} - Leaves the element enqueued when interrupted
    ///
    /// @since 0.1.2
    public static final Break TRANSFER_ENQUEUES_ELEMENT_WHEN_INTERRUPTED =
            new Break("TRANSFER_ENQUEUES_ELEMENT_WHEN_INTERRUPTED");

    /// Break constant that makes `tryTransfer` enqueue an element no consumer was waiting for.
    ///
    /// **Purpose:** `tryTransfer` returns `false` "without enqueuing the element" when no consumer is waiting
    /// ([TransferQueue#tryTransfer(Object)]); this break enqueues it.
    ///
    /// **Effect:** when the store's `tryTransfer(e)` returns `false`, the element is offered to the store, and
    /// `false` is returned.
    ///
    /// **Affected Methods:**
    /// - {@link #tryTransfer(Object)} - Enqueues the element it did not transfer
    ///
    /// @since 0.1.2
    public static final Break TRY_TRANSFER_ENQUEUES_ELEMENT =
            new Break("TRY_TRANSFER_ENQUEUES_ELEMENT");

    /// Break constant that makes the timed `tryTransfer` try once, without waiting.
    ///
    /// **Purpose:** the timed `tryTransfer` "waits until the element is received by a consumer, returning false if
    /// the specified wait time elapses" ([TransferQueue#tryTransfer(Object, long, TimeUnit)]); this break does not
    /// wait.
    ///
    /// **Effect:** `tryTransfer(e, timeout, unit)` returns the store's untimed `tryTransfer(e)`.
    ///
    /// **Affected Methods:**
    /// - {@link #tryTransfer(Object, long, TimeUnit)} - Does not wait
    ///
    /// @since 0.1.2
    public static final Break TRY_TRANSFER_WITH_TIMEOUT_DOES_NOT_WAIT =
            new Break("TRY_TRANSFER_WITH_TIMEOUT_DOES_NOT_WAIT");

    /// Break constant that makes the timed `tryTransfer` leave its element enqueued when it gives up.
    ///
    /// **Purpose:** when the timed `tryTransfer` times out or is interrupted, the element "is not left enqueued"
    /// ([TransferQueue#tryTransfer(Object, long, TimeUnit)]); this break leaves it enqueued.
    ///
    /// **Effect:** when the store's timed `tryTransfer` returns `false` or throws `InterruptedException`, the element
    /// is offered to the store before `false` is returned or the exception thrown again.
    ///
    /// **Affected Methods:**
    /// - {@link #tryTransfer(Object, long, TimeUnit)} - Leaves the element enqueued when it gives up
    ///
    /// @since 0.1.2
    public static final Break TRY_TRANSFER_WITH_TIMEOUT_ENQUEUES_ELEMENT =
            new Break("TRY_TRANSFER_WITH_TIMEOUT_ENQUEUES_ELEMENT");

    /// Break constant that makes `transfer` not atomic.
    ///
    /// **Purpose:** simulates a `transfer` that is not thread safe, though a `BlockingQueue`'s "queuing methods
    /// achieve their effects atomically" ([java.util.concurrent.BlockingQueue]).
    ///
    /// **Effect:** check-then-act on the store: if the store `hasWaitingConsumer()`, `transfer(e)` offers the element
    /// and returns; otherwise it calls the store's `transfer(e)`. Two producers that see one waiting consumer both
    /// offer, and one element is left enqueued after its `transfer` returned.
    ///
    /// **Affected Methods:**
    /// - {@link #transfer(Object)} - Not atomic
    ///
    /// @since 0.1.2
    public static final Break TRANSFER_IS_NOT_THREAD_SAFE =
            new Break("TRANSFER_IS_NOT_THREAD_SAFE");

    /// Break constant that makes `tryTransfer` not atomic.
    ///
    /// **Purpose:** simulates a `tryTransfer` that is not thread safe, though a `BlockingQueue`'s "queuing methods
    /// achieve their effects atomically" ([java.util.concurrent.BlockingQueue]).
    ///
    /// **Effect:** check-then-act on the store: if the store `hasWaitingConsumer()`, `tryTransfer(e)` offers the
    /// element and returns `true`; otherwise it returns the store's `tryTransfer(e)`. Two producers that see one
    /// waiting consumer both return `true`, and one element is left enqueued.
    ///
    /// **Affected Methods:**
    /// - {@link #tryTransfer(Object)} - Not atomic
    ///
    /// @since 0.1.2
    public static final Break TRY_TRANSFER_IS_NOT_THREAD_SAFE =
            new Break("TRY_TRANSFER_IS_NOT_THREAD_SAFE");

    private static final int PSEUDO_RANDOMIZER_CONSTANT = 1000;

    @Serial
    private static final long serialVersionUID = 1L;

    // ========== Instance Fields ==========

    /// The underlying TransferQueue that this BreakableTransferQueue wraps.
    /// All operations delegate to this queue unless breaks are applied.
    private final TransferQueue<E> transferQueue;

    // ========== Constructors ==========

    /// Creates an empty BreakableTransferQueue backed by a LinkedTransferQueue.
    ///
    /// This constructor creates a new BreakableTransferQueue backed by an empty LinkedTransferQueue.
    /// The queue will have default null policies and no breaks applied.
    ///
    /// **Default Configuration:**
    /// - Empty TransferQueue (LinkedTransferQueue implementation)
    /// - Permits null elements: true
    /// - No breaks applied
    ///
    /// **Usage:**
    /// ```java
    /// BreakableTransferQueue<String> queue = new BreakableTransferQueue<>();
    /// queue.transfer("message");
    /// queue.tryTransfer("urgent");
    /// ```
    public BreakableTransferQueue() {
        this(new LinkedTransferQueue<>(), new HashSet<>(), new HashMap<>(),
                DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS, DEFAULT_SAFETY, Object.class);
    }

    /// Creates a BreakableTransferQueue by copying another BreakableTransferQueue.
    ///
    /// This constructor creates a new instance with a copy of the source queue's elements, in a store of
    /// the same kind (a bounded store keeps its capacity and fairness), and all of its configuration.
    ///
    /// **Inherited Configuration:**
    /// - All break settings from source
    /// - Null element policies
    /// - Method support configuration
    /// - The elements, copied into a new store: the copy shares no state with the source
    ///
    /// **Usage:**
    /// ```java
    /// BreakableTransferQueue<String> original = new BreakableTransferQueue<>();
    /// BreakableTransferQueue<String> copy = new BreakableTransferQueue<>(original);
    /// ```
    ///
    /// @param other the BreakableTransferQueue to copy configuration from
    /// @throws NullPointerException if other is null
    /// @since 1.0.0
    public BreakableTransferQueue(final BreakableTransferQueue<E> other) {
        this(copyOf(other.transferQueue), new HashSet<>(other.breaks()),
                new HashMap<>(other.methodStatuses()), other.characteristics(), other.permits(),
                other.isSafe(), other.compatibleType());
    }

    /// Creates a BreakableTransferQueue with the specified configuration.
    ///
    /// This constructor allows full control over the BreakableTransferQueue configuration,
    /// including the underlying TransferQueue, breaks, and spliterator characteristics.
    ///
    /// **Usage:**
    /// ```java
    /// LinkedTransferQueue<String> linkedQueue = new LinkedTransferQueue<>();
    /// Set<Break> breaks = Set.of(TRANSFER_ALWAYS_BLOCKS);
    /// BreakableTransferQueue<String> queue = new BreakableTransferQueue<>(
    ///     linkedQueue, breaks, new HashMap<>(), 0, DEFAULT_PERMITS, true, Object.class);
    /// ```
    ///
    /// @param transferQueueArg   the TransferQueue to wrap
    /// @param breaks          the breaks to apply
    /// @param methodStatuses  the method support status for each optional method
    /// @param characteristics the spliterator characteristics
    /// @param permits         the flags that indicate what types of values are supported by the collection.
    /// @param isSafe          indicates whether the collection is safe for concurrent use
    /// @param compatibleType  the type that is compatible with this collection
    /// @throws NullPointerException if any of the mandatory parameters are null
    /// @since 1.0.0
    protected BreakableTransferQueue(
            final TransferQueue<E> transferQueueArg,
            final Set<Break> breaks,
            final Map<InterfaceMethod, MethodStatus> methodStatuses,
            final int characteristics,
            final int permits,
            final boolean isSafe,
            final Class<?> compatibleType) {
        super(transferQueueArg, breaks, methodStatuses, characteristics, permits, isSafe,
                compatibleType);
        this.transferQueue = transferQueueArg;
    }

    // ========== TransferQueue Interface Implementation ==========

    /// {@inheritDoc}
    ///
    /// Transfers the element to a waiting consumer immediately, if possible.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #TRANSFER_ALWAYS_BLOCKS} - Blocks indefinitely without transferring element
    /// - {@link #TRANSFER_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    /// - {@link #TRANSFER_DOES_NOT_ADD_ELEMENT} - Returns normally but doesn't transfer element
    /// - {@link #TRANSFER_THROWS_EXCEPTION} - Throws IllegalStateException
    /// - {@link #TRANSFER_DOES_NOT_WAIT} - Offers the element and returns without waiting
    /// - {@link #TRANSFER_DOES_NOT_WAIT_FOR_SPACE} - On a full queue, returns without adding the element
    /// - {@link #TRANSFER_IS_NOT_THREAD_SAFE} - Checks for a waiting consumer, then offers, in two steps
    /// - {@link #TRANSFER_ENQUEUES_ELEMENT_WHEN_INTERRUPTED} - Interrupted, leaves the element enqueued
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to TRANSFER_THROWS_INTERRUPTED_EXCEPTION break
    /// - May throw IllegalStateException due to TRANSFER_THROWS_EXCEPTION break
    @Override
    public void transfer(final E e) throws InterruptedException {
        checkMethodSupport(TransferQueueMethods.TRANSFER);

        if (hasBreak(TRANSFER_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("Transfer operation interrupted due to break");
        }

        if (hasBreak(TRANSFER_THROWS_EXCEPTION)) {
            throw new IllegalStateException("Transfer operation failed due to break");
        }

        if (hasBreak(TRANSFER_DOES_NOT_ADD_ELEMENT)) {
            return; // Return without transferring the element
        }

        if (hasBreak(TRANSFER_ALWAYS_BLOCKS)) {
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

        if (hasBreak(TRANSFER_DOES_NOT_WAIT)) {
            transferQueue.offer(e);
            return;
        }

        if (hasBreak(TRANSFER_DOES_NOT_WAIT_FOR_SPACE) && transferQueue.remainingCapacity() == 0) {
            return;
        }

        if (hasBreak(TRANSFER_IS_NOT_THREAD_SAFE) && transferQueue.hasWaitingConsumer()) {
            transferQueue.offer(e);
            return;
        }

        try {
            transferQueue.transfer(e);
        } catch (final InterruptedException ie) {
            if (hasBreak(TRANSFER_ENQUEUES_ELEMENT_WHEN_INTERRUPTED)) {
                transferQueue.offer(e);
            }
            throw ie;
        }
    }

    /// {@inheritDoc}
    ///
    /// Transfers the element to a waiting consumer immediately, if possible, returning false if not.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #TRY_TRANSFER_ALWAYS_RETURNS_FALSE} - Returns false regardless of transfer success
    /// - {@link #TRY_TRANSFER_THROWS_EXCEPTION} - Throws IllegalStateException
    /// - {@link #TRY_TRANSFER_DOES_NOT_ADD_ELEMENT} - Returns normally but doesn't transfer element
    /// - {@link #TRY_TRANSFER_IS_NOT_THREAD_SAFE} - Checks for a waiting consumer, then offers, in two steps
    /// - {@link #TRY_TRANSFER_ENQUEUES_ELEMENT} - Enqueues the element it did not transfer
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw IllegalStateException due to TRY_TRANSFER_THROWS_EXCEPTION break
    @Override
    public boolean tryTransfer(final E e) {
        checkMethodSupport(TransferQueueMethods.TRY_TRANSFER);

        if (hasBreak(TRY_TRANSFER_THROWS_EXCEPTION)) {
            throw new IllegalStateException("TryTransfer operation failed due to break");
        }

        if (hasBreak(TRY_TRANSFER_DOES_NOT_ADD_ELEMENT)) {
            return true; // Return true but don't actually transfer the element
        }

        // Perform the transfer first
        boolean result;
        if (hasBreak(TRY_TRANSFER_IS_NOT_THREAD_SAFE) && transferQueue.hasWaitingConsumer()) {
            result = transferQueue.offer(e);
        } else {
            result = transferQueue.tryTransfer(e);
        }

        if (!result && hasBreak(TRY_TRANSFER_ENQUEUES_ELEMENT)) {
            transferQueue.offer(e);
        }

        if (hasBreak(TRY_TRANSFER_ALWAYS_RETURNS_FALSE)) {
            return false;
        }

        return result;
    }

    /// {@inheritDoc}
    ///
    /// Transfers the element to a waiting consumer, waiting up to the specified wait time if necessary.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #TRY_TRANSFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE} - Returns false regardless of timeout
    /// - {@link #TRY_TRANSFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    /// - {@link #TRY_TRANSFER_WITH_TIMEOUT_DOES_NOT_ADD_ELEMENT} - Returns normally but doesn't transfer element
    /// - {@link #TRY_TRANSFER_WITH_TIMEOUT_DOES_NOT_WAIT} - Tries once, without waiting
    /// - {@link #TRY_TRANSFER_WITH_TIMEOUT_ENQUEUES_ELEMENT} - Leaves the element enqueued when it gives up
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to TRY_TRANSFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break
    @Override
    public boolean tryTransfer(final E e, final long timeout, final TimeUnit unit)
            throws InterruptedException {
        checkMethodSupport(TransferQueueMethods.TRY_TRANSFER_TIMEOUT);

        if (hasBreak(TRY_TRANSFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("TryTransfer with timeout operation interrupted due to break");
        }

        if (hasBreak(TRY_TRANSFER_WITH_TIMEOUT_DOES_NOT_ADD_ELEMENT)) {
            return true; // Return true but don't actually transfer the element
        }

        // Perform the transfer first
        boolean result;
        if (hasBreak(TRY_TRANSFER_WITH_TIMEOUT_DOES_NOT_WAIT)) {
            result = transferQueue.tryTransfer(e);
        } else {
            try {
                result = transferQueue.tryTransfer(e, timeout, unit);
            } catch (final InterruptedException ie) {
                if (hasBreak(TRY_TRANSFER_WITH_TIMEOUT_ENQUEUES_ELEMENT)) {
                    transferQueue.offer(e);
                }
                throw ie;
            }
        }

        if (!result && hasBreak(TRY_TRANSFER_WITH_TIMEOUT_ENQUEUES_ELEMENT)) {
            transferQueue.offer(e);
        }

        if (hasBreak(TRY_TRANSFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE)) {
            return false;
        }

        return result;
    }

    /// {@inheritDoc}
    ///
    /// Returns true if there is at least one consumer waiting to receive an element via
    /// take or a timed poll.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #HAS_WAITING_CONSUMER_ALWAYS_RETURNS_FALSE} - Returns false regardless of actual state
    /// - {@link #HAS_WAITING_CONSUMER_ALWAYS_RETURNS_TRUE} - Returns true regardless of actual state
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    @Override
    public boolean hasWaitingConsumer() {
        checkMethodSupport(TransferQueueMethods.HAS_WAITING_CONSUMER);

        if (hasBreak(HAS_WAITING_CONSUMER_ALWAYS_RETURNS_FALSE)) {
            return false;
        }

        if (hasBreak(HAS_WAITING_CONSUMER_ALWAYS_RETURNS_TRUE)) {
            return true;
        }

        return transferQueue.hasWaitingConsumer();
    }

    /// {@inheritDoc}
    ///
    /// Returns an estimate of the number of consumers waiting to receive elements via
    /// take or a timed poll.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #GET_WAITING_CONSUMER_COUNT_ALWAYS_RETURNS_ZERO} - Returns 0 regardless of actual count
    /// - {@link #GET_WAITING_CONSUMER_COUNT_RETURNS_RANDOM} - Returns arbitrary value
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    @Override
    public int getWaitingConsumerCount() {
        checkMethodSupport(TransferQueueMethods.GET_WAITING_CONSUMER_COUNT);

        if (hasBreak(GET_WAITING_CONSUMER_COUNT_ALWAYS_RETURNS_ZERO)) {
            return 0;
        }

        if (hasBreak(GET_WAITING_CONSUMER_COUNT_RETURNS_RANDOM)) {
            // Never the count: a deterministic "random" value above it.
            return transferQueue.getWaitingConsumerCount() + 1 + Math.floorMod(hashCode(), PSEUDO_RANDOMIZER_CONSTANT);
        }

        return transferQueue.getWaitingConsumerCount();
    }

    /// A copy of a store: a bounded store keeps its capacity and fairness.
    private static <E> TransferQueue<E> copyOf(final TransferQueue<E> store) {
        if (store instanceof BoundedTransferQueue<E> bounded) {
            return bounded.copy();
        }
        return new LinkedTransferQueue<>(store);
    }

    // ========== Builder Class ==========

    /// Builder class for creating BreakableTransferQueue instances with fluent configuration.
    ///
    /// This builder extends BreakableBlockingQueue.Builder and provides additional configuration
    /// options specific to TransferQueue functionality.
    ///
    /// @param <E> the type of elements held in the queue to be built
    /// @author evanbergstrom
    /// @since 1.0.0
    public static class Builder<E> extends AbstractBuilder<Builder<E>, BreakableTransferQueue<E>, E> {

        /// Creates a new Builder with default configuration.
        ///
        /// The builder will be initialized with an empty element collection and no breaks.
        public Builder() {
            super();
        }

        /// Creates a new Builder by copying configuration from another builder.
        ///
        /// @param other the builder to copy configuration from
        /// @throws NullPointerException if other is null
        /// @since 1.0.0
        public Builder(final Builder<E> other) {
            super(other);
        }

        /// Creates a new Builder pre-populated with elements.
        ///
        /// @param elements the elements to be placed in the builder
        /// @throws NullPointerException if elements is null
        /// @since 1.0.0
        public Builder(final Collection<E> elements) {
            super(elements);
        }

        /// Returns this builder instance for method chaining.
        ///
        /// @return this builder
        @Override
        public Builder<E> self() {
            return this;
        }

        /// Creates a deep copy of this builder.
        ///
        /// @return a new Builder instance with identical configuration
        @Override
        public Builder<E> copy() {
            return new Builder<>(this);
        }

        @Override
        public BreakableTransferQueue<E> copy(final BreakableTransferQueue<E> other) {
            return new BreakableTransferQueue<>(other);
        }

        /// Builds a new BreakableTransferQueue instance with the configured settings.
        ///
        /// The store is a `LinkedTransferQueue`, unbounded and not fair, unless a capacity or fairness is set:
        /// - **A capacity** ([#setCapacity(int)], 1 or more) gives a bounded store of that capacity. A negative
        ///   capacity, the default, means no capacity, as for [BreakableBlockingQueue]; a capacity of 0 is refused,
        ///   since a `TransferQueue` with no room would be a `SynchronousQueue`.
        /// - **Fairness** ([#setFair(boolean)] with `true`) gives a store whose lock is fair, unbounded unless a
        ///   capacity is set too. The builder cannot tell `setFair(false)` from no call; both mean not fair.
        ///
        /// The JDK has no bounded `TransferQueue`, so the bounded and fair store is this package's own, on one
        /// `ReentrantLock`.
        ///
        /// @return a new BreakableTransferQueue
        /// @throws IllegalArgumentException if the capacity is 0, or there are more elements than the capacity
        @Override
        public BreakableTransferQueue<E> build(final Iterable<E> elements) {
            List<E> all = new ArrayList<>(elements());
            elements.forEach(all::add);
            TransferQueue<E> storage;
            if (capacity() >= 0 || fair()) {
                storage = new BoundedTransferQueue<>(capacity() >= 0 ? capacity() : Integer.MAX_VALUE, fair(), all);
            } else {
                storage = new LinkedTransferQueue<>(all);
            }
            return new BreakableTransferQueue<>(storage, new HashSet<>(breaks()), new HashMap<>(methodStatuses()),
                    characteristics(), permits(), isSafe(), compatibleType());
        }
    }

    // ========== Static Factory Methods ==========

    /// Creates a BreakableTransferQueue that wraps the specified TransferQueue with the given breaks.
    ///
    /// @param <E>           the type of elements held in the transfer queue
    /// @param transferQueue the TransferQueue to wrap
    /// @param breaks        the set of breaks to apply
    /// @return a new BreakableTransferQueue wrapping the specified queue
    /// @throws NullPointerException if transferQueue or breaks is null
    /// @since 1.0.0
    public static <E> BreakableTransferQueue<E> wrap(
            final TransferQueue<E> transferQueue,
            final Set<Break> breaks) {
        return new BreakableTransferQueue<>(transferQueue, breaks, new HashMap<>(), DEFAULT_CHARACTERISTICS,
                DEFAULT_PERMITS, DEFAULT_SAFETY, Object.class);
    }

    /// Creates a BreakableTransferQueue that wraps the specified TransferQueue with full configuration.
    ///
    /// @param <E>             the type of elements held in the transfer queue
    /// @param transferQueue   the TransferQueue to wrap
    /// @param breaks          the set of breaks to apply
    /// @param characteristics the spliterator characteristics
    /// @return a new BreakableTransferQueue wrapping the specified queue with given configuration
    /// @throws NullPointerException if transferQueue or breaks is null
    /// @since 1.0.0
    public static <E> BreakableTransferQueue<E> wrap(
            final TransferQueue<E> transferQueue,
            final Set<Break> breaks,
            final int characteristics) {
        return new BreakableTransferQueue<>(transferQueue, breaks, new HashMap<>(), characteristics, DEFAULT_PERMITS,
                DEFAULT_SAFETY, Object.class);
    }
}
