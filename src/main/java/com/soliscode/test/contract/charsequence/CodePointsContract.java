/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.charsequence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CharSequenceContractSupport;

import static com.soliscode.test.contract.charsequence.CharSequenceChecks.codePointsOf;
import static com.soliscode.test.contract.charsequence.CharSequenceChecks.describe;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/// Contract for the [codePoints()][CharSequence#codePoints()] method of a [CharSequence].
///
/// It follows the Javadoc: each surrogate pair is combined "as if by `Character.toCodePoint`", and every other code
/// unit, unpaired surrogates included, is zero-extended. The expected code points are decoded by the contract, not by
/// a JDK `codePoints()`. As for `chars()`, the late binding to a mutable sequence is not tested.
///
/// @param <T> the class under test
/// @since 0.1.4
/// @see CharSequence#codePoints()
public interface CodePointsContract<T extends CharSequence> extends CharSequenceContractSupport<T> {

    /// Tests that `codePoints()` streams the content's code points, for every content.
    @Test
    @DisplayName("codePoints() combines surrogate pairs and zero-extends every other char")
    default void codePointsCombinesSurrogatePairsAndZeroExtendsTheRest() {
        for (final String s : contents()) {
            assertArrayEquals(codePointsOf(s), create(s).codePoints().toArray(),
                    () -> "codePoints() of " + describe(s));
        }
    }
}
