package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.MethodStatus;
import org.soliscode.test.contract.blockingdeque.BlockingDequeMethods;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

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
/// - **Testing BlockingDeque Contract Compliance**: Verifying that code correctly handles blocking double-ended operations
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
/// - **PUT_FIRST_DOES_NOT_ADD_ELEMENT**: Forces putFirst() to accept but not add_singleElement_returnsTrueAndUpdatesSize elements
/// - **PUT_FIRST_THROWS_EXCEPTION**: Forces putFirst() to throw exceptions instead of normal operation
/// - **PUT_LAST_THROWS_INTERRUPTED_EXCEPTION**: Forces putLast() to throw InterruptedException
/// - **PUT_LAST_DOES_NOT_ADD_ELEMENT**: Forces putLast() to accept but not add_singleElement_returnsTrueAndUpdatesSize elements
/// - **PUT_LAST_THROWS_EXCEPTION**: Forces putLast() to throw exceptions instead of normal operation
///
/// ### Blocking Take Operations
/// - **TAKE_FIRST_THROWS_INTERRUPTED_EXCEPTION**: Forces takeFirst() to throw InterruptedException
/// - **TAKE_FIRST_ALWAYS_RETURNS_NULL**: Control takeFirst() method to always return null
/// - **TAKE_FIRST_DOES_NOT_REMOVE_ELEMENT**: Forces takeFirst() to return element without removing it
/// - **TAKE_LAST_THROWS_INTERRUPTED_EXCEPTION**: Forces takeLast() to throw InterruptedException
/// - **TAKE_LAST_ALWAYS_RETURNS_NULL**: Control takeLast() method to always return null
/// - **TAKE_LAST_DOES_NOT_REMOVE_ELEMENT**: Forces takeLast() to return element without removing it
///
/// ### Timeout Operations
/// - **OFFER_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE**: Control offerFirst(timeout) to always return false
/// - **OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION**: Forces offerFirst(timeout) to throw InterruptedException
/// - **OFFER_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE**: Control offerLast(timeout) to always return false
/// - **OFFER_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION**: Forces offerLast(timeout) to throw InterruptedException
/// - **POLL_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL**: Control pollFirst(timeout) to always return null
/// - **POLL_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION**: Forces pollFirst(timeout) to throw InterruptedException
/// - **POLL_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL**: Control pollLast(timeout) to always return null
/// - **POLL_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION**: Forces pollLast(timeout) to throw InterruptedException
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

    @Serial
    private static final long serialVersionUID = 1L;

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

    // ========== Instance Fields ==========

    /// The underlying BlockingDeque that this BreakableBlockingDeque wraps.
    /// All operations delegate to this deque unless breaks are applied.
    private final @NonNull BlockingDeque<E> blockingDeque;

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
    public BreakableBlockingDeque(final @NonNull BreakableBlockingDeque<E> other) {
        this(new LinkedBlockingDeque<>(other.blockingDeque), new HashSet<>(other.breaks()),
                new HashMap<>(other.methodStatuses()), other.characteristics(), other.permits(), other.isSafe(),
                other.compatibleType());
    }

    /// Constructs a new `BreakableBlockingDeque` which allows enhanced control over a blocking deque
    /// by introducing breakpoints and status tracking for method executions. This facilitates flexible,
    /// interruptible operations in concurrent programming scenarios.
    ///
    /// @param blockingDeque   The underlying [BlockingDeque] instance that this class wraps. It must
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
            final @NonNull BlockingDeque<E> blockingDeque,
            final @NonNull Set<Break> breaks,
            final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses,
            final int characteristics,
            final int permits,
            final boolean isSafe,
            final Class<?> compatibleType) {
        super(this.blockingDeque = blockingDeque, breaks, methodStatuses, characteristics, permits, isSafe,
                compatibleType);
    }

    // BreakableBlockingDeque cant extend both BreakableDeque and BreakableBlockingQueue, so we need to
    // re-resolve that annotation differences that were handled in BreakablBlockingQueue.

    /// {@inheritDoc}
    @Override
    public boolean add(final @NonNull E e) {   // resolve annotation conflict between Collection and BlockingDeque
        return super.add(e);
    }

    /// {@inheritDoc}
    @Override
    public boolean offer(final @NonNull E e) { // resolve annotation conflict between queue and BlockingQueue
        return super.offer(e);
    }

    /// {@inheritDoc}
    @Override
    public void push(final @NonNull E e) { // resolve annotation conflict between Deque and BlockingDeque
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
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to PUT_FIRST_THROWS_INTERRUPTED_EXCEPTION break
    /// - May throw IllegalStateException due to PUT_FIRST_THROWS_EXCEPTION break
    @Override
    public void putFirst(final @NonNull E e) throws InterruptedException {
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
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to PUT_LAST_THROWS_INTERRUPTED_EXCEPTION break
    /// - May throw IllegalStateException due to PUT_LAST_THROWS_EXCEPTION break
    @Override
    public void putLast(final @NonNull E e) throws InterruptedException {
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
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to TAKE_FIRST_THROWS_INTERRUPTED_EXCEPTION break
    @Override
    public @NonNull E takeFirst() throws InterruptedException {
        checkMethodSupport(BlockingDequeMethods.TAKE_FIRST);

        if (hasBreak(TAKE_FIRST_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("TakeFirst operation interrupted due to break");
        }

        if (hasBreak(TAKE_FIRST_ALWAYS_RETURNS_NULL)) {
            //noinspection DataFlowIssue
            return null; // Non-standard behavior for testing
        }

        if (hasBreak(TAKE_FIRST_DOES_NOT_REMOVE_ELEMENT)) {
            E e = blockingDeque.takeFirst();
            blockingDeque.offerFirst(e);
            return e;
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
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to TAKE_LAST_THROWS_INTERRUPTED_EXCEPTION break
    @SuppressWarnings("DataFlowIssue")
    @Override
    public @NonNull E takeLast() throws InterruptedException {
        checkMethodSupport(BlockingDequeMethods.TAKE_LAST);

        if (hasBreak(TAKE_LAST_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("TakeLast operation interrupted due to break");
        }

        if (hasBreak(TAKE_LAST_ALWAYS_RETURNS_NULL)) {
            return null; // Non-standard behavior for testing
        }

        if (hasBreak(TAKE_LAST_DOES_NOT_REMOVE_ELEMENT)) {
            E first = takeFirst();
            putFirst(first);
            return first;
        }

        return blockingDeque.takeLast();
    }

    /// {@inheritDoc}
    ///
    /// Inserts the specified element at the front of this deque, waiting up to the specified wait time if necessary for space to become available.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #OFFER_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE} - Returns false regardless of success
    /// - {@link #OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break
    @Override
    public boolean offerFirst(final @NonNull E e, final long timeout, final @NonNull TimeUnit unit)
            throws InterruptedException {
        checkMethodSupport(BlockingDequeMethods.OFFER_FIRST_TIMEOUT);

        if (hasBreak(OFFER_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("OfferFirst with timeout operation interrupted due to break");
        }

        if (hasBreak(OFFER_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE)) {
            return false;
        }

        return blockingDeque.offerFirst(e, timeout, unit);
    }

    /// {@inheritDoc}
    ///
    /// Inserts the specified element at the end of this deque, waiting up to the specified wait time if necessary for space to become available.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #OFFER_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE} - Returns false regardless of success
    /// - {@link #OFFER_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to OFFER_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break
    @Override
    public boolean offerLast(final @NonNull E e, final long timeout, final @NonNull TimeUnit unit)
            throws InterruptedException {
        checkMethodSupport(BlockingDequeMethods.OFFER_LAST_TIMEOUT);

        if (hasBreak(OFFER_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("OfferLast with timeout operation interrupted due to break");
        }

        if (hasBreak(OFFER_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_FALSE)) {
            return false;
        }

        return blockingDeque.offerLast(e, timeout, unit);
    }

    /// {@inheritDoc}
    ///
    /// Retrieves and removes the first element of this deque, waiting up to the specified wait time if necessary for an element to become available.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #POLL_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL} - Returns null regardless of content
    /// - {@link #POLL_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to POLL_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break
    @Override
    public @Nullable E pollFirst(final long timeout, final @NonNull TimeUnit unit) throws InterruptedException {
        checkMethodSupport(BlockingDequeMethods.POLL_FIRST_TIMEOUT);

        if (hasBreak(POLL_FIRST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("PollFirst with timeout operation interrupted due to break");
        }

        if (hasBreak(POLL_FIRST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL)) {
            return null;
        }

        return blockingDeque.pollFirst(timeout, unit);
    }

    /// {@inheritDoc}
    ///
    /// Retrieves and removes the last element of this deque, waiting up to the specified wait time if necessary for an element to become available.
    /// The behavior can be modified by the following breaks:
    ///
    /// **Supported Breaks:**
    /// - {@link #POLL_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL} - Returns null regardless of content
    /// - {@link #POLL_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION} - Throws InterruptedException
    ///
    /// **Exception Handling:**
    /// - Throws UnsupportedOperationException if the method is not supported
    /// - May throw InterruptedException due to POLL_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION break
    @Override
    public @Nullable E pollLast(final long timeout, final @NonNull TimeUnit unit) throws InterruptedException {
        checkMethodSupport(BlockingDequeMethods.POLL_LAST_TIMEOUT);

        if (hasBreak(POLL_LAST_WITH_TIMEOUT_THROWS_INTERRUPTED_EXCEPTION)) {
            throw new InterruptedException("PollLast with timeout operation interrupted due to break");
        }

        if (hasBreak(POLL_LAST_WITH_TIMEOUT_ALWAYS_RETURNS_NULL)) {
            return null;
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
    public void put(final @NonNull E e) throws InterruptedException {
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
    public @NonNull E take() throws InterruptedException {
        return takeFirst();
    }

    /// {@inheritDoc}
    ///
    /// Inserts the specified element into this deque, waiting up to the specified wait time if necessary for space to become available.
    /// This method is equivalent to offerLast(E, long, TimeUnit).
    ///
    /// **Exception Handling:**
    /// - Delegates to offerLast() which may throw exceptions based on configured breaks
    @Override
    public boolean offer(final @NonNull E e, final long timeout, final @NonNull TimeUnit unit)
            throws InterruptedException {
        return offerLast(e, timeout, unit);
    }

    /// {@inheritDoc}
    ///
    /// Retrieves and removes the head of this deque, waiting up to the specified wait time if necessary for an element to become available.
    /// This method is equivalent to pollFirst(long, TimeUnit).
    ///
    /// **Exception Handling:**
    /// - Delegates to pollFirst() which may throw exceptions based on configured breaks
    @Override
    public @Nullable E poll(final long timeout, final @NonNull TimeUnit unit) throws InterruptedException {
        return pollFirst(timeout, unit);
    }

    /// {@inheritDoc}
    ///
    /// Returns the number of additional elements that this deque can ideally accept without blocking.
    ///
    /// **Exception Handling:**
    /// - Delegates to underlying BlockingDeque implementation
    @Override
    public int remainingCapacity() {
        return blockingDeque.remainingCapacity();
    }

    /// {@inheritDoc}
    ///
    /// Removes all available elements from this deque and adds them to the given collection.
    ///
    /// **Exception Handling:**
    /// - Delegates to underlying BlockingDeque implementation
    @Override
    public int drainTo(final @NonNull Collection<? super E> c) {
        return blockingDeque.drainTo(c);
    }

    /// {@inheritDoc}
    ///
    /// Removes at most the given number of available elements from this deque and adds them to the given collection.
    ///
    /// **Exception Handling:**
    /// - Delegates to underlying BlockingDeque implementation
    @Override
    public int drainTo(final @NonNull Collection<? super E> c, final int maxElements) {
        return blockingDeque.drainTo(c, maxElements);
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

        private static final boolean DEFAULT_FAIRNESS = false;

        /// The capacity of the deque. If negative, the deque has no explicit capacity limit.
        private int capacity;

        /// Creates a new AbstractBuilder with default configuration.
        public AbstractBuilder() {
            this.capacity = -1;
        }

        /// Creates a new AbstractBuilder pre-populated with elements.
        ///
        /// @param elements the elements to be placed in the builder
        public AbstractBuilder(final @NonNull Collection<E> elements) {
            super(elements);
            this.capacity = -1;
        }

        /// Creates a new AbstractBuilder by copying configuration from another builder.
        ///
        /// @param other the builder to copy configuration from
        public AbstractBuilder(final @NonNull AbstractBuilder<B, C, E> other) {
            super(other);
            this.capacity = other.capacity;
        }

        /// Sets the capacity of the blocking deque.
        ///
        /// @param capacity the capacity to set; negative for no limit
        public void setCapacity(final int capacity) {
            this.capacity = capacity;
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
        ///     .addElement("item1");
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
        public Builder(final @NonNull Builder<E> other) {
            super(other);
        }

        /// Creates a new Builder pre-populated with the specified elements.
        ///
        /// @param elements the initial elements to be placed in the blocking deque
        public Builder(final @NonNull Collection<E> elements) {
            super(elements);
        }

        /// {@inheritDoc}
        @Override
        public @NonNull Builder<E> self() {
            return this;
        }

        /// {@inheritDoc}
        @Override
        public @NonNull Builder<E> copy() {
            return new Builder<>(this);
        }

        /// Builds a new [BreakableBlockingDeque] instance with the configured settings.
        ///
        /// @return a new BreakableBlockingDeque instance
        @Override
        public @NonNull BreakableBlockingDeque<E> build() {
            BlockingDeque<E> queue;
            if (capacity() < 0) {
                queue = new LinkedBlockingDeque<>(elements());
            } else {
                queue = new LinkedBlockingDeque<>(capacity());
                queue.addAll(elements());
            }
            return new BreakableBlockingDeque<>(queue, breaks(), methodStatuses(), characteristics(), permits(),
                    isSafe(), compatibleType());
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
    public static <E> @NonNull BreakableBlockingDeque<E> wrap(
            final @NonNull BlockingDeque<E> blockingDeque,
            final @NonNull Set<Break> breaks) {
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
    public static <E> @NonNull BreakableBlockingDeque<E> wrap(
            final @NonNull BlockingDeque<E> blockingDeque,
            final @NonNull Set<Break> breaks,
            final int characteristics) {
        return new BreakableBlockingDeque<>(blockingDeque, breaks, DEFAULT_METHOD_STATUSES,
                characteristics, DEFAULT_PERMITS, DEFAULT_SAFETY, Object.class);
    }
}
