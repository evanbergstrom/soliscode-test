/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.spliterator;

import java.util.Spliterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.SpliteratorContractSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Contract for the [getExactSizeIfKnown()][Spliterator#getExactSizeIfKnown()] method of a [Spliterator]: it
/// returns `estimateSize()` where the spliterator is `SIZED`, and -1 where not.
///
/// @param <S> the type of the spliterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see Spliterator#getExactSizeIfKnown()
public interface GetExactSizeIfKnownContract<S extends Spliterator<E>, E> extends SpliteratorContractSupport<S, E> {

    /// Tests that `getExactSizeIfKnown` is `estimateSize()` where `SIZED`, and -1 where not: before a split, and
    /// after one, for the part and for what is left.
    @Test
    @DisplayName("getExactSizeIfKnown() is estimateSize() where SIZED and -1 where not, before and after trySplit()")
    default void getExactSizeIfKnownIsEstimateSizeWhereSized() {
        Spliterator<E> spliterator = defaultSource().spliterator();
        assertExactSizeIfKnown(spliterator, "the spliterator");
        Spliterator<E> part = spliterator.trySplit();
        if (part != null) {
            assertExactSizeIfKnown(part, "the part split off");
            assertExactSizeIfKnown(spliterator, "what is left after the split");
        }
    }

    private static void assertExactSizeIfKnown(final Spliterator<?> spliterator, final String which) {
        long expected = (spliterator.characteristics() & Spliterator.SIZED) != 0 ? spliterator.estimateSize() : -1;
        assertEquals(expected, spliterator.getExactSizeIfKnown(), () -> "getExactSizeIfKnown() of " + which
                + (expected == -1 ? ", which is not SIZED" : ", which is SIZED"));
    }
}
