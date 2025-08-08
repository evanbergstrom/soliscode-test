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

import java.util.Iterator;
import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// `AssertEquals` is a collection of utility methods that support asserting that a collection
/// contains specific elements in the expected order.
///
/// @author evanbergstrom
/// @since 1.0.0
final class AssertEqualsByIdentity extends IterableAssertion {
    private AssertEqualsByIdentity() {
    }

    /// Test if an iterable contains all of a set of elements in the same order. The elements are considered the same if
    /// the `equals` method returns true.
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertEqualsByIdentity(final Iterable<?> expected, final Iterable<?> actual) {
        checkEquals(expected, actual, null);
    }

    /// Test if an iterable contains all of an array of elements in the same order. The elements are considered the same
    /// if the `equals` method returns true.
    ///
    /// @param expected  An array of the elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static <E> void assertEqualsByIdentity(final E[] expected, final Iterable<?> actual) {
        checkEquals(asIterable(expected), actual, null);
    }

    /// Test if an array contains all of a list of elements in the same order. The elements are considered the same
    /// if the `equals` method returns true.
    ///
    /// @param expected  The elements the array must contain.
    /// @param actual    The array that should contain the elements.
    /// @throws AssertionFailedError if the array does not contain all the elements or if either of the
    ///                              arguments is null.
    public static <E> void assertEqualsByIdentity(final Iterable<?> expected, final E[] actual) {
        checkEquals(expected, asIterable(actual), null);
    }

    /// Test if an iterable contains all of a set of elements in the same order. The elements are considered the same if
    ///  the `equals` method returns true.
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is null.
    public static void assertEqualsByIdentity(final Iterable<?> expected, final Iterable<?> actual, final String message) {
        checkEquals(expected, actual, message);
    }

    /// Test if an iterable contains all of an array of elements in the same order. The elements are considered the same
    /// if the `equals` method returns true.
    ///
    /// @param expected  An array of the elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is `null`.
    public static <E> void assertEqualsByIdentity(final E[] expected, final Iterable<?> actual, final String message) {
        checkEquals(asIterable(expected), actual, message);
    }

    /// Test if an array contains all of a set of elements in the same order. The elements are considered the same if
    ///  the `equals` method returns true.
    ///
    /// @param expected  The elements the array must contain.
    /// @param actual    The array that should contain the elements.
    /// @param message   The message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the arguments
    ///                              is null.
    public static <E> void assertEqualsByIdentity(final Iterable<?> expected, final E[] actual, final String message) {
        checkEquals(expected, asIterable(actual), message);
    }

    /// Test if an iterable contains all of a set of elements in the same order. The elements are considered the same if
    /// the `equals` method returns true.
    ///
    /// @param expected  The elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the iterable
    ///                              arguments is `null`.
   public static void assertEqualsByIdentity(final Iterable<?> expected, final Iterable<?> actual,
                                   final Supplier<String> messageSupplier) {
       checkEquals(expected, actual, messageSupplier);
   }

   /// Test if an iterable contains all of an array of elements in the same order. The elements are considered the same
   /// if the `equals` method returns true.
   ///
   /// @param expected  An array of the elements the iterable must contain.
   /// @param actual    The iterable that should contain the elements.
   /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
   /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the
   ///                              arguments is null.
   public static <E> void assertEqualsByIdentity(final E[] expected, final Iterable<?> actual,
                                       final Supplier<String> messageSupplier) {
        checkEquals(asIterable(expected), actual, messageSupplier);
   }

    /// Test if an iterable contains all of an array of elements in the same order. The elements are considered the same
    /// if the `equals` method returns true.
    ///
    /// @param expected  An array of the elements the iterable must contain.
    /// @param actual    The iterable that should contain the elements.
    /// @param messageSupplier   The supplier to call top generate  message to supply if the assertion fails.
    /// @throws AssertionFailedError if the iterable does not contain all the elements or if either of the
    ///                              arguments is null.
    public static <E> void assertEqualsByIdentity(final Iterable<?> expected, final E[] actual,
                                                  final Supplier<String> messageSupplier) {
        checkEquals(expected, asIterable(actual), messageSupplier);
    }

    private static void checkEquals(final Iterable<?> expected, final Iterable<?> actual,
                                   final Object messageOrSupplier) {
       assertIterablesNotNull(expected, actual, messageOrSupplier);

       if (expected == actual) {
           return;
       }

       Iterator<?> expectedIterator = expected.iterator();
       Iterator<?> actualIterator = expected.iterator();

       while (expectedIterator.hasNext()) {
           if (!actualIterator.hasNext()) {
               throw buildException(expected, actual, messageOrSupplier);
           }
           if (expectedIterator.next() != actualIterator.next()) {
               throw buildException(expected, actual, messageOrSupplier);
           }
       }
       if (actualIterator.hasNext()) {
           throw buildException(expected, actual, messageOrSupplier);
       }
   }

    private static AssertionFailedError buildException(final Iterable<?> expected, final Iterable<?> actual,
           final Object messageOrSupplier) {
       return assertionFailure()
               .message(messageOrSupplier)
               .expected(expected)
               .actual(actual)
               .build();
    }
}
