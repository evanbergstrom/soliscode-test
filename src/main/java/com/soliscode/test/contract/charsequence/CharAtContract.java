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
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [charAt(int)][CharSequence#charAt(int)] method of a [CharSequence].
///
/// It follows the Javadoc: `charAt(index)` returns the `char` at `index`, a surrogate as itself, and throws
/// [IndexOutOfBoundsException] "if the `index` argument is negative or not less than `length()`". The indexes are
/// the content's, not the sequence's own `length()`, so that a fault of `length` is not reported here.
///
/// @param <T> the class under test
/// @since 0.1.4
/// @see CharSequence#charAt(int)
public interface CharAtContract<T extends CharSequence> extends CharSequenceContractSupport<T> {

    /// Tests that `charAt(i)` is the content's `char` at `i`, for every index of every content.
    @Test
    @DisplayName("charAt() returns the char at each index, a surrogate as itself")
    default void charAtReturnsEachChar() {
        for (final String s : contents()) {
            T sequence = create(s);
            for (int i = 0; i < s.length(); i++) {
                int index = i;
                assertEquals(s.charAt(i), sequence.charAt(i), () -> "charAt(" + index + ") of " + describe(s));
            }
        }
    }

    /// Tests that `charAt(-1)` and `charAt(length)` throw [IndexOutOfBoundsException], for every content.
    @Test
    @DisplayName("charAt() of a negative index, or of the length, throws IndexOutOfBoundsException")
    default void charAtOutOfBoundsThrowsIndexOutOfBoundsException() {
        for (final String s : contents()) {
            T sequence = create(s);
            for (final int index : new int[] {-1, s.length()}) {
                assertThrows(IndexOutOfBoundsException.class, () -> sequence.charAt(index),
                        () -> "charAt(" + index + ") of " + describe(s));
            }
        }
    }
}
