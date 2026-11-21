/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.soliscode.test.breakable.BreakableComparator.COMPARE_ALWAYS_RETURNS_ZERO;
import static com.soliscode.test.breakable.BreakableComparator.COMPARE_BREAKS_TIES_BY_IDENTITY;
import static com.soliscode.test.breakable.BreakableComparator.COMPARE_IS_CYCLIC;
import static com.soliscode.test.breakable.BreakableComparator.COMPARE_IS_NOT_SIGN_SYMMETRIC;
import static com.soliscode.test.breakable.BreakableComparator.COMPARE_IS_REVERSED;
import static com.soliscode.test.breakable.BreakableComparator.COMPARE_NULL_DOES_NOT_THROW;
import static com.soliscode.test.breakable.BreakableComparator.COMPARE_NULL_THROWS_ILLEGAL_ARGUMENT;
import static com.soliscode.test.breakable.BreakableComparator.COMPARE_OF_AN_OBJECT_WITH_ITSELF_IS_NOT_ZERO;
import static com.soliscode.test.breakable.BreakableComparator.COMPARE_TREATS_NEAR_VALUES_AS_EQUAL;
import static com.soliscode.test.breakable.BreakableComparator.REVERSED_KEEPS_THE_ORDER;
import static com.soliscode.test.breakable.BreakableComparator.THEN_COMPARING_ACCEPTS_NULL;
import static com.soliscode.test.breakable.BreakableComparator.THEN_COMPARING_CONSULTS_THE_OTHER_FIRST;
import static com.soliscode.test.breakable.BreakableComparator.THEN_COMPARING_DOUBLE_IGNORES_THE_KEY;
import static com.soliscode.test.breakable.BreakableComparator.THEN_COMPARING_IGNORES_THE_KEY;
import static com.soliscode.test.breakable.BreakableComparator.THEN_COMPARING_IGNORES_THE_OTHER;
import static com.soliscode.test.breakable.BreakableComparator.THEN_COMPARING_INT_IGNORES_THE_KEY;
import static com.soliscode.test.breakable.BreakableComparator.THEN_COMPARING_LONG_IGNORES_THE_KEY;
import static com.soliscode.test.breakable.BreakableComparator.THEN_COMPARING_REVERSES_THE_KEY_COMPARATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `BreakableComparator`: that it follows its delegate unbroken, and that each break has the effect it
/// describes and reaches no other method.
///
/// @see BreakableComparator
@DisplayName("Tests for BreakableComparator")
public class BreakableComparatorTest {

    /// Strings of one length compare as equal, so that the `thenComparing` forms have ties to break.
    private static final Comparator<String> BY_LENGTH = Comparator.comparingInt(String::length);

    /// Default constructor.
    public BreakableComparatorTest() {
        super();
    }

    private static BreakableComparator<Integer> natural(final Break... breaks) {
        return new BreakableComparator<>(Comparator.<Integer>naturalOrder(), Set.of(breaks));
    }

    private static BreakableComparator<String> byLength(final Break... breaks) {
        return new BreakableComparator<>(BY_LENGTH, Set.of(breaks));
    }

    private static int sign(final Comparator<Integer> c, final Integer x, final Integer y) {
        return Integer.signum(c.compare(x, y));
    }

    private static int sign(final Comparator<String> c, final String x, final String y) {
        return Integer.signum(c.compare(x, y));
    }

    @Test
    @DisplayName("Unbroken, compare is the delegate's, and a null argument throws NullPointerException")
    void unbrokenFollowsItsDelegate() {
        BreakableComparator<Integer> c = natural();
        assertEquals(-1, sign(c, 1, 2));
        assertEquals(1, sign(c, 2, 1));
        assertEquals(0, sign(c, 2, 2));
        assertThrows(NullPointerException.class, () -> c.compare(null, 1));
        assertThrows(NullPointerException.class, () -> c.compare(1, null));
        assertThrows(NullPointerException.class, () -> c.compare(null, null));
        BreakableComparator<Integer> overNullsFirst = new BreakableComparator<>(
                Comparator.nullsFirst(Comparator.<Integer>naturalOrder()), Set.of());
        assertThrows(NullPointerException.class, () -> overNullsFirst.compare(null, 1));
    }

