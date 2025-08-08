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

package org.soliscode.test.assertions.collection;

import org.opentest4j.AssertionFailedError;

import java.util.function.Supplier;

/// A collection of utility methods that support asserting conditions on collections in tests.
///
/// @author evanbergstrom
/// @since 1.0.0
public final class CollectionAssertions {

    private CollectionAssertions() {
    }

    /// Assert that the collection is empty.
    ///
    /// @param actual The iterable that should be empty.
    /// @throws AssertionFailedError if the iterable is not empty or the iterable is null.
    public static void assertIsEmpty(final Iterable<?> actual) {
        AssertIsEmpty.assertIsEmpty(actual);
    }

    /// Assert that the collection is empty.
    ///
    /// @param actual The iterable that should be empty.
    /// @param message The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable is not empty or the iterable is null.
    public static void assertIsEmpty(final Iterable<?> actual, final String message) {
        AssertIsEmpty.assertIsEmpty(actual, message);
    }

    /// Assert that the iterable is empty.
    ///
    /// @param actual The iterable that should be empty.
    /// @param messageSupplier The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable is not empty or the iterable is null.
    public static void assertIsEmpty(final Iterable<?> actual, final Supplier<String> messageSupplier) {
        AssertIsEmpty.assertIsEmpty(actual, messageSupplier);
    }

    /// Assert that the collections are the same size.
    ///
    /// @param expected The collection with the expected size.
    /// @param actual The collection with the actual size.
    /// @throws AssertionFailedError if the collection is not empty.
    /// @throws NullPointerException is either of the collections are null.
    public static void assertSameSize(final Iterable<?> expected, final Iterable<?> actual) {
        AssertSameSize.assertSameSize(expected, actual);
    }

    /// Assert that the collections are the same size.
    ///
    /// @param expected The collection with the expected size.
    /// @param actual The collection with the actual size.
    /// @param message The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the collection is not empty.
    /// @throws NullPointerException is either of the collections are null.
    public static void assertSameSize(final Iterable<?> expected, final Iterable<?> actual,
            final String message) {
        AssertSameSize.assertSameSize(expected, actual, message);
    }

    /// Assert that the collections are the same size.
    ///
    /// @param expected The collection with the expected size.
    /// @param actual The collection with the actual size.
    /// @param messageSupplier The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the collection is not empty.
    /// @throws NullPointerException is either of the collections are null.
    public static void assertSameSize(final Iterable<?> expected, final Iterable<?> actual,
            final Supplier<String> messageSupplier) {
        AssertSameSize.assertSameSize(expected, actual, messageSupplier);
    }

    /// Test if an iterable contains a specific element. The elements are considered the same if the `equals` method
    /// returns true.
    ///
    /// @param expected  The element it should contain
    /// @param actual    The iterable that should contain the elements.
    /// @throws AssertionFailedError if the iterable does not the element or if either of the iterable
    ///                              arguments is null.
    public static void assertContains(final Object expected, final Iterable<?> actual) {
        AssertContains.assertContains(expected, actual);
    }

    /// Test if an iterable contains a specific element. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param expected  The element it should contain
    /// @param actual    The iterable that should contain the element.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not the element or if either of the iterable
    ///                              arguments is null.
    public static void assertContains(final Object expected, final Iterable<?> actual, final String message) {
        AssertContains.assertContains(expected, actual, message);
    }

    /// Test if an iterable contains a specific element. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param expected  The element it should contain
    /// @param actual    The iterable that should contain the element.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not the element or if either of the iterable
    ///                              arguments is null.
    public static void assertContains(final Object expected, final Iterable<?> actual, final Supplier<String> messageSupplier) {
        AssertContains.assertContains(expected, actual, messageSupplier);
    }

