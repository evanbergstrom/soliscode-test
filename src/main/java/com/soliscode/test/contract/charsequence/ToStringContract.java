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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CharSequenceContractSupport;

import static com.soliscode.test.contract.charsequence.CharSequenceChecks.describe;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/// Contract for the [toString()][CharSequence#toString()] method of a [CharSequence].
///
/// It follows the Javadoc: `toString()` is "a string consisting of exactly this sequence of characters", of the
/// sequence's length.
///
/// @param <T> the class under test
/// @since 0.1.4
/// @see CharSequence#toString()
public interface ToStringContract<T extends CharSequence> extends CharSequenceContractSupport<T> {

    /// Tests that `toString()` equals the content, for every content.
    @Test
    @DisplayName("toString() holds the same chars, in the same order")
    default void toStringHoldsTheSameChars() {
        for (final String s : contents()) {
            String actual = create(s).toString();
            assertNotNull(actual, () -> "toString() of " + describe(s));
            assertEquals(s, actual, () -> "toString() of " + describe(s) + " is " + describe(actual));
        }
    }
}
