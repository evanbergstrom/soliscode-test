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
