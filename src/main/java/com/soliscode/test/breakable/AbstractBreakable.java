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
package com.soliscode.test.breakable;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.MethodSupport;

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

    @Serial
    private static final long serialVersionUID = 1L;

    /// The serialized field that holds the breaks, the field [#breaks] that 0.1.4 serialized by default.
    private static final String BREAKS_FIELD = "breaks";

    /// The serialized field that holds the methods whose status is explicitly supported (#59).
    private static final String SUPPORTED_FIELD = "supportedMethods";

    /// The serialized field that holds the methods whose status is explicitly unsupported (#59).
    private static final String UNSUPPORTED_FIELD = "unsupportedMethods";

    /// The serialized form: the breaks, as 0.1.4 wrote them, and the method statuses, which [MethodSupport] does not
    /// serialize, since it is not [Serializable]. A stream written by 0.1.4 has no statuses and reads with none, and
    /// 0.1.4 skips the status fields of a newer stream, but still resolves their methods, so it cannot read a status of
    /// a method constant added after it.
    @Serial
    // UnusedVariable: serialization reads serialPersistentFields by reflection.
    @SuppressWarnings("UnusedVariable")
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField(BREAKS_FIELD, Set.class),
        new ObjectStreamField(SUPPORTED_FIELD, InterfaceMethod[].class),
        new ObjectStreamField(UNSUPPORTED_FIELD, InterfaceMethod[].class),
    };

    /// Random number generator for subclasses.
    private static final Random RANDOM = new Random();

    /// The breaks that will be applied while running methods.
    /// Not final, so that [#clone()] and deserialization can give the object a set of its own.
    private Set<Break> breaks;

    /// Lock for synchronizing access to the breakable object.
    /// This is used to simulate thread safety or lack thereof based on the safety configuration.
    /// Not final, so that [#clone()] and deserialization can give the object a lock of its own.
    private transient @Nullable ReentrantLock lock;

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
    public AbstractBreakable(final AbstractBreakable other) {
        super(other);
        this.breaks = new HashSet<>(other.breaks);
        this.lock = other.lock != null ? new ReentrantLock() : null;
    }

    /// Creates a breakable object with the specified collection of breaks.
    ///
    /// This constructor initializes the break set with the provided breaks, allowing
    /// immediate application of behavioral modifications.
    ///
    /// This is the most common constructor for creating breakable objects with known
    /// behavioral deviations, typically used in test scenarios.
    ///
    /// @param breaksArg the breaks to apply to this object, copied, so that a later change to the caller's set does
    ///               not reach this object; must not be null and may be empty to create an unbroken object
    /// @param methodStatuses the method status configuration, copied.
    /// @param isSafe whether the resulting object is safe for concurrent access.
    /// @throws NullPointerException if `breaksArg` or `methodStatuses` is null
    protected AbstractBreakable(final Set<Break> breaksArg,
                                final Map<InterfaceMethod, MethodStatus> methodStatuses,
                                final boolean isSafe) {
        super(methodStatuses);
        this.breaks = new HashSet<>(Objects.requireNonNull(breaksArg));
        if (isSafe) {
            this.lock = new ReentrantLock();
        } else {
            this.lock = null;
        }

    }

    /// Returns a copy made by [Object#clone()], with a break set and method-status map of its own, and a lock of its
    /// own if this object is safe, so that a break added to the copy does not reach this object, or the reverse.
    /// Only a subclass that implements [Cloneable] can be cloned.
    ///
    /// @return the copy
    /// @throws CloneNotSupportedException if this object's class does not implement [Cloneable]
    /// @since 0.1.5
    @Override
    protected Object clone() throws CloneNotSupportedException {
        final AbstractBreakable copy = (AbstractBreakable) super.clone();
        copy.breaks = new HashSet<>(breaks);
        copy.lock = lock != null ? new ReentrantLock() : null;
        return copy;
    }

    /// Serialization support for writing the object state.
    ///
    /// Writes the fields of [#serialPersistentFields]: the breaks, and the methods whose status is explicitly
    /// supported and explicitly unsupported. Then it records whether the object has a lock, which is not serializable.
    ///
    /// @param out the [ObjectOutputStream] to write to
    /// @throws IOException if an I/O error occurs, or [java.io.NotSerializableException] if a method whose status is
    ///                     set is not serializable (every [InterfaceMethod] in this library is an enum)
    @Serial
    private void writeObject(final ObjectOutputStream out) throws IOException {
        final List<InterfaceMethod> supported = new ArrayList<>();
        final List<InterfaceMethod> unsupported = new ArrayList<>();
        methodStatuses().forEach((method, status) -> (status.supported() ? supported : unsupported).add(method));
        final ObjectOutputStream.PutField fields = out.putFields();
        fields.put(BREAKS_FIELD, breaks);
        fields.put(SUPPORTED_FIELD, supported.toArray(new InterfaceMethod[0]));
        fields.put(UNSUPPORTED_FIELD, unsupported.toArray(new InterfaceMethod[0]));
        out.writeFields();
        out.writeBoolean(lock != null);
    }

    /// Serialization support for reading the object state.
    ///
    /// Reads the fields of [#serialPersistentFields], checks them, and restores the breaks and the method statuses;
    /// a stream written by 0.1.4 has no status fields and reads with no statuses. Then it gives the object a lock if
    /// the original had one.
    ///
    /// @param in the [ObjectInputStream] to read from
    /// @throws IOException if an I/O error occurs, or [InvalidObjectException] if the breaks are not a set of
    ///                     [Break]s, or a status field is not an array of methods, holds a null, or names a method
    ///                     already read
    /// @throws ClassNotFoundException if the class of a serialized object could not be found
    @Serial
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        final ObjectInputStream.GetField fields = in.readFields();
        this.breaks = readBreaks(fields.get(BREAKS_FIELD, null));
        final Map<InterfaceMethod, MethodStatus> statuses = new HashMap<>();
        readStatuses(fields.get(SUPPORTED_FIELD, null), MethodStatus.SUPPORTED, statuses);
        readStatuses(fields.get(UNSUPPORTED_FIELD, null), MethodStatus.UNSUPPORTED, statuses);
        restoreMethodStatuses(statuses);
        if (in.readBoolean()) {
            this.lock = new ReentrantLock();
        }
    }

    /// Returns a set of its own holding the breaks read from a stream.
    ///
    /// @param value the value of the breaks field
    /// @return the breaks
    /// @throws InvalidObjectException if `value` is not a set, or holds something other than a [Break]
    private static Set<Break> readBreaks(final @Nullable Object value) throws InvalidObjectException {
        if (!(value instanceof Set<?> set)) {
            throw new InvalidObjectException("The breaks are not a set");
        }
        final Set<Break> result = new HashSet<>();
        for (final Object element : set) {
            if (!(element instanceof Break aBreak)) {
                throw new InvalidObjectException("The breaks hold something other than a Break: " + element);
            }
            result.add(aBreak);
        }
        return result;
    }

    /// Adds the methods read from a stream's status field to `statuses`, each with `status`.
    ///
    /// @param value the value of the status field; null if the stream has none, as one written by 0.1.4
    /// @param status the status of the field's methods
    /// @param statuses the statuses read so far, added to
    /// @throws InvalidObjectException if `value` is not an array of methods, or holds a null or a method already read
    private static void readStatuses(final @Nullable Object value, final MethodStatus status,
                                     final Map<InterfaceMethod, MethodStatus> statuses)
            throws InvalidObjectException {
        if (value == null) {
            return;
        }
        if (!(value instanceof InterfaceMethod[] methods)) {
            throw new InvalidObjectException("A method-status field is not an array of methods");
        }
        for (final Object element : methods) {
            if (!(element instanceof InterfaceMethod method)) {
                throw new InvalidObjectException("A method-status field holds a null");
            }
            if (statuses.put(method, status) != null) {
                throw new InvalidObjectException("A method-status field repeats " + method);
            }
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
    public boolean hasBreak(final Break aBreak) {
        return breaks.contains(Objects.requireNonNull(aBreak));
    }

    /// {@inheritDoc}
    ///
    /// This implementation returns an unmodifiable view of the internal break set,
    /// preventing external modification while allowing iteration and inspection.
    /// The returned set is a view: breaks added to this object later appear in it.
    ///
    /// @return an unmodifiable set containing all currently applied breaks;
    ///         never null but may be empty
    @Override
    public Set<Break> breaks() {
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
    public void addBreak(final Break aBreak) {
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
    public void addBreaks(final Collection<Break> newBreaks) {
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
        return Math.floorMod(RANDOM.nextInt(), length);
    }

    /// Returns a message indicating that the specified method is not supported.
    /// @param method the method that is not supported
    /// @return the not supported message
    protected static String notSupportedMessage(final InterfaceMethod method) {
        return method.methodName() + " is not supported by " + Breakable.class.getSimpleName();
    }

    /// Checks if the specified method is supported.
    /// @param method the method to check
    /// @throws UnsupportedOperationException if the method is not supported
    protected void checkMethodSupport(final InterfaceMethod method) {
        if (!supportsMethod(method)) {
            throw new UnsupportedOperationException(notSupportedMessage(method));
        }
    }

    /// Checks if the specified optional method is supported.
    /// If the method is not supported and the specified break is present, a [RuntimeException] is thrown.
    /// Otherwise, if the method is not supported, an [UnsupportedOperationException] is thrown.
    /// @param method the method to check
    /// @param wrongExceptionBreak the break that triggers a [RuntimeException] instead of
    ///     [UnsupportedOperationException]
    /// @throws RuntimeException if the method is not supported and the break is present
    /// @throws UnsupportedOperationException if the method is not supported and the break is not present
    protected void checkOptionalMethodSupport(final InterfaceMethod method,
                                              final Break wrongExceptionBreak) {
        if (!supportsMethod(method)) {
            if (hasBreak(wrongExceptionBreak)) {
                throw new WrongException(notSupportedMessage(method));
            }
            throw new UnsupportedOperationException(notSupportedMessage(method));
        }
    }

    /// Creates a [WrongException] indicating that the wrong exception was thrown.
    /// @param e the original exception
    /// @return the new [WrongException]
    protected RuntimeException wrongException(final Exception e) {
        return new WrongException("Threw wrong exception", e);
    }

    /// Checks if the object is configured to be thread-safe.
    /// @return true if safe, false otherwise
    public boolean isSafe() {
        return lock != null;
    }

    /// Returns whether this object holds a lock, its own or one it shares with the breakable it is a view of. A
    /// breakable whose store is thread safe (a concurrent map, a blocking queue) holds none, though it is safe.
    ///
    /// @return whether this object holds a lock
    final boolean hasLock() {
        return lock != null;
    }

    /// Gives a view the owner's lock, the same [ReentrantLock] or none, so that the view runs under the lock of the
    /// breakable it is a view of, as the JDK's synchronized wrappers synchronize a view on the parent's lock (#52).
    ///
    /// @param <T> the type of the view
    /// @param view the view, which takes the owner's lock
    /// @param owner the breakable whose lock the view takes
    /// @return the view
    static <T extends AbstractBreakable> T sharingLockOf(final T view, final AbstractBreakable owner) {
        ((AbstractBreakable) view).lock = owner.lock;
        return view;
    }

    /// Runs the runnable under this object's lock, if it has one. For a method that has no `*_IS_NOT_THREAD_SAFE`
    /// break.
    ///
    /// @param runnable the runnable to run
    /// @since 0.1.5
    protected void runWithSafety(final Runnable runnable) {
        final ReentrantLock theLock = lock;
        if (theLock != null) {
            theLock.lock();
            try {
                runnable.run();
            } finally {
                theLock.unlock();
            }
        } else {
            runnable.run();
        }
    }

    /// Returns the supplier's result, got under this object's lock, if it has one. For a method that has no
    /// `*_IS_NOT_THREAD_SAFE` break.
    ///
    /// @param <T> the type of the result
    /// @param supplier the supplier to run
    /// @return the supplier's result
    /// @since 0.1.5
    protected <T extends @Nullable Object> T getWithSafety(final Supplier<T> supplier) {
        final ReentrantLock theLock = lock;
        if (theLock != null) {
            theLock.lock();
            try {
                return supplier.get();
            } finally {
                theLock.unlock();
            }
        }
        return supplier.get();
    }

    /// Executes the specified runnable with synchronization if the object is safe and the safety break is not present.
    /// @param safetyBreak the break that disables safety
    /// @param runnable the runnable to execute
    protected void runWithBreakableSafety(final Break safetyBreak, final Runnable runnable) {
        final ReentrantLock theLock = lock;
        if (theLock != null && !hasBreak(safetyBreak)) {
            theLock.lock();
            try {
                runnable.run();
            } finally {
                theLock.unlock();
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
    protected <T> T getWithBreakableSafety(final Break safetyBreak, final Supplier<T> supplier) {
        final ReentrantLock theLock = lock;
        if (theLock != null && !hasBreak(safetyBreak)) {
            theLock.lock();
            try {
                return supplier.get();
            } finally {
                theLock.unlock();
            }
        }
        return supplier.get();
    }
}
