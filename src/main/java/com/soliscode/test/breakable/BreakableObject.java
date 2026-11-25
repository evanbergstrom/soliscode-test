/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.jspecify.annotations.Nullable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;

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
/// assertTrue(breakable.equals(new BreakableObject<>("test")));
///
/// breakable.addBreak(BreakableObject.EQUALS_ALWAYS_RETURNS_FALSE);
/// assertFalse(breakable.equals(breakable)); // equals now returns false, even for the object itself
/// ```
///
/// ## Thread Safety
/// This class is thread-safe as it inherits from [AbstractBreakable], which
/// uses a mutex for synchronizing access to the set of active breaks.
///
/// @param <T> the type of the underlying object
/// @since 0.1.0
public class BreakableObject<T> extends AbstractBreakable implements Serializable {

    /// A break that causes `equals(Object)` to always return `true`,
    /// violating the reflexive, symmetric, and transitive properties.
    /// **Affected Methods:** [equals(Object)][#equals(Object)]
    public static final Break EQUALS_ALWAYS_RETURNS_TRUE =
            new Break("The method equals(Object) always returns true");

    /// A break that causes `equals(Object)` to always return `false`,
    /// even when comparing an object to itself.
    /// **Affected Methods:** [equals(Object)][#equals(Object)]
    public static final Break EQUALS_ALWAYS_RETURNS_FALSE =
            new Break("The method equals(Object) always returns false");

    /// A break that causes `equals(Object)` to return the logical negation
    /// of the actual equality result.
    /// **Affected Methods:** [equals(Object)][#equals(Object)]
    public static final Break EQUALS_ALWAYS_RETURNS_OPPOSITE_VALUE =
            new Break("The method equals(Object) always returns the opposite value");

    /// A break that causes `equals(Object)` to throw a `ClassCastException`
    /// when the other object's class does not match this object's class.
    /// **Affected Methods:** [equals(Object)][#equals(Object)]
    public static final Break EQUALS_THROWS_FOR_OTHER_TYPES =
            new Break("The method equals(Object) throws exception when type does not match");

    /// A break that causes `hashCode()` to always return `0`.
    /// **Affected Methods:** [hashCode()][#hashCode()]
    public static final Break HASH_CODE_ALWAYS_RETURNS_ZERO =
            new Break("The method hashCode() always returns zero");

    /// A break that causes `hashCode()` to return a random value on each call,
    /// violating the stability requirement of `hashCode()`.
    /// **Affected Methods:** [hashCode()][#hashCode()]
    public static final Break HASH_CODE_IS_NOT_STABLE =
            new Break("The method hashCode() has an unstable return value");

    /// A break that causes `toString()` to always return an empty string.
    /// **Affected Methods:** [toString()][#toString()]
    public static final Break TO_STRING_RETURNS_EMPTY_STRING =
            new Break("The method toString() returns the empty string");

    /// A break that makes `equals(Object)` not symmetric: between two distinct instances of one class with equal
    /// values, it holds only from the instance made first to the one made later, by their serials (#70).
    ///
    /// Each instance takes a serial when it is made, or read from a stream, from one counter. The break acts only on
    /// two instances of one class; against an object of another class the result is left alone.
    /// **Affected Methods:** [equals(Object)][#equals(Object)]
    /// @since 0.1.7
    public static final Break EQUALS_IS_NOT_SYMMETRIC =
            new Break("The method equals(Object) holds only from an earlier instance to a later one");

    /// A break that makes `equals(Object)` not transitive: between two distinct instances of one class with equal
    /// values, it holds only when their serials differ by one (#70). It stays reflexive and symmetric.
    ///
    /// For a value and two copies made one after another, on one thread, `x.equals(y)` and `y.equals(z)` hold and
    /// `x.equals(z)` does not. Instances made in between, by another thread or by a provider that makes more than one
    /// per call, widen the gaps, and a copy may then be unequal to its original, a different fault. The break acts
    /// only on two instances of one class; against an object of another class the result is left alone.
    /// **Affected Methods:** [equals(Object)][#equals(Object)]
    /// @since 0.1.7
    public static final Break EQUALS_IS_NOT_TRANSITIVE =
            new Break("The method equals(Object) holds only between instances made one after another");

