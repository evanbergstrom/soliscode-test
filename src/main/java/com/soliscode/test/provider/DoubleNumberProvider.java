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
public interface DoubleNumberProvider<T extends Number> extends NumberProvider<T> {

    @Override
    default T defaultInstance() {
        return createValue(0.0);
    }

    /// Creates an instance of the double number class with a numeric value equal to the
    /// primitive `double` value.
    ///
    /// @param value the primitive double value.
    /// @return an instance of the double class.
    T createValue(double value);

    /// {@inheritDoc}
    @Override
    default T createInstance(final long seed) {
        return createValue((double) seed);
    }

    /// {@inheritDoc}
    @Override
    default T copyInstance(final T o) {
        return createValue(o.doubleValue());
    }

    /// The maximum double value that can be represented by the double class.
    /// @return the maximum double value.
    @SuppressWarnings("SameReturnValue")
    default double maxPrimitiveValue() {
        return Double.MAX_VALUE;
    }

    /// The minimum double value that can be represented by the double class.
    /// @return the minimum double value.
    @SuppressWarnings("SameReturnValue")
    default double minPrimitiveValue() {
        return Double.MIN_VALUE;
    }

    /// {@inheritDoc}
    @Override
    default T maxValue() {
        return createValue(maxPrimitiveValue());
    }

    /// {@inheritDoc}
    @Override
    default T minValue() {
        return createValue(minPrimitiveValue());
    }
}