    @Test
    @DisplayName("Unbroken, reversed() and each thenComparing form are Comparator's defaults over its compare")
    void unbrokenDefaults() {
        BreakableComparator<String> c = byLength();
        assertEquals(1, sign(c.reversed(), "a", "bb"));
        Comparator<String> natural = Comparator.naturalOrder();
        assertEquals(-1, sign(c.thenComparing(natural), "a", "b"));
        assertEquals(1, sign(c.thenComparing(natural), "bb", "a"));
        assertEquals(-1, sign(c.thenComparing(Function.identity()), "a", "b"));
        assertEquals(1, sign(c.thenComparing(Function.identity(), Comparator.reverseOrder()), "a", "b"));
        assertEquals(-1, sign(c.thenComparingInt(s -> s.charAt(0)), "a", "b"));
        assertEquals(-1, sign(c.thenComparingLong(s -> s.charAt(0)), "a", "b"));
        assertEquals(-1, sign(c.thenComparingDouble(s -> s.charAt(0)), "a", "b"));
        assertThrows(NullPointerException.class, () -> c.thenComparing((Comparator<String>) null));
        assertThrows(NullPointerException.class, () -> c.thenComparing((Function<String, String>) null));
        assertThrows(NullPointerException.class, () -> c.thenComparing(Function.identity(), null));
        assertThrows(NullPointerException.class, () -> c.thenComparingInt(null));
        assertThrows(NullPointerException.class, () -> c.thenComparingLong(null));
        assertThrows(NullPointerException.class, () -> c.thenComparingDouble(null));
    }

    @Test
    @DisplayName("A break of compare is seen through reversed() and thenComparing()")
    void compareBreaksReachTheDefaults() {
        BreakableComparator<String> c = byLength(COMPARE_IS_REVERSED);
        assertEquals(1, sign(c, "a", "bb"));
        assertEquals(-1, sign(c.reversed(), "a", "bb"));
        assertEquals(1, sign(c.thenComparing(Comparator.naturalOrder()), "a", "bb"));
    }

    @Test
    @DisplayName("COMPARE_IS_REVERSED and COMPARE_ALWAYS_RETURNS_ZERO change the order, not null's refusal")
    void reversedAndZero() {
        assertEquals(1, sign(natural(COMPARE_IS_REVERSED), 1, 2));
        assertEquals(0, sign(natural(COMPARE_IS_REVERSED), 2, 2));
        assertEquals(0, sign(natural(COMPARE_ALWAYS_RETURNS_ZERO), 1, 2));
        assertThrows(NullPointerException.class, () -> natural(COMPARE_IS_REVERSED).compare(null, 1));
        assertThrows(NullPointerException.class, () -> natural(COMPARE_ALWAYS_RETURNS_ZERO).compare(1, null));
    }

    @Test
    @DisplayName("COMPARE_OF_AN_OBJECT_WITH_ITSELF_IS_NOT_ZERO returns 1 for the same object only")
    void selfIsNotZero() {
        BreakableComparator<Integer> c = natural(COMPARE_OF_AN_OBJECT_WITH_ITSELF_IS_NOT_ZERO);
        Integer x = Integer.valueOf(100_000);
        assertEquals(1, sign(c, x, x));
        assertEquals(0, sign(c, x, Integer.valueOf(100_000)));
        assertEquals(-1, sign(c, 1, 2));
    }

    @Test
    @DisplayName("COMPARE_IS_NOT_SIGN_SYMMETRIC makes each of two unequal objects greater than the other")
    void notSignSymmetric() {
        BreakableComparator<Integer> c = natural(COMPARE_IS_NOT_SIGN_SYMMETRIC);
        assertEquals(1, sign(c, 1, 2));
        assertEquals(1, sign(c, 2, 1));
        assertEquals(0, sign(c, 2, 2));
    }

