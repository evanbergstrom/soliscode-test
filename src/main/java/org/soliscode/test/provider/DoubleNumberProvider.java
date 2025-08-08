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

import org.jetbrains.annotations.NotNull;

/// Provides instances of a class that implements the [Number] interface that represent an integer value for the
/// purposes of testing.
///
/// @param <T> The number.
/// @author evanbergstrom
/// @since 1.0;0
public interface DoubleNumberProvider<T extends Number> extends NumberProvider<T> {

    @Override
    default @NotNull T defaultInstance() {
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
    default @NotNull T createInstance(int seed) {
        return createValue(seed);
    }

    /// {@inheritDoc}
    @Override
    default @NotNull T copyInstance(@NotNull T o) {
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
    default @NotNull T maxValue() {
        return createValue(maxPrimitiveValue());
    }

    /// {@inheritDoc}
    default @NotNull T minValue() {
        return createValue(maxPrimitiveValue());
    }
}
