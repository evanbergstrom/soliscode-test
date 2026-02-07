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

/// Provides instances of a class that implements the `Number` interface for the purposes of testing.
/// This interface extends `ObjectProvider` to add_singleElement_returnsTrueAndUpdatesSize number-specific functionality for retrieving
/// maximum and minimum values of numeric types.
///
/// ## Purpose
///
/// `NumberProvider` is designed to facilitate testing of numeric types by providing a consistent
/// interface for creating number instances and accessing their boundary values. This is particularly
/// useful for testing edge cases and boundary conditions in numeric operations.
///
/// ## Implementation Notes
///
/// Concrete implementations should provide efficient methods for creating number instances and
/// must implement the abstract methods `maxValue()` and `minValue()` to return the appropriate
/// boundary values for the specific numeric type.
///
/// ## Usage Examples
///
/// **Creating and using a custom provider:**
/// ```java
/// NumberProvider<Integer> intProvider = new IntegerProvider();
///
/// // Get boundary values
/// Integer max = intProvider.maxValue();  // Integer.MAX_VALUE
/// Integer min = intProvider.minValue();  // Integer.MIN_VALUE
///
/// // Create test instances
/// Integer zero = intProvider.defaultInstance();           // 0
/// Integer custom = intProvider.createInstance(42);        // 42
/// Integer copy = intProvider.copyInstance(custom);        // 42 (equal to original)
/// ```
///
/// **Testing boundary conditions:**
/// ```java
/// NumberProvider<Double> doubleProvider = new DoubleProvider();
///
/// // Test with extreme values
/// List<Double> testValues = Arrays.asList(
///     doubleProvider.minValue(),     // Double.MIN_VALUE
///     doubleProvider.defaultInstance(),  // 0.0
///     doubleProvider.maxValue()      // Double.MAX_VALUE
/// );
///
/// // Use in numeric operation tests
/// for (Double value : testValues) {
///     assertDoesNotThrow(() -> performCalculation(value));
/// }
/// ```
///
/// **Integration with test frameworks:**
/// ```java
/// @ParameterizedTest
/// @MethodSource("numberProvider")
/// void testNumericOperation(Number value) {
///     assertTrue(value.doubleValue() >= 0);
/// }
///
/// static Stream<Number> numberProvider() {
///     NumberProvider<Integer> provider = new IntegerProvider();
///     return Stream.of(
///         provider.minValue(),
///         provider.defaultInstance(),
///         provider.maxValue()
///     );
/// }
/// ```
///
/// @param <T> the specific `Number` subtype being provided (e.g., `Integer`, `Double`, `BigDecimal`)
/// @author evanbergstrom
/// @since 1.0
/// @see ObjectProvider
/// @see IntegerNumberProvider
/// @see DoubleNumberProvider
public interface NumberProvider<T extends Number> extends ObjectProvider<T> {

    /// Returns the maximum value representable by the `Number` implementation.
    ///
    /// This method provides access to the upper boundary of the numeric type,
    /// which is essential for testing edge cases and overflow conditions.
    ///
    /// ## Implementation Requirements
    ///
    /// - Must return a non-null value
    /// - Should return the mathematically largest value the type can represent
    /// - Must be consistent across multiple calls
    /// - The returned value should be equal to other instances created with the same maximum value
    ///
    /// ## Examples
    ///
    /// ```java
    /// NumberProvider<Integer> intProvider = new IntegerProvider();
    /// Integer maxInt = intProvider.maxValue();  // 2147483647
    ///
    /// NumberProvider<Double> doubleProvider = new DoubleProvider();
    /// Double maxDouble = doubleProvider.maxValue();  // 1.7976931348623157E308
    /// ```
    ///
    /// @return the maximum value that can be represented by this number type
    /// @implSpec implementations must ensure the returned value represents the maximum
    ///           possible value for the specific number type
    /// @complexity constant time
    @NonNull T maxValue();

    /// Returns the minimum value representable by the `Number` implementation.
    ///
    /// This method provides access to the lower boundary of the numeric type,
    /// which is essential for testing edge cases and underflow conditions.
    ///
    /// ## Implementation Requirements
    ///
    /// - Must return a non-null value
    /// - Should return the mathematically smallest value the type can represent
    /// - Must be consistent across multiple calls
    /// - The returned value should be equal to other instances created with the same minimum value
    ///
    /// ## Behavior Notes
    ///
    /// For signed integer types, this returns the most negative value (e.g., `Integer.MIN_VALUE`).
    /// For floating-point types, this may return the smallest positive normal value
    /// (e.g., `Double.MIN_VALUE`) or the most negative value depending on the implementation.
    ///
    /// ## Examples
    ///
    /// ```java
    /// NumberProvider<Integer> intProvider = new IntegerProvider();
    /// Integer minInt = intProvider.minValue();  // -2147483648
    ///
    /// NumberProvider<Long> longProvider = new LongProvider();
    /// Long minLong = longProvider.minValue();   // -9223372036854775808L
    /// ```
    ///
    /// @return the minimum value that can be represented by this number type
    /// @implSpec implementations must ensure the returned value represents the minimum
    ///           possible value for the specific number type
    /// @complexity constant time
    @NonNull T minValue();
}
