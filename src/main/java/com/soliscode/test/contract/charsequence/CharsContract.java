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
