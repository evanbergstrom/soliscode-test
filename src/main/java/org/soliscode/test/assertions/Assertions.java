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

package org.soliscode.test.assertions;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.function.Executable;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/// Utility class containing methods that support asserting conditions in tests. These methods are complimentary to the
/// assertions provided in the [Assertions][org.junit.jupiter.api.Assertions] class provided by the JUnit library
///
/// @author evanbergstrom
/// @see org.junit.jupiter.api.Assertions
public class Assertions {

    private Assertions() {}

    /// Asserts that the executable will throw one of a list of possible exception types.
    /// @param expectedTypes the exception types that the executable should throw.
    /// @param executable the executable to test.
    public static void assertThrowsAny(final @NotNull Collection<Class<? extends Throwable>> expectedTypes,
                                       final @NotNull Executable executable) {
        AssertThrowsAny.assertThrowsAny(expectedTypes, executable);
    }

    /// Asserts that the executable will throw one of a list of possible exception types.
    /// @param expectedTypes the exception types that the executable should throw.
    /// @param executable the executable to test.
    /// @param message the message to include in the exception if the assertions fails.
    public static void assertThrowsAny(final @NotNull Collection<Class<? extends Throwable>> expectedTypes,
                                       final @NotNull Executable executable, final String message) {
        AssertThrowsAny.assertThrowsAny(expectedTypes, executable, message);
    }

    /// Asserts that the executable will throw one of a list of possible exception types.
    /// @param expectedTypes the exception types that the executable should throw.
    /// @param executable the executable to test.
    /// @param messageSupplier the supplier of the message to include in the exception if the assertions fails.
    public static void assertThrowsAny(final @NotNull Collection<Class<? extends Throwable>> expectedTypes,
                                       final @NotNull Executable executable, Supplier<String> messageSupplier) {
        AssertThrowsAny.assertThrowsAny(expectedTypes, executable, messageSupplier);
    }

    /// Asserts that the executable will throw an exception that is not one of a list of prohibited exception types.
    /// @param prohibitedType the exception type that the executable should not throw.
    /// @param executable the executable to test.
    public static void assertThrowsDifferent(final @NotNull Class<? extends Throwable> prohibitedType,
                                             final @NotNull Executable executable) {
        AssertThrowsDifferent.assertThrowsDifferent(prohibitedType, executable);
    }

    /// Asserts that the executable will throw an exception that is not one of a list of prohibited exception types.
    /// @param prohibitedTypes the exception types that the executable should not throw.
    /// @param executable the executable to test.
    public static void assertThrowsDifferent(final @NotNull Collection<Class<? extends Throwable>> prohibitedTypes,
                                             final @NotNull Executable executable) {
        AssertThrowsDifferent.assertThrowsDifferent(prohibitedTypes, executable);
    }

    /// Asserts that the executable will throw an exception that is not one of a list of prohibited exception types.
    /// @param prohibitedType the exception type that the executable should not throw.
    /// @param executable the executable to test.
    /// @param message the message to include in the exception if the assertions fails.
    public static void assertThrowsDifferent(final @NotNull Class<? extends Throwable> prohibitedType,
                                             final @NotNull Executable executable, final String message) {
        AssertThrowsDifferent.assertThrowsDifferent(prohibitedType, executable, message);
    }

    /// Asserts that the executable will throw an exception that is not one of a list of prohibited exception types.
    /// @param prohibitedTypes the exception types that the executable should not throw.
    /// @param executable the executable to test.
    /// @param message the message to include in the exception if the assertions fails.
    public static void assertThrowsDifferent(final @NotNull Collection<Class<? extends Throwable>> prohibitedTypes,
                                             final @NotNull Executable executable, final String message) {
        AssertThrowsDifferent.assertThrowsDifferent(prohibitedTypes, executable, message);
    }

    /// Asserts that the executable will throw an exception that is not one of a list of prohibited exception types.
    /// @param prohibitedTypes the exception types that the executable should not throw.
    /// @param executable the executable to test.
    /// @param messageSupplier the supplier of the message to include in the exception if the assertions fails.
    public static void assertThrowsDifferent(final @NotNull Collection<Class<? extends Throwable>> prohibitedTypes,
                                             final @NotNull Executable executable, Supplier<String> messageSupplier) {
        AssertThrowsDifferent.assertThrowsDifferent(prohibitedTypes, executable, messageSupplier);
    }

    /// Asserts that the executable will throw an exception that is not one of a list of prohibited exception types.
    /// @param prohibitedType the exception type that the executable should not throw.
    /// @param executable the executable to test.
    /// @param messageSupplier the supplier of the message to include in the exception if the assertions fails.
    public static void assertThrowsDifferent(final @NotNull Class<? extends Throwable> prohibitedType,
                                             final @NotNull Executable executable, Supplier<String> messageSupplier) {
        AssertThrowsDifferent.assertThrowsDifferent(prohibitedType, executable, messageSupplier);
    }


