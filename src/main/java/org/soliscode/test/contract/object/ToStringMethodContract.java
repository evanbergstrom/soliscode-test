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

/// **Contract for the `Object#toString()` method**
///
/// This interface defines tests for the `toString()` method as specified in [Object].
/// It verifies consistency, equality correlation, and that the method has been overridden.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a class's `toString()` implementation follows the
/// contract defined by [Object#toString()], providing a concise but informative representation
/// of the object.
///
/// ## Usage Examples
/// This contract is normally used through the [ObjectContract] class:
///
/// ```java
/// public class MyClassTest extends ObjectContract<MyClass> {
/// }
/// ```
///
/// If a class does not implement `toString()` according to the [Object] specification, it can
/// be omitted using the `doesNotSupportMethod()` method:
///
/// ```java
/// public class MyClassTest extends ObjectContract<MyClass> {
///     public MyClassTest() {
///         doesNotSupportMethod(ObjectMethods.TO_STRING);
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
/// @see Object#toString()
/// @see ObjectContract
/// @since 1.0.0
public interface ToStringMethodContract<T> extends ContractSupport<T> {

    /// Tests that the `toString()` method consistently returns the same string value over multiple
    /// invocations on the same instance.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#toString()
    @Test
    @DisplayName("toString() is consistent")
    default void toString_whenRepeated_isConsistent() {
        if (supportsMethod(ObjectMethods.TO_STRING)) {
            T value = provider().createInstance();
            String string1 = value.toString();
            String string2 = value.toString();
            assertEquals(string1, string2);
        }
    }

    /// Tests that the `toString()` method returns the same value for two objects that are equal.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#toString()
    @Test
    @DisplayName("toString() returns the same value for equal objects")
    default void toString_withEqualValues_returnsSameString() {
        if (supportsMethod(ObjectMethods.TO_STRING)) {
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
    @DisplayName("toString() returns different values for unique objects")
    default void toString_withUniqueValues_returnsUniqueStrings() {
        if (supportsMethod(ObjectMethods.TO_STRING)) {
            List<T> values = provider().createUniqueInstances(10);
            long uniqueValues = values.stream().map(Object::toString).distinct().count();
            assertEquals(values.size(), uniqueValues);
        }
    }

    /// Tests that the `toString()` method has been overridden and does not return the default
    /// implementation (ClassName@hashCode).
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#toString()
    @Test
    @DisplayName("toString() is overridden")
    default void toString_whenCalled_isOverridden() {
        if (supportsMethod(ObjectMethods.TO_STRING)) {
            List<T> values = provider().createUniqueInstances(10);
            for (T value : values) {
                String defaultString = value.getClass().getName() + '@' + Integer.toHexString(value.hashCode());
                assertNotEquals(value.toString(), defaultString);
            }
        }
    }
}
