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

import org.junit.jupiter.api.Test;
import org.soliscode.test.contract.support.ContractSupport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/// This interface tests if a class has implemented the `toString()` method correctly. This contract class can be used
/// individually by a test class, but it is normally used through the [ObjectContract] class:
/// ```java
/// public class MyClassTest extends ObjectContract<MyClass> {
/// }
/// ```
/// If a test is using the ObjectContract class, but the class being tested does not implement the toString method based
/// upon the specification in the [Object] class, then it can be omitted from the tests using the
/// `doesNotSupportMethod()` method:
/// ```java
/// public class MyClassTest extends ObjectContract<MyClass> {
///     public MyClassTest() {
///         doesNotSupportMethod(ObjectMethods.ToString);
///     }
/// }
/// ```
/// @param <T> The type being tested.
/// @author evanbergstrom
/// @see Object#toString()
/// @see ObjectContract
/// @since 1.0
public interface ToStringContract<T> extends ContractSupport<T> {

    /// Tests that the `toString()` method consistently returns the same string value over multiple
    /// invocations on the same instance.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#toString()
    @Test
    default void testToStringIsConsistent() {
        if (supportsMethod(ObjectMethods.ToString)) {
            T value = provider().createInstance();
            String string1 = value.toString();
            String string2 = value.toString();
            assertEquals(string1, string2);
        }
    }

    /// Tests that the `toString()` method returns the same value for two strings that are equal.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#toString()
    @Test
    default void testToStringForEqualValues() {
        if (supportsMethod(ObjectMethods.ToString)) {
            T value = provider().createInstance();
            T other = provider().copyInstance(value);
            String string = value.toString();
            String otherString = other.toString();
            assertEquals(string, otherString);
        }
    }

    /// Tests that the `toString()` method returns different values for instances that are not equal.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#toString()
    @Test
    default void testToStringForDifferentValues() {
        if (supportsMethod(ObjectMethods.ToString)) {
            List<T> values = provider().createUniqueInstances(10);
            long uniqueValues = values.stream().map(Object::toString).distinct().count();
            assertEquals(values.size(), uniqueValues);
        }
    }

    /// Tests that the `toString()` method has been overridden.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
     /// @see Object#toString()
    @Test
    default void testToStringOverridden() {
        if (supportsMethod(ObjectMethods.ToString)) {
            List<T> values = provider().createUniqueInstances(10);
            for (T value : values) {
                String defaultString = value.getClass().getName() + '@' + Integer.toHexString(value.hashCode());
                assertNotEquals(value.toString(), defaultString);
            }
        }
    }
}
