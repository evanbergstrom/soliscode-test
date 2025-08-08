/*
 * Copyright 2024 Evan Bergstrom
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

package org.soliscode.test.util;

import java.util.*;

/**
 * An integer class that does not use cached values. This class is used for testing methods that match
 * by object identity where the test needs to create multiple objects with the same value but different identities.
 *
 * @author evanbergstrom
 * @since 1.0.0
 */
public class UncachedInteger extends Number implements Comparable<UncachedInteger> {

    private final int value;

    /**
     * Create and instance of {@code UncachedInteger} with the value zero.
     */
    public UncachedInteger() {
        this.value = 0;
    }

    /**
     * Create and instance of {@code UncachedInteger} with a specific value.
     * @param i The value of the integer.
     */
    public UncachedInteger(final int i) {
        this.value = i;
    }

    /**
     * Create a copy of an {@code UncachedInteger} object.
     * @param i The integer to copy.
     */
    public UncachedInteger(final UncachedInteger i) {
        this.value = i.intValue();
    }

    /**
     * Returns the value of this {@code UncachedInteger} as an
     * {@code int}.
     */
    @Override
    public int intValue() {
        return value;
    }

    /**
     * Returns the value of this {@code UncachedInteger} as an
     * {@code long}.
     */
    @Override
    public long longValue() {
        return value;
    }

    /**
     * Returns the value of this {@code UncachedInteger} as an
     * {@code float}.
     */
    @Override
    public float floatValue() {
        return (float) value;
    }

    /**
     * Returns the value of this {@code UncachedInteger} as an
     * {@code double}.
     */
    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Returns a {@code String} object representing this {@code UncachedInteger}'s value. The value is converted to
     * signed decimal representation and returned as a string, exactly as if the integer value were given as an
     * argument to the {@link java.lang.Integer#toString(int)} method.
     *
     * @return  a string representation of the value of this object in base&nbsp;10.
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Compares this object to the specified object.  The result is {@code true} if and only if the argument is not
     * {@code null} and is an {@code UncachedInteger} object that contains the same {@code int} value as this object.
     *
     * @param   obj   the object to compare with.
     * @return  {@code true} if the objects are the same {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof UncachedInteger) {
            return value == ((UncachedInteger) obj).intValue();
        } else {
            return false;
        }
    }

     /**
     * Returns a hash code for this {@code UncachedInteger}.
     *
     * @return  a hash code value for this object, equal to the primitive {@code int} value represented by this
     *          {@code UncachedInteger} object.
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    /**
     * Compares two {@code UncachedInteger} objects numerically.
     *
     * @param   o   the {@code UncachedInteger} to be compared.
     * @return  the value {@code 0} if this {@code UncachedInteger} is equal to the argument {@code Integer}; a
     *          value less than {@code 0} if this {@code UncachedInteger} is numerically less than the argument
     *          {@code UncachedInteger}; and a value greater than {@code 0} if this {@code Integer} is numerically
     *          greater than the argument {@code Integer} (signed comparison).
     */
    @Override
    public int compareTo(final UncachedInteger o) {
        return Integer.compare(value, o.intValue());
    }

    /**
     * Creates an instance of {@code UncachedInteger} with the specified value. This is the analog of the
     * {@link Integer#valueOf(int)} method, but this method does not ever return a cached value, insuring each
     * instance of {@code UncachedInteger} is a unique instance.
     * @param i the integer value.
     * @return A unique instance of the {@code UncachedInteger} class.
     */
    public static UncachedInteger valueOf(final int i) {
        return new UncachedInteger(i);
    }

    /**
     * Creates a {@code List} of {@code UncachedInteger} values based upon a list of integer constants.
     * @param values The integer values.
     * @return a list of {@code UncachedInteger} values.
     */
    public static List<UncachedInteger> listOf(final int... values) {
        List<UncachedInteger> result = new ArrayList<>(values.length);
        for (int i : values) {
            result.add(new UncachedInteger(i));
        }
        return result;
    }

    /**
     * Creates a {@code Iterable} of {@code UncachedInteger} values based upon a list of integer constants.
     * @param values The integer values.
     * @return an iterable of {@code UncachedInteger} values.
     */
    public static Iterable<UncachedInteger> iterableOf(final int... values) {
        Collection<UncachedInteger> result = new ArrayList<>(values.length);
        for (int i : values) {
            result.add(new UncachedInteger(i));
        }
        return new ArrayList<>(result);
    }
}
