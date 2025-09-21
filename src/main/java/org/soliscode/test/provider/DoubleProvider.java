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

/// Provides instances of `Double` for testing purposes.
///
/// This provider creates `Double` instances with values spanning the full range of
/// double-precision floating-point numbers. It implements `DoubleNumberProvider` to
/// provide floating-point specific functionality while maintaining all general provider capabilities.
///
/// ## Features
///
/// - **Full Range Support**: Can create any valid Double value including special values
/// - **IEEE 754 Compliance**: Handles all standard floating-point values and edge cases
/// - **Boundary Value Access**: Provides direct access to min/max representable values
/// - **Seed-Based Generation**: Deterministic instance creation from numeric seeds
/// - **Special Value Handling**: Supports NaN, positive/negative infinity, and zero
///
/// ## Floating-Point Considerations
///
/// This provider works with IEEE 754 double-precision floating-point numbers, which have:
/// - **Precision**: ~15-17 decimal digits
/// - **Range**: ±4.9e-324 to ±1.7976931348623157e+308
/// - **Special Values**: `NaN`, `POSITIVE_INFINITY`, `NEGATIVE_INFINITY`, `+0.0`, `-0.0`
///
/// ## Usage Examples
///
/// ### Basic Usage
/// ```java
/// DoubleProvider provider = new DoubleProvider();
///
/// // Create instances with different approaches
/// Double zero = provider.defaultInstance();        // 0.0
/// Double fromSeed = provider.createInstance(42);   // 42.0
/// Double pi = provider.createValue(Math.PI);       // 3.141592653589793
/// Double max = provider.maxValue();                // 1.7976931348623157E308
/// Double min = provider.minValue();                // 4.9E-324
/// ```
///
/// ### Special Values and Edge Cases
/// ```java
/// DoubleProvider provider = new DoubleProvider();
///
/// // Test with extreme and special values
/// Double nan = provider.createValue(Double.NaN);
/// Double posInf = provider.createValue(Double.POSITIVE_INFINITY);
/// Double negInf = provider.createValue(Double.NEGATIVE_INFINITY);
/// Double maxValue = provider.maxValue();
/// Double minValue = provider.minValue();
///
/// // Verify special value properties
/// assertTrue(Double.isNaN(nan));
/// assertTrue(Double.isInfinite(posInf));
/// ```
///
/// ### Precision Testing
/// ```java
/// DoubleProvider provider = new DoubleProvider();
///
/// // Generate test data for precision analysis
/// List<Double> precisionTests = provider.createUniqueInstances(100);
/// List<Double> randomTests = provider.createRandomInstances(50);
///
/// // Test floating-point arithmetic precision
/// for (Double value : precisionTests) {
///     double result = value * 2.0 / 2.0;
///     assertEquals(value, result, Double.MIN_VALUE);
/// }
/// ```
///
/// ## Implementation Details
///
/// This provider converts the seed parameter to a double value using type casting,
/// preserving the numeric relationship between seeds and generated values. This
/// approach maintains deterministic behavior while providing access to the full
/// range of double values.
///
/// @author evanbergstrom
/// @since 1.0.0
/// @see Double
/// @see DoubleNumberProvider
/// @see NumberProvider
public class DoubleProvider implements DoubleNumberProvider<Double> {

    /// Creates a new DoubleProvider instance.
    ///
    /// This constructor initializes the provider with default settings for creating
    /// Double instances across the full range of valid double-precision floating-point values.
    public DoubleProvider() { }

    /// Creates a `Double` instance with the specified primitive double value.
    ///
    /// This method performs direct boxing of the primitive double value into a Double object.
    /// It handles all valid double values including special IEEE 754 values such as NaN,
    /// positive infinity, negative infinity, and signed zeros.
    ///
    /// ## Supported Values
    ///
    /// This method accepts all valid double values:
    /// - **Normal values**: Standard floating-point numbers
    /// - **Subnormal values**: Very small numbers near zero
    /// - **Special values**: `NaN`, `POSITIVE_INFINITY`, `NEGATIVE_INFINITY`
    /// - **Signed zeros**: Both `+0.0` and `-0.0`
    ///
    /// ## Examples
    ///
    /// ```java
    /// DoubleProvider provider = new DoubleProvider();
    ///
    /// // Normal values
    /// Double zero = provider.createValue(0.0);           // 0.0
    /// Double pi = provider.createValue(3.14159);         // 3.14159
    /// Double negative = provider.createValue(-100.5);    // -100.5
    ///
    /// // Special values
    /// Double nan = provider.createValue(Double.NaN);                    // NaN
    /// Double posInf = provider.createValue(Double.POSITIVE_INFINITY);   // Infinity
    /// Double negInf = provider.createValue(Double.NEGATIVE_INFINITY);   // -Infinity
    ///
    /// // Extreme values
    /// Double max = provider.createValue(Double.MAX_VALUE);              // 1.7976931348623157E308
    /// Double min = provider.createValue(Double.MIN_VALUE);              // 4.9E-324
    /// ```
    ///
    /// @param value the primitive double value to box into a Double object
    /// @return a Double instance containing the specified value
    /// @complexity constant time
    @Override
    public @NonNull Double createValue(final double value) {
        return value;
    }
}