    /// Test if an iterable contains all of a set of elements. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertContainsAll(final Iterable<?> expected, final Iterable<?> actual) {
        AssertContainsAll.assertContainsAll(expected, actual);
    }

    /// Test if an iterable contains all of a set of elements. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertContainsAll(final Iterable<?> expected, final Iterable<?> actual,
            final String message) {
        AssertContainsAll.assertContainsAll(expected, actual, message);
    }

    /// Test if an iterable contains all of a set of elements. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertContainsAll(final Iterable<?> expected, final Iterable<?> actual,
            final Supplier<String> messageSupplier) {
        AssertContainsAll.assertContainsAll(expected, actual, messageSupplier);
    }

    /// Test if an iterable contains all of a set of elements in the same order. The elements are considered the same if
    /// the `equals` method returns true.
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertEquals(final Iterable<?> expected, final Iterable<?> actual) {
        AssertEquals.assertEquals(expected, actual);
    }

    /// Test if an iterable contains all of an array of elements in the same order. The elements are considered the same
    /// if the `equals` method returns true.
    ///
    /// @param <E> the type of the element.
    /// @param expected  An array of the elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static <E> void assertEquals(final E[] expected, final Iterable<?> actual) {
        AssertEquals.assertEquals(expected, actual);
    }

    /// Test if an array contains all of a list of elements in the same order. The elements are considered the same
    /// if the `equals` method returns true.
    ///
    /// @param <E> the type of the element.
    /// @param expected  The elements the array must contain.
    /// @param actual    The array that should contain the elements.
    /// @throws AssertionFailedError if the array does not contain all the elements or if either of the
    ///                              arguments is null.
    public static <E> void assertEquals(final Iterable<?> expected, final E[] actual) {
        AssertEquals.assertEquals(expected, actual);
    }

    /// Test if an array contains all of a list of elements in the same order. The elements are considered the same
    /// if the `equals` method returns true.
    ///
    /// @param <E> the type of the element.
    /// @param expected  The elements the array must contain.
    /// @param actual    The array that should contain the elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the array does not contain all the elements or if either of the
    ///                              arguments is null.
    public static <E> void assertEquals(final Iterable<?> expected, final E[] actual, String message) {
        AssertEquals.assertEquals(expected, actual, message);
    }

    /// Test if an array contains all of a list of elements in the same order. The elements are considered the same
    /// if the `equals` method returns true.
    ///
    /// @param <E> the type of the element.
    /// @param expected  The elements the array must contain.
    /// @param actual    The array that should contain the elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the array does not contain all the elements or if either of the
    ///                              arguments is null.
    public static <E> void assertEquals(final Iterable<?> expected, final E[] actual,
                                        final Supplier<String> messageSupplier) {
        AssertEquals.assertEquals(expected, actual, messageSupplier);
    }

    /// Test if an iterable contains all of a set of elements in the same order. The elements are considered the same if
    ///  the `equals` method returns true.
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertEquals(final Iterable<?> expected, final Iterable<?> actual, final String message) {
        AssertEquals.assertEquals(expected, actual, message);
    }

    /// Test if an iterable contains all of an array of elements in the same order. The elements are considered the same
    /// if the `equals` method returns true.
    ///
    /// @param <E> the type of the element.
    /// @param expected  An array of the elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is `null`.
    public static <E> void assertEquals(final E[] expected, final Iterable<?> actual, final String message) {
        AssertEquals.assertEquals(expected, actual, message);
    }

    /// Test if an iterable contains all of a set of elements in the same order. The elements are considered the same if
    /// the `equals` method returns true.
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is `null`.
    public static void assertEquals(final Iterable<?> expected, final Iterable<?> actual,
                                    final Supplier<String> messageSupplier) {
        AssertEquals.assertEquals(expected, actual, messageSupplier);
    }

    /// Test if an iterable contains all of an array of elements in the same order. The elements are considered the same
    /// if the `equals` method returns true.
    ///
    /// @param <E> the type of the element.
    /// @param expected  An array of the elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the
    ///                              arguments is null.
    public static <E> void assertEquals(final E[] expected, final Iterable<?> actual,
                                        final Supplier<String> messageSupplier) {
        AssertEquals.assertEquals(expected, actual, messageSupplier);
    }







    /// Test if an iterable contains all of a set of elements in the same order. The elements are considered the same if
    /// only if they same object.
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertEqualsByIdentity(final Iterable<?> expected, final Iterable<?> actual) {
        AssertEqualsByIdentity.assertEqualsByIdentity(expected, actual);
    }

    /// Test if an iterable contains all of an array of elements in the same order. The elements are considered the same
    /// only if they same object.
    ///
    /// @param <E> the type of the element.
    /// @param expected  An array of the elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static <E> void assertEqualsByIdentity(final E[] expected, final Iterable<?> actual) {
        AssertEqualsByIdentity.assertEqualsByIdentity(expected, actual);
    }

    /// Test if an array contains all of a list of elements in the same order. The elements are considered the same
    /// only if they same object.
    ///
    /// @param <E> the type of the element.
    /// @param expected  The elements the array must contain.
    /// @param actual    The array that should contain the elements.
    /// @throws AssertionFailedError if the array does not contain all the elements or if either of the
    ///                              arguments is null.
    public static <E> void assertEqualsByIdentity(final Iterable<?> expected, final E[] actual) {
        AssertEqualsByIdentity.assertEqualsByIdentity(expected, actual);
    }

    /// Test if an array contains all of a list of elements in the same order. The elements are considered the same
    /// only if they same object.
    ///
    /// @param <E> the type of the element.
    /// @param expected  The elements the array must contain.
    /// @param actual    The array that should contain the elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the array does not contain all the elements or if either of the
    ///                              arguments is null.
    public static <E> void assertEqualsByIdentity(final Iterable<?> expected, final E[] actual, String message) {
        AssertEqualsByIdentity.assertEqualsByIdentity(expected, actual, message);
    }

    /// Test if an array contains all of a list of elements in the same order. The elements are considered the same
    /// only if they same object.
    ///
    /// @param <E> the type of the element.
    /// @param expected  The elements the array must contain.
    /// @param actual    The array that should contain the elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the array does not contain all the elements or if either of the
    ///                              arguments is null.
    public static <E> void assertEqualsByIdentity(final Iterable<?> expected, final E[] actual,
                                        final Supplier<String> messageSupplier) {
        AssertEqualsByIdentity.assertEqualsByIdentity(expected, actual, messageSupplier);
    }

    /// Test if an iterable contains all of a set of elements in the same order. The elements are considered the same
    /// only if they same object.
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertEqualsByIdentity(final Iterable<?> expected, final Iterable<?> actual, final String message) {
        AssertEqualsByIdentity.assertEqualsByIdentity(expected, actual, message);
    }

    /// Test if an iterable contains all of an array of elements in the same order. The elements are considered the same
    /// only if they same object.
    ///
    /// @param <E> the type of the element.
    /// @param expected  An array of the elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is `null`.
    public static <E> void assertEqualsByIdentity(final E[] expected, final Iterable<?> actual, final String message) {
        AssertEqualsByIdentity.assertEqualsByIdentity(expected, actual, message);
    }

    /// Test if an iterable contains all of a set of elements in the same order. The elements are considered the same
    /// only if they same object.
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is `null`.
    public static void assertEqualsByIdentity(final Iterable<?> expected, final Iterable<?> actual,
                                    final Supplier<String> messageSupplier) {
        AssertEqualsByIdentity.assertEqualsByIdentity(expected, actual, messageSupplier);
    }

    /// Test if an iterable contains all of an array of elements in the same order. The elements are considered the same
    /// only if they same object.
    ///
    /// @param <E> the type of the element.
    /// @param expected  An array of the elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the
    ///                              arguments is null.
    public static <E> void assertEqualsByIdentity(final E[] expected, final Iterable<?> actual,
                                        final Supplier<String> messageSupplier) {
        AssertEqualsByIdentity.assertEqualsByIdentity(expected, actual, messageSupplier);
    }


    /// Test if an iterable contains all of a set of elements. Two elements are only considered the same is they
    /// have the same identity (*i.e* they are the same object).
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertContainsAllByIdentity(final Iterable<?> expected, final Iterable<?> actual) {
        AssertContainsAllByIdentity.assertContainsAllByIdentity(expected, actual);
    }

    /// Test if an iterable contains all of a set of elements. Two elements are only considered the same is they
    /// have the same identity (*i.e* they are the same object).
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertContainsAllByIdentity(final Iterable<?> expected, final Iterable<?> actual,
            final String message) {
        AssertContainsAllByIdentity.assertContainsAllByIdentity(expected, actual, message);
    }

    /// Test if an iterable contains all of a set of elements. Two elements are only considered the same is they
    /// have the same identity (*i.e* they are the same object).
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertContainsAllByIdentity(final Iterable<?> expected, final Iterable<?> actual,
            final Supplier<String> messageSupplier) {
        AssertContainsAllByIdentity.assertContainsAllByIdentity(expected, actual, messageSupplier);
    }

    /// Test if an iterable contains no element from a set of elements. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param excluded  The elements the iterable must not contain.
    /// @param actual    The iterable that should not contain the elements.
    /// @throws AssertionFailedError if the iterable contains any of the elements or if either of the iterable
    ///     arguments is null.
    public static void assertContainsNone(final Iterable<?> excluded, final Iterable<?> actual) {
        AssertContainsNone.assertContainsNone(excluded, actual);
    }

    /// Test if an iterable contains no element from a set of elements. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param excluded  The elements the iterable must not contain.
    /// @param actual    The iterable that should not contain the elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable contains any of the elements or if either of the iterable
    ///     arguments is null.
    public static void assertContainsNone(final Iterable<?> excluded, final Iterable<?> actual,
            final String message) {
        AssertContainsNone.assertContainsNone(excluded, actual, message);
    }

    /// Test if an iterable contains no element from a set of elements. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param excluded          The elements the iterable must not contain.
    /// @param actual            The iterable that should not contain the elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable contains any of the elements or if either of the iterable
    ///    arguments is null.
    public static void assertContainsNone(final Iterable<?> excluded, final Iterable<?> actual,
            final Supplier<String> messageSupplier) {
        AssertContainsNone.assertContainsNone(excluded, actual, messageSupplier);
    }

    /// A collection of utility methods that support asserting that two collections contain the same elements.
    /// The iterators must have the same number of elements, and for each element in the iterator being tested
    /// (actual), there must be an element in the comparison iterator (expected) that returns true when the
    /// equals()` method is called.
    ///
    /// This function should be called through the [CollectionAssertions] class:
    /// ```java
    ///    Iterable<Integer> actual = ...
    ///    Iterable<Integer> expected = ...
    ///    CollectionAssertions.assertContainsSame(expected, actual);
    /// ```
    ///
    /// @param expected  The iterable with the expected set of elements.
    /// @param actual    The iterable with the actual set of elements.
    /// @throws AssertionFailedError if the iterable does not contain all the elements.
    public static void assertContainsSame(final Iterable<?> expected, final Iterable<?> actual) {
        AssertContainsSame.assertContainsSame(expected, actual);
    }

    /// A collection of utility methods that support asserting that two collections contain the same elements.
    /// The iterators must have the same number of elements, and for each element in the iterator being tested
    /// (actual), there must be an element in the comparison iterator (expected) that returns true when the
    /// equals()` method is called.
    ///
    /// This function should be called through the [CollectionAssertions] class:
    /// ```java
    ///    Iterable<Integer> actual = ...
    ///    Iterable<Integer> expected = ...
    ///    CollectionAssertions.assertContainsSame(expected, actual, "Iterators are not the same");
    /// ```
    ///
    /// @param expected  The iterable with the expected set of elements.
    /// @param actual    The iterable with the actual set of elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements.
    public static void assertContainsSame(final Iterable<?> expected, final Iterable<?> actual,
            final String message) {
        AssertContainsSame.assertContainsSame(expected, actual, message);
    }

    /// A collection of utility methods that support asserting that two collections contain the same elements.
    /// The iterators must have the same number of elements, and for each element in the iterator being tested
    /// (actual), there must be an element in the comparison iterator (expected) that returns true when the
    /// equals()` method is called.
    ///
    /// This function should be called through the [CollectionAssertions] class:
    /// ```java
    ///    Iterable<Integer> actual = ...
    ///    Iterable<Integer> expected = ...
    ///    CollectionAssertions.assertContainsSame(expected, actual, () -> "Iterators are not the same");
    /// ```
    ///
    /// @param expected  The iterable with the expected set of elements.
    /// @param actual    The iterable with the actual set of elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements.
    public static void assertContainsSame(final Iterable<?> expected, final Iterable<?> actual,
            final Supplier<String> messageSupplier) {
        AssertContainsSame.assertContainsSame(expected, actual, messageSupplier);
    }

    /// Test if two iterables contain the same set of elements. Two elements are only considered the same is they
    /// have the same identity (*i.e* they are the same object).
    ///
    /// @param expected  The iterable with the expected set of elements.
    /// @param actual    The iterable with the actual set of elements.
    /// @throws AssertionFailedError if the iterable does not contain all the elements.
    public static void assertContainsSameByIdentity(final Iterable<?> expected, final Iterable<?> actual) {
        AssertContainsSameByIdentity.assertContainsSameByIdentity(expected, actual);
    }

    /// Test if two iterables contain the same set of elements. Two elements are only considered the same is they
    /// have the same identity (*i.e* they are the same object).
    ///
    /// @param expected  The iterable with the expected set of elements.
    /// @param actual    The iterable with the actual set of elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements.
    public static void assertContainsSameByIdentity(final Iterable<?> expected, final Iterable<?> actual,
            final String message) {
        AssertContainsSameByIdentity.assertContainsSameByIdentity(expected, actual, message);
    }

    /// Test if two iterables contain the same set of elements. Two elements are only considered the same is they
    /// have the same identity (*i.e* they are the same object).
    ///
    /// @param expected  The iterable with the expected set of elements.
    /// @param actual    The iterable with the actual set of elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements.
    public static void assertContainsSameByIdentity(final Iterable<?> expected, final Iterable<?> actual,
            final Supplier<String> messageSupplier) {
        AssertContainsSameByIdentity.assertContainsSameByIdentity(expected, actual, messageSupplier);
    }

    /// Test if an iterable does not contain a specific element. The elements are considered the same if the `equals` method
    /// returns true.
    ///
    /// @param expected  The element it should not contain
    /// @param actual    The iterable that should not contain the elements.
    /// @throws AssertionFailedError if the iterable contains the element or if either of the iterable
    ///                              arguments is null.
    public static void assertDoesNotContain(final Object expected, final Iterable<?> actual) {
        AssertDoesNotContain.assertDoesNotContain(expected, actual);
    }
    /// Test if an iterable does not contain a specific element. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param expected  The element it should not contain
    /// @param actual    The iterable that should not contain the element.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable contains the element or if either of the iterable
    ///                              arguments is null.
    public static void assertDoesNotContain(final Object expected, final Iterable<?> actual, String message) {
        AssertDoesNotContain.assertDoesNotContain(expected, actual, message);
    }

    /// Test if an iterable does not contain a specific element. The elements are considered the same if the
    /// `equals` method returns true.
    ///
    /// @param expected  The element it should not contain
    /// @param actual    The iterable that should not contain the element.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable contains the element or if either of the iterable
    ///                              arguments is null.
    public static void assertDoesNotContain(final Object expected, final Iterable<?> actual,
                                            final Supplier<String> messageSupplier) {
        AssertDoesNotContain.assertDoesNotContain(expected, actual, messageSupplier);
    }
}