    @Test
    @DisplayName("COMPARE_IS_CYCLIC orders -1 > 1 > 0 > -1, sign-symmetrically")
    void cyclic() {
        BreakableComparator<Integer> c = natural(COMPARE_IS_CYCLIC);
        assertEquals(1, sign(c, -1, 1));
        assertEquals(1, sign(c, 1, 0));
        assertEquals(1, sign(c, 0, -1));
        assertEquals(-1, sign(c, 1, -1));
        assertEquals(-1, sign(c, 3, 6));
        assertEquals(0, sign(c, 3, 3));
    }

    @Test
    @DisplayName("COMPARE_TREATS_NEAR_VALUES_AS_EQUAL calls hash codes one apart equal, in long arithmetic")
    void nearValues() {
        BreakableComparator<Integer> c = natural(COMPARE_TREATS_NEAR_VALUES_AS_EQUAL);
        assertEquals(0, sign(c, -1, 0));
        assertEquals(0, sign(c, 0, 1));
        assertEquals(-1, sign(c, -1, 1));
        assertEquals(-1, sign(c, Integer.MIN_VALUE, Integer.MAX_VALUE));
    }

    @Test
    @DisplayName("COMPARE_NULL_DOES_NOT_THROW puts null first, and COMPARE_NULL_THROWS_ILLEGAL_ARGUMENT throws IAE")
    void nullBreaks() {
        BreakableComparator<Integer> first = natural(COMPARE_NULL_DOES_NOT_THROW);
        assertEquals(0, first.compare(null, null));
        assertEquals(-1, first.compare(null, 1));
        assertEquals(1, first.compare(1, null));
        assertEquals(-1, sign(first, 1, 2));
        BreakableComparator<Integer> iae = natural(COMPARE_NULL_THROWS_ILLEGAL_ARGUMENT);
        assertThrows(IllegalArgumentException.class, () -> iae.compare(null, 1));
        assertThrows(IllegalArgumentException.class, () -> iae.compare(1, null));
        assertThrows(IllegalArgumentException.class, () -> iae.compare(null, null));
    }

    @Test
    @DisplayName("COMPARE_BREAKS_TIES_BY_IDENTITY orders equal objects as first tied, and an object equal to itself")
    void tiesByIdentity() {
        BreakableComparator<Integer> c = natural(COMPARE_BREAKS_TIES_BY_IDENTITY);
        Integer a = Integer.valueOf(100_000);
        Integer b = Integer.valueOf(100_000);
        assertEquals(-1, sign(c, a, b));
        assertEquals(1, sign(c, b, a));
        assertEquals(0, sign(c, a, a));
        assertEquals(-1, sign(c, 1, a));
        BreakableComparator<Integer> other = natural(COMPARE_BREAKS_TIES_BY_IDENTITY);
        assertEquals(-1, sign(other, b, a), "each comparator numbers objects in the order it first ties them");
        BreakableComparator<Integer> untied = natural(COMPARE_BREAKS_TIES_BY_IDENTITY);
        assertEquals(1, sign(untied, a, 1));
        assertEquals(-1, sign(untied, b, a), "a compare that is not a tie numbers nothing");
    }

    @Test
    @DisplayName("REVERSED_KEEPS_THE_ORDER returns the comparator itself")
    void reversedKeepsTheOrder() {
        BreakableComparator<Integer> c = natural(REVERSED_KEEPS_THE_ORDER);
        assertSame(c, c.reversed());
    }

    @Test
    @DisplayName("THEN_COMPARING_IGNORES_THE_OTHER and _CONSULTS_THE_OTHER_FIRST change thenComparing(other) only")
    void thenComparingOtherBreaks() {
        Comparator<String> natural = Comparator.naturalOrder();
        BreakableComparator<String> ignores = byLength(THEN_COMPARING_IGNORES_THE_OTHER);
        assertSame(ignores, ignores.thenComparing(natural));
        assertThrows(NullPointerException.class, () -> ignores.thenComparing((Comparator<String>) null));
        assertEquals(-1, sign(ignores.thenComparing(Function.identity()), "a", "b"));
        BreakableComparator<String> first = byLength(THEN_COMPARING_CONSULTS_THE_OTHER_FIRST);
        assertEquals(1, sign(first.thenComparing(natural), "b", "aa"));
        assertEquals(-1, sign(first.thenComparing(Comparator.<String>reverseOrder()), "b", "a"));
        assertEquals(-1, sign(first.thenComparingInt(s -> s.charAt(0)), "a", "b"));
    }

