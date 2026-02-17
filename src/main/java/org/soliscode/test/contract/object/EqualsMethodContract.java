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

package org.soliscode.test.contract.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.ContractSupport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/// **Contract for the `Object#equals(Object)` method**
///
/// This interface defines tests for the `equals()` method as specified in [Object].
/// It verifies reflexivity, symmetry, transitivity, consistency, and null-handling.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a class's `equals()` implementation follows the
/// contract defined by [Object#equals(Object)], which is essential for correct behavior in
/// collections and other data structures.
///
/// ## Usage Examples
/// This contract is normally used through the [ObjectContract] class:
///
/// ```java
/// public class MyClassTest extends ObjectContract<MyClass> {
/// }
/// ```
///
/// If a class does not implement `equals()` according to the [Object] specification, it can
/// be omitted using the `doesNotSupportMethod()` method:
///
/// ```java
/// public class MyClassTest extends ObjectContract<MyClass> {
///     public MyClassTest() {
///         doesNotSupportMethod(ObjectMethods.EQUALS);
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the object and [org.soliscode.test.provider.ObjectProvider] implementations being tested.
///
/// @param <T> The type being tested.
/// @author evanbergstrom
/// @see Object#equals(Object)
/// @see ObjectContract
/// @since 1.0.0
public interface EqualsMethodContract<T> extends ContractSupport<T> {

    /// Tests that the `equals()` method is reflexive: `x.equals(x)` should return `true`.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#equals(Object)
    @Test
    @DisplayName("equals() is reflexive")
    default void equals_whenSameInstance_returnsTrue() {
        if (supportsMethod(ObjectMethods.EQUALS)) {
            T x = provider().createInstance();
            assertEquals(x, x);
        }
    }

    /// Tests that the `equals()` method is symmetric: if `x.equals(y)`, then `y.equals(x)` should return `true`.
    /// It also verifies that if `x` and `y` are not equal, then both `x.equals(y)` and `y.equals(x)` return `false`.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#equals(Object)
    @Test
    @DisplayName("equals() is symmetric")
    default void equals_whenCalledWithEqualAndUnequalValues_isSymmetric() {
        if (supportsMethod(ObjectMethods.EQUALS)) {
            T x = provider().createInstance();
            T y = provider().copyInstance(x);
            assertEquals(x, y);
            assertEquals(y, x);

            List<T> values = provider().createUniqueInstances(2);
            assertNotEquals(values.get(0), values.get(1));
            assertNotEquals(values.get(1), values.get(0));
        }
    }

    /// Tests that the `equals()` method is transitive: if `x.equals(y)` and `y.equals(z)`,
    /// then `x.equals(z)` should return `true`.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#equals(Object)
    @Test
    @DisplayName("equals() is transitive")
    default void equals_whenChained_isTransitive() {
        if (supportsMethod(ObjectMethods.EQUALS)) {
            T x = provider().createInstance();
            T y = provider().copyInstance(x);
            T z = provider().copyInstance(y);
            assertEquals(x, y);
            assertEquals(y, z);
            assertEquals(x, z);
        }
    }

    /// Tests that the `equals()` method is consistent: multiple invocations of `x.equals(y)`
    /// consistently return the same result, provided no information used in `equals`
    /// comparisons on the objects is modified.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#equals(Object)
    @Test
    @DisplayName("equals() is consistent")
    default void equals_whenRepeated_returnsConsistentResult() {
        if (supportsMethod(ObjectMethods.EQUALS)) {
            T x = provider().createInstance();
            T y = provider().copyInstance(x);
            for (int i = 0; i < CONSISTENCY_REPEATS; i++) {
                assertEquals(x, y, "Failed on " + i + " attempt");
            }
        }
    }

    /// Tests that for any non-null reference value `x`, `x.equals(null)` should return `false`.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#equals(Object)
    @Test
    @DisplayName("equals() with null value returns false")
    default void equals_withNullValue_returnsFalse() {
        if (supportsMethod(ObjectMethods.EQUALS)) {
            T value = provider().createInstance();
            assertNotEquals(null, value);
        }
    }
}
