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
