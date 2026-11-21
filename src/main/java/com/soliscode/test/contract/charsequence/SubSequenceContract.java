/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.charsequence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CharSequenceContractSupport;

import static com.soliscode.test.contract.charsequence.CharSequenceChecks.describe;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [subSequence(int, int)][CharSequence#subSequence(int, int)] method of a [CharSequence].
///
/// It follows the Javadoc: the subsequence holds the `char`s from `start` to `end - 1`, and its length is
/// `end - start`. It throws [IndexOutOfBoundsException] "if `start` or `end` are negative, if `end` is greater than
/// `length()`, or if `start` is greater than `end`". The indexes are the content's, not the sequence's own
/// `length()`.
///
/// It reads the Javadoc more loosely in one place: a refused call is not checked for leaving the sequence unchanged,
/// and which exception comes first, where a call breaks several rules, is not checked.
///
/// @param <T> the class under test
/// @since 0.1.4
/// @see CharSequence#subSequence(int, int)
public interface SubSequenceContract<T extends CharSequence> extends CharSequenceContractSupport<T> {

    /// Tests that `subSequence(start, end)` holds the content's `char`s from `start` to `end - 1`, by its `length()`,
    /// each `charAt` and its `toString()`, for every `0 <= start <= end <= length` of every content.
    @Test
    @DisplayName("subSequence() holds the chars from start to end - 1")
    default void subSequenceHoldsTheCharsFromStartToEnd() {
        for (final String s : contents()) {
            T sequence = create(s);
            for (int start = 0; start <= s.length(); start++) {
                for (int end = start; end <= s.length(); end++) {
                    String call = "subSequence(" + start + ", " + end + ") of " + describe(s);
                    CharSequence sub = sequence.subSequence(start, end);
                    assertNotNull(sub, call);
                    assertEquals(end - start, sub.length(), () -> "length() of " + call);
                    for (int i = 0; i < end - start; i++) {
                        int index = i;
                        assertEquals(s.charAt(start + i), sub.charAt(i), () -> "charAt(" + index + ") of " + call);
                    }
                    assertEquals(s.substring(start, end), sub.toString(), () -> "toString() of " + call);
                }
            }
        }
    }

    /// Tests that `subSequence` throws [IndexOutOfBoundsException] for a negative `start` `(-1, 0)`, a negative `end`
    /// `(0, -1)`, an `end` past the length `(0, length + 1)`, and a `start` after `end` `(1, 0)`, for every content.
    @Test
    @DisplayName("subSequence() out of bounds throws IndexOutOfBoundsException")
    default void subSequenceOutOfBoundsThrowsIndexOutOfBoundsException() {
        for (final String s : contents()) {
            T sequence = create(s);
            int[][] ranges = {{-1, 0}, {0, -1}, {0, s.length() + 1}, {1, 0}};
            for (final int[] r : ranges) {
                assertThrows(IndexOutOfBoundsException.class, () -> sequence.subSequence(r[0], r[1]),
                        () -> "subSequence(" + r[0] + ", " + r[1] + ") of " + describe(s));
            }
        }
    }
}
