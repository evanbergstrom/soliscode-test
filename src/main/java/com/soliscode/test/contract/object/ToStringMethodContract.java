/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.object;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ContractSupport;
import com.soliscode.test.methods.ObjectMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/// **Contract for the `Object#toString()` method**
///
/// This interface defines tests for the `toString()` method as specified in [Object].
/// It verifies that the result is not `null`, as [Object#toString()] requires, and, reading its recommendations more
/// strictly, consistency, equality correlation, distinct strings for unequal values, and that the method has been
/// overridden.
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
/// public class MyClassTest extends AbstractTest implements ObjectContract<MyClass> {
///     // provider(), as in ObjectContract's example
/// }
/// ```
///
/// If a class does not implement `toString()` according to the [Object] specification, it can
/// be omitted using the `doesNotSupportMethod()` method:
///
/// ```java
/// public class MyClassTest extends AbstractTest implements ObjectContract<MyClass> {
///     // provider(), as in ObjectContract's example
///
///     public MyClassTest() {
///         doesNotSupportMethod(ObjectMethods.TO_STRING);
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the object and [com.soliscode.test.provider.ObjectProvider] implementations being tested.
///
/// @param <T> The type being tested.
/// @author evanbergstrom
/// @see Object#toString()
/// @see ObjectContract
/// @since 1.0.0
public interface ToStringMethodContract<T> extends ContractSupport<T> {

    /// Returns whether the class under test's `toString` describes its value, so that the three tests that read
    /// [Object#toString()]'s recommendations as rules run: [#toStringWhenRepeatedIsConsistent()],
    /// [#toStringWithEqualValuesReturnsSameString()] and [#toStringWhenCalledIsOverridden()].
    ///
    /// [Object#toString()] asks for a string that "textually represents" the object, and recommends that subclasses
    /// override it, but requires only that the result is not `null`. A class whose string legitimately changes, such
    /// as one that includes a call counter, returns `false`; [#toStringWhenCalledReturnsNonNull()] still runs.
    ///
    /// @return `true` unless overridden
    /// @since 0.1.7
    default boolean toStringDescribesValue() {
        return true;
    }

    /// Returns whether the class under test's `toString` gives unequal values distinct strings, so that
    /// [#toStringWithUniqueValuesReturnsUniqueStrings()], which reads [Object#toString()] more strictly than it is
    /// written, runs. A class whose string shows only part of what `equals` compares returns `false`.
    ///
    /// @return `true` unless overridden
    /// @since 0.1.7
    default boolean toStringDistinguishesUnequalValues() {
        return true;
    }

    /// Tests that the `toString()` method does not return `null`, for the default instance and up to 10 unique
    /// ones: "Satisfying this method's contract implies a non-`null` result must be returned"
    /// ([Object#toString()]). It runs whatever [#toStringDescribesValue()] and
    /// [#toStringDistinguishesUnequalValues()] say, and only `doesNotSupportMethod(ObjectMethods.TO_STRING)` turns it
    /// off.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#toString()
    /// @since 0.1.7
    @Test
    @DisplayName("toString() does not return null")
    default void toStringWhenCalledReturnsNonNull() {
        if (supportsMethod(ObjectMethods.TO_STRING)) {
            assertNotNull(provider().defaultInstance().toString(), "the default instance's toString()");
            int count = (int) Math.min(10, provider().uniqueSizeLimit());
            for (final T value : provider().createUniqueInstances(count)) {
                assertNotNull(value.toString());
            }
        }
    }

    /// Tests that the `toString()` method consistently returns the same string value over multiple
    /// invocations on the same instance.
    ///
    /// This reads [Object#toString()] more strictly than it is written, which says "The string output is not
    /// necessarily stable over time or across JVM invocations." The reading: two calls, one after the other, in one
    /// JVM, with no change between them, give the same string. It runs only where [#toStringDescribesValue()] is
    /// `true`.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#toString()
    @Test
    @DisplayName("toString() is consistent")
    default void toStringWhenRepeatedIsConsistent() {
        if (supportsMethod(ObjectMethods.TO_STRING) && toStringDescribesValue()) {
            T value = provider().createInstance();
            String string1 = value.toString();
            String string2 = value.toString();
            assertEquals(string1, string2);
        }
    }

    /// Tests that the `toString()` method returns the same value for two objects that are equal.
    ///
    /// This reads [Object#toString()]'s "textually represents" as a rule, more strictly than it is written. It runs
    /// only where [#toStringDescribesValue()] is `true`, and, since it compares a value with its copy, where
    /// [com.soliscode.test.contract.support.ContractSupport#equalsComparesValues] is `true`.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#toString()
    @Test
    @DisplayName("toString() returns the same value for equal objects")
    default void toStringWithEqualValuesReturnsSameString() {
        if (supportsMethod(ObjectMethods.TO_STRING) && toStringDescribesValue() && equalsComparesValues()) {
            T value = provider().createInstance();
            T other = provider().copyInstance(value);
            String string = value.toString();
            String otherString = other.toString();
            assertEquals(string, otherString);
        }
    }

    /// Tests that the `toString()` method returns different values for instances that are not equal.
    ///
    /// This reads [Object#toString()]'s "concise but informative representation" more strictly than it is written.
    /// It runs only where [#toStringDistinguishesUnequalValues()] is `true`.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#toString()
    @Test
    @DisplayName("toString() returns different values for unique objects")
    default void toStringWithUniqueValuesReturnsUniqueStrings() {
        if (supportsMethod(ObjectMethods.TO_STRING) && toStringDistinguishesUnequalValues()) {
            List<T> values = provider().createUniqueInstances(10);
            long uniqueValues = values.stream().map(Object::toString).distinct().count();
            assertEquals(values.size(), uniqueValues);
        }
    }

    /// Tests that the `toString()` method has been overridden and does not return the default
    /// implementation (ClassName@hashCode).
    ///
    /// This reads [Object#toString()]'s "It is recommended that all subclasses override this method" as a rule. It
    /// runs only where [#toStringDescribesValue()] is `true`: a class that keeps `Object`'s `toString` returns
    /// `false` from it, and [#toStringWhenCalledReturnsNonNull()] still runs.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#toString()
    @Test
    @DisplayName("toString() is overridden")
    default void toStringWhenCalledIsOverridden() {
        if (supportsMethod(ObjectMethods.TO_STRING) && toStringDescribesValue()) {
            List<T> values = provider().createUniqueInstances(10);
            for (final T value : values) {
                String defaultString = value.getClass().getName() + '@' + Integer.toHexString(value.hashCode());
                assertNotEquals(value.toString(), defaultString);
            }
        }
    }
}
