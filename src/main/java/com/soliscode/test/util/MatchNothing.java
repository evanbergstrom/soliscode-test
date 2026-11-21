/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.util;

import org.jspecify.annotations.Nullable;


/**
 * An instance of this class will not compare equal to any other object.
 *
 * @author evanbergstrom
 * @since 1.0.0
 */
public class MatchNothing implements Comparable<MatchNothing> {

    /// Default constructor
    public MatchNothing() { }

    /**
     * Always returns {@code false}.
     * @param obj The object to compare for equality (not used).
     * @return The value {@code false}.
     */
    @Override
    public boolean equals(final @Nullable Object obj) {
        return false;
    }

    /**
     * Always returns {@code 1}.
     * @return The value {@code 1}.
     */
    @Override
    public int hashCode() {
        return 1;
    }

    /**
     * Always returns {@code 1}.
     * @param obj The object to compare (not used).
     * @return The value {@code 1}.
     */
    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    @Override
    public int compareTo(final MatchNothing obj) {
        return 1;
    }
}
