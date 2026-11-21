/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.interfaces;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

/// The `ObjectOnly` class wraps any object and only exposes the methods on the [Object] interface. This is used for
/// testing a class or method implementation that operates on an `Object` without any optimizations for the
/// instantiated class or any other implemented interfaces.
///
/// Objects of this class should be created using the [Interfaces] utility class:
/// ```java
///     String s = "a string value";
///     Object obj = Interfaces.objectOnly(s);
/// ```
///
/// @author evanbergstrom
/// @see Interfaces
/// @since 1.0
public class ObjectOnly {

    private final Object obj;

    /// Creates an instance of ObjectOnly. The instance created will wrap the object Integer(0).
    public ObjectOnly() {
        this.obj = 0;
    }

    /// Copies and instance of ObjectOnly.
    /// @param other the instance to copy.
    public ObjectOnly(final ObjectOnly other) {
        this.obj = Objects.requireNonNull(other).obj;
    }

    /// Creates an `ObjectOnly` object from any other object.
    /// @param objArg the object whose interface is being restricted.
    public ObjectOnly(final Object objArg) {
        this.obj = Objects.requireNonNull(objArg);
    }

    /// {@inheritDoc}
    @Override
    public int hashCode() {
        return obj.hashCode();
    }

    /// {@inheritDoc}
    @Override
    public boolean equals(final @Nullable Object o) {
        if (o instanceof ObjectOnly only) {
            return obj.equals(only.obj);
        } else {
            return false;
        }
    }

    /// {@inheritDoc}
    @Override
    public String toString() {
        return obj.toString();
    }
}
