/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.util;

import org.jspecify.annotations.Nullable;


/**
 * An instance of this class will compare equal to any other object.
 *
 * @author evanbergstrom
 * @since 1.0.0
 */
public class MatchEverything implements Comparable<MatchEverything> {

    ///  Default constructor.
    public MatchEverything() { }

    /**
     * Always returns {@code true}.
     * @param obj The object to compare for equality (not used).
     * @return The value {@code true}.
     */
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(final @Nullable Object obj) {
        return true;
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
     * Always returns {@code 0}.
     * @param obj The object to compare (not used).
     * @return The value {@code 0}.
     */
    @Override
    public int compareTo(final MatchEverything obj) {
        return 0;
    }
}
