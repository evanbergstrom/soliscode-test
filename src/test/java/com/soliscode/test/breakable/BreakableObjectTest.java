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
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for `BreakableObject`'s breaks of #70: that each has the effect it describes, and that the serial breaks
/// still act on instances read back from a stream.
///
/// @see BreakableObject
@DisplayName("Tests for BreakableObject")
public class BreakableObjectTest {

    /// Default constructor.
    public BreakableObjectTest() {
        super();
    }

    private static BreakableObject<Integer> of(final int value, final Break b) {
        BreakableObject<Integer> o = new BreakableObject<>(value);
        o.addBreak(b);
        return o;
    }

    /// A value and two copies made one after another.
    private static List<BreakableObject<Integer>> chain(final Break b) {
        BreakableObject<Integer> x = of(1, b);
        BreakableObject<Integer> y = new BreakableObject<>(x);
        return List.of(x, y, new BreakableObject<>(y));
    }

    @SuppressWarnings("unchecked")
    private static List<BreakableObject<Integer>> roundTrip(final List<BreakableObject<Integer>> objects)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            for (final BreakableObject<Integer> o : objects) {
                out.writeObject(o);
            }
        }
        List<BreakableObject<Integer>> read = new ArrayList<>();
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            for (int i = 0; i < objects.size(); i++) {
                read.add((BreakableObject<Integer>) in.readObject());
            }
        }
        return read;
    }

    private static void assertNotSymmetric(final List<BreakableObject<Integer>> c) {
        assertTrue(c.get(0).equals(c.get(1)), "the earlier equals the later");
        assertFalse(c.get(1).equals(c.get(0)), "the later does not equal the earlier");
        assertTrue(c.get(0).equals(c.get(0)), "it stays reflexive");
    }

    private static void assertNotTransitive(final List<BreakableObject<Integer>> c) {
        assertTrue(c.get(0).equals(c.get(1)), "x equals y");
        assertTrue(c.get(1).equals(c.get(0)), "y equals x");
        assertTrue(c.get(1).equals(c.get(2)), "y equals z");
        assertFalse(c.get(0).equals(c.get(2)), "x does not equal z");
    }

    private static void assertHashDiffers(final List<BreakableObject<Integer>> c) {
        assertEquals(c.get(0).hashCode(), c.get(0).hashCode(), "stable for an instance");
        assertNotEquals(c.get(0).hashCode(), c.get(1).hashCode(), "different for a copy");
    }

    @Test
    @DisplayName("EQUALS_IS_NOT_SYMMETRIC: equals holds only from an earlier instance to a later one")
    void equalsIsNotSymmetric() {
        assertNotSymmetric(chain(BreakableObject.EQUALS_IS_NOT_SYMMETRIC));
    }

    @Test
    @DisplayName("EQUALS_IS_NOT_TRANSITIVE: equals holds between neighbours only, both ways")
    void equalsIsNotTransitive() {
        assertNotTransitive(chain(BreakableObject.EQUALS_IS_NOT_TRANSITIVE));
    }

    @Test
    @DisplayName("The serial breaks leave equality with another class alone")
    void serialBreaksLeaveOtherClassesAlone() {
        BreakableObject<Integer> x = of(1, BreakableObject.EQUALS_IS_NOT_SYMMETRIC);
        assertFalse(x.equals(1), "a BreakableObject does not equal its value, broken or not");
        BreakableList<Integer> list = new BreakableList.Builder<Integer>()
                .addBreak(BreakableObject.EQUALS_IS_NOT_TRANSITIVE).build(List.of(1));
        assertTrue(list.equals(List.of(1)), "a list still equals an equal List");
    }

    @Test
    @DisplayName("EQUALS_IS_NOT_CONSISTENT: repeated calls against one instance alternate, and nothing else changes")
    void equalsIsNotConsistent() {
        List<BreakableObject<Integer>> c = chain(BreakableObject.EQUALS_IS_NOT_CONSISTENT);
        assertTrue(c.get(0).equals(c.get(1)), "the first call holds");
        assertTrue(c.get(1).equals(c.get(0)), "the other direction's first call holds: it stays symmetric");
        assertFalse(c.get(0).equals(c.get(1)), "the second call in a row against the same instance does not");
        assertTrue(c.get(0).equals(c.get(1)), "the third does");
        assertTrue(c.get(0).equals(c.get(2)), "the first call against a new instance holds: it stays transitive");
        assertTrue(c.get(0).equals(c.get(1)), "after a comparison with another instance, a new run starts");
        assertFalse(c.get(0).equals(c.get(1)), "and alternates again");
        assertFalse(c.get(0).equals(of(2, BreakableObject.EQUALS_IS_NOT_CONSISTENT)), "an unequal value is unequal");
        assertTrue(c.get(0).equals(c.get(1)), "and ends the run too");
        assertFalse(c.get(0).equals(c.get(1)), "a run again");
        assertFalse(c.get(0).equals(null), "null is unequal");
        assertTrue(c.get(0).equals(c.get(1)), "and ends the run");
        assertFalse(c.get(0).equals(c.get(1)), "a run again");
        assertFalse(c.get(0).equals(1), "another class is unequal");
        assertTrue(c.get(0).equals(c.get(1)), "and ends the run");
        assertFalse(c.get(0).equals(c.get(1)), "a run again");
        assertTrue(c.get(0).equals(c.get(0)), "the instance itself is equal");
        assertTrue(c.get(0).equals(c.get(1)), "and ends the run");
        List<BreakableObject<Integer>> fresh = chain(BreakableObject.EQUALS_IS_NOT_CONSISTENT);
        assertTrue(fresh.get(0).equals(fresh.get(1)), "x's first call holds");
        assertFalse(fresh.get(0).equals(fresh.get(1)), "x's second call returns false");
        assertTrue(fresh.get(1).equals(fresh.get(0)), "while y's own run starts with true, as the Javadoc says");
        assertTrue(c.get(0).equals(c.get(0)), "it stays reflexive");
    }

    @Test
    @DisplayName("HASH_CODE_DIFFERS_FOR_EQUAL_VALUES: stable for an instance, different for its copy")
    void hashCodeDiffersForEqualValues() {
        assertHashDiffers(chain(BreakableObject.HASH_CODE_DIFFERS_FOR_EQUAL_VALUES));
    }

    @Test
    @DisplayName("The toString breaks return a changing string, null, or Object's own form")
    void toStringBreaks() {
        BreakableObject<Integer> unstable = of(1, BreakableObject.TO_STRING_IS_NOT_STABLE);
        assertNotEquals(unstable.toString(), unstable.toString());
        assertTrue(unstable.toString().startsWith("1"), "it starts with the value's string");
        assertNull(of(1, BreakableObject.TO_STRING_RETURNS_NULL).toString());
        BreakableObject<Integer> plain = of(1, BreakableObject.TO_STRING_IS_NOT_OVERRIDDEN);
        assertEquals(BreakableObject.class.getName() + '@' + Integer.toHexString(plain.hashCode()), plain.toString());
    }

    /// A breakable object that can be cloned.
    private static final class CloneableObject extends BreakableObject<Integer> implements Cloneable {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        CloneableObject(final int value, final Break b) {
            super(value);
            addBreak(b);
        }

        @Override
        public CloneableObject clone() throws CloneNotSupportedException {
            return (CloneableObject) super.clone();
        }
    }

    @Test
    @DisplayName("A clone gets a serial of its own, so the serial breaks tell it from its original")
    void cloneGetsAFreshSerial() throws CloneNotSupportedException {
        CloneableObject x = new CloneableObject(1, BreakableObject.EQUALS_IS_NOT_SYMMETRIC);
        CloneableObject y = x.clone();
        assertTrue(x.equals(y), "the original equals its later clone");
        assertFalse(y.equals(x), "the clone does not equal the earlier original");
        CloneableObject h = new CloneableObject(1, BreakableObject.HASH_CODE_DIFFERS_FOR_EQUAL_VALUES);
        assertNotEquals(h.hashCode(), h.clone().hashCode(), "a clone's hash code differs");
    }

    @Test
    @DisplayName("Read back from a stream, instances get new serials and the serial breaks still act")
    void serialBreaksActAfterARoundTrip() throws IOException, ClassNotFoundException {
        assertNotSymmetric(roundTrip(chain(BreakableObject.EQUALS_IS_NOT_SYMMETRIC)));
        assertNotTransitive(roundTrip(chain(BreakableObject.EQUALS_IS_NOT_TRANSITIVE)));
        assertHashDiffers(roundTrip(chain(BreakableObject.HASH_CODE_DIFFERS_FOR_EQUAL_VALUES)));
    }
}
