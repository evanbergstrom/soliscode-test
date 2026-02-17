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

package org.soliscode.test.contract.numeric;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.IntegerSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Test suite for classes that implement the {@link Number} interface with integer-like behavior.
///
/// ### Purpose
/// This contract verifies that an implementation of {@link Number} (specifically one that behaves
/// like an integer) correctly implements the conversion methods to various primitive types.
/// It ensures that `intValue()`, `longValue()`, `floatValue()`, and `doubleValue()` return
/// consistent and accurate results for a range of values including minimum, maximum,
/// zero, and negative values.
///
/// ### Usage Examples
///
/// #### Implementation for a custom Integer-like class
/// ```java
/// public class MyIntegerTest implements IntegerContract<MyInteger> {
///
///     @Override
///     public IntegerNumberProvider<MyInteger> provider() {
///         return new MyIntegerProvider();
///     }
/// }
/// ```
///
/// ### Thread Safety
/// The tests in this contract are not designed to be thread-safe. They should be run
/// in a single-threaded environment.
///
/// @param <T> The number type being tested.
/// @author evanbergstrom
/// @see Number
/// @since 1.0.0
public interface IntegerContract<T extends Number> extends IntegerSupport<T> {

    /// Tests that the implementation of the {@link Number#intValue()} method works for various values.
    ///
    /// The test verifies:
    /// 1. `intValue()` returns the correct value for `minIntegerValue`.
    /// 2. `intValue()` returns -1 for value -1.
    /// 3. `intValue()` returns 0 for value 0.
    /// 4. `intValue()` returns 1 for value 1.
    /// 5. `intValue()` returns the correct value for `maxIntegerValue`.
    ///
    /// @throws AssertionError if any of the values do not match.
    /// @since 1.0.0
    @Test
    @DisplayName("The intValue() function works for various values.")
    default void intValue_whenCalled_returnsExpectedValues() {
        long max = provider().maxIntegerValue();
        long min = provider().minIntegerValue();

        assertEquals(min, provider().createValue(min).intValue());
        assertEquals(-1, provider().createValue(-1).intValue());
        assertEquals(0, provider().createValue(0).intValue());
        assertEquals(1, provider().createValue(1).intValue());
        assertEquals(max, provider().createValue(max).intValue());
    }

    /// Tests that the implementation of the {@link Number#longValue()} method works for various values.
    ///
    /// The test verifies:
    /// 1. `longValue()` returns -1L for value -1.
    /// 2. `longValue()` returns 0L for value 0.
    /// 3. `longValue()` returns 1L for value 1.
    ///
    /// @throws AssertionError if any of the values do not match.
    /// @since 1.0.0
    @Test
    @DisplayName("The longValue() function works for various values.")
    default void longValue_whenCalled_returnsExpectedValues() {
        assertEquals(-1L, provider().createValue(-1).longValue());
        assertEquals(0L, provider().createValue(0).longValue());
        assertEquals(1L, provider().createValue(1).longValue());
    }

    /// Tests that the implementation of the {@link Number#floatValue()} method works for various values.
    ///
    /// The test verifies:
    /// 1. `floatValue()` returns -1.0f for value -1.
    /// 2. `floatValue()` returns 0.0f for value 0.
    /// 3. `floatValue()` returns 1.0f for value 1.
    ///
    /// @throws AssertionError if any of the values do not match.
    /// @since 1.0.0
    @Test
    @DisplayName("The floatValue() function works for various values.")
    default void floatValue_whenCalled_returnsExpectedValues() {
        assertEquals(-1.0f, provider().createValue(-1).floatValue());
        assertEquals(0.0f, provider().createValue(0).floatValue());
        assertEquals(1.0f, provider().createValue(1).floatValue());
    }

    /// Tests that the implementation of the {@link Number#doubleValue()} method works for various values.
    ///
    /// The test verifies:
    /// 1. `doubleValue()` returns -1.0d for value -1.
    /// 2. `doubleValue()` returns 0.0d for value 0.
    /// 3. `doubleValue()` returns 1.0d for value 1.
    ///
    /// @throws AssertionError if any of the values do not match.
    /// @since 1.0.0
    @Test
    @DisplayName("The doubleValue() function works for various values.")
    default void doubleValue_whenCalled_returnsExpectedValues() {
        assertEquals(-1.0d, provider().createValue(-1).doubleValue());
        assertEquals(0.0d, provider().createValue(0).doubleValue());
        assertEquals(1.0d, provider().createValue(1).doubleValue());
    }
}
