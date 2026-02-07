package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.MethodStatus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/// A breakable wrapper for an object that allows for intentional misbehavior.
///
/// ## Purpose
/// This class is designed for testing purposes, allowing developers to simulate
/// various failures and edge cases in standard `Object` methods such as `equals`,
/// `hashCode`, and `toString`. It provides a set of predefined [Break] instances
/// that can be applied to alter the behavior of these methods.
///
/// ## Usage Examples
/// ```java
/// BreakableObject<String> breakable = new BreakableObject<>("test");
/// breakable.apply(BreakableObject.EQUALS_ALWAYS_RETURNS_FALSE);
/// // Now breakable.equals("test") will return false
/// ```
///
/// ## Thread Safety
/// This class is thread-safe as it inherits from [AbstractBreakable], which
/// uses a mutex for synchronizing access to the set of active breaks.
///
/// @param <T> the type of the underlying object
/// @since 0.1.0
public class BreakableObject<T> extends AbstractBreakable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /// The underlying value wrapped by this breakable object.
    private final @NonNull T value;

    /// A break that causes `equals(Object)` to always return `true`,
    /// violating the reflexive, symmetric, and transitive properties.
    public static final Break EQUALS_ALWAYS_RETURNS_TRUE =
            new Break("The method equals(Object) always returns true");

    /// A break that causes `equals(Object)` to always return `false`,
    /// even when comparing an object to itself.
    public static final Break EQUALS_ALWAYS_RETURNS_FALSE =
            new Break("The method equals(Object) always returns false");

    /// A break that causes `equals(Object)` to return the logical negation
    /// of the actual equality result.
    public static final Break EQUALS_ALWAYS_RETURNS_OPPOSITE_VALUE =
            new Break("The method equals(Object) always returns the opposite value");

    /// A break that causes `equals(Object)` to throw a `ClassCastException`
    /// when the other object's class does not match this object's class.
    public static final Break EQUALS_THROWS_FOR_OTHER_TYPES =
            new Break("The method equals(Object) throws exception when type does not match");

    /// A break that causes `hashCode()` to always return `0`.
    public static final Break HASH_CODE_ALWAYS_RETURNS_ZERO =
            new Break("The method hashCode() always returns zero");

    /// A break that causes `hashCode()` to return a random value on each call,
    /// violating the stability requirement of `hashCode()`.
    public static final Break HASH_CODE_IS_NOT_STABLE =
            new Break("The method hashCode() has an unstable return value");

    /// A break that causes `toString()` to always return an empty string.
    public static final Break TO_STRING_RETURNS_EMPTY_STRING =
            new Break("The method toString() returns the empty string");

    /// Creates a new `BreakableObject` by copying the state of another one.
    ///
    /// @param other the `BreakableObject` to copy
    public BreakableObject(final BreakableObject<T> other) {
        this(other.value, new HashSet<>(other.breaks()), new HashMap<>(other.methodStatuses()), other.isSafe());
    }

    /// Creates a new `BreakableObject` with the given value.
    ///
    /// @param value the value to wrap
    public BreakableObject(final @NonNull T value) {
        this(value, new HashSet<>(), new HashMap<>(), true);
    }

    /// Internal constructor for creating a `BreakableObject` with a specific state.
    ///
    /// @param value the underlying value
    /// @param breaks the initial set of breaks
    /// @param methodStatuses the initial method statuses
    /// @param isSafe the initial safety state
    protected BreakableObject(final @NonNull T value, final @NonNull Set<Break> breaks,
                              final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses,
                              final boolean isSafe) {
        super(breaks, methodStatuses, isSafe);
        this.value = value;
    }

    /// Compares this object to the specified object for equality.
    ///
    /// The behavior of this method can be modified by applying various
    /// equality-related breaks.
    ///
    /// @param o the object to compare with
    /// @return `true` if the objects are equal (or if a break forces it), `false` otherwise
    /// @throws ClassCastException if the EQUALS_THROWS_FOR_OTHER_TYPES break is active and types mismatch
    /// @see Object#equals(Object)
    @Override
    public boolean equals(final Object o) {
        if (hasBreak(EQUALS_ALWAYS_RETURNS_TRUE)) {
            return true;
        }
        if (hasBreak(EQUALS_ALWAYS_RETURNS_FALSE)) {
            return false;
        }
        boolean result;
        if (!getClass().equals(o.getClass())) {
            if (hasBreak(EQUALS_THROWS_FOR_OTHER_TYPES)) {
                throw new ClassCastException();
            }
            result = false;
        } else {
            result =  valueEquals((BreakableObject<?>) o);
        }
        if (hasBreak(EQUALS_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
            return !result;
        }
        return result;
    }

    /// Compares the underlying values of this object and another `BreakableObject`.
    ///
    /// @param other the other `BreakableObject` to compare
    /// @return `true` if the underlying values are equal, `false` otherwise
    protected boolean valueEquals(final BreakableObject<?> other) {
        return value.equals(other.value);
    }

    /// Returns a hash code value for the object.
    ///
    /// The behavior of this method can be modified by applying various
    /// hashCode-related breaks.
    ///
    /// @return a hash code value for this object
    /// @see Object#hashCode()
    @Override
    public int hashCode() {
        if (hasBreak(HASH_CODE_ALWAYS_RETURNS_ZERO)) {
            return 0;
        }
        if (hasBreak(HASH_CODE_IS_NOT_STABLE)) {
            return randomInt();
        }
        return value.hashCode();
    }

    /// Returns a string representation of the object.
    ///
    /// The behavior of this method can be modified by applying various
    /// toString-related breaks.
    ///
    /// @return a string representation of the object
    /// @see Object#toString()
    @Override
    public String toString() {
        if (hasBreak(TO_STRING_RETURNS_EMPTY_STRING)) {
            return "";
        }
        return value.toString();
    }

    /// Returns the underlying value wrapped by this object.
    ///
    /// @return the underlying value
    protected T value() {
        return this.value;
    }

    /// Serialization support for writing the object state.
    ///
    /// This method ensures that the object's fields, including the inherited
    /// behavioral modifications from [AbstractBreakable], are correctly serialized.
    ///
    /// @param out the [ObjectOutputStream] to write to
    /// @throws IOException if an I/O error occurs
    @Serial
    private void writeObject(final @NonNull ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    /// Serialization support for reading the object state.
    ///
    /// This method ensures that the object's fields, including the inherited
    /// behavioral modifications from [AbstractBreakable], are correctly restored
    /// during deserialization.
    ///
    /// @param in the [ObjectInputStream] to read from
    /// @throws IOException if an I/O error occurs
    /// @throws ClassNotFoundException if the class of a serialized object could not be found
    @Serial
    private void readObject(final @NonNull ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}
