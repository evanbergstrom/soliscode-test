/*
 * Copyright 2026 Evan Bergstrom
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
package com.soliscode.test.contract.charsequence;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CharSequenceContractSupport;

import static com.soliscode.test.contract.charsequence.CharSequenceChecks.describe;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [getChars(int, int, char\[\], int)][CharSequence#getChars(int, int, char\[\], int)] method of a
/// [CharSequence], new in Java 25.
///
/// It follows the Javadoc: `getChars(srcBegin, srcEnd, dst, dstBegin)` copies the `char`s from `srcBegin` to
/// `srcEnd - 1` into `dst` from `dstBegin`. It throws [IndexOutOfBoundsException] if `srcBegin` or `dstBegin` is
/// negative, if `srcBegin > srcEnd`, if `srcEnd > length()`, or if `dstBegin + srcEnd - srcBegin > dst.length`; and
/// [NullPointerException] if `dst` is null. The indexes are the content's, not the sequence's own `length()`.
///
/// It reads the Javadoc more strictly in one place: `dst` must be unchanged outside the subarray copied into, of which
/// the Javadoc says nothing. It reads it more loosely in two: a refused call is not checked for writing nothing, and
/// the partial copy the Javadoc allows under concurrent truncation is not tested.
///
/// @param <T> the class under test
/// @since 0.1.4
/// @see CharSequence#getChars(int, int, char[], int)
public interface GetCharsContract<T extends CharSequence> extends CharSequenceContractSupport<T> {

    /// Tests that `getChars(begin, end, dst, 2)` copies the content's `char`s from `begin` to `end - 1` into `dst`
    /// from index 2, and leaves the rest of `dst` unchanged, for every `0 <= begin <= end <= length` of every content.
    /// `dst` has two spare `char`s on each side, filled with the least `char` not in the content.
    @Test
    @DisplayName("getChars() copies the range into the destination from dstBegin, and nothing else")
    default void getCharsCopiesTheRangeIntoTheDestination() {
        for (final String s : contents()) {
            T sequence = create(s);
            char sentinel = 0;
            while (s.indexOf(sentinel) >= 0) {
                sentinel++;
            }
            for (int begin = 0; begin <= s.length(); begin++) {
                for (int end = begin; end <= s.length(); end++) {
                    char[] expected = new char[s.length() + 4];
                    Arrays.fill(expected, sentinel);
                    for (int i = begin; i < end; i++) {
                        expected[2 + i - begin] = s.charAt(i);
                    }
                    char[] dst = new char[s.length() + 4];
                    Arrays.fill(dst, sentinel);
                    sequence.getChars(begin, end, dst, 2);
                    String call = "getChars(" + begin + ", " + end + ", dst, 2) of " + describe(s);
                    assertArrayEquals(expected, dst, () -> call + ", into a char[" + dst.length + "]");
                }
            }
        }
    }

    /// Tests that `getChars` throws [IndexOutOfBoundsException] for each rule, each case breaking it: a negative
    /// `srcBegin` `(-1, 0)`, a negative `dstBegin` `(0, 0)` at -1, `srcBegin > srcEnd` `(1, 0)`, `srcEnd` past the
    /// length `(0, length + 1)`, all into a `char[length + 4]`, and a destination too small, `(0, length)` into a
    /// `char[length]` at 1, for every content.
    @Test
    @DisplayName("getChars() out of bounds throws IndexOutOfBoundsException")
    default void getCharsOutOfBoundsThrowsIndexOutOfBoundsException() {
        for (final String s : contents()) {
            T sequence = create(s);
            int n = s.length();
            int[][] cases = {{-1, 0, n + 4, 0}, {0, 0, n + 4, -1}, {1, 0, n + 4, 0}, {0, n + 1, n + 4, 0},
                {0, n, n, 1}};
            for (final int[] c : cases) {
                assertThrows(IndexOutOfBoundsException.class,
                        () -> sequence.getChars(c[0], c[1], new char[c[2]], c[3]),
                        () -> "getChars(" + c[0] + ", " + c[1] + ", new char[" + c[2] + "], " + c[3] + ") of "
                                + describe(s));
            }
        }
    }

    /// Tests that `getChars(0, length, null, 0)` throws [NullPointerException], for every content.
    @Test
    @DisplayName("getChars() into a null destination throws NullPointerException")
    default void getCharsNullDestinationThrowsNullPointerException() {
        for (final String s : contents()) {
            T sequence = create(s);
            assertThrows(NullPointerException.class, () -> sequence.getChars(0, s.length(), null, 0),
                    () -> "getChars(0, " + s.length() + ", null, 0) of " + describe(s));
        }
    }
}
