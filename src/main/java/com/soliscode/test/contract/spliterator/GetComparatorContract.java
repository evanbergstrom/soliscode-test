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
