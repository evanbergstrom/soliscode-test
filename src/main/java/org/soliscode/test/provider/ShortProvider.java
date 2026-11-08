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

package org.soliscode.test.provider;

import org.jspecify.annotations.NonNull;

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
    public @NonNull Short createValue(final long value) {
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
