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

/// Provides instances of a class that implements the [Number] interface that represent an integer value for the
/// purposes of testing.
///
/// @param <T> The number.
/// @author evanbergstrom
/// @since 1.0;0
public interface IntegerNumberProvider<T extends Number> extends NumberProvider<T> {

    @Override
    default @NonNull T defaultInstance() {
        return createValue(0);
    }

    /// Creates an instance of the integer number class with a numeric value equal to the
    /// primitive integer value.
    ///
    /// @param value the primitive integer value.
    /// @return an instance of the integer class.
    T createValue(long value);

    @Override
    default @NonNull T createInstance(final long seed) {
        long value = seed;
        if (seed < minIntegerValue() || seed > maxIntegerValue()) {
            value = (seed > 0) ? seed % maxIntegerValue() : seed % minIntegerValue();
        }
        return createValue(value);
    }

    @Override
    default @NonNull T copyInstance(final @NonNull T o) {
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
    default @NonNull T maxValue() {
        return createValue(maxIntegerValue());
    }

    /// {@inheritDoc}
    default @NonNull T minValue() {
        return createValue(minIntegerValue());
    }
}
