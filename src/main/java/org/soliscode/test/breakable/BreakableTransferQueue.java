package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.MethodStatus;
import org.soliscode.test.contract.transferqueue.TransferQueueMethods;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

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
/// - **TRANSFER_DOES_NOT_ADD_ELEMENT**: Forces transfer() to accept but not add_singleElement_returnsTrueAndUpdatesSize elements
/// - **TRANSFER_THROWS_EXCEPTION**: Forces transfer() to throw exceptions instead of normal operation
///
/// ### Try Transfer Operations
/// - **TRY_TRANSFER_ALWAYS_RETURNS_FALSE**: Control tryTransfer() to always return false
/// - **TRY_TRANSFER_THROWS_EXCEPTION**: Forces tryTransfer() to throw exceptions
/// - **TRY_TRANSFER_DOES_NOT_ADD_ELEMENT**: Forces tryTransfer() to accept but not add_singleElement_returnsTrueAndUpdatesSize elements
///
/// ### Try Transfer with Timeout Operations
/// - **TRY_TRANSFER_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE**: Control tryTransfer(timeout) to always return false
/// - **TRY_TRANSFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION**: Forces tryTransfer(timeout) to throw InterruptedException
/// - **TRY_TRANSFER_WITH_TIMEOUT_DOES_NOT_ADD_ELEMENT**: Forces tryTransfer(timeout) to accept but not add_singleElement_returnsTrueAndUpdatesSize elements
///
/// ### Consumer Detection Operations
/// - **HAS_WAITING_CONSUMER_ALWAYS_RETURNS_FALSE**: Forces hasWaitingConsumer() to always return false
/// - **HAS_WAITING_CONSUMER_ALWAYS_RETURNS_TRUE**: Forces hasWaitingConsumer() to always return true
/// - **GET_WAITING_CONSUMER_COUNT_ALWAYS_RETURNS_ZERO**: Forces getWaitingConsumerCount() to always return 0
/// - **GET_WAITING_CONSUMER_COUNT_RETURNS_RANDOM**: Forces getWaitingConsumerCount() to return arbitrary values
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

    private static final int PSEUDO_RANDOMIZER_CONSTANT = 1000;

    @Serial
    private static final long serialVersionUID = 1L;

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
    /// a random value instead of the actual count of waiting consumers.
    ///
    /// **Affected Methods:**
    /// - {@link #getWaitingConsumerCount()} - Returns arbitrary value
    ///
    /// **Use Cases:**
    /// - Testing robustness against incorrect consumer counts
    /// - Simulating corrupted consumer tracking
    /// - Testing consumer count validation logic
    public static final Break GET_WAITING_CONSUMER_COUNT_RETURNS_RANDOM =
            new Break("GET_WAITING_CONSUMER_COUNT_RETURNS_RANDOM");

    // ========== Instance Fields ==========

    /// The underlying TransferQueue that this BreakableTransferQueue wraps.
    /// All operations delegate to this queue unless breaks are applied.
    private final @NonNull TransferQueue<E> transferQueue;

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
    /// This constructor creates a new instance that shares the underlying transfer queue data
    /// and inherits all configuration from the source queue.
    ///
    /// **Inherited Configuration:**
    /// - All break settings from source
    /// - Null element policies
    /// - Method support configuration
    /// - Underlying TransferQueue reference (shared, not copied)
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
    public BreakableTransferQueue(final @NonNull BreakableTransferQueue<E> other) {
        this(new LinkedTransferQueue<>(other.transferQueue), new HashSet<>(other.breaks()),
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
    /// @param transferQueue   the TransferQueue to wrap
    /// @param breaks          the breaks to apply
    /// @param methodStatuses  the method support status for each optional method
    /// @param characteristics the spliterator characteristics
    /// @param permits         the flags that indicate what types of values are supported by the collection.
    /// @param isSafe          indicates whether the collection is safe for concurrent use
    /// @param compatibleType  the type that is compatible with this collection
    /// @throws NullPointerException if any of the mandatory parameters are null
    /// @since 1.0.0
    protected BreakableTransferQueue(
            final @NonNull TransferQueue<E> transferQueue,
            final @NonNull Set<Break> breaks,
            final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses,
            final int characteristics,
            final int permits,
            final boolean isSafe,
            final Class<?> compatibleType) {
        super(this.transferQueue = transferQueue, breaks, methodStatuses, characteristics, permits, isSafe,
                compatibleType);
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
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to TRANSFER_THROWS_INTERRUPTED_EXCEPTION break
    /// - May throw IllegalStateException due to TRANSFER_THROWS_EXCEPTION break
    @Override
    public void transfer(final @NonNull E e) throws InterruptedException {
        if (!supportsMethod(TransferQueueMethods.TRANSFER)) {
            throw new UnsupportedOperationException("transfer() method is not supported");
        }

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
            // Block indefinitely - simulate by waiting on a never-notified object
            synchronized (this) {
                try {
                    wait(); // This will block indefinitely
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ie;
                }
            }
            return;
        }

        transferQueue.transfer(e);
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
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw IllegalStateException due to TRY_TRANSFER_THROWS_EXCEPTION break
    @Override
    public boolean tryTransfer(final @NonNull E e) {
        if (!supportsMethod(TransferQueueMethods.TRY_TRANSFER)) {
            throw new UnsupportedOperationException("tryTransfer() method is not supported");
        }

        if (hasBreak(TRY_TRANSFER_THROWS_EXCEPTION)) {
            throw new IllegalStateException("TryTransfer operation failed due to break");
        }

        if (hasBreak(TRY_TRANSFER_DOES_NOT_ADD_ELEMENT)) {
            return true; // Return true but don't actually transfer the element
        }

        // Perform the transfer first
        boolean result = transferQueue.tryTransfer(e);

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
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to TRY_TRANSFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break
    @Override
    public boolean tryTransfer(final @NonNull E e, final long timeout, final @NonNull TimeUnit unit)
            throws InterruptedException {
        if (!supportsMethod(TransferQueueMethods.TRY_TRANSFER_TIMEOUT)) {
            throw new UnsupportedOperationException("tryTransfer(timeout) method is not supported");
        }

        if (hasBreak(TRY_TRANSFER_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("TryTransfer with timeout operation interrupted due to break");
        }

        if (hasBreak(TRY_TRANSFER_WITH_TIMEOUT_DOES_NOT_ADD_ELEMENT)) {
            return true; // Return true but don't actually transfer the element
        }

        // Perform the transfer first
        boolean result = transferQueue.tryTransfer(e, timeout, unit);

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
        if (!supportsMethod(TransferQueueMethods.HAS_WAITING_CONSUMER)) {
            throw new UnsupportedOperationException("hasWaitingConsumer() method is not supported");
        }

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
        if (!supportsMethod(TransferQueueMethods.GET_WAITING_CONSUMER_COUNT)) {
            throw new UnsupportedOperationException("getWaitingConsumerCount() method is not supported");
        }

        if (hasBreak(GET_WAITING_CONSUMER_COUNT_ALWAYS_RETURNS_ZERO)) {
            return 0;
        }

        if (hasBreak(GET_WAITING_CONSUMER_COUNT_RETURNS_RANDOM)) {
            return Math.abs(hashCode() % PSEUDO_RANDOMIZER_CONSTANT); // Return deterministic "random" value
        }

        return transferQueue.getWaitingConsumerCount();
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
            super(new ArrayDeque<>());
        }

        /// Creates a new Builder by copying configuration from another builder.
        ///
        /// @param other the builder to copy configuration from
        /// @throws NullPointerException if other is null
        /// @since 1.0.0
        public Builder(final @NonNull Builder<E> other) {
            super(other);
        }

        /// Creates a new Builder pre-populated with elements.
        ///
        /// @param elements the elements to be placed in the builder
        /// @throws NullPointerException if elements is null
        /// @since 1.0.0
        public Builder(final @NonNull Collection<E> elements) {
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
        public @NonNull Builder<E> copy() {
            return new Builder<>(this);
        }

        /// Builds a new BreakableTransferQueue instance with the configured settings.
        ///
        /// @return a new BreakableTransferQueue
        @Override
        public @NonNull BreakableTransferQueue<E> build() {
            return new BreakableTransferQueue<>(new LinkedTransferQueue<>(elements()), breaks(), methodStatuses(),
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
    public static <E> @NonNull BreakableTransferQueue<E> wrap(
            final @NonNull TransferQueue<E> transferQueue,
            final @NonNull Set<Break> breaks) {
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
    public static <E> @NonNull BreakableTransferQueue<E> wrap(
            final @NonNull TransferQueue<E> transferQueue,
            final @NonNull Set<Break> breaks,
            final int characteristics) {
        return new BreakableTransferQueue<>(transferQueue, breaks, new HashMap<>(), characteristics, DEFAULT_PERMITS,
                DEFAULT_SAFETY, Object.class);
    }
}
