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

/// Provides instances of `Integer` for testing purposes.
///
/// This provider creates `Integer` instances with values ranging from `Integer.MIN_VALUE`
/// to `Integer.MAX_VALUE`. It implements `IntegerNumberProvider` to provide integer-specific
/// functionality while maintaining all the general object provider capabilities.
///
/// ## Features
///
/// - **Full Range Support**: Can create any valid Integer value
/// - **Boundary Value Access**: Provides direct access to min/max values
/// - **Seed-Based Generation**: Deterministic instance creation from seeds
/// - **Type Safety**: Validates input values to prevent overflow
///
/// ## Usage Examples
///
/// ### Basic Usage
/// ```java
/// IntegerProvider provider = new IntegerProvider();
///
/// // Create instances with different approaches
/// Integer zero = provider.defaultInstance();        // 0
/// Integer fromSeed = provider.createInstance(42);   // 42
/// Integer max = provider.maxValue();                // 2147483647
/// Integer min = provider.minValue();                // -2147483648
/// ```
///
/// ### Boundary Testing
/// ```java
/// IntegerProvider provider = new IntegerProvider();
///
/// // Test edge cases
/// Integer maxValue = provider.maxValue();
/// Integer minValue = provider.minValue();
///
/// // Verify boundaries
/// assertEquals(Integer.MAX_VALUE, maxValue.intValue());
/// assertEquals(Integer.MIN_VALUE, minValue.intValue());
/// ```
///
/// ### Collection Generation
/// ```java
/// IntegerProvider provider = new IntegerProvider();
///
/// // Generate test data sets
/// List<Integer> equalValues = provider.createEqualObjects(5);     // [0, 0, 0, 0, 0]
/// List<Integer> uniqueValues = provider.createUniqueInstances(5); // [0, 1, 2, 3, 4]
/// List<Integer> randomValues = provider.createRandomInstances(5); // Random integers
/// ```
///
/// ## Implementation Details
///
/// This provider uses the integer value directly from the seed parameter, with validation
/// to ensure the value fits within the Integer range. Values outside the valid range will
/// cause an `IllegalArgumentException` to be thrown.
///
/// @author evanbergstrom
/// @since 1.0
/// @see Integer
/// @see IntegerNumberProvider
/// @see NumberProvider
public class IntegerProvider implements IntegerNumberProvider<Integer> {

    /// Creates a new IntegerProvider instance.
    ///
    /// This constructor initializes the provider with default settings for creating
    /// Integer instances within the full range of valid integer values.
    public IntegerProvider() { }

    /// Creates an `Integer` instance with a numeric value equal to the specified long value.
    ///
    /// This method performs range validation to ensure the provided value can be
    /// represented as a valid `Integer`. Values outside the valid range will result
    /// in an exception.
    ///
    /// ## Value Range
    ///
    /// Valid input values must be within the range:
    /// - Minimum: `-2,147,483,648` (`Integer.MIN_VALUE`)
    /// - Maximum: `2,147,483,647` (`Integer.MAX_VALUE`)
    ///
    /// ## Examples
    ///
    /// ```java
    /// IntegerProvider provider = new IntegerProvider();
    ///
    /// Integer zero = provider.createValue(0);                    // 0
    /// Integer positive = provider.createValue(42);               // 42
    /// Integer negative = provider.createValue(-100);             // -100
    /// Integer max = provider.createValue(Integer.MAX_VALUE);     // 2147483647
    ///
    /// // This would throw an exception:
    /// // provider.createValue(Long.MAX_VALUE);  // IllegalArgumentException
    /// ```
    ///
    /// @param value the primitive long value to convert to an Integer
    /// @return an Integer instance with the specified value
    /// @throws IllegalArgumentException if the value is outside the valid Integer range
    /// @complexity constant time
    @Override
    public @NonNull Integer createValue(final long value) {
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("value (" + value + ") is not a valid Integer value");
        }
        return (int) value;
    }

    /// Returns the maximum integer value that can be represented by this provider.
    ///
    /// This method returns `Integer.MAX_VALUE` which represents the largest value
    /// that can be stored in a 32-bit signed integer.
    ///
    /// @return `Integer.MAX_VALUE` (2,147,483,647)
    /// @complexity constant time
    @Override
    public long maxIntegerValue() {
        return Integer.MAX_VALUE;
    }

    /// Returns the minimum integer value that can be represented by this provider.
    ///
    /// This method returns `Integer.MIN_VALUE` which represents the smallest value
    /// that can be stored in a 32-bit signed integer.
    ///
    /// @return `Integer.MIN_VALUE` (-2,147,483,648)
    /// @complexity constant time
    @Override
    public long minIntegerValue() {
        return Integer.MIN_VALUE;
    }
}
