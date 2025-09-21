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

/// Provides instances of `Long` for testing purposes.
///
/// This provider creates `Long` instances with values ranging from `Long.MIN_VALUE` to
/// `Long.MAX_VALUE`. It implements `IntegerNumberProvider` to provide integer-specific
/// functionality while maintaining all the general object provider capabilities for 64-bit
/// signed integer values.
///
/// ## Features
///
/// - **Full 64-bit Range Support**: Can create any valid Long value
/// - **Extended Integer Range**: Supports values far beyond 32-bit integer limits
/// - **Boundary Value Access**: Provides direct access to min/max values
/// - **Seed-Based Generation**: Deterministic instance creation from seeds
/// - **Type Safety**: Direct primitive-to-object boxing without overflow concerns
///
/// ## Range Characteristics
///
/// Long values provide 64-bit signed integer storage with:
/// - **Range**: -9,223,372,036,854,775,808 to 9,223,372,036,854,775,807
/// - **Precision**: Exact integer representation (no floating-point approximation)
/// - **Performance**: Efficient arithmetic operations on modern 64-bit systems
/// - **Compatibility**: Direct compatibility with `long` primitive operations
///
/// ## Usage Examples
///
/// ### Basic Usage
/// ```java
/// LongProvider provider = new LongProvider();
///
/// // Create instances with different approaches
/// Long zero = provider.defaultInstance();        // 0L
/// Long fromSeed = provider.createInstance(42);   // 42L
/// Long large = provider.createInstance(1000000); // 1000000L
/// Long max = provider.maxValue();                // 9223372036854775807L
/// Long min = provider.minValue();                // -9223372036854775808L
/// ```
///
/// ### Large Number Testing
/// ```java
/// LongProvider provider = new LongProvider();
///
/// // Test with values beyond Integer range
/// Long beyondInt = provider.createValue(3_000_000_000L);  // > Integer.MAX_VALUE
/// Long negative = provider.createValue(-5_000_000_000L);  // < Integer.MIN_VALUE
///
/// // Verify range capabilities
/// assertTrue(beyondInt > Integer.MAX_VALUE);
/// assertTrue(negative < Integer.MIN_VALUE);
/// ```
///
/// ### Boundary and Edge Case Testing
/// ```java
/// LongProvider provider = new LongProvider();
///
/// // Test extreme values
/// Long maxValue = provider.maxValue();
/// Long minValue = provider.minValue();
///
/// // Test near-boundary values
/// Long nearMax = provider.createValue(Long.MAX_VALUE - 1);
/// Long nearMin = provider.createValue(Long.MIN_VALUE + 1);
///
/// // Verify arithmetic behavior near boundaries
/// assertEquals(Long.MAX_VALUE, nearMax + 1);
/// assertEquals(Long.MIN_VALUE, nearMin - 1);
/// ```
///
/// ### Collection Generation for Large Data Sets
/// ```java
/// LongProvider provider = new LongProvider();
///
/// // Generate large sequences for performance testing
/// List<Long> equalValues = provider.createEqualObjects(1000);      // [0L, 0L, ...]
/// List<Long> uniqueValues = provider.createUniqueInstances(1000);  // [0L, 1L, 2L, ...]
/// List<Long> randomValues = provider.createRandomInstances(1000);  // Random long values
///
/// // Test with timestamp-like values
/// List<Long> timestamps = provider.createUniqueInstances(100, System.currentTimeMillis());
/// ```
///
/// ### Integration with Time-Based Testing
/// ```java
/// LongProvider provider = new LongProvider();
///
/// // Generate timestamp-like values for temporal testing
/// long baseTime = System.currentTimeMillis();
/// List<Long> timeSequence = IntStream.range(0, 10)
///     .mapToObj(i -> provider.createValue(baseTime + i * 1000))  // 1 second intervals
///     .collect(Collectors.toList());
///
/// // Test chronological ordering
/// for (int i = 1; i < timeSequence.size(); i++) {
///     assertTrue(timeSequence.get(i) > timeSequence.get(i - 1));
/// }
/// ```
///
/// ## Implementation Details
///
/// This provider directly uses the seed parameter as the long value through primitive
/// boxing. Since the seed parameter is already a `long`, no conversion or range checking
/// is necessary, making this provider highly efficient for large-scale data generation.
///
/// ## Use Cases
///
/// Long providers are particularly useful for:
/// - **High-precision counting**: Sequence numbers, identifiers
/// - **Time-based testing**: Timestamps, durations, intervals
/// - **Large dataset simulation**: Database keys, record counts
/// - **Memory/performance testing**: Large numeric computations
/// - **Range boundary testing**: Values beyond 32-bit integer limits
///
/// @author evanbergstrom
/// @since 1.0
/// @see Long
/// @see IntegerNumberProvider
/// @see NumberProvider
public class LongProvider implements IntegerNumberProvider<Long> {

    /// Creates a new LongProvider instance.
    ///
    /// This constructor initializes the provider with default settings for creating
    /// Long instances within the full range of valid 64-bit signed integer values.
    public LongProvider() { }

    /// Creates a `Long` instance with a numeric value equal to the specified primitive long value.
    ///
    /// This method performs direct boxing of the primitive long value into a Long object.
    /// Since the input parameter is already a `long`, no range validation or conversion
    /// is necessary, making this operation highly efficient.
    ///
    /// ## Direct Value Mapping
    ///
    /// The method provides a direct one-to-one mapping between input and output:
    /// - Input `0L` produces `Long.valueOf(0L)`
    /// - Input `Long.MAX_VALUE` produces `Long.valueOf(Long.MAX_VALUE)`
    /// - Input `Long.MIN_VALUE` produces `Long.valueOf(Long.MIN_VALUE)`
    /// - All intermediate values are preserved exactly
    ///
    /// ## Examples
    ///
    /// ```java
    /// LongProvider provider = new LongProvider();
    ///
    /// // Basic values
    /// Long zero = provider.createValue(0L);                    // 0L
    /// Long positive = provider.createValue(42L);               // 42L
    /// Long negative = provider.createValue(-100L);             // -100L
    ///
    /// // Large values beyond Integer range
    /// Long huge = provider.createValue(10_000_000_000L);       // 10000000000L
    /// Long bigNegative = provider.createValue(-10_000_000_000L); // -10000000000L
    ///
    /// // Boundary values
    /// Long max = provider.createValue(Long.MAX_VALUE);         // 9223372036854775807L
    /// Long min = provider.createValue(Long.MIN_VALUE);         // -9223372036854775808L
    ///
    /// // Time-based values
    /// Long timestamp = provider.createValue(System.currentTimeMillis());
    /// Long nanos = provider.createValue(System.nanoTime());
    /// ```
    ///
    /// ## Performance Characteristics
    ///
    /// This method has optimal performance characteristics:
    /// - No range validation overhead
    /// - Direct primitive boxing
    /// - No mathematical operations
    /// - Constant time complexity regardless of value magnitude
    ///
    /// @param value the primitive long value to box into a Long object
    /// @return a Long instance containing exactly the specified value
    /// @complexity constant time
    @Override
    public @NonNull Long createValue(final long value) {
        return value;
    }
}