    /// A break that makes `equals(Object)` not consistent: when an instance is compared with the same distinct, equal
    /// instance of its class several times in a row, the results alternate, `true`, `false`, `true`, and so on (#70).
    ///
    /// Each instance remembers only the run of comparisons it is in: any other comparison, with an unequal value,
    /// another instance, another class or `null`, ends the run. So the first comparison of a run holds, and the break
    /// stays reflexive, transitive, and symmetric on each direction's first call: `x.equals(y)` and `y.equals(x)`
    /// each hold on their first call. As for
    /// any `equals` that is not consistent, the call that returns `false` disagrees for that moment with the other
    /// direction, whose own run is apart: after `x.equals(y)` twice, `y.equals(x)` still holds once. The
    /// memory is the instance's own, not guarded, so the break is meant for a single-threaded test. It acts only on
    /// two instances of one class; against an object of another class the result is left alone.
    /// **Affected Methods:** [equals(Object)][#equals(Object)]
    /// @since 0.1.7
    public static final Break EQUALS_IS_NOT_CONSISTENT =
            new Break("The method equals(Object) alternates on repeated calls with the same equal instance");

    /// A break that makes `hashCode()` differ for equal instances: it adds the instance's serial to the value's hash
    /// code, so that it is stable for an instance and differs for its copy (#70).
    /// **Affected Methods:** [hashCode()][#hashCode()]
    /// @since 0.1.7
    public static final Break HASH_CODE_DIFFERS_FOR_EQUAL_VALUES =
            new Break("The method hashCode() returns different values for equal instances");

    /// A break that makes `toString()` return a different string on each call: the value's string followed by a
    /// number that rises on every call (#70).
    /// **Affected Methods:** [toString()][#toString()]
    /// @since 0.1.7
    public static final Break TO_STRING_IS_NOT_STABLE =
            new Break("The method toString() returns a different string on each call");

    /// A break that causes `toString()` to return `null`, which [Object#toString()] does not allow (#70).
    /// **Affected Methods:** [toString()][#toString()]
    /// @since 0.1.7
    public static final Break TO_STRING_RETURNS_NULL =
            new Break("The method toString() returns null");

    /// A break that causes `toString()` to return [Object]'s own form, the class name, `@` and the hash code in
    /// hexadecimal, as if the method were not overridden (#70).
    /// **Affected Methods:** [toString()][#toString()]
    /// @since 0.1.7
    public static final Break TO_STRING_IS_NOT_OVERRIDDEN =
            new Break("The method toString() returns Object's default form");

    @Serial
    private static final long serialVersionUID = 1L;

    /// The counter of the instances' serials.
    private static final AtomicLong SERIALS = new AtomicLong();


    /// The counter of the calls to `toString` under [#TO_STRING_IS_NOT_STABLE].
    private static final AtomicLong TO_STRING_CALLS = new AtomicLong();

    /// The underlying value wrapped by this breakable object.
    private final T value;

    /// The order in which this instance was made or read from a stream, which the serial breaks read. It is not
    /// serialized, so the serialized form is that of 0.1.4 (#59); `readObject` and [#clone()] give a new one.
    private transient long serial;

    /// The serial of the instance this one was last compared with under [#EQUALS_IS_NOT_CONSISTENT], or -1.
    private transient long lastCompared = -1;

    /// Whether the last comparison with that instance returned `false` under [#EQUALS_IS_NOT_CONSISTENT].
    private transient boolean lastWasFalse;

