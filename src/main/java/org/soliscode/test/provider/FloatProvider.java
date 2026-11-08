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
    public @NonNull Float createValue(final double value) {
        return (float) value;
    }

    // The maximum double value that can be represented by the double class.
    /// @return the maximum double value.
    @SuppressWarnings("SameReturnValue")
    public double maxPrimitiveValue() {
        return Float.MAX_VALUE;
    }

    /// The minimum double value that can be represented by the double class.
    /// @return the minimum double value.
    @SuppressWarnings("SameReturnValue")
    public double minPrimitiveValue() {
        return Float.MIN_VALUE;
    }
}
