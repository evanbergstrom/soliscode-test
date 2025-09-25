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

import java.util.Objects;

/// Provides instances of `String` for testing purposes.
///
/// This provider creates `String` instances by converting numeric seed values to their
/// string representations. It's designed to generate predictable string values for
/// testing scenarios where string content needs to be deterministic and repeatable.
///
/// ## Features
///
/// - **Seed-Based Generation**: Creates strings from numeric seeds for predictability
/// - **Empty String Support**: Provides empty string as the default instance
/// - **Immutability Optimized**: Takes advantage of String immutability for efficient copying
/// - **Null Safety**: Prevents null values and provides clear error handling
///
/// ## Usage Examples
///
/// ### Basic String Creation
/// ```java
/// StringProvider provider = new StringProvider();
///
/// // Create strings with different approaches
/// String empty = provider.defaultInstance();         // ""
/// String zero = provider.createInstance(0);          // "0"
/// String positive = provider.createInstance(42);     // "42"
/// String negative = provider.createInstance(-100);   // "-100"
/// ```
///
/// ### Test Data Generation
/// ```java
/// StringProvider provider = new StringProvider();
///
/// // Generate predictable test data
/// List<String> equalStrings = provider.createEqualObjects(3);     // ["", "", ""]
/// List<String> uniqueStrings = provider.createUniqueInstances(5); // ["0", "1", "2", "3", "4"]
/// List<String> randomStrings = provider.createRandomInstances(3); // Random string values
/// ```
///
/// ### Copying and Validation
/// ```java
/// StringProvider provider = new StringProvider();
/// String original = "test";
///
/// // Copy operation (returns same instance due to immutability)
/// String copy = provider.copyInstance(original);
/// assertSame(original, copy);  // Same reference due to immutability
///
/// // Null safety validation
/// assertThrows(NullPointerException.class, () -> {
///     provider.copyInstance(null);
/// });
/// ```
///
/// ## String Generation Strategy
///
/// The provider uses `String.valueOf(seed)` to convert numeric seeds into string
/// representations. This approach ensures:
/// - Deterministic output for the same seed
/// - Human-readable string values for debugging
/// - Coverage of positive, negative, and zero values
///
/// @author evanbergstrom
/// @since 1.0
/// @see String
/// @see ObjectProvider
public class StringProvider implements ObjectProvider<String> {

    /// Creates a new StringProvider instance.
    ///
    /// This constructor initializes the provider for creating String instances
    /// from numeric seeds and handling string operations safely.
    public StringProvider() { }

    /// Returns the default String instance.
    ///
    /// The default instance is an empty string, which is useful for testing
    /// scenarios involving empty or uninitialized string values.
    ///
    /// ## Examples
    ///
    /// ```java
    /// StringProvider provider = new StringProvider();
    /// String defaultStr = provider.defaultInstance();  // ""
    /// assertTrue(defaultStr.isEmpty());
    /// ```
    ///
    /// @return an empty string (`""`)
    /// @complexity constant time
    @Override
    public @NonNull String defaultInstance() {
        return "";
    }

    /// Creates a String instance with a value equal to the string representation of the specified seed.
    ///
    /// This method converts the numeric seed to its string representation using
    /// `String.valueOf()`. The resulting string will contain the decimal representation
    /// of the seed value, including negative signs for negative numbers.
    ///
    /// ## Seed Conversion Examples
    ///
    /// | Seed Value | Resulting String |
    /// |------------|------------------|
    /// | `0`        | `"0"`           |
    /// | `42`       | `"42"`          |
    /// | `-100`     | `"-100"`        |
    /// | `9999999`  | `"9999999"`     |
    ///
    /// ## Usage Examples
    ///
    /// ```java
    /// StringProvider provider = new StringProvider();
    ///
    /// String zero = provider.createInstance(0);      // "0"
    /// String pos = provider.createInstance(123);     // "123"
    /// String neg = provider.createInstance(-456);    // "-456"
    /// String large = provider.createInstance(Long.MAX_VALUE);  // "9223372036854775807"
    /// ```
    ///
    /// @param seed the numeric seed value to convert to a string
    /// @return a String containing the decimal representation of the seed
    /// @complexity constant time
    @Override
    public @NonNull String createInstance(final long seed) {
        return String.valueOf(seed);
    }

    /// Creates a copy of the specified string value.
    ///
    /// Since `String` instances are immutable in Java, this method performs validation
    /// to ensure the input is not null, then returns the same instance. No actual
    /// copying is necessary due to the immutability guarantee.
    ///
    /// ## Immutability Benefits
    ///
    /// String immutability means:
    /// - No defensive copying required
    /// - Thread-safe by design
    /// - Memory efficient (same instance can be reused)
    /// - Predictable behavior in collections
    ///
    /// ## Examples
    ///
    /// ```java
    /// StringProvider provider = new StringProvider();
    /// String original = "test";
    /// String copy = provider.copyInstance(original);
    ///
    /// // Same reference due to immutability
    /// assertSame(original, copy);
    /// assertEquals("test", copy);
    /// ```
    ///
    /// @param other the string value to copy
    /// @return the same string instance (due to immutability)
    /// @throws NullPointerException if the argument is null
    /// @complexity constant time
    @Override
    public @NonNull String copyInstance(final @NonNull String other) {
        return Objects.requireNonNull(other);
    }
}
