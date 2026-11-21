/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.charsequence;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.CharSequenceContractSupport;

import static com.soliscode.test.contract.charsequence.CharSequenceChecks.compareByChar;
import static com.soliscode.test.contract.charsequence.CharSequenceChecks.describe;
import static org.junit.jupiter.api.Assertions.assertEquals;

/// Contract for the static [CharSequence#compare(CharSequence, CharSequence)] method, on the class under test.
///
/// It follows the Javadoc: `compare` is lexicographic by `char` value. At the first index where the two differ, their
/// `char`s are compared numerically; with no such index, the shorter sequence is less, and sequences of one length
/// are equal. The expected sign is computed by the contract, from the contents.
///
/// `compare` is static, so the class under test reaches it in two ways, and the test checks both:
/// - for two sequences of one class that is [Comparable], the JDK's `compare` returns that class's `compareTo`;
/// - for a sequence against a `String` (of another class, unless the class under test is `String`), it loops over
///   `charAt` and `length`.
///
/// It reads the Javadoc more strictly in one place: a `Comparable` class's natural order must be by `char`, as
/// String's, StringBuilder's and CharBuffer's are. The shortcut to `compareTo` is in the JDK's body of `compare`, not
/// in its Javadoc, but with another natural order `compare` would break its own Javadoc. A class with another natural
/// order overrides [com.soliscode.test.contract.support.CharSequenceContractSupport#naturalOrderIsByChar()] to return
/// `false`, and the test then checks only the comparisons against a `String` (the user's decision on #43).
///
/// It reads the Javadoc more loosely in one place: `compare` with a null argument is not tested. Its
/// [NullPointerException] comes from the static method itself, not from the class under test.
///
/// @param <T> the class under test
/// @since 0.1.4
/// @see CharSequence#compare(CharSequence, CharSequence)
public interface CompareContract<T extends CharSequence> extends CharSequenceContractSupport<T> {

    /// Tests that `CharSequence.compare` gives the sign of the lexicographic comparison by `char`, for every pair of
    /// contents: of two sequences made of them, each made afresh so that equal contents are two objects, unless
    /// [#naturalOrderIsByChar()] is `false`; and of a sequence against the other content as a `String`, both ways.
    @Test
    @DisplayName("CharSequence.compare() is lexicographic by char value")
    default void compareIsLexicographicByChar() {
        List<String> contents = contents();
        boolean sameClass = naturalOrderIsByChar();
        for (final String a : contents) {
            for (final String b : contents) {
                int expected = compareByChar(a, b);
                String pair = describe(a) + ", " + describe(b);
                if (sameClass) {
                    assertEquals(expected, Integer.signum(CharSequence.compare(create(a), create(b))),
                            () -> "signum(compare(" + pair + ")) of two sequences of the class under test");
                }
                assertEquals(expected, Integer.signum(CharSequence.compare(create(a), b)),
                        () -> "signum(compare(" + pair + ")) of a sequence of the class under test and a String");
                assertEquals(expected, Integer.signum(CharSequence.compare(a, create(b))),
                        () -> "signum(compare(" + pair + ")) of a String and a sequence of the class under test");
            }
        }
    }
}