    /// Creates a new `BreakableObject` by copying the state of another one.
    ///
    /// @param other the `BreakableObject` to copy
    public BreakableObject(final BreakableObject<T> other) {
        this(other.value, new HashSet<>(other.breaks()), new HashMap<>(other.methodStatuses()), other.isSafe());
    }

    /// Creates a new `BreakableObject` with the given value.
    ///
    /// @param valueArg the value to wrap
    public BreakableObject(final T valueArg) {
        this(valueArg, new HashSet<>(), new HashMap<>(), true);
    }

    /// Internal constructor for creating a `BreakableObject` with a specific state.
    ///
    /// @param valueArg the underlying value
    /// @param breaks the initial set of breaks, copied
    /// @param methodStatuses the initial method statuses
    /// @param isSafe the initial safety state
    protected BreakableObject(final T valueArg, final Set<Break> breaks,
                              final Map<InterfaceMethod, MethodStatus> methodStatuses,
                              final boolean isSafe) {
        super(breaks, methodStatuses, isSafe);
        this.value = valueArg;
        this.serial = SERIALS.getAndIncrement();
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
    // EqualsGetClass: equality is decided per exact class on purpose. An object of another class (a subclass
    // included) is compared by valueEqualsOtherType, and EQUALS_THROWS_FOR_OTHER_TYPES fires for it; with
    // instanceof, a subclass would compare through valueEquals one way and valueEqualsOtherType the other way,
    // and equals would not be symmetric.
    @SuppressWarnings("EqualsGetClass")
    @Override
    public boolean equals(final @Nullable Object o) {
        return getWithSafety(() -> {
            if (hasBreak(EQUALS_ALWAYS_RETURNS_TRUE)) {
                return true;
            }
            if (hasBreak(EQUALS_ALWAYS_RETURNS_FALSE)) {
                return false;
            }
            final long previous = takeLastCompared();
            boolean result;
            if (o == null) {
                result = false;
            } else if (getClass() != o.getClass()) {
                if (hasBreak(EQUALS_THROWS_FOR_OTHER_TYPES)) {
                    throw new ClassCastException();
                }
                result = valueEqualsOtherType(o);
            } else {
                BreakableObject<?> other = (BreakableObject<?>) o;
                result = valueEquals(other) && serialsAllowEquality(other, previous);
            }
            if (hasBreak(EQUALS_ALWAYS_RETURNS_OPPOSITE_VALUE)) {
                return !result;
            }
            return result;
        });
    }

    /// Returns whether the serial and consistency breaks let this instance equal another of its class with an equal
    /// value.
    ///
    /// @param other another instance of this class
    /// @param previous the serial of the instance [#EQUALS_IS_NOT_CONSISTENT]'s run was with, or -1
    /// @return `false` where [#EQUALS_IS_NOT_SYMMETRIC], [#EQUALS_IS_NOT_TRANSITIVE] or [#EQUALS_IS_NOT_CONSISTENT]
    ///     makes the two unequal
    // ReferenceEquality: an instance always equals itself, whatever the serial breaks say.
    @SuppressWarnings("ReferenceEquality")
    private boolean serialsAllowEquality(final BreakableObject<?> other, final long previous) {
        if (other == this) {
            return true;
        }
        if (hasBreak(EQUALS_IS_NOT_SYMMETRIC) && serial > other.serial) {
            return false;
        }
        if (hasBreak(EQUALS_IS_NOT_TRANSITIVE) && Math.abs(serial - other.serial) != 1) {
            return false;
        }
        return !hasBreak(EQUALS_IS_NOT_CONSISTENT) || alternates(other, previous);
    }

    /// Ends the run of comparisons [#EQUALS_IS_NOT_CONSISTENT] remembers, and returns the serial of the instance it
    /// was with: every comparison ends it, and only one with an equal, distinct instance of this class starts another.
    ///
    /// @return the serial of the instance last compared with, or -1
    private long takeLastCompared() {
        if (!hasBreak(EQUALS_IS_NOT_CONSISTENT)) {
            return -1;
        }
        long previous = lastCompared;
        lastCompared = -1;
        return previous;
    }

    /// Returns `true` on the first of a run of comparisons with the same instance, then `false`, `true`, and so on.
    ///
    /// @param other another instance of this class
    /// @param previous the serial of the instance the run so far was with, or -1
    /// @return whether this comparison holds under [#EQUALS_IS_NOT_CONSISTENT]
    private boolean alternates(final BreakableObject<?> other, final long previous) {
        lastWasFalse = other.serial == previous && !lastWasFalse;
        lastCompared = other.serial;
        return !lastWasFalse;
    }

    /// Compares the underlying values of this object and another `BreakableObject`.
    ///
    /// @param other the other `BreakableObject` to compare
    /// @return `true` if the underlying values are equal, `false` otherwise
    protected boolean valueEquals(final BreakableObject<?> other) {
        return value.equals(other.value);
    }

    /// Returns whether this object's value equals an object of another class. The default is `false`; a type whose
    /// interface defines equality across implementations (a [java.util.Set], a [java.util.List]) overrides it.
    ///
    /// @param other an object, not null, of another class than this one
    /// @return whether the two are equal
    protected boolean valueEqualsOtherType(final Object other) {
        return false;
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
        return getWithSafety(() -> {
            if (hasBreak(HASH_CODE_ALWAYS_RETURNS_ZERO)) {
                return 0;
            }
            if (hasBreak(HASH_CODE_IS_NOT_STABLE)) {
                return randomInt();
            }
            if (hasBreak(HASH_CODE_DIFFERS_FOR_EQUAL_VALUES)) {
                return value.hashCode() + (int) serial;
            }
            return value.hashCode();
        });
    }

    /// Returns a string representation of the object.
    ///
    /// The behavior of this method can be modified by applying various
    /// toString-related breaks.
    ///
    /// @return a string representation of the object, or `null` under [#TO_STRING_RETURNS_NULL]
    /// @see Object#toString()
    // NullAway: TO_STRING_RETURNS_NULL returns null on purpose, which Object.toString does not allow.
    @SuppressWarnings("NullAway")
    @Override
    public String toString() {
        return getWithSafety(() -> {
            if (hasBreak(TO_STRING_RETURNS_NULL)) {
                return null;
            }
            if (hasBreak(TO_STRING_RETURNS_EMPTY_STRING)) {
                return "";
            }
            if (hasBreak(TO_STRING_IS_NOT_STABLE)) {
                return value.toString() + TO_STRING_CALLS.getAndIncrement();
            }
            if (hasBreak(TO_STRING_IS_NOT_OVERRIDDEN)) {
                return getClass().getName() + '@' + Integer.toHexString(hashCode());
            }
            return value.toString();
        });
    }

    /// Returns a copy made by [AbstractBreakable#clone()], with a serial of its own, so that the serial breaks tell a
    /// clone from its original as they tell a copy. Only a subclass that implements [Cloneable] can be cloned.
    ///
    /// @return the copy
    /// @throws CloneNotSupportedException if this object's class does not implement [Cloneable]
    @Override
    protected Object clone() throws CloneNotSupportedException {
        BreakableObject<?> copy = (BreakableObject<?>) super.clone();
        copy.serial = SERIALS.getAndIncrement();
        copy.lastCompared = -1;
        copy.lastWasFalse = false;
        return copy;
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
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    /// Serialization support for reading the object state.
    ///
    /// This method ensures that the object's fields, including the inherited
    /// behavioral modifications from [AbstractBreakable], are correctly restored
    /// during deserialization, and gives the instance a new serial, which is not serialized.
    ///
    /// @param in the [ObjectInputStream] to read from
    /// @throws IOException if an I/O error occurs
    /// @throws ClassNotFoundException if the class of a serialized object could not be found
    @Serial
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        serial = SERIALS.getAndIncrement();
        lastCompared = -1;
    }
}
