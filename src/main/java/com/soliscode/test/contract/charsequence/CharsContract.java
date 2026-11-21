/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.charsequence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CharSequenceContractSupport;

import static com.soliscode.test.contract.charsequence.CharSequenceChecks.charsOf;
import static com.soliscode.test.contract.charsequence.CharSequenceChecks.describe;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/// Contract for the [chars()][CharSequence#chars()] method of a [CharSequence].
///
/// It follows the Javadoc: the stream is of the `char` values, each zero-extended to an `int`, surrogates "passed
/// through uninterpreted". It reads the Javadoc more loosely in one place: that the stream binds to a mutable
/// sequence when its terminal operation starts is not tested, since `CharSequence` has no method to change one.
///
/// @param <T> the class under test
/// @since 0.1.4
/// @see CharSequence#chars()
public interface CharsContract<T extends CharSequence> extends CharSequenceContractSupport<T> {

    /// Tests that `chars()` streams each `char` of the content, zero-extended, for every content.
    @Test
    @DisplayName("chars() streams each char, zero-extended")
    default void charsZeroExtendsEachChar() {
        for (final String s : contents()) {
            assertArrayEquals(charsOf(s), create(s).chars().toArray(), () -> "chars() of " + describe(s));
        }
    }
}
