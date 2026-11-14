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
package com.soliscode.test.breakable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.soliscode.test.breakable.BreakableCharSequence.CHARS_SIGN_EXTENDS;
import static com.soliscode.test.breakable.BreakableCharSequence.CHAR_AT_IS_OFF_BY_ONE;
import static com.soliscode.test.breakable.BreakableCharSequence.CHAR_AT_NEGATIVE_INDEX_THROWS_ILLEGAL_ARGUMENT;
import static com.soliscode.test.breakable.BreakableCharSequence.CHAR_AT_PAST_THE_END_RETURNS_ZERO;
import static com.soliscode.test.breakable.BreakableCharSequence.CODE_POINTS_DOES_NOT_COMBINE_SURROGATE_PAIRS;
import static com.soliscode.test.breakable.BreakableCharSequence.CODE_POINTS_DROPS_UNPAIRED_SURROGATES;
import static com.soliscode.test.breakable.BreakableCharSequence.COMPARE_TO_COMPARES_CODE_POINTS;
import static com.soliscode.test.breakable.BreakableCharSequence.COMPARE_TO_COMPARES_LENGTHS_FIRST;
import static com.soliscode.test.breakable.BreakableCharSequence.GET_CHARS_ACCEPTS_A_NULL_DESTINATION;
import static com.soliscode.test.breakable.BreakableCharSequence.GET_CHARS_ALLOWS_A_NEGATIVE_DESTINATION_BEGIN;
import static com.soliscode.test.breakable.BreakableCharSequence.GET_CHARS_ALLOWS_A_NEGATIVE_SOURCE_BEGIN;
import static com.soliscode.test.breakable.BreakableCharSequence.GET_CHARS_ALLOWS_SOURCE_BEGIN_AFTER_END;
import static com.soliscode.test.breakable.BreakableCharSequence.GET_CHARS_ALLOWS_SOURCE_END_PAST_THE_LENGTH;
import static com.soliscode.test.breakable.BreakableCharSequence.GET_CHARS_COPIES_ONE_CHAR_TOO_MANY;
import static com.soliscode.test.breakable.BreakableCharSequence.GET_CHARS_DOES_NOT_CHECK_THE_DESTINATION_SIZE;
import static com.soliscode.test.breakable.BreakableCharSequence.GET_CHARS_IGNORES_THE_DESTINATION_OFFSET;
import static com.soliscode.test.breakable.BreakableCharSequence.IS_EMPTY_ALWAYS_RETURNS_FALSE;
import static com.soliscode.test.breakable.BreakableCharSequence.LENGTH_COUNTS_CODE_POINTS;
import static com.soliscode.test.breakable.BreakableCharSequence.SUB_SEQUENCE_ALLOWS_A_NEGATIVE_END;
import static com.soliscode.test.breakable.BreakableCharSequence.SUB_SEQUENCE_ALLOWS_A_NEGATIVE_START;
import static com.soliscode.test.breakable.BreakableCharSequence.SUB_SEQUENCE_ALLOWS_END_PAST_THE_LENGTH;
import static com.soliscode.test.breakable.BreakableCharSequence.SUB_SEQUENCE_ALLOWS_START_AFTER_END;
import static com.soliscode.test.breakable.BreakableCharSequence.SUB_SEQUENCE_TREATS_END_AS_A_LENGTH;
import static com.soliscode.test.breakable.BreakableCharSequence.TO_STRING_IS_OBJECTS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `BreakableCharSequence`: that it follows its delegate unbroken, and that each break has the effect it
/// describes.
///
/// @see BreakableCharSequence
@DisplayName("Tests for BreakableCharSequence")
public class BreakableCharSequenceTest {

    private static final String HIGH = "\uD83D"; // a high surrogate alone: no character to write
    private static final String LOW = "\uDE00"; // a low surrogate alone: no character to write
    private static final String MAX = "\uFFFF"; // U+FFFF, the greatest char

    /// A surrogate pair between two ASCII `char`s: four `char`s, three code points.
    private static final String PAIR = "a" + HIGH + LOW + "b";

    /// Default constructor.
    public BreakableCharSequenceTest() {
        super();
    }

