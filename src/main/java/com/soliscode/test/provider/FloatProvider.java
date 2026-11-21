/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;


/// Provides instances of the class [Float] for the purposes of testing.
///
/// @author evanbergstrom
/// @since 1.0.0
public class FloatProvider implements DoubleNumberProvider<Float> {

    ///  Default constructor.
    public FloatProvider() { }

    /// Creates an instance of the [Float] class with a numeric value equal to the
    /// primitive float value.
    ///
    /// @param value the primitive float value.
    /// @return an instance of the `Float` class.
    @Override
    public Float createValue(final double value) {
        return (float) value;
    }

    // The maximum double value that can be represented by the double class.
    /// @return the maximum double value.
    @SuppressWarnings("SameReturnValue")
    @Override
    public double maxPrimitiveValue() {
        return Float.MAX_VALUE;
    }

    /// The minimum double value that can be represented by the double class.
    /// @return the minimum double value.
    @SuppressWarnings("SameReturnValue")
    @Override
    public double minPrimitiveValue() {
        return Float.MIN_VALUE;
    }
}
