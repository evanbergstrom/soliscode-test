/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.charsequence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/// Tests for `CharSequenceChecks`: the contract's own oracles, on values worked out by hand.
///
/// @see CharSequenceChecks
@DisplayName("Tests for CharSequenceChecks")
public class CharSequenceChecksTest {

    private static final String HIGH = "\uD83D"; // a high surrogate alone: no character to write
    private static final String LOW = "\uDE00"; // a low surrogate alone: no character to write
    private static final String MAX = "\uFFFF"; // U+FFFF, the greatest char

    /// Default constructor.
    public CharSequenceChecksTest() {
        super();
    }

    @Test
    @DisplayName("charsOf() zero-extends each char")
    void charsOfZeroExtends() {
        assertArrayEquals(new int[] {}, CharSequenceChecks.charsOf(""));
        assertArrayEquals(new int[] {0x61, 0xFFFF, 0xD83D}, CharSequenceChecks.charsOf("a" + MAX + HIGH));
    }

    @Test
    @DisplayName("codePointsOf() combines pairs and zero-extends unpaired surrogates")
    void codePointsOfCombinesPairs() {
        assertArrayEquals(new int[] {0x61, 0x1F600, 0x62}, CharSequenceChecks.codePointsOf("a" + HIGH + LOW + "b"));
        assertArrayEquals(new int[] {0xD83D, 0x78}, CharSequenceChecks.codePointsOf(HIGH + "x"));
        assertArrayEquals(new int[] {0x61, 0x62, 0xD83D}, CharSequenceChecks.codePointsOf("ab" + HIGH));
        assertArrayEquals(new int[] {0xDE00, 0xD83D}, CharSequenceChecks.codePointsOf(LOW + HIGH));
    }

    @Test
    @DisplayName("compareByChar() is lexicographic by char value, the shorter first on a common prefix")
    void compareByCharIsLexicographic() {
        assertEquals(0, CharSequenceChecks.compareByChar("ab", "ab"));
        assertEquals(-1, CharSequenceChecks.compareByChar("ab", "b"));
        assertEquals(1, CharSequenceChecks.compareByChar("b", "ab"));
        assertEquals(-1, CharSequenceChecks.compareByChar("ab", "abc"));
        assertEquals(-1, CharSequenceChecks.compareByChar("", "a"));
        assertEquals(1, CharSequenceChecks.compareByChar(MAX, HIGH + LOW));
        assertEquals(1, CharSequenceChecks.compareByChar("é", "hello, world"));
    }

    @Test
    @DisplayName("describe() quotes a content and escapes what is not printable ASCII")
    void describeEscapes() {
        assertEquals("\"hello, world\"", CharSequenceChecks.describe("hello, world"));
        assertEquals("\"a\\uD83D\\uDE00\\u00E9\\u005C\\u0022\"",
                CharSequenceChecks.describe("a" + HIGH + LOW + "é\\\""));
    }
}
