/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.interfaces;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

/// The `NumberOnly` lass wraps any object and only exposes the methods on the [Number] interface. This is used for
/// testing a class or method implementation that operates on a `Number` without any optimizations for the
/// instantiated class or any other implemented interfaces.
///
/// Objects of this class should be created using the [Interfaces] utility class:
/// ```java
///     Integer i = 3;
///     Number n = Interfaces.numberOnly(i);
/// ```
///
/// @author evanbergstrom
/// @see Interfaces
/// @since 1.0
public class NumberOnly extends Number {

    /// The value of the Number object.
    private final Number number;

    /// Creates an instance of `NumberOnly` that wraps an instance of `Number` and narrows the interface to only the
    /// methods available in the `Number` interface.
    /// @param numberArg an instance of number to wrap.
    public NumberOnly(final Number numberArg) {
        if (numberArg instanceof NumberOnly other) {
            this.number = other.number;
        } else {
            this.number = numberArg;
        }
    }

    /// {@inheritDoc}
    @Override
    public int hashCode() {
        return number.hashCode();
    }

    /// {@inheritDoc}
    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj instanceof NumberOnly only) {
            return number.equals(only.number);
        } else if (obj instanceof Number n) {
            return number.equals(n);
        } else {
            return false;
        }
    }

    /// {@inheritDoc}
    @Override
    public String toString() {
        return number.toString();
    }

    /// {@inheritDoc}
    @Override
    public int intValue() {
        return number.intValue();
    }

    /// {@inheritDoc}
    @Override
    public long longValue() {
        return number.longValue();
    }

    /// {@inheritDoc}
    @Override
    public float floatValue() {
        return number.floatValue();
    }

    /// {@inheritDoc}
    @Override
    public double doubleValue() {
        return number.doubleValue();
    }

    /// Creates a `List` of `NumberOnly` values based upon a list of integer constants.
    /// @param values The integer values.
    /// @return a list of `NumberOnly` values.
    public static List<NumberOnly> listOf(final int... values) {
        List<NumberOnly> result = new ArrayList<>(values.length);
        for (final int i : values) {
            result.add(new NumberOnly(i));
        }
        return result;
    }
}
