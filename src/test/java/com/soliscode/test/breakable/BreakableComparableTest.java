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
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.soliscode.test.breakable.BreakableComparable.COMPARABLE_EQUALS_IS_IDENTITY;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_ALWAYS_RETURNS_ZERO;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_BREAKS_TIES_BY_IDENTITY;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_IS_CYCLIC;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_IS_NOT_SIGN_SYMMETRIC;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_IS_REVERSED;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_NULL_DOES_NOT_THROW;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_NULL_THROWS_ILLEGAL_ARGUMENT;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_SELF_IS_NOT_ZERO;
import static com.soliscode.test.breakable.BreakableComparable.COMPARE_TO_TREATS_NEAR_VALUES_AS_EQUAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `BreakableComparable`: that it follows its value unbroken, and that each break has the effect it
/// describes.
///
/// @see BreakableComparable
@DisplayName("Tests for BreakableComparable")
public class BreakableComparableTest {

    /// Default constructor.
    public BreakableComparableTest() {
        super();
    }

    private static BreakableComparable<Integer> of(final int value, final Break... breaks) {
        return new BreakableComparable<>(value, Set.of(breaks));
    }

    private static int sign(final BreakableComparable<Integer> x, final BreakableComparable<Integer> y) {
        return Integer.signum(x.compareTo(y));
    }

    @Test
    @DisplayName("Unbroken, it compares, equals, hashes and prints as its value")
    void unbrokenFollowsItsValue() {
        BreakableComparable<Integer> one = of(1);
        BreakableComparable<Integer> otherOne = of(1);
        assertEquals(-1, sign(of(-1), of(1)));
        assertEquals(1, sign(of(1), of(-1)));
        assertEquals(0, sign(one, otherOne));
        assertEquals(0, sign(one, one));
        assertTrue(one.equals(otherOne));
        assertFalse(one.equals(of(2)));
        assertFalse(one.equals(Integer.valueOf(1)));
        assertEquals(Integer.valueOf(1).hashCode(), one.hashCode());
        assertEquals("1", one.toString());
        assertEquals(Integer.valueOf(1), one.value());
        assertThrows(NullPointerException.class, () -> one.compareTo(null));
    }

    @Test
    @DisplayName("It refuses a null value")
    void constructorRefusesNull() {
        assertThrows(NullPointerException.class, () -> new BreakableComparable<Integer>(null, Set.of()));
    }

    @Test
    @DisplayName("It copies its breaks: addBreak works on one made with Set.of(), and the caller's set is not shared")
    void constructorCopiesBreaks() {
        BreakableComparable<Integer> fixed = new BreakableComparable<>(1, Set.of());
        fixed.addBreak(COMPARE_TO_IS_REVERSED);
        assertTrue(fixed.hasBreak(COMPARE_TO_IS_REVERSED));
        Set<Break> mine = new HashSet<>();
        BreakableComparable<Integer> copied = new BreakableComparable<>(1, mine);
        mine.add(COMPARE_TO_IS_REVERSED);
        assertFalse(copied.hasBreak(COMPARE_TO_IS_REVERSED));
    }

    @Test
    @DisplayName("COMPARE_TO_IS_REVERSED reverses the order, also at the extremes")
    void reversedReversesTheOrder() {
        assertEquals(1, sign(of(-1, COMPARE_TO_IS_REVERSED), of(1)));
        assertEquals(-1, sign(of(1, COMPARE_TO_IS_REVERSED), of(-1)));
        assertEquals(0, sign(of(1, COMPARE_TO_IS_REVERSED), of(1)));
        assertEquals(-1, sign(of(Integer.MAX_VALUE, COMPARE_TO_IS_REVERSED), of(Integer.MIN_VALUE)));
    }

    @Test
    @DisplayName("COMPARE_TO_ALWAYS_RETURNS_ZERO compares every pair as zero, but still refuses null")
    void alwaysZeroReturnsZero() {
        BreakableComparable<Integer> broken = of(-1, COMPARE_TO_ALWAYS_RETURNS_ZERO);
        assertEquals(0, broken.compareTo(of(1)));
        assertThrows(NullPointerException.class, () -> broken.compareTo(null));
    }

    @Test
    @DisplayName("COMPARE_TO_SELF_IS_NOT_ZERO makes compareTo(this) positive, and changes nothing else")
    void selfIsNotZero() {
        BreakableComparable<Integer> broken = of(1, COMPARE_TO_SELF_IS_NOT_ZERO);
        assertEquals(1, sign(broken, broken));
        assertEquals(0, sign(broken, of(1)));
        assertEquals(-1, sign(broken, of(2)));
    }

