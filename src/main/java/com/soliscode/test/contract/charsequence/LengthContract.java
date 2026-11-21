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

/// Contract for the [length()][CharSequence#length()] method of a [CharSequence].
///
/// It follows the Javadoc: the length "is the number of 16-bit `char`s in the sequence", so a surrogate pair counts
/// as two.
///
/// @param <T> the class under test
/// @since 0.1.4
/// @see CharSequence#length()
public interface LengthContract<T extends CharSequence> extends CharSequenceContractSupport<T> {

    /// Tests that `length()` is the number of `char`s of the content, for every content.
    @Test
    @DisplayName("length() is the number of chars, a surrogate pair counting as two")
    default void lengthIsTheNumberOfChars() {
        for (final String s : contents()) {
            assertEquals(s.length(), create(s).length(), () -> "length() of " + describe(s));
        }
    }
}