    /// Asserts that the object is not a instance of a class.
    /// @param expectedType The type that the object being tested is expected *not* to be.
    /// @param actual The object being tested.
    /// @param <T> The type that is not expected.
    public static <T> void assertNotInstanceOf(final Class<T> expectedType, final Object actual) {
        AssertNotInstanceOf.assertNotInstanceOf(expectedType, actual);
    }

    /// Asserts that the object is not a instance of a class.
    /// @param expectedType The type that the object being tested is expected *not* to be.
    /// @param actual The object being tested.
    /// @param <T> The type that is not expected.
    /// @param message the message to include in the exception if the assertions fails.
    public static <T> void assertNotInstanceOf(final Class<T> expectedType, final Object actual,
                                         final String message) {
        AssertNotInstanceOf.assertNotInstanceOf(expectedType, actual, message);
    }

    /// Asserts that the object is not a instance of a class.
    /// @param expectedType The type that the object being tested is expected *not* to be.
    /// @param actual The object being tested.
    /// @param <T> The type that is not expected.
    /// @param messageSupplier the supplier of the message to include in the exception if the assertions fails.
    public static <T> void assertInstanceOf(final Class<T> expectedType, final Object actual,
                                          final Supplier<String> messageSupplier) {
        AssertNotInstanceOf.assertNotInstanceOf(expectedType, actual, messageSupplier);
    }

    /// Asserts that an object is an instance of a class that implements interfaces only from a set of permitted
    /// interfaces. This will only test for interfaces that the class of the object implements directly.
    /// @param expected the classes or interfaces that the object can be an instance of.
    /// @param actual The object being tested.
    public static void assertImplementsOnly(final Class<?> expected, final Object actual) {
        AssertImplementsOnly.assertImplementsOnly(List.of(expected), actual);
    }

    /// Asserts that an object is an instance of a class that implements interfaces only from a set of permitted
    /// interfaces. This will only test for interfaces that the class of the object implements directly.
    /// @param expected the classes or interfaces that the object can be an instance of.
    /// @param actual The object being tested.
    public static void assertImplementsOnly(final Collection<Class<?>> expected, final Object actual) {
        AssertImplementsOnly.assertImplementsOnly(expected, actual);
    }

    /// Asserts that an object is an instance of a class that implements interfaces only from a set of permitted
    /// interfaces. This will only test for interfaces that the class of the object implements directly.
    /// @param expected the classes or interfaces that the object can be an instance of.
    /// @param actual The object being tested.
    /// @param message the message to include in the exception if the assertions fails.
    public static void assertInstanceOfOnly(final Class<?> expected, final Object actual,
                                            final String message) {
        AssertImplementsOnly.assertImplementsOnly(List.of(expected), actual, message);
    }

    /// Asserts that an object is an instance of a class that implements interfaces only from a set of permitted
    /// interfaces. This will only test for interfaces that the class of the object implements directly.
    /// @param expected the classes or interfaces that the object can be an instance of.
    /// @param actual The object being tested.
    /// @param message the message to include in the exception if the assertions fails.
    public static void assertInstanceOfOnly(final Collection<Class<?>> expected, final Object actual,
                                                final String message) {
        AssertImplementsOnly.assertImplementsOnly(expected, actual, message);
    }

    /// Asserts that an object is an instance of a class that implements interfaces only from a set of permitted
    /// interfaces. This will only test for interfaces that the class of the object implements directly.
    /// @param expected the classes or interfaces that the object can be an instance of.
    /// @param actual The object being tested.
    /// @param messageSupplier the supplier of the message to include in the exception if the assertions fails.
    public static void assertInstanceOfOnly(final Class<?> expected, final Object actual,
                                            final Supplier<String> messageSupplier) {
        AssertImplementsOnly.assertImplementsOnly(List.of(expected), actual, messageSupplier);
    }

    /// Asserts that an object is an instance of a class that implements interfaces only from a set of permitted
    /// interfaces. This will only test for interfaces that the class of the object implements directly.
    /// @param expected the classes or interfaces that the object can be an instance of.
    /// @param actual The object being tested.
    /// @param messageSupplier the supplier of the message to include in the exception if the assertions fails.
    public static void assertInstanceOfOnly(final Collection<Class<?>> expected, final Object actual,
                                            final Supplier<String> messageSupplier) {
        AssertImplementsOnly.assertImplementsOnly(expected, actual, messageSupplier);
    }

    public static <T extends Comparable<T>> void assertLessThan(T first, T second) {
        AssertLessThan.assertLessThan(first, second);
    }

    public static <T extends Comparable<T>> void assertLessThan(T first, T second, String message) {
        AssertLessThan.assertLessThan(first, second, message);
    }

    public static <T extends Comparable<T>> void assertLessThan(T first, T second, final Supplier<String> messageSupplier) {
        AssertLessThan.assertLessThan(first, second, messageSupplier);
    }
}