    private static BreakableCharSequence of(final String s, final Break... breaks) {
        return new BreakableCharSequence(s, Set.of(breaks));
    }

    /// Copies `getChars(begin, end, dst, at)` into a `char[size]` of `'#'`, and returns it as a string.
    private static String copy(final BreakableCharSequence s, final int begin, final int end, final int size,
                               final int at) {
        char[] dst = new char[size];
        Arrays.fill(dst, '#');
        s.getChars(begin, end, dst, at);
        return new String(dst);
    }

    @Test
    @DisplayName("Unbroken, every method is the delegate's")
    void unbrokenIsTheDelegate() {
        BreakableCharSequence s = of(PAIR);
        assertEquals(4, s.length());
        assertEquals('b', s.charAt(3));
        assertThrows(IndexOutOfBoundsException.class, () -> s.charAt(4));
        assertThrows(IndexOutOfBoundsException.class, () -> s.charAt(-1));
        assertFalse(s.isEmpty());
        assertTrue(of("").isEmpty());
        assertEquals(HIGH + LOW, s.subSequence(1, 3).toString());
        assertThrows(IndexOutOfBoundsException.class, () -> s.subSequence(2, 1));
        assertEquals(PAIR, s.toString());
        assertArrayEquals(PAIR.chars().toArray(), s.chars().toArray());
        assertArrayEquals(new int[] {'a', 0x1F600, 'b'}, s.codePoints().toArray());
        assertEquals("#a" + HIGH + "#", copy(s, 0, 2, 4, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> copy(s, 0, 4, 4, 1));
        assertThrows(NullPointerException.class, () -> s.getChars(0, 1, null, 0));
        assertTrue(of("ab").compareTo(of("b")) < 0);
        assertEquals(0, of("ab").compareTo(of("ab")));
        assertThrows(NullPointerException.class, () -> s.compareTo(null));
        assertSame(PAIR, s.delegate());
    }

    @Test
    @DisplayName("equals and hashCode are Object's")
    void equalsIsIdentity() {
        BreakableCharSequence s = of("ab");
        assertNotEquals(s, of("ab"));
        assertEquals(s, s);
        assertEquals(System.identityHashCode(s), s.hashCode());
    }

    @Test
    @DisplayName("The constructor copies the breaks, and addBreak changes only the copy")
    void constructorCopiesTheBreaks() {
        Set<Break> breaks = new HashSet<>(Set.of(IS_EMPTY_ALWAYS_RETURNS_FALSE));
        BreakableCharSequence s = new BreakableCharSequence("", breaks);
        breaks.clear();
        assertFalse(s.isEmpty());
        s.addBreak(TO_STRING_IS_OBJECTS);
        assertTrue(s.toString().startsWith(BreakableCharSequence.class.getName() + "@"));
        assertEquals(Set.of(), breaks);
    }

    @Test
    @DisplayName("LENGTH_COUNTS_CODE_POINTS counts a surrogate pair as one")
    void lengthCountsCodePoints() {
        assertEquals(3, of(PAIR, LENGTH_COUNTS_CODE_POINTS).length());
        assertEquals(4, of(PAIR, LENGTH_COUNTS_CODE_POINTS).toString().length());
    }

    @Test
    @DisplayName("The charAt breaks: off by one, zero past the end, IllegalArgumentException for a negative index")
    void charAtBreaks() {
        BreakableCharSequence off = of("abc", CHAR_AT_IS_OFF_BY_ONE);
        assertEquals('b', off.charAt(0));
        assertEquals('c', off.charAt(1));
        assertEquals('c', off.charAt(2));
        assertThrows(IndexOutOfBoundsException.class, () -> off.charAt(3));
        BreakableCharSequence past = of("abc", CHAR_AT_PAST_THE_END_RETURNS_ZERO);
        assertEquals('\0', past.charAt(3));
        assertThrows(IndexOutOfBoundsException.class, () -> past.charAt(-1));
        BreakableCharSequence negative = of("abc", CHAR_AT_NEGATIVE_INDEX_THROWS_ILLEGAL_ARGUMENT);
        assertThrows(IllegalArgumentException.class, () -> negative.charAt(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> negative.charAt(3));
        assertEquals('a', negative.charAt(0));
    }

    @Test
    @DisplayName("IS_EMPTY_ALWAYS_RETURNS_FALSE makes the empty sequence not empty")
    void isEmptyAlwaysReturnsFalse() {
        assertFalse(of("", IS_EMPTY_ALWAYS_RETURNS_FALSE).isEmpty());
    }

    @Test
    @DisplayName("SUB_SEQUENCE_TREATS_END_AS_A_LENGTH checks the bounds, then takes end chars from start")
    void subSequenceTreatsEndAsALength() {
        BreakableCharSequence s = of("hello", SUB_SEQUENCE_TREATS_END_AS_A_LENGTH);
        assertEquals("ell", s.subSequence(1, 3).toString());
        assertEquals("llo", s.subSequence(2, 4).toString());
        assertEquals("hel", s.subSequence(0, 3).toString());
        assertThrows(IndexOutOfBoundsException.class, () -> s.subSequence(2, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> s.subSequence(0, 6));
    }

    @Test
    @DisplayName("Each subSequence bounds break acts on its own rule alone")
    void subSequenceBoundsBreaks() {
        BreakableCharSequence negativeStart = of("ab", SUB_SEQUENCE_ALLOWS_A_NEGATIVE_START);
        assertEquals("a", negativeStart.subSequence(-1, 1).toString());
        assertThrows(IndexOutOfBoundsException.class, () -> negativeStart.subSequence(-1, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> negativeStart.subSequence(0, -1));
        BreakableCharSequence negativeEnd = of("ab", SUB_SEQUENCE_ALLOWS_A_NEGATIVE_END);
        assertEquals("", negativeEnd.subSequence(0, -1).toString());
        assertThrows(IndexOutOfBoundsException.class, () -> negativeEnd.subSequence(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> negativeEnd.subSequence(1, 0));
        BreakableCharSequence afterEnd = of("ab", SUB_SEQUENCE_ALLOWS_START_AFTER_END);
        assertEquals("", afterEnd.subSequence(1, 0).toString());
        assertThrows(IndexOutOfBoundsException.class, () -> afterEnd.subSequence(0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> afterEnd.subSequence(3, 2));
        BreakableCharSequence pastLength = of("ab", SUB_SEQUENCE_ALLOWS_END_PAST_THE_LENGTH);
        assertEquals("ab", pastLength.subSequence(0, 3).toString());
        assertThrows(IndexOutOfBoundsException.class, () -> pastLength.subSequence(-1, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> pastLength.subSequence(1, 0));
    }

    @Test
    @DisplayName("TO_STRING_IS_OBJECTS gives Object's toString")
    void toStringIsObjects() {
        BreakableCharSequence s = of("ab", TO_STRING_IS_OBJECTS);
        assertEquals(BreakableCharSequence.class.getName() + "@" + Integer.toHexString(System.identityHashCode(s)),
                s.toString());
    }

    @Test
    @DisplayName("CHARS_SIGN_EXTENDS makes U+FFFF -1 and a surrogate negative")
    void charsSignExtends() {
        assertArrayEquals(new int[] {'a', -1, (short) 0xD83D},
                of("a" + MAX + HIGH, CHARS_SIGN_EXTENDS).chars().toArray());
    }

    @Test
    @DisplayName("The codePoints breaks: pairs not combined, unpaired surrogates dropped")
    void codePointsBreaks() {
        assertArrayEquals(PAIR.chars().toArray(),
                of(PAIR, CODE_POINTS_DOES_NOT_COMBINE_SURROGATE_PAIRS).codePoints().toArray());
        assertArrayEquals(new int[] {'a', 0x1F600, 'x'},
                of("a" + HIGH + LOW + LOW + "x" + HIGH, CODE_POINTS_DROPS_UNPAIRED_SURROGATES).codePoints().toArray());
    }

    @Test
    @DisplayName("COMPARE_TO_COMPARES_CODE_POINTS puts U+FFFF before U+1F600")
    void compareToComparesCodePoints() {
        assertTrue(of(MAX, COMPARE_TO_COMPARES_CODE_POINTS).compareTo(of(HIGH + LOW)) < 0);
        assertTrue(of(MAX).compareTo(of(HIGH + LOW)) > 0);
        assertTrue(of("ab", COMPARE_TO_COMPARES_CODE_POINTS).compareTo(of("abc")) < 0);
        assertEquals(0, of("ab", COMPARE_TO_COMPARES_CODE_POINTS).compareTo(of("ab")));
    }

    @Test
    @DisplayName("COMPARE_TO_COMPARES_LENGTHS_FIRST puts a shorter sequence first")
    void compareToComparesLengthsFirst() {
        assertTrue(of("b", COMPARE_TO_COMPARES_LENGTHS_FIRST).compareTo(of("ab")) < 0);
        assertTrue(of("ab", COMPARE_TO_COMPARES_LENGTHS_FIRST).compareTo(of("ac")) < 0);
        assertTrue(CharSequence.compare(of("b", COMPARE_TO_COMPARES_LENGTHS_FIRST), of("ab")) < 0);
    }

    @Test
    @DisplayName("The getChars value breaks: dstBegin ignored, one char too many")
    void getCharsValueBreaks() {
        assertEquals("bc####", copy(of("abcd", GET_CHARS_IGNORES_THE_DESTINATION_OFFSET), 1, 3, 6, 2));
        assertThrows(IndexOutOfBoundsException.class,
                () -> copy(of("abcd", GET_CHARS_IGNORES_THE_DESTINATION_OFFSET), 1, 3, 3, 2));
        BreakableCharSequence tooMany = of("abcd", GET_CHARS_COPIES_ONE_CHAR_TOO_MANY);
        assertEquals("##bcd#", copy(tooMany, 1, 3, 6, 2));
        assertEquals("##cd##", copy(tooMany, 2, 4, 6, 2));
        assertEquals("#bc", copy(tooMany, 1, 3, 3, 1));
    }

    @Test
    @DisplayName("Each getChars bounds break acts on its own rule alone")
    void getCharsBoundsBreaks() {
        BreakableCharSequence negativeSource = of("ab", GET_CHARS_ALLOWS_A_NEGATIVE_SOURCE_BEGIN);
        assertEquals("a###", copy(negativeSource, -1, 1, 4, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> copy(negativeSource, -1, 1, 4, -1));
        BreakableCharSequence negativeDestination = of("ab", GET_CHARS_ALLOWS_A_NEGATIVE_DESTINATION_BEGIN);
        assertEquals("ab##", copy(negativeDestination, 0, 2, 4, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> copy(negativeDestination, 0, 3, 4, -1));
        BreakableCharSequence afterEnd = of("ab", GET_CHARS_ALLOWS_SOURCE_BEGIN_AFTER_END);
        assertEquals("####", copy(afterEnd, 1, 0, 4, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> copy(afterEnd, 3, 2, 4, 0));
        BreakableCharSequence pastLength = of("ab", GET_CHARS_ALLOWS_SOURCE_END_PAST_THE_LENGTH);
        assertEquals("#ab#", copy(pastLength, 0, 3, 4, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> copy(pastLength, 0, 3, 2, 1));
        BreakableCharSequence tooSmall = of("abc", GET_CHARS_DOES_NOT_CHECK_THE_DESTINATION_SIZE);
        assertEquals("#ab", copy(tooSmall, 0, 3, 3, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> copy(tooSmall, 0, 3, 3, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> copy(tooSmall, 0, 4, 3, 1));
        BreakableCharSequence nullDestination = of("ab", GET_CHARS_ACCEPTS_A_NULL_DESTINATION);
        nullDestination.getChars(0, 2, null, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> copy(nullDestination, 0, 3, 4, 0));
    }

    @Test
    @DisplayName("It is serializable, with its breaks, where its delegate is")
    void serializes() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(of("ab", IS_EMPTY_ALWAYS_RETURNS_FALSE));
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            BreakableCharSequence copy = (BreakableCharSequence) in.readObject();
            assertEquals("ab", copy.toString());
            assertTrue(copy.hasBreak(IS_EMPTY_ALWAYS_RETURNS_FALSE));
        }
    }
}
