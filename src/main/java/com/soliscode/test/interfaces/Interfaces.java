/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.interfaces;


import java.util.Collection;

/// Utility class that helps create interface-narrowing views on objects.
///
/// ## Thread Safety
///
/// This class is thread-safe. All methods are static and stateless, making concurrent
/// access safe from multiple threads. The returned interface-narrowing wrapper objects
/// are also thread-safe as they are immutable views of the wrapped objects.
///
/// @author evanbergstrom
/// @since 1.0
public final class Interfaces {

    private Interfaces() { }

    /// Wraps the object so that only the methods in the [Object] interface are available.
    /// @param obj the object to wrap.
    /// @return the wrapped object.
    public static ObjectOnly narrowToObject(final Object obj) {
        return new ObjectOnly(obj);
    }

    /// Wraps the object so that only the methods in the [Number] interface are available.
    /// @param n the number to wrap.
    /// @return the wrapped number.
    public static NumberOnly narrowToNumber(final Number n) {
        return new NumberOnly(n);
    }

    /// Wraps the object so that only the methods in the [Iterable] interface are available.
    /// @param <E> the element type for the iterable.
    /// @param i the iterable to wrap
    /// @return the wrapped iterable.
    public static <E> IterableOnly<E> narrowToIterable(final Iterable<E> i) {
        return new IterableOnly<>(i);
    }

    /// Wraps the object so that only the methods in the [Collection] interface are available.
    /// @param <E> the element type for the iterable.
    /// @param i the iterable to wrap
    /// @return the wrapped iterable.
    public static <E> CollectionOnly<E> narrowToCollection(final Collection<E> i) {
        return new CollectionOnly<>(i);
    }
}
