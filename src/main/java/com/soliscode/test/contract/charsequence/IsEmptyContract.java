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

/// Contract for the [isEmpty()][CharSequence#isEmpty()] method of a [CharSequence].
///
/// It follows the Javadoc: `isEmpty()` is `true` "if `length()` is `0`, otherwise `false`". The expected value is
/// the content's, not the sequence's own `length()`, so that a fault of `length` is not reported here.
///
/// It is not `contract.collection.IsEmptyContract` or `contract.map.IsEmptyContract`.
///
/// @param <T> the class under test
/// @since 0.1.4
/// @see CharSequence#isEmpty()
public interface IsEmptyContract<T extends CharSequence> extends CharSequenceContractSupport<T> {

    /// Tests that `isEmpty()` is `true` exactly for the empty content.
    @Test
    @DisplayName("isEmpty() is true exactly when the length is zero")
    default void isEmptyIsTrueExactlyWhenTheLengthIsZero() {
        for (final String s : contents()) {
            assertEquals(s.isEmpty(), create(s).isEmpty(), () -> "isEmpty() of " + describe(s));
        }
    }
}
