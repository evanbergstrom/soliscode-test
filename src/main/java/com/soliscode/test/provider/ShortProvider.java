/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.provider;


/// Provides instances of an [Short] for the purposes of testing.
///
/// @author evanbergstrom
/// @since 1.0
/// @see Short
public class ShortProvider implements IntegerNumberProvider<Short> {

    ///  Default constructor.
    public ShortProvider() { }

    /// Creates an instance of the [Short] class with a numeric value equal to the primitive short value.
    ///
    /// @param value the primitive short value.
    /// @return an instance of the `Short` class.
    @Override
    public Short createValue(final long value) {
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw new IllegalArgumentException("value (" + value + ") is not a valid Short value");
        }
        return (short) value;
    }

    /// {@inheritDoc}
    /// @return This method will return `Integer.MAX_VALUE`.
    @Override
    public long maxIntegerValue() {
        return Short.MAX_VALUE;
    }


    /// {@inheritDoc}
    /// @return This method will return `Integer.MIN_VALUE`.
    @Override
    public long minIntegerValue() {
        return Short.MIN_VALUE;
    }
}
