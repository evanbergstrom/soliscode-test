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

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// This interface tests if a class has implemented the `hashCode()` method correctly. This contract class can be used
/// individually by a test class, but it is normally used through the [ObjectContract] class:
/// ```java
/// public class MyClassTest extends ObjectContract<MyClass> {
/// }
/// ```
/// If a test is using the ObjectContract class, but the class being tested does not implement the hashCode method based
/// upon the specification in the [Object] class, then it can be omitted from the tests using the
/// `doesNotSupportMethod()` method:
/// ```java
/// public class MyClassTest extends ObjectContract<MyClass> {
///     public MyClassTest() {
///         doesNotSupportMethod(ObjectMethods.HashCode);
///     }
/// }
/// ```
/// @param <T> The type being tested.
/// @author evanbergstrom
/// @see Object#hashCode()
/// @see ObjectContract
/// @since 1.0
public interface HashCodeContract<T> extends ContractSupport<T> {

    /// Tests that the `hashCode()` method consistently returns the same integer value over multiple
    /// invocations.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#hashCode()
    @Test
    @DisplayName("hashCode() returns the same integer for multiple invocations.")
    default void testHashCodeIsStable() {
        if (supportsMethod(ObjectMethods.HashCode)) {
            T value = provider().createInstance();
            int hash1 = value.hashCode();
            int hash2 = value.hashCode();
            assertEquals(hash1, hash2);
        }
    }

    /// Tests that the `hashCode()` method returns the same integer value for objects that are equal.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#hashCode()
    @Test
    @DisplayName("hashCode() returns the same integer for equals values")
    default void testHashCodeWithEqualValues() {
       if (supportsMethod(ObjectMethods.HashCode)) {
           T value = provider().createInstance();
           T other = provider().copyInstance(value);
           int hash = value.hashCode();
           int otherHash = other.hashCode();
           assertEquals(hash, otherHash);
       }
    }

    /// Tests that the `hashCode()` method returns different values for objects that are not equal. This is not
    /// strictly required by the `Object` interface, but it will result in improved performance.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#hashCode()
    @Test
    @DisplayName("hashCode() returns different integer for values that are not equal.")
    default void testHashDifferentValues() {
        if (supportsMethod(ObjectMethods.HashCode)) {
            List<T> values = provider().createUniqueInstances(10);
            long uniqueValues = values.stream().map(Object::hashCode).distinct().count();
            assertEquals(values.size(), uniqueValues);
        }
    }

    /// Test that the hashCode function results in a uniform distribution of hashcode values.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#hashCode()
    @Test
    @DisplayName("hashCode() returns integer that have a uniform distribution.")
    default void testHashCodeDistribution() {
        if (supportsMethod(ObjectMethods.HashCode)) {
            final double expectedLoadFactor = 0.75;
            final int numberOfObjects = 1000;
            final int hashTableSize = (int) (numberOfObjects / expectedLoadFactor);

            final double[] table = new double[hashTableSize];
            Supplier<T> supplier = provider().uniqueInstanceSupplier();

            Stream.generate(supplier).limit(numberOfObjects).forEach((o) -> {
                int hash = o.hashCode();
                int index = Math.abs(hash % hashTableSize);
                table[index] = table[index] + 1.0;
            });

            double actualLLoadFactor = Arrays.stream(table).sum() / hashTableSize;
            assertTrue(actualLLoadFactor - expectedLoadFactor < 0.10);
        }
    }
}