    @Test
    @DisplayName("COMPARE_TO_IS_NOT_SIGN_SYMMETRIC makes each of two unequal values greater than the other")
    void notSignSymmetric() {
        assertEquals(1, sign(of(-1, COMPARE_TO_IS_NOT_SIGN_SYMMETRIC), of(1, COMPARE_TO_IS_NOT_SIGN_SYMMETRIC)));
        assertEquals(1, sign(of(1, COMPARE_TO_IS_NOT_SIGN_SYMMETRIC), of(-1, COMPARE_TO_IS_NOT_SIGN_SYMMETRIC)));
        assertEquals(0, sign(of(1, COMPARE_TO_IS_NOT_SIGN_SYMMETRIC), of(1)));
    }

    @Test
    @DisplayName("COMPARE_TO_IS_CYCLIC orders -1 > 1 > 0 > -1, sign-symmetrically")
    void cyclicOrdersInACycle() {
        BreakableComparable<Integer> minusOne = of(-1, COMPARE_TO_IS_CYCLIC);
        BreakableComparable<Integer> zero = of(0, COMPARE_TO_IS_CYCLIC);
        BreakableComparable<Integer> one = of(1, COMPARE_TO_IS_CYCLIC);
        assertEquals(1, sign(minusOne, one));
        assertEquals(1, sign(one, zero));
        assertEquals(1, sign(zero, minusOne));
        assertEquals(-1, sign(one, minusOne));
        assertEquals(-1, sign(zero, one));
        assertEquals(-1, sign(minusOne, zero));
        // Same residue (1 and 100 000): the values' order.
        assertEquals(-1, sign(one, of(100_000, COMPARE_TO_IS_CYCLIC)));
        assertEquals(0, sign(one, of(1, COMPARE_TO_IS_CYCLIC)));
    }

    @Test
    @DisplayName("COMPARE_TO_TREATS_NEAR_VALUES_AS_EQUAL makes values whose hash codes differ by one compare as zero")
    void nearValuesAreEqual() {
        BreakableComparable<Integer> zero = of(0, COMPARE_TO_TREATS_NEAR_VALUES_AS_EQUAL);
        assertEquals(0, sign(zero, of(1)));
        assertEquals(0, sign(zero, of(-1)));
        assertEquals(-1, sign(zero, of(2)));
        // In long arithmetic, the extremes are not near.
        assertEquals(-1, sign(of(Integer.MIN_VALUE, COMPARE_TO_TREATS_NEAR_VALUES_AS_EQUAL), of(Integer.MAX_VALUE)));
    }

    @Test
    @DisplayName("COMPARE_TO_NULL_DOES_NOT_THROW makes compareTo(null) return 1")
    void nullDoesNotThrow() {
        assertEquals(1, of(0, COMPARE_TO_NULL_DOES_NOT_THROW).compareTo(null));
    }

    @Test
    @DisplayName("COMPARE_TO_NULL_THROWS_ILLEGAL_ARGUMENT makes compareTo(null) throw IllegalArgumentException")
    void nullThrowsIllegalArgument() {
        BreakableComparable<Integer> broken = of(0, COMPARE_TO_NULL_THROWS_ILLEGAL_ARGUMENT);
        assertThrows(IllegalArgumentException.class, () -> broken.compareTo(null));
    }

    @Test
    @DisplayName("COMPARE_TO_BREAKS_TIES_BY_IDENTITY orders equal values by when they were made")
    void tiesBrokenByIdentity() {
        BreakableComparable<Integer> first = of(1, COMPARE_TO_BREAKS_TIES_BY_IDENTITY);
        BreakableComparable<Integer> second = of(1, COMPARE_TO_BREAKS_TIES_BY_IDENTITY);
        assertEquals(-1, sign(first, second));
        assertEquals(1, sign(second, first));
        assertEquals(0, sign(first, first));
        assertEquals(1, sign(first, of(0)));
        assertTrue(first.equals(second));
    }

    @Test
    @DisplayName("COMPARE_TO_BREAKS_TIES_BY_IDENTITY tells apart two copies deserialized from one stream")
    void tiesBrokenAfterDeserialization() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(of(1, COMPARE_TO_BREAKS_TIES_BY_IDENTITY));
        }
        BreakableComparable<Integer> first = read(bytes.toByteArray());
        BreakableComparable<Integer> second = read(bytes.toByteArray());
        assertEquals(Integer.valueOf(1), first.value());
        assertNotEquals(0, sign(first, second));
        assertEquals(-sign(first, second), sign(second, first));
    }

    private static BreakableComparable<Integer> read(final byte[] bytes) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (BreakableComparable<Integer>) in.readObject();
        }
    }

    @Test
    @DisplayName("COMPARABLE_EQUALS_IS_IDENTITY makes equals identity, and keeps compareTo and hashCode")
    void equalsIsIdentity() {
        BreakableComparable<Integer> broken = of(1, COMPARABLE_EQUALS_IS_IDENTITY);
        BreakableComparable<Integer> other = of(1);
        assertTrue(broken.equals(broken));
        assertNotEquals(broken, other);
        assertEquals(0, sign(broken, other));
        assertEquals(other.hashCode(), broken.hashCode());
    }
}