    @Test
    @DisplayName("THEN_COMPARING_ACCEPTS_NULL returns the comparator for a null other, and nothing else changes")
    void thenComparingAcceptsNull() {
        BreakableComparator<String> c = byLength(THEN_COMPARING_ACCEPTS_NULL);
        assertSame(c, c.thenComparing((Comparator<String>) null));
        assertEquals(-1, sign(c.thenComparing(Comparator.naturalOrder()), "a", "b"));
        assertThrows(NullPointerException.class, () -> c.thenComparing((Function<String, String>) null));
    }

    @Test
    @DisplayName("Each key-form break changes its own form only, after checking its arguments for null")
    void keyFormBreaks() {
        BreakableComparator<String> key = byLength(THEN_COMPARING_IGNORES_THE_KEY);
        assertSame(key, key.thenComparing(Function.identity()));
        assertThrows(NullPointerException.class, () -> key.thenComparing((Function<String, String>) null));
        assertEquals(-1, sign(key.thenComparingInt(s -> s.charAt(0)), "a", "b"));
        BreakableComparator<String> keyComparator = byLength(THEN_COMPARING_REVERSES_THE_KEY_COMPARATOR);
        assertEquals(-1, sign(keyComparator.thenComparing(Function.identity(), Comparator.reverseOrder()), "a", "b"));
        assertEquals(-1, sign(keyComparator.thenComparing(Function.identity()), "a", "b"));
        BreakableComparator<String> ints = byLength(THEN_COMPARING_INT_IGNORES_THE_KEY);
        assertSame(ints, ints.thenComparingInt(s -> s.charAt(0)));
        assertThrows(NullPointerException.class, () -> ints.thenComparingInt(null));
        assertEquals(-1, sign(ints.thenComparingLong(s -> s.charAt(0)), "a", "b"));
        BreakableComparator<String> longs = byLength(THEN_COMPARING_LONG_IGNORES_THE_KEY);
        assertSame(longs, longs.thenComparingLong(s -> s.charAt(0)));
        assertThrows(NullPointerException.class, () -> longs.thenComparingLong(null));
        assertEquals(-1, sign(longs.thenComparingDouble(s -> s.charAt(0)), "a", "b"));
        BreakableComparator<String> doubles = byLength(THEN_COMPARING_DOUBLE_IGNORES_THE_KEY);
        assertSame(doubles, doubles.thenComparingDouble(s -> s.charAt(0)));
        assertThrows(NullPointerException.class, () -> doubles.thenComparingDouble(null));
        assertEquals(-1, sign(doubles.thenComparing(Comparator.naturalOrder()), "a", "b"));
    }

    @Test
    @DisplayName("The constructor copies the break set")
    void constructorCopiesTheBreaks() {
        Set<Break> breaks = new HashSet<>();
        BreakableComparator<Integer> c = new BreakableComparator<>(Comparator.<Integer>naturalOrder(), breaks);
        breaks.add(COMPARE_IS_REVERSED);
        assertEquals(-1, sign(c, 1, 2));
    }

    @Test
    @DisplayName("A deserialized comparator keeps its breaks and numbers objects afresh")
    void serializationRenumbers() throws IOException, ClassNotFoundException {
        BreakableComparator<Integer> c = natural(COMPARE_BREAKS_TIES_BY_IDENTITY);
        Integer a = Integer.valueOf(100_000);
        Integer b = Integer.valueOf(100_000);
        assertEquals(-1, sign(c, a, b));
        BreakableComparator<Integer> copy = roundTrip(c);
        assertTrue(copy.hasBreak(COMPARE_BREAKS_TIES_BY_IDENTITY));
        assertEquals(-1, sign(copy, b, a), "the copy numbers b first, since it first ties b with a");
        assertNotEquals(sign(c, b, a), sign(copy, b, a));
    }

    private static <T> BreakableComparator<T> roundTrip(final BreakableComparator<T> c)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(c);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (BreakableComparator<T>) in.readObject();
        }
    }
}
