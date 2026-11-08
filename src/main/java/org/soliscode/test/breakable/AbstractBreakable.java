/*
 * Copyright 2024 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.MethodStatus;
import org.soliscode.test.MethodSupport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/// Abstract base class that provides common functionality for implementing breakable collection classes.
///
/// This class serves as the foundation for all breakable collection implementations, providing:
/// - Break management (adding, checking, and retrieving breaks)
/// - Optional method support inheritance from {@link MethodSupport}
/// - Thread-safe break storage and manipulation
/// - Standard constructors for different initialization scenarios
///
/// ## Purpose
///
/// AbstractBreakable eliminates boilerplate code when creating new breakable collection types by providing
/// a complete implementation of the {@link Breakable} interface. Subclasses only need to implement the
/// specific collection interface methods and apply the breaks appropriately.
///
/// ## Usage Pattern
///
/// Typical usage involves extending this class and implementing the desired collection interface:
///
/// ```java
/// public class BreakableQueue<E> extends AbstractBreakable implements Queue<E> {
///     private final Queue<E> delegate;
///
///     public BreakableQueue(Queue<E> delegate, Collection<Break> breaks) {
///         super(breaks);
///         this.delegate = delegate;
///     }
///
///     @Override
///     public boolean offer(E e) {
///         if (hasBreak(OFFER_ALWAYS_RETURNS_FALSE)) {
///             return false;
///         }
///         return delegate.offer(e);
///     }
///
///     // ... other Queue methods with break handling
/// }
/// ```
///
/// ## Break Management
///
/// This class maintains an internal set of {@link Break} instances that define how the collection's
/// behavior should deviate from the standard contract. Breaks can be:
/// - Set during construction
/// - Copied from another breakable object
/// - Added dynamically after construction
/// - Queried to determine if specific behavior modifications should be applied
///
/// ## Thread Safety
///
/// The break management operations in this class are not inherently thread-safe. Subclasses that need
/// thread safety should implement appropriate synchronization or use concurrent collections for the
/// internal break storage.
///
/// ## Inheritance Hierarchy
///
/// This class extends {@link MethodSupport}, which provides the ability to mark certain
/// collection methods as unsupported, causing them to throw {@link UnsupportedOperationException}.
/// This is useful for testing scenarios where collections have partial interface implementations.
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see Breakable
/// @see Break
/// @see MethodSupport
/// @see BreakableIterable
/// @see BreakableList
/// @see BreakableCollection
public abstract class AbstractBreakable extends MethodSupport implements Breakable, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /// Default value for the safety field
    protected static final boolean DEFAULT_SAFETY = false;

    /// A protected static final set that serves as the default collection of breaks
    /// within the application. This set is intended to be immutable and initialized
    /// as an empty set to ensure no breaks are predefined by default.
    protected static final Set<Break> DEFAULT_BREAKS = Collections.emptySet();

    /// A static, immutable map that defines the default statuses for interface methods.
    ///
    /// This map provides a baseline configuration linking [InterfaceMethod] instances
    /// to their corresponding [MethodStatus] values. It is initialized as an empty map
    /// using [Collections#emptyMap()], indicating no default method-to-status mappings
    /// are predefined until explicitly populated elsewhere in the application.
    protected static final Map<InterfaceMethod, MethodStatus> DEFAULT_METHOD_STATUSES = Collections.emptyMap();

    /// The breaks that will be applied while running methods
    private final @NonNull Set<Break> breaks;

    /// Random number generator for subclasses.
    private static final Random RANDOM = new Random();

    /// Lock for synchronizing access to the breakable object.
    /// This is used to simulate thread safety or lack thereof based on the safety configuration.
    private final transient ReentrantLock lock;

    /// Serialization support for writing the object state.
    ///
    /// This method ensures that the object's fields, including the behavioral
    /// modifications, are correctly serialized. It also handles the non-serializable
    /// mutex field by recording its presence.
    ///
    /// @param out the [ObjectOutputStream] to write to
    /// @throws IOException if an I/O error occurs
    @Serial
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeBoolean(lock != null);
    }

    /// Serialization support for reading the object state.
    ///
    /// This method ensures that the object's fields, including the behavioral
    /// modifications, are correctly restored during deserialization. It also
    /// reconstructs the mutex field if it was present in the original object.
    ///
    /// @param in the [ObjectInputStream] to read from
    /// @throws IOException if an I/O error occurs
    /// @throws ClassNotFoundException if the class of a serialized object could not be found
    @Serial
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (in.readBoolean()) {
            try {
                var field = AbstractBreakable.class.getDeclaredField("lock");
                field.setAccessible(true);
                field.set(this, new ReentrantLock());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IOException("Failed to restore lock field", e);
            }
        }
    }

    /// Creates a breakable object with no breaks applied.
    ///
    /// This constructor initializes an empty break set, creating a collection that behaves
    /// according to the standard interface contract. Breaks can be added later using
    /// {@link #addBreaks(Collection)}.
    ///
    /// This is typically used when you want to create a baseline collection for testing
    /// or when breaks will be determined and added dynamically.
    public AbstractBreakable() {
        super();
        this.breaks = new HashSet<>();
        this.lock = null;
    }

    /// Creates a breakable object by copying the breaks and optional method support from another breakable object.
    ///
    /// This copy constructor creates a deep copy of the break set and optional method configuration
    /// from the source object. The resulting object will have the same behavioral modifications
    /// but is completely independent of the original.
    ///
    /// This is useful for creating variations of existing breakable objects or for implementing
    /// clone-like functionality in subclasses.
    ///
    /// @param other the breakable object to copy breaks and method support from
    /// @throws NullPointerException if other is null
    public AbstractBreakable(final @NonNull AbstractBreakable other) {
        super(other);
        this.breaks = new HashSet<>(other.breaks);
        this.lock = (other.isSafe()) ? new ReentrantLock() : null;
    }

    /// Creates a breakable object with the specified collection of breaks.
    ///
    /// This constructor initializes the break set with the provided breaks, allowing
    /// immediate application of behavioral modifications.
    ///
    /// This is the most common constructor for creating breakable objects with known
    /// behavioral deviations, typically used in test scenarios.
    ///
    /// @param breaks the collection of breaks to apply to this object; must not be null
    ///               and may be empty to create an unbroken object
    /// @param methodStatuses the method status configuration.
    /// @param isSafe whether the resulting object is safe for concurrent access.
    /// @throws NullPointerException if breaks is null
    protected AbstractBreakable(final @NonNull Set<Break> breaks,
                                final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses,
                                final boolean isSafe) {
        super(methodStatuses);
        this.breaks = breaks;
        if (isSafe) {
            this.lock = new ReentrantLock();
        } else {
            this.lock = null;
        }

    }

    /// {@inheritDoc}
    ///
    /// This implementation performs an efficient set lookup to determine if the specified
    /// break is currently applied to this object. The operation is typically O(1) for
    /// HashSet implementations.
    ///
    /// @param aBreak the break to check for; must not be null
    /// @return true if the break is currently applied, false otherwise
    /// @throws NullPointerException if aBreak is null
    @Override
    public boolean hasBreak(final @NonNull Break aBreak) {
        return breaks.contains(Objects.requireNonNull(aBreak));
    }

    /// {@inheritDoc}
    ///
    /// This implementation returns an unmodifiable view of the internal break set,
    /// preventing external modification while allowing iteration and inspection.
    /// The returned set reflects the current state of breaks and will not change
    /// even if breaks are added to this object later.
    ///
    /// @return an unmodifiable set containing all currently applied breaks;
    ///         never null but may be empty
    @Override
    public @NonNull Set<Break> breaks() {
        return Collections.unmodifiableSet(breaks);
    }

    /// {@inheritDoc}
    ///
    /// This implementation adds a provided breaks to the internal break set.
    /// Duplicate breaks are automatically handled by the set semantics - adding
    /// a break that already exists has no effect.
    ///
    /// This method allows dynamic modification of the object's behavior after
    /// construction, which can be useful for testing scenarios where breaks
    /// need to be applied conditionally.
    ///
    /// @param aBreak the break to add; must not be null
    @Override
    public void addBreak(final @NonNull Break aBreak) {
        this.breaks.add(aBreak);
    }

    /// {@inheritDoc}
    ///
    /// This implementation adds all provided breaks to the internal break set.
    /// Duplicate breaks are automatically handled by the set semantics - adding
    /// a break that already exists has no effect.
    ///
    /// This method allows dynamic modification of the object's behavior after
    /// construction, which can be useful for testing scenarios where breaks
    /// need to be applied conditionally.
    ///
    /// @param newBreaks the collection of breaks to add; must not be null
    ///                  but may be empty (which results in no changes)
    @Override
    public void addBreaks(final @NonNull Collection<Break> newBreaks) {
        this.breaks.addAll(newBreaks);
    }

    /// Returns a random integer.
    /// @return a random integer
    protected static int randomInt() {
        return RANDOM.nextInt();
    }

    /// Returns a random index within the specified length.
    /// @param length the maximum length (exclusive)
    /// @return a random index between 0 (inclusive) and length (exclusive)
    protected static int randomIndex(final int length) {
        return Math.abs(RANDOM.nextInt()) % length;
    }

    /// Returns a message indicating that the specified method is not supported.
    /// @param method the method that is not supported
    /// @return the not supported message
    protected static String notSupportedMessage(final @NonNull InterfaceMethod method) {
        return method.methodName() + " is not supported by " + Breakable.class.getSimpleName();
    }

    /// Checks if the specified method is supported.
    /// @param method the method to check
    /// @throws UnsupportedOperationException if the method is not supported
    protected void checkMethodSupport(final @NonNull InterfaceMethod method) {
        if (!supportsMethod(method)) {
            throw new UnsupportedOperationException(notSupportedMessage(method));
        }
    }

    /// Checks if the specified optional method is supported.
    /// If the method is not supported and the specified break is present, a [RuntimeException] is thrown.
    /// Otherwise, if the method is not supported, an [UnsupportedOperationException] is thrown.
    /// @param method the method to check
    /// @param wrongExceptionBreak the break that triggers a [RuntimeException] instead of [UnsupportedOperationException]
    /// @throws RuntimeException if the method is not supported and the break is present
    /// @throws UnsupportedOperationException if the method is not supported and the break is not present
    protected void checkOptionalMethodSupport(final @NonNull InterfaceMethod method,
                                              final @NonNull Break wrongExceptionBreak) {
        if (!supportsMethod(method)) {
            if (hasBreak(wrongExceptionBreak)) {
                throw new RuntimeException(notSupportedMessage(method));
            }
            throw new UnsupportedOperationException(notSupportedMessage(method));
        }
    }

    /// Creates a [RuntimeException] indicating that the wrong exception was thrown.
    /// @param e the original exception
    /// @return the new [RuntimeException]
    protected RuntimeException wrongException(final @NonNull Exception e) {
        return new RuntimeException("Threw wrong exception", e);
    }

    /// Checks if the object is configured to be thread-safe.
    /// @return true if safe, false otherwise
    public boolean isSafe() {
        return lock != null;
    }

    /// Executes the specified runnable with synchronization if the object is safe and the safety break is not present.
    /// @param safetyBreak the break that disables safety
    /// @param runnable the runnable to execute
    protected void runWithBreakableSafety(final @NonNull Break safetyBreak, final @NonNull Runnable runnable) {
        if (lock != null && !hasBreak(safetyBreak)) {
            try {
                lock.lock();
                runnable.run();
            } finally {
                lock.unlock();
            }
        } else {
            runnable.run();
        }
    }

    /// Executes the specified supplier with synchronization if the object is safe and the safety break is not present.
    /// @param <T> the type of the result
    /// @param safetyBreak the break that disables safety
    /// @param supplier the supplier to execute
    /// @return the result from the supplier
    protected <T> T getWithBreakableSafety(final @NonNull Break safetyBreak, final @NonNull Supplier<T> supplier) {
        if (lock != null && !hasBreak(safetyBreak)) {
            try {
                lock.lock();
                return supplier.get();
            } finally {
                lock.unlock();
            }
        }
        return supplier.get();
    }
}
