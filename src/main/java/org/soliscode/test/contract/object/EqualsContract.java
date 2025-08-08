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

/// This interface tests if a class has implemented the `equals()` method correctly. This contract class can be used
/// individually by a test class, but it is normally used through the [ObjectContract] class:
/// ```java
/// public class MyClassTest extends ObjectContract<MyClass> {
/// }
/// ```
/// If a test is using the ObjectContract class, but the class being tested does not implement the equals method based
/// upon the specification in the [Object] class, then it can be omitted from the tests using the
/// `doesNotSupportMethod()` method:
/// ```java
/// public class MyClassTest extends ObjectContract<MyClass> {
///     public MyClassTest() {
///         doesNotSupportMethod(ObjectMethods.Equals);
///     }
/// }
/// ```
/// @param <T> The type being tested.
/// @author evanbergstrom
/// @see Object#equals(Object)
/// @see ObjectContract
/// @since 1.0
public interface EqualsContract<T> extends ContractSupport<T> {

    /// Tests that the `equals()` method is reflexive.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#equals(Object)
    @Test
    @DisplayName("the equals() method is reflexive")
    @SuppressWarnings("EqualsWithItself")
    default void testEqualsIsReflexive() {
        if (supportsMethod(ObjectMethods.Equals)) {
            T x = provider().createInstance();
            assertEquals(x, x);
        }
    }

    /// Tests that the `equals()` method is symmetric.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#equals(Object)
    @Test
    @DisplayName("the equals() method is symmetric")
    default void testEqualsIsSymmetric() {
        if (supportsMethod(ObjectMethods.Equals)) {
            T x = provider().createInstance();
            T y = provider().copyInstance(x);
            assertEquals(x, y);
            assertEquals(y, x);

            List<T> values = provider().createUniqueInstances(2);
            assertNotEquals(values.get(0), values.get(1));
            assertNotEquals(values.get(1), values.get(0));
        }
    }

    /// Tests that the `equals()` method is transitive.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#equals(Object)
    @Test
    @DisplayName("the equals() method is transitive")
    default void testEqualsIsTransitive() {
        if (supportsMethod(ObjectMethods.Equals)) {
            T x = provider().createInstance();
            T y = provider().copyInstance(x);
            T z = provider().copyInstance(y);
            assertEquals(x, y);
            assertEquals(y, z);
        }
    }

    /// Tests that the `equals()` method is consistent.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#equals(Object)
    @Test
    @DisplayName("the equals() method is consistent")
    default void testEqualsIsConsistent() {
        if (supportsMethod(ObjectMethods.Equals)) {
            T x = provider().createInstance();
            T y = provider().copyInstance(x);
            for (int i=0; i < 10; i++) {
                assertEquals(x, y);
            }
        }
    }

    /// Tests that the `equals()` method return false for null values.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#equals(Object)
    @Test
    default void testEqualsForNullValue() {
        if (supportsMethod(ObjectMethods.Equals)) {
            T value = provider().createInstance();
            assertNotEquals(null, value);
        }
    }
}
