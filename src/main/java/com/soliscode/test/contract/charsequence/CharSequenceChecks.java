/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.charsequence;

import java.util.Arrays;
import java.util.Locale;

/// The oracles the char sequence contracts share, each computed from a content `String` with `length()` and
/// `charAt` alone, so that no sample's own `chars()`, `codePoints()` or `compareTo` computes what it is checked
/// against; and the form a failure message gives a content.
final class CharSequenceChecks {

    private CharSequenceChecks() { }

    /// The `char`s of a content, each zero-extended to an `int`, as `chars()` documents.
    ///
    /// @param s the content
    /// @return its `char` values
    static int[] charsOf(final String s) {
        int[] chars = new int[s.length()];
        for (int i = 0; i < s.length(); i++) {
            chars[i] = s.charAt(i);
        }
        return chars;
    }

    /// The code points of a content, as `codePoints()` documents: each surrogate pair combined as by
    /// [Character#toCodePoint(char, char)], and every other `char`, unpaired surrogates included, zero-extended.
    ///
    /// @param s the content
    /// @return its code points
    static int[] codePointsOf(final String s) {
        int[] points = new int[s.length()];
        int n = 0;
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i++);
            if (Character.isHighSurrogate(c) && i < s.length() && Character.isLowSurrogate(s.charAt(i))) {
                points[n++] = Character.toCodePoint(c, s.charAt(i++));
            } else {
                points[n++] = c;
            }
        }
        return Arrays.copyOf(points, n);
    }

    /// The sign of the lexicographic comparison of two contents by `char` value, as [CharSequence#compare] documents
    /// it: at the first index where they differ, the `char`s' numeric order; with no such index, the shorter first.
    ///
    /// @param a the first content
    /// @param b the second content
    /// @return -1, 0 or 1
    static int compareByChar(final String a, final String b) {
        int n = Math.min(a.length(), b.length());
        for (int i = 0; i < n; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return a.charAt(i) < b.charAt(i) ? -1 : 1;
            }
        }
        return Integer.signum(a.length() - b.length());
    }

    /// A content as a failure message gives it: quoted, with every `char` that is not printable ASCII, and the
    /// backslash and quote, written as a Java escape (a backslash, `u` and four hexadecimal digits).
    ///
    /// @param s the content
    /// @return its description
    static String describe(final String s) {
        StringBuilder b = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= ' ' && c <= '~' && c != '\\' && c != '"') {
                b.append(c);
            } else {
                b.append(String.format(Locale.ROOT, "\\u%04X", (int) c));
            }
        }
        return b.append('"').toString();
    }
}
