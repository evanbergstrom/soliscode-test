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

/// **Contract for the `Object#hashCode()` method**
///
/// This interface defines tests for the `hashCode()` method as specified in [Object].
/// It verifies stability, equality consistency, and distribution quality.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a class's `hashCode()` implementation follows the
/// contract defined by [Object#hashCode()], which is essential for correct behavior in hash-based
/// collections like [java.util.HashMap] and [java.util.HashSet].
///
/// ## Usage Examples
/// This contract is normally used through the [ObjectContract] class:
///
/// ```java
/// public class MyClassTest extends ObjectContract<MyClass> {
/// }
/// ```
///
/// If a class does not implement `hashCode()` according to the [Object] specification, it can
/// be omitted using the `doesNotSupportMethod()` method:
///
/// ```java
/// public class MyClassTest extends ObjectContract<MyClass> {
///     public MyClassTest() {
///         doesNotSupportMethod(ObjectMethods.HASH_CODE);
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
/// @see Object#hashCode()
/// @see ObjectContract
/// @since 1.0.0
public interface HashCodeMethodContract<T> extends ContractSupport<T> {

    /// The amount to allow the load factor of a hash table to exceed the target.
    double LOAD_FACTOR_ALLOWANCE = 0.10;

    /// Tests that the `hashCode()` method consistently returns the same integer value over multiple
    /// invocations on the same instance.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#hashCode()
    @Test
    @DisplayName("hashCode() returns the same integer for multiple invocations")
    default void hashCode_whenRepeated_isConsistent() {
        if (supportsMethod(ObjectMethods.HASH_CODE)) {
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
    @DisplayName("hashCode() returns the same integer for equal values")
    default void hashCode_withEqualValues_returnsSameInteger() {
       if (supportsMethod(ObjectMethods.HASH_CODE)) {
           T value = provider().createInstance();
           T other = provider().copyInstance(value);
           int hash = value.hashCode();
           int otherHash = other.hashCode();
           assertEquals(hash, otherHash);
       }
    }

    /// Tests that the `hashCode()` method returns different values for objects that are not equal.
    /// This is not strictly required by the `Object` interface, but it will result in improved performance.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#hashCode()
    @Test
    @DisplayName("hashCode() returns different integers for unequal values")
    default void hashCode_withUniqueValues_returnsUniqueIntegers() {
        if (supportsMethod(ObjectMethods.HASH_CODE)) {
            List<T> values = provider().createUniqueInstances(10);
            long uniqueValues = values.stream().map(Object::hashCode).distinct().count();
            assertEquals(values.size(), uniqueValues);
        }
    }

    /// Test that the hashCode function results in a uniform distribution of hashcode values.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#hashCode()
    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    @DisplayName("hashCode() returns integers that have a uniform distribution")
    default void hashCode_whenCalledManyTimes_hasUniformDistribution() {
        if (supportsMethod(ObjectMethods.HASH_CODE)) {
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
            assertTrue(actualLLoadFactor - expectedLoadFactor < LOAD_FACTOR_ALLOWANCE);
        }
    }
}
