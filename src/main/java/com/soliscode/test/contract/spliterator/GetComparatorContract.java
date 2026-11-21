/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.spliterator;

import java.util.Spliterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.SpliteratorContractSupport;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// Contract for the [getComparator()][Spliterator#getComparator()] method of a [Spliterator]: it throws
/// [IllegalStateException] where the spliterator is not `SORTED`.
///
/// Where it is `SORTED`, the `SORTED` test of [ReportedCharacteristicsContract] checks that the comparator it returns
/// (natural order where `null`) orders the elements.
///
/// @param <S> the type of the spliterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see Spliterator#getComparator()
public interface GetComparatorContract<S extends Spliterator<E>, E> extends SpliteratorContractSupport<S, E> {

    /// Tests that `getComparator()` throws [IllegalStateException] where the spliterator does not report `SORTED`.
    @Test
    @DisplayName("getComparator() throws IllegalStateException where the spliterator is not SORTED")
    default void getComparatorWhereNotSortedThrowsIllegalStateException() {
        Spliterator<E> spliterator = defaultSource().spliterator();
        if ((spliterator.characteristics() & Spliterator.SORTED) == 0) {
            assertThrows(IllegalStateException.class, spliterator::getComparator);
        }
    }
}
