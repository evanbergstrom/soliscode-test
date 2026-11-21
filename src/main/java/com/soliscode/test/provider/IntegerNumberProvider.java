/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;


/// Provides instances of a class that implements the [Number] interface that represent an integer value for testing.
///
/// @param <T> The number.
/// @author evanbergstrom
/// @since 1.0;0
public interface IntegerNumberProvider<T extends Number> extends NumberProvider<T> {

    @Override
    default T defaultInstance() {
        return createValue(0);
    }

    /// Creates an instance of the integer number class with a numeric value equal to the
    /// primitive integer value.
    ///
    /// @param value the primitive integer value.
    /// @return an instance of the integer class.
    T createValue(long value);

    @Override
    default T createInstance(final long seed) {
        long value = seed;
        if (seed < minIntegerValue() || seed > maxIntegerValue()) {
            value = (seed > 0) ? seed % maxIntegerValue() : seed % minIntegerValue();
        }
        return createValue(value);
    }

    @Override
    default T copyInstance(final T o) {
        return createValue(o.longValue());
    }

    /// The maximum integer value that can be represented by the integer class.
    /// @return the maximum integer value.
    @SuppressWarnings("SameReturnValue")
    default long maxIntegerValue() {
        return Long.MAX_VALUE;
    }

    /// The minimum integer value that can be represented by the integer class.
    /// @return the minimum integer value.
    @SuppressWarnings("SameReturnValue")
    default long minIntegerValue() {
        return Long.MIN_VALUE;
    }

    /// {@inheritDoc}
    @Override
    default T maxValue() {
        return createValue(maxIntegerValue());
    }

    /// {@inheritDoc}
    @Override
    default T minValue() {
        return createValue(minIntegerValue());
    }
}
